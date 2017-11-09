/******************************************************************************
* Title: MainHandler.java
* Author: Mike Schoonover
* Date: 01/15/15
*
* Purpose:
*
* This class is the main handler for all hardware interfaces. It provides
* methods to initialize devices and for sending and receiving data to and from
* those devices.
*
* It also generates simulated data for testing and demonstration purposes.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import controller.MainController;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.IniFile;
import model.SharedSettings;
import view.LogPanel;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainHandler
//

public class MainHandler
{

    private final int handlerNum;
    private final MainController mainController;
    private final IniFile configFile;
    private final SharedSettings sharedSettings;
    
    static public int SCAN = 0, INSPECT = 1, STOPPED = 2;
    static public int INSPECT_WITH_TIMER_TRACKING = 3, PAUSED = 4;
    private int opMode = STOPPED;

    LogPanel logPanel;

    private int numDevices;
    private int maxNumChannels;
    public int getMaxNumChannels() { return maxNumChannels; }

    private boolean hdwParamsDirty = false;
    public boolean getHdwParamsDirty(){ return hdwParamsDirty; }
    public void setHdwParamsDirty(boolean pState){ hdwParamsDirty = pState;}

    int peakScanDev;
    int peakScanCh;

    Device devices[];
    public Device[] getDevices(){ return(devices); }
    ArrayList<Boolean> deviceSimModes = new ArrayList<>();
    ArrayList<String> deviceTypesSimulated = new ArrayList<>();
    ArrayList<String> deviceTypes = new ArrayList<>();
    
    private ControlDevice controlDevice;
    private String nameOfDeviceToUseForControl;

    private boolean allDevicesSimulatedOverride;

    public boolean ready = false;
    
    //true means monitor mode active, false means not
    private boolean monitorStatus = false;
    
    private boolean opModeChanged = false;
    
    //START control vars
    
    private String msg = "";
    
    private int scanRateCounter = 0;
    
    private String encoderHandlerName;
    
    private InspectControlVars inspectCtrlVars;
    
    private EncoderCalValues encoderCalValues;
    
    private HardwareVars hdwVs;
    private boolean manualInspectControl = false;
    private boolean flaggingEnabled = false;
    
    private final DecimalFormat decFmt0x0 = new DecimalFormat("0.0");
    
    private boolean prepareForNewPiece;
    public boolean needToPrepareForNewPiece() { return prepareForNewPiece; }
    public void setPrepareForNewPiece(boolean pPrep) { prepareForNewPiece = pPrep; }
    
    private boolean readyToAdvanceInsertionPoints = false;
    
    private EncoderHandler encoders;
    private EncoderValues encoderValues;
    
    private double previousTally = 0.0;

    //number of counts each encoder moves to trigger an inspection data packet
    //these values are read later from the config file
    private int encoder1DeltaTrigger, encoder2DeltaTrigger;
    
    //END control vars

    private static final int LONG = 0;  //longitudinal system
    private static final int TRANS = 1; //transverse system
    private static final int WALL = 2;  //wall system

//-----------------------------------------------------------------------------
// MainHandler::MainHandler (constructor)
//

public MainHandler(int pHandlerNum, MainController pMainController,
                           SharedSettings pSharedSettings, IniFile pConfigFile)
{

    handlerNum = pHandlerNum; mainController = pMainController;
    sharedSettings = pSharedSettings; configFile = pConfigFile;

}//end of MainHandler::MainHandler (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{    

    loadConfigSettings();
    
    hdwVs = new HardwareVars(configFile); hdwVs.init();
    setEncoderHandler();
    
    inspectCtrlVars = new InspectControlVars(encoders); inspectCtrlVars.init();
    encoderCalValues = new EncoderCalValues(); encoderCalValues.init();

    //add one logging master panel for the main handler to use

    ArrayList<LogPanel> logPanels = mainController.setupDeviceLogPanels(1,true);
    logPanel = logPanels.get(0); logPanel.setTitle("Device Handler");

    //set up a logging text panel so each device can display messages
    logPanels = mainController.setupDeviceLogPanels(numDevices, false);

    //create the handlers for the different remote hardware data acquisition
    //and control boards

    setUpDevices(logPanels);

}// end of MainHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::setEncoderHandler
//
// Creates and initiaizes the encoder hander object base on settings read
// from the config file.
//

private void setEncoderHandler()
{

    switch(encoderHandlerName){

        case "Linear and Rotational" :
            encoders = new EncoderLinearAndRotational(hdwVs.encoderValues, null);
        break;

        case "Encoder Dual Linear" :
            encoders = new EncoderDualLinear(hdwVs.encoderValues, null);
        break;

        default:
            encoders = new EncoderLinearAndRotational(hdwVs.encoderValues,null);
        break;

    }

    encoders.init();

}//end of MainHandler::setEncoderHandler
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::connectToDevices
//
// Establishes communication link between Device objects and the hardware
// devices.
//
// Should be done in a thread other than the Java Event Handler Thread so the
// process can display status messages during execution.
//

public void connectToDevices()
{

    logPanel.appendTS("Searching for devices...\n\n");

    boolean status;

    status = findAndConnectToDevices();

    ready = status; //if set true, devices are ready for use

}// end of MainHandler::connectToDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::setUpDevices
//
// Creates and sets up the devices for communicating with the data acquisition
// and control boards. Each device will be connected with a data logging
// text panel in the pLogPanels list.
//
// The type of class to create for each device is determined by the string
// in the deviceTypes list.
//

private void setUpDevices(ArrayList<LogPanel> pLogPanels)
{

    devices = new Device[numDevices];

    int index = 0;

    LogPanel logIter;

    ListIterator iDevType = deviceTypes.listIterator();
    ListIterator iLogPanel = pLogPanels.listIterator();

    while(iDevType.hasNext()){

        if(iLogPanel.hasNext()){ logIter = (LogPanel)iLogPanel.next(); }
        else { logIter = null; }

        devices[index] = createDevice((String) iDevType.next(), index,
                                        logIter, configFile, sharedSettings,
                                       (boolean)deviceSimModes.get(index));
        devices[index].init();
        index++;
    }
    
    configureControlDevice();

}// end of MainHandler::setUpDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::createDevice
//
// Creates and returns an object of a child class of Device. The particular
// child class is specified by pDeviceType. The new object will be assigned
// an index number of pIndex for its internal use and a reference to a
// configuration file of pConfigFile.
//
// Each device will be given a link to a logging text panel on which it can
// display messages.
//
// The pSimMode value will be passed on to the device.
//

private Device createDevice(String pDeviceType, int pIndex, LogPanel pLogPanel,
                            IniFile pConfigFile, SharedSettings pSettings,
                            boolean pSimMode)
{

    switch(pDeviceType){

        case "MultiIO Longitudinal" : return (new
            Multi_IO_A_Longitudinal(pIndex, pLogPanel, pConfigFile,
                                            pSettings, pSimMode));

        case "MultiIO Transverse" : return (new
              Multi_IO_A_Transverse(pIndex, pLogPanel, pConfigFile,
                                            pSettings, pSimMode));

        case "MultiIO Wall" : return (new
                    Multi_IO_A_Wall(pIndex, pLogPanel, pConfigFile,
                                            pSettings, pSimMode));

        case "MultiIO Control" : return (new
                    Multi_IO_A_Control(pIndex, pLogPanel, pConfigFile,
                                            pSettings, pSimMode));

        default: return(null);

    }

}// end of MainHandler::createDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::findAndConnectToDevices
//
// Makes initial contact with all devices and makes an ethernet connection
// with each.
//
// Returns true if all device objects properly connected to matching found
// devices.
// Returns false on error.
//

private boolean findAndConnectToDevices()
{

    try{

        HashMap<InetAddress, String>ipToDeviceTypeMap = new HashMap<>();

        findDevices(ipToDeviceTypeMap);

        logPanel.appendTS("\nAll devices found.\n\n");

        assignFoundDevicesToDeviceObjects(ipToDeviceTypeMap);

        connectToFoundDevices();

    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 227");
        logPanel.appendTS("Error: " + e.getMessage() + " - Error: 247\n\n");
        logPanel.appendTS("Error: Some devices were not\n"
                        + "found or set up properly!\n\n");
        return(false);
    }

    return(true);

}// end of MainHandler::findAndConnectToDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::findDevices
//
// Makes initial contact with all devices and determines their IP addresses.
// The IP addresses and board
//

private void findDevices(HashMap<InetAddress, String> pIPToDeviceTypeMap)
                                           throws SocketException , IOException
{

    SocketSet socketSet = new SocketSet("Device Query");

    try{

        NetworkInterface networkInterface;

        networkInterface = findNetworkInterface();

        findDevicesAndCollectIPAddresses(
                               socketSet, networkInterface, pIPToDeviceTypeMap);

        if(pIPToDeviceTypeMap.size() < numDevices){
            throw new IOException("Some devices not found!"); }

    }
    finally{
        socketSet.closeAll();
    }

}// end of MainHandler::findDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::findDevicesAndCollectIPAddresses
//
// Opens a TCP/IP connection with the Control Board.
//
// To find the boards, first makes a UDP connection via the network interface
// pNetworkInterface. If there are multiple interfaces in the system, such as
// an Internet connection, the UDP broadcasts will fail unless tied to the
// interface connected to the remotes.
//

private void findDevicesAndCollectIPAddresses(SocketSet pSocketSet,
        NetworkInterface pNetworkInterface,
        HashMap<InetAddress, String> pIPToDeviceTypeMap) throws IOException
{

    logPanel.appendTS("Broadcasting to all devices...\n");

    openMulticastSocket(pSocketSet, pNetworkInterface);

    if (!pSocketSet.checkIfASocketOpened()){ throw new IOException(); }

    setupSocketAndDatagram(pSocketSet);

    broadcastAndCollectResponses(pSocketSet, pIPToDeviceTypeMap);

}//end of MainHandler::findDevicesAndCollectIPAddresses
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::broadcastAndCollectResponses
//

private void broadcastAndCollectResponses(SocketSet pSocketSet,
             HashMap<InetAddress, String> pIPToDeviceTypeMap) throws IOException
{

    int loopCount = 0;

    //broadcast the roll call greeting several times, checking for responses
    //each time - quit when expected number of unique devices have responded

    while(loopCount++ < 5 && pIPToDeviceTypeMap.size() < numDevices){

        pSocketSet.sendOutPacket(); //broadcast the query

        waitSleep(1000); //sleep to delay between broadcasts

        collectResponses(pSocketSet, pIPToDeviceTypeMap);

    }

}//end of MainHandler::broadcastAndCollectResponses
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler:collectResponses
//
// Monitors the socket for responses from Devices and adds any responders to
// a list. Some devices may respond multiple times as the broadcast message is
// repeated to ensure that it is received. Each response is added to the list
// only once.
//
// The collection loop executes the number of times equal to the number of
// expected devices.
//
// It is common that between the time the broadcast message is sent and the
// time this method begins collecting responses, all devices will have responded
// and their packets will be queued in the socket's buffer. In that case, they
// will all be read before this function exits. Since the receive method waits
// a bit before timing out, this gives even more time for responses.
//
// If the timeout is set to 1 second (typical), usually at least one device
// will have responded in that time. The second device thus has 2 seconds to
// respond, and so forth. Thus all devices will usually be collected in the
// first pass.
//
// If some devices do not respond quickly or did not receive the broadcast,
// this method will time out as well and another broadcast message should be
// sent by the calling method. This method can then be called again to collect
// any late coming responses.
//

private void collectResponses(SocketSet pSocketSet,
            HashMap<InetAddress, String> pIPToDeviceTypeMap) throws IOException
{

    String response;
    byte[] inBuf = new byte[256];
    DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);
    int loopCount = 0;


    boolean status;

    while(loopCount++ < numDevices){

        status = pSocketSet.receive(inPacket);

        if(status){

            String ipAddrS = inPacket.getAddress().toString();

            //get response string sent by device
            response = new String(inPacket.getData(), 0, inPacket.getLength());

            //display the response string from the remote -- any device might
            //respond several times before the process is complete

            logPanel.appendTS(ipAddrS + "  " + response + "\n");

            pIPToDeviceTypeMap.put(inPacket.getAddress(), response);

        }
    }

}//end of MainHandler::collectResponses
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler:findNetworkInterface
//
// Finds the network interface for communication with the remotes. Returns
// null if no suitable interface found.
//
// The first interface which is connected and has an IP address beginning with
// 169.254.*.* is returned.
//
// NOTE: If more than one interface is connected and has a 169.254.*.*
// IP address, the first one in the list will be returned. Will need to add
// code to further differentiate the interfaces if such a set up is to be
// used. Internet connections will typically not have such an IP address, so
// a second interface connected to the Internet will not cause a problem with
// the existing code.
//
// If a network interface is not specified for the connection, Java will
// choose the first one it finds. The TCP/IP protocol seems to work even if
// the wrong interface is chosen. However, the UDP broadcasts for wake up calls
// will not work unless the socket is bound to the appropriate interface.
//
// If multiple interface adapters are present, enabled, and running (such as
// an Internet connection), it can cause the UDP broadcasts to fail unless
// they are directed to the proper interface.
//

public NetworkInterface findNetworkInterface() throws SocketException
{

    logPanel.appendTS("");

    NetworkInterface iFace = null;

    logPanel.appendTS("Full list of Network Interfaces:" + "\n\n");
    for (Enumeration<NetworkInterface> en =
          NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

        NetworkInterface intf = en.nextElement();
        logPanel.appendTS("    " + intf.getName() + " " +
                                            intf.getDisplayName() + "\n");

        for (Enumeration<InetAddress> enumIpAddr =
                 intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

            String ipAddr = enumIpAddr.nextElement().toString();

            logPanel.appendTS("        " + ipAddr + "\n");

            if(ipAddr.startsWith("/169.254")){
                iFace = intf;
                logPanel.appendTS("^^==>> Binding to above adapter...^^\n");
                logPanel.appendTS("====================================\n");
            }
        }
    }

    logPanel.appendTS("\n");

    if(iFace == null){
        logPanel.appendTS("WARNING: no viable adapter found, using default.\n"
        + "System may not find devices!\n\n");
    }

    return(iFace);

}//end of MainHandler::findNetworkInterface
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::openMulticastSocket
//
// Opens a Multicast socket which allows messages to be broadcast to multiple
// receiving devices via ethernet without knowing their IP addresses.
//
// If one or more devices are not configured to be simulated, an ethernet
// socket is opened. If one or more devices are configured to be simulated,
// a simulated socket is opened.
//
// The socket and simulated socket reference are passed via pSocketSet.
//
// Both an ethernet socket and a simulated socked may be opened if there are
// some devices simulated and some not.
//

private void openMulticastSocket(SocketSet pSocketSet,
                                            NetworkInterface pNetworkInterface)
                                            throws SocketException, IOException
{

    boolean someDevicesNotSimulated = checkIfAnyDevicesNotSimulated();
    boolean someDevicesSimulated = checkIfAnyDevicesSimulated();

    if (someDevicesNotSimulated) {
        pSocketSet.socket = new MulticastSocket(4445);
        if (pNetworkInterface != null) {
            pSocketSet.socket.setNetworkInterface(pNetworkInterface);
        }
    }

    if (someDevicesSimulated) {
        pSocketSet.simSocket = new UDPSimulator(4445, deviceTypesSimulated);
    }

}// end of MainHandler::openMulticastSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler:assignFoundDevicesToDeviceObjects
//
// Assigns each device found to a Device object.
//
// Throws IOException if there are more found hardware devices than Device
// objects or vice versa.
//

private void assignFoundDevicesToDeviceObjects(
            HashMap <InetAddress, String>pIPToDeviceTypeMap) throws IOException
{

    int numFound = pIPToDeviceTypeMap.size();
    String msg;

    if(numFound < numDevices){
        msg = "Only " + numFound + " of " + numDevices
                                                     + " expected were found.";
        logSevere(msg + " Error: 448"); logPanel.appendTS(msg + "\n\n");
        throw(new IOException(msg));
    }

    if(numFound > numDevices){
        msg = "" + numFound + " devices were found when only "
                                            + numDevices + " were expected.";
        logSevere(msg + " Error: 448"); logPanel.appendTS(msg + "\n\n");
        throw(new IOException(msg));
    }

    //connect every found device to a device object with a matching Device Type
    //there may be multiple devices with the same Device Type, each will be
    //assigned to a different Device object matching that type in no particular
    //order of assignment

    for(Map.Entry<InetAddress, String> entry : pIPToDeviceTypeMap.entrySet()){

        for(Device device : devices){

            if(device.getIPAddr() == null
                    && entry.getValue().startsWith(device.getDeviceType())){

                device.setIPAddr(entry.getKey());
                break;
            }
        }
    }

    //scan through device objects checking to see if any were not assigned to
    //a found device, usually due to mismatched Device Type strings in the
    //config file vs the device itself

    for(Device device : devices){

        if(device.getIPAddr() == null){
            msg = "Missing a device of type: " + device.getDeviceType();
            throw(new IOException(msg));
        }
    }

}// end of MainHandler::assignFoundDevicesToDeviceObjects
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler:connectToFoundDevices
//
// Connects all Device objects to the hardware devices via ethernet.
//
// Returns true if all devices connected, false otherwise.
// debug mks -- need to add code to return false on failure?
//

public boolean connectToFoundDevices()
{

    for(Device device : devices){

        //pass the Runnable interfaced Device object to a thread and
        //the run function of will complete connection tasks

        Thread thread = new Thread(device, "Device " + device.getDeviceNum());
        thread.start();

    }

    boolean allDevicesConnected = true;

    //wait a while for each object to complete its connection
    for(int i=0; i<5; i++){
        allDevicesConnected = true;
        for (Device device : devices) {
            if (!device.getConnectionSuccessful()) {
                allDevicesConnected = false;
            }
        }
        if(allDevicesConnected) { break; }
        waitSleep(500);
    }

    if(allDevicesConnected){
        logPanel.appendTS("All devices are connected.\n\n");
    }else{
        logPanel.appendTS("Error: Some devices did not connect!\n\n");
    }

    return(allDevicesConnected);

}//end of MainHandler::connectToFoundDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::setupSocketAndDatagram
//
// Prepares the socket and DataGramPacket for use.
//

private void setupSocketAndDatagram(SocketSet pSocketSet)
                                   throws UnknownHostException, SocketException
{

    pSocketSet.group = InetAddress.getByName("230.0.0.1");

    pSocketSet.outPacket = new DatagramPacket(pSocketSet.outBuf,
                            pSocketSet.outBuf.length, pSocketSet.group, 4446);

    //force socket.receive to return if no packet available within 1 second
    pSocketSet.setSoTimeout(1000);

}// end of MainHandler::setupSocketAndDatagram
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::configureControlDevice
//
// Properly sets the control device to a device found in a list of devices or
// creates a timer driven control device object to use if necessary.
//

private void configureControlDevice()
{
     
    if (sharedSettings.timerDrivenTracking) { createControlDeviceTimerDriven(); }
    else { findControlDevice(); }

}// end of MainHandler::configureControlDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::createControlDeviceTimerDriven
//
// If necessary, cretates a control device as if it was read from the IniFile.
//
// This helps with using legacy ini file formats brought over from Chart.
//

private void createControlDeviceTimerDriven()
{
    
    //make sure a timer driven control device is present if desired operation 
    //is timer driven tracking and one was specifically created for the purpose
    ControlDeviceTimerDriven dev = new ControlDeviceTimerDriven(sharedSettings);
    dev.init();   
        
    if (sharedSettings.timerDrivenTracking) { 
        controlDevice = dev; 
    }
    
}// end of MainHandler::createControlDeviceTimerDriven
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::findControlDevice
//
// Finds and sets the control device. Sends error and success messages to the 
// log panel.
//

private void findControlDevice()
{
   
    String foundMsg = "";
    
    boolean success = false;
       
    boolean found = false;
    for (Device d : devices) {

        if (d.getTitle().equals(nameOfDeviceToUseForControl)) {

            found = true;

            if (!d.canBeControlDevice()) {
                foundMsg += "Error Finding Control Device: Device " 
                                + nameOfDeviceToUseForControl 
                                + " cannot be used as a control device.\n";
            } else { controlDevice = d; success = true; }

        }

    }

    if (!found) {
        foundMsg += "Error Finding Control Device: Device " 
                + nameOfDeviceToUseForControl 
                + " was not found in list of devices.\n";
    }
    
    if (success) { foundMsg = "Control devices successfully configured.\n"; }

    logPanel.appendTS(foundMsg);

}// end of MainHandler::findControlDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::collectData
//
// Collects data from all source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//
// Also processes any changes made to values such as channel parameters which
// need to be sent to the hardware devices.
//
// Also drives the simulation functions for any devices in simulation mode.
//

public void collectData()
{

    handleSettingsChanges(); //process if a new op mode has been set
    
    processChannelParameterChanges(); //process updated values
    
    if (opMode == SCAN || opMode == INSPECT_WITH_TIMER_TRACKING) {
        collectDataForScanOrTimerMode();
    }
    else if (opMode == INSPECT) {
        collectDataForInspectMode();
    }

    for(Device device : devices){ device.collectData(); }

    for(Device device : devices){ device.driveSimulation(); }

}// end of MainHandler::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::collectDataForScanOrTimerMode
//
// Currently does nothing.
//
// This function is specifically for SCAN and INSPECT_WITH_TIMER_TRACKING modes
// which use a timer to drive the traces rather than hardware encoder inputs.
//
// Peak data is requested periodically rather than being requested when the
// encoder position dictates such.
//

private void collectDataForScanOrTimerMode()
{
    
    //do nothing if not time to update
    if (scanRateCounter-- == 0){ 
        scanRateCounter = 10 - sharedSettings.scanSpeed; 
    }
    else { 
        return; 
    }

    //made it to here, so assume we can move forward
    readyToAdvanceInsertionPoints = true;

}//end of MainHandler::collectDataForScanOrTimerMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::collectDataForInspectMode
//
// Collects analog data from all channels and stores it in the appropriate trace
// buffers, collects encoder and digital I/O inputs and processes as necessary.
//
// This function is specifically for INSPECT mode which uses encoder data to
// drive the traces rather than a timer.
//
// Peak data is requested each time the encoder moves the specified tigger
// amount.
//

private void collectDataForInspectMode()
{

    //process position information from whatever device is handling the encoder
    //inputs

    collectEncoderDataInspectMode();

}//end of MainHandler::collectDataForInspectMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::collectEncoderDataInspectMode
//
// Collects and processes encoder and digital inputs.
//
// Returns true if new data has been processed.
//
// If the encoder has reached the next array position, chInfo[?].nextIndex++
// is updated so that data will be placed in the next position.  It takes
// a set amount of encoder counts to reach the next array position as specified
// by the calibration value in the configuration file.
//

boolean collectEncoderDataInspectMode()
{

    //do nothing until a new Inspect packet is ready
    if (!controlDevice.getNewInspectDataReady()) { return false; }

    //debug mks -- could a new packet be received between the above line and the
    //setting of the flag below -- miss a packet? -- packet processing in this
    //same thread then no prob -- different thread, then problem

    //ignore further calls to this function until a new packet is ready
    controlDevice.setNewInspectDataReady(false);

    //retrieve all the info related to inpection control -- photo eye status,
    //encoder values, etc.
    controlDevice.getInspectControlVars(inspectCtrlVars);

    //On entering INSPECT mode, the system will wait until signalled that the
    //head is off the pipe or the pipe is out of the system, then it will wait
    //until the head is on the pipe or pipe enters the system before moving the
    //traces

    // manual control option will override signals from the Control Board and
    // begin inspection immediately after the operator presses the Inspect
    // button should manual control option be removed after fixing XXtreme unit?

    //if waiting for piece clear of system, do nothing until flag says true
    if (hdwVs.waitForOffPipe){

        if (manualInspectControl) {inspectCtrlVars.onPipeFlag = false;}

        if (inspectCtrlVars.onPipeFlag) {return false;}
        else {
            //piece has been removed; prepare for it to enter to begin
            hdwVs.waitForOffPipe = false;
            hdwVs.waitForOnPipe = true;
            displayMsg("system clear, previous tally = " + 
                                       decFmt0x0.format(previousTally));
            previousTally = 0;
            
            }
        }

    if (manualInspectControl) {inspectCtrlVars.onPipeFlag = true;}

    //if waiting for piece to enter the head, do nothing until flag says true
    if (hdwVs.waitForOnPipe){

        if (!inspectCtrlVars.onPipeFlag) {return false;}
        else {
            hdwVs.waitForOnPipe = false; hdwVs.watchForOffPipe = true;

            //the direction of the linear encoder at the start of the inspection
            //sets the forward direction (increasing or decreasing encoder
            //count)
            encoders.setCurrentLinearDirectionAsFoward();            
            
            initializePlotterOffsetDelays(
                                    encoders.getDirectionSetForLinearFoward());
            
            //set the text description for the direction of inspection
            if (encoders.getDirectionSetForLinearFoward() == 
                                                  encoders.getAwayDirection()) {
                sharedSettings.inspectionDirectionDescription
                        = sharedSettings.awayFromHome;
            }
            else {
                sharedSettings.inspectionDirectionDescription
                        = sharedSettings.towardsHome;
            }

            encoders.resetAll();
            
            //record the value of linear encoder at start of inspection
            encoders.recordLinearStartCount();
            displayMsg("entry eye blocked...");
        }
    }

    if (manualInspectControl){
        inspectCtrlVars.head1Down = true;
        inspectCtrlVars.head2Down = true;
        inspectCtrlVars.head3Down = true;
    }
        
    //watch for piece to exit head
    if (hdwVs.watchForOffPipe){
        if (!inspectCtrlVars.onPipeFlag){

            //use tracking counter to delay after leading photo eye cleared
            //until position where modifier is to be added until the end of
            //the piece
            hdwVs.nearEndOfPieceTracker = hdwVs.nearEndOfPiecePosition;
            //start counting down to near end of piece modifier apply start
            //position
            hdwVs.trackToNearEndofPiece = true;
            //calculate length of tube
            controlDevice.requestAllEncoderValuesPacket();

            hdwVs.measuredLength = encoders.calculateTally();

            previousTally = hdwVs.measuredLength;
            
            hdwVs.watchForOffPipe = false;

            //set flag to force preparation for a new piece
            prepareForNewPiece = true;
            
            displayMsg("exit eye cleared, tally = " + 
                                        decFmt0x0.format(hdwVs.measuredLength));

        }//if (!inspectCtrlVars.onPipeFlag)
    }//if (hdwVs.watchForOffPipe)

    boolean newPositionData = true;  //signal that position has been changed

    //check to see if encoder hand over should occur
    encoders.handleEncoderSwitchOver();
    
    //made it to here, so assume we can move forward
    readyToAdvanceInsertionPoints = true;

    return newPositionData;

}//end of MainHandler::collectEncoderDataInspectMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::initializePlotterOffsetDelays
//
// Initializes the plotter start delays for the different types of plotter
// objects. See the notes in each method called for more details.

public void initializePlotterOffsetDelays(int pDirection)
{

    /*//DEBUG HSS//initializeTraceOffsetDelays(pDirection);

    analogDriver.initializeMapOffsetDelays(
                                      pDirection, encoders.getAwayDirection());*/

}//end of MainHandler::initializePlotterOffsetDelays
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::initializeTraceOffsetDelays
//
// Sets the trace start delays so the traces don't start until their associated
// sensors reach the pipe.
//
// The distance is set depending on the direction of inspection.  Some systems
// have different photo eye to sensor distances depending on the direction
// of travel.
//
// The delay is necessary because each sensor may be a different distance from
// the photo-eye which detects the start of the pipe.
//
// Two sets of values are stored:
//
// The distance of each sensor from the front edge of its head.
// The front edge of the head is the edge which reaches the inspection piece
// first when the carriage is moving away from the operator's station
// (the "forward" direction).
//
// The distances of Photo Eye 1 and Photo Eye 2 to the front edge of each
// head.
//
// Photo Eye 1 is the photo eye which reaches the inspection piece first when
// the carriage is moving away from the operator's station (the "forward"
// direction).
//

public void initializeTraceOffsetDelays(int pDirection)
{

    /*//DEBUG HSS//Plotter plotterPtr;

    double leadingTraceCatch, trailingTraceCatch;
    int lead = 0, trail = 0;
        for (ChartGroup chartGroup : chartGroups) {
            int nSC = chartGroup.getNumberOfStripCharts();
            for (int sc = 0; sc < nSC; sc++) {
                int nTr = chartGroup.getStripChart(sc).getNumberOfPlotters();
                //these used to find the leading trace (smallest offset) and the
                //trailing trace (greatest offset) for each chart
                leadingTraceCatch = Double.MAX_VALUE;
                trailingTraceCatch = Double.MIN_VALUE;
                for (int tr = 0; tr < nTr; tr++) {
                    plotterPtr = chartGroup.getStripChart(sc).getPlotter(tr);
                    //if the current direction is the "Away" direction, then set
                    //the offsets properly for the carriage moving away from the
                    //operator otherwise set them for the carriage moving towards
                    //the operator see more notes in this method's header
                    if (plotterPtr != null){
                        
                        //start with all false, one will be set true
                        plotterPtr.leadPlotter = false;
                        
                        if (pDirection == encoders.getAwayDirection()) {
                            plotterPtr.delayDistance =
                                    plotterPtr.startFwdDelayDistance;
                        }
                        else {
                            plotterPtr.delayDistance =
                                    plotterPtr.startRevDelayDistance;
                        }
                        
                        //find the leading and trailing traces
                        if (plotterPtr.delayDistance < leadingTraceCatch){
                            lead = tr; leadingTraceCatch = plotterPtr.delayDistance;
                        }
                        if (plotterPtr.delayDistance > trailingTraceCatch){
                            trail = tr; trailingTraceCatch=plotterPtr.delayDistance;
                        }
                        
                    }//if (tracePtr != null)
                } //for (int tr = 0; tr < nTr; tr++)
                chartGroup.getStripChart(sc).getPlotter(lead).leadPlotter = true;
                chartGroup.getStripChart(sc).getPlotter(trail).trailPlotter = true;
                chartGroup.getStripChart(sc).setLeadTrailTraces(lead, trail);
            } //for (int sc = 0; sc < nSC; sc++)
        } //for (int cg = 0; cg < chartGroups.length; cg++)*/

}//end of MainHandler::initializeTraceOffsetDelays
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::enableFlagging
//
// Enables or disables all flagging for all devices.
//
// This method is generally called when the inspection start/stop signals are
// received. In other places in the code there is a distance delay after this
// signal to avoid recording the glitches incurred while the head is settling.
//

void enableFlagging(boolean pEnable)
{

    for (Device d : devices) { d.enableFlagging(pEnable); }

}//end of MainHandler::enableFlagging
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::initForPeakScan
//
// Sets pointers and counters in preparation for calls to getNextPeakData to
// scan through all channels.
//

public void initForPeakScan()
{

    peakScanDev = 0;
    peakScanCh = 0;

}// end of MainHandler::initForPeakScan
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::getNextPeakData
//
// Returns data for the next peakData info to be displayed or processed via
// the pPeakData parameter.
//
// When no more datasets are available, returns -1. Returns 0 otherwise.
//
// NOTE: This function is used more often to collect a list of channels
//       rather than for actual peak data collection. It is recommended that
//       the scanning object itself iterate over all channels of all devices.
//       The data update flag returned from getDataAndReset can be handled
//       easier that way and the code is clearer.
//

public int getNextPeakData(PeakData pPeakData)
{

    if(peakScanDev == numDevices){ return(-1); }

    //move to next device after reaching last channel for the current one
    //use while loop to skip past devices which have 0 channels
    while(peakScanCh == devices[peakScanDev].getNumChannels())
    {
        peakScanDev++;
        if(peakScanDev == numDevices){ return(-1); }
        peakScanCh = 0;
    }

    devices[peakScanDev].getPeakDataAndReset(peakScanCh, pPeakData);

    //put recently grabbed meta data in main meta
    pPeakData.meta = pPeakData.metaArray[peakScanCh];

    peakScanCh++;

    return(0);

}// end of MainHandler::getNextPeakData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::startMonitor
//
// Tells all of the devices to start monitoring.
//

public void startMonitor()
{

    monitorStatus = true;
    
    for (Device d : devices) { d.startMonitor();}

}//end of MainHandler::startMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::stopMonitor
//
// Tells all of the devices to stop monitoring.
//

public void stopMonitor()
{
    
    monitorStatus = false;

    for (Device d : devices) { d.stopMonitor();}

}//end of MainHandler::stopMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::getMonitorPacket
//
// Retrieves a data packet containing monitor data.
//
// //WIP HSS// NOTE: Please note that this only works with one Device at
//              the moment. If multiple Devices are present, only the
//              packet for the first one will be grabbed. This can/should be
//              changed in the future. Only this way now because I copied code
//              over from Chart program and wanted to make it work properly
//              before I started hacking away at it. Right now, if no Devices 
//              are present, null returned
//

public byte[] getMonitorPacket(boolean pRequestPacket)
{
    
    if (!monitorStatus || devices == null) { return null; }

    for (Device d : devices) { return d.getMonitorPacket(pRequestPacket); }

    return null;

}//end of MainHandler::getMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::checkIfAnyDevicesSimulated
//
// Returns true if any devices in the list are configured for simulation
// OR
// if the allDevicesSimulatedOverride flag is true.
//

private boolean checkIfAnyDevicesSimulated()
{

    if(allDevicesSimulatedOverride) { return(true); }

    return(checkSimListForValue(true));

}// end of MainHandler::checkIfAnyDevicesSimulated
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::checkIfAnyDevicesNotSimulated
//
// Returns true if allDevicesSimulatedOverride is false and any devices in the
// list are configured NOT for simulation.
//
// Returns false if all in the list are set to simulate or if
// allDevicesSimulatedOverride is true.
//

private boolean checkIfAnyDevicesNotSimulated()
{

    if(allDevicesSimulatedOverride){ return(false); }

    return(checkSimListForValue(false));

}// end of MainHandler::checkIfAnyDevicesNotSimulated
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::checkSimListForValue
//
// Returns true if pValue is found in the devSimModes ArrayList.
//

private boolean checkSimListForValue(boolean pValue)
{

    ListIterator iter = deviceSimModes.listIterator();

    while(iter.hasNext()){
        if ((boolean)iter.next() == pValue){ return(true); }
    }

    return(false);

}// end of MainHandler::checkSimListForValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::processData
//
// Displays, analyzes, stores peak data which has been collected for each
// channel.
//
// Should be called periodically from a Timer thread which needs to be
// thread safe with the GUI so display controls can be updated.
//

public void processData()
{

    for(Device device : devices){ device.collectData(); }





}// end of MainHandler::processData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::setOperationMode
//
// Sets a flag indicating that the operation mode has changed and actions need
// to be taken in the main thread to handle the new mode.
//
// This function is only called from the GUI thread.
//
// Currently, pOpMode is ignored because everything in Hardware reads the
// mode from SharedSettings. It is only passed in because it may be required
// in the future.
//

synchronized public void setOperationMode(int pOpMode)
{

    opModeChanged = true;

}//end of MainHandler::setOperationMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::handleSettingsChanges
//
// Handles all settings that have been flagged as changed.
//

private void handleSettingsChanges()
{

    //handle operation mode changes
    if (opModeChanged) {
    
        opModeChanged = false; //not considered changed after this

        switch (sharedSettings.opMode) {

            case SharedSettings.STOP_MODE:
                startStopMode();
                break;

            case SharedSettings.SCAN_MODE:
                startScanMode();
                break;

            case SharedSettings.INSPECT_MODE:
                startInspectMode();
                break;

            default:
                break;

        }
        
    }

}//end of MainHandler::handleSettingsChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::startStopMode
//
// Puts the system in Stop mode.
//
// WARNING: Should only be called from the "Main Thread". Other threads, such
// as the "Event Dispatch Thread" should trigger the "Main Thread" to execute
// this function by setting invokeStopModeTrigger true.
//

private void startStopMode()
{
    
    opMode = MainHandler.STOPPED;
 
    //disable tracking pulses from Control to other devices
    controlDevice.setTrackPulsesEnabledFlag(false);

}//end of MainHandler::startStopMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::startScanMode
//
// Puts the system in Scan mode.
//
// WARNING: Should only be called from the "Main Thread". Other threads, such
// as the "Event Dispatch Thread" should trigger the "Main Thread" to execute
// this function by setting invokeScanModeTrigger true.
//

private void startScanMode()
{
    
    opMode = MainHandler.SCAN;

    prepareRemotesForNextRun(); //prepare Devices, Control Boards, etc.
    
    //in scan mode, map is advanced on each TDC, so enable Control board to
    //provide a pulse for each TDC it detects
    controlDevice.setTrackPulsesEnabledFlag(false);

}//end of MainHandler::startScanMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::startInspectMode
//
// Puts the system in Inspect mode.
//
// WARNING: Should only be called from the "Main Thread". Other threads, such
// as the "Event Dispatch Thread" should trigger the "Main Thread" to execute
// this function by setting invokeInspectModeTrigger true.
//

private void startInspectMode()
{
    
    //determine whether to use timer driven or remote device driven
    if (sharedSettings.timerDrivenTracking
        || (sharedSettings.timerDrivenTrackingInCalMode 
                && sharedSettings.calMode))
    { 
        opMode = MainHandler.INSPECT_WITH_TIMER_TRACKING; 
    } 
    else { opMode = MainHandler.INSPECT; }

    prepareRemotesForNextRun(); //prep all devices
    
    readyToAdvanceInsertionPoints = false; //not ready on first go around
    
    //system waits until it receives flag that head is off the pipe or no
    //pipe is in the system
    hdwVs.waitForOffPipe = true;

    //track from photo eye clear to end of pipe
    hdwVs.trackToEndOfPiece = false;

    //use a flag and a tracking counter to indicate when head is still near
    //the beginning of the piece
    hdwVs.nearStartOfPiece = true;
    hdwVs.nearStartOfPieceTracker = hdwVs.nearStartOfPiecePosition;

    //flags set true later when end of pipe is near
    hdwVs.trackToNearEndofPiece = false;
    hdwVs.nearEndOfPiece = false;

    //reset length of tube
    hdwVs.measuredLength = 0;
    
    //tell control device to start inspect
    controlDevice.startInspect();

    //ignore the Inspect status flags until a new packet is received
    controlDevice.setNewInspectDataReady(false);

    //force remote to send Inspect packet so all the flags will be up to date
    controlDevice.requestInspectPacket();

}//end of MainHandler::startInspectMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::prepareRemotesForNextRun
//
// Prepares all remote devices for the next inspection run.
//
// WARNING: Should only be called from the "Main Thread". Other threads, such
// as the "Event Dispatch Thread" should trigger the "Main Thread" to execute
// this function by setting invokeStopModeTrigger true.
//

private void prepareRemotesForNextRun()
{

    //make sure tracking pulses are disabled before issuing reset command
    controlDevice.setTrackPulsesEnabledFlag(false);
        
    //tell Control board to pulse the Track Counter Reset line to zero the
    //tracking counters
    controlDevice.resetTrackCounters();
    
    //tell all devices to reset for next run
    for (Device d : devices) { d.sendResetForNextRunCmd(); }
        
}//end of MainHandler::prepareRemotesForNextRun
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::updateChannelParameters
//
// Updates channel parameters contained in parameter pInfo. pInfo should
// identify which device, channel, and parameter are to be updated along with
// the new value.
//
// If pForceUpdate is false, the value will only be updated and its dirty flag
// set true if the new value differs from the old value.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// pInfo Format:
//
//      "Update Channel,Gain Spinner,Transverse,0,2,0,128"
//
//  where 0,2,0,128 are:
//      GUI control ID, device number, channel number, value
//  and:
//      "Gain Spinner" and "Transverse" phrases vary according to context
//
// This method is synchronized along with the method which checks the values
// for changes and processes those changes to that separate threads can update
// and respond to the same variables.
//
// Returns true if the value was updated, false otherwise.
//

synchronized public boolean updateChannelParameters(
                                            String pInfo, boolean pForceUpdate)
{

    String[] split = pInfo.split(",");

    int deviceNum = Integer.parseInt(split[4]);

    boolean result = devices[deviceNum].updateChannelParameters(
                                split[1], split[5], split[6], pForceUpdate);

    if(result) { setHdwParamsDirty(true); }

    return(false);

}//end of MainHandler::updateChannelParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::processChannelParameterChanges
//
// Processes any channel parameters which have been modified since the last
// time this method ran.
//
// All dirty flags are cleared as the changes are processes.
//
// This method is synchronized along with the method which checks the values
// for changes and processes those changes to that separate threads can update
// and respond to the same variables.
//

synchronized private void processChannelParameterChanges()
{

    if(!getHdwParamsDirty()){ return; } //do nothing if no values changed

    // invoke all devices with changed values to process those changes

    for(Device device : devices){
        if (device.getHdwParamsDirty()){
            device.processChannelParameterChanges();
        }
    }

    //updates have been applied, so clear dirty flag...since this method and
    //the method which handels the updates are synchronized, no updates will
    //have occurred while all this method has processed all the updates

    setHdwParamsDirty(false);

}//end of MainHandler::processChannelParameterChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::isReadyToAdvanceInsertionPoints
//
// Returns true if it is time advance all transfer buffers' insertion points.
//
// Resets to false so that next call to this function will say not ready unless
// set back to true again.
//

public boolean isReadyToAdvanceInsertionPoints()
{
    
    boolean isReady = readyToAdvanceInsertionPoints;
    readyToAdvanceInsertionPoints = false;

    return isReady;

}// end of MainHandler::isReadyToAdvanceInsertionPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::shutDown
//
// This function should be called before exiting the program.  Overriding the
// "finalize" method does not work as it does not get called reliably upon
// program exit.
//

public void shutDown()
{

    //tell all devices to shut down
    for (Device d : devices) { d.shutDown(); }

}//end of MainHandler::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Hardware Settings";

    allDevicesSimulatedOverride = configFile.readBoolean(
                                        section, "simulate all devices", true);
    
    //if true, the traces will be driven by software timer rather than by
    //encoder inputs - used for weldline crabs and systems without encoders
    sharedSettings.timerDrivenTracking = configFile.readBoolean(
                                    section, "timer driven tracking", false);

    sharedSettings.timerDrivenTrackingInCalMode = configFile.readBoolean(
                       section, "timer driven tracking in cal mode", false);

    numDevices = configFile.readInt(section, "number of devices", 0);
    maxNumChannels = configFile.readInt(section, "max number of channels", 10);

    //read in the device type string for each device
    for(int i=0; i<numDevices; i++){

        String t = configFile.readString(section, "device " + i + " type", "");
        deviceTypes.add(t);

        boolean sim = configFile.readBoolean(
                           section, "device " + i + " simulation mode", false);

        deviceSimModes.add(sim || allDevicesSimulatedOverride);

        //if device is simulated, add its type to a list of such
        if(sim || allDevicesSimulatedOverride){ deviceTypesSimulated.add(t); }

    }
    
    nameOfDeviceToUseForControl = configFile.readString(section, 
                                    "title of device to use for control", "");
    
    encoderHandlerName = configFile.readString(
                  section, "encoder handler name", "Linear and Rotational");
    
    sharedSettings.awayFromHome =
        configFile.readString(
            section,
            "Description for inspecting in the direction leading away from the"
            + " operator's compartment", "Away From Home");

    sharedSettings.towardsHome =
        configFile.readString(
            section,
            "Description for inspecting in the direction leading toward the"
            + " operator's compartment", "Toward Home");

}// end of MainHandler::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile(IniFile pCalFile)
{
    
    sharedSettings.scanSpeed = pCalFile.readInt("Hardware", 
                                        "Scanning and Inspecting Speed", 10);

    for (Device d : devices) { d.loadCalFile(pCalFile); }

}//end of MainHandler::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

public void saveCalFile(IniFile pCalFile)
{

    pCalFile.writeInt("Hardware", "Scanning and Inspecting Speed",
                        sharedSettings.scanSpeed);

    for (Device d : devices) { d.saveCalFile(pCalFile); }

}//end of MainHandler::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::displayMsg
//
// Displays a message on the msgLabel using a threadsafe method.
//
// There is no bufferering, so if this function is called again before
// invokeLater calls displayMsgThreadSafe, the prior message will be
// overwritten.
//

public void displayMsg(String pMessage)
{

    msg = pMessage;

    javax.swing.SwingUtilities.invokeLater(this::displayMsgThreadSafe);    

}//end of MainHandler::displayMsg
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::displayMsgThreadSafe
//
// Displays a message on the msgLabel and should only be called from
// invokeLater.
//

public void displayMsgThreadSafe()
{

    //DEBUG HSS// //WIP HSS//settings.msgLabel.setText(msg);
    
}//end of MainHandler::displayMsgThreadSafe
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of MainHandler::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of MainHandler::logStackTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::waitSleep
//
// Sleeps for pTime milliseconds.
//

void waitSleep(int pTime)
{

    try{ Thread.sleep(pTime); } catch(InterruptedException e){}

}//end of MainHandler::waitSleep
//-----------------------------------------------------------------------------


}//end of class MainHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SocketSet
//

class SocketSet
{

    MulticastSocket socket = null;
    MulticastSocket simSocket = null;

    byte[] outBuf;
    InetAddress group;
    DatagramPacket outPacket;

//-----------------------------------------------------------------------------
// SocketSet::SocketSet (constructor)
//
// String pBroadcastMsg is the phrase which will be broadcast to all
// devices to elicit a response.
//

public SocketSet(String pBroadcastMsg)
{

    outBuf = pBroadcastMsg.getBytes();

}//end of SocketSet::SocketSet (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SocketSet::checkIfASocketOpened
//
// Returns true if either socket or simSocket are not null, false otherwise.
//

boolean checkIfASocketOpened()
{

    if(socket != null || simSocket != null){ return(true); }
    else { return(false); }

}//end of SocketSet::checkIfASocketOpened
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SocketSet::sendOutPacket
//
// Sends outPackt via socket if socket is opened and via simSocket if it is
// open.
//

void sendOutPacket()throws IOException
{

    if (socket != null) { socket.send(outPacket); }
    if (simSocket != null) { simSocket.send(outPacket); }

}//end of SocketSet::sendOutPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SocketSet::receive
//
// If socket is open, checks socket for a received packet which is then
// returned. If a timeout has been set on the socket (recommended for most
// uses), the attempt will time out if no packet received.
//
// If a timeout occurs before a packet is received and simSocket is open,
// simSocket is checked for a packet.
//
// Returns true if a packet was received.
// Returns false if timeout occurred before packet received or if socket and
// simSocket are both closed.
//

boolean receive(DatagramPacket pPacket) throws IOException
{

    //check real socket for packet first

    try{
        if(socket != null) { socket.receive(pPacket); return(true); }
    }
    catch(SocketTimeoutException e){ }

    //if socket timed out or null and simSocket open, check it for a packet

    try{
        if(simSocket != null){ simSocket.receive(pPacket); return(true); }
    }
    catch(SocketTimeoutException e){ }

    //no packet received if this point reached
    return(false);

}//end of SocketSet::receive
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SocketSet::setSoTimeout
//
// If socket is open, sets its timeout for receiving to pTimeMilliSec.
//

void setSoTimeout(int pTimeMilliSec) throws SocketException
{

    if (socket != null) { socket.setSoTimeout(pTimeMilliSec); }

}//end of SocketSet::setSoTimeout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SocketSet::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of SocketSet::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SocketSet::closeAll
//
// Closes the ethernet socket and releases the simulated socket object.
//

void closeAll()
{

    if(socket!= null){ socket.close(); }
    simSocket = null;

}//end of SocketSet::closeAll
//-----------------------------------------------------------------------------

}//end of class SocketSet
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

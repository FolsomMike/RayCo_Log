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

    LogPanel logPanel;    
    
    private int numDevices;

    int peakScanDev;
    int peakScanCh;

    Device devices[];
    public Device[] getDevices(){ return(devices); }
    ArrayList<Boolean> deviceSimModes = new ArrayList<>();
    ArrayList<String> deviceTypesSimulated = new ArrayList<>();
    ArrayList<String> deviceHandlers = new ArrayList<>();
    ArrayList<String> deviceTypes = new ArrayList<>();
    
    private boolean allDevicesSimulatedOverride;
    
    public boolean ready = false;
    
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
    
    //if (status) { status = initializeDevices();}
    
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
                     logIter, configFile, (boolean)deviceSimModes.get(index));
        devices[index].init();
        index++;
    }

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

private Device createDevice(
        String pDeviceType, int pIndex, LogPanel pLogPanel, IniFile pConfigFile,
                                                               boolean pSimMode)
{
        
    switch(pDeviceType){
    
        case "Longitudinal ~ RayCo Log ~ A" : return (new
            Multi_IO_A_Longitudinal(pIndex, pLogPanel, pConfigFile, pSimMode));

        case "Transverse ~ RayCo Log ~ A" : return (new
              Multi_IO_A_Transverse(pIndex, pLogPanel, pConfigFile, pSimMode));
            
        case "Wall ~ RayCo Log ~ A" : return (new
                    Multi_IO_A_Wall(pIndex, pLogPanel, pConfigFile, pSimMode));

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
// MainHandler::collectData
//
// Collects data from all source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//
// Also drives the simulation functions for any devices in simulation mode.
//

public void collectData()
{
    
    for(Device device : devices){ device.collectData(); }

    for(Device device : devices){ device.driveSimulation(); }
    
}// end of MainHandler::collectData
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
    while(peakScanCh == devices[peakScanDev].getNumChannels()){
        peakScanDev++;
        if(peakScanDev == numDevices){ return(-1); }
        peakScanCh = 0;
    }
    
    devices[peakScanDev].getPeakDataAndReset(peakScanCh, pPeakData);
    
    peakScanCh++;
    
    return(0);
    
}// end of MainHandler::getNextPeakData
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
// MainHandler::updateChannelParameters
//
// Updates channel parameters contained in parameter pInfo. pInfo should
// identify which device, channel, and parameter are to be updated along with
// the new value.
//

public void updateChannelParameters(String pInfo)
{


    return;

}//end of MainHandler::updateChannelParameters
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
    
    numDevices = configFile.readInt(section, "number of devices", 0);
    
    //read in the device type string for each device
    for(int i=0; i<numDevices; i++){        
        
        String s = configFile.readString(section,"device " +i+ " handler", "");
        deviceHandlers.add(s);        
        
        String t = configFile.readString(section, "device " + i + " type", "");
        deviceTypes.add(s);
        
        boolean sim = configFile.readBoolean(
                           section, "device " + i + " simulation mode", false);
        
        deviceSimModes.add(sim || allDevicesSimulatedOverride);
        
        //if device is simulated, add its type to a list of such
        if(sim || allDevicesSimulatedOverride){ deviceTypesSimulated.add(t); }
        
    }

}// end of MainHandler::loadConfigSettings
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

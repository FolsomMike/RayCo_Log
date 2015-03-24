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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListIterator;
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
    ArrayList<Boolean> deviceSimModes;
    ArrayList<String> deviceTypes;

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
    
    deviceTypes = new ArrayList<>();
    deviceSimModes = new ArrayList<>();
    
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
    logPanel.append("Searching for devices...\n\n");
    
    //set up a logging text panel so each device can display messages
    logPanels = mainController.setupDeviceLogPanels(numDevices, false);
    
    //create the handlers for the different remote hardware data acquisition
    //and control boards

    setUpDevices(logPanels);

    findDevices();
    
    ready = true; //devices are ready for access
    
}// end of MainHandler::init
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
// MainHandler::findDevices
//
// Makes initial contact with all devices and determines their IP addresses.
//

private void findDevices()
{

    NetworkInterface networkInterface;

    networkInterface = findNetworkInterface();

    findDevicesAndCollectIPAddresses(networkInterface);
    
}// end of MainHandler::findDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::findDevicesAndCollectIPAddresses
//
// Makes initial contact with all devices and determines their IP addresses.
//

private void findDevicesAndCollectIPAddresses(
                                            NetworkInterface pNetworkInterface)
{

    
}// end of MainHandler::findDevicesAndCollectIPAddresses
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

public NetworkInterface findNetworkInterface()
{

    logPanel.append("");

    NetworkInterface iFace = null;

    try{
        
        logPanel.append("Full list of Network Interfaces:" + "\n\n");
        for (Enumeration<NetworkInterface> en =
              NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

            NetworkInterface intf = en.nextElement();
            logPanel.append("    " + intf.getName() + " " +
                                                intf.getDisplayName() + "\n");

            for (Enumeration<InetAddress> enumIpAddr =
                     intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

                String ipAddr = enumIpAddr.nextElement().toString();

                logPanel.append("        " + ipAddr + "\n");

                if(ipAddr.startsWith("/169.254")){
                    iFace = intf;
                    logPanel.append("^^==>> Binding to above adapter...^^\n");
                }
            }
        }
    }
    catch (SocketException e) {
        logPanel.append(" (error retrieving network interface list)" + "\n");
    }

    logPanel.append("\n");
    
    return(iFace);

}//end of MainHandler::findNetworkInterface
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

public void collectData()
{
    
    for(Device device : devices){ device.collectData(); }
    
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
// MainHandler::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{
    
    String section = "Hardware Settings";

    numDevices = configFile.readInt(section, "number of devices", 0);

    //read in the device type string for each device
    for(int i=0; i<numDevices; i++){
        String s = configFile.readString(section, "device " + i + " type", "");
        deviceTypes.add(s);
        boolean b = configFile.readBoolean(
                           section, "device " + i + " simulation mode", false);
        deviceSimModes.add(b);        
    }

}// end of MainHandler::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class MainHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

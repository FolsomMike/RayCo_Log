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
import java.util.ArrayList;
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

    //set up a logging text panel so each device can display messages
    ArrayList<LogPanel> logPanels = 
                               mainController.setupDeviceLogPanels(numDevices);
    
    //create the handlers for the different remote hardware data acquisition
    //and control boards

    setUpDevices(logPanels);
    
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
    
    LogPanel logPanel;
    
    ListIterator iDevType = deviceTypes.listIterator();
    ListIterator iLogPanel = pLogPanels.listIterator();
    
    while(iDevType.hasNext()){
        
        if(iLogPanel.hasNext()){ logPanel = (LogPanel)iLogPanel.next(); }
        else { logPanel = null; }
        
        devices[index] = createDevice((String) iDevType.next(), index,
                     logPanel, configFile, (boolean)deviceSimModes.get(index));
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

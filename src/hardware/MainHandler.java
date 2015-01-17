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

import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainHandler
//

public class MainHandler
{

    private final int index;
    private final IniFile configFile;    
    private final SharedSettings sharedSettings;

    private final int NUM_DEVICES = 3;

    Device devices[];
    
    private static final int LONG = 0;  //longitudinal system
    private static final int TRANS = 1; //transverse system
    private static final int WALL = 2;  //wall system
    
//-----------------------------------------------------------------------------
// MainHandler::MainHandler (constructor)
//

public MainHandler(int pIndex, SharedSettings pSharedSettings,
                                                        IniFile pConfigFile)
{

    index = pIndex; sharedSettings = pSharedSettings;
    configFile = pConfigFile;
    
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

    //create the handlers for the different remote hardware data acquisition
    //and control boards

    setUpDevices();
        
}// end of MainHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::setUpDevices
//
// Creates and sets up the devices for communicating with the data acquisition
// and control boards.
//

private void setUpDevices()
{

    devices = new Device[NUM_DEVICES];
    
    devices[LONG] = new Multi_IO_A_Longitudinal(LONG, configFile);
    devices[LONG].init();

    devices[TRANS] = new Multi_IO_A_Transverse(TRANS, configFile);
    devices[TRANS].init();

    devices[WALL] = new Multi_IO_A_Wall(WALL, configFile);
    devices[WALL].init();

}// end of MainHandler::setUpDevices
//-----------------------------------------------------------------------------
        
//-----------------------------------------------------------------------------
// MainHandler::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{
    
    String section = "Hardware Settings";
    
}// end of MainHandler::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class MainHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

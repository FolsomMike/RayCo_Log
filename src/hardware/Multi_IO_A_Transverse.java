/******************************************************************************
* Title: Multi_IO_A_Transverse.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class handles communication with a Multi-IO board configuration A
* used for Transverse data acquisition.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import java.net.SocketException;
import model.IniFile;
import model.SharedSettings;
import view.LogPanel;

//-----------------------------------------------------------------------------
// class Multi_IO_A_Transverse
//

public class Multi_IO_A_Transverse extends PeakDevice
{

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::Multi_IO_A_Transverse (constructor)
//

public Multi_IO_A_Transverse(int pIndex, LogPanel pLogPanel,
                                IniFile pConfigFile, SharedSettings pSettings,
                                boolean pSimMode)
{

    super(pIndex, pLogPanel, pConfigFile, pSettings, pSimMode);
    
    canBeControlDevice = true;

//debug remove this -- superseded by Socket Simulator      if(simMode){ simulator = new SimulatorTransverse(0); simulator.init(); }

}//end of Multi_IO_A_Transverse::Multi_IO_A_Transverse (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    loadConfigSettings();

    initAfterLoadingConfig();

}// end of Multi_IO_A_Transverse::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){

    super.initAfterConnect();
    
    sendRabbitControlFlags();

    setEncodersDeltaTrigger();

}//end of Multi_IO_A_Transverse::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::driveSimulation
//
// Drive any simulation functions if they are active.  This function is usually
// called from a thread.
//

@Override
public void driveSimulation()
{

    super.driveSimulation();

}//end of Multi_IO_A_Transverse::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::createSimulatedSocket
//
// Creates an instance of the Simulated class or subclass to simulate an
// actual device connected to Socket.
//
// This is usually called by the parent class to allow each subclass to create
// the appropriate object type.
//

@Override
void createSimulatedSocket() throws SocketException
{

    super.createSimulatedSocket();

    SimulatorTransverse transSimulator = new SimulatorTransverse(
                                                    getIPAddr(), 23, title, "");
    transSimulator.setActiveChannels(channels);
    transSimulator.init(0);

    socket = transSimulator;

}//end of Multi_IO_A_Transverse::createSimulatedSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::collectData
//
// Collects data from source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//

@Override
public void collectData()
{

    super.collectData();

}// end of Multi_IO_A_Transverse::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    super.loadConfigSettings();

    String section = "Device " + getDeviceNum() + " Settings";

}// end of Multi_IO_A_Transverse::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Transverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

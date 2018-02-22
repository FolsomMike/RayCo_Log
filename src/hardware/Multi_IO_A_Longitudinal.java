/******************************************************************************
* Title: Multi_IO_A_Longitudinal.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class handles communication with a Multi-IO board configuration A
* used for Longitudinal data acquisition.
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
// class Multi_IO_A_Longitudinal
//

public class Multi_IO_A_Longitudinal extends PeakDevice
{

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::Multi_IO_A_Longitudinal (constructor)
//

public Multi_IO_A_Longitudinal(int pIndex, LogPanel pLogPanel,
                                IniFile pConfigFile, SharedSettings pSettings,
                                boolean pSimMode)
{

    super(pIndex, pLogPanel, pConfigFile, pSettings, pSimMode);

//debug remove this -- superseded by Socket Simulator  if(simMode){ simulator = new SimulatorLongitudinal(0); simulator.init(); }

}//end of Multi_IO_A_Longitudinal::Multi_IO_A_Longitudinal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    loadConfigSettings();

    initAfterLoadingConfig();

}// end of Multi_IO_A_Longitudinal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){

    super.initAfterConnect();

}//end of Multi_IO_A_Longitudinal::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::driveSimulation
//
// Drive any simulation functions if they are active.  This function is usually
// called from a thread.
//

@Override
public void driveSimulation()
{

    super.driveSimulation();

}//end of Multi_IO_A_Longitudinal::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::createSimulatedSocket
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


    /* debug mks -- remove this
    super.createSimulatedSocket();

    SimCh2 simCh2 = new SimCh2(getIPAddr(), 23, "", "");

    simCh2.init(0);

    socket = simCh2;
    */

    super.createSimulatedSocket();

    SimulatorLongitudinal longSimulator = new SimulatorLongitudinal(
                                                   getIPAddr(), 23, title, "");
    longSimulator.setActiveChannels(channels);
    longSimulator.init(0);

    socket = longSimulator;

}//end of Multi_IO_A_Longitudinal::createSimulatedSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::collectData
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

}// end of Multi_IO_A_Longitudinal::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    super.loadConfigSettings();

    String section = "Device " + getDeviceNum() + " Settings";

}// end of Multi_IO_A_Longitudinal::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Longitudinal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

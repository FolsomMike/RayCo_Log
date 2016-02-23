/******************************************************************************
* Title: Multi_IO_A_Wall.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class handles communication with a Multi-IO board configuration A
* used for Wall data acquisition.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import java.net.SocketException;
import model.IniFile;
import view.LogPanel;

//-----------------------------------------------------------------------------
// class Multi_IO_A_Wall
//

public class Multi_IO_A_Wall extends MultiIODevice
{

    
//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::Multi_IO_A_Wall (constructor)
//

public Multi_IO_A_Wall(int pIndex, LogPanel pLogPanel, 
                                         IniFile pConfigFile, boolean pSimMode)
{

    super(pIndex, pLogPanel, pConfigFile, pSimMode);

//debug remove this -- superseded by Socket Simulator  if(simMode){ simulator = new SimulatorWall(0); simulator.init(); }
   
}//end of Multi_IO_A_Longitudinal::Multi_IO_A_Wall (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Wall::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{
    
    super.init();

    loadConfigSettings();
  
    initAfterLoadingConfig();

}// end of Multi_IO_A_Wall::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Wall::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){

    super.initAfterConnect();
     
}//end of Multi_IO_A_Wall::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Wall::driveSimulation
//
// Drive any simulation functions if they are active.  This function is usually
// called from a thread.
//

@Override
public void driveSimulation()
{

    super.driveSimulation();
    
}//end of Multi_IO_A_Wall::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Wall::createSimulatedSocket
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
    
    SimulatorWall wallSimulator = new SimulatorWall(getIPAddr(), 23, title, "");

    wallSimulator.init(0);

    socket = wallSimulator;
    
}//end of Multi_IO_A_Wall::createSimulatedSocket
//-----------------------------------------------------------------------------    

//-----------------------------------------------------------------------------
// Multi_IO_A_Wall::collectData
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
        
}// end of Multi_IO_A_Wall::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Wall::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{
    
    super.loadConfigSettings();
    
    String section = "Device " + getDeviceNum() + " Settings";

}// end of Multi_IO_A_Wall::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Wall
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

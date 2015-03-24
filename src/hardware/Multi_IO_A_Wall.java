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
    
    PACKET_SIZE = 9;
    
    if(simMode){ simulator = new SimulatorWall(0); simulator.init(); }
   
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
    
    String section = "Device " + deviceNum + " Settings";

}// end of Multi_IO_A_Wall::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Wall
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

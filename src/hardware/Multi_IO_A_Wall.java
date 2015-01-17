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

//-----------------------------------------------------------------------------
// class Multi_IO_A_Wall
//

public class Multi_IO_A_Wall extends Device
{

    
//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::Multi_IO_A_Wall (constructor)
//

public Multi_IO_A_Wall(int pIndex, IniFile pConfigFile)
{

    super(pIndex, pConfigFile);
    
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
    
    setUpChannels();    

}// end of Multi_IO_A_Wall::init
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
    
    String section = "Device " + index + " Settings";

}// end of Multi_IO_A_Wall::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Wall
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

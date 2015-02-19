/******************************************************************************
* Title: Multi_IO_A_Transverse.java
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

import model.IniFile;

//-----------------------------------------------------------------------------
// class Multi_IO_A_Transverse
//

public class Multi_IO_A_Transverse extends Device
{

    
//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::Multi_IO_A_Transverse (constructor)
//

public Multi_IO_A_Transverse(int pIndex, IniFile pConfigFile)
{

    super(pIndex, pConfigFile);
    
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

    setUpChannels();
    
}// end of Multi_IO_A_Transverse::init
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
    
    String section = "Device " + deviceNum + " Settings";

}// end of Multi_IO_A_Transverse::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Transverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

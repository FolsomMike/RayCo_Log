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

import model.IniFile;

//-----------------------------------------------------------------------------
// class Multi_IO_A_Longitudinal
//

public class Multi_IO_A_Longitudinal extends Device
{

    
//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::Multi_IO_A_Longitudinal (constructor)
//

public Multi_IO_A_Longitudinal(int pIndex, IniFile pConfigFile)
{

    super(pIndex, pConfigFile);
    
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
    
    setUpChannels();    

}// end of Multi_IO_A_Longitudinal::init
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
    
    String section = "Device " + index + " Settings";

}// end of Multi_IO_A_Longitudinal::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Longitudinal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

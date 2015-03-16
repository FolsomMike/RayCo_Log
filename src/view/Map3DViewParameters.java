/******************************************************************************
* Title: Map3DViewParameters.java
* Author: Mike Schoonover
* Date: 03/15/15
*
* Purpose:
*
* This class encapsulates variables related to the view of a 3D map.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;
import model.IniFile;

//-----------------------------------------------------------------------------
// class Map3DViewParameters
//

public class Map3DViewParameters{

    public int xPos, yPos;
    public int xFrom, yFrom, zFrom;
    public int xAt, yAt, zAt;
    public int xUp, yUp, zUp;
    public int rotation;
    public int viewAngle;
    
//-----------------------------------------------------------------------------
// Map3DViewParameters::setValues
//
// Sets all values from the values in pValues.
//

public void setValues(Map3DViewParameters pValues)
{

    xPos = pValues.xPos; yPos = pValues.yPos;
    xFrom = pValues.xFrom; yFrom = pValues.yFrom; zFrom = pValues.zFrom;
    xAt = pValues.xAt; yAt = pValues.yAt; zAt = pValues.zAt;
    xUp = pValues.xUp; yUp = pValues.yUp; zUp = pValues.zUp;
    rotation = pValues.rotation;
    viewAngle = pValues.viewAngle;
    
}// end of Map3DViewParameters::setValues
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// Map3DViewParameters::loadConfigSettings
//
// Loads settings for the object from configFile from section 
// pConfigFileSection.
//
// The string pKeyModifier is inserted into the key string to find the value.
//

public void loadConfigSettings(IniFile pConfigFile, 
                                String pConfigFileSection, String pKeyModifier)
{

    String keySuffix = "~3D map " + pKeyModifier + " layout view setting";
    
    
    xPos = pConfigFile.readInt(pConfigFileSection, "xPos" + keySuffix, 0);
    yPos = pConfigFile.readInt(pConfigFileSection, "yPos" + keySuffix, -54);
    xFrom = pConfigFile.readInt(pConfigFileSection, "xFrom" + keySuffix, 0);
    yFrom = pConfigFile.readInt(pConfigFileSection, "yFrom" + keySuffix, 10);
    zFrom = pConfigFile.readInt(pConfigFileSection, "zFrom" + keySuffix, 5);
    xAt = pConfigFile.readInt(pConfigFileSection, "xAt" + keySuffix, 0);
    yAt = pConfigFile.readInt(pConfigFileSection, "yAt" + keySuffix, 0);
    zAt = pConfigFile.readInt(pConfigFileSection, "zAt" + keySuffix, 0);
    xUp = pConfigFile.readInt(pConfigFileSection, "xUp" + keySuffix, 0);
    yUp = pConfigFile.readInt(pConfigFileSection, "yUp" + keySuffix, 0);
    zUp = pConfigFile.readInt(pConfigFileSection, "zUp" + keySuffix, 1);
    rotation = pConfigFile.readInt(
                              pConfigFileSection, "rotation" + keySuffix, 180);
    viewAngle = pConfigFile.readInt(
                              pConfigFileSection, "viewAngle" + keySuffix, 12);
    
    yPos = -(yPos); //flip so higher values are higher on the screen
    
}// end of Map3DViewParameters::loadConfigSettings
//-----------------------------------------------------------------------------
        
}//end of class Map3DViewParameters
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

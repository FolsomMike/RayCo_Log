/******************************************************************************
* Title: ControlsGroup.java
* Author: Mike Schoonover
* Date: 03/10/15
*
* Purpose:
*
* This interface provides a mechanism to set and retrieve all values in a
* group of controls.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// interface ControlsGroup
//

interface ControlsGroup{
    
    public int getChartGroupNum();
    
    public int getChartNum();
        
    public ArrayList<Object> getAllValues();

    public void setAllValues(ArrayList<Object> pValues);
    
    public void refresh();
    
}//end of interface ControlsGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

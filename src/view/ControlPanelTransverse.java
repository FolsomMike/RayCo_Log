/******************************************************************************
* Title: ControlPanelTransverse.java
* Author: Mike Schoonover
* Date: 04/27/15
*
* Purpose:
*
* This class subclasses ChannelControlPanel to display controls for adjusting
* settings for hardware channels of the Transverse system such as gain,
* hardware offset (referred to as centering), positive trace offset, and
* negative trace offset.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ControlPanelTransverse
//

class ControlPanelTransverse extends ControlPanelBasic
{

//-----------------------------------------------------------------------------
// ControlPanelTransverse::ControlPanelTransverse (constructor)
//
//

public ControlPanelTransverse(int pChartGroupNum, int pChartNum,
    String pPanelTitle, LinkedHashSet<String> pGroupTitles,
     ArrayList<ChannelInfo> pChannelList, ActionListener pParentActionListener)
{

    super(pChartGroupNum, pChartNum, pPanelTitle, pGroupTitles, 
                                          pChannelList, pParentActionListener);
  
    panelTitle = "Transverse Controls";
    
}//end of ControlPanelTransverse::ControlPanelTransverse (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelTransverse::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    
}// end of ControlPanelTransverse::init
//-----------------------------------------------------------------------------

}//end of class ControlPanelTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

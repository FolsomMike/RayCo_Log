/******************************************************************************
* Title: ControlPanelWall.java
* Author: Hunter Schoonover
* Date: 04/28/15
*
* Purpose:
*
* This class subclasses ChannelControlPanel to display controls for adjusting
* settings for hardware channels of the Wall system such as gain,
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
// class ControlPanelWall
//

class ControlPanelWall extends ControlPanelBasic
{

//-----------------------------------------------------------------------------
// ControlPanelWall::ControlPanelWall (constructor)
//
//

public ControlPanelWall(int pChartGroupNum, int pChartNum, Chart pChart,
        String pPanelTitle, LinkedHashSet<String> pGroupTitles,
        ArrayList<ChannelInfo> pChannelList,
        ArrayList<Threshold[]> pThresholds, ActionListener pParentActionListener)
{

    super(pChartGroupNum, pChartNum, pChart, pPanelTitle,
                pGroupTitles, pChannelList, pThresholds, pParentActionListener);

    panelTitle = "Wall Controls";

}//end of ControlPanelWall::ControlPanelWall (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelWall::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();


}// end of ControlPanelWall::init
//-----------------------------------------------------------------------------

}//end of class ControlPanelWall
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

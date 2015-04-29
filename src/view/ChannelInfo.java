/******************************************************************************
* Title: ChannelControls.java
* Author: Mike Schoonover
* Date: 04/27/15
*
* Purpose:
*
* This class holds references to various GUI controls and related variables.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import javax.swing.JCheckBox;
import mksystems.mswing.MFloatSpinner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChannelInfo
//

public class ChannelInfo
{

    public int deviceNum;
    public int channelNum;

    public String calPanelGroup;
    public String calPanelName;
        
    public JCheckBox onOffBox;
    public MFloatSpinner gainSpin;
    public MFloatSpinner offsetSpin;

//-----------------------------------------------------------------------------
// ChannelInfo::ChannelInfo (constructor)
//
//

public ChannelInfo(int pDeviceNum, int pChannelNum, String pCalPanelGroup,
                                                        String pCalPanelName)
{

    deviceNum = pDeviceNum; channelNum = pChannelNum;
    
    calPanelGroup = pCalPanelGroup; calPanelName = pCalPanelName;
    
}//end of ChannelInfo::ChannelInfo (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChannelInfo::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
}// end of ChannelInfo::init
//-----------------------------------------------------------------------------

}//end of class ChannelInfo
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

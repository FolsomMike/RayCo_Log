/******************************************************************************
* Title: MaskCtrlByPLCSignal.java
* Author: Mike Schoonover
* Date: 10/23/187
*
* Purpose:
*
* This class determines the start/stop state of flag masks by monitoring the
* PLC Inspection Control signal received by the Control board from the PLC
* via a timed pulse digital input.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package hardware;

//-----------------------------------------------------------------------------

import model.SharedSettings;

//-----------------------------------------------------------------------------
// class MaskCtrlByPLCSignal
//
//

public class MaskCtrlByPLCSignal extends MaskCtrlHandler {


//-----------------------------------------------------------------------------
// MaskCtrlByPLCSignal::MaskCtrlByPLCSignal (constructor)
//

public MaskCtrlByPLCSignal(SharedSettings pSettings, HardwareVars pHdwVs,
              InspectControlVars pInspectCtrlVars)
{

    super(pSettings, pHdwVs, pInspectCtrlVars);

}//end of MaskCtrlByPLCSignal::MaskCtrlByPLCSignal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlByPLCSignal::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

@Override
public void init()
{



}//end of MaskCtrlByPLCSignal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlByPLCSignal::process
//
// This function determines if masks should be enabled or disabled based on
// the Inspection Control signal transmitted from the PLC to the Control board.
// It should be called repeatedly during the inspection process.
//
// If a head was raised, the head number of that head is returned.
// If no head was raised, returns N0_HEAD.
//

@Override
public int process()
{

    /*int headRaised = NO_HEAD;


    //if head 1 is up and goes down, enable flagging for all traces on head 1
    //a small distance delay is used to prevent flagging of the initial
    //transition
    if (!hdwVs.head1Down && inspectCtrlVars.head1Down){
        hdwVs.head1Down = true; flaggingEnableDelayHead1 = MASK_DISABLE_DELAY;
        settings.displayMsg("head 1 down...");
    }

    //if head 2 is up and goes down, enable flagging for all traces on head 2
    //a small distance delay is used to prevent flagging of the initial
    //transition; also enable track sync pulses from Control Board and saving
    //of map data; UT Boards already enabled to send map data

    //debug mks -- why is mapping hardcoded to head 2 here? Fix?

    if (!hdwVs.head2Down && inspectCtrlVars.head2Down){
        hdwVs.head2Down = true; flaggingEnableDelayHead2 = MASK_DISABLE_DELAY;
        settings.displayMsg("head 2 down...");
        analogDriver.setTrackPulsesEnabledFlag(true);
        analogDriver.setDataBufferIsEnabled(true);
    }

    //if head 3 is up and goes down, enable flagging for all traces on head 3
    //a small distance delay is used to prevent flagging of the initial
    //transition
    if (!hdwVs.head3Down && inspectCtrlVars.head3Down){
        hdwVs.head3Down = true; flaggingEnableDelayHead3 = MASK_DISABLE_DELAY;
        settings.displayMsg("head 3 down...");
    }

    //if head 1 is down and goes up, disable flagging for all traces on head 1
    if (hdwVs.head1Down && !inspectCtrlVars.head1Down){
        hdwVs.head1Down = false; headRaised = HEAD_1;
        settings.displayMsg("head 1 up...");
    }

    //if head 2 is down and goes up, disable flagging for all traces on head 2
    //disable saving to the map buffer and disable remote sending of map data

    //debug mks -- why is mapping hardcoded to head 2 here?

    if (hdwVs.head2Down && !inspectCtrlVars.head2Down){
        hdwVs.head2Down = false; headRaised = HEAD_2;
        settings.displayMsg("head 2 up...");
        analogDriver.setDataBufferIsEnabled(false);
        analogDriver.enableWallMapPackets(false);
    }

    //if head 3 is down and goes up, disable flagging for all traces on head 3
    if (hdwVs.head3Down && !inspectCtrlVars.head3Down){
        hdwVs.head3Down = false; headRaised = HEAD_3;
        settings.displayMsg("head 3 up...");
    }

    return(headRaised);*/ //DEBUG HSS// uncomment and use later
    
    return 0; //DEBUG HSS// remove after uncomment

}//end of MaskCtrlByPLCSignal::process
//-----------------------------------------------------------------------------


}//end of class MaskCtrlByPLCSignal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

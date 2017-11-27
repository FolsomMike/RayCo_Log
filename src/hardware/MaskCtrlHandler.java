/******************************************************************************
* Title: MaskCtrlHandler.java
* Author: Mike Schoonover
* Date: 10/23/187
*
* Purpose:
*
* This class handles photo eye and encoder inputs and PLC messages which
* determine when a new inspection run starts and ends. It is a parent class for
* child classes which handle different setups such as systems where the PLC
* sends pulses to the Control board or systems where the Control board receives
* the state of the entry eye and uses that along with encoder inputs to
* determine the start/stop state.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package hardware;

import model.SharedSettings;


//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MaskCtrlHandler
//
//

public class MaskCtrlHandler {

    SharedSettings settings;
    public HardwareVars hdwVs;
    InspectControlVars inspectCtrlVars;

    int flaggingEnableDelayHead1 = 0;
    int flaggingEnableDelayHead2 = 0;
    int flaggingEnableDelayHead3 = 0;

    //this value needs to be at least 1 because if the delay is set to zero
    //it gets ignored...the code only catches when it decrements to 0
    //value of 1 actually triggers immediately, so functions as zero delay
    final static int MASK_DISABLE_DELAY = 3;

//-----------------------------------------------------------------------------
// MaskCtrlHandler::MaskCtrlHandler (constructor)
//

public MaskCtrlHandler(SharedSettings pSettings, HardwareVars pHdwVs,
            InspectControlVars pInspectCtrlVars)
{

    settings = pSettings;
    hdwVs = pHdwVs; inspectCtrlVars = pInspectCtrlVars;

}//end of MaskCtrlHandler::MaskCtrlHandler (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlHandler::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

public void init()
{


}//end of MaskCtrlHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlHandler::process
//
// This function determines if masks should be enabled or disabled. It should
// be called repeatedly during the inspection process.
//
// Should be overridden by child classes to provide custom handling based on
// the system configuration.
//

public int process()
{

    return(0);

}//end of MaskCtrlHandler::process
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlHandler::resetFlaggingEnableDelays
//
// Resets all resetFlaggingEnableDelay* variables to zero.
//

public void resetFlaggingEnableDelays()
{

    flaggingEnableDelayHead1 = 0; flaggingEnableDelayHead2 = 0;
    flaggingEnableDelayHead3 = 0;

}//end of MaskCtrlHandler::resetFlaggingEnableDelays
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlHandler::checkFlaggingEnableDelayHead1
//
// If counter is not zero, decrements and returns true if that makes it zero.
// Returns false if counter was not zero or does not become zero.
//

public boolean checkFlaggingEnableDelayHead1()
{

    return(flaggingEnableDelayHead1 != 0 && --flaggingEnableDelayHead1 == 0);

}//end of MaskCtrlHandler::checkFlaggingEnableDelayHead1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlHandler::checkFlaggingEnableDelayHead2
//
// If counter is not zero, decrements and returns true if that makes it zero.
// Returns false if counter was not zero or does not become zero.
//

public boolean checkFlaggingEnableDelayHead2()
{

    return(flaggingEnableDelayHead2 != 0 && --flaggingEnableDelayHead2 == 0);

}//end of MaskCtrlHandler::checkFlaggingEnableDelayHead2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlHandler::checkFlaggingEnableDelayHead3
//
// If counter is not zero, decrements and returns true if that makes it zero.
// Returns false if counter was not zero or does not become zero.
//

public boolean checkFlaggingEnableDelayHead3()
{

    return(flaggingEnableDelayHead3 != 0 && --flaggingEnableDelayHead3 == 0);

}//end of MaskCtrlHandler::checkFlaggingEnableDelayHead3
//-----------------------------------------------------------------------------

}//end of class MaskCtrlHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

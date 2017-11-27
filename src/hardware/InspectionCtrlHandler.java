/******************************************************************************
* Title: InspectionCtrlHandler.java
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


//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class InspectionCtrlHandler
//
//

public class InspectionCtrlHandler {

InspectControlVars inspectCtrlVars;


//-----------------------------------------------------------------------------
// InspectionCtrlHandler::InspectionCtrlHandler (constructor)
//

public InspectionCtrlHandler()
{


}//end of InspectionCtrlHandler::InspectionCtrlHandler (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlHandler::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

public void init()
{


}//end of InspectionCtrlHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlHandler::setInspectCtrlVars
//
// Sets the setInspectCtrlVars reference.
//

public void setInspectCtrlVars(InspectControlVars pInspectCtrlVars)
{

    inspectCtrlVars = pInspectCtrlVars;

}//end of InspectionCtrlHandler::setInspectCtrlVars
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlHandler::getTubePresentState
//
// Checks inputs and PLC messages to determine if a tube is in the system.
//
// Returns true if tube is present, false otherwise.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

public boolean getTubePresentState()
{

    return(false);

}//end of InspectionCtrlHandler::getTubePresentState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlHandler::setTubePresentState
//
// Sets flags which indicate that a tube is present or not present in the
// system. Used to force the state to a desired value when it is not
// controlled by other means.
//
// The way this is done varies among the child classes and depends on the manner
// in which the system is configured.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

public void setTubePresentState(boolean pState)
{


}//end of InspectionCtrlHandler::setTubePresentState
//-----------------------------------------------------------------------------


}//end of class InspectionCtrlHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

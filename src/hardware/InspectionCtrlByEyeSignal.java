/******************************************************************************
* Title: InspectionCtrlByEyeSignal.java
* Author: Mike Schoonover
* Date: 10/23/187
*
* Purpose:
*
* This class determines the start/stop state of an inspection run by monitoring
* the entry photo eye signal received by the Control board either directly
* from the eye or via the PLC.
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
// class InspectionCtrlByEyeSignal
//
//

public class InspectionCtrlByEyeSignal extends InspectionCtrlHandler {


//-----------------------------------------------------------------------------
// InspectionCtrlByEyeSignal::InspectionCtrlByEyeSignal (constructor)
//

public InspectionCtrlByEyeSignal()
{

    super();

}//end of InspectionCtrlByEyeSignal::InspectionCtrlByEyeSignal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlByEyeSignal::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

@Override
public void init()
{


}//end of InspectionCtrlByEyeSignal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlByEyeSignal::getTubePresentState
//
// Checks inspectCtrlVars.entryEyeFlag to determine if tube is in the system.
// This received from the Control board and controlled by the entry eye signal.
//
// Returns true if tube is present, false otherwise.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//


@Override
public boolean getTubePresentState()
{

    return(inspectCtrlVars.entryEyeFlag);

}//end of InspectionCtrlByEyeSignal::getTubePresentState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlByEyeSignal::setTubePresentState
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

@Override
public void setTubePresentState(boolean pState)
{

    inspectCtrlVars.entryEyeFlag = pState;

}//end of InspectionCtrlByEyeSignal::setTubePresentState
//-----------------------------------------------------------------------------

}//end of class InspectionCtrlByEyeSignal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: InspectionCtrlByPLCSignal.java
* Author: Mike Schoonover
* Date: 10/23/187
*
* Purpose:
*
* This class determines the start/stop state of an inspection run by monitoring
* the PLC Inspection Control signal received by the Control board from the PLC
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
//-----------------------------------------------------------------------------
// class InspectionCtrlByPLCSignal
//
//

public class InspectionCtrlByPLCSignal extends InspectionCtrlHandler {


//-----------------------------------------------------------------------------
// InspectionCtrlByPLCSignal::InspectionCtrlByPLCSignal (constructor)
//

public InspectionCtrlByPLCSignal()
{

    super();
    
}//end of InspectionCtrlByPLCSignal::InspectionCtrlByPLCSignal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlByPLCSignal::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

@Override
public void init()
{


}//end of InspectionCtrlByPLCSignal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlByPLCSignal::getTubePresetnState
//
// Checks inspectCtrlVars.onPipeFlag to determine if tube is in the system.
// This received from the Control board and controlled by a signal from the PLC.
//
// Returns true if tube is present, false otherwise.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//


@Override
public boolean getTubePresentState()
{

    return(inspectCtrlVars.onPipeFlag);

}//end of InspectionCtrlByPLCSignal::getTubePresentState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// InspectionCtrlByPLCSignal::setTubePresentState
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

    inspectCtrlVars.onPipeFlag = pState;

}//end of InspectionCtrlByPLCSignal::setTubePresentState
//-----------------------------------------------------------------------------

}//end of class InspectionCtrlByPLCSignal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

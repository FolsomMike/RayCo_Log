/******************************************************************************
* Title: MaskCtrlByEncoder.java
* Author: Mike Schoonover
* Date: 10/23/187
*
* Purpose:
*
* This class determines the start/stop state of flag masks by monitoring the
* encoder position data. For best accuracy, it determines the trailing mask
* start by tracking the trailing end after it clears the entry photo eye.
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
// class MaskCtrlByEncoder
//
//

public class MaskCtrlByEncoder extends MaskCtrlHandler {


//-----------------------------------------------------------------------------
// MaskCtrlByEncoder::MaskCtrlByEncoder (constructor)
//

public MaskCtrlByEncoder(SharedSettings pSettings, HardwareVars pHdwVs,
                            InspectControlVars pInspectCtrlVars)
{

    super(pSettings, pHdwVs, pInspectCtrlVars);

}//end of MaskCtrlByEncoder::MaskCtrlByEncoder (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlByEncoder::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Should be overridden by child classes to provide custom handling based on
// the encoder configuration.
//

@Override
public void init()
{



}//end of MaskCtrlByEncoder::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MaskCtrlByEncoder::process
//
// This function determines if masks should be enabled or disabled based on
// encoder position readings. It should be called repeatedly during the
// inspection process.
//

@Override
public int process()
{


    return(0);


}//end of MaskCtrlByEncoder::process
//-----------------------------------------------------------------------------


}//end of class MaskCtrlByEncoder
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

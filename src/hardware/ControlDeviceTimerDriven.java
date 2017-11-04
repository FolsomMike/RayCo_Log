/******************************************************************************
* Title: ControlDeviceTimerDriven.java
* Author: Hunter Schoonover
* Date: 11/03/17
*
* Purpose:
*
* This class subclasses Device to serve as a Control Device that is timer
* driven.
* 
* This device behaves differently than standard Control Devices because it
* does not have a remote device from which it reads encoder values, photo eyes,
* etc. It does not use distance traveled to determine when it is done with
* a specific insertion point in the transfers buffers. Instead, it uses the 
* program's scan speed to determine when to allow the insertion points to be
* incremented, which then allows for other threads to access the data.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.SharedSettings;


//-----------------------------------------------------------------------------
// class ControlDeviceTimerDriven
//

public class ControlDeviceTimerDriven implements ControlDevice
{
    
    private final SharedSettings sharedSettings;
    
    private int scanRateCounter;

//-----------------------------------------------------------------------------
// ControlDeviceTimerDriven::ControlDeviceTimerDriven (constructor)
//

public ControlDeviceTimerDriven(SharedSettings pSettings)
{
    
    sharedSettings = pSettings;
    
    scanRateCounter = 0;

}//end of ControlDeviceTimerDriven::ControlDeviceTimerDriven (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlDeviceTimerDriven::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

}// end of ControlDeviceTimerDriven::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlDeviceTimerDriven::isReadyToAdvanceInsertionPoints
//
// Returns true if it is time advance all transfer buffers' insertion points.
//

@Override
public boolean isReadyToAdvanceInsertionPoints()
{

    if (scanRateCounter-- == 0){ 
        scanRateCounter = 10 - sharedSettings.scanSpeed; 
        return true;
    }
    else { return false; }

}// end of ControlDeviceTimerDriven::isReadyToAdvanceInsertionPoints
//-----------------------------------------------------------------------------

}//end of class ControlDeviceTimerDriven
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
/******************************************************************************
* Title: ControlDevice.java
* Author: Hunter Schoonover
* Date: 11/04/17
*
* Purpose:
*
* This Interface provides methods required to act as a control device.
*
*/

//-----------------------------------------------------------------------------

package hardware;

public interface ControlDevice {

    public boolean isReadyToAdvanceInsertionPoints();
    public void setTrackPulsesEnabledFlag(boolean pState);
    public void resetTrackCounters();
    public void setNewInspectData(boolean pState);
    public boolean requestInspectPacket();

}

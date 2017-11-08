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
    
    //modes
    static final int FORWARD = 0;
    static final int REVERSE = 1;
    static final int STOP = 2;
    static final int RESET = 3;
    
    //Masks for the Control Board command flags
    static byte ON_PIPE_CTRL =      (byte)0x01;
    static byte HEAD1_DOWN_CTRL =   (byte)0x02;
    static byte HEAD2_DOWN_CTRL =   (byte)0x04;
    static byte HEAD3_DOWN_CTRL =   (byte)0x08;
    static byte UNUSED2_CTRL =      (byte)0x10;
    static byte UNUSED3_CTRL =      (byte)0x20;
    static byte UNUSED4_CTRL =      (byte)0x40;
    static byte UNUSED5_CTRL =      (byte)0x80;
    
    //Masks for the Control Board inputs
    static byte UNUSED1_MASK = (byte)0x10;	// bit on Port A
    static byte UNUSED2_MASK = (byte)0x20;	// bit on Port A
    static byte INSPECT_MASK = (byte)0x40;	// bit on Port A
    static byte ON_PIPE_MASK = (byte)0x80;	// bit on Port A ??no longer true??
    static byte TDC_MASK = (byte)0x01;    	// bit on Port E
    static byte UNUSED3_MASK = (byte)0x20;	// bit on Port E

    public boolean isReadyToAdvanceInsertionPoints();
    public void setTrackPulsesEnabledFlag(boolean pState);
    public void resetTrackCounters();
    public void startInspect();
    public boolean getNewInspectDataReady();
    public void setNewInspectDataReady(boolean pState);
    public boolean requestInspectPacket();
    public void getInspectControlVars(InspectControlVars pICVars);
    
    public void requestAllEncoderValuesPacket();

}

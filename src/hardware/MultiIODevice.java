/******************************************************************************
* Title: MultiIODevice.java
* Author: Mike Schoonover
* Date: 02/24/15
*
* Purpose:
*
* This class is the parent class for subclasses which handle communication with
* Multi-IO boards.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.IniFile;
import model.SharedSettings;
import view.LogPanel;


//-----------------------------------------------------------------------------
// class MultiIODevice
//

public class MultiIODevice extends Device
{

    byte[] runDataPacket;
    byte[] inspectPacket;

    static final int AD_MAX_VALUE = 255;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 127;
    static final int AD_ZERO_OFFSET = 127;

    static final int OFFSET_POT = 0;
    static final int GAIN_POT = 1;

    //Commands for all Devices
    //These should match the values in the code for the hardware.

    //NOTE: Each subclass can have its own command codes. They should be in the
    //range 40~100 so that they don't overlap the codes in this parent class.

    static final byte NO_ACTION_CMD = 0;
    static final byte ACK_CMD = 1;
    static final byte GET_ALL_STATUS_CMD = 2;
    static final byte SET_INSPECTION_MODE_CMD = 3;
    static final byte SET_POT_CMD = 4;
    static final byte UNUSED1_CMD = 5;
    static final byte SET_ONOFF_CMD = 6;
    static final byte GET_RUN_DATA_CMD = 7;
    static final byte SEND_DATA_CMD = 8;
    static final byte DATA_CMD = 9;
    static final byte LOAD_FIRMWARE_CMD = 10;
    static final byte GET_ALL_LAST_AD_VALUES_CMD = 11;
    static final byte SET_LOCATION_CMD = 12;
    static final byte SET_CLOCK_CMD = 13;
    static byte GET_INSPECT_PACKET_CMD = 14;
    static byte ZERO_ENCODERS_CMD = 15;
    static byte GET_MONITOR_PACKET_CMD = 16;
    static byte PULSE_OUTPUT_CMD = 17;
    static byte TURN_ON_OUTPUT_CMD = 18;
    static byte TURN_OFF_OUTPUT_CMD = 19;
    static byte SET_ENCODERS_DELTA_TRIGGER_CMD = 20;
    static byte START_INSPECT_CMD = 21;
    static byte STOP_INSPECT_CMD = 22;
    static byte START_MONITOR_CMD = 23;
    static byte STOP_MONITOR_CMD = 24;
    static byte GET_CHASSIS_SLOT_ADDRESS_CMD = 25;
    static byte SET_CONTROL_FLAGS_CMD = 26;
    static byte RESET_TRACK_COUNTERS_CMD = 27;
    static byte GET_ALL_ENCODER_VALUES_CMD = 28;
    static byte SET_MODE_CMD = 29;
    static byte RESET_FOR_NEXT_RUN_CMD = 30;
    
    // transmit tracking pulse to DSPs for every o'clock position and a reset
    // pulse at every TDC
    static final int RABBIT_SEND_CLOCK_MARKERS = 0x0001;
    // transmit a single pulse at every TDC detection
    static final int RABBIT_SEND_TDC = 0x0002;
    // enables sending of track sync pulses (doesn't affect track reset pulses)
    static final int TRACK_PULSES_ENABLED = 0x0004;

    static final byte ERROR = 125;
    static final byte DEBUG_CMD = 126;
    static final byte EXIT_CMD = 127;

//-----------------------------------------------------------------------------
// MultiIODevice::MultiIODevice (constructor)
//

public MultiIODevice(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                        SharedSettings pSettings, boolean pSimMode)
{

    super(pDeviceNum, pLogPanel, pConfigFile, pSettings, pSimMode);
    
    runDataPacketSize = 212; //includes Rabbit checksum byte
    
    inspectPacketSize = 28; //includes Rabbit checksum byte //WIP HSS// //DEBUG HSS// verify later!!!!!
    
    monitorPacketSize = 28; //includes Rabbit checksum byte
    
    allEncodersPacketSize = 32; //includes Rabbit checksum byte

}//end of MultiIODevice::MultiIODevice (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Do not call loadConfigSettings here...the subclasses should do it.
//

@Override
public void init()
{

    super.init();

    runDataPacket = new byte[runDataPacketSize];
    inspectPacket = new byte[inspectPacketSize];
    
}// end of MultiIODevice::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::initAfterLoadingConfig
//
// Further initializes the object using data loaded from the config file.
// Must be called by subclasses after they call loadConfigSettings(), which
// they must call themselves as they specify the section to be read from.
//

@Override
public void initAfterLoadingConfig()
{

    super.initAfterLoadingConfig();

}// end of MultiIODevice::initAfterLoadingConfig
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){

    super.initAfterConnect();

}//end of MultiIODevice::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::collectData
//
// Collects data from source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//

@Override
public void collectData()
{

    super.collectData();

    processInspectPacket();

}// end of MultiIODevice::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::processInspectPacket
//
// Processes the inpsect packet, performing calculations, setting values, etc.
//
// This packet is only received when the the device is acting as a control
// device. It is only sent by the remote device when the encoders have moved
// a pre-determined amount, which means that each time this packet is received,
// it is time to advance transfer buffer insertion points, which will give
// other classes and threads access to the point it was previously putting data
// into. Note that the incrementation of the insertion points is not done here,
// this just determines whether or not someone else is allowed to.
//

private void processInspectPacket()
{

    //bail if no need to process
    if (!getInspectPacketFromDevice(inspectPacket)) { return; }
    
    int x = 0;
    
    // combine four bytes each to make the encoder counts

    int encoder1Count, encoder2Count;

    // create integer from four bytes in buffer
    encoder1Count = ((inspectPacket[x++] << 24));
    encoder1Count |= (inspectPacket[x++] << 16) & 0x00ff0000;
    encoder1Count |= (inspectPacket[x++] << 8)  & 0x0000ff00;
    encoder1Count |= (inspectPacket[x++])       & 0x000000ff;

    // create integer from four bytes in buffer
    encoder2Count = ((inspectPacket[x++] << 24));
    encoder2Count |= (inspectPacket[x++] << 16) & 0x00ff0000;
    encoder2Count |= (inspectPacket[x++] << 8)  & 0x0000ff00;
    encoder2Count |= (inspectPacket[x++])       & 0x000000ff;

    //transfer to the class variables in one move -- this will be an atomic
    //copy so it is safe for other threads to access those variables
    encoder1 = encoder1Count; encoder2 = encoder2Count;

    //flag if encoder count was increased or decreased
    //a no change case should not occur since packets are sent when there
    //has been a change of encoder count

    if (encoder1 > prevEncoder1) {
        encoder1Dir = EncoderHandler.INCREASING;
    }
    else {
        encoder1Dir = EncoderHandler.DECREASING;
    }

    //flag if encoder count was increased or decreased
    if (encoder2 > prevEncoder2) {
        encoder2Dir = EncoderHandler.INCREASING;
    }
    else {
        encoder2Dir = EncoderHandler.DECREASING;
    }

    //update the previous encoder values for use next time
    prevEncoder1 = encoder1; prevEncoder2 = encoder2;

    //transfer the status of the Control board input ports
    processControlFlags = inspectPacket[x++];
    controlPortE = inspectPacket[x++];

    //control flags are active high
    onPipeFlag = ((processControlFlags & ON_PIPE_CTRL) != 0);

    head1Down = ((processControlFlags & HEAD1_DOWN_CTRL) != 0);

    head2Down = ((processControlFlags & HEAD2_DOWN_CTRL) != 0);

    head3Down = ((processControlFlags & HEAD3_DOWN_CTRL) != 0);

    //port E inputs are active low
    tdcFlag = ((controlPortE & TDC_MASK) == 0);

    unused1Flag = ((controlPortA & UNUSED1_MASK) == 0);

    unused2Flag = ((controlPortA & UNUSED2_MASK) == 0);

    unused3Flag = ((controlPortE & UNUSED3_MASK) == 0);
    
    //retrieval of an inspect packet means that we are ready to advance
    //insertion points in transfer buffers
    readyToAdvanceInsertionPoints = true;

}// end of MultiIODevice::processInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::startInspect
//
// Puts device in the inspect mode.
//

@Override
public void startInspect()
{

    sendPacket(START_INSPECT_CMD, (byte) 0);

}//end of MultiIODevice::startInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendResetForNextRunCmd
//
// Sends to the remote the command to reset for the next run.
//

@Override
public void sendResetForNextRunCmd()
{

    sendPacket(RESET_FOR_NEXT_RUN_CMD, (byte) (0));

}//end of MultiIODevice::sendResetForNextRunCmd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::stopInspect
//
// Takes device out of the inspect mode.
//

@Override
public void stopInspect()
{

    sendPacket(STOP_INSPECT_CMD, (byte) 0);

}//end of MultiIODevice::stopInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::startMonitor
//
// Puts device in the monitor mode.
//

@Override
public void startMonitor()
{

    sendPacket(START_MONITOR_CMD, (byte) 0);
    
}//end of MultiIODevice::startMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::stopMonitor
//
// Takes the device out of monitor mode.
//

@Override
public void stopMonitor()
{

    sendPacket(STOP_MONITOR_CMD, (byte) 0);

}//end of MultiIODevice::stopMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::zeroEncoderCounts
//
// Sends command to zero the encoder counts.
//

@Override
public void zeroEncoderCounts()
{

}//end of MultiIODevice::zeroEncoderCounts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::setTrackPulsesEnabledFlag
//
// Sets the proper flag in rabbitControlFlags and transmits it to the remote.
//

@Override
public void setTrackPulsesEnabledFlag(boolean pState) 
{

    if (pState){
        rabbitControlFlags |= TRACK_PULSES_ENABLED;
    }
    else{
        rabbitControlFlags &= (~TRACK_PULSES_ENABLED);
    }

    //send updated flags to the remotes
    sendRabbitControlFlags();
    
}//end of MultiIODevice::setTrackPulsesEnabledFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::resetTrackCounters
//
// Sends to the remote the command to fire a Track Counter Reset pulse to
// zero the tracking counters.
//

@Override
public void resetTrackCounters()
{
    
    sendPacket(RESET_TRACK_COUNTERS_CMD, (byte)(0));

}//end of MultiIODevice::resetTrackCounters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendRabbitControlFlags
//
// Sends the rabbitControlFlags value to the remotes. These flags control
// the functionality of the remotes.
//
// The paramater pCommand is the command specific to the subclass for its
// Rabbit remote.
//

@Override
public void sendRabbitControlFlags()
{

    sendPacket(SET_CONTROL_FLAGS_CMD,
                (byte) ((rabbitControlFlags >> 8) & 0xff),
                (byte) (rabbitControlFlags & 0xff)
                );

}//end of MultiIODevice::sendRabbitControlFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetPotPacket
//
// Sets gain or offset digital pot for hardware channel pHdwChannel to pValue.
//
// Each digital pot chip contains four pots. Two pots are used for the gain
// and offset of a channel while the other two pots are used for a second
// channel. Thus, each chip is shared by a channel pair.
//
// Each pot chip is enabled by an I/O pin on a PIC:
// CH1/CH2 pot by PIC1 (I2C address 0)
// CH3/CH4 pot by PIC3 (I2C address 2)
// ...and so on...
//
// To set a pot value, the chip containing that pot is first enabled by the
// Master PIC by sending a command to the appropriate Slave PIC which controls
// the enable line of that chip. Afterwards, it disables the pot chip using a
// second command.
//
// pChannel: 0-7
// pGainOrOffset: GAIN_POT or OFFSET_POT
// pVAlue: 0-255
//
// Note: on the schematic/board PIC and Channel numbering is 1 based, i.e.
//      Channel 0~7 -> channel 1~8  PIC 0~7 -> PIC 1~8
//
// Should be overridden by children classes.
//

void sendSetPotPacket(int pHdwChannel, int pGainOrOffset, int pValue)
{

    numACKsExpected++;

}//end of MultiIODevice::sendSetPotPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetGainPacket
//
// Sets gain digital pot for hardware channel pHdwChannel to pValue.
//
// See sendSetPotPacket for more info.
//
// pChannel: 0-7
// pValue: 0-255
//

void sendSetGainPacket(int pHdwChannel, int pValue)
{

}//end of MultiIODevice::sendSetGainPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetOffsetPacket
//
// Sets offset digital pot for hardware channel pHdwChannel to pValue.
//
// See sendSetPotPacket for more info.
//
// pChannel: 0-7
// pValue: 0-255
//

void sendSetOffsetPacket(int pHdwChannel, int pValue)
{

}//end of MultiIODevice::sendSetOffsetPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetOnOffPacket
//
// Sends a command to the remote device to set the On/Off state for pHdwChannel
// to pValue.
//
// Sends value of 1 if state is on; value of 0 if off.
//
// The remote should return an ACK packet.
//
// Should be overridden by children classes.
//

void sendSetOnOffPacket(int pHdwChannel, boolean pValue)
{

}//end of MultiIODevice::sendSetOnOffPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::processChannelParameterChanges
//
// Processes any channel parameters which have been modified since the last
// time this method ran.
//
// All dirty flags are cleared as the changes are processes.
//

@Override
public void processChannelParameterChanges()
{

    super.processChannelParameterChanges();

    if(!getHdwParamsDirty()){ return; } //do nothing if no values changed

}//end of MultiIODevice::processChannelParameterChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::requestInspectPacket
//
// Sends a request to the device for a packet with inspect data such as signal
// photo-eye states, and encoder values.
//
// The returned packet will be handled by handleInspectPacket. See that
// method for more details.
//
// Overridden by children classes for custom handling.
//

@Override
public boolean requestInspectPacket()
{

    if (!super.requestInspectPacket()) { return false; }

    sendPacket(GET_INSPECT_PACKET_CMD, (byte)0);

    return true;

}//end of MultiIODevice::requestInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::requestRunDataPacket
//
// Sends a request to the device for a packet with runtime data such as signal
// peaks, signal maps, photo-eye states, and encoder values.
//
// The returned packed will be handled by handleRunDataPacket. See that
// method for more details.
//

@Override
boolean requestRunDataPacket()
{

    if (!super.requestRunDataPacket()) { return false; }

    sendPacket(GET_RUN_DATA_CMD, (byte)0);

    return true;

}//end of Device::requestRunDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::requestAllStatusPacket
//
// Sends a request to the device for a packet with all status information.
// The returned packed will be handled by handleAllStatusPacket(). See that
// method for more details.
//

void requestAllStatusPacket()
{

    sendPacket(GET_ALL_STATUS_CMD, (byte)0);

}//end of MultiIODevice::requestAllStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::requestAllLastADValues
//
// Sends a request to the device for a packet of all of the latest AD values
// converted and stored by each slave PIC.
//
// The returned packed will be handled by handleAllLastADValuesPacket(). See
// that method for more details.
//

void requestAllLastADValues()
{

    sendPacket(GET_ALL_LAST_AD_VALUES_CMD, (byte)0);

}//end of MultiIODevice::requestAllLastADValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::handleAllLastADValuesPacket
//
// Extracts from packet and displays in the log panel all of the latest AD
// values converted and stored by each slave PIC.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//
// Packet Format from remote device:
//
// first slave PIC Latest AD Value (MSB)
// first slave PIC Latest AD Value (LSB)
// first slave PIC packet checksum
//
// ...packets for remaining Slave PIC packets...
//
// Master PIC packet checksum
//
// Rabbit's overall packet checksum appended by sendPacket function
//

int handleAllLastADValuesPacket()
{

    int numBytesInPkt = 26; //includes Rabbit checksum byte

    byte[] buffer = new byte[numBytesInPkt];

    int result = readBytesAndVerify(buffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    int i = 0, v;
    int errorSum = packetErrorCnt; //number of errors recorded by host

    logPanel.appendTS("\n----------------------------------------------\n");
    logPanel.appendTS("-- All Latest AD Values --\n\n");

    int numSlaves = 8;

    for(int j=0; j<numSlaves; j++){

        logPanel.appendTS("" + String.format("0x%4x",
         getUnsignedShortFromPacket(buffer, i)).replace(' ', '0'));
        i=i+2; //adjust for integer extracted above

        logPanel.appendTS("," + String.format(
          "0x%2x", buffer[i++]).replace(' ', '0')); //Slave PIC packet checksum
        logPanel.appendTS("\n");

    }

    logPanel.appendTS("Master PIC checksum: " + String.format("0x%2x",
                buffer[i++]).replace(' ', '0')); //Master PIC packet checksum
    logPanel.appendTS("\n");

    logPanel.appendTS("Rabbit checksum: " + String.format("0x%2x",
                buffer[i++]).replace(' ', '0')); //Rabbit packet checksum
    logPanel.appendTS("\n");

    return(result);

}//end of MultiIODevice::handleAllLastADValuesPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::handleACKPackets
//
// Handles ACK_CMD packets. Increments the numACKsReceive counter.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//


@Override
public int handleACKPackets()
{

    int result = super.handleACKPackets();

    int numBytesInPkt = 1; //does not include checksum byte

    result = readBytesAndVerify(
                       inBuffer, numBytesInPkt, ACK_CMD);
    if (result != numBytesInPkt){ return(result); }

    return(result);

}//end of MultiIODevice::handleACKPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::handleChassisSlotAddressPacket
//
// Transfers chassis slot address packet received from the remote into an array.
//
// Returns number of bytes retrieved from the socket.
//
// Overridden by children classes for custom handling.
//

int handleChassisSlotAddressPacket()
{

    return 0;

}//end of MultiIODevice::handleChassisSlotAddressPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::requestMonitorPacket
//
// Sends a request to the device for the monitor packet
//

@Override
boolean requestMonitorPacket()
{

    if (!super.requestMonitorPacket()) { return false; }

    sendPacket(GET_MONITOR_PACKET_CMD, (byte)0);

    return true;

}//end of MultiIODevice::requestMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::handleAllEncoderValuesPacket
//
// Parses and stores data from a GET_ALL_ENCODER_VALUES_CMD packet.
//
// Returns number of bytes retrieved from the socket.
//
// Overridden by children classes for custom handling.
//

int handleAllEncoderValuesPacket()
{

    return 0;

}//end of MultiIODevice::handleAllEncoderValuesPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleAllStatusPacket
//
// Handles the all status packet.
//
// Overridden by children for custom handling.
//

int handleAllStatusPacket()
{

    return 0;

}//end of Device::handleAllStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::takeActionBasedOnPacketId
//
// Takes different actions based on the packet id.
//

@Override
protected int takeActionBasedOnPacketId(int pPktID)
{

    int results = super.takeActionBasedOnPacketId(pPktID);

    if (pPktID == GET_ALL_STATUS_CMD) {
        results = handleAllStatusPacket();
    }
    else if (pPktID == ACK_CMD){
        results = handleACKPackets();
    }
    else if (pPktID == GET_ALL_LAST_AD_VALUES_CMD){
        results = handleAllLastADValuesPacket();
    }
    else if (pPktID == GET_RUN_DATA_CMD){
        results = handleRunDataPacket();
    }
    else if (pktID == GET_CHASSIS_SLOT_ADDRESS_CMD){
        return handleChassisSlotAddressPacket();
    }
    else if (pktID == GET_MONITOR_PACKET_CMD) {
        return handleMonitorPacket();
    }
    else if (pktID == GET_ALL_ENCODER_VALUES_CMD) {
        return handleAllEncoderValuesPacket();
    }

    return results;

}//end of MultiIODevice::takeActionBasedOnPacketId
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    super.loadConfigSettings();

}// end of MultiIODevice::loadConfigSettings
//-----------------------------------------------------------------------------

}//end of class MultiIODevice
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class InstallFirmwareSettings
//
// This class is used to pass in all necessary settings to the
// installNewRabbitFirmware function.
//

class InstallFirmwareSettings extends Object{

    public byte loadFirmwareCmd;
    public byte noAction;
    public byte error;
    public byte sendDataCmd;
    public byte dataCmd;
    public byte exitCmd;

}//end of class InstallFirmwareSettings
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
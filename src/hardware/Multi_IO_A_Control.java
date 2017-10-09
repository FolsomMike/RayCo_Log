/******************************************************************************
* Title: ControlDevice.java
* Author: Hunter Schoonover
* Date: 10/04/17
*
* Purpose:
*
* This class interfaces with a Control Device.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package hardware;

import java.io.*;
import java.net.*;
import model.IniFile;
import model.SharedSettings;
import view.LogPanel;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Multi_IO_A_Control
//
// This class creates and handles an interface to a Control board.
//

public class Multi_IO_A_Control extends MultiIODevice
        implements AudibleAlarmController
{

    byte[] monitorBuffer;
    byte[] allEncoderValuesBuf;
    String fileFormat;

    short rabbitControlFlags = 0;

    int packetRequestTimer = 0;

    int runtimePacketSize;

    boolean encoderDataPacketProcessed = false;
    int timeOutWFP = 0; //used by processDataPackets

    boolean newInspectPacketReady = false;

    int encoder1, prevEncoder1;
    int encoder2, prevEncoder2;
    int encoder1Dir, encoder2Dir;

    EncoderValues encoderValues;

    int inspectPacketCount = 0;

    boolean onPipeFlag = false;
    boolean inspectControlFlag = false;
    boolean head1Down = false;
    boolean head2Down = false;
    boolean head3Down = false;
    boolean tdcFlag = false;
    boolean unused1Flag = false;
    boolean unused2Flag = false;
    boolean unused3Flag = false;
    byte controlPortA, controlPortE;
    byte processControlFlags;

    //number of counts each encoder moves to trigger an inspection data packet
    //these values are read later from the config file
    int encoder1DeltaTrigger, encoder2DeltaTrigger;

    protected boolean audibleAlarmController;
    protected int audibleAlarmOutputChannel;
    protected String audibleAlarmPulseDuration;

    // values for the Rabbits control flag - only lower 16 bits are used
    // as the corresponding variable in the Rabbit is an unsigned int

    // transmit tracking pulse to DSPs for every o'clock position and a reset
    // pulse at every TDC
    static final int RABBIT_SEND_CLOCK_MARKERS = 0x0001;
    // transmit a single pulse at every TDC detection
    static final int RABBIT_SEND_TDC = 0x0002;
    // enables sending of track sync pulses (doesn't affect track reset pulses)
    static final int TRACK_PULSES_ENABLED = 0x0004;

    //Status Codes for Control boards
    //These should match the values in the code for those boards.

    static byte NO_STATUS = 0;

    static int MONITOR_PACKET_SIZE = 29;
    static int ALL_ENCODERS_PACKET_SIZE = 32;
    static int RUNTIME_PACKET_SIZE = 2048;

    //Masks for the Control Board inputs

    static byte UNUSED1_MASK = (byte)0x10;	// bit on Port A
    static byte UNUSED2_MASK = (byte)0x20;	// bit on Port A
    static byte INSPECT_MASK = (byte)0x40;	// bit on Port A
    static byte ON_PIPE_MASK = (byte)0x80;	// bit on Port A ??no longer true??
    static byte TDC_MASK = (byte)0x01;    	// bit on Port E
    static byte UNUSED3_MASK = (byte)0x20;	// bit on Port E

    //Masks for the Control Board command flags

    static byte ON_PIPE_CTRL =      (byte)0x01;
    static byte HEAD1_DOWN_CTRL =   (byte)0x02;
    static byte HEAD2_DOWN_CTRL =   (byte)0x04;
    static byte HEAD3_DOWN_CTRL =   (byte)0x08;
    static byte UNUSED2_CTRL =      (byte)0x10;
    static byte UNUSED3_CTRL =      (byte)0x20;
    static byte UNUSED4_CTRL =      (byte)0x40;
    static byte UNUSED5_CTRL =      (byte)0x80;

    //modes
    static int FORWARD = 0;
    static int REVERSE = 1;
    static int STOP = 2;
    static int RESET = 3;

//-----------------------------------------------------------------------------
// UTBoard::UTBoard (constructor)
//
// The parameter configFile is used to load configuration data.  The IniFile
// should already be opened and ready to access.
//

public Multi_IO_A_Control(int pIndex, LogPanel pLogPanel, IniFile pConfigFile,
                            SharedSettings pSettings, boolean pSimMode)
{

    super(pIndex, pLogPanel, pConfigFile, pSettings, pSimMode);

}//end of UTBoard::UTBoard (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

@Override
public void init()
{

    monitorBuffer = new byte[MONITOR_PACKET_SIZE];

    encoderValues = new EncoderValues(); encoderValues.init();

    allEncoderValuesBuf = new byte[ALL_ENCODERS_PACKET_SIZE];

    //read the configuration file and create/setup the charting/control elements
    loadConfigSettings();

}//end of Multi_IO_A_Control::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){


}//end of Multi_IO_A_Control::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//
// Does not call super because Device assumes device has channels.
//
//

@Override
public void loadCalFile(IniFile pCalFile)
{



}//end of Multi_IO_A_Control::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

@Override
public void saveCalFile(IniFile pCalFile)
{

}//end of Device::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::loadConfigSettings
//
// Loads configuration settings from the config file.
//
// The various child objects are then created as specified by the config data.
//

@Override
void loadConfigSettings()
{

    super.loadConfigSettings();

    inBuffer = new byte[RUNTIME_PACKET_SIZE];
    outBuffer = new byte[RUNTIME_PACKET_SIZE];

    //debug mks -- calculate this delta to give one packet per pixel????
    encoder1DeltaTrigger = configFile.readInt(section,
                            "Encoder 1 Delta Count Trigger", 83);

    encoder2DeltaTrigger = configFile.readInt(section,
                            "Encoder 2 Delta Count Trigger", 83);

    String positionTrackingMode = configFile.readString(section,
                            "Position Tracking Mode", "Send Clock Markers");
    parsePositionTrackingMode(positionTrackingMode);

    audibleAlarmController = configFile.readBoolean(section,
                            "Audible Alarm Module", false);

    audibleAlarmOutputChannel = configFile.readInt(section,
                            "Audible Alarm Output Channel", 0);

    audibleAlarmPulseDuration = configFile.readString(section,
                            "Audible Alarm Pulse Duration", "1");

}//end of Multi_IO_A_Control::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::parsePositionTrackingMode
//
// Sets various flags based on the type of angular and linear position tracking
// specified in the config file.
//

void parsePositionTrackingMode(String pValue)

{

    switch (pValue) {
        case "Send Clock Markers":
            rabbitControlFlags |= RABBIT_SEND_CLOCK_MARKERS;
            break;
        case "Send TDC Markers":
            rabbitControlFlags |= RABBIT_SEND_TDC;
            break;
    }

}//end of Multi_IO_A_Control::parsePositionTrackingMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::setTrackPulsesEnabledFlag
//
// Sets the TRACK_PULSES_ENABLED flag in rabbitControlFlags and transmits it
// to the remote.
//

public void setTrackPulsesEnabledFlag(boolean pState) {

    if (pState){
        rabbitControlFlags |= TRACK_PULSES_ENABLED;
    }
    else{
        rabbitControlFlags &= (~TRACK_PULSES_ENABLED);
    }

    //send updated flags to the remotes
    sendRabbitControlFlags();

}//end of Multi_IO_A_Control::setTrackPulsesEnabledFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::resetTrackCounters
//
// Sends to the remote the command to fire a Track Counter Reset pulse to
// zero the tracking counters in the UT Boards.
//

public void resetTrackCounters()
{

    sendPacket(RESET_TRACK_COUNTERS_CMD, (byte)(0));

}//end of Multi_IO_A_Control::resetTrackCounters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Transverse::createSimulatedSocket
//
// Creates an instance of the Simulated class or subclass to simulate an
// actual device connected to Socket.
//
// This is usually called by the parent class to allow each subclass to create
// the appropriate object type.
//

@Override
void createSimulatedSocket() throws SocketException
{

    super.createSimulatedSocket();

    SimulatorControl ctrlSimulator = new SimulatorControl(
                                                    getIPAddr(), 23, title, "");
    ctrlSimulator.setActiveChannels(channels);
    ctrlSimulator.init(0);

    socket = ctrlSimulator;

}//end of Multi_IO_A_Transverse::createSimulatedSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control:initialize
//
// Sets up various settings on the board.
//

public void initialize()
{

    sendRabbitControlFlags();

    setEncodersDeltaTrigger();

}//end of Multi_IO_A_Control::initialize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::requestAllEncoderValues
//
// Requests a packet from the remote with all encoder values saved at different
// points in the inspection process.
//
// Note that the values will not be valid until the packet is received. If
// any encoder value is Integer.MAX_VALUE, the packet has not been received.
//

public void requestAllEncoderValues()
{

    //set all values to max so it can be detected when they are set by
    //the code which processes the return packet - processAllEncoderValuesPacket

    encoderValues.setAllToMaxValue();

    //request packet; returned packet handled by processAllEncoderValuesPacket
    sendPacket(GET_ALL_ENCODER_VALUES_CMD);

}//end of Multi_IO_A_Control::requestAllEncoderValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::processAllEncoderValuesPacket
//
// Parses and stores data from a GET_ALL_ENCODER_VALUES_CMD packet.
//
// Returns number of bytes retrieved from the socket.
//

public int processAllEncoderValuesPacket()
{

    setWaitingForRemoteResponse(false);

    int result;
    result = readBytesAndVerify(allEncoderValuesBuf,
                                    ALL_ENCODERS_PACKET_SIZE, pktID);
    if (result != ALL_ENCODERS_PACKET_SIZE){ return(result); }

    int x = 0;

    //wip mks -- shrink this by calling function to convert bytes

    encoderValues.encoderPosAtOnPipeSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtOffPipeSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtHead1DownSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtHead1UpSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtHead2DownSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtHead2UpSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtHead3DownSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    encoderValues.encoderPosAtHead3UpSignal = encoderValues.convertBytesToInt(
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++],
                        allEncoderValuesBuf[x++], allEncoderValuesBuf[x++]);

    return(ALL_ENCODERS_PACKET_SIZE);

}//end of Multi_IO_A_Control::processAllEncoderValuesPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::setEncoderValuesObject
//
// Sets the EncoderValues object.
//

public void setEncoderValuesObject(EncoderValues pEncoderValues)
{

    encoderValues = pEncoderValues;

}//end of Multi_IO_A_Control::setEncoderValuesObject
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::getEncoderValuesObject
//
// Returns an object containing the encoder values retrieved from the remote.
//

public EncoderValues getEncoderValuesObject()
{

    return(encoderValues);

}//end of Multi_IO_A_Control::getEncoderValuesObject
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::startMonitor
//
// Places the Control board in Monitor status and displays the status of
// various I/O as sent back from the Control board.
//

public void startMonitor()
{

    sendPacket(START_MONITOR_CMD, (byte) 0);

}//end of Multi_IO_A_Control::startMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::stopMonitor
//
// Takes the Control board out of monitor mode.
//

public void stopMonitor()
{

    sendPacket(STOP_MONITOR_CMD, (byte) 0);

}//end of Multi_IO_A_Control::stopMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::processMonitorPacket
//
// Transfers I/O status received from the remote into an array.
//
// Returns number of bytes retrieved from the socket.
//

public int processMonitorPacket()
{

    setWaitingForRemoteResponse(false);

    int numBytesInPkt = MONITOR_PACKET_SIZE; //includes Rabbit checksum byte

    int result;
    result = readBytesAndVerify(monitorBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    return result;

}//end of Multi_IO_A_Control::processMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::getMonitorPacket
//
// Returns in a byte array I/O status data which has already been received and
// stored from the remote.
// If pRequestPacket is true, then a packet is requested every so often.
// If false, then packets are only received when the remote computer sends
// them.
//
// NOTE: This function is often called from a different thread than the one
// transferring the data from the input buffer -- erroneous values for some of
// the multibyte values may occur due to thread collision but they are for
// display/debugging only and an occasional glitch in the displayed values
// should not be of major concern.
//

public byte[] getMonitorPacket(boolean pRequestPacket)
{

    if (pRequestPacket){
        //request a packet be sent if the counter has timed out
        //this packet will arrive in the future and be processed by another
        //function so it can be retrieved by another call to this function
        if (packetRequestTimer++ == 50){
            packetRequestTimer = 0;
            sendPacket(GET_MONITOR_PACKET_CMD, (byte) 0);
        }
    }

    return monitorBuffer;

}//end of Multi_IO_A_Control::getMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::processStatusPacket
//
// Transfers status packet received from the remote into an array.
//
// Returns number of bytes retrieved from the socket.
//

public int processStatusPacket()
{

    setWaitingForRemoteResponse(false);

    int numBytesInPkt = 2; //includes Rabbit checksum byte

    int result;
    result = readBytesAndVerify(inBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    return result;

}//end of Multi_IO_A_Control::processStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::processChassisSlotAddressPacket
//
// Transfers chassis slot address packet received from the remote into an array.
//
// Returns number of bytes retrieved from the socket.
//

public int processChassisSlotAddressPacket()
{

    setWaitingForRemoteResponse(false);

    int numBytesInPkt = 2; //includes Rabbit checksum byte

    int result;
    result = readBytesAndVerify(inBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    return result;

}//end of Multi_IO_A_Control::processChassisSlotAddressPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::requestInspectPacket
//
// Normally, the Control board sends Inspect packets as necessary.  This
// function is used to force the send of an Inspect packet so that all local
// flags and values will be updated.
//
// Returns number of bytes retrieved from the socket.
//

public void requestInspectPacket()
{

    sendPacket(GET_INSPECT_PACKET_CMD, (byte) 0);

}//end of Multi_IO_A_Control::requestInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::processInspectPacket
//
// Processes an Inspect packet from the remote with flags and encoder values.
//
// Returns number of bytes retrieved from the socket.
//

public int processInspectPacket()
{

    setWaitingForRemoteResponse(false);

    int numBytesInPkt = 12; //includes Rabbit checksum byte

    int result;
    result = readBytesAndVerify(inBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    int x = 0;

    inspectPacketCount = (int)((inBuffer[x++]<<8) & 0xff00)
                                             + (int)(inBuffer[x++] & 0xff);

    // combine four bytes each to make the encoder counts

    int encoder1Count, encoder2Count;

    // create integer from four bytes in buffer
    encoder1Count = ((inBuffer[x++] << 24));
    encoder1Count |= (inBuffer[x++] << 16) & 0x00ff0000;
    encoder1Count |= (inBuffer[x++] << 8)  & 0x0000ff00;
    encoder1Count |= (inBuffer[x++])       & 0x000000ff;

    // create integer from four bytes in buffer
    encoder2Count = ((inBuffer[x++] << 24));
    encoder2Count |= (inBuffer[x++] << 16) & 0x00ff0000;
    encoder2Count |= (inBuffer[x++] << 8)  & 0x0000ff00;
    encoder2Count |= (inBuffer[x++])       & 0x000000ff;

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
    processControlFlags = inBuffer[x++];
    controlPortE = inBuffer[x++];

    //control flags are active high

    if ((processControlFlags & ON_PIPE_CTRL) != 0) {
        onPipeFlag = true;
    }
    else {
        onPipeFlag = false;
    }

    if ((processControlFlags & HEAD1_DOWN_CTRL) != 0) {
        head1Down = true;
    } else {
        head1Down = false;
    }

    if ((processControlFlags & HEAD2_DOWN_CTRL) != 0) {
        head2Down = true;
    } else {
        head2Down = false;
    }

    if ((processControlFlags & HEAD3_DOWN_CTRL) != 0) {
        head3Down = true;
    } else {
        head3Down = false;
    }

    //port E inputs are active low

    if ((controlPortE & TDC_MASK) == 0) {
        tdcFlag = true;
    } else {
        tdcFlag = false;
    }

    if ((controlPortA & UNUSED1_MASK) == 0) {
        unused1Flag = true;
    } else {
        unused1Flag = false;
    }

    if ((controlPortA & UNUSED2_MASK) == 0) {
        unused2Flag = true;
    } else {
        unused2Flag = false;
    }

    if ((controlPortE & UNUSED3_MASK) == 0) {
        unused3Flag = true;
    } else {
        unused3Flag = false;
    }

    newInspectPacketReady = true; //signal other objects

    return result;

}//end of Multi_IO_A_Control::processInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::zeroEncoderCounts
//
// Sends command to zero the encoder counts.
//

public void zeroEncoderCounts()
{

    sendPacket(ZERO_ENCODERS_CMD, (byte) 0);

}//end of Multi_IO_A_Control::zeroEncoderCounts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::pulseOutput
//
// Pulses the specified output.
//
// NOTE: current ignores the pulse duration read from config file -- needs to
// be fixed.
//

public void pulseOutput(int pWhichOutput)
{

    sendPacket(PULSE_OUTPUT_CMD, (byte) pWhichOutput);

}//end of Multi_IO_A_Control::pulseOutput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::turnOnOutput
//
// Turns on the specified output.
//

public void turnOnOutput(int pWhichOutput)
{

    sendPacket(TURN_ON_OUTPUT_CMD, (byte) pWhichOutput);

}//end of Multi_IO_A_Control::turnOnOutput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::turnOffOutput
//
// Turns off the specified output.
//

public void turnOffOutput(int pWhichOutput)
{

    sendPacket(TURN_OFF_OUTPUT_CMD, (byte) pWhichOutput);

}//end of Multi_IO_A_Control::turnOffOutput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::turnOnAudibleAlarm
//
// Turns on the output which fires the audible alarm for one second.
//

@Override
public void turnOnAudibleAlarm()
{

    turnOnOutput(audibleAlarmOutputChannel);

}//end of Multi_IO_A_Control::turnOnAudibleAlarm
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::turnOffAudibleAlarm
//
// Turns off the output which fires the audible alarm for one second.
//

@Override
public void turnOffAudibleAlarm()
{

    turnOffOutput(audibleAlarmOutputChannel);

}//end of Multi_IO_A_Control::turnOffAudibleAlarm
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::pulseAudibleAlarm
//
// Pulses the output which fires the audible alarm for one second.
//

@Override
public void pulseAudibleAlarm()
{

    pulseOutput(audibleAlarmOutputChannel);

}//end of Multi_IO_A_Control::pulseAudibleAlarm
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::pulseAlarmMarker
//
// Pulses the output specified by pChannel which fires the output for one
// second.
//

@Override
public void pulseAlarmMarker(int pChannel)
{

    pulseOutput(pChannel);

}//end of Multi_IO_A_Control::pulseAlarmMarker
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::isAudibleAlarmController
//
// Returns audibleAlarmController.
//

@Override
public boolean isAudibleAlarmController()
{

    return(audibleAlarmController);

}//end of Multi_IO_A_Control::isAudibleAlarmController
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::setEncodersDeltaTrigger
//
// Tells the Control board how many encoder counts to wait before sending
// an encoder value update.  The trigger value for each encoder is sent.
//
// Normally, this value will be set to something reasonable like .25 to 1.0
// inch of travel of the piece being inspected. Should be no larger than the
// distance represented by a single pixel.
//

public void setEncodersDeltaTrigger()
{

    sendPacket(SET_ENCODERS_DELTA_TRIGGER_CMD,
                (byte)((encoder1DeltaTrigger >> 8) & 0xff),
                (byte)(encoder1DeltaTrigger & 0xff),
                (byte)((encoder2DeltaTrigger >> 8) & 0xff),
                (byte)(encoder2DeltaTrigger & 0xff)
                );

}//end of Multi_IO_A_Control::setEncodersDeltaTrigger
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Board::sendRabbitControlFlags
//
// Sends the rabbitControlFlags value to the remotes. These flags control
// the functionality of the remotes.
//
// The paramater pCommand is the command specific to the subclass for its
// Rabbit remote.
//

public void sendRabbitControlFlags()
{

    sendPacket(SET_CONTROL_FLAGS_CMD,
                (byte) ((rabbitControlFlags >> 8) & 0xff),
                (byte) (rabbitControlFlags & 0xff)
                );

}//end of Board::sendRabbitControlFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::startInspect
//
// Puts Control board in the inspect mode.  In this mode the Control board
// will monitor encoder and status signals and return position information to
// the host.
//

public void startInspect()
{

    sendPacket(START_INSPECT_CMD, (byte) 0);

}//end of Multi_IO_A_Control::startInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::stopInspect
//
// Takes Control board out of the inspect mode.
//

public void stopInspect()
{

    sendPacket(STOP_INSPECT_CMD, (byte) 0);

}//end of Multi_IO_A_Control::stopInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::prepareData
//
// Retrieves a data packet from the incoming data buffer and distributes it
// to the newData variables in each gate.
//
// Returns true if new data is available, false if not.
//

public boolean prepareData()
{

    if (byteIn != null) {
        try {

            int c = byteIn.available();

            //if a full packet is not ready, return false
            if (c < runtimePacketSize) {return false;}

            byteIn.read(inBuffer, 0, runtimePacketSize);

            //wip mks - distribute the data to the gate's newData variables here

        }
        catch(EOFException eof){
            logPanel.appendTS("End of stream.\n");
            return false;
        }
        catch(IOException e){
            logSevere(e.getMessage() + " - Error: 672");
            return false;
        }
    }

    return true;

}//end of Multi_IO_A_Control::prepareData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::takeActionBasedOnPacketId
//
// Takes different actions based on the packet id.
//
// This class does not call the parent function because it uses commands whose
// values clash with commad values in the parent class.
//

@Override
protected int takeActionBasedOnPacketId(int pPktID)
{

    pktID = pPktID;

    if (pktID == GET_ALL_STATUS_CMD) {
        return processStatusPacket();
    }
    else if (pktID == GET_CHASSIS_SLOT_ADDRESS_CMD){
        return processChassisSlotAddressPacket();
    }
    else if (pktID == GET_INSPECT_PACKET_CMD) {
        return processInspectPacket();
    }
    else if (pktID == GET_MONITOR_PACKET_CMD) {
        return processMonitorPacket();
    }
    else if (pktID == GET_ALL_ENCODER_VALUES_CMD) {
        return processAllEncoderValuesPacket();
    }

    return 0;

}//end of Multi_IO_A_Control::takeActionBasedOnPacketId
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::getInspectControlVars
//
// Transfers local variables related to inspection control signals and encoder
// counts.
//

public void getInspectControlVars(InspectControlVars pICVars)
{

    pICVars.onPipeFlag = onPipeFlag;

    pICVars.head1Down = head1Down;

    pICVars.head2Down = head2Down;

    pICVars.head3Down = head3Down;

    pICVars.encoderHandler.encoder1 = encoder1;
    pICVars.encoderHandler.prevEncoder1 = prevEncoder1;

    pICVars.encoderHandler.encoder2 = encoder2;
    pICVars.encoderHandler.prevEncoder2 = prevEncoder2;

    pICVars.encoderHandler.encoder1Dir = encoder1Dir;
    pICVars.encoderHandler.encoder2Dir = encoder2Dir;

}//end of Multi_IO_A_Control::getInspectControlVars
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::installNewRabbitFirmware
//
// Transmits the Rabbit firmware image to the Control board to replace the
// existing code.
//
// See corresponding function in the parent class Board.
//

public void installNewRabbitFirmware()
{

    //create an object to hold codes specific to the UT board for use by the
    //firmware installer method

    InstallFirmwareSettings settings = new InstallFirmwareSettings();
    settings.loadFirmwareCmd = LOAD_FIRMWARE_CMD;
    settings.noAction = NO_ACTION_CMD;
    settings.error = ERROR;
    settings.sendDataCmd = SEND_DATA_CMD;
    settings.dataCmd = DATA_CMD;
    settings.exitCmd = EXIT_CMD;

    //WIP HSS//  this should definitely be done later
    /*super.installNewRabbitFirmware(
            "Control", "Rabbit\\CAPULIN CONTROL BOARD.bin", settings);*/


}//end of Multi_IO_A_Control::installNewRabbitFirmware
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Control::various get/set functions
//

public boolean getOnPipeFlag(){return onPipeFlag;}

public boolean getInspectFlag(){return false;} //replace this???

public boolean getNewInspectPacketReady(){return newInspectPacketReady;}

public void setNewInspectPacketReady(boolean pValue)
    {newInspectPacketReady = pValue;}

//end of Multi_IO_A_Control::various get/set functions
//-----------------------------------------------------------------------------

}//end of class Multi_IO_A_Control
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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

    byte[] packet;

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

    //Commands for Control boards
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

    packet = new byte[RUN_DATA_BUFFER_SIZE];

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

    //updates have been applied, so clear dirty flag...since this method and
    //the method which handels the updates are synchronized, no updates will
    //have occurred while all this method has processed all the updates

    setHdwParamsDirty(false);

}//end of MultiIODevice::processChannelParameterChanges
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

@Override
void requestAllStatusPacket()
{

    super.requestAllStatusPacket();

    sendPacket(GET_ALL_STATUS_CMD, (byte)0);

}//end of MultiIODevice::requestAllStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::handleAllStatusPacket
//
// Extracts from packet and displays in the log panel all status and error
// information from the Host computer, the Rabbit, Master PIC, and all
// Slave PICs.
//
// The voltage present at the A/D converter input of each Slave PIC is also
// displayed.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//
// Packet Format from remote device:
//
// Rabbit Status Data
//
// 0xaa,0x55,0xbb,0x66,Packet ID        (these already removed from buffer)
// Rabbit Software Version MSB
// Rabbit Software Version LSB
// Rabbit Control Flags (MSB)
// Rabbit Control Flags (LSB)
// Rabbit System Status
// Rabbit Host Com Error Count MSB
// Rabbit Host Com Error Count LSB
// Rabbit Master PIC Com Error Count MSB
// Rabbit Master PIC Com Error Count LSB
// 0x55,0xaa,0x5a                       (unused)
//
// Master PIC Status Data
//
// Master PIC Software Version MSB
// Master PIC Software Version LSB
// Master PIC Flags
// Master PIC Status Flags
// Master PIC Rabbit Com Error Count
// Master PIC Slave PIC Com Error Count
// 0x55,0xaa,0x5a                       (unused)
//
// Slave PIC 0 Status Data
//
// Slave PIC I2C Bus Address (0-7)
// Slave PIC Software Version MSB
// Slave PIC Software Version LSB
// Slave PIC Flags
// Slave PIC Status Flags
// Slave PIC Master PIC Com Error Count
// Slave PIC Last read A/D value
// 0x55,0xaa,0x5a                       (unused)
// Slave PIC packet checksum
//
// ...packets for remaining Slave PIC packets...
//
// Master PIC packet checksum
//
// Rabbit's overall packet checksum appended by sendPacket function
//

@Override
int handleAllStatusPacket()
{

    int result = super.handleAllStatusPacket();

    int numBytesInPkt = 111; //includes Rabbit checksum byte

    byte[] buffer = new byte[numBytesInPkt];

    result = readBytesAndVerify(buffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    int i = 0, v;
    int errorSum = packetErrorCnt; //number of errors recorded by host

    logPanel.appendTS("\n----------------------------------------------\n");
    logPanel.appendTS("-- All Status Information --\n\n");

    logPanel.appendTS("Host com errors: " + packetErrorCnt + "\n\n");

    logPanel.appendTS(" - Rabbit Status Data -\n\n");

    //software version
    logPanel.appendTS(" " + buffer[i++] + ":" + buffer[i++]);

    //control flags
    logPanel.appendTS("," + String.format("0x%4x",
                            getUnsignedShortFromPacket(buffer, i))
                                                .replace(' ', '0'));
    i=i+2; //adjust for integer extracted above

    //system status
    logPanel.appendTS("," + String.format("0x%2x", buffer[i++])
                                                            .replace(' ', '0'));

    //host com error count
    v = getUnsignedShortFromPacket(buffer, i); i+=2; errorSum += v;
    logPanel.appendTS("," + v);

    //serial com error count
    v = getUnsignedShortFromPacket(buffer, i); i+=2; errorSum += v;
    logPanel.appendTS("," + v);

    //unused values
    logPanel.appendTS("," + buffer[i++] + "," + buffer[i++]+ "," + buffer[i++]);
    logPanel.appendTS("\n\n");

    logPanel.appendTS(" - Master PIC Status Data -\n\n");

    //software version
    logPanel.appendTS(" " + buffer[i++] + ":" + buffer[i++]);

    //flags
    logPanel.appendTS("," + String.format(
                            "0x%2x", buffer[i++]).replace(' ', '0'));

    //status flags
    logPanel.appendTS("," + String.format(
                            "0x%2x", buffer[i++]).replace(' ', '0'));

    //serial com error count
    v = buffer[i++]; errorSum += v; logPanel.appendTS("," + v);

    //I2C com error count
    v = buffer[i++]; errorSum += v; logPanel.appendTS("," + v);

    //unused values
    logPanel.appendTS("," + buffer[i++] + "," + buffer[i++]+ "," + buffer[i++]);
    logPanel.appendTS("\n\n");

    logPanel.appendTS(" - Slave PIC 0~7 Status Data -\n\n");

    int numSlaves = 8;

    for(int j=0; j<numSlaves; j++){

        logPanel.appendTS(buffer[i++] + "-"); //I2C bus address
        logPanel.appendTS(" " + buffer[i++] + ":" + buffer[i++]); //software ver
        logPanel.appendTS("," + String.format(
                               "0x%2x", buffer[i++]).replace(' ', '0')); //flags
        logPanel.appendTS("," + String.format(
                        "0x%2x", buffer[i++]).replace(' ', '0')); //status flags
        v = buffer[i++]; errorSum += v;
        logPanel.appendTS("," + v); //I2C com error count
        logPanel.appendTS("," + buffer[i++]); //last read A/D value
        //unused values
        logPanel.appendTS(","+buffer[i++]+","+buffer[i++]+ "," + buffer[i++]);
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

    logPanel.appendTS("Total com error count: " + errorSum + "\n\n");

    return(result);

}//end of MultiIODevice::handleAllStatusPacket
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

@Override
void requestAllLastADValues()
{

    super.requestAllLastADValues();

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

@Override
int handleAllLastADValuesPacket()
{

    int result = super.handleAllLastADValuesPacket();

    int numBytesInPkt = 26; //includes Rabbit checksum byte

    byte[] buffer = new byte[numBytesInPkt];

    result = readBytesAndVerify(buffer, numBytesInPkt, pktID);
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
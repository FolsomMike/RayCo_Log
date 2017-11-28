/******************************************************************************
* Title: PeakDevice.java
* Author: Hunter Schoonover
* Date: 10/09/17
*
* Purpose:
*
* This class is the parent class for subclasses which handle communication with
* Multi-IO Peak Device boards.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import static hardware.Channel.CATCH_HIGHEST;
import static hardware.Channel.CATCH_LOWEST;
import model.DataTransferIntMultiDimBuffer;
import model.DataTransferSnapshotBuffer;
import model.IniFile;
import model.SharedSettings;
import toolkit.MKSInteger;
import view.LogPanel;


//-----------------------------------------------------------------------------
// class PeakDevice
//

public class PeakDevice extends MultiIODevice
{

    private int data;
    private int[] snapData;
    private int[] mapData;

    private int snapshotPeakType;

    PeakSnapshotBuffer peakSnapshotBuffer;
    SampleMetaData snapshotMeta = new SampleMetaData(0);
    @Override public SampleMetaData getSnapshotMeta(){ return(snapshotMeta); }
    @Override public void setSnapshotDataBuffer(DataTransferSnapshotBuffer pV)
        { snapshotMeta.dataSnapshotBuffer = pV; }
    @Override public DataTransferSnapshotBuffer getSnapshotDataBuffer()
        { return(snapshotMeta.dataSnapshotBuffer); }

    SampleMetaData mapMeta = new SampleMetaData(0);
    @Override public SampleMetaData getMapMeta(){ return(mapMeta); }

    @Override public void setMapDataBuffer(DataTransferIntMultiDimBuffer pV) {
                                                   mapMeta.dataMapBuffer = pV;}
    @Override public DataTransferIntMultiDimBuffer getMapDataBuffer() {
                                               return(mapMeta.dataMapBuffer); }

//-----------------------------------------------------------------------------
// PeakDevice::PeakDevice (constructor)
//

public PeakDevice(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                        SharedSettings pSettings, boolean pSimMode)
{

    super(pDeviceNum, pLogPanel, pConfigFile, pSettings, pSimMode);

    mapMeta.deviceNum = pDeviceNum;
    snapshotMeta.deviceNum = pDeviceNum;

}//end of PeakDevice::PeakDevice (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Do not call loadConfigSettings here...the subclasses should do it.
//

@Override
public void init()
{

    super.init();

}// end of PeakDevice::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){

    super.initAfterConnect();
    
    //debug mks

    requestAllStatusPacket();

    setLinearLocationsOfChannels();

    waitSleep(300);

    //DEBUG HSS// requestAllLastADValues();

    //DEBUG HSS// requestRunDataPacket(); //DEBUG HSS// -- remove line later

    //waitSleep(5000); //without the sleep, calling twice locks up the PICs -- why?
    //requestAllStatusPacket();

    //debug mks end

}//end of PeakDevice::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::getDeviceDataAndReset
//
// Gets the device data (peaks, snapshot, and map) and resets the values.
//
// This function makes use of DeviceData so that actions are synchronized.
//

@Override
public boolean getDeviceDataAndReset(PeakData pPeakData,
                                    PeakSnapshotData pSnapshotData,
                                    PeakMapData pMapData)
{

    super.getDeviceDataAndReset(pPeakData, pSnapshotData, pMapData);

    return deviceData.getData(pPeakData, pSnapshotData, pMapData);

}// end of PeakDevice::getDeviceDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::getPeakForChannelAndReset
//
// Retrieves the current value of the peak for channel pChannel and resets
// the peak to the reset value.
//
// This class returns an object as the peak may be of various data types.
//

@Override
public void getPeakForChannelAndReset(int pChannel, MKSInteger pPeakValue)
{

    super.getPeakForChannelAndReset(pChannel, pPeakValue);

    channels[pChannel].getPeakAndReset(pPeakValue);

}// end of PeakDevice::getPeakForChannelAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::getPeakDataAndReset
//
// Retrieves the current value of the peak for channel pChannel along with
// all relevant info for the channel such as the chart & trace to which it is
// attached.
//
// Resets the peak to the reset value.
//

@Override
public void getPeakDataAndReset(int pChannel, PeakData pPeakData)
{

    super.getPeakDataAndReset(pChannel, pPeakData);

    channels[pChannel].getPeakDataAndReset(pPeakData);

}// end of PeakDevice::getPeakDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::getPeakSnapshotDataAndReset
//
// Retrieves the current values of the snapshot data peaks along with all
// relevant info for the channel such as the chart & graph to which it is
// attached.
//
// All data in the pPeakMapData.metaArray is set to the snapshot system number
// of this device so the data can be identified as necessary.
//
// Resets the peaks to the reset value.
//
// Returns true if the peak has been updated since the last call to this method
// or false otherwise.
//

@Override
public boolean getPeakSnapshotDataAndReset(PeakSnapshotData pPeakSnapData)
{

    if(peakSnapshotBuffer == null) { return(false); }

    pPeakSnapData.meta = snapshotMeta; //channel/buffer/graph etc. info
    pPeakSnapData.peak = peakSnapshotBuffer.peak;

    boolean peakUpdated
                = peakSnapshotBuffer.getPeakAndReset(pPeakSnapData.peakArray);

    return(peakUpdated);

}// end of PeakDevice::getPeakSnapshotDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::getPeakMapDataAndReset
//
// Retrieves the current values of the map data peaks along with all relevant
// info for the channel such as the chart & graph to which it is attached.
//
// All data in the pPeakMapData.metaArray is set to the map system number of
// this device so the data can be identified as necessary.
//
// Resets the peaks to the reset value.
//
// Returns true if the peak has been updated since the last call to this method
// or false otherwise.
//

@Override
public boolean getPeakMapDataAndReset(PeakMapData pPeakMapData)
{

    if(peakMapBuffer == null) { return(false); }

    pPeakMapData.meta = mapMeta; //channel/buffer/graph etc. info

    boolean peakUpdated = peakMapBuffer.getPeakAndReset(pPeakMapData.peakArray);

    pPeakMapData.setMetaArray(mapMeta.system);

    return(peakUpdated);

}// end of PeakDevice::getPeakMapDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::sendSetPotPacket
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

@Override
void sendSetPotPacket(int pHdwChannel, int pGainOrOffset, int pValue)
{

    super.sendSetPotPacket(pHdwChannel, pGainOrOffset, pValue);

    int slavePICAddr, potNum;

    //even number channels enabled by PIC with address same as channel num
    //  Offset = Pot 0 inside chip
    //  Gain   = Pot 1 inside chip

    //odd number channels enabled by PIC with address one less than channel num
    //  Offset = Pot 3 inside chip
    //  Gain   = Pot 2 inside chip

    if((pHdwChannel % 2) == 0){
        slavePICAddr = pHdwChannel;
        potNum = (pGainOrOffset == OFFSET_POT) ? 0 : 1;
    }else{
        slavePICAddr = pHdwChannel - 1;
        potNum = (pGainOrOffset == OFFSET_POT) ? 3 : 2;
    }

    sendPacket(SET_POT_CMD, (byte)slavePICAddr, (byte)potNum, (byte)pValue);

}//end of PeakDevice::sendSetPotPacket
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

@Override
void sendSetGainPacket(int pHdwChannel, int pValue)
{

    super.sendSetGainPacket(pHdwChannel, pValue);

    sendSetPotPacket(pHdwChannel, GAIN_POT, pValue);

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

@Override
void sendSetOffsetPacket(int pHdwChannel, int pValue)
{

    super.sendSetOffsetPacket(pHdwChannel, pValue);

    sendSetPotPacket(pHdwChannel, OFFSET_POT, pValue);

}//end of MultiIODevice::sendSetOffsetPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::sendSetOnOffPacket
//
// Sends a command to the remote device to set the On/Off state for pHdwChannel
// to pValue.
//
// Sends value of 1 if state is on; value of 0 if off.
//
// The remote should return an ACK packet.
//

@Override
void sendSetOnOffPacket(int pHdwChannel, boolean pValue)
{

    super.sendSetOnOffPacket(pHdwChannel, pValue);

    sendPacket(SET_ONOFF_CMD, (byte)pHdwChannel, (byte)(pValue ? 1 : 0));

}//end of PeakDevice::sendSetOnOffPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::sendSetLocationPacket
//
// Sends a packet to the remote device to set the location of pHdwChannel to
// pValue.
//

@Override
void sendSetLocationPacket(int pHdwChannel, int pValue)
{

    super.sendSetLocationPacket(pHdwChannel, pValue);

    sendPacket(SET_LOCATION_CMD, (byte)pHdwChannel, (byte)pValue);

}//end of PeakDevice::sendSetLocationPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::setLinearLocationsOfChannels
//
// Sets the linear locations of each channel to their proper values.
//

@Override
void setLinearLocationsOfChannels()
{

    super.setLinearLocationsOfChannels();

    for(Channel channel : channels){

        if (channel.getBoardChannel() == -1) { continue; }

        sendSetLocationPacket(channel.getBoardChannel(),
                                        channel.getLinearLocation());
    }

}//end of PeakDevice::setLinearLocationsOfChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::sendSetClockPacket
//
// Sends a packet to the remote device to set the clock position of pHdwChannel
// to pValue.
//

@Override
void sendSetClockPacket(int pHdwChannel, int pValue)
{

    super.sendSetClockPacket(pHdwChannel, pValue);

    sendPacket(SET_CLOCK_CMD, (byte)pHdwChannel, (byte)pValue);

}//end of PeakDevice::sendSetClockPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::processChannelParameterChanges
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

    // invoke all devices with changed values to process those changes

    for(Channel channel : channels){
        if (channel.getHdwParams().getHdwParamsDirty()){
            if(channel.getHdwParams().isGainDirty()){
                sendSetGainPacket(channel.getBoardChannel(),
                                channel.getHdwParams().getGain(false));
            }
            if(channel.getHdwParams().isOffsetDirty()){
                sendSetOffsetPacket(channel.getBoardChannel(),
                                channel.getHdwParams().getOffset(false));
            }
            if(channel.getHdwParams().isOnOffDirty()){
                sendSetOnOffPacket(channel.getBoardChannel(),
                            channel.getHdwParams().getOnOff(false));
            }
            channel.getHdwParams().setHdwParamsDirty(false);
        }
    }

}//end of PeakDevice::processChannelParameterChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::initAfterLoadingConfig
//
// Further initializes the object using data loaded from the config file.
// Must be called by subclasses after they call loadConfigSettings(), which
// they must call themselves as they specify the section to be read from.
//

@Override
public void initAfterLoadingConfig()
{

    super.initAfterLoadingConfig();

    if(numClockPositions != 0){ mapData = new int[numClockPositions]; }
    snapData = new int[128]; //WIP HSS// number of bytes needs to be specified in ini file

    setUpPeakMapBuffer();

    setUpPeakSnapshotBuffer();

    setUpChannels();

    channelPeaks = new int[channels.length];

    mapMeta.numClockPositions = numClockPositions;

}// end of PeakDevice::initAfterLoadingConfig
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::setClockPositionsOfChannels
//
// Sets the clock position of each channel to what it's proper value.
//

void setClockPositionsOfChannels()
{

    for(Channel channel : channels){

        if (channel.getBoardChannel() == -1) { continue; }

        sendSetClockPacket(channel.getBoardChannel(),
                                        channel.getClockPosition());

    }

}//end of PeakDevice::setClockPositionsOfChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::handleAllStatusPacket
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

}//end of PeakDevice::handleAllStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::collectData
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

    boolean processPacket = getRunPacketFromDevice(runDataPacket);

    if (processPacket){

        //first channel's buffer location specifies start of channel data section
        int index = channels[0].getBufferLoc();

        //Device ALWAYS sends back enough bytes for 16 channels, even if ini
        //file specifies otherwise. (2 bytes per channel)
        int clockMapIndex = index+32;
        int snapshotIndex = clockMapIndex+48;

        int peak=0;
        for(int i=0; i<channels.length; i++){

            data = getUnsignedShortFromPacket(runDataPacket, index);
            data = Math.abs(data - AD_ZERO_OFFSET);
            index+=2; //skip two because short is 2 bytes
            channelPeaks[i] = data;

            if (data>peak) { peak=data; }

        }

        extractSnapshotData(runDataPacket, snapshotIndex);

        if(numClockPositions > 0) { extractMapData(runDataPacket, clockMapIndex); }
        
        deviceData.putData(channelPeaks, peak, snapData, mapData);

    }

    //send a request to the device for the next packet
    if (sharedSettings.opMode == SharedSettings.SCAN_MODE
        || sharedSettings.opMode == SharedSettings.INSPECT_MODE
        || sharedSettings.opMode == SharedSettings.INSPECT_WITH_TIMER_TRACKING_MODE) 
    {
        requestRunDataPacket();
    }

}// end of PeakDevice::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::requestRunDataPacket
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

    //may also need check to see if in cal mode????? //DEBUG HSS// //WIP HSS//
    
    return super.requestRunDataPacket();//DEBUG HSS// remove later
    
    /*if (sharedSettings.timerDrivenTracking 
            || sharedSettings.timerDrivenTrackingInCalMode) {
        return requestRunDataPacketForScanOrTimerMode();
    } else {
        return requestRunDataPacketForInspectMode();
    }*/
    
}//end of PeakDevice::requestRunDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::requestRunDataPacketForInspectMode
//
// Requests a run data packet from the device if time do so.
//
// This function is specifically for INSPECT mode which uses encoder data to
// drive the traces rather than a timer.
//
// Peak data is requested each time the encoder moves the specified tigger
// amount.
//

public boolean requestRunDataPacketForInspectMode()
{
    
    return super.requestRunDataPacket(); //DEBUG HSS//

    //process position information from whatever device is handling the encoder
    //inputs

    /*boolean newPositionData = collectEncoderDataInspectMode();

    //call collectAnalogData again if new position data has been received --
    //this makes sure the new position in the buffer is filled with something --
    //the position will usually be overwritten by the next peak data
    //wip mks -- not required to call collectAnalogData any more since
    // the new TraceData class fills empty buffer spaces with previous data?

    //also send a request to the remote device(s) for a peak data packet
    //the returned data packet will be processed on subsequent calls to
    //collectData

    if (newPositionData){

        collectAnalogData();

        analogDriver.requestPeakDataForAllBoards();

    }*/ //DEBUG HSS// fix this later //WIP HSS//

}//end of PeakDevice::requestRunDataPacketForInspectMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::extractMapData
//
// Extracts map data from pPacket beginning at position pIndex.
//
// Returns the updated value of pIndex which will then point at the next
// bayte after the map data which was extracted.
//
// Should be overridden by children classes.
//

private int extractMapData(byte[] pPacket, int pIndex)
{

    for(int i=0; i<numClockPositions; i++){

        int raw = getUnsignedByteFromPacket(pPacket, pIndex)/3; //WIP HSS// -- divisor should be read from config file

        for (int j=0; j<numClockPositions; j++) {

            if (i == clockTranslations[j]) { mapData[j] = raw;  }

        }

        pIndex++;

    }

    return pIndex;

}// end of PeakDevice::extractMapData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::extractSnapshotData
//
// Extracts snapshot data from pPacket beginning at position pIndex.
//
// Returns the updated value of pIndex which will then point at the next
// bayte after the map data which was extracted.
//

private int extractSnapshotData(byte[] pPacket, int pIndex)
{

    //not used, but good to have
    int lastEnteredAddr = pPacket[pIndex++];

    for(int i=0; i<snapData.length; i++) {
        //retrieve the next byte from packet
        snapData[i]=getUnsignedByteFromPacket(pPacket, pIndex++)-AD_ZERO_OFFSET;
    }

    return(pIndex);

}// end of PeakDevice::extractSnapshotData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::setUpChannels
//
// Creates and sets up the channels.
//

private void setUpChannels()
{

    if (numChannels <= 0){ return; }

    channels = new Channel[numChannels];

    BoardChannelParameters params = null;
    int lastBoardChannel = -1; double lastDist = 0.0;     
    for(int i=0; i<numChannels; i++){

        channels[i] = new Channel(getDeviceNum(), i, configFile, sharedSettings);
        channels[i].init();

        //give same BoardChannelParameters instance to software channels that
        //represent the same board channel
        if (channels[i].getBoardChannel()==lastBoardChannel&&params!=null)
        {
            channels[i].setHdwParams(params);
        }
        else {
            channels[i].setHdwParams(params = new BoardChannelParameters());
        }
        
        //set all channels that have paired distance from device start values to
        //match the distance of the previous channel
        if (channels[i].getDistanceSensorToFrontEdgeOfHead()==-1)
        {
            channels[i].setDistanceSensorToFrontEdgeOfHead(lastDist); 
        }
        
        lastBoardChannel = channels[i].getBoardChannel();
        lastDist = channels[i].getDistanceSensorToFrontEdgeOfHead();

    }

}// end of PeakDevice::setUpChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::updateChannelParameters
//
// Updates channel parameters. pParamType specifies the type of value, such as
// "Gain Spinner", "Offset Spinner", etc. pChannelNum specifies which
// Channel object in the channels array is to be modified while pValue is the
// new value as a String.
//
// If pForceUpdate is false, the value will only be updated and its dirty flag
// set true if the new value differs from the old value.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// Returns true if the value was updated, false otherwise.
//
// Should be overridden by children.
//

@Override
public boolean updateChannelParameters(String pParamType, String pChannelNum,
                                           String pValue, boolean pForceUpdate)
{

    super.updateChannelParameters(pParamType, pChannelNum, pValue, pForceUpdate);

    int chNum = Integer.parseInt(pChannelNum);

    boolean result = false;

        switch (pParamType) {
            case "Gain Spinner":
                result = channels[chNum].getHdwParams().setGain(pValue,
                                                                pForceUpdate);
                break;
            case "Offset Spinner":
                result = channels[chNum].getHdwParams().setOffset(pValue,
                                                                pForceUpdate);
                break;
            case "On-Off Checkbox":
                result = channels[chNum].getHdwParams().setOnOff(pValue,
                                                                pForceUpdate);
                break;
        }

    if(result) { setHdwParamsDirty(true); }

    return(result);

}//end of PeakDevice::updateChannelParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::catchMapPeak
//
// Catches the passed in clock map data.
//

@Override
public void catchMapPeak(int[] pData)
{

    super.catchMapPeak(pData);

     peakMapBuffer.catchPeak(pData);

};// end of PeakDevice::catchMapPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::catchSnapshotPeak
//
// Catches the passed in snapshot data.
//

@Override
public void catchSnapshotPeak(int pPeak, int[] pData)
{

    super.catchSnapshotPeak(pPeak, pData);

    peakSnapshotBuffer.catchPeak(pPeak, pData);

}// end of PeakDevice::catchSnapshotPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::setUpPeakSnapshotBuffer
//
// Creates and sets up the appropriate PeakBuffer subclass to capture the type
// of peak specified in the config file, i.e. highest value, lowest value, etc.
//

public void setUpPeakSnapshotBuffer()
{

    switch (snapshotPeakType){

        case CATCH_HIGHEST:
            peakSnapshotBuffer = new HighPeakSnapshotBuffer(0, 128); //WIP HSS// size needs to be ini
            peakSnapshotBuffer.setResetValue(0);
            break;

        case CATCH_LOWEST:
            peakSnapshotBuffer = new LowPeakSnapshotBuffer(0, 128); //WIP HSS// size needs to be ini
            peakSnapshotBuffer.setResetValue(0);
            break;

        default:
            peakSnapshotBuffer = new HighPeakSnapshotBuffer(0, 128); //WIP HSS// size needs to be ini
            peakSnapshotBuffer.setResetValue(Integer.MIN_VALUE);
            break;

    }

    peakSnapshotBuffer.reset();

}// end of PeakDevice::setUpPeakSnapshotBuffer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::setUpPeakMapBuffer
//
// Creates and sets up the appropriate PeakArrayBufferInt subclass to capture
// the type of peak specified in the config file, i.e. highest value, lowest
// value, etc.
//
// If the number of clock positions is zero, the buffers are not created.
//

public void setUpPeakMapBuffer()
{

    if (numClockPositions == 0) { return; }

    switch (mapPeakType){

        case CATCH_HIGHEST:
            peakMapBuffer = new HighPeakArrayBufferInt(0, numClockPositions);
            peakMapBuffer.setResetValue(Integer.MIN_VALUE);
            break;

        case CATCH_LOWEST:
            peakMapBuffer = new HighPeakArrayBufferInt(0, numClockPositions);
            peakMapBuffer.setResetValue(Integer.MAX_VALUE);
            break;

        default:
            peakMapBuffer = new HighPeakArrayBufferInt(0, numClockPositions);
            peakMapBuffer.setResetValue(Integer.MIN_VALUE);
            break;

    }

    peakMapBuffer.reset();

}// end of PeakDevice::setUpPeakMapBuffer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    super.loadConfigSettings();

    numChannels = configFile.readInt(section, "number of channels", 0);

    numClockPositions = configFile.readInt(
                                    section, "number of clock positions", 12);

    if(numClockPositions > 0) loadClockMappingTranslation(section);

    ////WIP HSS// actually load snapshot settings from ini file
    snapshotPeakType = CATCH_HIGHEST; //WIP HSS// temp setting, should be loaded from ini
    snapshotMeta.chartGroup = configFile.readInt(section, "snapshot chart group", -1);
    snapshotMeta.chart = configFile.readInt(section, "snapshot chart", -1);
    snapshotMeta.graph = configFile.readInt(section, "snapshot graph", -1);

    String s;

    s = configFile.readString(section, "map data type", "integer");
    parseDataType(s);

    s = configFile.readString(section, "map peak type", "catch highest");
    parsePeakType(s);

    mapMeta.chartGroup = configFile.readInt(section, "map chart group", -1);

    mapMeta.chart = configFile.readInt(section, "map chart", -1);

    mapMeta.graph = configFile.readInt(section, "map graph", -1);

    mapMeta.system = configFile.readInt(section, "map system", -1);

}// end of PeakDevice::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::loadClockMappingTranslation
//
// Loads the translation tables for translating clock positions in the data map
// received from the device into clock positions on a 3D map.
//

void loadClockMappingTranslation(String pSection)
{

    clockTranslations = new int[numClockPositions];

    //translations default to 0>0,1>1,2>2 etc.
    for(int i=0; i<clockTranslations.length; i++){
        clockTranslations[i] = i;
    }

    numGridsHeightPerSourceClock = configFile.readInt(pSection,
                                "number of grids height per source clock", 1);

    numGridsLengthPerSourceClock = configFile.readInt(pSection,
                                "number of grids length per source clock", 1);

    String line = configFile.readString(pSection,
                                "source clock to grid clock translation", "");
    if (!line.isEmpty()){

        //before split: "0>0:5,1>6:11,..."
        String[] translations = line.split(",");


        //strip out source clock and from/to translations
        for(String trans : translations){

            //before split: "0>0:5", "1>6:11", etc,
            String[] srcClkAndFromTo = trans.split(">");

            //do nothing else if missing values
            if(srcClkAndFromTo.length < 2
                || srcClkAndFromTo[0].isEmpty()
                || srcClkAndFromTo[1].isEmpty()) {
                continue;
            }

            //before split: "0:5", "6:11", etc.
            String[] fromTo = srcClkAndFromTo[1].split(":");

            //do nothing else if missing values
            if(fromTo.length < 2
                || fromTo[0].isEmpty()
                || fromTo[1].isEmpty()) {
                continue;
            }


            //convert values to integers and store them
            try{

                int sourceClock = Integer.parseInt(srcClkAndFromTo[0]);
                int gridClockFrom = Integer.parseInt(fromTo[0]);
                int gridClockTo = Integer.parseInt(fromTo[1]);

                //store source clock in all grid clock positions that it covers
                for (int i=gridClockFrom;
                        i<=gridClockTo && i<clockTranslations.length && i>=0;
                        i++)
                {
                    clockTranslations[i] = sourceClock;
                }

            }
            catch(NumberFormatException e){ }
        }
    }

}// end of PeakDevice::loadClockMappingTranslation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//
//

@Override
public void loadCalFile(IniFile pCalFile)
{

    super.loadCalFile(pCalFile);

    for (Channel c : channels) { c.loadCalFile(pCalFile); }

}//end of PeakDevice::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakDevice::saveCalFile
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

    for (Channel c : channels) { c.saveCalFile(pCalFile); }

}//end of PeakDevice::saveCalFile
//-----------------------------------------------------------------------------

}//end of class PeakDevice
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
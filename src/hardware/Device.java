/******************************************************************************
* Title: Device.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class is the parent class for subclasses which handle communication with
* various remote devices which perform data acquisition, outputs, etc.
*
* All values which are adjustable via GUI controls which are then transmitted
* via Ethernet are "flagged" variables which can be written to by the GUI
* thread and then read later by the thread which handles the Ethernet
* connection.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import static hardware.Channel.CATCH_HIGHEST;
import static hardware.Channel.CATCH_LOWEST;
import static hardware.Channel.DOUBLE_TYPE;
import static hardware.Channel.INTEGER_TYPE;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DataTransferIntMultiDimBuffer;
import model.DataTransferSnapshotBuffer;
import model.IniFile;
import model.SharedSettings;
import toolkit.MKSInteger;
import view.LogPanel;

//-----------------------------------------------------------------------------
// class Device
//

public class Device implements Runnable, ControlDevice
{

    boolean shutDown = false;

    final IniFile configFile;
    final String section;
    final String calFileSection;
    final SharedSettings sharedSettings;
    
    protected boolean canBeControlDevice = false;
    public boolean canBeControlDevice() { return canBeControlDevice; }
    
    boolean flaggingEnabled = true;
    
    protected byte[] allEncoderValuesBuffer;
    protected int allEncodersPacketSize = 0; //needs to be set by child classes

    private final int deviceNum;
    public int getDeviceNum(){ return(deviceNum); }
    String title = "", shortTitle = "";
    public String getTitle() { return title; }
    String deviceType = "";
    public String getDeviceType(){ return(deviceType); }
    String deviceSubtype = "";
    public String getDeviceSubtype(){ return(deviceSubtype); }
    
    double photoEye1DistanceToFrontEdge = 0.0;
    public double getPhotoEye1DistanceToFrontEdge(){ return photoEye1DistanceToFrontEdge; }
    
    double photoEye2DistanceToFrontEdge = 0.0;
    public double getPhotoEye2DistanceToFrontEdge(){ return photoEye2DistanceToFrontEdge; }
    
    int numChannels = 0;
    public int getNumChannels(){ return(numChannels); }
    Channel[] channels = null;
    public Channel[] getChannels(){ return(channels); }

    private boolean hdwParamsDirty = false;
    public boolean getHdwParamsDirty(){ return hdwParamsDirty; }
    public void setHdwParamsDirty(boolean pState){ hdwParamsDirty = pState;}

    int numClockPositions;
    int[] clockTranslations;
    int numGridsHeightPerSourceClock, numGridsLengthPerSourceClock;

    byte runDataBuffer[] = new byte[RUN_DATA_BUFFER_SIZE];
    int runDataPacketSize = 0; //needs to be set by child classes
    private boolean newRunData = false;
    private int prevRbtRunDataPktCnt = -1;
    private int rbtRunDataPktCntError = 0;
    private int prevPICRunDataPktCnt = -1;
    private int picRunDataPktCntError = 0;
    
    byte[] inspectBuffer;
    int inspectPacketSize = 0; //needs to be set by child classes
    int inspectPacketCount = 0;
    private boolean newInspectPacket = false;
    private boolean newInspectData = false;
    @Override public boolean getNewInspectDataReady() { return newInspectData; }
    @Override public void setNewInspectDataReady(boolean pState) { newInspectData = pState; }
    
    byte[] monitorBuffer;
    int monitorPacketSize = 0; //needs to be set by child classes
    int monitorPacketRequestTimer = 0;

    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    byte[] inBuffer;
    byte[] outBuffer;
    DataOutputStream byteOut = null;
    DataInputStream byteIn = null;

    private InetAddress ipAddr = null;
    public InetAddress getIPAddr(){ return(ipAddr); }
    private String ipAddrS;

    private boolean waitingForRemoteResponse = false;
    protected void setWaitingForRemoteResponse(boolean pW) { waitingForRemoteResponse = pW; }

    int pktID;
    boolean reSynced;
    int reSyncCount = 0, reSyncPktID;
    int packetErrorCnt = 0;

    int numACKsExpected = 0;
    int numACKsReceived = 0;

    protected boolean connectionAttemptCompleted = false;
    public boolean getConnectionAttemptCompleted(){
                                         return (connectionAttemptCompleted); }
    protected boolean connectionSuccessful = false;
    public boolean getConnectionSuccessful(){ return (connectionSuccessful); }

    //WIP HSS//
    protected DeviceData deviceData;
    protected int[] channelPeaks;
    //WIP HSS//

    protected boolean hasSnapshot = false;
    public boolean hasSnapshot() { return hasSnapshot; }
    public SampleMetaData getSnapshotMeta(){ return null; }
    public void setSnapshotDataBuffer(DataTransferSnapshotBuffer pV) {}
    public DataTransferSnapshotBuffer getSnapshotDataBuffer() { return null; }
    public SampleMetaData getMapMeta(){ return null; }
    public void setMapDataBuffer(DataTransferIntMultiDimBuffer pV) {}
    public DataTransferIntMultiDimBuffer getMapDataBuffer() { return null; }

    boolean simMode;

    protected boolean hasMap = false;
    public boolean hasMap() { return hasMap; }
    int mapDataType;
    int mapPeakType;

    PeakArrayBufferInt peakMapBuffer;

    LogPanel logPanel;
    
    //START control vars
    short rabbitControlFlags = 0;
    
    int encoder1, prevEncoder1;
    int encoder2, prevEncoder2;
    int encoder1Dir, encoder2Dir;
    
    //number of counts each encoder moves to trigger an inspection data packet
    //these values are read later from the config file
    int encoder1DeltaTrigger, encoder2DeltaTrigger;
    
    boolean onPipeFlag = false;
    boolean entryEyeFlag = false;
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
    //END control vars

    final static int RUN_DATA_BUFFER_SIZE = 1024;
    final static int RUNTIME_PACKET_SIZE = 50;

    final static int OUT_BUFFER_SIZE = 255;
    final static int IN_BUFFER_SIZE = 255;

//-----------------------------------------------------------------------------
// Device::Device (constructor)
//

public Device(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                SharedSettings pSettings, boolean pSimMode)
{

    deviceNum = pDeviceNum; configFile = pConfigFile; logPanel = pLogPanel;
    sharedSettings = pSettings; simMode = pSimMode;

    outBuffer = new byte[OUT_BUFFER_SIZE];
    inBuffer = new byte[IN_BUFFER_SIZE];

    section = "Device " + deviceNum + " Settings";
    calFileSection = "Device " + deviceNum + " Settings";
    
    canBeControlDevice = false;

}//end of Device::Device (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    configureForUseAsControlDevice(); //will only configure if necessary

}// end of Device::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::configureForUseAsControlDevice
//
// If this device serves as a Control Device, it is set up as one.
//

private void configureForUseAsControlDevice()
{

    if (!canBeControlDevice()) { return; } //bail if not Control Device
    
    monitorBuffer = new byte[monitorPacketSize];
    inspectBuffer = new byte[inspectPacketSize];

    allEncoderValuesBuffer = new byte[allEncodersPacketSize];

}// end of Device::configureForUseAsControlDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::driveSimulation
//
// Drive any simulation functions if they are active.  This function is usually
// called from a thread.
//
// Should be overridden by child classes to provide custom handling.
//

public void driveSimulation()
{

    if (simMode && socket != null) {
        ((Simulator)socket).processDataPackets(false);
    }

}//end of Device::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::initAfterLoadingConfig
//
// Further initializes the object using data loaded from the config file.
// Must be called by subclasses after they call loadConfigSettings(), which
// they must call themselves as they specify the section to be read from.
//

public void initAfterLoadingConfig()
{

    logPanel.setTitle(shortTitle);

    deviceData = new DeviceData(this);
    deviceData.init();

}// end of Device::initAfterLoadingConfig
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendPacket
//
// Sends a packet with command code pCommand followed by a variable number of
// bytes (one or more) to the remote device,
//
// A header is prepended and a checksum byte appended. The checksum includes
// the command byte and all bytes in pBytes, but not the header bytes.
//

void sendPacket(byte pCommand, byte... pBytes)
{
    
    if (byteOut == null) {return;}  //do nothing if the port is closed

    int i = 0, checksum;

    outBuffer[i++] = (byte)0xaa; outBuffer[i++] = (byte)0x55;
    outBuffer[i++] = (byte)0xbb; outBuffer[i++] = (byte)0x66;

    outBuffer[i++] = pCommand;        //command byte included in checksum
    checksum = pCommand;

    for(int j=0; j<pBytes.length; j++){
        outBuffer[i++] = pBytes[j];
        checksum += pBytes[j];
    }

    //calculate checksum and put at end of buffer
    outBuffer[i++] = (byte)(0x100 - (byte)(checksum & 0xff));

    //send packet to remote
    if (byteOut != null) {
        try{
              byteOut.write(outBuffer, 0 /*offset*/, i); byteOut.flush();
        }
        catch (IOException e) {
            logSevere(e.getMessage() + " - Error: 188");
        }
    }

}//end of Device::sendPacket
//-----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// Device::readBytesAndVerify
//
// Attempts to read pNumBytes number of bytes from ethernet into pBuffer and
// verifies the data using the last byte as a checksum.
//
// Note: pNumBytes count should include the data bytes plus the checksum byte.
//
// The packet ID should be provided via pPktID -- it is only used to verify the
// checksum as it is included in that calculation by the sender.
//
// Returns the number of bytes read, including the checksum byte.
// On checksum error, returns -1.
// If pNumBytes are not available after waiting, returns -2.
// On IOException, returns -3.
//

int readBytesAndVerify(byte[] pBuffer, int pNumBytes, int pPktID)
{
    
    if (byteIn == null) {return -1;}  //do nothing if the port is closed

    try{

        int timeOutProcess = 0;

        while(timeOutProcess++ < 2){
            if (byteIn.available() >= pNumBytes) {break;}
            waitSleep(10);
        }

        if (byteIn.available() >= pNumBytes) {
            byteIn.read(pBuffer, 0, pNumBytes);
        }else{
            packetErrorCnt++; return(-2);
        }

    }// try
    catch(IOException e){
        packetErrorCnt++;
        logSevere(e.getMessage() + " - Error: 281");
        return(-3);
    }

    byte sum = (byte)pPktID; //packet ID is included in the checksum

    //validate checksum by summing the packet id and all data

    for(int i = 0; i < pNumBytes; i++){ sum += pBuffer[i]; }

    if ( (sum & 0xff) == 0) { return(pNumBytes); }
    else{ packetErrorCnt++; return(-1); }

}//end of Device::readBytesAndVerify
//----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::isReadyToAdvanceInsertionPoints
//
// Returns whether or not the device is ready to advance the the transfer buffer
// insertion points. By default, all devices are always ready.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
public boolean isReadyToAdvanceInsertionPoints()
{

    return true;

}// end of Device::isReadyToAdvanceInsertionPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleACKPackets
//
// Handles ACK_CMD packets. Increments the numACKsReceive counter.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//


public int handleACKPackets()
{

    numACKsReceived++;

    return 0;

}//end of Device::handleACKPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::requestMonitorPacket
//
// Sends a request to the device for a packet with monitor data such as signal
// photo-eye states, and encoder values.
//
// When in monitor mode, it is expected that devices will send back monitor
// packets continously without request. This is generally only used when those
// devices fail to do send back a packet for a certain period of time.
//
// The returned packet will be handled by handleMonitorPacket. See that
// method for more details.
//
// Overridden by children classes for custom handling.
//

boolean requestMonitorPacket()
{

    //waiting for remote response or not a control return false since we bailed
    if (waitingForRemoteResponse || !canBeControlDevice()) { return false; }

    //return true because we did not bail
    return waitingForRemoteResponse = true;

}//end of Device::requestMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleMonitorPacket
//
// Transfers debugging data received from the remote into an array.
//
// The array the data is put into is generally gathered by external classes
// from getMonitorPacket(). See that method for more details.
//
// Returns number of bytes retrieved from the socket.
//
// Overridden by children classes for custom handling.
//

public int handleMonitorPacket()
{
    
    waitingForRemoteResponse = false;

    int numBytesInPkt = monitorPacketSize; //includes Rabbit checksum byte
    
    monitorPacketRequestTimer = 0; //reset since we got a packet

    int result;
    result = readBytesAndVerify(monitorBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    return result;

}//end of Device::handleMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getMonitorPacket
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
        if (monitorPacketRequestTimer++ == 50){
            monitorPacketRequestTimer = 0;
            requestMonitorPacket();
        }
    }

    return monitorBuffer;

}//end of Device::getMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::requestAllEncoderValuesPacket
//
// Requests a packet from the remote with all encoder values saved at different
// points in the inspection process.
//
// Note that the values will not be valid until the packet is received. If
// any encoder value is Integer.MAX_VALUE, the packet has not been received.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
public void requestAllEncoderValuesPacket()
{

}//end of Device::requestAllEncoderValuesPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::requestInspectPacket
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

    //waiting for remote response or not a control return false since we bailed
    if (waitingForRemoteResponse || !canBeControlDevice()) { return false; }

    //return true because we did not bail
    return waitingForRemoteResponse = true;

}//end of Device::requestInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleInspectPacket
//
// Copies the remainder of the packet from the ethernet buffer to the
// inspectBuffer for later retrieval.
//
// Sets newInspectData flag true.
//
// Overridden by children classes for custom handling.
//

public int handleInspectPacket()
{

    waitingForRemoteResponse = false;

    int numBytesInPkt = inspectPacketSize; //includes Rabbit checksum byte

    int result;
    result = readBytesAndVerify(inspectBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    newInspectPacket = true;

    return(result);

}// end of Device::handleInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getInspectControlVars
//
// Transfers local variables related to inspection control signals and encoder
// counts.
//

@Override
public void getInspectControlVars(InspectControlVars pICVars)
{

    pICVars.onPipeFlag = onPipeFlag;
    
    pICVars.entryEyeFlag = entryEyeFlag;

    pICVars.head1Down = head1Down;

    pICVars.head2Down = head2Down;

    pICVars.head3Down = head3Down;
    
    pICVars.encoderHandler.encoder1 = encoder1;
    pICVars.encoderHandler.prevEncoder1 = prevEncoder1;

    pICVars.encoderHandler.encoder2 = encoder2; 
    pICVars.encoderHandler.prevEncoder2 = prevEncoder2;

    pICVars.encoderHandler.encoder1Dir = encoder1Dir;
    pICVars.encoderHandler.encoder2Dir = encoder2Dir;

}//end of Device::getInspectControlVars
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getInspectPacketFromDevice
//
// Returns the last inspect packet received from the device.
//
// If a new packet has been received since the last call, returns true and the
// packet is copied to pPacket.
//
// If no packet has been recevied since the last call, returns false and the
// data in pPacket is invalid.
//

boolean getInspectPacketFromDevice(byte[] pPacket)
{

    if(!newInspectPacket || !canBeControlDevice()){ return false; }

    System.arraycopy(inspectBuffer, 0, pPacket, 0, pPacket.length);

    //no new packets, but new data is available to other objects
    newInspectPacket = false; newInspectData = true;

    return(true);

}// end of Device::getInspectPacketFromDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::zeroEncoderCounts
//
// Sends command to zero the encoder counts.
//

public void zeroEncoderCounts()
{

}//end of Device::zeroEncoderCounts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setTrackPulsesEnabledFlag
//
// Sets the proper flag in rabbitControlFlags and transmits it to the remote.
//

@Override
public void setTrackPulsesEnabledFlag(boolean pState)
{
    
}//end of Device::setTrackPulsesEnabledFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::resetTrackCounters
//
// Sends to the remote the command to fire a Track Counter Reset pulse to
// zero the tracking counters.
//

@Override
public void resetTrackCounters()
{

}//end of Device::resetTrackCounters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setEncodersDeltaTrigger
//
// Tells the Control board how many encoder counts to wait before sending
// an encoder value update.  The trigger value for each encoder is sent.
//
// Normally, this value will be set to something reasonable like .25 to 1.0
// inch of travel of the piece being inspected. Should be no larger than the
// distance represented by a single pixel.
//
// Overridden by children classes for custom handling.
//

public void setEncodersDeltaTrigger()
{

}//end of Device::setEncodersDeltaTrigger
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendRabbitControlFlags
//
// Sends the rabbitControlFlags value to the remotes. These flags control
// the functionality of the remotes.
//
// The paramater pCommand is the command specific to the subclass for its
// Rabbit remote.
//

public void sendRabbitControlFlags()
{

}//end of Device::sendRabbitControlFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::pulseOutput
//
// Pulses the specified output.
//
// Overridden by children classes for custom handling.
//

public void pulseOutput(int pWhichOutput)
{

}//end of Device::pulseOutput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::turnOnOutput
//
// Turns on the specified output.
//
// Overridden by children classes for custom handling.
//

public void turnOnOutput(int pWhichOutput)
{

}//end of Device::turnOnOutput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::turnOffOutput
//
// Turns off the specified output.
//
// Overridden by children classes for custom handling.
//

public void turnOffOutput(int pWhichOutput)
{

}//end of Device::turnOffOutput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::collectData
//
// Collects data from source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//
// Should be overridden by child classes to provide custom handling.
//

public void collectData()
{

    processAllAvailableDataPackets();

}// end of Device::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::catchMapPeak
//
// Catches the passed in clock map data.
//

public void catchMapPeak(int[] pData)
{

}// end of Device::catchMapPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::catchSnapshotPeak
//
// Catches the passed in snapshot data.
//

public void catchSnapshotPeak(int pPeak, int[] pData)
{

}// end of Device::catchSnapshotPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::parsePeakType
//
// Converts the descriptive string loaded from the config file for the map peak
// type (catch highest, lowest value, etc.) into the corresponding constant.
//

void parsePeakType(String pValue)
{

    switch (pValue) {
         case "catch highest": mapPeakType = CATCH_HIGHEST; break;
         case "catch lowest" : mapPeakType = CATCH_LOWEST;  break;
         default : mapPeakType = CATCH_LOWEST;  break;
    }

}// end of Device::parsePeakType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::parseDataType
//
// Converts the descriptive string loaded from the config file for the map data
// type (integer, double, etc.) into the corresponding constant.
//

void parseDataType(String pValue)
{

    switch (pValue) {
         case "integer": mapDataType = INTEGER_TYPE; break;
         case "double" : mapDataType = DOUBLE_TYPE;  break;
         default : mapDataType = INTEGER_TYPE;  break;
    }

}// end of Device::parseDataType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{

    title = configFile.readString(section, "title", "Device " + deviceNum);

    shortTitle = configFile.readString(section, "short title",
                                                        "Device " + deviceNum);

    deviceType = configFile.readString(section, "type", "unknown");
    deviceSubtype = configFile.readString(section, "subtype", "unknown");
    
    photoEye1DistanceToFrontEdge = configFile.readDouble(section,
                                    "photo eye 1 distance to front edge", 0.0);
    
    photoEye2DistanceToFrontEdge = configFile.readDouble(section,
                                    "photo eye 2 distance to front edge", 0.0);

    //only override if previously set simMode not true
    boolean readSimMode = configFile.readBoolean(section, "simulate", false);
    if (!simMode) { simMode = readSimMode; }
    
    encoder1DeltaTrigger =
          configFile.readInt("Hardware", "encoder 1 delta count trigger", 83);

    encoder2DeltaTrigger =
          configFile.readInt("Hardware", "encoder 2 delta count trigger", 83);

}// end of Device::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getDeviceDataAndReset
//
// Gets the device data (peaks, snapshot, and map) and resets the values.
//
// This function makes use of DeviceData so that actions are synchronized.
//
// Overridden by children classes for custom handling.
//

public boolean getDeviceDataAndReset(PeakData pPeakData,
                                    PeakSnapshotData pSnapshotData,
                                    PeakMapData pMapData)
{

    return false;

}// end of Device::getDeviceDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakForChannelAndReset
//
// Retrieves the current value of the peak for channel pChannel and resets
// the peak to the reset value.
//
// This class returns an object as the peak may be of various data types.
//
// Overridden by children classes for custom handling.
//

public void getPeakForChannelAndReset(int pChannel, MKSInteger pPeakValue)
{

    //does nothing because channels may not even be set

}// end of Device::getPeakForChannelAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakDataAndReset
//
// Retrieves the current value of the peak for channel pChannel along with
// all relevant info for the channel such as the chart & trace to which it is
// attached.
//
// Resets the peak to the reset value.
//
// Overridden by children classes for custom handling.
//

public void getPeakDataAndReset(int pChannel, PeakData pPeakData)
{

    //does nothing because channels may not even be set

}// end of Device::getPeakDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakSnapshotDataAndReset
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
// Overridden by children classes for custom handling.
//

public boolean getPeakSnapshotDataAndReset(PeakSnapshotData pPeakSnapData)
{

    return false;

}// end of Device::getPeakSnapshotDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakMapDataAndReset
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

public boolean getPeakMapDataAndReset(PeakMapData pPeakMapData)
{

    return false;

}// end of Device::getPeakMapDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::startInspect
//
// Puts device in the inspect mode.
//
// Overridden by children classes for custom handling.
//

@Override
public void startInspect()
{

}//end of Device::startInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::stopInspect
//
// Takes device out of the inspect mode.
//
// Overridden by children classes for custom handling.
//

public void stopInspect()
{

}//end of Device::stopInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::startMonitor
//
// Puts device in the monitor mode.
//
// Overridden by children classes for custom handling.
//

public void startMonitor()
{

}//end of Device::startMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::stopMonitor
//
// Takes the device out of monitor mode.
//
// Overridden by children classes for custom handling.
//

public void stopMonitor()
{

}//end of Device::stopMonitor
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
// Overridden by children classes for custom handling.
//

boolean requestRunDataPacket()
{

    //waiting for remote response, return false since we bailed
    if (waitingForRemoteResponse) { return false; }

    //return true because we did not bail
    return waitingForRemoteResponse = true;

}//end of Device::requestRunDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleRunDataPacket
//
// Copies the remainder of the packet from the ethernet buffer to the
// runDataBuffer for later retrieval.
//
// Sets newRunData flag true.
//
// Overridden by children classes for custom handling.
//

int handleRunDataPacket()
{

    waitingForRemoteResponse = false;

    int numBytesInPkt = runDataPacketSize; //includes Rabbit checksum byte

    int result;
    result = readBytesAndVerify(runDataBuffer, numBytesInPkt, pktID);
    if (result != numBytesInPkt){ return(result); }

    //check the run data packet counts for errors
    if (runDataBuffer[0] != ((prevRbtRunDataPktCnt+1)&0xff)) {
        ++rbtRunDataPktCntError;
    }
    if (runDataBuffer[1] != ((prevPICRunDataPktCnt+1)&0xff)) {
        ++picRunDataPktCntError;
    }

    //store the run data packet counts
    prevRbtRunDataPktCnt = runDataBuffer[0];
    prevPICRunDataPktCnt = runDataBuffer[1];

    newRunData = true;

    return(result);

}// end of Device::handleRunDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getRunPacketFromDevice
//
// Returns the last run time packet received from the device.
//
// If a new packet has been received since the last call, returns true and the
// packet is copied to pPacket.
//
// If no packet has been recevied since the last call, returns false and the
// data in pPacket is invalid.
//

boolean getRunPacketFromDevice(byte[] pPacket)
{

    if(!newRunData){ return(false); }

    System.arraycopy(runDataBuffer, 0, pPacket, 0, pPacket.length);

    newRunData = false;

    return(true);

}// end of Device::getRunPacketFromDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendSetLocationPacket
//
// Sends a packet to the remote device to set the location of pHdwChannel to
// pValue.
//
// Overridden by children classes for custom handling.
//

void sendSetLocationPacket(int pHdwChannel, int pValue)
{

    numACKsExpected++;

}//end of Device::sendSetLocationPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setLinearLocationsOfChannels
//
// Sets the linear locations of each channel to their proper values.
//
// Overridden by children classes for custom handling.
//

void setLinearLocationsOfChannels()
{

}//end of Device::setLinearLocationsOfChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendSetClockPacket
//
// Sends a packet to the remote device to set the clock position of pHdwChannel
// to pValue.
//
// Overridden by children classes for custom handling.
//

void sendSetClockPacket(int pHdwChannel, int pValue)
{

    numACKsExpected++;

}//end of Device::sendSetClockPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendResetForNextRunCmd
//
// Sends to the remote the command to reset for the next run.
//

public void sendResetForNextRunCmd()
{

}//end of Device::sendResetForNextRunCmd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getByteFromPacket
//
// Extracts a byte from pPacket and returns the value as a signed byte.
//

int getByteFromPacket(byte[] pPacket, int pIndex)
{

    return(pPacket[pIndex]);

}//end of Device::getByteFromPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getUnsignedByteFromPacket
//
// Extracts a byte from pPacket and returns the value as an int. This results
// in the byte being treated as an unsigned value.
//
// In the cast from byte to int, the integer will sign extend to reflect the
// sign of the byte. Since the byte is intended to be unsigned, zeroing the top
// bytes of the integer will leave the original byte value in the least
// significant byte and the integer will be positive.
//

int getUnsignedByteFromPacket(byte[] pPacket, int pIndex)
{

    return((int)(pPacket[pIndex] & 0xff));

}//end of Device::getUnsignedByteFromPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getIntFromPacket
//
// Extracts and combines four bytes from pPacket, MSB first starting at
// location pIndex, and returns the value as an int.
//

int getIntFromPacket(byte[] pPacket, int pIndex)
{

    return(
         ((pPacket[pIndex++]<<24) & 0xff000000)
         + ((pPacket[pIndex++]<<16) & 0xff0000)
          + ((pPacket[pIndex++]<<8) & 0xff00)
            + (pPacket[pIndex++] & 0xff)
    );

}//end of Device::getIntFromPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getUnsignedShortFromPacket
//
// Extracts and combines two bytes representing an unsigned short from pPacket,
// MSB first starting at location pIndex, and returns the value as an int.
//

int getUnsignedShortFromPacket(byte[] pPacket, int pIndex)
{

    return (
            (int)((pPacket[pIndex++]<<8) & 0xff00)
            + (int)(pPacket[pIndex++] & 0xff)
    );

}//end of Device::getUnsignedShortFromPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setIPAddr
//
// Sets the IP address for this board. Also sets the IP address string
// variable.
//

public void setIPAddr(InetAddress pIPAddr)
{

    ipAddr = pIPAddr;

    ipAddrS = pIPAddr.toString();

}//end of Device::setIPAddr
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::run
//
// This method executed when this object is used as a thread object.
//
// Since the sockets and associated streams were created by this
// thread, it cannot be closed without disrupting the connections. If
// other threads try to read from the socket after the thread which
// created the socket finishes, an exception will be thrown.  This
// thread just waits() after performing the connect function.  The
// alternative is to close the socket and allow another thread to
// reopen it, but this results in a lot of overhead.
//


@Override
public void run()
{

    connectToDevice();

    initAfterConnect();

    notifyThreadsWaitingOnConnection();

    waitForever();

}//end of Device::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

void initAfterConnect(){

}//end of Device::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::notifyThreadsWaitingOnConnection
//
// Wakes up all threads which are waiting for the connection to the device to
// be completed.
//

public synchronized void notifyThreadsWaitingOnConnection()
{

    notifyAll();

}//end of Device::notifyThreadsWaitingOnConnection
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::waitForever
//
// Puts the thread in wait mode forever.
//

public synchronized void waitForever()
{

    while (!shutDown){ try{wait();} catch (InterruptedException e) { } }

}//end of Device::waitForever
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::connectToDevice
//
// Opens a TCP/IP connection with the device with which this object is linked
// via the IP address.
//

public synchronized void connectToDevice()
{

    logPanel.appendTS("Connecting to " + title + "...\n");

    try {

        logPanel.appendTS("IP Address: " + ipAddr.toString() + "\n");

        if (!simMode) {
            socket = new Socket(ipAddr, 23);
        }
        else {
            createSimulatedSocket();
        }

        //set amount of time in milliseconds that a read from the socket will
        //wait for data - this prevents program lock up when no data is ready
        socket.setSoTimeout(250);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        byteOut = new DataOutputStream(socket.getOutputStream());
        byteIn =  new DataInputStream(socket.getInputStream());

    }//try
    catch (IOException e) {
        logSevere(e.getMessage() + " - Error: 817");
        logPanel.appendTS("\nError 817: "+e.getMessage()+" " + ipAddrS + "\n");
        connectionAttemptCompleted = true;
        return;
    }

    try {
        //note that readLine hangs if a line is not received -- could change
        //to use ready() and read() for each byte and concatenate to form string
        String s = in.readLine();
        //display the greeting message sent by the remote
        logPanel.appendTS(ipAddrS + " says " + s + "\n");
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 601");
        connectionAttemptCompleted = true;
        return;
    }

    connectionAttemptCompleted = true;

    connectionSuccessful = true;

    logPanel.appendTS("\nConnection successful.");

}//end of Device::connectToDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::createSimulatedSocket
//
// Creates an instance of the Simulated class or subclass to simulate an
// actual device connected to Socket.
//
// Must be overridden by child classes to provide custom handling.
//

void createSimulatedSocket() throws SocketException
{

}//end of Device::createSimulatedSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::waitForConnectCompletion
//
// Waits until the connectionComplete flag is true. The trhead sleeps until
// notified to wake up and check the flag again. This object's connectToDevice
// method executes a notifyAll when it is finished which will awaken any thread
// waiting in this method.
//
// As this method is typically called just after starting the thread (and thus
// entering the run method), the caller may be blocked from even entering this
// method if the thread has already entered the connectToDevice method as both
// are synchronized. Being blocked in that manner is fine as it serves also
// to pause the calling thread until the connection has been completed.
//
// Returns true if the connection attempt was successful, false otherwise.
//

public synchronized boolean waitForConnectCompletion()
{

    while(!connectionAttemptCompleted){
        try {wait(); } catch (InterruptedException e) { }
    }

    return(connectionSuccessful);

}//end of Device::waitForConnectCompletion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::processAllAvailableDataPackets
//
// Processes all packets waiting in the socket. A packet will be processed
// when at least 5 bytes are in the socket, which means that the packet header
// and the packet identifier have been received.
//

public void processAllAvailableDataPackets()
{

    if (byteIn == null) { return; }  //do nothing if the port is closed

    try{
        while (byteIn.available() >= 5) { processOneDataPacket(false, 0); }
    }catch(IOException e){}

}//end of Device::processAllAvailableDataPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::processOneDataPacket
//
// This function processes a single data packet if it is available.  If
// pWaitForPkt is true, the function will wait until data is available.
//
// The amount of time the function is to wait for a packet is specified by
// pTimeOut.  Each count of pTimeOut equals 10 ms.
//
// This function should be called often to allow processing of data packets
// received from the remotes and stored in the socket buffer.
//
// All packets received from the remote devices should begin with
// 0xaa, 0x55, 0xbb, 0x66, followed by the packet identifier, the DSP chip
// identifier, and the DSP core identifier.
//
// Returns number of bytes retrieved from the socket, not including the
// 4 header bytes, the packet ID, the DSP chip ID, and the DSP core ID.
// Thus, if a non-zero value is returned, a packet was processed.  If zero
// is returned, some bytes may have been read but a packet was not successfully
// processed due to missing bytes or header corruption.
// A return value of -1 means that the buffer does not contain a packet.
//

public int processOneDataPacket(boolean pWaitForPkt, int pTimeOut)
{

    if (byteIn == null) {return -1;}  //do nothing if the port is closed

    try{

        //wait a while for a packet if parameter is true
        if (pWaitForPkt){
            int timeOutWFP = 0;
            while(byteIn.available() < 5 && timeOutWFP++ < pTimeOut){
                waitSleep(10);
            }
        }

        //wait until 5 bytes are available - this should be the 4 header bytes,
        //and the packet identifier
        if (byteIn.available() < 5) {return -1;}

        //read the bytes in one at a time so that if an invalid byte is
        //encountered it won't corrupt the next valid sequence in the case
        //where it occurs within 3 bytes of the invalid byte

        //check each byte to see if the first four create a valid header
        //if not, jump to resync which deletes bytes until a valid first header
        //byte is reached

        //if the reSynced flag is true, the buffer has been resynced and an 0xaa
        //byte has already been read from the buffer so it shouldn't be read
        //again

        //after a resync, the function exits without processing any packets

        if (!reSynced){
            //look for the 0xaa byte unless buffer just resynced
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] != (byte)0xaa) {reSync(); return 0;}
        }
        else {reSynced = false;}

        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x55) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0xbb) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x66) {reSync(); return 0;}

        //read in the packet identifier
        byteIn.read(inBuffer, 0, 1);

        //store the ID of the packet (the packet type)
        pktID = inBuffer[0];

        return takeActionBasedOnPacketId(pktID);

    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 865");
    }

    return 0;

}//end of Device::processOneDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::takeActionBasedOnPacketId
//
// Takes different actions based on the packet id.
//

protected int takeActionBasedOnPacketId(int pPktID)
{

    return 0;

}//end of Device::takeActionBasedOnPacketId
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::reSync
//
// Clears bytes from the socket buffer until 0xaa byte reached which signals
// the *possible* start of a new valid packet header or until the buffer is
// empty.
//
// If an 0xaa byte is found, the flag reSynced is set true to that other
// functions will know that an 0xaa byte has already been removed from the
// stream, signalling the possible start of a new packet header.
//
// There is a special case where a 0xaa is found just before the valid 0xaa
// which starts a new packet - the first 0xaa is the last byte of the previous
// packet (usually the checksum).  In this case, the next packet will be lost
// as well.  This should happen rarely.
//

public void reSync()
{
    
    if (byteIn == null) { return; }  //do nothing if the port is closed

    reSynced = false;

    //track the number of times this function is called, even if a resync is not
    //successful - this will track the number of sync errors
    reSyncCount++; packetErrorCnt++;

    //store info pertaining to what preceded the reSync - these values will be
    //overwritten by the next reSync, so they only reflect the last error
    //NOTE: when a reSync occurs, these values are left over from the PREVIOUS
    // good packet, so they indicate what PRECEDED the sync error.

    reSyncPktID = pktID;

    try{
        while (byteIn.available() > 0) {
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] == (byte)0xaa) {reSynced = true; break;}
        }
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 847");
    }

}//end of Device::reSync
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::updateChannelParameters
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

public boolean updateChannelParameters(String pParamType, String pChannelNum,
                                           String pValue, boolean pForceUpdate)
{

    return false;

}//end of Device::updateChannelParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::processChannelParameterChanges
//
// Processes any channel parameters which have been modified since the last
// time this method ran.
//
// All dirty flags are cleared as the changes are processes.
//
// Should be overridden by child classes to provide custom handling.
//

public void processChannelParameterChanges()
{

}//end of Device::processChannelParameterChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::shutDown
//
// This function should be called before exiting the program.  Overriding the
// "finalize" method does not work as it does not get called reliably upon
// program exit.
//

public void shutDown()
{

    //close everything - the order of closing may be important
    try{
        if (byteOut != null) {byteOut.close();}
        if (byteIn != null) {byteIn.close();}
        if (out != null) {out.close();}
        if (in != null) {in.close();}
        if (socket != null) {socket.close();}
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 1739");
    }

    //kill device thread, no longer needed alive
    shutDown = true;

}//end of Device::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//
// Should be overridden by children classes.
//

public void loadCalFile(IniFile pCalFile)
{

}//end of Device::loadCalFile
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
// Should be overridden by children classes.
//

public void saveCalFile(IniFile pCalFile)
{

}//end of Device::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::displayMsg
//
// Displays a message on the msgLabel using a threadsafe method.
//
// There is no bufferering, so if this function is called again before
// invokeLater calls displayMsgThreadSafe, the prior message will be
// overwritten.
//

public void displayMsg(String pMessage)
{
    
    sharedSettings.displayMsg(pMessage);  

}//end of Device::displayMsg
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::waitSleep
//
// Sleeps for pTime milliseconds.
//

public void waitSleep(int pTime)
{

    try {Thread.sleep(pTime);} catch (InterruptedException e) { }

}//end of Device::waitSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of Device::logSevere
//-----------------------------------------------------------------------------

}//end of class Device
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DataTransferIntBuffer;
import model.DataTransferIntMultiDimBuffer;
import model.IniFile;
import toolkit.MKSInteger;
import view.LogPanel;

//-----------------------------------------------------------------------------
// class Device
//

public class Device implements Runnable
{

    final IniFile configFile;
    private final int deviceNum;
    public int getDeviceNum(){ return(deviceNum); }
    String title = "", shortTitle = "";
    String deviceType = "";
    public String getDeviceType(){ return(deviceType); }
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

    private boolean newRunData = false;

    byte runDataBuffer[] = new byte[RUN_DATA_BUFFER_SIZE];

    private int prevRbtRunDataPktCnt = -1;
    private int rbtRunDataPktCntError = 0;
    private int prevPICRunDataPktCnt = -1;
    private int picRunDataPktCntError = 0;

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

    int pktID;
    boolean reSynced;
    int reSyncCount = 0, reSyncPktID;
    int packetErrorCnt = 0;

    int numACKsExpected = 0;
    int numACKsReceived = 0;

    private boolean connectionAttemptCompleted = false;
    public boolean getConnectionAttemptCompleted(){
                                         return (connectionAttemptCompleted); }
    private boolean connectionSuccessful = false;
    public boolean getConnectionSuccessful(){ return (connectionSuccessful); }

    private int snapshotPeakType;
    PeakArrayBufferInt peakSnapshotBuffer;
    SampleMetaData snapshotMeta = new SampleMetaData(0);
    public SampleMetaData getSnapshotMeta(){ return(snapshotMeta); }
    public void setSnapshotDataBuffer(DataTransferIntMultiDimBuffer pV)
        { snapshotMeta.dataSnapshotBuffer = pV; }
    public DataTransferIntMultiDimBuffer getSnapshotDataBuffer()
        { return(snapshotMeta.dataSnapshotBuffer); }

    SampleMetaData mapMeta = new SampleMetaData(0);
    public SampleMetaData getMapMeta(){ return(mapMeta); }

    public void setMapDataBuffer(DataTransferIntMultiDimBuffer pV) {
                                                   mapMeta.dataMapBuffer = pV;}
    public DataTransferIntMultiDimBuffer getMapDataBuffer() {
                                               return(mapMeta.dataMapBuffer); }

    boolean simMode;

    int mapDataType;
    int mapPeakType;

    PeakArrayBufferInt peakMapBuffer;

    LogPanel logPanel;

    final static int RUN_DATA_BUFFER_SIZE = 1024;

    final static int OUT_BUFFER_SIZE = 255;
    final static int IN_BUFFER_SIZE = 255;

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

    static final byte ERROR = 125;
    static final byte DEBUG_CMD = 126;
    static final byte EXIT_CMD = 127;

//-----------------------------------------------------------------------------
// Device::Device (constructor)
//

public Device(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                                                              boolean pSimMode)
{

    deviceNum = pDeviceNum; configFile = pConfigFile; logPanel = pLogPanel;
    simMode = pSimMode;

    mapMeta.deviceNum = deviceNum;
    snapshotMeta.deviceNum = deviceNum;

    outBuffer = new byte[OUT_BUFFER_SIZE];
    inBuffer = new byte[IN_BUFFER_SIZE];

}//end of Device::Device (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

}// end of Device::init
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

    setUpPeakMapBuffer();

    setUpPeakSnapshotBuffer();

    setUpChannels();

    mapMeta.numClockPositions = numClockPositions;

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
// Device::requestAllStatusPacket
//
// Sends a request to the device for a packet with all status information.
// The returned packed will be handled by handleAllStatusPacket(). See that
// method for more details.
//

void requestAllStatusPacket()
{

    sendPacket(GET_ALL_STATUS_CMD, (byte)0);

}//end of Device::requestAllStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleAllStatusPacket
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

int handleAllStatusPacket()
{

    int numBytesInPkt = 111; //includes Rabbit checksum byte

    byte[] buffer = new byte[numBytesInPkt];

    int result;
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

}//end of Device::handleAllStatusPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::requestAllLastADValues
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

}//end of Device::requestAllLastADValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::handleAllLastADValuesPacket
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

    int result;
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

}//end of Device::handleAllLastADValuesPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::handleACKPackets
//
// Handles ACK_CMD packets. Increments the numACKsReceive counter.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//


public int handleACKPackets()
{

    int numBytesInPkt = 1; //does not include checksum byte

    int result = readBytesAndVerify(
                       inBuffer, numBytesInPkt, Device.ACK_CMD);
    if (result != numBytesInPkt){ return(result); }

    numACKsReceived++;

    return(result);

}//end of Simulator::handleACKPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setUpChannels
//
// Creates and sets up the channels.
//

void setUpChannels()
{

    if (numChannels <= 0){ return; }

    channels = new Channel[numChannels];

    for(int i=0; i<numChannels; i++){

        channels[i] = new Channel(deviceNum, i, configFile);
        channels[i].init();

    }

}// end of Device::setUpChannels
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
// Device::parsePeakType
//
// Converts the descriptive string loaded from the config file for the map peak
// type (catch highest, lowest value, etc.) into the corresponding constant.
//

private void parsePeakType(String pValue)
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

private void parseDataType(String pValue)
{

    switch (pValue) {
         case "integer": mapDataType = INTEGER_TYPE; break;
         case "double" : mapDataType = DOUBLE_TYPE;  break;
         default : mapDataType = INTEGER_TYPE;  break;
    }

}// end of Device::parseDataType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::loadClockMappingTranslation
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

    int numMapTransLines = configFile.readInt(pSection,
                 "source clock to grid clock translation number of lines", 0);


    String key = "source clock to grid clock translation line ";

    for(int i=0; i<numMapTransLines; i++){

        String transLine = configFile.readString(pSection, key + (i+1), "");

        if (transLine.isEmpty()){ continue; }

        //split line (0>1, 1>1, etc.) to x>y pairs
        String[] transSplits = transLine.split(",");

        for(String trans : transSplits){

            //split line (x>y) into x and y lines
            String []transSplit = trans.split(">");

            if(transSplit.length < 2 ||
              transSplit[0].isEmpty() || transSplit[1].isEmpty()) { continue; }

            try{
                //convert x and y into "from clock" and "to clock" values
                int fromClk = Integer.parseInt(transSplit[0]);
                int toClk = Integer.parseInt(transSplit[1]);
                if(fromClk < 0 || fromClk >= clockTranslations.length)
                    { continue; }
                //store "to clock" in array at "fromClk" position
                clockTranslations[fromClk] = toClk;
            }
            catch(NumberFormatException e){ }
        }
    }

}// end of Device::loadClockMappingTranslation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{

    String section = "Device " + deviceNum + " Settings";

    title = configFile.readString(section, "title", "Device " + deviceNum);

    shortTitle = configFile.readString(section, "short title",
                                                        "Device " + deviceNum);

    deviceType = configFile.readString(section, "device type", "unknown");

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

}// end of Device::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakForChannelAndReset
//
// Retrieves the current value of the peak for channel pChannel and resets
// the peak to the reset value.
//
// This class returns an object as the peak may be of various data types.
//

public void getPeakForChannelAndReset(int pChannel, MKSInteger pPeakValue)
{

    channels[pChannel].getPeakAndReset(pPeakValue);

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

public void getPeakDataAndReset(int pChannel, PeakData pPeakData)
{

    channels[pChannel].getPeakDataAndReset(pPeakData);

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

public boolean getPeakSnapshotDataAndReset(PeakSnapshotData pPeakSnapData)
{

    if(peakSnapshotBuffer == null) { return(false); }

    pPeakSnapData.meta = snapshotMeta; //channel/buffer/graph etc. info

    boolean peakUpdated
                = peakSnapshotBuffer.getPeakAndReset(pPeakSnapData.peakArray);

    pPeakSnapData.setMetaArray(snapshotMeta.system);

    return(peakUpdated);

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

    if(peakMapBuffer == null) { return(false); }

    pPeakMapData.meta = mapMeta; //channel/buffer/graph etc. info

    boolean peakUpdated = peakMapBuffer.getPeakAndReset(pPeakMapData.peakArray);

    pPeakMapData.setMetaArray(mapMeta.system);

    return(peakUpdated);

}// end of Device::getPeakMapDataAndReset
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

void requestRunDataPacket()
{

    if (waitingForRemoteResponse) { return; }

    waitingForRemoteResponse = true;

    sendPacket(GET_RUN_DATA_CMD, (byte)0);

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

int handleRunDataPacket()
{

    waitingForRemoteResponse = false;

    int numBytesInPkt = 212; //includes Rabbit checksum byte

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

void sendSetLocationPacket(int pHdwChannel, int pValue)
{

    sendPacket(SET_LOCATION_CMD, (byte)pHdwChannel, (byte)pValue);

    numACKsExpected++;

}//end of Device::sendSetLocationPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setLinearLocationsOfChannels
//
// Sets the linear locations of each channel to their proper values.
//

void setLinearLocationsOfChannels()
{

    for(Channel channel : channels){

        if (channel.getBoardChannel() == -1) { continue; }

        sendSetLocationPacket(channel.getBoardChannel(),
                                        channel.getLinearLocation());
    }

}//end of Device::setLinearLocationsOfChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::sendSetClockPacket
//
// Sends a packet to the remote device to set the clock position of pHdwChannel
// to pValue.
//

void sendSetClockPacket(int pHdwChannel, int pValue)
{

    sendPacket(SET_CLOCK_CMD, (byte)pHdwChannel, (byte)pValue);

    numACKsExpected++;

}//end of Device::sendSetClockPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setClockPositionsOfChannels
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

}//end of Device::setClockPositionsOfChannels
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
// Channel::setUpPeakSnapshotBuffer
//
// Creates and sets up the appropriate PeakBuffer subclass to capture the type
// of peak specified in the config file, i.e. highest value, lowest value, etc.
//

public void setUpPeakSnapshotBuffer()
{

    switch (snapshotPeakType){

        case CATCH_HIGHEST:
            peakSnapshotBuffer = new HighPeakArrayBufferInt(0, 128); //WIP HSS// size needs to be ini
            peakSnapshotBuffer.setResetValue(Integer.MIN_VALUE);
            break;

        case CATCH_LOWEST:
            peakSnapshotBuffer = new LowPeakArrayBufferInt(0, 128); //WIP HSS// size needs to be ini
            peakSnapshotBuffer.setResetValue(Integer.MAX_VALUE);
            break;

        default:
            peakSnapshotBuffer = new HighPeakArrayBufferInt(0, 128); //WIP HSS// size needs to be ini
            peakSnapshotBuffer.setResetValue(Integer.MIN_VALUE);
            break;

    }

    peakSnapshotBuffer.reset();

}// end of Channel::setUpPeakSnapshotBuffer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setUpPeakMapBuffer
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

}// end of Device::setUpPeakMapBuffer
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

    while (true){ try{wait();} catch (InterruptedException e) { } }

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

    logPanel.appendTS("Connecting...\n");

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

        if (pktID == GET_ALL_STATUS_CMD) { return handleAllStatusPacket(); }
        else
        if (pktID == ACK_CMD){ return handleACKPackets(); }
        else
        if (pktID == GET_ALL_LAST_AD_VALUES_CMD){
            return handleAllLastADValuesPacket();
        }
        else
        if (pktID == GET_RUN_DATA_CMD){
            return handleRunDataPacket();
        }

    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 865");
    }

    return 0;

}//end of Device::processOneDataPacket
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
// NOTE: This method and processChannelParameterChanges() should only be called
// by synchronized methods so that values cannot be updated by one thread while
// another is processing all the changes. The device object's dirty flag is
// cleared after all changes handled, so no changes can be allowed during that
// process.
//
// Returns true if the value was updated, false otherwise.
//

public boolean updateChannelParameters(String pParamType, String pChannelNum,
                                           String pValue, boolean pForceUpdate)
{

    int chNum = Integer.parseInt(pChannelNum);

    boolean result = false;

        switch (pParamType) {
            case "Gain Spinner":
                result = channels[chNum].setGain(pValue, pForceUpdate);
                break;
            case "Offset Spinner":
                result = channels[chNum].setOffset(pValue, pForceUpdate);
                break;
            case "On-Off Checkbox":
                result = channels[chNum].setOnOff(pValue, pForceUpdate);
                break;
        }

    if(result) { setHdwParamsDirty(true); }

    return(result);

}//end of Device::updateChannelParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::processChannelParameterChanges
//
// Processes any channel parameters which have been modified since the last
// time this method ran.
//
// All dirty flags are cleared as the changes are processes.
//
// NOTE: This method and processChannelParameterChanges() should only be called
// by synchronized methods so that values cannot be updated by one thread while
// another is processing all the changes. The device object's dirty flag is
// cleared after all changes handled, so no changes can be allowed during that
// process.
//
// Should be overridden by child classes to provide custom handling.
//

synchronized public void processChannelParameterChanges()
{

}//end of MainHandler::processChannelParameterChanges
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

}//end of MainHandler::logSevere
//-----------------------------------------------------------------------------

}//end of class Device
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

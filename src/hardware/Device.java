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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    int numClockPositions;
    int[] clockTranslations;
    int numGridsHeightPerSourceClock, numGridsLengthPerSourceClock;

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

    int pktID;
    boolean reSynced;
    int reSyncCount = 0, reSyncPktID;    
    int packetErrorCnt = 0;
    
    private boolean connectionAttemptCompleted = false;
    private boolean connectionSuccessful = false;
    
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
    
    final static int OUT_BUFFER_SIZE = 255;
    final static int IN_BUFFER_SIZE = 255;

    //Commands for all Devices
    //These should match the values in the code for the hardware.
    
    //NOTE: Each subclass can have its own command codes. They should be in the
    //range 40~100 so that they don't overlap the codes in this parent class.

    static byte NO_ACTION = 0;
    static byte GET_ALL_STATUS_CMD = 1;
    static byte LOAD_FIRMWARE_CMD = 2;
    static byte DATA_CMD = 3;
    static byte SEND_DATA_CMD = 4;

    
    static byte ERROR = 125;
    static byte DEBUG_CMD = 126;
    static byte EXIT_CMD = 127;
    
    
    
//-----------------------------------------------------------------------------
// Device::Device (constructor)
//

public Device(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                                                              boolean pSimMode)
{

    deviceNum = pDeviceNum; configFile = pConfigFile; logPanel = pLogPanel;
    simMode = pSimMode;

    mapMeta.deviceNum = deviceNum;
    
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
// Note: pNumBytes should include the data bytes ONLY and NOT the checksum byte.
// This method will automatically read the checksum byte.
//
// The packet ID should be provided via pPktID -- it is only used to verify the
// checksum as it is included in that calculation by the sender.
//
// Returns the number of bytes read, including the checksum byte.
// On checksum error, returns -1.
// If pNumBytes and checksum are not available after waiting, returns -2.
//

int readBytesAndVerify(byte[] pBuffer, int pNumBytes, int pPktID)
{

    byte checksum = 0;
    
    try{

        int totalNumBytes = pNumBytes + 1; //account for checksum
        int timeOutProcess = 0;
        
        while(timeOutProcess++ < 2){            
            if (byteIn.available() >= totalNumBytes) {break;}
            waitSleep(10);            
        }

        if (byteIn.available() >= totalNumBytes) {
            byteIn.read(pBuffer, 0, pNumBytes);
            checksum = (byte)byteIn.read();
        }else{
            return(-2);
        }

    }// try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 245");
    }
    
    byte sum = (byte)pPktID; //packet ID is included in the checksum

    //validate checksum by summing the packet id and all data
    
    for(int i = 0; i < pNumBytes; i++){ sum += pBuffer[i]; }

    if ( ((sum + checksum) & 0xff) == 0) { return(pNumBytes); }
    else{ return(-1); }

}//end of Device::readBytesAndVerify
//----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::requestAllStatusPacket
//
// Sends a request to the device for a packet will all status information.
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
// Returns the number of bytes this method extracted from the socket.
//

int handleAllStatusPacket()
{
    
    int numBytesInPkt = 12;
    
    byte[] buffer = new byte[numBytesInPkt];
    
    int status = readBytesAndVerify(buffer, numBytesInPkt, pktID);
    
    int i = 0;
    
    logPanel.appendTS("\n----------------------------------------------\n");
    logPanel.appendTS("-- All Status Information --\n\n");
    
    logPanel.appendTS(" - Transmission Errors -\n");
    logPanel.appendTS("Rabbit to Host: " + packetErrorCnt + "\n");
    logPanel.appendTS("Host to Rabbit: " + buffer[i++] + "\n");
    logPanel.appendTS("Master PIC to Rabbit: " + buffer[i++] + "\n");
    logPanel.appendTS("Rabbit to Master PIC: " + buffer[i++] + "\n");
    logPanel.appendTS("Slave PICs to Master PIC: " + buffer[i++] + "\n");

    int numSlaves = 8;
    
    for(int j=0; j<numSlaves; j++){
        logPanel.appendTS(
                     "Master PIC to Slave " + j + ": " + buffer[i++] + "\n");
    }
    
    int sum = packetErrorCnt; //number of errors recorded by host
    //add with errors reported by device
    for(int k=0; k<buffer.length; k++){ sum+= buffer[k]; }

    logPanel.appendTS("Total error count: " + sum + "\n");
    
    return(status);    
    
}//end of Device::handleAllStatusPacket
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
            catch(NumberFormatException e){ continue; }
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

public boolean getPeakDataAndReset(PeakMapData pPeakMapData)
{
    
    if(peakMapBuffer == null) { return(false); }
    
    pPeakMapData.meta = mapMeta; //channel/buffer/graph etc. info
        
    boolean peakUpdated = peakMapBuffer.getPeakAndReset(pPeakMapData.peakArray);
        
    pPeakMapData.setMetaArray(mapMeta.system);
    
    return(peakUpdated);
    
}// end of Device::getPeakMapDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getRunPacketFromDevice
//
// Retrieves a run-time data packet from the remote device.
//

void getRunPacketFromDevice(byte[] pPacket)
{
    
}// end of Device::getRunPacketFromDevice
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

    //debug mks
    requestAllStatusPacket();
    //debug mks end
    
    notifyThreadsWaitingOnConnection();
    
    waitForever();
    
}//end of Device::run
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
// ControlBoard::connectToDevice
//
// Opens a TCP/IP connection with the device with which this object is linked
// via the IP address.
//

public synchronized void connectToDevice()
{

    logPanel.appendTS("Connecting...\n");
    
    try {

        logPanel.appendTS("IP Address: " + ipAddr.toString() + "\n");

        boolean simulate = false; //debug mks -- make this a class member and set this from config file!
        
        if (!simulate) {
            socket = new Socket(ipAddr, 23);
        }
        else {
            /* //debug mks
            ControlSimulator controlSimulator = new ControlSimulator(
                        ipAddr, 23, fileFormat, simulationDataSourceFilePath);
            controlSimulator.init();
            
            socket = controlSimulator;
            
            //when simulating, the socket is a ControlSimulator class object
            //which is also a MessageLink implementor, so cast it for use as
            //such so that messages can be sent to the object
            mechSimulator = (MessageLink)socket;
                    */
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
        logSevere(e.getMessage() + " - Error: 591");
        logPanel.appendTS("\nError: couldn't get I/O for " + ipAddrS + "\n");
        connectionAttemptCompleted = true;
        return;
    }

    try {
        //display the greeting message sent by the remote
        logPanel.appendTS(ipAddrS + " says " + in.readLine() + "\n");
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
//        else
//        if (pktID == GET_CHASSIS_SLOT_ADDRESS_CMD){return readBytes(2);}

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

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
import java.net.InetAddress;
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

    private InetAddress ipAddr = null;
    public InetAddress getIPAddr(){ return(ipAddr); }
    private String ipAddrS;

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
    
//-----------------------------------------------------------------------------
// Device::Device (constructor)
//

public Device(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                                                              boolean pSimMode)
{

    deviceNum = pDeviceNum; configFile = pConfigFile; logPanel = pLogPanel;
    simMode = pSimMode;

    mapMeta.deviceNum = deviceNum;
    
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

@Override
public void run()
{

    connectToDevice();
    
}//end of Device::run
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
        
    connectionAttemptCompleted = true;
    
    connectionSuccessful = true;
    
    notifyAll(); //wake up all threads that are waiting for this to complete    
    
}//end of Device::connectToDevice
//-----------------------------------------------------------------------------    

//-----------------------------------------------------------------------------
// Board::waitForConnectCompletion
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
    
}//end of Board::waitForConnectCompletion
//-----------------------------------------------------------------------------

}//end of class Device
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

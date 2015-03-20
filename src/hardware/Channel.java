/******************************************************************************
* Title: Channel.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class is the parent class for subclasses which process data from a
* hardware channel.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.DataTransferIntBuffer;
import model.IniFile;
import toolkit.MKSInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Channel
//

public class Channel
{

    IniFile configFile;

    SampleMetaData meta = new SampleMetaData(0);
    
    public void setDataBuffer(DataTransferIntBuffer pV) {meta.dataBuffer = pV;}
    public DataTransferIntBuffer getDataBuffer() { return(meta.dataBuffer); }
    
    String title, shortTitle;
    
    public int getChartGroup(){ return(meta.chartGroup); }
    public int getChart(){ return(meta.chart); }
    public int getGraph(){ return(meta.graph); }
    public int getTrace(){ return(meta.trace); }

    private int dataType;
    public int getDataType(){ return (dataType);}
    
    private int bufferLoc;    
    public int getBufferLoc(){ return (bufferLoc);}
    
    int peakType;
    PeakBufferInt peakBuffer;
    
    MKSInteger data = new MKSInteger(0);
    
    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;
    
    public static final int INTEGER_TYPE = 0;
    public static final int DOUBLE_TYPE = 1;
    
//-----------------------------------------------------------------------------
// Channel::Channel (constructor)
//

public Channel(int pDeviceNum, int pChannelNum, IniFile pConfigFile)
{

    meta.deviceNum = pDeviceNum; meta.channelNum = pChannelNum;
    configFile = pConfigFile;
    
}//end of Channel::Channel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    meta.channel = this;
    
    loadConfigSettings();

    setUpPeakBuffer();
    
}// end of Channel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::setUpPeakBuffer
//
// Creates and sets up the appropriate PeakBuffer subclass to capture the type
// of peak specified in the config file, i.e. highest value, lowest value, etc.
//

public void setUpPeakBuffer()
{

    switch (peakType){
        
        case CATCH_HIGHEST: 
            peakBuffer = new HighPeakBufferInt(0);
            peakBuffer.setResetValue(Integer.MIN_VALUE);
            break;
        
        case CATCH_LOWEST: 
            peakBuffer = new LowPeakBufferInt(0);
            peakBuffer.setResetValue(Integer.MAX_VALUE);
            break;
        
        default: 
            peakBuffer = new HighPeakBufferInt(0);
            peakBuffer.setResetValue(Integer.MIN_VALUE);
            break;
            
    }

    peakBuffer.reset();
    
}// end of Channel::setUpPeakBuffer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    int deviceNum = meta.deviceNum, channelNum = meta.channelNum;
    
    String section = "Device " + deviceNum + " Channel " + channelNum;

    title = configFile.readString(
       section, "title", "Device " + meta.deviceNum + " Channel " + channelNum);

    shortTitle = configFile.readString(
                section, "short title", "Dev" + deviceNum + "Ch" + channelNum);

    meta.chartGroup = configFile.readInt(section, "chart group", -1);
    
    meta.chart = configFile.readInt(section, "chart", -1);
    
    meta.graph = configFile.readInt(section, "graph", -1);
    
    meta.trace = configFile.readInt(section, "trace", -1);    
    
    String s;
            
    s = configFile.readString(section, "peak type", "catch highest");
    parsePeakType(s);
    
    s = configFile.readString(section, "data type", "integer");
    parseDataType(s);

    bufferLoc = configFile.readInt(section, "buffer location", -1);
    
}// end of Trace::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::parsePeakType
//
// Converts the descriptive string loaded from the config file for the peak
// type (catch highest, lowest value, etc.) into the corresponding constant.
//

private void parsePeakType(String pValue)
{

    switch (pValue) {
         case "catch highest": peakType = CATCH_HIGHEST; break;
         case "catch lowest" : peakType = CATCH_LOWEST;  break;
         default : peakType = CATCH_LOWEST;  break;
    }
    
}// end of Channel::parsePeakType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::parseDataType
//
// Converts the descriptive string loaded from the config file for the data
// type (integer, double, etc.) into the corresponding constant.
//

private void parseDataType(String pValue)
{

    switch (pValue) {
         case "integer": dataType = INTEGER_TYPE; break;
         case "double" : dataType = DOUBLE_TYPE;  break;
         default : dataType = INTEGER_TYPE;  break;
    }
    
}// end of Channel::parseDataType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::catchPeak
//
// Stores value incapsulated in pPeakValue as a new peak if it is
// greater/lesser (depending on peak type) than the current peak. 
//

public void catchPeak(int pPeakValue)
{
    
    peakBuffer.catchPeak(pPeakValue);
    
}// end of Channel::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//
// This method returns an object as the peak may be of various data types.
//

public void getPeakAndReset(MKSInteger pPeakValue)
{
    
    peakBuffer.getPeakAndReset(pPeakValue);
    
}// end of Channel::getPeakAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::getPeakDataAndReset
//
// Retrieves the current value of the peak along with all relevant info for the
// channel such as the chart & trace to which it is attached.
//
// Resets the peak to the reset value.
//
// Returns true if the peak has been updated since the last call to this method
// or false otherwise.
//

public boolean getPeakDataAndReset(PeakData pPeakData)
{

    pPeakData.meta = meta; //channel/buffer/trace etc. info
        
    boolean peakUpdated = peakBuffer.getPeakAndReset(data);    
    
    pPeakData.peak = data.x;
    
    return(peakUpdated);
    
}// end of Channel::getPeakDataAndReset
//-----------------------------------------------------------------------------

}//end of class Channel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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

import model.IniFile;
import toolkit.MKSInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Channel
//

public class Channel
{

    IniFile configFile;
    
    private final int deviceIndex, index;
    String title, shortTitle;

    int chartGroup, chart, graph, trace;
    
    public int getChartGroup(){ return(chartGroup); }
    public int getChart(){ return(chart); }
    public int getGraph(){ return(graph); }
    public int getTrace(){ return(trace); }

    private int dataType;
    public int getDataType(){ return (dataType);}
    
    private int bufferLoc;    
    public int getBufferLoc(){ return (bufferLoc);}
    
    int peakType;
    PeakBuffer peakBuffer;
    
    MKSInteger data = new MKSInteger(0);
    
    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;
    
    public static final int INTEGER_TYPE = 0;
    public static final int DOUBLE_TYPE = 1;
    
//-----------------------------------------------------------------------------
// Channel::Channel (constructor)
//

public Channel(int pDeviceIndex, int pIndex, IniFile pConfigFile)
{

    deviceIndex = pDeviceIndex; index = pIndex; configFile = pConfigFile;
    
}//end of Channel::Channel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

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
            peakBuffer = new HighPeakBufferInteger(0);
            peakBuffer.setResetValue(new MKSInteger(Integer.MIN_VALUE));
            break;
        
        case CATCH_LOWEST: 
            peakBuffer = new LowPeakBufferInteger(0);
            peakBuffer.setResetValue(new MKSInteger(Integer.MAX_VALUE));
            break;
        
        default: 
            peakBuffer = new HighPeakBufferInteger(0);
            peakBuffer.setResetValue(new MKSInteger(Integer.MIN_VALUE));
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

    String section = "Device " + deviceIndex + " Channel " + index;

    title = configFile.readString(
               section, "title", "Device " + deviceIndex + " Channel " + index);

    shortTitle = configFile.readString(
                   section, "short title", "Dev" + deviceIndex + "Ch" + index);

    chartGroup = configFile.readInt(section, "chart group", -1);
    
    chart = configFile.readInt(section, "chart", -1);
    
    graph = configFile.readInt(section, "graph", -1);
    
    trace = configFile.readInt(section, "trace", -1);    
    
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

public void parsePeakType(String pValue)
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

public void parseDataType(String pValue)
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

public void catchPeak(Object pPeakValue)
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

public void getPeakAndReset(Object pPeakValue)
{
    
    peakBuffer.getPeakAndReset(pPeakValue);
    
}// end of Channel::getPeakAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::getPeakData
//
// Retrieves the current value of the peak along with all relevant info for the
// channel such as the chart & trace to which it is attached.
//
// Resets the peak to the reset value.
//

public void getPeakData(PeakData pPeakData)
{
    
    peakBuffer.getPeakAndReset(data);    
    pPeakData.peak = data.x;
    pPeakData.chartGroup = chartGroup;
    pPeakData.chart = chart;
    pPeakData.graph = graph;
    pPeakData.trace = trace;
    
}// end of Channel::getPeakData
//-----------------------------------------------------------------------------

}//end of class Channel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

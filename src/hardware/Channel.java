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
    
    int peakType;
    PeakBuffer peakBuffer;
    
    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;
    
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
            peakBuffer = new HighPeakBuffer<>(0);
            peakBuffer.setResetValue(Integer.MIN_VALUE);
            break;
        
        case CATCH_LOWEST: 
            peakBuffer = new LowPeakBuffer<>(0);
            peakBuffer.setResetValue(Integer.MAX_VALUE);
            break;
        
        default: 
            peakBuffer = new HighPeakBuffer<>(0);
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

    String section = "Device " + deviceIndex + " Channel " + index;

    title = configFile.readString(
               section, "title", "Device " + deviceIndex + " Channel " + index);

    shortTitle = configFile.readString(
                   section, "short title", "Dev" + deviceIndex + "Ch" + index);

    chartGroup = configFile.readInt(section, "chart group", -1);
    
    chart = configFile.readInt(section, "chart", -1);
    
    graph = configFile.readInt(section, "graph", -1);
    
    trace = configFile.readInt(section, "trace", -1);    
    
    String peakTypeText = configFile.readString(
                                        section, "peak type", "catch highest");
    parsePeakType(peakTypeText);
    
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
// Channel::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//
// This method returns an object as the peak may be of various data types.
//

public Object getPeakAndReset()
{
    
    return (peakBuffer.getPeakAndReset() );
    
}// end of Channel::getPeakAndReset
//-----------------------------------------------------------------------------

}//end of class Channel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

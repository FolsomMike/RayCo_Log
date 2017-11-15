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

import java.util.ArrayList;
import model.DataTransferIntBuffer;
import model.IniFile;
import model.SharedSettings;
import model.ThresholdInfo;
import toolkit.MKSBoolean;
import toolkit.MKSInteger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Channel
//

public class Channel
{

    private final SharedSettings sharedSettings;
    IniFile configFile;

    SampleMetaData meta = new SampleMetaData(0);
    public SampleMetaData getMetaData() { return meta; }

    public void setDataBuffer(DataTransferIntBuffer pV) {meta.dataBuffer = pV;}
    public DataTransferIntBuffer getDataBuffer() { return(meta.dataBuffer); }

    String title, shortTitle;

    private String calPanelGroup;
    private String calPanelName;

    private int boardChannel;
    public int getBoardChannel(){ return(boardChannel); }

    private int clockPosition;
    public int getClockPosition(){ return(clockPosition); }

    private int linearLocation;
    public int getLinearLocation(){ return(linearLocation); }

    public String getCalPanelGroup(){ return calPanelGroup; }
    public String getCalPanelName(){ return calPanelName; }

    public int getChartGroup(){ return(meta.chartGroup); }
    public int getChart(){ return(meta.chart); }
    public int getGraph(){ return(meta.graph); }
    public int getTrace(){ return(meta.trace); }

    private int dataType;
    public int getDataType(){ return (dataType);}

    private int bufferLoc;
    public int getBufferLoc(){ return (bufferLoc);}

    private BoardChannelParameters chanHdwParams;
    public BoardChannelParameters getHdwParams() { return chanHdwParams; }
    public void setHdwParams(BoardChannelParameters pH) { chanHdwParams = pH; }
    
    private double delayDistance = 1;
    public double getDelayDistance() { return delayDistance; }
    public void setDelayDistance(double pD) { delayDistance = pD; }
    
    private double startFwdDelayDistance;
    public double getStartFwdDelayDistance() { return startFwdDelayDistance; }
    public void setStartFwdDelayDistance(double pD) { startFwdDelayDistance = pD; }
    
    private double startRevDelayDistance;
    public double getStartRevDelayDistance() { return startRevDelayDistance; }
    public void setStartRevDelayDistance(double pD) { startRevDelayDistance = pD; }
    
    private double distanceSensorToFrontEdgeOfHead;
    public double getDistanceSensorToFrontEdgeOfHead() { return distanceSensorToFrontEdgeOfHead; }

    int peakType;
    PeakBufferInt peakBuffer;

    MKSInteger data = new MKSInteger(0);
    MKSInteger thresholdViolation = new MKSInteger(-1);
    private boolean flaggingEnabled = false;
    public void enableFlagging(boolean pEn) { flaggingEnabled = pEn; }

    ArrayList<ThresholdInfo> thresholdInfos = new ArrayList<>(10);

    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;

    public static final int INTEGER_TYPE = 0;
    public static final int DOUBLE_TYPE = 1;

//-----------------------------------------------------------------------------
// Channel::Channel (constructor)
//

public Channel(int pDeviceNum, int pChannelNum, IniFile pConfigFile,
                SharedSettings pSettings)
{

    meta.deviceNum = pDeviceNum; meta.channelNum = pChannelNum;
    configFile = pConfigFile; sharedSettings = pSettings;

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

    //get the thresholds that are on the same graph as this channel
    thresholdInfos = sharedSettings.getThresholdInfosForGraph(
                                    meta.chartGroup, meta.chart, meta.graph);

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
            peakBuffer.setResetValue(Integer.MIN_VALUE, -1);
            break;

        case CATCH_LOWEST:
            peakBuffer = new LowPeakBufferInt(0);
            peakBuffer.setResetValue(Integer.MAX_VALUE, -1);
            break;

        default:
            peakBuffer = new HighPeakBufferInt(0);
            peakBuffer.setResetValue(Integer.MIN_VALUE, -1);
            break;

    }

    peakBuffer.reset();

}// end of Channel::setUpPeakBuffer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::loadConfigSettings
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

    boardChannel = configFile.readInt(section, "board channel", -1);

    clockPosition = configFile.readInt(section, "clock position", -1);

    linearLocation = configFile.readInt(section, "linear location", -1);

    calPanelGroup = configFile.readString(
                        section, "calibration panel group", "Dev" + deviceNum);

    calPanelName = configFile.readString(
     section, "calibration panel name", "Dev" + deviceNum + "Ch" + channelNum);

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

}// end of Channel::loadConfigSettings
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

    peakBuffer.catchPeak(pPeakValue, checkThresholdViolation(pPeakValue));

}// end of Channel::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::checkThresholdViolation
//
// Returns -1 if no thresholds were violated. If a threshold was violated, that
// threshold's number is returned. Whether this is above or below the threshold
// is determined by flagOnOver.
//
// The threshold with the lowest (0) thresholdIndex is the highest severity
// threshold, highest index is lowest.  This function should be called for the
// thresholds in order of their index which happens automatically if they are
// stored in an array in this order.  If called in this order, no more
// thresholds should be checked after one returns true because lower severity
// thresholds should not override higher ones.
//

private int checkThresholdViolation(int pSig)

{
    
    if (!flaggingEnabled) { return -1; } //bail if not flagging

    for (ThresholdInfo info : thresholdInfos) {

        int lvl = info.getLevel();

        //true check for signal above, if false check for signal below
        if (info.getFlagOnOver()){
            if (pSig >= lvl) { return info.getThresholdNum(); }
        }
        else{ if (pSig <= lvl) { return info.getThresholdNum(); } }

    }

    return -1; //-1 because no threshold violated

}//end of Channel::checkThresholdViolation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//
// This method returns an object as the peak may be of various data types.
//

public void getPeakAndReset(MKSInteger pPeakValue, MKSInteger pThresViolation)
{

    peakBuffer.getPeakAndReset(pPeakValue, pThresViolation);

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

    pPeakData.metaArray[meta.channelNum] = meta; //channel/buffer/trace etc. info

    boolean peakUpdated = peakBuffer.getPeakAndReset(data, thresholdViolation);

    pPeakData.peakArray[meta.channelNum] = data.x;
    pPeakData.thresholdViolationArray[meta.channelNum] = thresholdViolation.x;

    return(peakUpdated);

}// end of Channel::getPeakDataAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//
//

public void loadCalFile(IniFile pCalFile)
{

    String section = "Device " + meta.deviceNum + " Channel " + meta.channelNum;

    chanHdwParams.setOnOff(pCalFile.readString(section, "onOff", "off"), true);
    chanHdwParams.setGain(pCalFile.readString(section, "gain", "5"), true);
    chanHdwParams.setOffset(pCalFile.readString(section, "offset", "127"), true);

}//end of Channel::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

public void saveCalFile(IniFile pCalFile)
{

    String section = "Device " + meta.deviceNum + " Channel " + meta.channelNum;

    pCalFile.writeBoolean(section, "onOff", chanHdwParams.getOnOff(true));
    pCalFile.writeInt(section, "gain", chanHdwParams.getGain(true));
    pCalFile.writeInt(section, "offset", chanHdwParams.getOffset(true));

}//end of Channel::saveCalFile
//-----------------------------------------------------------------------------

}//end of class Channel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: Trace.java
* Author: Mike Schoonover
* Date: 11/13/13
*
* Purpose:
*
* This class draws a trace
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import model.DataFlags;
import model.DataSetInt;
import model.DataTransferIntBuffer;
import model.IniFile;
import model.SharedSettings;
import model.ThresholdInfo;
import toolkit.Tools;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Trace
//

public class Trace{

    private IniFile configFile;
    private String section;

    private GraphInfo graphInfo;
    
    private SharedSettings sharedSettings;

    private Threshold[] thresholds;

    DataSetInt dataSet = new DataSetInt();

    DataTransferIntBuffer dataBuffer;
    public void setDataBuffer(DataTransferIntBuffer pV) { dataBuffer = pV; }
    public DataTransferIntBuffer getDataBuffer() { return(dataBuffer); }

    ArrayList<Integer> data = new ArrayList<>(10000);
    ArrayList<Integer> dataFlags = new ArrayList<>(10000);

    private int lastSegmentStartIndex = -1;
    private int lastSegmentEndIndex = -1;

    private String title, shortTitle, objectType;
    public Color traceColor;
    public String colorKeyText;
    public int colorKeyXPos;
    public int colorKeyYPos;
    public int chartGroupNum, chartNum, graphNum, traceNum;
    private int width, height, minWidth, minHeight;
    Color backgroundColor;
    Color gridColor;
    int gridTrack;
    private int dataIndex = 0;
    public int getDataIndex(){ return dataIndex; }
    private int prevX = -1, prevY = Integer.MAX_VALUE;
    public int getPrevX(){ return prevX; }
    private int xMax, yMax;
    private int numDataPoints;
    private double xScale = 1.0;
    private double yScale = 1.0;
    private int offset = 0;
    private int baseLine = 0;
    private final Color circleColor = DEFAULT_CIRCLE_COLOR;
    private boolean visible = true;
    private boolean connectPoints = true;
    private boolean invertTrace;
    private boolean leadDataPlotter;
    private int peakType;
    boolean drawGridBaseline;
    int gridXSpacing = 10;
    int gridYSpacing;
    int gridY1;

    int flagThreshold;
    private boolean flaggingEnabled = false;
    public void enableFlagging(boolean pEn) { flaggingEnabled = pEn; }

    private int lastRequestedPeak = -1;
    private int lastRequestedPeakX = -1;
    public int getLastRequestedPeakX() { return lastRequestedPeakX; }
    private int lastRequestedPeakY = -1;
    public int getLastRequestedPeakY() { return lastRequestedPeakY; }

    //simple getters & setters

    public int getWidth(){ return(width); }
    public int getPeakType(){ return(peakType); }
    public int getNumDataPoints() { return(numDataPoints); }

    //constants

    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;

    private static final Color VERTICAL_BAR_COLOR = Color.DARK_GRAY;
    private static final Color DEFAULT_CIRCLE_COLOR = Color.BLACK;

    public static final boolean CONNECT_POINTS = true;
    public static final boolean DO_NOT_CONNECT_POINTS = false;

//-----------------------------------------------------------------------------
// Trace::Trace (constructor)
//
//

public Trace()
{
    
    flaggingEnabled = true;//DEBUG HSS// remove later

}//end of Trace::Trace (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init(int pChartGroupNum, int pChartNum, int pGraphNum,
              int pTraceNum, int pWidth, int pHeight, Color pBackgroundColor,
              boolean pDrawGridBaseline, Color pGridColor, int pGridXSpacing,
              int pGridYSpacing, GraphInfo pGraphInfo, IniFile pConfigFile,
              Threshold[] pThresholds, SharedSettings pSettings)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    graphNum = pGraphNum; traceNum = pTraceNum;
    width = pWidth; height = pHeight;
    backgroundColor = pBackgroundColor;
    drawGridBaseline = pDrawGridBaseline; gridColor = pGridColor;
    gridXSpacing = pGridXSpacing; gridYSpacing = pGridYSpacing;
    graphInfo = pGraphInfo; configFile = pConfigFile;
    thresholds = pThresholds;
    sharedSettings = pSettings;
    
    gridY1 = gridYSpacing-1; //do math once for repeated use

    section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                + " Graph " + graphNum + " Trace " + traceNum;

    loadConfigSettings();

    xMax = width - 1; yMax = height - 1;

}// end of Trace::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//

public void updateDimensions(int pNewWidth, int pNewHeight)
{
    
    //make sure we stay within constraints
    if (pNewWidth<minWidth) { pNewWidth = minWidth; }
    if (pNewHeight<minHeight) { pNewHeight = minHeight; }

    width = pNewWidth; height = pNewHeight;

    xMax = width - 1; yMax = height - 1;

}// end of Trace::updateDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    title = configFile.readString(
                        section, "title", "Trace " + (traceNum + 1));

    shortTitle = configFile.readString(
                            section, "short title", "trace" + (traceNum + 1));

    objectType = configFile.readString(section, "object type", "trace");

    traceColor = configFile.readColor(section, "color", Color.BLACK);

    colorKeyText = configFile.readString(section, "color key text", "hidden");
    colorKeyXPos = configFile.readInt(section, "color key x position", 0);
    colorKeyYPos = configFile.readInt(section, "color key y position", 0);

    int configWidth = configFile.readInt(section, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0

    int configHeight = configFile.readInt(section, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0
    
    //store current width and height as the mins
    minWidth = width; minHeight = height;

    connectPoints = configFile.readBoolean(
                            section, "connect data points with line", false);

    invertTrace = configFile.readBoolean(section, "invert trace", true);

    numDataPoints = configFile.readInt(section, "number of data points", width);

    offset = configFile.readInt(section, "offset", 0);
    xScale = configFile.readDouble(section, "x scale", 1.0);
    yScale = configFile.readDouble(section, "y scale", 1.0);
    baseLine = configFile.readInt(section, "baseline", 0);

    String peakTypeText = configFile.readString(
                                        section, "peak type", "catch highest");
    parsePeakType(peakTypeText);

    leadDataPlotter = configFile.readBoolean(
                                          section, "lead data plotter", true);

}// end of Trace::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::parsePeakType
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

}// end of Trace::parsePeakType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setXScale
//
// Sets the trace display horizontal scale to pScale.
//

public void setXScale(double pScale)
{

    xScale = pScale;

}// end of Trace::setXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setYScale
//
// Sets the trace display vertical scale to pScale.
//

public void setYScale(double pScale)
{

    yScale = pScale;

}// end of Trace::setYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setOffset
//
// Sets the display offset for Trace pTrace to pOffset.
//

public void setOffset(int pOffset)
{

    offset = pOffset;

}// end of Trace::setOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setBaseLine
//
// Sets the baseLine value to pBaseLine. This will cause the pBaseline
// value to be shifted to zero when the trace is drawn.
//

public void setBaseLine(int pBaseLine)
{

    baseLine = pBaseLine;

}// end of Trace::setBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setConnectPoints
//
// Sets the connectPoints flag. If true, points will be connected by a line.
//

public void setConnectPoints(boolean pValue)
{

    connectPoints = pValue;

}// end of Trace::setConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::resetData
//
// Resets all data to zero and all flags to default. Resets all buffer pointers
// to starting positions.
//

public void resetData()
{

    if (dataBuffer!=null) { dataBuffer.reset(); }

    dataIndex = 0; gridTrack = 0;
    prevX = -1; prevY = Integer.MAX_VALUE;

    //reset local data buffers
    data.clear(); dataFlags.clear();

    //reset segment starts and ends
    lastSegmentStartIndex = -1; lastSegmentEndIndex = -1;

}// end of Trace::resetData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setVisible
//
// Sets the visible flag to pVisible.
//

public void setVisible(boolean pVisible)
{

    visible = pVisible;

}// end of Trace::setVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::checkForMatch
//
// Tests to see if this trace matches the trace specified by pGuiDataSet by
// comparing the chartGroupNum, chartNum, graphNum, and traceNum.
//
// If all values match, returns true. Otherwise returns false.
//

public boolean checkForMatch(GUIDataSet pGuiDataSet)
{

    return(
            (chartGroupNum == pGuiDataSet.chartGroupNum)
            && (chartNum == pGuiDataSet.chartNum)
            && (graphNum == pGuiDataSet.graphNum)
            && (traceNum == pGuiDataSet.traceNum)
            );

}// end of Trace::checkForMatch
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::paintTrace
//
// Draws the entire trace, drawing the grid as it goes.
//

public void paintTrace(Graphics2D pG2)
{

    if(!visible) { return; }

    int realX = prevX; prevX=-1; //store prev x for use after repaint
    int realY = prevY; prevY=0; //store prev y for use after repaint
    int realGridTrack = gridTrack; gridTrack=0; //store for use after repaint

    //start index at offset point
    int index = graphInfo.scrollOffset;

    //stop short of the end of the screen to avoid triggering chart scroll
    //in the plotPoint function
    for(int i=0; i<xMax; i++){

        //quit if index beyond data size
        if (index>=data.size()) { break; }

        //snag data and flags and inc pointer
        int d = data.get(index);
        int f = dataFlags.get(index);

        paintSingleTraceDataPoint(pG2, index, d, f);
        index++;

    }

    //restore so next data drawn in proper pos
    prevX = realX; prevY = realY; gridTrack = realGridTrack;

}// end of Trace::paintTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::drawGrid
//
// Draw grid lines and dots and other related objects.
//
// Value pX is the x location before taking into account the scroll offset. It
// is used to calculate whether a grid line should be drawn, then offset is
// factored in to calculate the position on the graph.
//

public void drawGrid (Graphics2D pG2, int pX)
{

    pG2.setColor(gridColor);

    //adjust for any scrolling that has occurred
    int xAdj = pX - graphInfo.scrollOffset;
    int prevXAdj = prevX - graphInfo.scrollOffset;

    for(int i=prevXAdj+1; i<=xAdj; i++){
        if (drawGridBaseline) {
            int y;
            if(invertTrace) { y=yMax; } else { y=0; }
            pG2.drawLine(i, y, i, y);
        }

        if((++gridTrack) == 10){
            gridTrack = 0;
            for(int j=gridY1; j<yMax; j+=gridYSpacing){
                pG2.drawLine(xAdj, j, xAdj, j);
            }
        }
    }

}// end of Trace::drawGrid
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::scrollGraph
//
// Scrolls the graph area to the left and erases the right most slice.
// The graph will be scrolled until location pX would be located on the graph.
//
// Note that pX may have skipped several values since the last pX if the x scale
// is larger than 1, so the graph may be shifted more than one pixel to bring
// the new pX value onto the graph range.
//

public void scrollGraph (Graphics2D pG2, int pX)
{
    //number of pixels to shift to bring pX back on the graph
    int shiftAmt = pX - graphInfo.scrollOffset - xMax;

    //scroll the screen to the left
    pG2.copyArea(0, 0, width, height, -1 * shiftAmt, 0);
    //erase the line at the far right
    pG2.setColor(backgroundColor);
    pG2.fillRect(width-shiftAmt, 0, shiftAmt, height);

    graphInfo.scrollOffset += shiftAmt;
    graphInfo.lastScrollAmount += shiftAmt;

}// end of Trace::scrollGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::handleLeadPlotterActions
//
// Performs all actions which only need to be done once for all traces on the
// graph. These actions, such as scrolling, grid drawing, etc. are triggered
// by the lead plotter being updated.
//

public void handleLeadPlotterActions (Graphics2D pG2, int pX)
{

    //scroll chart left if enabled and new point is off the chart
    if((pX - graphInfo.scrollOffset) > xMax){ scrollGraph(pG2, pX); }

    drawGrid(pG2, pX);

    //tell thresholds to draw their lines to the x
    for (Threshold t : thresholds) { t.drawNextSlice(pG2, pX); }

    graphInfo.lastDrawnX = pX - graphInfo.scrollOffset;

}// end of Trace::handleLeadPlotterActions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::paintSingleTraceDataPoint
//
// Draws line from the last point drawn to data point pDataIndex, pY and
// processes pFlags as appropriate where pDataIndex is used to calculate the
// x position of the data point by applying scaling and scroll offset.
//

public void paintSingleTraceDataPoint(
                             Graphics2D pG2,int pDataIndex, int pY, int pFlags)
{

    if(!visible) { return; }

    //calculate the x position in pixels
    int x = (int)Math.round(pDataIndex * xScale);

    //lead buffer invokes scrolling and decorating
    if (leadDataPlotter || (dataBuffer!=null && dataBuffer.isLeadBuffer())){ 
        handleLeadPlotterActions(pG2, x); 
    }

    //adjust for any scrolling that has occurred before plotting
    int xAdj = x - graphInfo.scrollOffset;
    int prevXAdj = prevX - graphInfo.scrollOffset;

    //draw a vertical line if the flag is set
    if ((pFlags & DataFlags.VERTICAL_BAR) != 0){
        pG2.setColor(VERTICAL_BAR_COLOR);
        pG2.drawLine(xAdj, 0, xAdj, yMax);
    }

    pG2.setColor(traceColor);

    int y = calculateY(pY);

    //if so configured, invert y so zero is at the bottom of the chart

    if(invertTrace){
        if(y > yMax){ y = 0; }
        else { y = yMax - y; }
    }

    //draw between each two points
    if(connectPoints) {
        pG2.drawLine(prevXAdj, prevY, xAdj, y);
    }
    else{
        pG2.drawLine(xAdj, y, xAdj, y);
    }

    prevX = x; prevY = y;

    //if there is a flag set for this data point then draw it - threshold

    //indices are shifted by two as 0 = no flag and 1 = user flag
    flagThreshold = ((pFlags & DataFlags.THRESHOLD_MASK) >> 9)-2;
    if (flagThreshold>=0) { thresholds[flagThreshold].drawFlag(pG2, xAdj, y); }

    //draw a circle on the datapoint if the CIRCLE flag is set
    if ((pFlags & DataFlags.CIRCLE) != 0){
        pG2.setColor(circleColor);
        pG2.draw(new Ellipse2D.Double(xAdj-3, y-3, 6, 6));
    }

    //if segment start/end flag set, draw a vertical separator bar, store index
    if ((pFlags & DataFlags.SEGMENT_START_SEPARATOR) != 0) {
        lastSegmentStartIndex = pDataIndex;
        pG2.setColor(gridColor);
        pG2.drawLine(xAdj, yMax, xAdj, 0);
    }
    if ((pFlags & DataFlags.SEGMENT_END_SEPARATOR) != 0) {
        lastSegmentEndIndex = pDataIndex;
        pG2.setColor(gridColor);
        pG2.drawLine(xAdj, yMax, xAdj, 0);
    }

}// end of Trace::paintSingleTraceDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::updateTrace
//
// Plots all data added to dataBuffer and erases any data which has been
// marked as erased.
//

public void updateTrace(Graphics2D pG2)
{

    int r;

    while((r = dataBuffer.getDataChange(dataSet)) != 0){
        
        //check to see if this data point should be segment start
        checkSegmentStart(dataSet);
        
        //check and flag any threshold violations
        checkThresholdViolations(dataSet);

        //store for future use
        data.add(dataSet.d);
        dataFlags.add(dataSet.flags);

        paintSingleTraceDataPoint(pG2, dataIndex, dataSet.d, dataSet.flags);

        if(r == 1){ dataIndex++; }
        else if(r == -1){ dataIndex--; }

    }

}// end of Trace::updateTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::checkThresholdViolations
//
// Checks to see if any thresholds were violated. If a threshold was violated,
// that threshold's number is stored in the flags stored in pDataSet. Whether 
// this is above or below the threshold is determined by flagOnOver.
//
// The threshold with the lowest (0) thresholdIndex is the highest severity
// threshold, highest index is lowest.  This function should be called for the
// thresholds in order of their index which happens automatically if they are
// stored in an array in this order.  If called in this order, no more
// thresholds should be checked after one returns true because lower severity
// thresholds should not override higher ones.
//

private void checkThresholdViolations(DataSetInt pDataSet)

{
    
    if (!flaggingEnabled) { return; } //bail if not flagging

    for (int i=0; i<thresholds.length; i++) {

        ThresholdInfo info = thresholds[i].getThresholdInfo();
        
        int lvl = info.getLevel();

        //true check for signal above, if false check for signal below
        if (info.getFlagOnOver()){
            if (pDataSet.d >= lvl) { flagThresholdViolation(i , pDataSet); }
        }
        else{ if (pDataSet.d <= lvl) { flagThresholdViolation(i , pDataSet); } }

    }

}//end of Trace::checkThresholdViolations
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::flagThresholdViolation
//
// Changes the flags variable found in pDataSet to indicate that a threshold
// has been violated.
//
// The threshold with the lowest (0) thresholdIndex is the highest severity
// threshold, highest index is lowest.  This function should be called for the
// thresholds in order of their index which happens automatically if they are
// stored in an array in this order.  If called in this order, no more
// thresholds should be checked after one returns true because lower severity
// thresholds should not override higher ones.
//

private int flagThresholdViolation(int pThresholdNum, DataSetInt pDataSet)

{
            
    pDataSet.flags &= DataFlags.CLEAR_THRESHOLD_MASK; //erase old value
    
    //shift up by value of 2 (see notes above)
    pThresholdNum += 2;
    
    //mask top bits to protect against invalid value
    pThresholdNum &= DataFlags.TRIM_THRESHOLD_MASK;
    
    pDataSet.flags += pThresholdNum << 9; //store new flag

    return -1; //-1 because no threshold violated

}//end of Trace::flagThresholdViolation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//

public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList,
                                                           String pObjectType)
{

    if (objectType.equals(pObjectType)){ pObjectList.add(this); }

}// end of Trace::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::calculateY
//
// Calculates and returns the scaled and offset y derived from pY.
//

private int calculateY(int pY)
{

    return (int)Math.round(((pY - baseLine) * yScale) + offset);

}// end of Trace::calculateY
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::getPeak
//
// Searches for and returns the highest peak within the indexes specified.
//

public int getPeak (int pXStart, int pXEnd)
{

    //ensure index start falls within array bounds
    if (pXStart<0) { lastRequestedPeakX = -1; return -1; }

    lastRequestedPeak=-1;
    for (int i=pXStart; i<=pXEnd&&i<data.size(); i++){
        if(data.get(i)>lastRequestedPeak) {
            lastRequestedPeak = data.get(i);
            lastRequestedPeakX = i;
        }
    }

    return lastRequestedPeak;

}// end of Trace::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::getPeak
//
// Searches for and returns the highest peak within the box specified.
//

public int getPeak (int pXStart, int pXEnd, int pYStart, int pYEnd)
{

    //get the peak within the x points
    int peak = getPeak(pXStart, pXEnd);

    //if peak does not lie within y points, set peak to -1
    lastRequestedPeakY = calculateY(peak);
    if (lastRequestedPeakY<pYStart||pYEnd<lastRequestedPeakY) { peak = -1; }

    return peak;

}// end of Trace::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::markSegmentStart
//
// Sets the flag to indicate that the next read data point should be flagged
// as segment start.
//

public void markSegmentStart()
{
    
    //bail if no data stored yet, just use flag read in from dataBuffer later
    if (dataFlags.size()<=0) { return; }
    
    //set flag at last data flag retrieved
    lastSegmentStartIndex = dataFlags.size()-1;
    int newFlag = dataFlags.get(lastSegmentStartIndex) | DataFlags.SEGMENT_START_SEPARATOR;
    dataFlags.set(lastSegmentStartIndex, newFlag);
    
}//end of Trace::markSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::markSegmentEnd
//
// Sets the flag of the last read data point to indicate that the data point
// assoicated with a segment end.
//

public void markSegmentEnd()
{
    
    //bail if no data stored yet, just use flag read in from dataBuffer later
    if (dataFlags.size()<=0) { return; }
    
    //set flag at last data flag retrieved
    lastSegmentEndIndex = dataFlags.size()-1;
    int newFlag = dataFlags.get(lastSegmentEndIndex) | DataFlags.SEGMENT_END_SEPARATOR;
    dataFlags.set(lastSegmentEndIndex, newFlag);

}//end of Trace::markSegmentEnd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::isSegmentStarted
//
// Checks to see if a segment has been started.  If the insertion point has
// moved a predetermined amount after the current segment was initiated, it is
// assumed that a segment has been started.
//
// The insertion point must move more than a few counts to satisfy the start
// criteria. This is to ignore any small errors.
//

public boolean isSegmentStarted()
{

    return lastSegmentStartIndex>-1 && data.size()>10;

}//end of Trace::isSegmentStarted
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::checkSegmentStart
//
// If in INSPECT or INSPECT_WITH_TIMER_DRIVEN_TRACKING mode, will start a
// segment if the flag to mark the next data point read in is true. The segment 
// is started by setting the flags variable in pDataSet to indicate a segment 
// start.
//

private void checkSegmentStart(DataSetInt pDataSet)
{
    
    if ((sharedSettings.opMode != SharedSettings.INSPECT_MODE 
        && sharedSettings.opMode != SharedSettings.INSPECT_WITH_TIMER_TRACKING_MODE)
        || lastSegmentStartIndex != -1)
        { return; } //bail if not in proper modes or if already started
    
    //DEBUG HSS// //WIP HSS// perform check to ensure distance traveled is past mask
    
    pDataSet.flags |= DataFlags.SEGMENT_START_SEPARATOR;

}//end of Trace::checkSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::saveSegment
//
// Saves data for the current segment to file.
//

public void saveSegment(BufferedWriter pOut) throws IOException
{

    //save trace meta data
    pOut.write("[Trace]"); pOut.newLine();
    pOut.write("Trace Index=" + traceNum); pOut.newLine();
    pOut.write("Trace Title=" + title); pOut.newLine();
    pOut.write("Trace Short Title=" + shortTitle); pOut.newLine();
    pOut.newLine();

    //catch unexpected case where start/stop are invalid and bail
    if (lastSegmentStartIndex < 0 || lastSegmentEndIndex < 0) {
        pOut.write("Segment start and/or start invalid - no data saved.");
        pOut.newLine(); pOut.newLine();
        return;
    }

    //save trace data points
    pOut.write("[Data Set 1]"); pOut.newLine();
    for (int i=lastSegmentStartIndex; i<=lastSegmentEndIndex; i++) {
        pOut.write(Integer.toString(data.get(i))); //write to file
        pOut.newLine();
    }
    pOut.write("[End of Set]"); pOut.newLine();

    //save trace flags
    pOut.write("[Flags]"); pOut.newLine();
    for (int i=lastSegmentStartIndex; i<=lastSegmentEndIndex; i++) {
        pOut.write(Integer.toString(dataFlags.get(i))); //write to file
        pOut.newLine();
    }
    pOut.write("[End of Set]"); pOut.newLine();

    pOut.newLine(); //blank line

}//end of Trace::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::loadSegment
//
// Loads segment from the IniFile.
//

public String loadSegment(BufferedReader pIn, String pLastLine)
        throws IOException
{

    data.clear(); dataFlags.clear(); //make sure clear

    String line = processTraceMetaData(pIn, pLastLine);

    try{
        //read in trace data points
        line = loadDataSeries(pIn, line, "[Data Set 1]", data, 0);

        //read in trace flags
        line = loadDataSeries(pIn, line, "[Flags]", dataFlags,
                                DataFlags.DATA_VALID);
    }
    catch(IOException e){

        //add identifying details to the error message and pass it on
        throw new IOException(e.getMessage() + " of " + section);
    }
    
    updateDimensions(data.size(), height);//DEBUG HSS//

    return line;

}//end of Trace::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::processTraceMetaData
//
// Processes file entries for the trace such as the title via pIn.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//
// For the Trace section, the [Trace] tag may or may not have already
// been read from the file by the code handling the previous section.  If it has
// been read, the line containing the tag should be passed in via pLastLine.
//

private String processTraceMetaData(BufferedReader pIn, String pLastLine)
                                                             throws IOException

{

    String line;
    boolean success = false;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //if pLastLine contains the [Trace] tag, then start loading section
    //immediately else read until "[Trace]" section tag reached

    if (Tools.matchAndParseString(pLastLine, "[Trace]", "",  matchSet)) {
        success = true; //tag already found
    }
    else {
        while ((line = pIn.readLine()) != null){  //search for tag
            if (Tools.matchAndParseString(line, "[Trace]", "",  matchSet)){
                success = true; break;
            }
        }//while
    }//else

    if (!success) {
        throw new IOException(
            "The file could not be read - section not found for " + section);
    }

    //set defaults
    int traceNumRead = -1;
    String titleRead = "", shortTitleRead = "";

    //scan the first part of the section and parse its entries
    //these entries apply to the chart group itself

    success = false;
    while ((line = pIn.readLine()) != null){

        //stop when next section tag reached (will start with [)
        if (Tools.matchAndParseString(line, "[", "",  matchSet)){
            success = true; break;
        }

        //read the "Trace Index" entry - if not found, default to -1
        if (Tools.matchAndParseInt(line, "Trace Index", -1, matchSet)) {
            traceNumRead = matchSet.rInt1;
        }

        //read the "Trace Title" entry - if not found, default to ""
        if (Tools.matchAndParseString(line, "Trace Title", "", matchSet)) {
            titleRead = matchSet.rString1;
        }

        //read the "Trace Short Title" entry - if not found, default to ""
        if (Tools.matchAndParseString(
                                    line, "Trace Short Title", "", matchSet)) {
            shortTitleRead = matchSet.rString1;
        }

    }

    //apply settings
    title = titleRead; shortTitle = shortTitleRead;

    if (!success) {
        throw new IOException(
        "The file could not be read - missing end of section for " + section);
    }

    //if the index number in the file does not match the index number for this
    //trace, abort the file read

    if (traceNumRead != traceNum) {
        throw new IOException(
            "The file could not be read - section not found for " + section);
    }

    return(line); //should be "[xxxx]" tag on success, unknown value if not

}//end of Trace::processTraceMetaData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::loadDataSeries
//
// Loads a data series into a one dimensional array from pIn.  The series could
// be "Data Set 1", "Data Set 2", or "Flags", etc. depending on the parameters
// passed in.
//
// The pStartTag string specifies the section start tag for the type of data
// expected and could be: "[Data Set 1]", "[Data Set 2]", or "[Flags]".  The
// pBuffer pointer should be set to the buffer associated with the data type.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//
// For these sections, the [xxx] section start tag may or may not have already
// been read from the file by the code handling the previous section.  If it has
// been read, the line containing the tag should be passed in via pLastLine.
//
// Value pDataModifier1 will be ORed with each data point as it is stored in
// the buffer. This allows any bit(s) to be forced to 1 if they are used as
// flag bits. If no bits are to be forced, pDataModifier1 should be 0.
//

public String loadDataSeries(BufferedReader pIn, String pLastLine,
                            String pStartTag, ArrayList<Integer> pBuffer,
                            int pDataModifier1) throws IOException
{

    String line;
    boolean success = false;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //if pLastLine contains the [xxx] tag, then skip ahead else read until
    // end of file reached or "[xxx]" section tag reached

    if (Tools.matchAndParseString(pLastLine, pStartTag, "",  matchSet)) {
        success = true;  //tag already found
    }
    else {
        while ((line = pIn.readLine()) != null){  //search for tag
            if (Tools.matchAndParseString(line, pStartTag, "",  matchSet)){
                success = true; break;
            }
        }//while
    }//else

    if (!success) {
        throw new IOException(
           "The file could not be read - section not found for " + pStartTag);
    }

    //scan the first part of the section and parse its entries

    int i = 0;
    success = false;
    while ((line = pIn.readLine()) != null){

        //stop when next section end tag reached (will start with [)
        if (Tools.matchAndParseString(line, "[", "",  matchSet)){
            success = true; break;
        }

        try{

            //convert the text to an integer and save in the buffer
            int dataInt = Integer.parseInt(line);
            pBuffer.add(dataInt | pDataModifier1);

            //catch buffer overflow
            if (i == pBuffer.size()) {
                throw new IOException(
                 "The file could not be read - too much data for " + pStartTag
                                                       + " at data point " + i);
            }

        }
        catch(NumberFormatException e){
            //catch error translating the text to an integer
            throw new IOException(
             "The file could not be read - corrupt data for " + pStartTag
                                                       + " at data point " + i);
        }

    }//while ((line = pIn.readLine()) != null)

    if (!success) {
        throw new IOException(
         "The file could not be read - missing end of section for "
                                                                + pStartTag);
    }

    return(line); //should be "[xxxx]" tag on success, unknown value if not

}//end of Trace::loadDataSeries
//-----------------------------------------------------------------------------

}//end of class Trace
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

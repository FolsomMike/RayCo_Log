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
import java.util.ArrayList;
import model.DataSetInt;
import model.DataTransferIntBuffer;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Trace
//

public class Trace{

    private IniFile configFile;

    private GraphInfo graphInfo;

    DataSetInt dataSet = new DataSetInt();

    DataTransferIntBuffer dataBuffer;
    public void setDataBuffer(DataTransferIntBuffer pV) { dataBuffer = pV; }
    public DataTransferIntBuffer getDataBuffer() { return(dataBuffer); }

    ArrayList<Integer> data = new ArrayList<>(10000);
    ArrayList<Integer> dataFlags = new ArrayList<>(10000);

    private String title, shortTitle, objectType;
    public Color traceColor;
    public String colorKeyText;
    public int colorKeyXPos;
    public int colorKeyYPos;
    public int chartGroupNum, chartNum, graphNum, traceNum;
    private int width, height;
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
              int pGridYSpacing, GraphInfo pGraphInfo, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    graphNum = pGraphNum; traceNum = pTraceNum;
    width = pWidth; height = pHeight;
    backgroundColor = pBackgroundColor;
    drawGridBaseline = pDrawGridBaseline; gridColor = pGridColor;
    gridXSpacing = pGridXSpacing; gridYSpacing = pGridYSpacing;
    graphInfo = pGraphInfo; configFile = pConfigFile;

    gridY1 = gridYSpacing-1; //do math once for repeated use

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

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                + " Graph " + graphNum + " Trace " + traceNum;

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

    int realX = prevX; prevX=0; //store prev x for use after repaint
    int realY = prevY; prevY=0; //store prev x for use after repaint
    int realGridTrack = gridTrack; gridTrack=0; //store for use after repaint
    for(int i=0; i<width-1; i++){

        if (i>=data.size()) { break; }

        paintSingleTraceDataPoint(pG2, i, data.get(i), dataFlags.get(i));

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
    graphInfo.lastScrollAmount = shiftAmt;

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

    //lead plotter invokes scrolling and decorating
    if (leadDataPlotter){ handleLeadPlotterActions(pG2, x); }

    //adjust for any scrolling that has occurred before plotting
    int xAdj = x - graphInfo.scrollOffset;
    int prevXAdj = prevX - graphInfo.scrollOffset;

    //draw a vertical line if the flag is set
    if ((pFlags & DataTransferIntBuffer.VERTICAL_BAR) != 0){
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

    //draw a circle on the datapoint if the CIRCLE flag is set
    if ((pFlags & DataTransferIntBuffer.CIRCLE) != 0){
        pG2.setColor(circleColor);
        pG2.draw(new Ellipse2D.Double(xAdj-3, y-3, 6, 6));
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

        //System.out.print(" ~ Peak: " + dataSet.d);//DEBUG HSS//

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

    return (int)((pY - baseLine) * yScale) + offset;

}// end of Trace::calculateY
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::getPeak
//
// Searches for and returns the highest peak within the indexes specified.
//

public int getPeak (int pXStart, int pXEnd)
{

    //ensure index starts and ends don't exceed array bounds
    if (pXStart<0||pXEnd>=data.size()) { lastRequestedPeakX = -1; return -1; }

    lastRequestedPeak=0;
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

}//end of class Trace
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

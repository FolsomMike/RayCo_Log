/******************************************************************************
* Title: Graph.java
* Author: Mike Schoonover
* Date: 02/28/15
*
* Purpose:
*
* This class subclasses a JPanel and is the parent class to various types
* of graphing objects.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import model.DataTransferIntMultiDimBuffer;
import model.DataTransferSnapshotBuffer;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Graph
//

public class Graph extends JPanel{

    IniFile configFile;
    String fileSection;
    String metaDataSectionName;

    ChartInfo chartInfo;
    public GraphInfo graphInfo = new GraphInfo();
    public GraphInfo getGraphInfo() { return graphInfo; }

    SharedSettings sharedSettings;

    String title, shortTitle, objectType;
    int chartGroupNum;
    public int getChartGroupNum(){ return(chartGroupNum); }
    int chartNum;
    public int getChartNum(){ return(chartNum); }
    int graphNum;
    public int getGraphNum(){ return(graphNum); }
    int width, height, specifiedWidth, specifiedHeight;
    Color backgroundColor;

    DataTransferSnapshotBuffer snapshotBuffer; //see notes at top of file
    public void setSnapshotBuffer(DataTransferSnapshotBuffer pSnapBuffer)
        { snapshotBuffer = pSnapBuffer; }
    public DataTransferSnapshotBuffer getSnapshotBuffer(){ return snapshotBuffer; }

    DataTransferIntMultiDimBuffer mapBuffer; //see notes at top of file
    public void setMapBuffer(DataTransferIntMultiDimBuffer pMapBuffer){
                                                      mapBuffer = pMapBuffer; }
    public DataTransferIntMultiDimBuffer getMapBuffer(){ return mapBuffer; }

    int scrollTrackChartGroupNum, scrollTrackChartNum, scrollTrackGraphNum;
    ArrayList<Graph> graphsTrackingThisGraphsScrolling;
    ArrayList<Graph> getGraphsTrackingThisGraphsScrolling(){
                                return (graphsTrackingThisGraphsScrolling); }

    //graph info of the graph that this guy tracks for scrolling
    GraphInfo scrollTrackGraphInfo;
    public void setScrollTrackGraphInfo(GraphInfo pG) { scrollTrackGraphInfo = pG; }

    int animationDirection = 0;
    int animationCount = 0;

    int numThresholds;
    Threshold[] thresholds;
    public Threshold[] getThresholds() { return thresholds; }

    //peakType is not used for all Graph types, some child classes such as
    //Trace load their own peakType setting from config file

    private int peakType;
    public int getPeakType(){ return(peakType); }

    //type of graph subclasses

    public static final int UNDEFINED_GRAPH = 0;
    public static final int TRACE_GRAPH = 1;
    public static final int ZOOM_GRAPH = 2;
    public static final int MAP3D_GRAPH = 3;

    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;

//-----------------------------------------------------------------------------
// Graph::Graph (constructor)
//
//

public Graph(int pChartGroupNum, int pChartNum, int pGraphNum,
            int pWidth, int pHeight, ChartInfo pChartInfo, IniFile pConfigFile,
            SharedSettings pSettings)
{

    chartGroupNum = pChartGroupNum;
    chartNum = pChartNum; graphNum = pGraphNum;
    width = pWidth; height = pHeight;
    chartInfo = pChartInfo; configFile = pConfigFile; sharedSettings=pSettings;

}//end of Chart::Graph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pTitle is the text title for the graph.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init()
{

    loadConfigSettings();

    setSizes(this, width, height);

}// end of Graph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//
// Should be overridden by child classes to provide custom handling.
//

public void updateDimensions()
{


}// end of Chart::updateDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::resetAll
//
// Resets all values and child values to default.
//

public void resetAll()
{

    graphInfo.scrollOffset = 0;
    graphInfo.lastScrollAmount = 0;

}// end of Graph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::update
//
// Updates with new parameters and forces a repaint. The new parameters are
// stored as objects in pValues.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void update(ArrayList <Object> pValues)
{


}// end of Graph::update
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getParameters
//
// Retrieves parameters specific to the subclass via objects in an ArrayList.
// Each subclass returns different parameters specific to its functionality.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public ArrayList<Object>getParameters()
{

    return(null);

}// end of Graph::getParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::scrollGraph
//
// Scrolls the graph area to the left by pShiftAmount and erases the right most
// slice.
//

public void scrollGraph (int pShiftAmount)
{

    Graphics2D g2 = (Graphics2D) getGraphics();

    //scroll the screen to the left
    g2.copyArea(0, 0, width, height, -1 * pShiftAmount, 0);
    //erase the line at the far right
    g2.setColor(backgroundColor);
    g2.fillRect(width-pShiftAmount, 0, pShiftAmount, height);

    graphInfo.scrollOffset += pShiftAmount;
    graphInfo.lastScrollAmount = pShiftAmount;

}// end of Graph::scrollGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getTrace
//
// Returns Trace pTrace.
//
// Generally overridden by subclasses to return a valid value.
//

public Trace getTrace(int pTrace)
{

    return(null);

}// end of Graph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getNumChildren
//
// Returns the number of child objects.
//
// Generally overridden by subclasses to return a valid value.
//

public int getNumChildren()
{

    return(0);

}// end of Graph::getNumChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setAllChildrenXScale
//
// Sets the display horizontal scale for all children to pScale.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setAllChildrenXScale(double pScale)
{


}// end of Graph::setAllChildrenXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildYScale
//
// For child pChildNum, sets the display vertical scale to pScale
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildYScale(int pChildNum, double pScale)
{

}// end of Graph::setChildYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildOffset
//
// For Trace pChildNum, sets the display offset to pOffset.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildOffset(int pChildNum, int pOffset)
{

}// end of Graph::setChildOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildBaseLine
//
// For child pChildNum, sets the baseLine value to pBaseLine. This will cause
// the pBaseline value to be shifted to zero when the child is drawn.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildBaseLine(int pChildNum, int pBaseLine)
{

}// end of Graph::setChildBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildConnectPoints
//
// For child pChildNum, sets the connectPoints flag. If true, points will be
// connected by a line.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildConnectPoints(int pChildNum, boolean pValue)
{


}// end of Graph::setChildConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setVerticalBarAllChildren
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setVerticalBarAllChildren()
{

}// end of Graph::setVerticalBarAllChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList,
                                                           String pObjectType)
{

    if (objectType.equals(pObjectType)){ pObjectList.add(this); }

}// end of Graph::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::isSegmentStarted
//
// Checks to see if a segment has been started and thus may have data which
// needs to be saved.
//

public boolean isSegmentStarted()
{

    return(false);

}//end of Graph::isSegmentStarted
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::updateChild
//
// Plots all new data added to the data transfer buffer and erases any data
// which has been marked as erased for pChildNum.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void updateChild(int pChildNum)
{

}// end of Graph::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::updateChildren
//
// Instructs all children listening for transfer buffer changes to check for
// changes and update.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void updateChildren()
{

}// end of Graph::updateChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setHeight
//
// Sets the height to pHeight. The calling oject is responsible for repacking
// the frame if desired.
//

public void setHeight(int pHeight)
{

    height = pHeight;
    setSizes(this, width, height);
    invalidate();

}// end of Graph::setHeight
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::addGraphTrackingThisGraphsScrolling
//
// Adds graph pGraph to the list of graphs which should be scolled when this
// graph is scrolled.
//

public void addGraphTrackingThisGraphsScrolling(Graph pGraph)
{

    if (graphsTrackingThisGraphsScrolling == null){
        graphsTrackingThisGraphsScrolling = new ArrayList<>();
    }

    graphsTrackingThisGraphsScrolling.add(pGraph);

}// end of Graph::addGraphTrackingThisGraphsScrolling
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getGraphTrackedForScrolling
//
// Returns the chart group/chart/graph numbers for the graph which this graph
// should track when the former is scrolled.
//
// If no graph is to be tracked, returns null.
//

public GUIDataSet getGraphTrackedForScrolling()
{

    if (scrollTrackChartGroupNum == -1 || scrollTrackChartNum == -1
                                                || scrollTrackGraphNum == -1){
        return(null);
    }

    GUIDataSet gds = new GUIDataSet();

    gds.chartGroupNum = scrollTrackChartGroupNum;
    gds.chartNum = scrollTrackChartNum;
    gds.graphNum = scrollTrackGraphNum;

    return(gds);

}// end of Graph::getGraphTrackedForScrolling
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setViewParamsToNormalLayout
//
// Sets the current viewing parameters to the normal layout.
//

public void setViewParamsToNormalLayout()
{

}// end of Graph::setViewParamsToNormalLayout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setViewParamsToExpandedLayout
//
// Sets the current viewing parameters to the expanded layout.
//

public void setViewParamsToExpandedLayout()
{

}// end of Graph::setViewParamsToExpandedLayout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::animate
//
// Animates the graph in some manner dependent on the subclass.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void animate()
{

}// end of Graph::animate
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::paintChildren
//
// Paints all the child objects on the canvas.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void paintChildren(Graphics2D pG2)
{

}// end of Graph::paintChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildCanvasSizeToMatchPanel
//
// Sets the canvas size in the map or other display to match the containing
// panel. This can be done when the panel size is changed so that content can
// be centered the display.
//
// The width and height are retrieved directly from the panel component using
// getWidth and getHeight to make sure the actual panel size is used.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildCanvasSizeToMatchPanel()
{

}// end of Graph::setChildCanvasSizeToMatchPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildCanvasSize
//
// Sets the canvas size in the map or other display to pWidth, pHeight.
// This can be done when the panel size is changed so that content can be
// centered the display.
//
// If either value is set to Integer.MAX_VALUE, that value is not changed.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildCanvasSize(int pWidth, int pHeight)
{

}// end of Graph::setChildCanvasSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::parsePeakType
//
// Converts the descriptive string loaded from the config file for the peak
// type (catch highest, lowest value, etc.) into the corresponding constant.
//

void parsePeakType(String pValue)
{

    switch (pValue) {
         case "catch highest": peakType = CATCH_HIGHEST; break;
         case "catch lowest" : peakType = CATCH_LOWEST;  break;
         default : peakType = CATCH_LOWEST;  break;
    }

}// end of Graph::parsePeakType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{

    title = configFile.readString(
             fileSection, "title", "Graph " + (graphNum + 1));
    graphInfo.title = title;

    shortTitle = configFile.readString(
               fileSection, "short title", "graph" + (graphNum + 1));
    graphInfo.shortTitle = shortTitle;

    objectType = configFile.readString(
                                    fileSection, "object type", "graph");

    int configWidth = configFile.readInt(fileSection, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0

    specifiedWidth = width; //save for restoring to normal size

    int configHeight = configFile.readInt(fileSection, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0

    specifiedHeight = height; //save for restoring to normal size

    backgroundColor = configFile.readColor(
                                fileSection, "background color", null);

    if(backgroundColor == null) { backgroundColor = getBackground(); }

    scrollTrackChartGroupNum = configFile.readInt(fileSection,
                      "chart group number of graph tracked for scrolling", -1);
    scrollTrackChartNum = configFile.readInt(fileSection,
                            "chart number of graph tracked for scrolling", -1);
    scrollTrackGraphNum = configFile.readInt(fileSection,
                            "graph number of graph tracked for scrolling", -1);

    String peakTypeText = configFile.readString(
                              fileSection, "peak type", "catch highest");
    parsePeakType(peakTypeText);

}// end of Graph::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void loadCalFile(IniFile pCalFile)
{

}//end of Graph::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void saveCalFile(IniFile pCalFile)
{

    pCalFile.writeString(fileSection, "Graph Test Value", "Test");

}//end of Graph::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::saveSegment
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void saveSegment(BufferedWriter pOut)  throws IOException
{

    saveMetaData(pOut);

}//end of Graph::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::loadSegment
//
// Loads segments' data.
//

public String loadSegment(BufferedReader pIn, String pLastLine)
        throws IOException
{

    String line = processMetaData(pIn, pLastLine, true);

    return line;

}//end of Graph::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::saveMetaData
//
// Saves file entries for this graph such as the title via pOut.
//

protected void saveMetaData(BufferedWriter pOut) throws IOException
{

    pOut.write("["+metaDataSectionName+"]"); pOut.newLine();

    pOut.write("Index=" + graphNum); pOut.newLine();
    pOut.write("Title=" + title); pOut.newLine();
    pOut.write("Short Title=" + shortTitle); pOut.newLine();

}//end of Graph::saveMetaData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::processMetaData
//
// Processes file entries for this graph such as the title via pIn.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//
// For the section, the [section] tag may or may not have already
// been read from the file by the code handling the previous section.  If it has
// been read, the line containing the tag should be passed in via pLastLine.
//

protected String processMetaData(BufferedReader pIn, String pLastLine,
                                    boolean pThrowErrorGraphNumNoMatch)
    throws IOException

{

    String section = "[" + metaDataSectionName + "]";

    String line;
    boolean success = false;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //if pLastLine contains the [Trace] tag, then start loading section
    //immediately else read until "[Trace]" section tag reached

    if (Tools.matchAndParseString(pLastLine, section, "",  matchSet)) {
        success = true; //tag already found
    }
    else {
        while ((line = pIn.readLine()) != null){  //search for tag
            if (Tools.matchAndParseString(line, section, "",  matchSet)){
                success = true; break;
            }
        }//while
    }//else

    if (!success) {
        throw new IOException(
            "The file could not be read - section not found for " + fileSection);
    }

    //set defaults
    int indexRead = -1;
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
        if (Tools.matchAndParseInt(line, "Index", -1, matchSet)) {
            indexRead = matchSet.rInt1;
        }

        //read the "Trace Title" entry - if not found, default to ""
        if (Tools.matchAndParseString(line, "Title", "", matchSet)) {
            titleRead = matchSet.rString1;
        }

        //read the "Trace Short Title" entry - if not found, default to ""
        if (Tools.matchAndParseString(line, "Short Title", "", matchSet)) {
            shortTitleRead = matchSet.rString1;
        }

    }

    //apply settings
    title = titleRead; shortTitle = shortTitleRead;

    if (!success) {
        throw new IOException(
        "The file could not be read - missing end of section for " + fileSection);
    }

    //if graph num and index read don't match, throw error if supposed to
    if (pThrowErrorGraphNumNoMatch && indexRead != graphNum) {
        throw new IOException(
            "The file could not be read - section not found for " + fileSection);
    }

    return(line); //should be "[xxxx]" tag on success, unknown value if not

}//end of Graph::processMetaData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of Graph::setSizes
//-----------------------------------------------------------------------------


}//end of class Graph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

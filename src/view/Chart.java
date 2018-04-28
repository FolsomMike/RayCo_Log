/******************************************************************************
* Title: Chart.java
* Author: Mike Schoonover
* Date: 11/01/14
*
* Purpose:
*
* This class subclasses a JPanel to display a chart.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Chart
//

class Chart extends JPanel implements MouseListener, MouseMotionListener {

    private final IniFile configFile;
    private final SharedSettings sharedSettings;

    private String title, shortTitle, objectType;
    private final int chartGroupNum;
    public int getChartGroupNum(){ return(chartGroupNum); }
    private final int chartNum;
    public int getChartNum(){ return(chartNum); }
    private int graphWidth, graphHeight;
    public int getGraphWidth() { return graphWidth; }
    public int getGraphHeight() { return graphHeight; }
    int numGraphs;
    boolean hasZoomGraph = false;
    boolean hasInfoPanel;
    int prevXGraph0;
    
    private boolean isChartVisible = false;

    boolean graphsVisible;
    boolean specifiedGraphsVisible;
    public boolean getSpecifiedGraphsVisible(){return(specifiedGraphsVisible);}

    ChartInfo chartInfo = new ChartInfo();

    private Graph graphs[];
    private ZoomGraph zoomGraph;
    private ArrayList<Object> traces;
    private ChartInfoPanel infoPanel;

    ActionListener parentActionListener;

//-----------------------------------------------------------------------------
// Chart::Chart (constructor)
//
//

public Chart(int pChartGroupNum, int pChartNum, int pDefaultGraphWidth,
              int pDefaultGraphHeight, ActionListener pParentActionListener,
                                IniFile pConfigFile, SharedSettings pSettings)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    configFile = pConfigFile;
    parentActionListener = pParentActionListener;

    graphWidth = pDefaultGraphWidth;
    graphHeight = pDefaultGraphHeight;

    sharedSettings = pSettings;

}//end of Chart::Chart (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    //add a listener to respond when the panel size is changed
    addComponentListener(new ChartComponentAdapter(this));

    loadConfigSettings();

    setBorder(BorderFactory.createTitledBorder(title));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    addGraphs();

    if(hasInfoPanel){ addInfoPanel(); }

    setGraphsVisible(graphsVisible);

    addMouseListener(this);
    addMouseMotionListener(this);
    
    setChartVisible(true); //ensure chart is visible

}// end of Chart::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.

public void updateDimensions()
{

    for (Graph g : graphs){ 
        g.updateDimensions(g.getGraphWidth(), g.getGraphHeight());
    }

}// end of Chart::updateDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateGraphDimensions
//
// Updates all children graph using the passed in dimnesions.

public void updateGraphDimensions(int pNewWidth, int pNewHeight)
{

    graphWidth = pNewWidth; graphHeight = pNewHeight;
    for (Graph g : graphs){ 
        g.updateDimensions(graphWidth, graphHeight);
    }
    
    if (infoPanel!=null) { 
        infoPanel.updateDimensions(graphWidth, infoPanel.getHeight()); 
    }

}// end of Chart::updateGraphDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateGraphYOffset
//
// Updates the specified graph's y-offset value.
//

public void updateGraphYOffset(int pGraph, int pOffset)
{

    graphs[pGraph].updateGraphYOffset(pOffset);

}// end of Chart::updateGraphYOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addGraphs
//
// Adds basic graphs to the chart. The type of each graph is loaded from the
// graph's section in the config file.
//

private void addGraphs()
{
    graphs = new Graph[numGraphs];

    for (int i = 0; i<numGraphs; i++){

        int graphType = loadGraphTypeFromConfigFile(i);

        if (graphType == Graph.TRACE_GRAPH){

            graphs[i] = new TraceGraph(chartGroupNum, chartNum, i,
                                        graphWidth, graphHeight, chartInfo,
                                        configFile, sharedSettings);
            graphs[i].init();
            graphs[i].addMouseMotionListener(this);
            add(graphs[i]);
            addSeparatorPanelSpecifiedInConfigFile(i);

        } else if (graphType == Graph.ZOOM_GRAPH){

            graphs[i] = new ZoomGraph(chartGroupNum, chartNum, i,
                               graphWidth, graphHeight, chartInfo,
                               configFile, sharedSettings);
            graphs[i].init();
            add(graphs[i]);
            addSeparatorPanelSpecifiedInConfigFile(i);
            hasZoomGraph = true;
            zoomGraph = (ZoomGraph)graphs[i]; //convenience reference

        } else if (graphType == Graph.MAP3D_GRAPH){

            graphs[i] = new Map3DGraph(chartGroupNum, chartNum, i,
                                        graphWidth, graphHeight,
                                        chartInfo, configFile, sharedSettings);
            graphs[i].init();
            add(graphs[i]);
            addSeparatorPanelSpecifiedInConfigFile(i);

        }
        
        //if any graphs used larger width or heights, use those
        if (graphs[i].getGraphWidth()>graphWidth) { 
            graphWidth = graphs[i].getGraphWidth(); 
        }
        if (graphs[i].getGraphHeight()>graphHeight) { 
            graphHeight = graphs[i].getGraphHeight(); 
        }

    }

    //set up the list of traces for future use
    traces=new ArrayList<>();
    scanForGUIObjectsOfAType(traces, "trace");

}// end of Chart::addGraphs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::loadGraphTypeFromConfigFile
//
// Loads the graph type from the section in the config file for the graph
// numbered pGraphNum.
//

private int loadGraphTypeFromConfigFile(int pGraphNum)
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                                    + " Graph " + pGraphNum;

    String type  = configFile.readString(section, "graph type", "undefined");

    return (parseGraphType(type));

}// end of Channel::loadGraphTypeFromConfigFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::addSeparatorPanelSpecifiedInConfigFile
//
// Loads the info from the graph pGraphNum's section in the config file for a
// separator panel to be added to the parent container after the graph is added.
//
// If the pHeight value loaded from the config file is 0, no panel will be
// added.
//

private void addSeparatorPanelSpecifiedInConfigFile(int pGraphNum)
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                                    + " Graph " + pGraphNum;

    int spHeight  = configFile.readInt(section, "separator panel height", 0);

    Color spLineColor = configFile.readColor(
                          section, "separator panel line color", Color.BLACK);

    int spLineThickness  = configFile.readInt(
                                section, "separator panel line thickness", 1);

    if (spHeight == 0){ return; } //no panel if height is zero


    addGraphSeparatorPanel(spHeight, spLineColor, spLineThickness);

}// end of Channel::addSeparatorPanelSpecifiedInConfigFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Channel::parseGraphType
//
// Converts the descriptive string loaded from the config file for the graph
// type into the corresponding constant.
//

private int parseGraphType(String pValue)
{

    switch (pValue) {
         case "trace graph": return(Graph.TRACE_GRAPH);
         case "zoom graph" : return(Graph.ZOOM_GRAPH);
         case "3D map graph" : return(Graph.MAP3D_GRAPH);
         default : return(Graph.UNDEFINED_GRAPH);
    }

}// end of Channel::parseGraphType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addInfoPanel
//
// Adds an information panel to the chart for use in explaining color keys,
// displaying monitoring info, etc.
//

private void addInfoPanel()
{

    infoPanel = new ChartInfoPanel(chartGroupNum, chartNum, 0, graphWidth, 15,
                                             parentActionListener, configFile);
    infoPanel.init();
    add(infoPanel);

    //add a color key for each trace

    for(Graph graph: graphs){
        for(int i=0; i<graph.getNumChildren(); i++){
            Trace trace = graph.getTrace(i);
            if (!trace.colorKeyText.equals("hidden")){
                infoPanel.addColorKey(trace.traceColor, trace.colorKeyText,
                                    trace.colorKeyXPos, trace.colorKeyYPos);
            }
        }
    }

}// end of Chart::addInfoPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setGraphsVisible
//
// Sets the visibility of the graphs. The chart is never actually hidden,
// rather the graphs it contains can be hidden so they take up no space which
// allows the chart to be shrunken vertically so it takes up minimal space.
//

public void setGraphsVisible(boolean pState)
{

    graphsVisible = pState;

    for(Graph graph:graphs){ graph.setVisible(pState); }

}// end of Chart::setGraphsVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getGraphHeights
//
// If pGraph != -1, then the height of graph pGraph is returned.
//
// If pChart == -1, returns the sum of the heights of all graphs.
//
// The value(s) of specifiedHeight is used which is the height read from the
// config file rather than whatever height the graph currently happens to be at
// as it may have been minimized.
//

public int getGraphHeights(int pGraph)
{
    //height of a single graph
    if(pGraph != -1){ return (graphs[pGraph].specifiedHeight); }

    //sum of all graph heights
    int h = 0;
    for(Graph graph:graphs){ h += graph.specifiedHeight; }
    return(h);

}// end of Chart::getGraphHeights
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getGraphs
//
// Returns instances of all of the graphs in this Chart.
//

public Graph[] getGraphs()
{
    
    return graphs;
    
}// end of Chart::getGraphs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setGraphHeight
//
// Sets the height of graph pGraph to pHeight. The calling oject is responsible
// for repacking the frame if desired.
//

public void setGraphHeight(int pGraph, int pHeight)
{

    graphs[pGraph].setHeight(pHeight);

}//end of Chart::setGraphHeight
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setViewParamsToNormalLayout
//
// Sets the current viewing parameters of graph pGraph to the scan/inspect
// layout.
//

public void setViewParamsToNormalLayout(int pGraph)
{

    graphs[pGraph].setViewParamsToNormalLayout();

}// end of Graph::setViewParamsToNormalLayout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setViewParamsToExpandedLayout
//
// Sets the current viewing parameters of graph pGraph to the expanded layout.
//

public void setViewParamsToExpandedLayout(int pGraph)
{

    graphs[pGraph].setViewParamsToExpandedLayout();

}// end of Graph::setViewParamsToExpandedLayout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum;

    title = configFile.readString(section, "title", "Chart " + (chartNum + 1));

    shortTitle = configFile.readString(
                             section, "short title", "chart" + (chartNum + 1));

    objectType = configFile.readString(section, "object type", "chart");

    numGraphs = configFile.readInt(section, "number of graphs", 0);

    hasInfoPanel = configFile.readBoolean(section, "has info panel", false);

    int configWidth = configFile.readInt(
                                   section, "default width for all graphs", 0);

    if (configWidth > 0) graphWidth = configWidth; //override if > 0

    int configHeight = configFile.readInt(
                                  section, "default height for all graphs", 0);

    if (configHeight > 0) graphHeight = configHeight; //override if > 0

    graphsVisible = configFile.readBoolean(section, "graphs are visible", true);

    specifiedGraphsVisible = graphsVisible; //save setting from config file

}// end of Chart::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addGraphSeparatorPanel
//
// Adds a panel mean to separate two graphs. This version contains a simple
// line with specified color and thickness.
//

public void addGraphSeparatorPanel(int pHeight, Color pLineColor,
                                                            int pLineThickness)
{

    SeparatorPanel spanel = new SeparatorPanel();

    spanel.init(graphWidth, pHeight, pLineColor , pLineThickness);

    add(spanel);

}// end of Chart::addGraphSeparatorPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::repaintChild
//
// Repaints pGraph of pChart.
//

public void repaintChild(int pGraph)
{

    graphs[pGraph].repaint();

}// end of Chart::repaintChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::repaintGraph
//
// Forces the graph to be repainted.
//

public void repaintGraph()
{

    invalidate();

}// end of Chart::repaintGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateThreshold
//
// Updates the specified threshold.
//

public void updateThreshold(int pGraph, int pThres, int pLvl)
{
    
    
    graphs[pGraph].updateThreshold(pThres, pLvl);

}// end of Chart::updateThreshold
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of Chart::setSizes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);

}// end of Chart::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::paintTraces
//
// Paints all the traces on the canvas.
//

public void paintTraces (Graphics2D pG2)
{

    for(int i=0; i<numGraphs; i++){ graphs[i].paintChildren(pG2); }

}// end of Chart::paintTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setAllTraceXScale
//
// Sets the display horizontal scale for all traces of all graphs to pScale.
//

public void setAllTraceXScale(double pScale)
{

    for (Graph graph : graphs) { graph.setAllChildrenXScale(pScale); }

}// end of Chart::setAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setChildYScale
//
// For child pChildNum of Graph pGraphNum, sets the display vertical scale
// to pScale.
//
// The child might be a trace or other plotting type depending on the graph
// type.
//

public void setChildYScale(int pGraphNum, int pChildNum, double pScale)
{

    if (pGraphNum < 0 || pGraphNum >= graphs.length){ return; }

    graphs[pGraphNum].setChildYScale(pChildNum, pScale);

}// end of Chart::setChildYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setChildOffset
//
// For child pChildNum of Graph pGraphNum, sets the display offset to pOffset.
//

public void setChildOffset(int pGraphNum, int pChildNum, int pOffset)
{

    if (pGraphNum < 0 || pGraphNum >= graphs.length){ return; }

    graphs[pGraphNum].setChildOffset(pChildNum, pOffset);

}// end of Chart::setChildOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setChildBaseLine
//
// For child pChildNum of Graph pGraphNum, sets the baseLine value to pBaseLine.
// This will cause the pBaseline value to be shifted to zero when the child is
// drawn.
//

public void setTraceBaseLine(int pGraphNum, int pTraceNum, int pBaseLine)
{

    if (pGraphNum < 0 || pGraphNum >= graphs.length){ return; }

    graphs[pGraphNum].setChildBaseLine(pTraceNum, pBaseLine);

}// end of Chart::setChildBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setChildConnectPoints
//
// For child pChildNum of TraceGraph pGraphNum, sets the connectPoints flag. If
// true, points will be connected by a line.
//

public void setChildConnectPoints(int pGraphNum, int pChildNum, boolean pValue)
{

    if (pGraphNum < 0 || pGraphNum >= graphs.length){ return; }

    graphs[pGraphNum].setChildConnectPoints(pChildNum, pValue);

}// end of Chart::setChildConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setVerticalBarAllChildren
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all children on all graphs.
//

public void setVerticalBarAllChildren()
{

    for (Graph graph : graphs) { graph.setVerticalBarAllChildren(); }

}// end of Chart::setVerticalBarAllChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::resetAll
//
// Resets all graphs to default starting values.
//

public void resetAll()
{

    prevXGraph0 = -1;

    for (Graph graph: graphs){ graph.resetAll(); }

}// end of Chart::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getNumChildrenForGraph
//
// Returns number of children from Graph pGraphNum.
//

public int getNumChildrenForGraph(int pGraphNum)
{

    if (pGraphNum < 0 || pGraphNum >= graphs.length){ return(0); }

    return(graphs[pGraphNum].getNumChildren());

}// end of Chart::getNumChildrenForGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//

public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList,
                                                           String pObjectType)
{

    if (objectType.equals(pObjectType)){ pObjectList.add(this); }

    for (Graph graph : graphs) {
        graph.scanForGUIObjectsOfAType(pObjectList, pObjectType);
    }

}// end of Chart::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getGraph
//
// Returns a reference to graph pGraph.
//

public Graph getGraph(int pGraph)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return(null); }

    return( graphs[pGraph] );

}// end of Chart::getGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getTrace
//
// Returns the reference to trace pTrace of pGraph.
//

public Trace getTrace(int pGraph, int pTrace)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return(null); }

    return( graphs[pGraph].getTrace(pTrace) );

}// end of Chart::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getIndexOfTracesPeak
//
// Determines and returns which index has the highest peak out of all of the
// traces and returns that index.
//

int debugHss = -1;//DEBUG HSS//
private int getIndexOfTracesPeak(int pIndexStart, int pIndexEnd)
{

    //use the x position that has the highest peak of all the traces to
    //determine which data set to display for this box
    Trace t; Trace peakTrace = null; int peak=-1; int peakX=-1;
    for (Object o : traces) {
        t = (Trace)o;
        int newP = t.getPeak(pIndexStart, pIndexEnd);
        if (newP>peak) { peak=newP; peakTrace=t; }
    }
    if (peakTrace!=null) { peakX = peakTrace.getLastRequestedPeakX(); }

    debugHss = peak; //DEBUG HSS//

    return peakX;

}// end of Chart::getIndexOfTracesPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::markSegmentStart
//
// Instructs children to mark last retrieved data as segment start.
//

public void markSegmentStart()
{
    
    for (Graph c : graphs){ c.markSegmentStart();  }
    
}//end of Chart::markSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::markSegmentEnd
//
// Instructs children to mark last retrieved data as segment end.
//

public void markSegmentEnd()
{
    
    for (Graph c : graphs){ c.markSegmentEnd();  }

}//end of Chart::markSegmentEnd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::isSegmentStarted
//
// Checks to see if a segment has been started and thus may have data which
// needs to be saved.
//

public boolean isSegmentStarted()
{

    for (Graph g : graphs){ if (g.isSegmentStarted()) { return(true);} }

    return(false);

}//end of Chart::isSegmentStarted
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::checkFlagging
//
// Tells all traces to enable or disable flagging depending on the leading and
// trailing masks. 
//
// Will only enable if in INSPECT or INSPECT_WITH_TIMER_DRIVEN_TRACKING mode.
//

private void checkFlagging()
{
    
    if ((sharedSettings.opMode != SharedSettings.INSPECT_MODE 
        && sharedSettings.opMode != SharedSettings.INSPECT_WITH_TIMER_TRACKING_MODE))
        { return; } //bail if not in proper modes or if already started
    
    //DEBUG HSS// //WIP HSS// perform check to ensure distance traveled is past mask
    
    for (Object o : traces) { Trace t = (Trace)o; t.enableFlagging(true); }

}//end of Chart::checkFlagging
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setChartVisible
//
// Sets all of the graphs in this chart visible or hidden.
//

public void setChartVisible(boolean pVisible)
{

    isChartVisible = pVisible;
    for (Graph g : graphs) { g.setVisible(pVisible); }

}//end of Chart::setChartVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::isChartVisible
//
// Returns true if chart visible, false if hidden.
//

public boolean isChartVisible()
{

    return isChartVisible;

}//end of Chart::isChartVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateAnnotationGraph
//
// Plots data added to annoBuffer and/or erases any data which has been
// flagged as erased.
//
// If prevXGraph0Trace0 == -1, then no update is performed as the first time
// through is skipped as the annotation object should not be added until the
// trace as passed the right edge of the space for that object.
//

public void updateAnnotationGraph()
{

    if(!hasZoomGraph){ return; }

    zoomGraph.retrieveDataChanges(); //retrieve data changes from data buffer

    int prevX = graphs[0].getLastDrawnX();
    
    //bail if no changes to last drawn x
    if (prevX == prevXGraph0) { return; }
    
    prevXGraph0 = prevX; //this is the new x
    
    //add a ZoomBox if necessary
    if (prevXGraph0 > zoomGraph.getNextBoxEndX()
            || zoomGraph.getZoomBoxes().size()<=0) 
    {

        int peakIndex = getIndexOfTracesPeak(zoomGraph.getNextBoxStartX(),
                                                zoomGraph.getNextBoxEndX());
        zoomGraph.addZoomBox(peakIndex);
        
        //now that we have a new ZoomBox, make sure that the previous last
        //one is set to only represent zoom data for the x points drawn
        //directly above it -- will no longer show zoom data until the very
        //end of the trace because I new box has been added in and takes over
        //everything after the last previous one
        int prevLastBoxIndex = zoomGraph.getZoomBoxes().size()-2;
        if (prevLastBoxIndex>=0) {
            ZoomBox prevLastBox = zoomGraph.getZoomBoxes().get(prevLastBoxIndex);

            
            int newRepXEnd = prevLastBox.getXEnd();
            prevLastBox.setRepresentedXEnd(newRepXEnd);

            peakIndex = getIndexOfTracesPeak(prevLastBox.getX(),
                                                    prevLastBox.getXEnd());

            zoomGraph.setDataOfZoomBoxToIndex(prevLastBoxIndex, peakIndex);
        }

    } 
    else { //allow last ZoomBox to represent more data points until new one
        
        zoomGraph.setLastZoomBoxToCoverUntilX(prevXGraph0);
        
    }

}// end of Chart::updateAnnotationGraph
//---:getTrac--------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateChild
//
// Plots all new data added to the data transfer buffer and/or erases any data
// which has been flagged as erased for pChildNum of pGraphNum.
//

public void updateChild(int pGraphNum, int pChildNum)
{
        
    graphs[pGraphNum].updateChild(pChildNum);

    scrollTrackingGraphs(pGraphNum);

}// end of Chart::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateChildren
//
// Instructs all children listening for transfer buffer changes to check for
// changes and update.
//

public void updateChildren()
{

    checkFlagging();
    
    for (Graph g : graphs) { 
        g.updateChildren(); 
        scrollTrackingGraphs(g.getGraphNum());
    }

}// end of Chart::updateChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::scrollTrackingGraphs
//
// Scroll any graphs which are set up to track this pGraphNum's scrolling.
//
// Check array for null first to avoid unnecessary processing on empty arrays
//

private void scrollTrackingGraphs(int pGraphNum)
{

    if (graphs[pGraphNum].graphInfo.lastScrollAmount != 0){

        ArrayList<Graph> scrollTrackingGraphs =
                      graphs[pGraphNum].getGraphsTrackingThisGraphsScrolling();

        if (scrollTrackingGraphs != null){

            for (Iterator<Graph> stg=scrollTrackingGraphs.iterator();
                                                             stg.hasNext();) {
                stg.next().scrollGraph(graphs[pGraphNum].
                                                    graphInfo.lastScrollAmount);
            }
        }
    }

    //reset now that tracking graphs scrolled
    graphs[pGraphNum].graphInfo.lastScrollAmount = 0;

}// end of Chart::scrollTrackingGraphs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getThresholds
//
// Returns an array of thresholds.
//

public ArrayList<Threshold[]> getThresholds()
{

    ArrayList<Threshold[]> thresholds = new ArrayList<>();

    for (Graph g : graphs) {
        if (g instanceof TraceGraph) { thresholds.add(g.getThresholds()); }
    }

    return thresholds;

}//end of Chart::getThresholds
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile(IniFile pCalFile)
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum;
    
    setChartVisible(pCalFile.readBoolean(section, "Chart is Visible", true));

    for (Graph g : graphs) { g.loadCalFile(pCalFile); }

}//end of Chart::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

public void saveCalFile(IniFile pCalFile)
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum;

    pCalFile.writeBoolean(section, "Chart is Visible", isChartVisible());

    for (Graph g : graphs) { g.saveCalFile(pCalFile); }

}//end of Chart::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::saveSegment
//

public void saveSegment(BufferedWriter pOut) throws IOException
{

    pOut.write("[Chart]"); pOut.newLine();
    pOut.write("Chart Index=" + chartNum); pOut.newLine();
    pOut.write("Chart Title=" + title); pOut.newLine();
    pOut.write("Chart Short Title=" + shortTitle); pOut.newLine();
    pOut.newLine();
    pOut.write(
            "Note that the Chart Title and Short Title may have been changed");
    pOut.newLine();
    pOut.write(
            "by the user, so the text displayed on the screen may not match");
    pOut.newLine();
    pOut.write("the values shown here.");
    pOut.newLine(); pOut.newLine();

    pOut.write("Chart is Visible=" + isVisible()); //save visibility flag
    pOut.newLine(); pOut.newLine();

    // tell each graph to save its data
    for (Graph g : graphs) { g.saveSegment(pOut); }

}//end of Chart::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::loadSegment
//
// Loads segments' data from pFile.
//

public String loadSegment(BufferedReader pIn, String pLastLine)
        throws IOException
{

    //handle entries for the chart itself
    String line = processChartEntries(pIn, pLastLine);

    if(line.startsWith("Alert")) { return line; }

    // tell each graph to load data
    for (Graph g : graphs) { line = g.loadSegment(pIn, line); }
    
    //DEBUG HSS//  test code to set width to first graph
    graphWidth = graphs[0].getGraphWidth();
    for (Graph g : graphs) { g.updateDimensions(graphWidth, g.getGraphHeight()); }
    if (infoPanel!=null) { 
        infoPanel.updateDimensions(graphWidth, infoPanel.getHeight()); 
    }
    //DEBUG HSS// end

    // draw all necessary zoomboxes if a zoomgraph is there
    if (zoomGraph==null) { return line; }
    int xStop = zoomGraph.getDataSize();
    for (int i=0; i<=xStop; i++) {

        //continue if not time to add new zoom box
        if (i%zoomGraph.getNextBoxEndX() != 0||i==0) { continue; }

        int peakIndex = getIndexOfTracesPeak(zoomGraph.getNextBoxStartX(),
                                                zoomGraph.getNextBoxEndX());
        zoomGraph.addZoomBox(peakIndex);

    }

    return line;

}//end of Chart::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// StripChart::processChartEntries
//
// Processes the entries for the chart itself via pIn.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//
// For the StripChart section, the [Chart] tag may or may not have already been
// read from the file by the code handling the previous section.  If it has
// been read, the line containing the tag should be passed in via pLastLine.
//

private String processChartEntries(BufferedReader pIn, String pLastLine)
        throws IOException
{

    String line;
    boolean success = false;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //if pLastLine contains the [Chart] tag, then skip ahead else read until
    //end of file reached or "[Chart]" section tag reached

    if (Tools.matchAndParseString(pLastLine, "[Chart]", "",  matchSet)) {
        success = true;
    } //tag already found
    else {
        while ((line = pIn.readLine()) != null){  //search for tag
            if (Tools.matchAndParseString(line, "[Chart]", "",  matchSet)){
                success = true; break;
            }
        }//while
    }//else

    if (!success) {
        return(
            "Alert: Section not found for Chart Group "
                                       + chartGroupNum + " Chart " + chartNum);
    }

    //set defaults
    int chartIndexRead = -1;
    String titleRead = "", shortTitleRead = "";
    boolean visibleRead = true;

    //scan the first part of the section and parse its entries
    //these entries apply to the chart group itself

    success = false;
    while ((line = pIn.readLine()) != null){

        //stop when next section tag reached (will start with [)
        if (Tools.matchAndParseString(line, "[", "",  matchSet)){
            success = true; break;
        }

        //read the "Chart Index" entry - if not found, default to -1
        if (Tools.matchAndParseInt(line, "Chart Index", -1,  matchSet)) {
            chartIndexRead = matchSet.rInt1;
        }

        //read the "Chart Title" entry - if not found, default to ""
        if (Tools.matchAndParseString(line, "Chart Title", "",  matchSet)) {
            titleRead = matchSet.rString1;
        }

        //read the "Chart Short Title" entry - if not found, default to ""
        if (Tools.matchAndParseString(
                                   line, "Chart Short Title", "",  matchSet)) {
            shortTitleRead = matchSet.rString1;
        }

        //read the "Chart is Visible" entry - if not found, default to true
        if (Tools.matchAndParseBoolean(
                                  line, "Chart is Visible", true,  matchSet)) {
            visibleRead = matchSet.rBoolean1;
        }

    }// while ((line = pIn.readLine()) != null)

    //apply settings
    title = titleRead;
    shortTitle = shortTitleRead;
    setVisible(visibleRead);

    if (!success) {
        throw new IOException(
        "The file could not be read - missing end of section for Chart Group "
                                         + chartGroupNum + " Chart " + chartNum);
    }

    //if the index number in the file does not match the index number for this
    //strip chart, abort the file read

    if (chartIndexRead != chartNum) {
        throw new IOException(
            "The file could not be read - section not found or out of order "
                + "for Chart Group " + chartGroupNum + " Chart " + chartNum);
    }

    return line; //should be "[xxxx]" tag on success, unknown value if not

}//end of StripChart::processChartEntries
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::mouseMoved
//
// Called when the mouse has moved on any object that this class has been
// assigned as a listener.
//

@Override
public void mouseMoved(MouseEvent pEvent) {

    int x = pEvent.getX(); int y = pEvent.getY();

    //only take action if mouse movement was on a TraceGraph
    if (pEvent.getComponent() instanceof TraceGraph && zoomGraph!=null) {
        TraceGraph g = (TraceGraph)pEvent.getComponent();

        //determine the x at which to display data
        int dataX = g.getXOfPeakInBox(x, y, 10, 10); //WIP HSS// read width and height from ini file
        if (dataX!=-1) { zoomGraph.updateZoomBox(dataX); }
    }

}// end of Chart::mouseMoved
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::mouseExited
//
// Called when the mouse has exited any object that this class has been
// assigned as a listener.
//

@Override
public void mouseExited(MouseEvent pEvent) {

    //only take action if there is a zoomgraph
    if (zoomGraph!=null) { zoomGraph.resetLastUpdatedZoomBox(); }

}// end of Chart::mouseExited
//-----------------------------------------------------------------------------

//Required to be overriden for interface MouseMotionListener
@Override
public void mouseDragged(MouseEvent me) {}

//Required to be overriden for interface MouseListener
@Override
public void mouseClicked(MouseEvent me) {
}

@Override
public void mousePressed(MouseEvent me) {
}

@Override
public void mouseReleased(MouseEvent me) {
}

@Override
public void mouseEntered(MouseEvent me) {
}


}//end of class Chart
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChartComponentAdapter
//
// Responds to component actions on the Chart.
//
// NOTE: this functionality is not currently used and was not fully tested.
// It might make more sense for this to be moved to ChartGroup class so that
// class can also respond to size changes.
//
// See notes at the top of ChartGroup class titled "Display Sizing" for
// important info about setting the size of the charts.
//

class ChartComponentAdapter extends ComponentAdapter{

Chart owner;

public ChartComponentAdapter(Chart pOwner){ owner = pOwner; }

@Override
public void componentResized(ComponentEvent e){

//    owner.updateDimensions();

}

}//end of class ChartComponentAdapter
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

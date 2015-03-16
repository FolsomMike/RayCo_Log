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
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Chart
//

class Chart extends JPanel{

    private final IniFile configFile;
    
    private String title, shortTitle, objectType;
    private final int chartGroupNum;
    public int getChartGroupNum(){ return(chartGroupNum); }    
    private final int chartNum;
    public int getChartNum(){ return(chartNum); }    
    private int graphWidth, graphHeight;    
    int numGraphs;
    boolean hasZoomGraph = false;
    boolean hasInfoPanel;
    int prevXGraph0Trace0;

    boolean graphsVisible;
    boolean specifiedGraphsVisible;
    public boolean getSpecifiedGraphsVisible(){return(specifiedGraphsVisible);}
    
    ChartInfo chartInfo = new ChartInfo();
    
    private Graph graphs[];
    private ZoomGraph zoomGraph;
    private ChartInfoPanel infoPanel;
    
    ActionListener parentActionListener;

//-----------------------------------------------------------------------------
// Chart::Chart (constructor)
//
//

public Chart(int pChartGroupNum, int pChartNum, int pDefaultGraphWidth,
              int pDefaultGraphHeight, ActionListener pParentActionListener,
                                                           IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    configFile = pConfigFile;
    parentActionListener = pParentActionListener;
    
    graphWidth = pDefaultGraphWidth;
    graphHeight = pDefaultGraphHeight;
        
}//end of Chart::Chart (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    loadConfigSettings();

    setBorder(BorderFactory.createTitledBorder(title));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));    
    
    addGraphs();

    if(hasInfoPanel){ addInfoPanel(); }

    setGraphsVisible(graphsVisible);
    
}// end of Chart::init
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
                               graphWidth, graphHeight, chartInfo, configFile);
            graphs[i].init();
            add(graphs[i]);
            addSeparatorPanelSpecifiedInConfigFile(i);
            
        } else if (graphType == Graph.ZOOM_GRAPH){

            graphs[i] = new ZoomGraph(chartGroupNum, chartNum, i,
                               graphWidth, graphHeight, chartInfo, configFile);
            graphs[i].init();
            add(graphs[i]);
            addSeparatorPanelSpecifiedInConfigFile(i);
            hasZoomGraph = true;
            zoomGraph = (ZoomGraph)graphs[i]; //convenience reference
                        
        } else if (graphType == Graph.MAP3D_GRAPH){

            graphs[i] = new Map3DGraph(chartGroupNum, chartNum, i,
                               graphWidth, graphHeight, chartInfo, configFile);
            graphs[i].init();
            add(graphs[i]);
            addSeparatorPanelSpecifiedInConfigFile(i);
            
        }
        
    }

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

    prevXGraph0Trace0 = -1;
    
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
    
    int prevX = graphs[0].getTrace(0).getPrevX();    
    
    if (prevXGraph0Trace0 == -1){ 
        prevXGraph0Trace0 = prevX;
        return;
    }
    
    if (prevX != prevXGraph0Trace0 && (prevX % 104 == 0)){    
        prevXGraph0Trace0 = prevX;
        zoomGraph.addZoomBox(0, simulateZoomGraph());
    }
    
}// end of Chart::updateAnnotationGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::updateChild
//
// Plots all data added to the data transfer buffer and/or erases any data
// which has been flagged as erased for pChildNum of pGraphNum.
//

public void updateChild(int pGraphNum, int pChildNum)
{

    graphs[pGraphNum].updateChild(pChildNum);

    //scroll any graphs which are set up to track this graph's scrolling
    //check array for null first to avoid unnecessary processing on empty arrays
    
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

    graphs[pGraphNum].graphInfo.lastScrollAmount = 0;

}// end of Chart::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ???::simulateZoomGraph
//
// Creates a simulated data stream representing a high resolution graph of
// an indication.
//
// debug mks -- move this to a simulator class
//

private int[] simulateZoomGraph()
{
        
    int data[] = new int[100];
    
    for(int i=0; i<data.length; i++){
        data[i] = (int)(5 * Math.random());
    }

    int spikeLoc = (int)(40 + 20 * Math.random());
    
    data[spikeLoc-2] = 20 + (int)(5 * Math.random());
    data[spikeLoc] = - 20 - (int)(5 * Math.random());
    data[spikeLoc+2] = 20 + (int)(5 * Math.random());
    
    return(data);
    
}// end of ???::simulateZoomGraph
//-----------------------------------------------------------------------------


}//end of class Chart
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

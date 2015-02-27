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

import controller.GUIDataSet;
import java.awt.*;
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Chart
//

class Chart extends JPanel{

    private IniFile configFile;
    
    private String title, shortTitle;
    private int chartGroupNum, chartNum;
    private int graphWidth, graphHeight;    
    int numGraphs;
    boolean hasAnnotationGraph;
    boolean hasInfoPanel;
 
    private Graph graphs[];
    private ZoomGraph zoomGraph;
    private ChartInfoPanel infoPanel;
    
    private int graphPtr;

//-----------------------------------------------------------------------------
// Chart::Chart (constructor)
//
//

public Chart()
{

}//end of Chart::Chart (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pTitle is the text title for the graph.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init(int pChartGroupNum, int pChartNum, int pDefaultGraphWidth,
        int pDefaultGraphHeight, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    configFile = pConfigFile;
    
    graphWidth = pDefaultGraphWidth;
    graphHeight = pDefaultGraphHeight;

    loadConfigSettings();

    setBorder(BorderFactory.createTitledBorder(title));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));    
    
    addGraphs();
    
    if (hasAnnotationGraph){ addAnnotationGraph(); }
    
    if(hasInfoPanel){ addInfoPanel(); }

}// end of Chart::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addGraphs
//
// Adds basic graphs to the chart.
//

private void addGraphs()
{
    
    graphs = new Graph[numGraphs];
    
    for (int i = 0; i<numGraphs; i++){
        graphs[i] = new Graph(); //the traces are drawn on this panel
        graphs[i].init(chartGroupNum, chartNum, i,
                                    graphWidth, graphHeight, configFile);
        add(graphs[i]);
        if(i<numGraphs-1){ addGraphSeparatorPanel(); }
    }

}// end of Chart::addGraphs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addAnnotationGraph
//
// Adds an annotation graph to the chart.
//

private void addAnnotationGraph()
{

    zoomGraph = new ZoomGraph();
    zoomGraph.init(chartGroupNum, chartNum, 0,
                                       graphWidth, graphHeight, configFile);
    addGraphSeparatorPanel();
    add(zoomGraph);

}// end of Chart::addAnnotationGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addInfoPanel
//
// Adds an information panel to the chart for use in explaining color keys,
// displaying monitoring info, etc.
//

private void addInfoPanel()
{

    infoPanel = new ChartInfoPanel();
    infoPanel.init(chartGroupNum, chartNum, graphWidth, 15);
    add(infoPanel);

    //add a color key for each trace
    
    for(Graph graph: graphs){     
        for(int i=0; i<graph.getNumTraces(); i++){        
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
    
    numGraphs = configFile.readInt(section, "number of graphs", 0);
    
    hasAnnotationGraph = configFile.readBoolean(
                                       section, "has annotation graph", false);

    hasInfoPanel = configFile.readBoolean(section, "has info panel", false);

    int configWidth = configFile.readInt(
                                   section, "default width for all graphs", 0);

    if (configWidth > 0) graphWidth = configWidth; //override if > 0
    
    int configHeight = configFile.readInt(
                                  section, "default height for all graphs", 0);

    if (configHeight > 0) graphHeight = configHeight; //override if > 0
    
}// end of Chart::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::addGraphSeparatorPanel
//
// Adds a panel mean to separate two graphs. This version contains a simple
// line with specified color and thickness.
//

public void addGraphSeparatorPanel()
{

    SeparatorPanel spanel = new SeparatorPanel();
    
    spanel.init(graphWidth, 1, Color.LIGHT_GRAY , 1);
    
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

    for(int i=0; i<numGraphs; i++){
        graphs[i].paintTraces(pG2);
    }

}// end of Chart::paintTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setAllTraceXScale
//
// Sets the display horizontal scale for all traces of all graphs to pScale.
//

public void setAllTraceXScale(double pScale)
{
    
    for (Graph graph : graphs) { graph.setAllTraceXScale(pScale); }

}// end of Chart::setAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceYScale
//
// For Trace pTrace of Graph pGraph, sets the display vertical scale for
// to pScale
//

public void setTraceYScale(int pGraph, int pTrace, double pScale)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return; }

    graphs[pGraph].setTraceYScale(pTrace, pScale);

}// end of Chart::setTraceYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceOffset
//
// For Trace pTrace of Graph pGraph, sets the display offset for Trace pTrace
// to pOffset.
//

public void setTraceOffset(int pGraph, int pTrace, int pOffset)
{
    
    if (pGraph < 0 || pGraph >= graphs.length){ return; }

    graphs[pGraph].setTraceOffset(pTrace, pOffset);

}// end of Chart::setTraceOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceBaseLine
//
// For Trace pTrace of Graph pGraph, sets the baseLine value to pBaseLine.
// This will cause the pBaseline value to be shifted to zero when the trace is
// drawn.
//

public void setTraceBaseLine(int pGraph, int pTrace, int pBaseLine)
{
    
    if (pGraph < 0 || pGraph >= graphs.length){ return; }

    graphs[pGraph].setTraceBaseLine(pTrace, pBaseLine);

}// end of Chart::setTraceBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceConnectPoints
//
// For Trace pTrace of Graph pGraph, sets the connectPoints flag. If true,
// points will be connected by a line.
//

public void setTraceConnectPoints(int pGraph, int pTrace, boolean pValue)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return; }

    graphs[pGraph].setTraceConnectPoints(pTrace, pValue);
    
}// end of Chart::setTraceConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setVerticalBarAllTraces
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces on all graphs.
//

public void setVerticalBarAllTraces()
{

    for (Graph graph : graphs) { graph.setVerticalBarAllTraces(); }

}// end of Chart::setVerticalBarAllTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::resetAllTraceData
//
// For all traces of all graphs resets all data to zero and all flags to
// DEFAULT_FLAGS. Resets dataInsertPos to zero.
//

public void resetAllTraceData()
{

    for (Graph graph: graphs){        
            graph.resetAllTraceData();
        }

}// end of Chart::resetAllTraceData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getNumTracesForGraph
//
// Returns numTraces from Graph pGraph.
//

public int getNumTraces(int pGraph)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return(0); }
    
    return(graphs[pGraph].getNumTraces());

}// end of Chart::getNumTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::initForGUIChildrenScan
//
// Prepares for iteration through all GUI child objects.
//

public void initForGUIChildrenScan()
{
    
    graphPtr = 0;
    
    for (Graph graph : graphs) { graph.initForGUIChildrenScan(); }

}// end of Chart::initForGUIChildrenScan
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getNextGUIChild
//
// Returns the index of the next GUI child object in the scan order in the
// appropriate variable in guiDataSet.
//
// Returns 0 if a valid child other than the last is being returned, -1 if
// not valid children are available, and 1 if the last child is being returned.
//
// If the variable in guiDataSet for the next child layer is not the RESET
// value, then the index for the next child object is returned as well.
//
// This method can be used to iterate through all subsequent layers of child
// objects by setting all the index number variables in guiDataSet to any
// value other than RESET.
//

public int getNextGUIChild(GUIDataSet pGuiDataSet)
{
    
    int status;

    if(graphPtr >= graphs.length){ 
        pGuiDataSet.graphNum = -1;
        return(-1);
    }else if (graphPtr == graphs.length - 1){
        status = 1;
        pGuiDataSet.graphNum = graphPtr;
    }else{
        status = 0;
        pGuiDataSet.graphNum = graphPtr;
    }
    
    if(pGuiDataSet.traceNum == GUIDataSet.RESET){        
        //don't scan deeper layer of children, move to the next child        
        graphPtr++;
        return(status);
    }else{     
        // scan the next layer of children as well, only moving to the next
        // local child when all next layer children have been scanned
        
        int grandChildStatus = 
                    graphs[graphPtr].getNextGUIChild(pGuiDataSet);
        //if last child's last child returned, move to next child for next call
        if (grandChildStatus == 1){ graphPtr++; }
        //if last grandchild of last child, flag so parent moves to next child 
        if (grandChildStatus == 1 && status == 1){ return(status);}
        else{ return(0); }
    }
    
}// end of Chart::getNextGUIChild
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
// Chart::updateTrace
//
// Plots all data added to dataBuffer and erases any data which has been
// marked as erased for pTrace of pGraph.
//

public void updateTrace(int pGraph, int pTrace)
{

    graphs[pGraph].updateTrace(pTrace);

}// end of Chart::updateTrace
//-----------------------------------------------------------------------------

}//end of class Chart
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

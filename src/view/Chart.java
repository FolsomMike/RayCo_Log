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
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Chart
//

class Chart extends JPanel{

    private IniFile configFile;
    
    private String title, shortTitle;
    private int chartGroupIndex, index;
    private int graphWidth, graphHeight;    
    int numGraphs;
    boolean hasAnnotationGraph;
 
    private Graph graphs[];
    private ZoomGraph zoomGraph;


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

public void init(int pChartGroupIndex, int pIndex, int pDefaultGraphWidth,
        int pDefaultGraphHeight, IniFile pConfigFile)
{

    chartGroupIndex = pChartGroupIndex; index = pIndex;
    configFile = pConfigFile;
    
    graphWidth = pDefaultGraphWidth;
    graphHeight = pDefaultGraphHeight;

    loadConfigSettings();

    setBorder(BorderFactory.createTitledBorder(title));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));    
    
    graphs = new Graph[numGraphs];
    
    for (int i = 0; i<numGraphs; i++){
        graphs[i] = new Graph(); //the traces are drawn on this panel
        graphs[i].init(chartGroupIndex, index, i,
                                    graphWidth, graphHeight, configFile);
        add(graphs[i]);
        if(i<numGraphs-1){ addGraphSeparatorPanel(); }
    }
    
    if (hasAnnotationGraph){
        zoomGraph = new ZoomGraph();
        zoomGraph.init(chartGroupIndex, index, 0,
                                           graphWidth, graphHeight, configFile);
        addGraphSeparatorPanel();
        add(zoomGraph);
    }

}// end of Chart::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupIndex + " Chart " + index;

    title = configFile.readString(section, "title", "Chart " + (index + 1));

    shortTitle = configFile.readString(
                                section, "short title", "chart" + (index + 1));    
    
    numGraphs = configFile.readInt(section, "number of graphs", 0);
    
    hasAnnotationGraph = configFile.readBoolean(
                                       section, "has annotation graph", false);
    
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
// Chart::insertDataPointInTrace
//
// Stores pData in Trace pTrace of Graph pGraph.
//

public void insertDataPointInTrace(int pGraph, int pTrace, int pData)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return; }

    graphs[pGraph].insertDataPointInTrace(pTrace, pData);

}// end of Chart::insertDataPointInTrace
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
// Chart::setTraceFlags
//
// For Trace pTrace of Graph pGraph, OR's pFlags of Trace pTrace with
// flags[pIndex] to set one or more flag bits in the flags array at the
// specified position pIndex.
//

public void setTraceFlags(int pGraph, int pTrace, int pIndex, int pFlags)
{

   if (pGraph < 0 || pGraph >= graphs.length){ return; }

   graphs[pGraph].setTraceFlags(pTrace, pIndex, pFlags);

}// end of Chart::setTraceFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceFlagsAtCurrentInsertionPoint
//
// For Trace pTrace of Graph pGraph, OR's pFlags with 
// flags[<current insertion point>] to set one or more flag bits in the flags
// array at the current data insertionPoint.
//

public void setTraceFlagsAtCurrentInsertionPoint(int pGraph, int pTrace,
                                                                    int pFlags)
{

   if (pGraph < 0 || pGraph >= graphs.length){ return; }

   graphs[pGraph].setTraceFlagsAtCurrentInsertionPoint(pTrace, pFlags);

}// end of Chart::setTraceFlagsAtCurrentInsertionPoint
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
// Chart::getTrace
//
// Returns Trace pTrace of Graph pGraph.
//

public Trace getTrace(int pGraph, int pTrace)
{

    if (pGraph < 0 || pGraph >= graphs.length){ return(null); }

    return(graphs[pGraph].getTrace(pTrace));

}// end of Chart::getTrace
//-----------------------------------------------------------------------------

}//end of class Chart
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

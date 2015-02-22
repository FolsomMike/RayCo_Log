/******************************************************************************
* Title: Graph.java
* Author: Mike Schoonover
* Date: 01/14/15
*
* Purpose:
*
* This class subclasses a JPanel to represent a graph with traces.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import controller.GUIDataSet;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Graph
//

public class Graph extends JPanel{

    private IniFile configFile;
    
    private int numTraces;
    private Trace[] traces;

    private String title, shortTitle;
    private int chartGroupNum, chartNum, graphNum;
    private int width, height;

    private int tracePtr;
    
//-----------------------------------------------------------------------------
// Graph::Graph (constructor)
//

public Graph()
{
    
}//end of Graph::Graph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::init
//

public void init(int pChartGroupNum, int pChartNum, int pGraphNum,
                                  int pWidth, int pHeight, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; 
    chartNum = pChartNum; graphNum = pGraphNum; 
    width = pWidth; height = pHeight;
    configFile = pConfigFile;

    loadConfigSettings();
    
    setSizes(this, width, height);
    
    createTraces();
    
}// end of Graph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                                        + " Graph " + graphNum;

    title = configFile.readString(section, "title", "Graph " + (graphNum + 1));

    shortTitle = configFile.readString(
                             section, "short title", "graph" + (graphNum + 1));
        
    numTraces = configFile.readInt(section, "number of traces", 0);
        
    int configWidth = configFile.readInt(section, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0
    
    int configHeight = configFile.readInt(section, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0
    
}// end of Chart::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::createTraces
//
// Creates and sets up the traces.
//

private void createTraces()
{

    traces = new Trace[numTraces];

    for(int i=0; i<traces.length; i++){

        traces[i] = new Trace();
        traces[i].init(chartGroupNum, chartNum, graphNum, i,
                                                    width, height, configFile);
    }

}//end of Graph::createTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    Graphics2D g2 = (Graphics2D) g;

    paintTraces(g2);

}// end of Graph::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::paintTraces
//
// Paints all the traces on the canvas.
//

public void paintTraces(Graphics2D pG2)
{
    
    for (Trace trace : traces) { trace.paintTrace(pG2); }

}// end of Graph::paintTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::paintSingleTraceDataPoint
//
// Draws line from data point specified by pIndex to the next point in the
// buffer for pTrace.
//

public void paintSingleTraceDataPoint(int pTrace, int pIndex)
{

    traces[pTrace].paintTrace((Graphics2D) getGraphics());

}// end of Graph::paintSingleTraceDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::paintLastTraceDataPoint
//
// Draws line from data point before the last inserted to the last inserted
// data point in the buffer for pTrace.
//

public void paintLastTraceDataPoint(int pTrace)
{
    
    traces[pTrace].paintLastTraceDataPoint((Graphics2D) getGraphics());

}// end of Graph::paintLastTraceDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getTrace
//
// Returns Trace pTrace.
//

public Trace getTrace(int pTrace)
{

    if (pTrace < 0 || pTrace >= traces.length){ return(null); }

    return(traces[pTrace]);

}// end of Graph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getNumTraces
//
// Returns numTraces.
//

public int getNumTraces()
{

    return(numTraces);

}// end of Graph::getNumTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::resetAllTraceData
//
// For all traces resets all data to zero and all flags to DEFAULT_FLAGS.
// Resets dataInsertPos to zero.
//

public void resetAllTraceData()
{
    
    for (Trace trace : traces) { trace.resetData(); }

}// end of Graph::resetAllTraceData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setVerticalBarAllTraces
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces.
//

public void setVerticalBarAllTraces()
{

    for (Trace trace : traces) {
        trace.setFlags(trace.getDataInsertPos(), Trace.VERTICAL_BAR);
    }

}// end of Graph::setVerticalBarAllTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setTraceFlagsAtCurrentInsertionPoint
//
// For Trace pTrace, OR's pFlags with flags[<current insertion point>] to set
// one or more flag bits in the flags array at the current data insertionPoint.
//

public void setTraceFlagsAtCurrentInsertionPoint(int pTrace, int pFlags)
{

   if (pTrace < 0 || pTrace >= traces.length){ return; }

   traces[pTrace].setFlagsAtCurrentInsertionPoint(pFlags);

}// end of Graph::setTraceFlagsAtCurrentInsertionPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceFlags
//
// For Trace pTrace, OR's pFlags of Trace pTrace with flags[pIndex] to set one
// or more flag bits in the flags array at the specified position pIndex.
//

public void setTraceFlags(int pTrace, int pIndex, int pFlags)
{

   if (pTrace < 0 || pTrace >= traces.length){ return; }

   traces[pTrace].setFlags(pIndex, pFlags);

}// end of Chart::setTraceFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setTraceConnectPoints
//
// For Trace pTrace, sets the connectPoints flag. If true,
// points will be connected by a line.
//

public void setTraceConnectPoints(int pTrace, boolean pValue)
{

    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setConnectPoints(pValue);
    
}// end of Graph::setTraceConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setTraceYScale
//
// For Trace pTrace, sets the display vertical scale to pScale
//

public void setTraceYScale(int pTrace, double pScale)
{

    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setYScale(pScale);

}// end of Graph::setTraceYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setTraceOffset
//
// For Trace pTrace, sets the display offset to pOffset.
//

public void setTraceOffset(int pTrace, int pOffset)
{
    
    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setOffset(pOffset);

}// end of Graph::setTraceOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setTraceBaseLine
//
// For Trace pTrace, sets the baseLine value for to pBaseLine. This will cause
// the pBaseline value to be shifted to zero when the trace is drawn.
//

public void setTraceBaseLine(int pTrace, int pBaseLine)
{
    
    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setBaseLine(pBaseLine);

}// end of Graph::setTraceBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setAllTraceXScale
//
// Sets the display horizontal scale for all traces to pScale.
//

public void setAllTraceXScale(double pScale)
{
    
    for (Trace trace : traces) { trace.setXScale(pScale); }

}// end of Graph::setAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::insertDataPointInTrace
//
// Stores pData in Trace pTrace.
//

public void insertDataPointInTrace(int pTrace, int pData)
{

    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].insertDataPoint(pData);

}// end of Graph::insertDataPointInTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::initForGUIChildrenScan
//
// Prepares for iteration through all traces.
//

public void initForGUIChildrenScan()
{
    
    tracePtr = 0;

}// end of Chart::initForGUIChildrenScan
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getNextGUIChild
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

    if(tracePtr >= traces.length){
        //no more children
        pGuiDataSet.traceNum = -1;
        return(-1);
    }else if (tracePtr == traces.length - 1){
        //this is the last child
        status = 1;
        pGuiDataSet.traceNum = tracePtr;
    }else{
        //this is a valid child but not the last one
        status = 0;
        pGuiDataSet.traceNum = tracePtr;
    }
    
    tracePtr++;

    return(status);
        
}// end of Graph::getNextGUIChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getTrace
//
// Returns the reference to trace pTrace.
//

public Trace getTrace(int pGraph, int pTrace)
{

    if (pTrace < 0 || pTrace >= traces.length){ return(null); }            
    
    return( traces[pTrace] );
    
}// end of Graph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of Graph::setSizes
//-----------------------------------------------------------------------------


}//end of class Graph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: Simple.java
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import model.DataTransferIntBuffer;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SimpleGraph
//

public class SimpleGraph extends Graph{
    
    private int numTraces;
    private Trace[] traces;

    Color gridColor;
    int gridXSpacing = 10;
    int gridYSpacing = 10;
    private boolean drawGridBaseline;

    private int tracePtr;

    private boolean invertGraph;
    
//-----------------------------------------------------------------------------
// SimpleGraph::SimpleGraph (constructor)
//

public SimpleGraph()
{
    
}//end of SimpleGraph::SimpleGraph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::init
//

@Override
public void init(int pChartGroupNum, int pChartNum, int pGraphNum,
                                  int pWidth, int pHeight, IniFile pConfigFile)
{

    super.init(pChartGroupNum, pChartNum, pGraphNum,
                                               pWidth,   pHeight, pConfigFile);
        
    setOpaque(true);
    setBackground(backgroundColor);    
    createTraces();
    
}// end of SimpleGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    configFileSection = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                                        + " Graph " + graphNum;

    super.loadConfigSettings();
    
    numTraces = configFile.readInt(configFileSection, "number of traces", 0);
    
    gridColor = configFile.readColor(
                                 configFileSection, "grid color", Color.BLACK);
    
    drawGridBaseline = configFile.readBoolean(
                               configFileSection, "draw grid baseline", false);
    
    invertGraph = configFile.readBoolean(
                                      configFileSection, "invert graph", true);
    
    int numVerGridDivisions = configFile.readInt(
                   configFileSection, "number of vertical grid divisions", 10);
    
    gridYSpacing = height / numVerGridDivisions;
    
}// end of Chart::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::createTraces
//
// Creates and sets up the traces.
//

private void createTraces()
{

    traces = new Trace[numTraces];

    for(int i=0; i<traces.length; i++){

        traces[i] = new Trace();
        traces[i].init(chartGroupNum, chartNum, graphNum, i, width, height,
            backgroundColor, drawGridBaseline, gridColor, gridXSpacing,
            gridYSpacing, configFile);
    }

}//end of SimpleGraph::createTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);
    
    Graphics2D g2 = (Graphics2D) g;

    if (drawGridBaseline){
        int y;
        if(invertGraph) { y=getHeight()-1; } else { y=0; }
        g2.setColor(gridColor);
        g2.drawLine(0, y, width-1, y);
    }

    paintTraces(g2);

}// end of SimpleGraph::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::paintTraces
//
// Paints all the traces on the canvas.
//

public void paintTraces(Graphics2D pG2)
{
    
    for (Trace trace : traces) { trace.paintTrace(pG2); }

}// end of SimpleGraph::paintTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::paintSingleTraceDataPoint
//
// Draws line from data point specified by pIndex to the next point in the
// buffer for pTrace.
//

public void paintSingleTraceDataPoint(int pTrace, int pIndex)
{

    traces[pTrace].paintTrace((Graphics2D) getGraphics());

}// end of SimpleGraph::paintSingleTraceDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::updateTrace
//
// Plots all data added to dataBuffer and erases any data which has been
// marked as erased for pTrace.
//

public void updateTrace(int pTrace)
{
    
    traces[pTrace].updateTrace((Graphics2D) getGraphics());

}// end of SimpleGraph::updateTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::getTrace
//
// Returns Trace pTrace.
//

public Trace getTrace(int pTrace)
{

    if (pTrace < 0 || pTrace >= traces.length){ return(null); }

    return(traces[pTrace]);

}// end of SimpleGraph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::getNumTraces
//
// Returns numTraces.
//

public int getNumTraces()
{

    return(numTraces);

}// end of SimpleGraph::getNumTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::resetAllTraceData
//
// For all traces resets all data to zero and all flags to DEFAULT_FLAGS.
// Resets dataInsertPos to zero.
//

public void resetAllTraceData()
{
    
    for (Trace trace : traces) { trace.resetData(); }

}// end of SimpleGraph::resetAllTraceData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::setVerticalBarAllTraces
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces.
//

public void setVerticalBarAllTraces()
{

    for (Trace trace : traces) {
        trace.getDataBuffer().setFlagsAtCurrentInsertionPoint(
                                           DataTransferIntBuffer.VERTICAL_BAR);
    }

}// end of SimpleGraph::setVerticalBarAllTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::setTraceConnectPoints
//
// For Trace pTrace, sets the connectPoints flag. If true,
// points will be connected by a line.
//

public void setTraceConnectPoints(int pTrace, boolean pValue)
{

    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setConnectPoints(pValue);
    
}// end of SimpleGraph::setTraceConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::setTraceYScale
//
// For Trace pTrace, sets the display vertical scale to pScale
//

public void setTraceYScale(int pTrace, double pScale)
{

    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setYScale(pScale);

}// end of SimpleGraph::setTraceYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::setTraceOffset
//
// For Trace pTrace, sets the display offset to pOffset.
//

public void setTraceOffset(int pTrace, int pOffset)
{
    
    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setOffset(pOffset);

}// end of SimpleGraph::setTraceOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::setTraceBaseLine
//
// For Trace pTrace, sets the baseLine value for to pBaseLine. This will cause
// the pBaseline value to be shifted to zero when the trace is drawn.
//

public void setTraceBaseLine(int pTrace, int pBaseLine)
{
    
    if (pTrace < 0 || pTrace >= traces.length){ return; }

    traces[pTrace].setBaseLine(pBaseLine);

}// end of SimpleGraph::setTraceBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::setAllTraceXScale
//
// Sets the display horizontal scale for all traces to pScale.
//

public void setAllTraceXScale(double pScale)
{
    
    for (Trace trace : traces) { trace.setXScale(pScale); }

}// end of SimpleGraph::setAllTraceXScale
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
// SimpleGraph::getNextGUIChild
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
        
}// end of SimpleGraph::getNextGUIChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimpleGraph::getTrace
//
// Returns the reference to trace pTrace.
//

public Trace getTrace(int pGraph, int pTrace)
{

    if (pTrace < 0 || pTrace >= traces.length){ return(null); }            
    
    return( traces[pTrace] );
    
}// end of SimpleGraph::getTrace
//-----------------------------------------------------------------------------

}//end of class SimpleGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

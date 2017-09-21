/******************************************************************************
* Title: TraceGraph.java
* Author: Mike Schoonover
* Date: 01/14/15
*
* Purpose:
*
* This class subclasses Graph to represent a graph with traces.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import model.DataTransferIntBuffer;
import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class TraceGraph
//

public class TraceGraph extends Graph{

    private int numTraces;
    private Trace[] traces;

    Color gridColor;
    int gridXSpacing = 10;
    int gridYSpacing = 10;
    private boolean drawGridBaseline;

    private boolean invertGraph;

//-----------------------------------------------------------------------------
// TraceGraph::TraceGraph (constructor)
//

public TraceGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
                    int pWidth, int pHeight, ChartInfo pChartInfo,
                    IniFile pConfigFile, SharedSettings pSettings)
{

    super(pChartGroupNum, pChartNum, pGraphNum, pWidth, pHeight, pChartInfo,
                pConfigFile, pSettings);

}//end of TraceGraph::TraceGraph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::init
//

@Override
public void init()
{

    super.init();

    setOpaque(true);
    setBackground(backgroundColor);
    addThresholds();
    addTraces();

}// end of TraceGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//

@Override
public void updateDimensions()
{

    super.updateDimensions();

    for (Trace trace : traces) {
        trace.updateDimensions(getHeight(), getWidth());
    }

}// end of TraceGraph::updateDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::loadConfigSettings
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

    numThresholds = configFile.readInt(configFileSection,
                                            "number of thresholds", 0);

    gridColor = configFile.readColor(
                                 configFileSection, "grid color", Color.BLACK);

    drawGridBaseline = configFile.readBoolean(
                               configFileSection, "draw grid baseline", false);

    invertGraph = configFile.readBoolean(
                                      configFileSection, "invert graph", true);

    int numVerGridDivisions = configFile.readInt(
                   configFileSection, "number of vertical grid divisions", 10);

    gridYSpacing = height / numVerGridDivisions;

}// end of TraceGraph::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

@Override
public void loadCalFile(IniFile pCalFile)
{

    for (Threshold t : thresholds) { t.loadCalFile(pCalFile); }

}//end of TraceGraph::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

@Override
public void saveCalFile(IniFile pCalFile)
{

    for (Threshold t : thresholds) { t.saveCalFile(pCalFile); }

}//end of TraceGraph::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::addTraces
//
// Creates and sets up the traces and adds them to the panel.
//

private void addTraces()
{

    traces = new Trace[numTraces];

    for(int i=0; i<traces.length; i++){

        traces[i] = new Trace();
        traces[i].init(chartGroupNum, chartNum, graphNum, i, width, height,
            backgroundColor, drawGridBaseline, gridColor, gridXSpacing,
            gridYSpacing, graphInfo, configFile, thresholds);
    }

}//end of TraceGraph::addTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::addThresholds
//
// Creates and sets up the thresholds and adds them to the panel.
//

private void addThresholds()
{

    thresholds = new Threshold[numThresholds];

    for(int i=0; i<thresholds.length; i++){

        thresholds[i] = new Threshold(sharedSettings, configFile, graphInfo, chartGroupNum,
                                        chartNum, graphNum, i, width, height,
                                        backgroundColor);
        thresholds[i].init();

    }

}//end of TraceGraph::addThresholds
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::paintComponent
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

    paintChildren(g2);

}// end of TraceGraph::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::paintChildren
//
// Paints all the traces and other child objects on the canvas.
//

@Override
public void paintChildren(Graphics2D pG2)
{

    for (Trace trace : traces) { trace.paintTrace(pG2); }

    //tell threshold to paint line all the way across
    for (Threshold t : thresholds) { t.paintThresholdLine(pG2); }

}// end of TraceGraph::paintChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::paintSingleTraceDataPoint
//
// Draws line from data point specified by pIndex to the next point in the
// buffer for pTrace.
//

public void paintSingleTraceDataPoint(int pTrace, int pIndex)
{

    traces[pTrace].paintTrace((Graphics2D) getGraphics());

}// end of TraceGraph::paintSingleTraceDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::updateChild
//
// Plots all data added to the data transfer buffer and erases any data which
// has been marked as erased for pChildNum.
//

@Override
public void updateChild(int pChildNum)
{

    traces[pChildNum].updateTrace((Graphics2D) getGraphics());

}// end of TraceGraph::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::getXOfPeakInBox
//
// Returns the x location of the greatest peak within the box. If no peak is
// found within the box, then the x location of the peak closest to the center
// of the box is returned.
//
// The box is centered at pX, pY.
//

public int getXOfPeakInBox(int pX, int pY, int pWidth, int pHeight)
{

    //calculate x points of box
    int xStart = pX+graphInfo.scrollOffset-pWidth/2;
    int xEnd = pX+graphInfo.scrollOffset+pWidth/2;

    //calculate y points of box
    int yStart = pY-pHeight/2;
    int yEnd = pY+pHeight/2;

    //determine the greatest peak
    Trace peakTrace = null; int peak=-1; int peakX=-1; int yDistance=-1;
    for (Trace t : traces) {

        int newP = t.getPeak(xStart, xEnd, yStart, yEnd);

        //if trace has a peak, but its not in the box, and no other
        //peaks have been found in the box yet, use the trace as the
        //peak trace if his peak's y distance from the center of the
        //box is closer than any other traces'
        if (newP==-1&&peak==-1&&t.getLastRequestedPeakX()!=-1) {
            int newYDist = Math.abs(t.getLastRequestedPeakY()-pY);

            if (yDistance==-1||newYDist<yDistance) {
                yDistance = newYDist;
                peakTrace=t;
            }
        }
        //peak found and is new peak
        else if (newP>peak) { peak=newP; peakTrace=t; }
    }
    if (peakTrace!=null) { peakX = peakTrace.getLastRequestedPeakX(); }

    return peakX;

}// end of TraceGraph::getXOfPeakInBox
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::getTrace
//
// Returns Trace pTrace.
//

@Override
public Trace getTrace(int pTrace)
{

    if (pTrace < 0 || pTrace >= traces.length){ return(null); }

    return(traces[pTrace]);

}// end of TraceGraph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::getNumChildren
//
// Returns numTraces.
//

@Override
public int getNumChildren()
{

    return(numTraces);

}// end of TraceGraph::getNumChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::resetAll
//
// Resets all values and child values to default.
//

@Override
public void resetAll()
{

    super.resetAll();

    for (Trace trace : traces) { trace.resetData(); }

}// end of TraceGraph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::resetAllChildrenData
//
// For all traces resets all data to zero and all flags to DEFAULT_FLAGS.
// Resets dataInsertPos to zero.
//

public void resetAllChildrenData()
{

    for (Trace trace : traces) { trace.resetData(); }

}// end of TraceGraph::resetAllChildrenData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::setVerticalBarAllChildren
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces.
//

@Override
public void setVerticalBarAllChildren()
{

    for (Trace trace : traces) {
        trace.getDataBuffer().setFlagsAtCurrentInsertionPoint(
                                           DataTransferIntBuffer.VERTICAL_BAR);
    }

}// end of TraceGraph::setVerticalBarAllChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::setChildConnectPoints
//
// For Trace pChildNum, sets the connectPoints flag. If true, points will be
// connected by a line.
//

@Override
public void setChildConnectPoints(int pChildNum, boolean pValue)
{

    if (pChildNum < 0 || pChildNum >= traces.length){ return; }

    traces[pChildNum].setConnectPoints(pValue);

}// end of TraceGraph::setChildConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::setChildYScale
//
// For Trace pChildNum, sets the display vertical scale to pScale
//

@Override
public void setChildYScale(int pChildNum, double pScale)
{

    if (pChildNum < 0 || pChildNum >= traces.length){ return; }

    traces[pChildNum].setYScale(pScale);

}// end of TraceGraph::setChildYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::setChildOffset
//
// For Trace pChildNum, sets the display offset to pOffset.
//

@Override
public void setChildOffset(int pChildNum, int pOffset)
{

    if (pChildNum < 0 || pChildNum >= traces.length){ return; }

    traces[pChildNum].setOffset(pOffset);

}// end of TraceGraph::setChildOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::setChildBaseLine
//
// For trace pChildNum, sets the baseLine value to pBaseLine. This will cause
// the pBaseline value to be shifted to zero when the trace is drawn.
//

@Override
public void setChildBaseLine(int pChildNum, int pBaseLine)
{

    if (pChildNum < 0 || pChildNum >= traces.length){ return; }

    traces[pChildNum].setBaseLine(pBaseLine);

}// end of TraceGraph::setChildBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::setAllChildrenXScale
//
// Sets the display horizontal scale for all traces to pScale.
//

@Override
public void setAllChildrenXScale(double pScale)
{

    for (Trace trace : traces) { trace.setXScale(pScale); }

}// end of TraceGraph::setAllChildrenXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::getTrace
//
// Returns the reference to trace pTrace.
//

public Trace getTrace(int pGraph, int pTrace)
{

    if (pTrace < 0 || pTrace >= traces.length){ return(null); }

    return( traces[pTrace] );

}// end of TraceGraph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//
//

@Override
public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList,
                                                           String pObjectType)
{

    super.scanForGUIObjectsOfAType(pObjectList, pObjectType);

    for (Trace trace : traces) {
        trace.scanForGUIObjectsOfAType(pObjectList, pObjectType);
    }

}// end of TraceGraph::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------


}//end of class TraceGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import model.DataFlags;
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

    metaDataSectionName = "Trace Graph";

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
// TraceGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    fileSection = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                                        + " Graph " + graphNum;

    super.loadConfigSettings();

    numTraces = configFile.readInt(fileSection, "number of traces", 0);

    numThresholds = configFile.readInt(fileSection,
                                            "number of thresholds", 0);

    gridColor = configFile.readColor(
                                 fileSection, "grid color", Color.BLACK);

    drawGridBaseline = configFile.readBoolean(
                               fileSection, "draw grid baseline", false);

    invertGraph = configFile.readBoolean(
                                      fileSection, "invert graph", true);

    int numVerGridDivisions = configFile.readInt(
                   fileSection, "number of vertical grid divisions", 10);

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
// TraceGraph::loadSegment
//
// Tells each trace to load the segment he was in pFile.
//

@Override
public String loadSegment(BufferedReader pIn, String pLastLine)
        throws IOException
{

    //does not call super because we don't want to throw error if index does
    //not match, legacy files from UT chart may not have indexes for each Graph
    //because they rely on StripChart, which is considered the graph
    String line = processMetaData(pIn, pLastLine, false);

    for (Trace t : traces) { line = t.loadSegment(pIn, line); }
    
    //DEBUG HSS//  test code to set width to first trace
    width = traces[0].getWidth();
    setSizes(this, width, height);
    //DEBUG HSS// end

    return line;

}//end of TraceGraph::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::saveSegment
//
// Tells each trace to save the current segment to file.
//

@Override
public void saveSegment(BufferedWriter pOut) throws IOException
{

    super.saveSegment(pOut);

    for (Trace t : traces) { t.saveSegment(pOut); }

}//end of TraceGraph::saveSegment
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
            gridYSpacing, graphInfo, configFile, thresholds, sharedSettings);
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
// TraceGraph::markSegmentStart
//
// Instructs children to mark last retrieved data as segment start.
//

@Override
public void markSegmentStart()
{
    
    for (Trace c : traces){ c.markSegmentStart();  }
    
}//end of TraceGraph::markSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::markSegmentEnd
//
// Instructs children to mark last retrieved data as segment end.
//

@Override
public void markSegmentEnd()
{
    
    for (Trace c : traces){ c.markSegmentEnd();  }

}//end of TraceGraph::markSegmentEnd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::isSegmentStarted
//
// Checks to see if a segment has been started and thus may have data which
// needs to be saved.
//

@Override
public boolean isSegmentStarted()
{

    for (Trace t : traces) { if (t.isSegmentStarted()) { return true; } }
    
    return(false);

}//end of TraceGraph::isSegmentStarted
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
// TraceGraph::updateChildren
//
// Instructs all children listening for transfer buffer changes to check for
// changes and update.
//

@Override
public void updateChildren()
{

    for (Trace t : traces) { t.updateTrace((Graphics2D) getGraphics()); }

}// end of TraceGraph::updateChildren
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

    //find peak
    Trace peakTrace=null; int peak=-1; int peakX=-1;
    Trace boxTrace=null; int boxPeak=-1; int boxPeakX=-1;
    for (Trace t : traces) {
        
        //get peak in box
        int newP = t.getPeak(xStart, xEnd, yStart, yEnd);
        
        //store if greatest peak in box
        if (newP!=-1 && newP>boxPeak) {
            boxTrace = t;
            boxPeak = t.getLastRequestedPeak();
            boxPeakX = t.getLastRequestedPeakX();
        }
        //store if not in box, but new greatest peak
        else if (t.getLastRequestedPeak()>peak&&t.getLastRequestedPeakX()!=-1) {
            peakTrace = t;
            peak = t.getLastRequestedPeak();
            peakX = t.getLastRequestedPeakX();
        }
        
    }
    
    //if peak was found in box, use it. Otherwise, use the greatest found peak
    //within the x range
    if (boxTrace!=null) {
        peakTrace = boxTrace;
        peak = boxPeak;
        peakX = boxPeakX;
    }
    
    
    if (peakTrace!=null) { peakX = peakTrace.getLastRequestedPeakX(); }

    return peakX;

}// end of TraceGraph::getXOfPeakInBox
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// TraceGraph::getLastDrawnX
//
// Returns the last known drawn x.
//

@Override
public int getLastDrawnX()
{

    int greatestX = 0;
    for (Trace t : traces) { 
        if (t.getPrevX()>greatestX) { greatestX = t.getPrevX(); } 
    }
    
    return greatestX;

}// end of TraceGraph::getLastDrawnX
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

    repaint();

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
                                           DataFlags.VERTICAL_BAR);
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

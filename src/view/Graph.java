/******************************************************************************
* Title: Chart.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Graph
//

public class Graph extends JPanel{

    private final int numTraces = 4; //change this to 0 after adding config load section
    Trace[] traces;

    String title;
    int chartIndex, index, width, height;
    
//-----------------------------------------------------------------------------
// Graph::Graph (constructor)
//
//

public Graph()
{
    
}//end of Graph::Graph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::init
//

public void init(int pChartIndex, int pIndex, int pWidth, int pHeight)
{

    chartIndex = pChartIndex; index = pIndex; 
    width = pWidth; height = pHeight;

    setSizes(this, width, height);
    
    createTraces(); //debug mks -- do this in the config section
    
}// end of Graph::init
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

public void paintTraces (Graphics2D pG2)
{
    
    for (Trace trace : traces) { trace.paint(pG2); }

}// end of Graph::paintTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::createTraces
//
// Creates and sets up the traces.
//

private void createTraces()
{

    traces = new Trace[numTraces];

    //trace colors
    Color color[] = new Color[numTraces];
    color[0] = new Color(Color.MAGENTA.getRGB());
    color[1] = new Color(Color.BLUE.getRGB());
    color[2] = new Color(Color.GREEN.getRGB());
    color[3] = new Color(Color.CYAN.getRGB());


    for(int i=0; i<traces.length; i++){

        traces[i] = new Trace();
        traces[i].init(i, width, height, color[i], Trace.CONNECT_POINTS);
        
        traces[i].setOffset(50); //place 0 value at vertical center

    }

}//end of Graph::createTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getTrace
//
// Returns Trace pTrace.
//

public Trace getTrace(int pTrace)
{

    if (pTrace < 0 || pTrace > traces.length){ return(null); }

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

   if (pTrace < 0 || pTrace > traces.length){ return; }

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

   if (pTrace < 0 || pTrace > traces.length){ return; }

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

    if (pTrace < 0 || pTrace > traces.length){ return; }

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

    if (pTrace < 0 || pTrace > traces.length){ return; }

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
    
    if (pTrace < 0 || pTrace > traces.length){ return; }

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
    
    if (pTrace < 0 || pTrace > traces.length){ return; }

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

    if (pTrace < 0 || pTrace > traces.length){ return; }

    traces[pTrace].insertDataPoint(pData);

}// end of Graph::insertDataPointInTrace
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

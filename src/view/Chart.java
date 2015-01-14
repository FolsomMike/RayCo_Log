/******************************************************************************
* Title: Chart.java
* Author: Mike Schoonover
* Date: 11/13/13
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

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Chart
//

class Chart extends JPanel{

    private final int numberTraces = 4;

    private String title;    
    private int index;
    private int width;
    private int height;

    Graph graph;
    
    Trace[] traces;

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

public void init(String pTitle, int pIndex, int pWidth, int pHeight)
{

    title = pTitle; index = pIndex; width = pWidth; height = pHeight;

    setBorder(BorderFactory.createTitledBorder(title));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    createTraces();    
    
    graph = new Graph(); //the traces are drawn on this panel
    graph.init(traces);
    add(graph);
    
    //set the size of the graph...the chart will be packed to fit around it
    setSizes(graph, width, height);

}// end of Chart::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::repaintGraph
//
// Forces the graph to be repainted.
//

public void repaintGraph()
{

    invalidate();
   // repaint();
    
  //  graph.repaint();
    
}// end of Chart::repaintGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::createTraces
//
// Creates and sets up the traces.
//

private void createTraces()
{

    traces = new Trace[numberTraces];

    //trace colors
    Color color[] = new Color[numberTraces];
    color[0] = new Color(Color.MAGENTA.getRGB());
    color[1] = new Color(Color.BLUE.getRGB());
    color[2] = new Color(Color.GREEN.getRGB());
    color[3] = new Color(Color.CYAN.getRGB());


    for(int i=0; i<traces.length; i++){

        traces[i] = new Trace();
        traces[i].init(i, width, height, color[i], Trace.CONNECT_POINTS);
        
        traces[i].setOffset(50); //place 0 value at vertical center

    }

}//end of Chart::createTraces
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

    graph.paintTraces (pG2);

}// end of Chart::paintTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::insertDataPointInTrace
//
// Stores pData in Trace pTrace.
//

public void insertDataPointInTrace(int pTrace, int pData)
{

    if (pTrace < 0 || pTrace > traces.length){ return; }

    traces[pTrace].insertDataPoint(pData);

}// end of Chart::insertDataPointInTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setAllTraceXScale
//
// Sets the display horizontal scale for all traces to pScale.
//

public void setAllTraceXScale(double pScale)
{
    
    for (Trace trace : traces) { trace.setXScale(pScale); }

}// end of Chart::setAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceYScale
//
// Sets the display vertical scale for pTrace to pScale
//

public void setTraceYScale(int pTrace, double pScale)
{

    if (pTrace < 0 || pTrace > traces.length){ return; }

    traces[pTrace].setYScale(pScale);

}// end of Chart::setTraceYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceConnectPoints
//
// Sets the connectPoints flag. If true, points will be connected by a line.
//

public void setTraceConnectPoints(int pTrace, boolean pValue)
{

    if (pTrace < 0 || pTrace > traces.length){ return; }

    traces[pTrace].setConnectPoints(pValue);
    
}// end of Chart::setTraceConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceFlags
//
// OR's pFlags of Trace pTrace with flags[pIndex] to set one or more flag bits
// in the flags array at the specified position pIndex.
//

public void setTraceFlags(int pTrace, int pIndex, int pFlags)
{

   if (pTrace < 0 || pTrace > traces.length){ return; }

   traces[pTrace].setFlags(pIndex, pFlags);

}// end of Chart::setTraceFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceFlagsAtCurrentInsertionPoint
//
// OR's pFlags with flags[<current insertion point>] to set one or more flag
// bits in the flags array at the current data insertionPoint.
//

public void setTraceFlagsAtCurrentInsertionPoint(int pTrace, int pFlags)
{

   if (pTrace < 0 || pTrace > traces.length){ return; }

   traces[pTrace].setFlagsAtCurrentInsertionPoint(pFlags);

}// end of Chart::setTraceFlagsAtCurrentInsertionPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setVerticalBarAllTraces
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces.
//

public void setVerticalBarAllTraces()
{

    for (Trace trace : traces) {
            trace.setFlags(trace.getDataInsertPos(), Trace.VERTICAL_BAR);
    }

}// end of Chart::setVerticalBarAllTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceOffset
//
// Sets the display offset for Trace pTrace to pOffset.
//

public void setTraceOffset(int pTrace, int pOffset)
{

    if (pTrace < 0 || pTrace > traces.length){ return; }

    traces[pTrace].setOffset(pOffset);

}// end of Chart::setTraceOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::setTraceBaseLine
//
// Sets the baseLine value for Trace pTrace to pBaseLine. This will cause the
// pBaseline value to be shifted to zero when the trace is drawn
//

public void setTraceBaseLine(int pTrace, int pBaseLine)
{

    if (pTrace < 0 || pTrace > traces.length){ return; }

    traces[pTrace].setBaseLine(pBaseLine);

}// end of Chart::setTraceBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::resetAllTraceData
//
// For all traces resets all data to zero and all flags to DEFAULT_FLAGS.
// Reses dataInsertPos to zero.
//

public void resetAllTraceData()
{
    
    for (Trace trace : traces) {
        trace.resetData();
    }

}// end of Chart::resetAllTraceData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getNumberTraces
//
// Returns numberTraces.
//

public int getNumberTraces()
{

    return(numberTraces);

}// end of Chart::getNumberTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::getTrace
//
// Returns Trace pTrace from the traces array.
//

public Trace getTrace(int pTrace)
{

    if (pTrace < 0 || pTrace > traces.length){ return(null); }

    return(traces[pTrace]);

}// end of Chart::getTrace
//-----------------------------------------------------------------------------

}//end of class Chart
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Graph
//

class Graph extends JPanel{

    Trace[] traces;
    
//-----------------------------------------------------------------------------
// Graph::init
//

public void init(Trace[] pTraces)
{

    traces = pTraces;

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

}//end of class Graph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

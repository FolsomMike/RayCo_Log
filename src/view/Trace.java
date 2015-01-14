/******************************************************************************
* Title: Trace.java
* Author: Mike Schoonover
* Date: 11/13/13
*
* Purpose:
*
* This class draws a trace
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.awt.geom.Ellipse2D;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Trace
//

public class Trace{

    private int index;
    private int width;
    private int height;
    private int maxY;
    private int data[];
    private int flags[];
    private int dataInsertPos = 0;
    private double xScale = 1.0;    
    private double yScale = 1.0;
    private int offset = 0;
    private int baseLine = 0;
    private Color traceColor;
    private final Color circleColor = DEFAULT_CIRCLE_COLOR;
    private boolean visible = true;
    private boolean connectPoints = true;
    
    public static final int DEFAULT_FLAGS = 0x00;
    public static final int VERTICAL_BAR = 0x01;
    public static final int CIRCLE = 0x02;

    private static final Color VERTICAL_BAR_COLOR = Color.DARK_GRAY;
    private static final Color DEFAULT_CIRCLE_COLOR = Color.BLACK;
    
    public static final boolean CONNECT_POINTS = true;
    public static final boolean DO_NOT_CONNECT_POINTS = false;
    
//-----------------------------------------------------------------------------
// Trace::Trace (constructor)
//
//

public Trace()
{

}//end of Trace::Trace (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init(int pIndex, int pWidth, int pHeight, Color pTraceColor,
                                                        boolean pConnectPoints)
{

    index = pIndex; width = pWidth; height = pHeight; maxY = height - 1;
    connectPoints = pConnectPoints;
    
    data = new int[width];

    flags = new int[width];
    for (int i = 0; i<width; i++){ flags[i] = DEFAULT_FLAGS; }

    traceColor = pTraceColor;

}// end of Trace::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setXScale
//
// Sets the trace display horizontal scale to pScale.
//

public void setXScale(double pScale)
{

    xScale = pScale;

}// end of Trace::setXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setYScale
//
// Sets the trace display vertical scale to pScale.
//

public void setYScale(double pScale)
{

    yScale = pScale;

}// end of Trace::setYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setOffset
//
// Sets the display offset for Trace pTrace to pOffset.
//

public void setOffset(int pOffset)
{

    offset = pOffset;

}// end of Trace::setOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setBaseLine
//
// Sets the baseLine value to pBaseLine. This will cause the pBaseline
// value to be shifted to zero when the trace is drawn
//

public void setBaseLine(int pBaseLine)
{

    baseLine = pBaseLine;

}// end of Trace::setBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setConnectPoints
//
// Sets the connectPoints flag. If true, points will be connected by a line.
//

public void setConnectPoints(boolean pValue)
{

    connectPoints = pValue;

}// end of Trace::setConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::insertDataPoint
//
// Stores pData at data[dataInsertPos] and incrmements dataInsertPos.
//

public void insertDataPoint(int pData)
{

    data[dataInsertPos] = pData;
    dataInsertPos++;

}// end of Trace::insertDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::resetData
//
// Resets all data to zero and all flags to DEFAULT_FLAGS. Reses dataInsertPos
// to zero.
//

public void resetData()
{

    for (int i = 0; i<width; i++){ data[i] = 0; }
    for (int i = 0; i<width; i++){ flags[i] = DEFAULT_FLAGS; }

    dataInsertPos = 0;

}// end of Trace::resetData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setFlags
//
// OR's pFlags with flags[pIndex] to set one or more flag bits in the flags
// array at the specified position pIndex.
//

public void setFlags(int pIndex, int pFlags)
{

    flags[pIndex] |= pFlags;

}// end of Trace::setFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setFlagsAtCurrentInsertionPoint
//
// OR's pFlags with flags[<current insertion point>] to set one or more flag
// bits in the flags array at the current data insertion point.
//

public void setFlagsAtCurrentInsertionPoint(int pFlags)
{

    flags[getDataInsertPos()] |= pFlags;

}// end of Trace::setFlagsAtCurrentInsertionPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::getDataInsertPos
//
// Returns dataInsertPos.
//

public int getDataInsertPos()
{

    return(dataInsertPos);

}// end of Trace::getDataInsertPos
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::getWidth
//
// Returns width.
//

public int getWidth()
{

    return(width);

}// end of Trace::getWidth
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setVisible
//
// Sets the visible flag to pVisible.
//

public void setVisible(boolean pVisible)
{

    visible = pVisible;

}// end of Trace::setVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::paint
//
// Draws the entire trace.
//

public void paint (Graphics2D pG2)
{

    if(!visible) { return; }
    
    for(int i = 0; i<width-1; i++){

        int x1 = (int)Math.round(i * xScale);
        int x2 = (int)Math.round((i+1) * xScale);
                
        //draw a vertical line if the flag is set
        if ((flags[i] & VERTICAL_BAR) != 0){
            pG2.setColor(VERTICAL_BAR_COLOR);
            pG2.drawLine(x1, 0, x1, maxY);
        }

        pG2.setColor(traceColor);

        //apply baseline shift
        int y1 = (int)(data[i] - baseLine);
        int y2 = (int)(data[i+1] - baseLine);

        //apply scaling and display offset
        y1 = (int)(y1 * yScale) + offset;
        y2 = (int)(y2 * yScale) + offset;

        //invert y1 & y2 so zero is at the bottom of the chart

        if(y1 > maxY){ y1 = 0; }
        else { y1 = maxY - y1; }

        if(y2 > maxY){ y2 = 0; }
        else { y2 = maxY - y2; }
        
        //draw between each two points
        if(connectPoints) { 
            pG2.drawLine(x1, y1, x2, y2);
        }
        else{
            if(data[i] != Integer.MAX_VALUE){
                pG2.drawLine(x1, y1, x1, y1);
            }
        }
        
        //draw a circle on the datapoint if the CIRCLE flag is set
        if ((flags[i] & CIRCLE) != 0){
            pG2.setColor(circleColor);
            pG2.draw(new Ellipse2D.Double(x1-3, y1-3, 6, 6));
        }
        
    }

}// end of Trace::paint
//-----------------------------------------------------------------------------

}//end of class Trace
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

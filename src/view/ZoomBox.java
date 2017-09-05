/******************************************************************************
* Title: ZoomBox.java
* Author: Mike Schoonover
* Date: 01/27/15
*
* Purpose:
*
* This class encapsulates a dataset which is displayed in a box. Useful for
* displaying a small graph.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomBox
//

class ZoomBox{

    private String title;
    private String shortTitle;
    private final int chartGroupNum, chartNum, graphNum, zoomBoxNum;
    private int x, y;
    public void setX(int pX) { x = pX; }
    public int getX() { return x; }
    public void setY(int pY) { y = pY; }
    public int getY() { return y; }
    private final int width, height;
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    int data[];

    //data start and end indexes are NOT used to determine where to start
    //in the data array stored in this class. They are used to store the
    //indexes that this box can cover in another array that contains the data
    //array
    private int dataStartIndex = 0;
    public void setDataStartIndex(int pI) { dataStartIndex = pI; }
    public int getDataStartIndex() { return dataStartIndex; }
    private int dataEndIndex = 0;
    public void setDataEndIndex(int pI) { dataEndIndex = pI; }
    public int getDataEndIndex() { return dataEndIndex; }

    //WIP HSS// all of these should be read from inifile (except x&y)
    private boolean hasArrows = true;
    private int arrowX = -1;
    private int arrowY = -1;
    private int arrowWidth = 10;
    private int arrowHeight = 6;

//-----------------------------------------------------------------------------
// ZoomBox::ZoomBox (constructor)
//
//

public ZoomBox(int pChartGroupNum, int pChartNum, int pGraphNum,
                int pZoomBoxNum, int pX, int pY, int pWidth, int pHeight)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum; graphNum = pGraphNum;
    zoomBoxNum = pZoomBoxNum;

    x = pX; y = pY; width = pWidth; height = pHeight;

    data = new int[pWidth];

}//end of Chart::ZoomBox (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::setData
//
// Copies the data in pData array to the data array so it can be displayed.
//

public void setData(int[] pData)
{

    int length;

    length = (pData.length < data.length) ? pData.length : data.length;

    System.arraycopy(pData, 0, data, 0, length);

}// end of ZoomBox::setData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::setArrowLocation
//
// The point of the arrow is set to the x,y location.
//
// Note that paint() needs to be called after this function for the changes to
// take affect.
//

public void setArrowLocation(int pX, int pY)
{

    arrowX = pX;
    arrowY = pY;

}// end of ZoomBox::setArrowLocation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::paint
//

public void paint(Graphics2D pG2)
{

    pG2.setColor(new Color(0xd9, 0xef, 0xef));

    RoundRectangle2D roundedRect =
                    new RoundRectangle2D.Float(x, y, width, height, 10, 10);
    pG2.fill(roundedRect);

    pG2.setColor(Color.BLACK);
    pG2.draw(roundedRect);

    pG2.drawLine(x,y+height/2, x+width, y+height/2);

    int vertOffset = y + height/2;

    pG2.setColor(Color.RED);
    for(int i=1; i<data.length; i++){
        //data values inverted because 0 is in top left corner
        int yScale = 5; //WIP HSS// read in from ini file
        int y1 = ((data[i-1]/yScale)*-1)+vertOffset;
        int y2 = ((data[i]/yScale)*-1)+vertOffset;
        pG2.drawLine(x+i-1, y1, x+i, y2);
    }

    drawArrow(pG2);

}// end of ZoomBox::paint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::drawArrow
//
// Draws the arrow the canvas.
//

private void drawArrow(Graphics2D pG2)
{

    //draw arrow
    int[] xPoints = {arrowX-arrowWidth/2,   arrowX,     arrowX+arrowWidth/2};
    int[] yPoints = {arrowY+arrowHeight,    arrowY,     arrowY+arrowHeight};

    //width to clear all possible x positions of the arrow. The start and end
    //indexes are the x positions.
    pG2.setColor(pG2.getBackground());
    int clearWidth = dataEndIndex-dataStartIndex;
    pG2.fillRect(x, arrowY, clearWidth, arrowHeight);

    //if left or right points lie outside the zoom box, split them in
    //half so that users can easily discern which arrow belongs to which box
    if (xPoints[0]<x) {
        xPoints[0] = arrowX; xPoints[2] += arrowWidth/2;
    }
    else if (xPoints[2]>x+width) {
        xPoints[2] = arrowX; xPoints[0] -= arrowWidth/2;
    }

    pG2.setColor(Color.BLACK);
    pG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);


    pG2.fillPolygon(xPoints, yPoints, 3);

}// end of ZoomBox::drawArrow
//-----------------------------------------------------------------------------

}//end of class ZoomBox
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

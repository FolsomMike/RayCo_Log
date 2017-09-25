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
import javax.swing.UIManager;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomBox
//

class ZoomBox{

    private GraphInfo graphInfo;

    private String title;
    private String shortTitle;
    private final int chartGroupNum, chartNum, graphNum, zoomBoxNum;
    private int x, xEnd, y;
    public void setX(int pX) { x = pX; }
    public int getX() { return x; }
    public void setXEnd(int pI) { xEnd = pI; }
    public int getXEnd() { return xEnd; }
    public void setY(int pY) { y = pY; }
    public int getY() { return y; }
    private final int width, height;
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    int data[];

    private final boolean hasArrow;
    private int arrowX;
    public int getArrowX() { return arrowX; }
    private final int arrowY;
    private final int arrowWidth;
    private final int arrowHeight;

//-----------------------------------------------------------------------------
// ZoomBox::ZoomBox (constructor)
//
//

public ZoomBox(int pChartGroupNum, int pChartNum, int pGraphNum,
                int pZoomBoxNum, GraphInfo pGraphInfo, int pX, int pXEnd,int pY,
                int pWidth, int pHeight, boolean pHasArrow,
                int pArrowX, int pArrowY, int pArrowWidth, int pArrowHeight)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum; graphNum = pGraphNum;
    zoomBoxNum = pZoomBoxNum;
    graphInfo = pGraphInfo;

    x = pX; xEnd = pXEnd; y = pY; width = pWidth; height = pHeight;

    hasArrow = pHasArrow;
    arrowX = pArrowX; arrowY = pArrowY;
    arrowWidth = pArrowWidth; arrowHeight = pArrowHeight;

    data = new int[pWidth];

}//end of Chart::ZoomBox (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::setData
//
// Copies the data in pData array to the data array so it can be displayed.
//

public void setData(int[] pData, int pDataX)
{

    int length;

    length = (pData.length < data.length) ? pData.length : data.length;

    System.arraycopy(pData, 0, data, 0, length);

    arrowX = pDataX;

}// end of ZoomBox::setData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::paint
//

public void paint(Graphics2D pG2)
{

    int adjX = x - graphInfo.scrollOffset;

    pG2.setColor(new Color(0xd9, 0xef, 0xef));

    RoundRectangle2D roundedRect =
                    new RoundRectangle2D.Float(adjX, y, width, height, 10, 10);
    pG2.fill(roundedRect);

    pG2.setColor(Color.BLACK);
    pG2.draw(roundedRect);

    pG2.drawLine(adjX,y+height/2, adjX+width, y+height/2);

    int vertOffset = y + height/2;

    pG2.setColor(Color.RED);
    for(int i=1; i<data.length; i++){
        //data values inverted because 0 is in top left corner
        int yScale = 5; //WIP HSS// read in from ini file
        int y1 = ((data[i-1]/yScale)*-1)+vertOffset;
        int y2 = ((data[i]/yScale)*-1)+vertOffset;
        pG2.drawLine(adjX+i-1, y1, adjX+i, y2);
    }

    if (hasArrow) { drawArrow(pG2); }

}// end of ZoomBox::paint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomBox::drawArrow
//
// Draws the arrow the canvas.
//

private void drawArrow(Graphics2D pG2)
{

    int adjX = x - graphInfo.scrollOffset;
    int adjXEnd = xEnd - graphInfo.scrollOffset;
    int adjArrowX = arrowX - graphInfo.scrollOffset;

    //draw arrow
    int[] xPoints = {adjArrowX-arrowWidth/2,adjArrowX,  adjArrowX+arrowWidth/2};
    int[] yPoints = {arrowY+arrowHeight,    arrowY,     arrowY+arrowHeight};

    //width to clear all possible x positions of the arrow. The start and end
    //indexes are the x positions.
    pG2.setColor(UIManager.getColor ("Panel.background"));
    pG2.fillRect(adjX, arrowY, adjXEnd-adjX, arrowHeight);

    //if left or right points lie outside the zoom box, split them in
    //half so that users can easily discern which arrow belongs to which box
    if (xPoints[0]<adjX) {
        xPoints[0] = adjArrowX; xPoints[2] += arrowWidth/2;
    }
    else if (xPoints[2]>adjX+width) {
        xPoints[2] = adjArrowX; xPoints[0] -= arrowWidth/2;
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

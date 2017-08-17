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
import model.DataTransferIntBuffer;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomBox
//

class ZoomBox{

    private String title;
    private String shortTitle;
    private final int chartGroupNum, chartNum, graphNum, zoomBoxNum;
    private final int x, y, width, height;

    int data[];

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

    int vertOffset = y + height/2;

    for(int i=1; i<data.length; i++){
        //data values inverted because 0 is in top left corner
        int yScale = 5; //WIP HSS// read in from ini file
        int y1 = ((data[i-1]/yScale)*-1)+vertOffset;
        int y2 = ((data[i]/yScale)*-1)+vertOffset;
        pG2.drawLine(x+i-1, y1, x+i, y2);
    }

}// end of ZoomBox::paint
//-----------------------------------------------------------------------------


}//end of class ZoomBox
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

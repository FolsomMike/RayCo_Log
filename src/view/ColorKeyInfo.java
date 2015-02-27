/******************************************************************************
* Title: ColorKeyInfo.java
* Author: Mike Schoonover
* Date: 02/27/15
*
* Purpose:
*
* This class contains information for a color key which is used to explain
* the meaning of a color used on a graph or other display.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ColorKeyInfo
//

class ColorKeyInfo{
    
    Color keyColor;
    String keyDescription;
    int xPos, yPos;
    
//-----------------------------------------------------------------------------
// ColorKeyInfo::ColorKeyInfo (constructor)
//
//

public ColorKeyInfo(Color pKeyColor, String pKeyDescription,
                                                          int pXPos, int pYPos)
{

    keyColor = pKeyColor; keyDescription = pKeyDescription;
    xPos = pXPos; yPos = pYPos;
        
}//end of ColorKeyInfo::ColorKeyInfo (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::paint
//

public void paint(Graphics2D pG2)
{
    //draw a color square
    pG2.setColor(keyColor);
    pG2.fillRect(xPos, yPos, 10, 10);
    //write the color's notation next to it
    pG2.setColor(Color.BLACK);
    pG2.drawString(keyDescription, xPos+12, yPos+10);

}// end of ChartInfoPanel::paint
//-----------------------------------------------------------------------------


}//end of class ChartInfoPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

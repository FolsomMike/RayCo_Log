/******************************************************************************
* Title: SeparatorPanel.java
* Author: Mike Schoonover
* Date: 1/14/15
*
* Purpose:
*
* This class subclasses a JPanel to display a separator line or other graphic
* to provide visual separation between other panels or GUI objecs.
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
// class SeparatorPanel
//

class SeparatorPanel extends JPanel{

    private int width;
    private int height;
    Color lineColor;
    int lineThickness;
 
//-----------------------------------------------------------------------------
// SeparatorPanel::SeparatorPanel (constructor)
//
//

public SeparatorPanel()
{

}//end of SeparatorPanel::SeparatorPanel (constructor)
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// SeparatorPanel::init
//

public void init(int pWidth, int pHeight, Color pLineColor, int pLineThickness)
{

    width = pWidth; height = pHeight; lineColor = pLineColor; 
    lineThickness = pLineThickness;
    
    setSizes(this, width, height);
        
}// end of SeparatorPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SeparatorPanel::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(lineColor);
    g2.drawLine(0, 0, width, 0);
    
}// end of SeparatorPanel::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SeparatorPanel::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of SeparatorPanel::setSizes
//-----------------------------------------------------------------------------

}//end of class SeparatorPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

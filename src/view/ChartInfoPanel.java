/******************************************************************************
* Title: ChartInfoPanel.java
* Author: Mike Schoonover
* Date: 02/27/15
*
* Purpose:
*
* This class subclasses a JPanel to display information about a chart, such
* as a color key, monitoring info, etc.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChartInfoPanel
//

class ChartInfoPanel extends JPanel{
    
    private String title;    
    private String shortTitle;
    private int chartGroupNum, chartNum;
    private int width, height;
    ArrayList<ColorKeyInfo> colorKeys = new ArrayList<>();
    
//-----------------------------------------------------------------------------
// ChartInfoPanel::ChartInfoPanel (constructor)
//
//

public ChartInfoPanel()
{

}//end of ChartInfoPanel::ChartInfoPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init(int pChartGroupNum, int pChartNum, int pWidth, int pHeight)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    width = pWidth; height = pHeight;
    
    setSizes(this, width, height);
    
}// end of ChartInfoPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::addColorKey
//
// Adds a color key item to the list so it will be displayed. Color keys
// display a colored square with a note to explain the color's meaning and
// usage.
//

public void addColorKey(Color pKeyColor, String pKeyDescription,
                                                          int pXPos, int pYPos)
{

    colorKeys.add(new ColorKeyInfo(pKeyColor, pKeyDescription, pXPos, pYPos));
    
}// end of ChartInfoPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of ChartInfoPanel::setSizes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);
 
    ListIterator iter = colorKeys.listIterator();
    
    while(iter.hasNext()){
        ((ColorKeyInfo)iter.next()).paint((Graphics2D)g);
    }
        
}// end of ChartInfoPanel::paintComponent
//-----------------------------------------------------------------------------


}//end of class ChartInfoPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

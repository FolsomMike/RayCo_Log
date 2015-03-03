/******************************************************************************
* Title: ZoomGraph.java
* Author: Mike Schoonover
* Date: 01/14/15
*
* Purpose:
*
* This class subclasses a JPanel to display zoomed views of signal indications.
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
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomGraph
//

class ZoomGraph extends Graph{
        
    ArrayList<ZoomBox> zoomBoxes = new ArrayList<>();

    int annoX = 0, annoY = 10;
    int annoWidth = 100, annoHeight = 50;
    int gap;
    
//-----------------------------------------------------------------------------
// ZoomGraph::ZoomGraph (constructor)
//
//

public ZoomGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
            int pWidth, int pHeight, ChartInfo pChartInfo, IniFile pConfigFile)
{

    super(pChartGroupNum, pChartNum, pGraphNum,
                                     pWidth, pHeight, pChartInfo, pConfigFile);
        
}//end of Chart::ZoomGraph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pTitle is the text title for the graph.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

@Override
public void init()
{

    super.init();
    
}// end of ZoomGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);

    ListIterator iter = zoomBoxes.listIterator();
    
    while(iter.hasNext()){
        ((ZoomBox)iter.next()).paint((Graphics2D)g);
    }
        
}// end of ZoomGraph::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::addZoomBox
//
// A ZoomBox to the list so it will be displayed on the graph.
//

public void addZoomBox(int pZoomBoxNum, int[] dataSet)
{

    zoomBoxes.add(new ZoomBox(chartGroupNum, chartNum, graphNum, pZoomBoxNum,
                annoX - graphInfo.scrollOffset, annoY, annoWidth, annoHeight));

    annoX += annoWidth + gap; //prepare x to add next anno object to the right
    
    zoomBoxes.get(zoomBoxes.size()-1).setData(dataSet);
    
    zoomBoxes.get(zoomBoxes.size()-1).paint((Graphics2D)getGraphics());
    
}// end of ChartInfoPanel::addZoomBox
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::scrollGraph
//
// Scrolls the graph area to the left by pShiftAmount and erases the right most
// slice.
//

public void scrollGraph (int pShiftAmount)
{
    
    Graphics2D g2 = (Graphics2D) getGraphics();
    
    //scroll the screen to the left
    g2.copyArea(0, 0, width, height, -1 * pShiftAmount, 0);
    //erase the line at the far right
    g2.setColor(backgroundColor);
    g2.fillRect(width-pShiftAmount, 0, pShiftAmount, height);

    graphInfo.scrollOffset += pShiftAmount;
    graphInfo.lastScrollAmount = pShiftAmount;
    
}// end of ZoomGraph::scrollGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::resetAll
//
// Resets all values and child values to default.
//

@Override
public void resetAll()
{
    
    super.resetAll();

    annoX = 0; //adding anno objects starts over at left edge
    
}// end of ZoomGraph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    configFileSection = 
            "Chart Group " + chartGroupNum + " Chart " + chartNum
                                            + " Annotation Graph " + graphNum;

    super.loadConfigSettings();

    gap = configFile.readInt(configFileSection, "gap between annotations", 4);
    
}// end of ZoomGraph::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class ZoomGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

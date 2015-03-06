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
        
    private final ArrayList<ZoomBox> zoomBoxes = new ArrayList<>();

    private int annoX = 0;
    private final int annoY = 10;
    private final int annoWidth = 100, annoHeight = 50;
    private int gap;
    private int maxNumZoomBoxes;
    
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
// ZoomGraph::addZoomBox
//
// A ZoomBox to the list so it will be displayed on the graph.
//

public void addZoomBox(int pZoomBoxNum, int[] pDataSet)
{

    zoomBoxes.add(new ZoomBox(chartGroupNum, chartNum, graphNum, pZoomBoxNum,
                annoX - graphInfo.scrollOffset, annoY, annoWidth, annoHeight));

    annoX += annoWidth + gap; //prepare x to add next anno object to the right
    
    zoomBoxes.get(zoomBoxes.size()-1).setData(pDataSet);
    
    zoomBoxes.get(zoomBoxes.size()-1).paint((Graphics2D)getGraphics());

    //limit number of boxes
    if (zoomBoxes.size() > maxNumZoomBoxes){ 
        zoomBoxes.remove(0); }
    
}// end of ZoomGraph::addZoomBox
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
    
    maxNumZoomBoxes = configFile.readInt(
            configFileSection, "maximum number of annotation objects", 20);
    
}// end of ZoomGraph::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class ZoomGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

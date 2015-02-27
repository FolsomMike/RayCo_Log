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
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomGraph
//

class ZoomGraph extends JPanel{

    private IniFile configFile;
    
    private String title;    
    private String shortTitle;
    private int chartGroupNum, chartNum, graphNum;
    private int width, height;
    
    ArrayList<ZoomBox> zoomBoxes = new ArrayList<>();

//-----------------------------------------------------------------------------
// ZoomGraph::ZoomGraph (constructor)
//
//

public ZoomGraph()
{

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

public void init(int pChartGroupNum, int pChartNum, int pGraphNum,
                                int pWidth, int pHeight, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum;
    chartNum = pChartNum; graphNum = pGraphNum;
    width = pWidth; height = pHeight;
    configFile = pConfigFile;

    loadConfigSettings();
    
    setSizes(this, width, height);
    
    //debug mks -- remove this
    addZoomBox(chartGroupNum, chartNum, graphNum, 0, 0, 10, 100, 50);
    zoomBoxes.get(0).setData(simulateZoomGraph());
    addZoomBox(chartGroupNum, chartNum, graphNum, 1, 105, 10, 100, 50);    
    zoomBoxes.get(1).setData(simulateZoomGraph());    
    //debug mks -- end remove this
    
}// end of ZoomGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::simulateZoomGraph
//
// Creates a simulated data stream representing a high resolution graph of
// an indication.

private int[] simulateZoomGraph()
{
    
    
    int data[] = new int[100];
    
    for(int i=0; i<data.length; i++){
        data[i] = (int)(5 * Math.random());
    }

    int spikeLoc = (int)(40 + 20 * Math.random());
    
    data[spikeLoc-2] = 20 + (int)(5 * Math.random());
    data[spikeLoc] = - 20 - (int)(5 * Math.random());
    data[spikeLoc+2] = 20 + (int)(5 * Math.random());
    
    return(data);
    
}// end of ZoomGraph::simulateZoomGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                            + " Annotation Graph " + graphNum;

    title = configFile.readString(
                       section, "title", "Annotation Graph " + (graphNum + 1));

    shortTitle = configFile.readString(
                         section, "short title", "annograph" + (graphNum + 1));

    int configWidth = configFile.readInt(section, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0
    
    int configHeight = configFile.readInt(section, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0
    
}// end of ZoomGraph::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of ZoomGraph::setSizes
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

public void addZoomBox(int pChartGroupNum, int pChartNum, int pGraphNum, 
                      int pZoomBoxNum, int pX, int pY, int pWidth, int pHeight)
{

    zoomBoxes.add(
        new ZoomBox(pChartGroupNum, pChartNum, pGraphNum, 
                                    pZoomBoxNum, pX, pY, pWidth, pHeight));
    
}// end of ChartInfoPanel::addZoomBox
//-----------------------------------------------------------------------------


}//end of class ZoomGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

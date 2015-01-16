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
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Chart
//

class ZoomGraph extends JPanel{

    private IniFile configFile;
    
    private String title;    
    private String shortTitle;
    private int chartGroupIndex, chartIndex, index;
    private int width, height;

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

public void init(int pChartGroupIndex, int pChartIndex, int pIndex,
                                  int pWidth, int pHeight, IniFile pConfigFile)
{

    chartGroupIndex = pChartGroupIndex; 
    chartIndex = pChartIndex; index = pIndex; 
    width = pWidth; height = pHeight;
    configFile = pConfigFile;

    loadConfigSettings();
    
    setSizes(this, width, height);
    
}// end of ZoomGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupIndex + " Chart " + chartIndex
            + " Annotation Graph " + index;

    title = configFile.readString(
                        section, "title", "Annotation Graph " + (index + 1));

    shortTitle = configFile.readString(
                            section, "short title", "annograph" + (index + 1));

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
    
}// end of ZoomGraph::paintComponent
//-----------------------------------------------------------------------------


}//end of class ZoomGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

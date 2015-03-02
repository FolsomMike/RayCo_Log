/******************************************************************************
* Title: Graph.java
* Author: Mike Schoonover
* Date: 02/28/15
*
* Purpose:
*
* This class subclasses a JPanel and is the parent class to various types
* of graphing objects.
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
// class Graph
//

public class Graph extends JPanel{

    IniFile configFile;
    String configFileSection;
    
    ChartInfo chartInfo;
    public GraphInfo graphInfo = new GraphInfo();
    
    String title;    
    String shortTitle;
    int chartGroupNum, chartNum, graphNum;
    int width, height;
    Color backgroundColor;
    
//-----------------------------------------------------------------------------
// Graph::Graph (constructor)
//
//

public Graph(int pChartGroupNum, int pChartNum, int pGraphNum,
            int pWidth, int pHeight, ChartInfo pChartInfo, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum;
    chartNum = pChartNum; graphNum = pGraphNum;
    width = pWidth; height = pHeight;
    chartInfo = pChartInfo; configFile = pConfigFile;
    
}//end of Chart::Graph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pTitle is the text title for the graph.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init()
{

    loadConfigSettings();
    
    setSizes(this, width, height);
    
}// end of Graph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::resetAll
//
// Resets all values and child values to default.
//

public void resetAll()
{

    graphInfo.scrollOffset = 0;
    graphInfo.lastScrollPixAmount = 0;
    
}// end of Graph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{

    title = configFile.readString(
             configFileSection, "title", "Annotation Graph " + (graphNum + 1));

    shortTitle = configFile.readString(
               configFileSection, "short title", "annograph" + (graphNum + 1));

    int configWidth = configFile.readInt(configFileSection, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0
    
    int configHeight = configFile.readInt(configFileSection, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0
    
    backgroundColor = configFile.readColor(
              configFileSection, "background color", new Color(238, 238, 238));

    
}// end of Graph::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of Graph::setSizes
//-----------------------------------------------------------------------------


}//end of class Graph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

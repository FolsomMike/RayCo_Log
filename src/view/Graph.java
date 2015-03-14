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
import java.util.ArrayList;
import java.util.Iterator;
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
    
    String title, shortTitle, objectType;
    int chartGroupNum, chartNum, graphNum;
    int width, height;
    Color backgroundColor;

    int scrollTrackChartGroupNum, scrollTrackChartNum, scrollTrackGraphNum;
    ArrayList<Graph> graphsTrackingThisGraphsScrolling;
    ArrayList<Graph> getGraphsTrackingThisGraphsScrolling(){
                                return (graphsTrackingThisGraphsScrolling); }
    
    //type of graph subclasses
    
    public static final int UNDEFINED_GRAPH = 0;
    public static final int TRACE_GRAPH = 1;
    public static final int ZOOM_GRAPH = 2;
    public static final int MAP3D_GRAPH = 3;
    
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
    graphInfo.lastScrollAmount = 0;
    
}// end of Graph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::update
//
// Updates with new parameters and forces a repaint. The new parameters are
// stored as objects in pValues.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void update(ArrayList <Object> pValues)
{
    

}// end of Graph::update
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getParameters
//
// Retrieves parameters specific to the subclass via objects in an ArrayList.
// Each subclass returns different parameters specific to its functionality.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public ArrayList<Object>getParameters()
{
    
    return(null);

}// end of Graph::getParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::scrollGraph
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
        
}// end of Graph::scrollGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getTrace
//
// Returns Trace pTrace.
//
// Generally overridden by subclasses to return a valid value.
//

public Trace getTrace(int pTrace)
{

    return(null);
    
}// end of Graph::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getNumChildren
//
// Returns the number of child objects.
//
// Generally overridden by subclasses to return a valid value.
//

public int getNumChildren()
{

    return(0);

}// end of Graph::getNumChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setAllChildrenXScale
//
// Sets the display horizontal scale for all children to pScale.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setAllChildrenXScale(double pScale)
{
    

}// end of Graph::setAllChildrenXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildYScale
//
// For child pChildNum, sets the display vertical scale to pScale
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildYScale(int pChildNum, double pScale)
{

}// end of Graph::setChildYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildOffset
//
// For Trace pChildNum, sets the display offset to pOffset.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildOffset(int pChildNum, int pOffset)
{
    
}// end of Graph::setChildOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildBaseLine
//
// For child pChildNum, sets the baseLine value to pBaseLine. This will cause
// the pBaseline value to be shifted to zero when the child is drawn.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildBaseLine(int pChildNum, int pBaseLine)
{   
 
}// end of Graph::setChildBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setChildConnectPoints
//
// For child pChildNum, sets the connectPoints flag. If true, points will be
// connected by a line.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setChildConnectPoints(int pChildNum, boolean pValue)
{

    
}// end of Graph::setChildConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::setVerticalBarAllChildren
//
// Sets a vertical bar to be drawn at the current data insertion location for
// all traces.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void setVerticalBarAllChildren()
{

}// end of Graph::setVerticalBarAllChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList, 
                                                           String pObjectType)
{
    
    if (objectType.equals(pObjectType)){ pObjectList.add(this); }

}// end of Graph::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::updateChild
//
// Plots all data added to the data transfer buffer and erases any data which
// has been marked as erased for pChildNum.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void updateChild(int pChildNum)
{

}// end of Graph::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::addGraphTrackingThisGraphsScrolling
//
// Adds graph pGraph to the list of graphs which should be scolled when this
// graph is scrolled.
//

public void addGraphTrackingThisGraphsScrolling(Graph pGraph)
{

    
    if (graphsTrackingThisGraphsScrolling == null){    
        graphsTrackingThisGraphsScrolling = new ArrayList<>();        
    }
    
    graphsTrackingThisGraphsScrolling.add(pGraph);
    
}// end of Graph::addGraphTrackingThisGraphsScrolling
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::getGraphTrackedForScrolling
//
// Returns the chart group/chart/graph numbers for the graph which this graph
// should track when the former is scrolled.
//
// If no graph is to be tracked, returns null.
//

public GUIDataSet getGraphTrackedForScrolling()
{

    if (scrollTrackChartGroupNum == -1 || scrollTrackChartNum == -1 
                                                || scrollTrackGraphNum == -1){        
        return(null);
    }
    
    GUIDataSet gds = new GUIDataSet();
    
    gds.chartGroupNum = scrollTrackChartGroupNum;
    gds.chartNum = scrollTrackChartNum;
    gds.graphNum = scrollTrackGraphNum;
        
    return(gds);
    
}// end of Graph::getGraphTrackedForScrolling
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::paintChildren
//
// Paints all the child objects on the canvas.
//
// Generally overridden by subclasses to provide appropriate processing.
//

public void paintChildren(Graphics2D pG2)
{
    
}// end of Graph::paintChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Graph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{

    title = configFile.readString(
             configFileSection, "title", "Graph " + (graphNum + 1));

    shortTitle = configFile.readString(
               configFileSection, "short title", "graph" + (graphNum + 1));

    objectType = configFile.readString(
                                    configFileSection, "object type", "graph");
    
    int configWidth = configFile.readInt(configFileSection, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0
    
    int configHeight = configFile.readInt(configFileSection, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0
    
    backgroundColor = configFile.readColor(
              configFileSection, "background color", new Color(238, 238, 238));

    scrollTrackChartGroupNum = configFile.readInt(configFileSection,
                      "chart group number of graph tracked for scrolling", -1);
    scrollTrackChartNum = configFile.readInt(configFileSection,
                            "chart number of graph tracked for scrolling", -1);
    scrollTrackGraphNum = configFile.readInt(configFileSection,
                            "graph number of graph tracked for scrolling", -1);
        
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

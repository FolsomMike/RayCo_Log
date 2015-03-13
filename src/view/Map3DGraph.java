/******************************************************************************
* Title: Map3DGraph.java
* Author: Mike Schoonover
* Date: 03/03/15
*
* Purpose:
*
* This class subclasses a JPanel to display a 3D map.
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
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3DGraph
//

class Map3DGraph extends Graph{

    private Map3D map3D;

    private int xPos, yPos;
    private int xFrom, yFrom, zFrom;
    private int xAt, yAt, zAt;
    private int xUp, yUp, zUp;
    private int rotation;
    private int viewAngle;

    private int mapWidthInDataPoints, mapHeightInDataPoints;
    
    private static final int ANALYSIS_STRETCHX = 1;
    private static final int ANALYSIS_STRETCHY = 1;
    private static final int ANALYSIS_NORMAL_LEVEL = 25;
    private static final int ANALYSIS_WARNING_LEVEL = 65;
    private static final int ANALYSIS_CRITICAL_LEVEL = 100;
        
//-----------------------------------------------------------------------------
// Map3DGraph::Map3DGraph (constructor)
//
//

public Map3DGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
            int pWidth, int pHeight, ChartInfo pChartInfo, IniFile pConfigFile)
{

    super(pChartGroupNum, pChartNum, pGraphNum,
                                     pWidth, pHeight, pChartInfo, pConfigFile);
        
}//end of Chart::Map3DGraph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    addMaps();
    
}// end of Map3DGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::addMaps
//
// Creates and sets up the maps and adds them to the panel.
//

private void addMaps()
{

    map3D = new Map3D(chartGroupNum, chartNum, graphNum,
        width, height,  mapWidthInDataPoints, mapHeightInDataPoints);

    map3D.init();
    map3D.createArrays();
    map3D.fillInputArray(0);

    map3D.setDataPoint(10, 5, 15);

}//end of Map3DGraph::addMaps
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::update
//
// Updates with new 3D view parameters and forces a repaint. The new parameters
// are stored as objects in pValues.
//

@Override
public void update(ArrayList <Object> pValues)
{

    int i = 1;
    
    xPos = (Integer)pValues.get(i++);
    
    yPos = (Integer)pValues.get(i++); yPos = -yPos;

    xFrom = (Integer)pValues.get(i++);
    
    yFrom = (Integer)pValues.get(i++);
    
    zFrom = (Integer)pValues.get(i++);    
    
    xAt = (Integer)pValues.get(i++);
    
    yAt = (Integer)pValues.get(i++);
    
    zAt = (Integer)pValues.get(i++);    
        
    rotation = (Integer)pValues.get(i++);
    
    viewAngle = (Integer)pValues.get(i++);
    
    repaint();

}// end of Map3DGraph::update
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::getParameters
//
// Retrieves parameters specific to this subclass via objects in an ArrayList.
//

@Override
public ArrayList<Object> getParameters()
{

    ArrayList<Object> values = new ArrayList<>();
    
    values.add("Map3DManipulator");
    
    values.add(xPos);
    
    values.add(yPos);
    
    values.add(xFrom);
    
    values.add(yFrom);
    
    values.add(zFrom);
    
    values.add(xAt);
    
    values.add(yAt);
    
    values.add(zAt);

    values.add(rotation);
    
    values.add(viewAngle);
    
    return(values);

}// end of Map3DGraph::getParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);
           
    map3D.paint((Graphics2D)g,
            xFrom, yFrom, zFrom, xAt, yAt, zAt,
            xPos, yPos, viewAngle, rotation, 
            ANALYSIS_STRETCHX, ANALYSIS_STRETCHY,
            true, false, false,
            90, 50, 10);
    
}// end of Map3DGraph::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::addRow
//
// Adds a new row of data to the 3D grid.
//

public void addRow(int[] pDataSet)
{

    
}// end of Map3DGraph::addRow
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::resetAll
//
// Resets all values and child values to default.
//

@Override
public void resetAll()
{
    
    super.resetAll();
    
}// end of Map3DGraph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    configFileSection = 
            "Chart Group " + chartGroupNum + " Chart " + chartNum
                                            + " Graph " + graphNum;

    super.loadConfigSettings();

    double pixelWidthOfGridBlockInRuntimeLayout;
    
    pixelWidthOfGridBlockInRuntimeLayout = configFile.readDouble(
            configFileSection, 
            "pixel width of each grid block in runtime layout", 9.5);
    
    mapWidthInDataPoints = 
       configFile.readInt(configFileSection, "width of map in data points", -1);

    //if value in config file is -1, set such that the graph is filled
    if (mapWidthInDataPoints == -1){
        mapWidthInDataPoints = 
                           (int)(width / pixelWidthOfGridBlockInRuntimeLayout);
    }

    mapHeightInDataPoints = 
      configFile.readInt(configFileSection, "height of map in data points", 12);
    
    //3D map view parameters for runtime layout

    xPos = configFile.readInt(
            configFileSection, "xPos~3D map runtime layout view setting", 0);
    yPos = configFile.readInt(
            configFileSection, "yPos~3D map runtime layout view setting", -54);
    xFrom = configFile.readInt(
             configFileSection, "xFrom~3D map runtime layout view setting", 0);
    yFrom = configFile.readInt(
            configFileSection, "yFrom~3D map runtime layout view setting", 10);
    zFrom = configFile.readInt(
             configFileSection, "zFrom~3D map runtime layout view setting", 5);
    xAt = configFile.readInt(
               configFileSection, "xAt~3D map runtime layout view setting", 0);
    yAt = configFile.readInt(
               configFileSection, "yAt~3D map runtime layout view setting", 0);
    zAt = configFile.readInt(
               configFileSection, "zAt~3D map runtime layout view setting", 0);
    xUp = configFile.readInt(
               configFileSection, "xUp~3D map runtime layout view setting", 0);
    yUp = configFile.readInt(
               configFileSection, "yUp~3D map runtime layout view setting", 0);
    zUp = configFile.readInt(
               configFileSection, "zUp~3D map runtime layout view setting", 1);
    rotation = configFile.readInt(
        configFileSection, "rotation~3D map runtime layout view setting", 180);
    viewAngle = configFile.readInt(
        configFileSection, "viewAngle~3D map runtime layout view setting", 12);

    
    //if xPos is MAX_VALUE, adjust to set left edge of grid to left graph edge
    if (xPos == Integer.MAX_VALUE){
        xPos = (int)((width - 
           (mapWidthInDataPoints * pixelWidthOfGridBlockInRuntimeLayout)) / 2);
        xPos = -xPos;
    }
    
}// end of Map3DGraph::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Map3DGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

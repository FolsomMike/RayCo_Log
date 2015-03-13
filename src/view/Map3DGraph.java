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

    private int numMaps;
    private Map3D[] maps;

    private int xPos = -24, yPos = -54;
    private int xFrom = 0, yFrom = 10, zFrom = 5;
    private int xAt = 0, yAt = 0, zAt = 0;
    private int xUp = 0, yUp = 0, zUp = 1;
    private int rotation = 180;
    private int viewAngle = 12;
    
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

    maps = new Map3D[numMaps];

    for(int i=0; i<maps.length; i++){

        maps[i] = new Map3D(chartGroupNum, chartNum, graphNum,
            width, height, 100, 12);        
        
        maps[i].init();
        maps[i].createArrays();
        maps[i].fillInputArray(0);
        
    }

    maps[0].setDataPoint(10, 5, 15);
    
    
}//end of Map3DGraph::addMaps
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::update
//
// Updates with new parameters and forces a repaint. The new parameters are
// stored as objects in pValues.
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
// Map3DGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);

    for(Map3D map : maps){
           
        map.paint((Graphics2D)g,
                xFrom, yFrom, zFrom, xAt, yAt, zAt,
                xPos, yPos, viewAngle, rotation, 
                ANALYSIS_STRETCHX, ANALYSIS_STRETCHY,
                true, false, false,
                90, 50, 10);
    }
    
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

    numMaps = configFile.readInt(configFileSection, "number of maps", 0);    

}// end of Map3DGraph::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Map3DGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3DGraph
//

class Map3DGraph extends Graph{

    private int numMaps;
    private Map3D[] maps;

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
            width, height, 50, 12);        
        
        maps[i].init();
        maps[i].createArrays();
        maps[i].fillInputArray(0); //debug mks -- set to 0
        
    }

    maps[0].setDataPoint(10, 5, 25);
    
    
}//end of Map3DGraph::addMaps
//-----------------------------------------------------------------------------

/*

    Defaults from C++ code

    private static final int ANALYSIS_AZ = 5;
    private static final int ANALYSIS_DX = -19;
    private static final int ANALYSIS_DY = -110;
    private static final int ANALYSIS_DANGLE = 27;
    private static final int ANALYSIS_DEGREE = 135;
    private static final int ANALYSIS_STRETCHX = 1;
    private static final int ANALYSIS_STRETCHY = 2;
    private static final int ANALYSIS_NORMAL_LEVEL = 25;
    private static final int ANALYSIS_WARNING_LEVEL = 65;
    private static final int ANALYSIS_CRITICAL_LEVEL = 100;
*/


    private static final int ANALYSIS_AZ = 5;
    private static final int ANALYSIS_DX = -19;
    private static final int ANALYSIS_DY = -110;
    private static final int ANALYSIS_DANGLE = 1;
    private static final int ANALYSIS_DEGREE = 200;
    private static final int ANALYSIS_STRETCHX = 1;
    private static final int ANALYSIS_STRETCHY = 1;
    private static final int ANALYSIS_NORMAL_LEVEL = 25;
    private static final int ANALYSIS_WARNING_LEVEL = 65;
    private static final int ANALYSIS_CRITICAL_LEVEL = 100;

//-----------------------------------------------------------------------------
// Map3DGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);

    for(Map3D map : maps){
           
        map.paint((Graphics2D)g,
                ANALYSIS_AZ, ANALYSIS_DX, ANALYSIS_DY,
                ANALYSIS_DANGLE, ANALYSIS_DEGREE, 
                ANALYSIS_STRETCHX, ANALYSIS_STRETCHY,
                true, false, false,
                90, 50, 10);
    }
    
    /*
    
    //default settings for 3D Map in Analysis Window
    const ANALYSIS_AZ = 5, ANALYSIS_DX = -19, ANALYSIS_DY = -110;
    const ANALYSIS_DANGLE = 27, ANALYSIS_DEGREE = 135;
    const ANALYSIS_STRETCHX = 1, ANALYSIS_STRETCHY = 2;
    const ANALYSIS_NORMAL_LEVEL = 25, ANALYSIS_WARNING_LEVEL = 65;
    const ANALYSIS_CRITICAL_LEVEL = 100;
 
    JobInfo->TopoMap.AZ = ANALYSIS_AZ; JobInfo->TopoMap.DX = ANALYSIS_DX;
    JobInfo->TopoMap.DY = ANALYSIS_DY; JobInfo->TopoMap.DAngle = ANALYSIS_DANGLE;
    JobInfo->TopoMap.Degree = ANALYSIS_DEGREE;
    JobInfo->TopoMap.StretchX = ANALYSIS_STRETCHX;
    JobInfo->TopoMap.StretchY = ANALYSIS_STRETCHY;

    tmap->Paint(JobInfo->TopoMap.AZ, JobInfo->TopoMap.DX, JobInfo->TopoMap.DY,
            JobInfo->TopoMap.DAngle, JobInfo->TopoMap.Degree,
            JobInfo->TopoMap.StretchX, JobInfo->TopoMap.StretchY, true, false,
            false, JobInfo->CriticalValue, JobInfo->WarnValue,
            JobInfo->NormalValue);

    
    */
    
    //debug mks
    //paint(Graphics2D pG2, int _az, int _dx, int _dy,
    //            int _dAngle, int _degree, int _StretchX, int _StretchY,
    //            boolean _HiddenSurfaceViewMode, boolean _WireFrameViewMode,
    //            boolean _BirdsEyeViewMode,
    //            int _criticalValue, int _warnValue, int _normalValue)
    //debug mks end
    
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

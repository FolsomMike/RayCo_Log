/******************************************************************************
* Title: Map3DGraph.java
* Author: Mike Schoonover
* Date: 03/03/15
*
* Purpose:
*
* This class subclasses a JPanel to display a 3D map.
*
* A reference to the transfer data buffer, mapBuffer is stored in the class but
* it is not used in the same manner as the dataBuffer reference in the Trace
* class. All the data for the Trace class is stored in the dataBuffer. For the
* 3D map, a local buffer holds data for display refresh. The data is copied
* one slice at a time from the mapBuffer as it is needed, but after copying
* the 3D map uses its internal buffer from there on.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import model.DataFlags;
import model.DataSetIntMultiDim;
import model.DataTransferIntMultiDimBuffer;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3DGraph
//

public class Map3DGraph extends Graph{

    //override to pass on to Map3D
    @Override
    public void setMapBuffer(DataTransferIntMultiDimBuffer pMapBuffer)
        { super.setMapBuffer(pMapBuffer); map3D.setMapBuffer(pMapBuffer); }

    //override to pass on to Map3D
    @Override
    public void setScrollTrackGraphInfo(GraphInfo pG)
        { super.setScrollTrackGraphInfo(pG); map3D.setScrollTrackGraphInfo(pG); }

    private Map3D map3D;

    int numSystems;
    String[] systemNames;
    Color[] systemColors;

    int colorMappingStyle;
    int mapBaselineThreshold;
    Color mapBaselineColor;

    // view parameters used when scanning/inspecting
    // this view must be directly from the side

    Map3DViewParameters normalViewParams = new Map3DViewParameters();

    // view parameters used when graph is expanded for better viewing

    Map3DViewParameters expandedViewParams = new Map3DViewParameters();

    // used to hold the parameters currently in use

    Map3DViewParameters currentViewParams = new Map3DViewParameters();

    //length is the x axis, width is the y axis (o'clock position)
    private int mapLengthInDataPoints;
    public int getMapLengthInDataPoints(){ return(mapLengthInDataPoints); }
    private int mapWidthInDataPoints;
    public int getMapWidthInDataPoints(){ return(mapWidthInDataPoints); }
    private int bufferLengthInDataPoints;
    public int getBufferLengthInDataPoints(){return(bufferLengthInDataPoints);}

    DataSetIntMultiDim mapDataSet;

    private static final int ANALYSIS_STRETCHX = 1;
    private static final int ANALYSIS_STRETCHY = 1;
    private static final int NORMAL_LEVEL = 2;
    private static final int WARNING_LEVEL = 10;
    private static final int CRITICAL_LEVEL = 20;

    public static final int ASSIGN_COLOR_BY_HEIGHT = 0;
    public static final int ASSIGN_COLOR_BY_SYSTEM = 1;

//-----------------------------------------------------------------------------
// Map3DGraph::Map3DGraph (constructor)
//
//

public Map3DGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
                    int pWidth, int pHeight, ChartInfo pChartInfo,
                    IniFile pConfigFile, SharedSettings pSettings)
{

    super(pChartGroupNum, pChartNum, pGraphNum, pWidth, pHeight, pChartInfo,
            pConfigFile, pSettings);

    metaDataSectionName = "Map 3D Graph";

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

    mapDataSet = new DataSetIntMultiDim(mapWidthInDataPoints);
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
        width, height,  mapLengthInDataPoints, mapWidthInDataPoints,
        numSystems, colorMappingStyle,
        mapBaselineThreshold, mapBaselineColor);

    map3D.init();
    map3D.createArrays();
    map3D.resetAll();

    map3D.setSystemInfo(systemNames, systemColors);

}//end of Map3DGraph::addMaps
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::update
//
// Updates with new 3D view parameters and forces a repaint. The new parameters
// are retrieved as objects from pValues.
//

@Override
public void update(ArrayList <Object> pValues)
{

    int i = 1;

    currentViewParams.xPos = (Integer)pValues.get(i++);

    currentViewParams.yPos = (Integer)pValues.get(i++);
    //flip y so higher values towards top of screen
    currentViewParams.yPos = -(currentViewParams.yPos);

    currentViewParams.xFrom = (Integer)pValues.get(i++);

    currentViewParams.yFrom = (Integer)pValues.get(i++);

    currentViewParams.zFrom = (Integer)pValues.get(i++);

    currentViewParams.xAt = (Integer)pValues.get(i++);

    currentViewParams.yAt = (Integer)pValues.get(i++);

    currentViewParams.zAt = (Integer)pValues.get(i++);

    currentViewParams.rotation = (Integer)pValues.get(i++);

    currentViewParams.viewAngle = (Integer)pValues.get(i++);

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

    values.add(currentViewParams.xPos);

    values.add(-(currentViewParams.yPos));

    values.add(currentViewParams.xFrom);

    values.add(currentViewParams.yFrom);

    values.add(currentViewParams.zFrom);

    values.add(currentViewParams.xAt);

    values.add(currentViewParams.yAt);

    values.add(currentViewParams.zAt);

    values.add(currentViewParams.rotation);

    values.add(currentViewParams.viewAngle);

    return(values);

}// end of Map3DGraph::getParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::updateChild
//
// Plots all new data added to the data transfer buffer and erases any data
// which has been marked as erased for pChildNum.
//
// For some Graph subclasses, pChildNum refers to a child plotting object
// such as a Trace. For this class, pChildNum is superfluous as there is always
// only one child map for the graph.
//

@Override
public void updateChild(int pChildNum)
{

    map3D.update((Graphics2D)getGraphics());

    /*int r;
    while((r = mapBuffer.getDataChange(mapDataSet)) != 0){

        map3D.setAndDrawDataRow(
                        (Graphics2D)getGraphics(), mapDataSet.d, mapDataSet.m);

    }*/ //DEBUG HSS

}// end of Map3DGraph::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);

    map3D.paint((Graphics2D)g,
            currentViewParams,
            ANALYSIS_STRETCHX, ANALYSIS_STRETCHY,
            true, false, false,
            CRITICAL_LEVEL, WARNING_LEVEL, NORMAL_LEVEL);

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
// Map3DGraph::setViewParamsToNormalLayout
//
// Sets the current viewing parameters to the normal layout.
//

@Override
public void setViewParamsToNormalLayout()
{

    currentViewParams.setValues(normalViewParams);

}// end of Map3DGraph::setViewParamsToNormalLayout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::setViewParamsToExpandedLayout
//
// Sets the current viewing parameters to the expanded layout.
//

@Override
public void setViewParamsToExpandedLayout()
{

    currentViewParams.setValues(expandedViewParams);

}// end of Map3DGraph::setViewParamsToExpandedLayout
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

    resetData();

    repaint();

}// end of Map3DGraph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::resetData
//
// Resets all data to zero and all flags to default. Resets all buffer pointers
// to starting positions.
//

public void resetData()
{

    super.resetAll();

    map3D.resetAll();

    if (mapBuffer!=null) { mapBuffer.reset(); }

}// end of Map3DGraph::resetData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::setChildCanvasSizeToMatchPanel
//
// Sets the canvas size in the map or other display to match the containing
// panel. This can be done when the panel size is changed so that content can
// be centered the display.
//
// The width and height are retrieved directly from the panel component using
// getWidth and getHeight to make sure the actual panel size is used.
//

@Override
public void setChildCanvasSizeToMatchPanel()
{

    map3D.setCanvasSize(getWidth(), getHeight());

}// end of Map3DGraph::setChildCanvasSizeToMatchPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::setChildCanvasSize
//
// Sets the canvas size in the map or other display to pWidth, pHeight.
// This can be done when the panel size is changed so that content can be
// centered the display.
//
// If either value is set to Integer.MAX_VALUE, that value is not changed.
//

@Override
public void setChildCanvasSize(int pWidth, int pHeight)
{

    map3D.setCanvasSize(pWidth, pHeight);

}// end of Map3DGraph::setChildCanvasSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::animate
//
// Animates the rotation of the graph from -25 to +25 degrees from its
// starting point.
//

@Override
public void animate()
{

    if(animationDirection == 0){

        animationCount++;

        //reverse direction when max reached
        if(animationCount > 24){ animationDirection = 1; }

    }else{

        animationCount--;

        //reverse direction when min reached
        if(animationCount < -24){ animationDirection = 0; }

    }

    currentViewParams.rotation = expandedViewParams.rotation + animationCount;

    repaint();

}// end of Map3DGraph::animate
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::parseColorMappingStyle
//
// Converts the descriptive string loaded from the config file for the color
// mapping style (color by system, color by height, etc.) into the
// corresponding constant.
//

private void parseColorMappingStyle(String pValue)
{

    switch (pValue) {
         case "assign by height":
             colorMappingStyle = ASSIGN_COLOR_BY_HEIGHT; break;
         case "assign by system" :
             colorMappingStyle = ASSIGN_COLOR_BY_SYSTEM; break;
         default :
             colorMappingStyle = ASSIGN_COLOR_BY_SYSTEM; break;
    }

}// end of Channel::parseColorMappingStyle
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::saveSegment
//
// Saves all of the zoom data.
//

@Override
public void saveSegment(BufferedWriter pOut) throws IOException
{

    super.saveSegment(pOut);

    map3D.saveSegment(pOut);

}//end of Map3DGraph::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::loadSegment
//
// Loads all of the map data.
//

@Override
public String loadSegment(BufferedReader pIn, String pLastLine)
        throws IOException
{

    String line = super.loadSegment(pIn, pLastLine);

    line = map3D.loadSegment(pIn, line, fileSection);

    repaint();

    return line;

}//end of Map3DGraph::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    fileSection =
            "Chart Group " + chartGroupNum + " Chart " + chartNum
                                            + " Graph " + graphNum;

    super.loadConfigSettings();

    double pixelWidthOfGridBlockInRuntimeLayout;

    pixelWidthOfGridBlockInRuntimeLayout = configFile.readDouble(
            fileSection,
            "pixel width of each grid block in runtime layout", 9.5);

    mapLengthInDataPoints =
      configFile.readInt(fileSection, "length of map in data points", 12);

    //if value in config file is -1, set such that the graph is filled
    if (mapLengthInDataPoints == -1){
        mapLengthInDataPoints =
                           (int)(width / pixelWidthOfGridBlockInRuntimeLayout);
    }

    mapWidthInDataPoints =
       configFile.readInt(fileSection, "width of map in data points", -1);

    bufferLengthInDataPoints = configFile.readInt(fileSection,
                "length of data buffer in data points", mapLengthInDataPoints);

    //3D map view parameters for runtime layout

    normalViewParams.loadConfigSettings(
                                    configFile, fileSection, "runtime");

    //3D map view parameters for when graph is expanded for better viewing

    expandedViewParams.loadConfigSettings(
                                    configFile, fileSection, "expanded");

    //if xPos is MAX_VALUE, adjust to set left edge of grid to left graph edge
    //this is only done for the runtime view...adjustments to expanded view
    //must be set in the config file

    if (normalViewParams.xPos == Integer.MAX_VALUE){
        normalViewParams.xPos = (int)((width -
           (mapLengthInDataPoints * pixelWidthOfGridBlockInRuntimeLayout)) / 2);
        normalViewParams.xPos = -(normalViewParams.xPos);
    }


     String s = configFile.readString(fileSection,
                                    "color mapping style", "assign by system");

    parseColorMappingStyle(s);

    mapBaselineThreshold = configFile.readInt(fileSection,
                                                 "map baseline threshold", 4);

    mapBaselineColor = configFile.readColor(fileSection,
                                       "map baseline color", Color.LIGHT_GRAY);

    //start out using the runtime view parameters
    currentViewParams.setValues(normalViewParams);

    loadSystemsConfigSettings(fileSection);

}// end of Map3DGraph::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::loadSystemsConfigSettings
//
// Loads settings for the map systems from configFile.
//

void loadSystemsConfigSettings(String fileSection)
{

    numSystems  =
       configFile.readInt(fileSection, "number of systems on map", 0);

    if (numSystems <= 0) { return; }

    systemNames = new String[numSystems];
    systemColors = new Color[numSystems];

    for(int i=0; i<numSystems; i++){

        systemNames[i] = configFile.readString(
                      fileSection, "system " + i + " name", "undefined");

        systemColors[i] = configFile.readColor(
                           fileSection, "system " + i + " color", null);

    }

}// end of Map3DGraph::loadSystemsConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DGraph::scrollGraph
//
// Scrolls the graph if appropriate.
//

@Override
public void scrollGraph (int pShiftAmount)
{

    //WIP HSS// 20 pixels subtracted looks good, but I used guesswork to figure
    //it out there has to be some way to do calculate it. Also if Map is working
    //faster than other graphs, gui errors occur
    if (map3D.getLastDrawnX()<map3D.getXMaxPix()-20) { return; }//DEBUG HSS/

    super.scrollGraph(pShiftAmount);

}// end of Map3DGraph::scrollGraph
//-----------------------------------------------------------------------------


}//end of class Map3DGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------


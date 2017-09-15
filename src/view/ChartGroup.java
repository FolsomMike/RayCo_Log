/******************************************************************************
* Title: ChartGroup.java
* Author: Mike Schoonover
* Date: 01/45/15
*
* Purpose:
*
* This class subclasses a JPanel to contain several Charts.
*
* Display Sizing
*
* The graph width should never be so large that the main window is larger than
* the display size. When that happens, the graph will be the specified size
* but will be truncated for some reason by the window...Java makes the window
* too small. This will cause the scrolling to fail.
*
* This also happens if the user manually reduces the size of the window and
* if the calibration panels are displayed and they increase the window size
* to larger than the display size.
*
* Currently, these problems are solved by setting the graphs small enough that
* the main window leaves space on the display at the right edge...enough that
* when the largest calibration panel is displayed, the entire window still
* fits on the screen.
*
* In the future, it would be better to catch the window resizing (untested code
* already added to Chart class) and shrink or grow the size of the graphs to
* accommodate the changing size of the calibration panel. The methods
* updateDimensions have already be added to various classes (not fully tested)
* which are meant to be called after changing the size of the graphs.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*
*/

package view;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChartGroup
//

class ChartGroup extends JPanel{

    private final IniFile configFile;

    private String title, shortTitle, objectType;
    private final int chartGroupNum;
    private int graphWidth, graphHeight;
    private int numCharts;
    private Chart charts[];

    private final Dimension usableScreenSize;

    ActionListener parentActionListener;

//-----------------------------------------------------------------------------
// ChartGroup::ChartGroup (constructor)
//
//

public ChartGroup(int pChartGroupNum, IniFile pConfigFile,
             Dimension pUsableScreenSize, ActionListener pParentActionListener)
{

    chartGroupNum = pChartGroupNum; configFile = pConfigFile;

    usableScreenSize = pUsableScreenSize;

    parentActionListener = pParentActionListener;

}//end of ChartGroup::ChartGroup (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    loadConfigSettings();

    createCharts();

    add(Box.createVerticalGlue()); //force charts towards top of display

}// end of Chart::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//

public void updateDimensions()
{

    for (Chart chart : charts){ chart.updateDimensions(); }

}// end of ChartGroup::updateDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::createCharts
//
// Creates and configures the charts.
//

private void createCharts()
{

    charts = new Chart[numCharts];

    for (int i = 0; i<charts.length; i++){
        charts[i] = new Chart(chartGroupNum, i, graphWidth, graphHeight,
                                            parentActionListener, configFile);
        charts[i].init();
        add(charts[i]);
    }

}// end of ChartGroup::createCharts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupNum;

    title = configFile.readString(
                       section, "title", "Chart Group " + (chartGroupNum + 1));

    shortTitle = configFile.readString(
                        section, "short title", "chgrp" + (chartGroupNum + 1));

    objectType = configFile.readString(section, "object type", "chart group");

    numCharts = configFile.readInt(section, "number of charts", 0);

    graphWidth =
              configFile.readInt(section, "default width for all graphs", 500);

    //See notes at the top of ChartGroup class titled "Display Sizing" for
    //important info about setting the size of the charts.

    //if -1, set graphWidth to fill the screen
    if(graphWidth == -1){ graphWidth = usableScreenSize.width - 350; }

    graphHeight = configFile.readInt(
                                  section, "default height for all graphs", 0);

    //if -1, set graphHeight to fill the screen
    if(graphHeight == -1){ graphHeight = usableScreenSize.height - 50; }

}// end of Chart::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::repaintGroup
//
// Forces all charts to be repainted.
//

public void repaintGroup()
{

    invalidate();

}// end of Chart::repaintGroup
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::resetAll
//
// For all traces of all charts - resets all data to zero and all flags to
// DEFAULT_FLAGS. Resets dataInsertPos to zero.
//

public void resetAll()
{

    for (Chart chart: charts){ chart.resetAll(); }

}// end of ChartGroup::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::setAllChartAllTraceXScale
//
// Sets the display horizontal scale for all traces of all charts to pScale.
//

public void setAllChartAllTraceXScale(double pScale)
{

    for (Chart chart : charts) { chart.setAllTraceXScale(pScale);}

}// end of ChartGroup::setAllChartAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::getChart
//
// Returns Chart pChart.
//

public Chart getChart(int pChart)
{

    if (pChart < 0 || pChart >= charts.length){ return(null); }

    return(charts[pChart]);

}// end of ChartGroup::getChart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//

public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList,
                                                           String pObjectType)
{

    if (objectType.equals(pObjectType)){ pObjectList.add(this); }

    for (Chart chart : charts) {
        chart.scanForGUIObjectsOfAType(pObjectList, pObjectType);
    }

}// end of ChartGroup::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::getGraph
//
// Returns the reference to graph pGraph of pChart.
//

public Graph getGraph(int pChart, int pGraph)
{

    if (pChart < 0 || pChart >= charts.length){ return(null); }

    return( charts[pChart].getGraph(pGraph) );

}// end of ChartGroup::getGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::getTrace
//
// Returns the reference to trace pTrace of pGraph of pChart.
//

public Trace getTrace(int pChart, int pGraph, int pTrace)
{

    if (pChart < 0 || pChart >= charts.length){ return(null); }

    return( charts[pChart].getTrace(pGraph, pTrace) );

}// end of ChartGroup::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::updateAnnotationGraphs
//
// Plots data added to annoBuffer and/or erases any data which has been
// flagged as erased for the annotation graphs of all charts.
//

public void updateAnnotationGraphs()
{

    for(Chart chart: charts){ chart.updateAnnotationGraph(); }

}// end of ChartGroup::updateAnnotationGraphs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::updateChild
//
// Plots all data new added to the transfer data buffer and erases any data
// which has been marked as erased for pTrace of pGraph of pChart.
//

public void updateChild(int pChart, int pGraph, int pTrace)
{

    charts[pChart].updateChild(pGraph, pTrace);

}// end of ChartGroup::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::expandChartHeight
//
// Expands the height of the chart pChart while minimizing the height of all
// other charts in the group by setting their visibility false.
//
// Only the graph pGraph of the chart pChart is expanded. The other graphs in
// pChart are left the same size.
//

public void expandChartHeight(int pChart, int pGraph)
{

    Chart chart = charts[pChart];
    Graph graph = chart.getGraph(pGraph);

    //set all other charts' graphs' visible flag to false so their graphs
    //will be minimized

    //add up all minimized graph heights so chart to be expanded can be resized
    //to fill space available after the graphs are minimized

    int allMinimizedGraphHeights = 0;

    for(Chart sChart : charts){


        if(sChart.getChartNum() != pChart){

            sChart.setGraphsVisible(false);

            allMinimizedGraphHeights += sChart.getGraphHeights(-1);
        }

    }

    int heightOfGraphToBeExpanded = chart.getGraphHeights(pGraph);

    int newHeight = heightOfGraphToBeExpanded + allMinimizedGraphHeights;

    //add the space made available to the existing height to calculate new
    chart.setGraphHeight(pGraph, newHeight);

    graph.setChildCanvasSize(Integer.MAX_VALUE, newHeight);

    //adjust the viewing parameters for the new layout
    chart.setViewParamsToExpandedLayout(pGraph);

}//end of ChartGroup::expandChartHeight
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::setNormalChartHeight
//
// Sets the height of the chart pChart to normal height while maximizing the
// height of all other charts in the group by setting their visibility back to
// the value specified in the config file for each graph.
//
// Only the graph pGraph of the chart pChart is changed. The other graphs in
// pChart are left the same size.
//

public void setNormalChartHeight(int pChart, int pGraph)
{

    Chart chart = charts[pChart];
    Graph graph = chart.getGraph(pGraph);

    //set all other charts' graphs' visible flag to their setting as loaded
    //from the config file

    for(Chart sChart : charts){
        sChart.setGraphsVisible(sChart.getSpecifiedGraphsVisible());
    }

    int newHeight = charts[pChart].getGraphHeights(pGraph);

    //set the height back to the normal height specified in the config file
    chart.setGraphHeight(pGraph, newHeight);

    graph.setChildCanvasSize(Integer.MAX_VALUE, newHeight);

    //adjust the viewing parameters for the new layout
    charts[pChart].setViewParamsToNormalLayout(pGraph);

}//end of ChartGroup::setNormalChartHeight
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::animateGraph
//
// Animates graph pGraph of chart pChart. What the graph does for animation is
// dependent on the type of graph.
//

public void animateGraph(int pChart, int pGraph)
{

    charts[pChart].getGraph(pGraph).animate();

}//end of ChartGroup::animateGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile(IniFile pCalFile)
{

    String section = "Chart Group " + chartGroupNum;

    // call each chart to load its data
    for (Chart c : charts) { c.saveCalFile(pCalFile); }

}//end of ChartGroup::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

public void saveCalFile(IniFile pCalFile)
{

    String section = "Chart Group " + chartGroupNum;

    pCalFile.writeString(section, "Chart Group Test Value", "Test");

    // call each chart to save its data
    for (Chart c : charts) { c.saveCalFile(pCalFile); }

}//end of ChartGroup::saveCalFile
//-----------------------------------------------------------------------------


}//end of class ChartGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

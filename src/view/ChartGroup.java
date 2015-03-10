/******************************************************************************
* Title: ChartGroup.java
* Author: Mike Schoonover
* Date: 01/45/15
*
* Purpose:
*
* This class subclasses a JPanel to contain several Charts.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import controller.GUIDataSet;
import java.util.ArrayList;
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChartGroup
//

class ChartGroup extends JPanel{

    private IniFile configFile;
    
    private String title, shortTitle, objectType;
    private int chartGroupNum;
    private int graphWidth, graphHeight;        
    private int numCharts;
    private Chart charts[];
    
//-----------------------------------------------------------------------------
// ChartGroup::ChartGroup (constructor)
//
//

public ChartGroup()
{

}//end of ChartGroup::ChartGroup (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pTitle is the text title for the graph.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init(int pChartGroupNum, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; configFile = pConfigFile;
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    
    loadConfigSettings();
        
    createCharts();

    add(Box.createVerticalGlue()); //force charts towards top of display
    
/*    
    //create chart for Longitudinal
    charts[LONG_CHART] = new Chart();
    charts[LONG_CHART].init(
           "Longitudinal", LONG_CHART, 2, true, 50, CHART_WIDTH, CHART_HEIGHT);
    panel.add(charts[LONG_CHART]);

    //create chart for Transverse
    charts[TRANS_CHART] = new Chart();
    charts[TRANS_CHART].init(
             "Transverse", TRANS_CHART, 2, true, 50, CHART_WIDTH, CHART_HEIGHT);
    panel.add(charts[TRANS_CHART]);
  
    //sample trace draws the sample points without connecting them
    charts[TRANS_CHART].setTraceConnectPoints(0, SAMPLE_TRACE, false);

    //create chart for Transverse
    charts[WALL_CHART] = new Chart();
    charts[WALL_CHART].init(
                    "Wall", WALL_CHART, 1, false, 0, CHART_WIDTH, CHART_HEIGHT);
    panel.add(charts[WALL_CHART]);
    
  */  
    
    
}// end of Chart::init
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
        charts[i] = new Chart();
        charts[i].init(chartGroupNum, i, graphWidth, graphHeight, configFile);
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
    
    graphWidth = configFile.readInt(section, "default width for all graphs", 0);

    graphHeight = configFile.readInt(
                                  section, "default height for all graphs", 0);

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
// Plots all data added to the transfer data buffer and erases any data which
// has been marked as erased for pTrace of pGraph of pChart.
//

public void updateChild(int pChart, int pGraph, int pTrace)
{

    charts[pChart].updateChild(pGraph, pTrace);

}// end of ChartGroup::updateChild
//-----------------------------------------------------------------------------


}//end of class ChartGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import javax.swing.*;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChartGroup
//

class ChartGroup extends JFrame
{

    private final IniFile configFile;
    private final SharedSettings sharedSettings;

    private String title, shortTitle, objectType;
    private final int chartGroupNum;
    private int graphWidth, graphHeight;
    public int getGraphWidth() { return graphWidth; }
    public int getGraphHeight() { return graphHeight; }
    private int numCharts;
    private Chart charts[];

    private Dimension totalScreenSize, usableScreenSize;

    private ActionListener parentActionListener;
    private WindowListener windowListener;
    
    private JPanel mainPanel;
    private ControlsPanel controlsPanel;

//-----------------------------------------------------------------------------
// ChartGroup::ChartGroup (constructor)
//
//

public ChartGroup(int pChartGroupNum, IniFile pConfigFile,
                    SharedSettings pSettings,
                    ActionListener pParentActionListener, 
                    WindowListener pListener)
{
    
    super("Universal Chart");

    chartGroupNum = pChartGroupNum;

    configFile = pConfigFile; sharedSettings = pSettings;

    parentActionListener = pParentActionListener;
    
    windowListener = pListener;

}//end of ChartGroup::ChartGroup (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Chart::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    setUpFrame();

    loadConfigSettings();
    
    //create user interface: buttons, displays, etc.
    setupGui();
    
    //arrange all the GUI items
    pack();
    
    //display the main frame
    setVisible(true);

}// end of Chart::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::getAllValuesFromCurrentControlPanel
//

public ArrayList<Object> getAllValuesFromCurrentControlPanel()
{

    return controlsPanel.getAllValuesFromCurrentControlPanel();

}// end of ChartGroup::getAllValuesFromCurrentControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::markSegmentStart
//
// Instructs children to mark last retrieved data as segment start.
//

public void markSegmentStart()
{
    
    for (Chart c : charts){ c.markSegmentStart();  }
    
}//end of ChartGroup::markSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::markSegmentEnd
//
// Instructs children to mark last retrieved data as segment end.
//

public void markSegmentEnd()
{
    
    for (Chart c : charts){ c.markSegmentEnd();  }

}//end of ChartGroup::markSegmentEnd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::isSegmentStarted
//
// Checks to see if a segment has been started and thus may have data which
// needs to be saved.
//

public boolean isSegmentStarted()
{

    for (Chart c : charts){ if (c.isSegmentStarted()) { return(true);} }

    return(false);

}//end of ChartGroup::isSegmentStarted
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::updateChildren
//
// Instructs all children listening for transfer buffer changes to check for
// changes and update.
//

public void updateChildren()
{

    for (Chart c : charts) { c.updateChildren(); }

}// end of ChartGroup::updateChildren
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
    
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    mainPanel.add(panel);

    for (int i = 0; i<charts.length; i++){
        charts[i] = new Chart(chartGroupNum, i, graphWidth, graphHeight,
                            parentActionListener, configFile, sharedSettings);
        charts[i].init();
        panel.add(charts[i]);
    }

}// end of ChartGroup::createCharts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::displayCalibrationPanel
//
// Displays a calibration panel appropriate for pChartNum of pChartGroupNum with
// name of pPanelName with all channels in pChannelList displayed on the panel.
//
// The ChannelInfo objects in list pChannelList provide the necessary
// information to link the GUI controls to the channels and traces.
//

public void displayCalibrationPanel(int pChartNum, String pPanelTitle, 
                                        ArrayList<ChannelInfo> pChannelList)
{

    LinkedHashSet<String> groupTitles = getListOfGroups(pChannelList);
    ArrayList<Threshold[]> thresholds = getThresholdsForChart(pChartNum);

    Chart chart = charts[pChartNum];
    controlsPanel.displayCalibrationPanel(chartGroupNum, pChartNum, chart,
                                            pPanelTitle, pChannelList, 
                                            groupTitles, thresholds, 
                                            new ArrayList<>());
    
    pack();

}//end of ChartGroup::displayCalibrationPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::displayControlsPanel
//
// Displays the controls panel, which contains job #, scan speed, etc.
//

public void displayControlsPanel()
{

    //only the main window is allowed to display the controls panel
    controlsPanel.displayControlsPanel();
    
    pack();

}//end of ChartGroup::displayControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::setChartVisible
//
// Sets the specified Chart visible or hidden.
//

public void setChartVisible(int pChartNum, boolean pVisible)
{

    charts[pChartNum].setChartVisible(pVisible);

}//end of ChartGroup::setChartVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::setupGUI
//
// Sets up the user interface on the mainPanel: buttons, displays, etc.
//

private void setupGui()
{

    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));

    controlsPanel = new ControlsPanel(parentActionListener, sharedSettings);
    controlsPanel.init();
    mainPanel.add(controlsPanel);
    
    createCharts();

    add(Box.createVerticalGlue()); //force charts towards top of display

}// end of ChartGroup::setupGui
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
// ChartGroup::refreshControlsPanel
//

public void refreshControlsPanel()
{

    controlsPanel.refresh();

}// end of ChartGroup::refreshControlsPanel
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
// ChartGroup::getListOfGroups
//
// Returns a list of the different unique groups to which the different
// channels in pChannelList have been assigned.
//

private LinkedHashSet<String> getListOfGroups(
                                        ArrayList<ChannelInfo> pChannelList)
{

    //use a Set as that automatically rejects duplicates
    LinkedHashSet<String> groups = new LinkedHashSet<>();

    ListIterator iter = pChannelList.listIterator();

    while(iter.hasNext()){
        ChannelInfo info = (ChannelInfo)iter.next();
        groups.add(info.calPanelGroup);
    }

    return(groups);

}//end of ChartGroup::getListOfGroups
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::getThresholdsForChart
//
// Returns an array of thresholds for pChartNum.
//

private ArrayList<Threshold[]> getThresholdsForChart(int pChartNum)
{

    return charts[pChartNum].getThresholds();

}//end of ChartGroup::getThresholdsForChart
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
// ChartGroup::getGraphParameters
//
// Retrieves parameters for graph specified by
//                                          pChartGroupNum/pChartNum/pGraphNum.
//
// The number and type of parameters is specific to each Graph subclass.
//

public ArrayList<Object> getGraphParameters(int pChartNum, int pGraphNum)
{

    return getGraph(pChartNum, pGraphNum).getParameters();

}// end of ChartGroup::getGraphParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::getScreenSize
//
// Retrieves the current screen size along with the actual usable vertical
// size after subtracting the size of the taskbar.
//

public void getScreenSize()
{
    
    totalScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

    //height of the task bar
    Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(
                                        getGraphicsConfiguration());
    int taskBarHeight = scnMax.bottom;

    usableScreenSize = new Dimension(
                  totalScreenSize.width, totalScreenSize.height-taskBarHeight);

}// end of ChartGroup::getScreenSize
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
// ChartGroup::updateGraphYOffset
//
// Updates the specified graph's y-offset value.
//

public void updateGraphYOffset(int pChart, int pGraph, int pOffset)
{

    charts[pChart].updateGraphYOffset(pGraph, pOffset);

}// end of ChartGroup::updateGraphYOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::repaintChild
//
// Repaints pGraph of pChart.
//

public void repaintChild(int pChart, int pGraph)
{

    charts[pChart].repaintChild(pGraph);

}// end of ChartGroup::repaintChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::updateThreshold
//
// Updates the specified threshold.
//

public void updateThreshold(int pChart, int pGraph, int pThres, int pLvl)
{
    
    
    charts[pChart].updateThreshold(pGraph, pThres, pLvl);

}// end of ChartGroup::updateThreshold
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
// ChartGroup::setUpFrame
//
// Sets various options and styles for the frame.
//

public void setUpFrame()
{

    //add a JPanel to the frame to provide a familiar container
    mainPanel = new JPanel();
    setContentPane(mainPanel);

    if (windowListener!=null) { addWindowListener(windowListener); }

    //turn off default bold for Metal look and feel
    UIManager.put("swing.boldMetal", Boolean.FALSE);

    //force "look and feel" to Java style
    try {
        UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        }
    catch (ClassNotFoundException | InstantiationException |
            IllegalAccessException | UnsupportedLookAndFeelException e) {
        System.out.println("Could not set Look and Feel");
        }

    //do not auto exit on close - shut down handled by the timer function
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    
    setTitle(sharedSettings.appTitle);

    getScreenSize();

}// end of ChartGroup::setUpFrame
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
// ChartGroup::shutDown
//
// Hides and disposes all resources assiciated with this group.
//

public void shutDown() {

    //dispose of this frame
    setVisible(false);
    dispose();

}//end of ChartGroup::shutDown
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
    for (Chart c : charts) { c.loadCalFile(pCalFile); }

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

//-----------------------------------------------------------------------------
// ChartGroup::saveSegment
//

public void saveSegment(BufferedWriter pOut)  throws IOException
{

    pOut.write("[Chart Group]"); pOut.newLine();
    pOut.write("Chart Group Index=" + chartGroupNum); pOut.newLine();
    pOut.newLine();

    // call each chart to save its data
    for (Chart c : charts) { c.saveSegment(pOut); }

}//end of ChartGroup::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::loadSegment
//
// Loads segments' data from pFile.
//

public String loadSegment(BufferedReader pIn, String pLastLine)
                                                            throws IOException
{

    //handle entries for the chart group itself
    String line = processChartGroupEntries(pIn, pLastLine);

    // call each chart to load its data
    for (Chart c : charts) { line = c.loadSegment(pIn, line); }
    
    //DEBUG HSS//  test code to use width to first graph of first chart
    graphWidth = charts[0].getGraphWidth();
    for (Chart c : charts) { 
        c.updateGraphDimensions(graphWidth, c.getGraphHeight());
    }
    //DEBUG HSS// end

    repaint();

    return line;

}//end of ChartGroup::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::open3DMapManipulatorControlPanel
//
// Displays a control panel for manipulating 3D maps.
//
// The chart group and chart number which sent the command to open the
// panel will be appended to pActionCommand from whence it will be extracted
// to link the panel to the appropriate chart.
//

public void open3DMapManipulatorControlPanel(int pChartNum, int pGraphNum)
{
    
    ArrayList<Object> graphParams = getGraphParameters(pChartNum, pGraphNum);

    controlsPanel.display3DMapManipulatorControlPanel(chartGroupNum, 
                                                        pChartNum, graphParams);

    pack();

}//end of ChartGroup::open3DMapManipulatorControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartGroup::processChartGroupEntries
//
// Processes the entries for the chart group itself via pIn.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//
// The [ChartGroup] tag may or may not have already been read from the file by
// the code handling the previous section.  If it has been read, the line
// containing the tag should be passed in via pLastLine.
//

private String processChartGroupEntries(BufferedReader pIn, String pLastLine)
                                                            throws IOException

{

    String line;
    boolean success = false;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //if pLastLine contains the [Chart Group] tag, then section found else read
    //until end of file reached or next "[Chart Group]" section tag reached

    if (Tools.matchAndParseString(pLastLine, "[Chart Group]", "", matchSet)) {
        success = true;
    } //tag already found
    else {
        while ((line = pIn.readLine()) != null){  //search for tag
            if (Tools.matchAndParseString(
                                        line, "[Chart Group]", "", matchSet)){
                success = true; break;
            }
        }//while
    }//else

    if (!success) {
        throw new IOException(
            "The file could not be read - section not found for Chart Group "
                                                            + chartGroupNum);
    }

    //set defaults
    int chartGroupIndexRead = -1;

    //scan the first part of the section and parse its entries
    //these entries apply to the chart group itself

    success = false;
    while ((line = pIn.readLine()) != null){

        //stop when next section tag reached (will start with [)
        if (Tools.matchAndParseString(line, "[", "",  matchSet)){
            success = true; break;
        }

        //catch the "Chart Group Index" entry - if not found, default to -1
        if (Tools.matchAndParseInt(line, "Chart Group Index", -1,  matchSet)) {
            chartGroupIndexRead = matchSet.rInt1;
        }

    }// while ((line = pIn.readLine()) != null)

    if (!success) {
        throw new IOException(
        "The file could not be read - missing end of section for Chart Group "
                                                           + chartGroupNum);
    }

    //if the index number in the file does not match the index number for this
    //chart group, abort the file read

    if (chartGroupIndexRead != chartGroupNum) {
        throw new IOException(
            "The file could not be read - section not found for Chart Group "
                                                             + chartGroupNum);
    }

    return(line); //should be "[xxxx]" tag on success, unknown value if not

}//end of Viewer::processChartGroupEntries
//-----------------------------------------------------------------------------

}//end of class ChartGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

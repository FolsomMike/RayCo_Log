/******************************************************************************
* Title: MainView.java
* Author: Mike Schoonover
* Date: 11/28/14
*
* Purpose:
*
* This class is the Main View in a Model-View-Controller architecture.
* It creates and handles all GUI components.
* It knows about the Model, but not the Controller.
*
* There may be many classes in the view package which handle different aspects
* of the GUI.
*
* All GUI control events, including Timer events are caught by this object
* and passed on to the "Controller" object pointed by the class member
* "eventHandler" for final handling.
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------

import controller.EventHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mksystems.mswing.MFloatSpinner;
import model.DataTransferIntBuffer;
import model.DataTransferIntMultiDimBuffer;
import model.DataTransferSnapshotBuffer;
import model.MainDataClass;
import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
// class MainView
//

public class MainView implements ActionListener, WindowListener, ChangeListener
{

    IniFile configFile;
    SharedSettings sharedSettings;

    private int numChartGroups;
    private ChartGroup chartGroups[];

    private JFrame mainFrame;
    private JPanel mainPanel;

    DeviceLog deviceLog = null;

    private final MainDataClass aDataClass;

    private MainMenu mainMenu;

    ControlsPanel controlsPanel;

    private JTextField dataVersionTField;
    private JTextField dataTArea1;
    private JTextField dataTArea2;

    private JTextField timeScaleInput;
    private JTextField upSampleMultiplierInput;
    private JTextField sampleFreqInput;
    private JComboBox sampleFreqUnitsInput;

    private JCheckBox displaySamplesInput;
    private JCheckBox displayZeroStuffedWaveFormInput;
    private JCheckBox displayFilteredWaveFormInput;
    private JTextArea filterCoeffInput;
    private MFloatSpinner filteredOutputScaling;

    private GuiUpdater guiUpdater;
    private Log log;
    private ThreadSafeLogger tsLog;
    private JobInfo jobInfo;
    private ChooseJob chooseJob;
    private NewJob newJob;
    private Help help;
    private About about;
    private Monitor monitor;

    private Xfer xfer;

    private javax.swing.Timer mainTimer;
    public javax.swing.Timer getMainTimer() { return mainTimer; }

    boolean animateGraph = false;
    int animateGraphTimer = 0;

    private final EventHandler eventHandler;
    
    private PieceInfo pieceIDInfo;

    private Font blackSmallFont, redSmallFont;
    private Font redLargeFont, greenLargeFont, yellowLargeFont, blackLargeFont;

    private JLabel statusLabel, infoLabel;
    private JLabel progressLabel;

    private Dimension totalScreenSize, usableScreenSize;

    private static final int CHART_WIDTH = 1000; //1670 for LG screen at RGNDT
    private static final int CHART_HEIGHT = 100;

    public static final int NUM_CHARTS = 3;

    public static final int LONG_CHART = 0;
    public static final int TRANS_CHART = 1;
    public static final int WALL_CHART = 2;

    public static final int SAMPLE_TRACE = 0;

    public static final int GRAPH_NUM_TO_EXPAND = 0;

//-----------------------------------------------------------------------------
// MainView::MainView (constructor)
//

public MainView(EventHandler pEventHandler, MainDataClass pADataClass,
                    SharedSettings pSharedSettings, IniFile pConfigFile)
{

    eventHandler = pEventHandler;
    aDataClass = pADataClass;
    sharedSettings = pSharedSettings;
    configFile = pConfigFile;

}//end of MainView::MainView (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    loadConfigSettings();

    setupMainFrame();

    xfer = new Xfer();

    mainFrame.setTitle(sharedSettings.appTitle);

    //create a window for displaying messages and an object to handle updating
    //it in threadsafe manner
    log = new Log(mainFrame); log.setLocation(230, 0);

    tsLog = new ThreadSafeLogger(log.textArea);

    //create an object to handle thread safe updates of GUI components
    guiUpdater = new GuiUpdater(mainFrame);
    guiUpdater.init();

    tsLog.appendLine("Hello"); tsLog.appendLine("");

    //add a menu to the main form, passing this as the action listener
    mainMenu = new MainMenu(this, sharedSettings); mainMenu.init();
    mainFrame.setJMenuBar(mainMenu);

    //create various fonts for use by the program
    createFonts();
    
    //create an object to hold info about each piece
    pieceIDInfo = new PieceInfo(mainFrame, sharedSettings.jobPathPrimary,
                                    sharedSettings.jobPathSecondary, 
                                    sharedSettings.currentJobName, 
                                    sharedSettings.currentJobNamePathFriendly, 
                                    this, false, sharedSettings.mainFileFormat);
    pieceIDInfo.init();

    //create user interface: buttons, displays, etc.
    setupGui();

    //arrange all the GUI items
    mainFrame.pack();

    //display the main frame
    mainFrame.setVisible(true);

}// end of MainView::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setupMainFrame
//
// Sets various options and styles for the main frame.
//

public void setupMainFrame()
{

    mainFrame = new JFrame("Universal Chart");

    //add a JPanel to the frame to provide a familiar container
    mainPanel = new JPanel();
    mainFrame.setContentPane(mainPanel);

    mainFrame.addWindowListener(this);

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
    mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    getScreenSize();

}// end of MainView::setupMainFrame
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
                                        mainFrame.getGraphicsConfiguration());
    int taskBarHeight = scnMax.bottom;

    usableScreenSize = new Dimension(
                  totalScreenSize.width, totalScreenSize.height-taskBarHeight);

}// end of ChartGroup::getScreenSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setupGUI
//
// Sets up the user interface on the mainPanel: buttons, displays, etc.
//

private void setupGui()
{

    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));

    controlsPanel = new ControlsPanel(this, sharedSettings);
    controlsPanel.init();
    mainPanel.add(controlsPanel);

    createChartGroups();

    linkGraphsWhichTrackOtherGraphsScolling();

}// end of MainView::setupGui
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getAllValuesFromCurrentControlPanel
//
//

public ArrayList<Object> getAllValuesFromCurrentControlPanel()
{

    return(controlsPanel.getAllValuesFromCurrentControlPanel());

}// end of MainView::getAllValuesFromCurrentControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::refreshControlsPanel
//

public void refreshControlsPanel()
{

    controlsPanel.refresh();

}// end of MainView::refreshControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createChartGroups
//
// Creates the chart groups and adds them to the panel.
//
// WIP MKS -- only works with one chart group right now -- need to specify
// which frame/panel each group gets added to.

private void createChartGroups()
{

    chartGroups = new ChartGroup[numChartGroups];

    for (int i = 0; i<numChartGroups; i++){
        chartGroups[i] = new ChartGroup(i, configFile, sharedSettings,
                                                    usableScreenSize, this);
        chartGroups[i].init();
        mainPanel.add(chartGroups[i]);
    }

}// end of MainView::createChartGroups
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::addVerticalSpacer
//
// Adds a vertical spacer of pNumPixels height to JPanel pTarget.
//

public static void addVerticalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(0,pNumPixels)));

}// end of MainView::addVerticalSpacer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::addHorizontalSpacer
//
// Adds a horizontal spacer of pNumPixels width to JPanel pTarget.
//

public static void addHorizontalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(pNumPixels,0)));

}// end of MainView::addHorizontalSpacer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::resetAll
//
// Clears all data from all traces of all charts of all chart groups and resets
// insertion pointers.
//

public void resetAll()
{

    for(ChartGroup chartGroup: chartGroups){ chartGroup.resetAll(); }

}// end of MainView::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setAllChartAllTraceXScale
//
// Sets the display horizontal scale for all traces of all charts of
// all chart groups to pScale.
//

public void setAllChartAllTraceXScale(double pScale)
{

    for (ChartGroup chartGroup : chartGroups) {
        chartGroup.setAllChartAllTraceXScale(pScale);
    }

}// end of MainView::setAllChartAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::repaintCharts
//
// Forces all charts of all chart groups to be repainted.
//

public void repaintCharts()
{

    for (ChartGroup chartGroup : chartGroups) {
        chartGroup.repaint();
    }

}// end of MainView::repaintCharts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getChartWidth
//
// Returns the chart width...the number of data points in the horizontal axis.
//
// WIP MKS -- need to get this value from the group/chart/graph itself?

public int getChartWidth()
{

    return(CHART_WIDTH);

}// end of MainView::getChartWidth
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getGraph
//
// Returns the reference to graph pGraph of pChart of pChartGroup.
//

public Graph getGraph(int pChartGroup, int pChart, int pGraph)
{

    if (pChartGroup < 0 || pChartGroup >= chartGroups.length){ return(null); }

    return( chartGroups[pChartGroup].getGraph(pChart, pGraph) );

}// end of MainView::getGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getTrace
//
// Returns the reference to trace pTrace of pGraph of pChart of pChartGroup.
//

public Trace getTrace(int pChartGroup, int pChart, int pGraph, int pTrace)
{

    if (pChartGroup < 0 || pChartGroup >= chartGroups.length){ return(null); }

    return( chartGroups[pChartGroup].getTrace(pChart, pGraph, pTrace) );

}// end of MainView::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getTrace
//
// Returns the reference to Trace of Graph of Chart of ChartGroup as specified
// by the values in pGuiDataSet.
//
//

public Trace getTrace(GUIDataSet pGuiDataSet)
{

    return( getTrace(pGuiDataSet.chartGroupNum,
                     pGuiDataSet.chartNum,
                     pGuiDataSet.graphNum,
                     pGuiDataSet.traceNum) );

}// end of MainView::getTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateAnnotationGraphs
//
// Plots data added to annoBuffer and/or erases any data which has been
// flagged as erased for the annotation graph of all charts of pChartGroup.
//

public void updateAnnotationGraphs(int pChartGroup)
{

    chartGroups[pChartGroup].updateAnnotationGraphs();

}// end of MainView::updateAnnotationGraphs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::markSegmentStart
//
// Instructs children to mark last retrieved data as segment start.
//

public void markSegmentStart()
{
    
    for (ChartGroup c : chartGroups){ c.markSegmentStart(); }
    
}//end of MainView::markSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::markSegmentEnd
//
// Instructs children to mark last retrieved data as segment end.
//

public void markSegmentEnd()
{
    
    for (ChartGroup c : chartGroups){ c.markSegmentEnd(); }

}//end of MainView::markSegmentEnd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::isSegmentStarted
//
// Checks to see if a segment has been started and thus may have data which
// needs to be saved.
//

public boolean isSegmentStarted()
{

    for (ChartGroup c : chartGroups){ if (c.isSegmentStarted()) { return(true);} }

    return(false);

}//end of MainView::isSegmentStarted
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateChild
//
// Plots all new data added to transfer data buffer and erases any data which
// has been marked as erased for pTrace of pGraph of pChart of pChartGroup.
//

public void updateChild(int pChartGroup, int pChart, int pGraph, int pTrace)
{

    chartGroups[pChartGroup].updateChild(pChart, pGraph, pTrace);

}// end of MainView::updateChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateChildren
//
// Instructs all children listening for transfer buffer changes to check for
// changes and update.
//

public void updateChildren()
{
    
    for (ChartGroup g : chartGroups) { 
        g.updateChildren(); g.updateAnnotationGraphs();
    }
    
}// end of MainView::updateChildren
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::repaintChild
//
// Repaints pGraph of pChart of pChartGroup.
//

public void repaintChild(int pChartGroup, int pChart, int pGraph)
{

    chartGroups[pChartGroup].repaintChild(pChart, pGraph);

}// end of MainView::repaintChild
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateThreshold
//
// Updates the specified threshold.
//

public void updateThreshold(int pChartGroup, int pChart, int pGraph, 
                                int pThres, int pLvl)
{
    
    
    chartGroups[pChartGroup].updateThreshold(pChart, pGraph, pThres, pLvl);

}// end of MainView::updateThreshold
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setAllUserInputData
//
// Sets the values for all of the user input GUI controls to values in pList.
//
// This is useful after loading the data from a text file.
//

public void setAllUserInputData(ArrayList<String> pList)
{

    ListIterator iter = pList.listIterator();

    if (!iter.hasNext()){ return; }
//    timeScaleInput.setText((String)iter.next());
    if (!iter.hasNext()){ return; }
//    sampleFreqUnitsInput.setSelectedIndex(Integer.valueOf((String)iter.next()));
    if (!iter.hasNext()){ return; }
//    displaySamplesInput.setSelected(Boolean.parseBoolean((String)iter.next()));
  if (!iter.hasNext()){ return; }
//    filteredOutputScaling.setValue(Integer.parseInt((String)iter.next()));

}//end of MainView::setAllUserInputData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getAllUserInputData
//
// Adds the values currently in all of the user input GUI controls to pList.
//
// This is useful for saving the data to a text file.
//

public void getAllUserInputData(ArrayList<String> pList)
{

//    pList.add(timeScaleInput.getText());

//    pList.add("" + sampleFreqUnitsInput.getSelectedIndex());

//    pList.add("" + displaySamplesInput.isSelected());

//    pList.add("" + filteredOutputScaling.getIntValue());

}//end of MainView::getAllUserInputData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getUserFilterCoeffInput
//
// Adds the text currently in the filter coefficient text box to pList.
//
// This is useful for saving the data to a text file.
//

public void getUserFilterCoeffInput(ArrayList<String> pList)
{

    pList.add("<start of coefficients>");

    pList.addAll(Arrays.asList(filterCoeffInput.getText().split("\\n")));

    pList.add("<end of coefficients>");

}//end of MainView::getUserFilterCoeffInput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setUserFilterCoeffInput
//
// Sets the text in the filter coefficient text box to the next set of text
// lines in pList.
//
// This is useful after loading the data from a text file.
//

public void setUserFilterCoeffInput(ListIterator pIter)
{

    filterCoeffInput.setText(""); //delete any existing text

    //put newlines before each line instead of after so that a blank line
    //is not left at the end -- need to skip first newline to avoid blank line
    //at the beginning
    boolean firstLine = true;

    if (!pIter.hasNext()){ return; }

    String input = (String)pIter.next();

    if (!input.equals("<start of coefficients>")){ return; }

    while(pIter.hasNext()){

        input = (String)pIter.next();

        if (input.equals("<end of coefficients>")){ break; }

        if (!firstLine) { filterCoeffInput.append("\n");}

        firstLine = false;

        filterCoeffInput.append(input);

    }

}//end of MainView::setUserFilterCoeffInput
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createFonts
//
// Creates fonts for use by the program.
//

public void createFonts()
{

    //create small and large red and green fonts for use with display objects
    HashMap<TextAttribute, Object> map = new HashMap<>();

    blackSmallFont = new Font("Dialog", Font.PLAIN, 12);

    map.put(TextAttribute.FOREGROUND, Color.RED);
    redSmallFont = blackSmallFont.deriveFont(map);

    //empty the map to use for creating the large fonts
    map.clear();

    blackLargeFont = new Font("Dialog", Font.PLAIN, 20);

    map.put(TextAttribute.FOREGROUND, Color.GREEN);
    greenLargeFont = blackLargeFont.deriveFont(map);

    map.put(TextAttribute.FOREGROUND, Color.RED);
    redLargeFont = blackLargeFont.deriveFont(map);

    map.put(TextAttribute.FOREGROUND, Color.YELLOW);
    yellowLargeFont = blackLargeFont.deriveFont(map);

}// end of MainView::createFonts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayPieceInfo
//
// Displays piece info.
//

public void displayPieceInfo()
{

    pieceIDInfo.setVisible(true);

}//end of MainView::displayPieceInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayJobInfo
//
// Displays job info.
//

public void displayJobInfo()
{

    jobInfo = new JobInfo(mainFrame, sharedSettings.jobPathPrimary,
                            sharedSettings.jobPathSecondary,
                            sharedSettings.currentJobName,
                            sharedSettings.currentJobNamePathFriendly,
                            this,
                            sharedSettings.mainFileFormat);
    jobInfo.init();
    jobInfo = null;  //window will be released on close, so point should be null

}//end of MainView::displayJobInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayChangeJob
//
// Displays Change Job window.
//

public void displayChangeJob()
{

    chooseJob = new ChooseJob(mainFrame, sharedSettings.dataPathPrimary,
                                sharedSettings.dataPathSecondary, xfer);
    chooseJob.init();
    chooseJob = null;

    //send message to event handler to change jobs if new job selected
    if (xfer.rBoolean1) {
        eventHandler.actionPerformed(new ActionEvent(this,
                                                ActionEvent.ACTION_PERFORMED,
                                                "Change Job"+","+xfer.rString1));
    }

}//end of MainView::displayChangeJob
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayNewJob
//
// Displays New Job window.
//

public void displayNewJob()
{

    newJob = new NewJob(mainFrame, sharedSettings.dataPathPrimary,
                                sharedSettings.dataPathSecondary, xfer,
                                sharedSettings.mainFileFormat);
    newJob.init();
    newJob = null;

    //send message to event handler to create if new job specified
    if (xfer.rBoolean1) {
        eventHandler.actionPerformed(new ActionEvent(this,
                                                ActionEvent.ACTION_PERFORMED,
                                                "New Job,"+xfer.rString1));
    }

}//end of MainView::displayNewJob
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayLog
//
// Displays the log window. It is not released after closing as the information
// is retained so it can be viewed the next time the window is opened.
//

public void displayLog()
{

    log.setVisible(true);

}//end of MainView::displayLog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayHelp
//
// Displays help information.
//

public void displayHelp()
{

    help = new Help(mainFrame);
    help = null;  //window will be released on close, so point should be null

}//end of MainView::displayHelp
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayAbout
//
// Displays about information.
//

public void displayAbout()
{

    about = new About(mainFrame);
    about = null;  //window will be released on close, so point should be null

}//end of MainView::displayAbout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayViewer
//
// Opens a viewer window for viewing saved segments.
//

public void displayViewer()
{

    //this part opens a viewer window for viewing saved segments
    Viewer viewer;
    viewer = new Viewer(sharedSettings, jobInfo,
                            sharedSettings.jobPathPrimary,
                            sharedSettings.jobPathSecondary,
                            sharedSettings.currentJobName,
                            sharedSettings.currentJobNamePathFriendly);
    viewer.init();

}//end of MainView::displayViewer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::startMonitor
//
// Displays the monitor window.
//

public void startMonitor()
{

    monitor = new Monitor(mainFrame, configFile, this);
    monitor.init();

}//end of MainView::startMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::stopMonitor
//
// Closes the monitor window and sets him to null.
//

public void stopMonitor()
{

    monitor.dispose(); monitor = null;

}//end of MainView::stopMonitor
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateMonitorStatus
//
// Updates the monitor status using pStatusBuffer if he's active.
//

public void updateMonitorStatus(byte[] pStatusBuffer)
{

    if (monitor!=null) { monitor.updateStatus(pStatusBuffer); }

}//end of MainView::updateMonitorStatus
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

public void displayErrorMessage(String pMessage)
{

    GUITools.displayErrorMessage(pMessage, mainFrame);

}//end of MainView::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateGUIDataSet1
//
// Updates some of the GUI with data from the model.
//

public void updateGUIDataSet1()
{

    dataVersionTField.setText(aDataClass.getDataVersion());

    dataTArea1.setText(aDataClass.getDataItem(0));

    dataTArea2.setText(aDataClass.getDataItem(1));

}//end of MainView::updateGUIDataSet1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::drawRectangle
//
// Draws a rectangle on mainPanel
//

public void drawRectangle()
{


    Graphics2D g2 = (Graphics2D)mainPanel.getGraphics();

     // draw Rectangle2D.Double
    g2.draw(new Rectangle2D.Double(20, 10,10, 10));

}//end of MainView::drawRectangle
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateModelDataSet1
//
// Updates some of the model data with values in the GUI.
//

public void updateModelDataSet1()
{

    aDataClass.setDataVersion(dataVersionTField.getText());

    aDataClass.setDataItem(0, dataTArea1.getText());

    aDataClass.setDataItem(1, dataTArea2.getText());

}//end of MainView::updateModelDataSet1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setupAndStartMainTimer
//
// Prepares and starts a Java Swing timer.
//

public void setupAndStartMainTimer()
{

    mainTimer = new javax.swing.Timer (10, this);
    mainTimer.setActionCommand ("Timer");
    mainTimer.start();

}// end of MainView::setupAndStartMainTimer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setTextForDataTArea1
//
// Sets the text value for text box.
//

public void setTextForDataTArea1(String pText)
{

    dataTArea1.setText(pText);

}// end of MainView::setTextForDataTArea1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setTextForDataTArea2
//
// Sets the text value for text box.
//

public void setTextForDataTArea2(String pText)
{

    dataTArea2.setText(pText);

}// end of MainView::setTextForDataTArea2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::loadConfigSettings
//
// Loads settings from a config file.
//
// The config file is left open so that it can be passed to other objects to
// allow them to load their settings as well.
//

public void loadConfigSettings()
{

    String section = "Main Settings";
    
    numChartGroups = configFile.readInt(section, "number of chart groups", 0);
    
    sharedSettings.pieceDescription 
            = configFile.readString(section, 
                                        "piece description", 
                                        "Joint");
    
    sharedSettings.pieceDescriptionLC
            = configFile.readString(section,
                                        "piece description lower case", 
                                        "joint");
    
    sharedSettings.pieceDescriptionPlural
            = configFile.readString(section,
                                        "piece description plural", 
                                        "Joints");
    
    sharedSettings.pieceDescriptionPluralLC
            = configFile.readString(section,
                                        "piece description plural lower case", 
                                        "joints");

}// end of MainView::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::scanForGUIObjectsOfAType
//
// Scans recursively all children, grandchildren, and so on for all objects
// with objectType which matches pObjectType. Each matching object should
// add itself to the ArrayList pObjectList and query its own children.
//

public void scanForGUIObjectsOfAType(ArrayList<Object>pObjectList,
                                                           String pObjectType)
{

    for (ChartGroup chartGroup : chartGroups) {
        chartGroup.scanForGUIObjectsOfAType(pObjectList, pObjectType);
    }

}// end of MainView::scanForGUIObjectsOfAType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::linkGraphsWhichTrackOtherGraphsScolling
//
// Scans through all graphs to find those which are set up to track the
// scrolling of other graphs and adds a reference to the list in the latter
// of the former. When the tracked graph is scrolled, it will also invoke
// the scrolling of the tracking graph.
//

private void linkGraphsWhichTrackOtherGraphsScolling()
{

    ArrayList<Object> graphs = new ArrayList<>();

    //prepare to iterate through all graphs of various types, each successive
    //call will add more graphs to the list if they match
    //wip mks -- need to create new scan function which will scan for
    //multiple types of objects

    scanForGUIObjectsOfAType(graphs, "trace graph");
    scanForGUIObjectsOfAType(graphs, "zoom graph");
    scanForGUIObjectsOfAType(graphs, "3D map graph");

    ListIterator iter = graphs.listIterator();

    while(iter.hasNext()){

        Graph graph = (Graph)iter.next();

        GUIDataSet gds = graph.getGraphTrackedForScrolling();

        if(gds == null) {
            continue;
        }else{
            Graph trackedGraph = chartGroups[gds.chartGroupNum]
                                        .getGraph(gds.chartNum, gds.graphNum);
            trackedGraph.addGraphTrackingThisGraphsScrolling(graph);
            graph.setScrollTrackGraphInfo(trackedGraph.getGraphInfo());
        }

    }

}// end of MainView::linkGraphsWhichTrackOtherGraphsScolling
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateGraph
//
// Updates graph specified by pChartGroupNum/pChartNum/pGraphNum with new
// parameters and forces it to repaint.
//

public void updateGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
                                                     ArrayList<Object> pValues)
{

    chartGroups[pChartGroupNum].getGraph(pChartNum, pGraphNum).update(
                                                                    pValues);

}// end of MainView::updateGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getGraphParameters
//
// Retrieves parameters for graph specified by
//                                          pChartGroupNum/pChartNum/pGraphNum.
//
// The number and type of parameters is specific to each Graph subclass.
//

public ArrayList<Object> getGraphParameters(
                            int pChartGroupNum, int pChartNum, int pGraphNum)
{

    return(chartGroups[pChartGroupNum].getGraph(
                                        pChartNum, pGraphNum).getParameters());

}// end of MainView::getGraphParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::actionPerformed
//
// Responds to events and passes them on to the "Controller" (MVC Concept)
// objects.
//

@Override
public void actionPerformed(ActionEvent e)
{

    actionPerformedLocal(e); //local processing

    eventHandler.actionPerformed(e); //parent handler processing

}//end of MainView::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::actionPerformedLocal
//
// Responds to events with handling local to this class
//

public void actionPerformedLocal(ActionEvent e)
{

    if ("Expand Chart Height".equals(e.getActionCommand())) {
        expandChartHeight();
        return;
    }

    if ("Set Normal Chart Height".equals(e.getActionCommand())) {
        setNormalChartHeight();
        animateGraph = false;
        return;
    }

    if ("Animate Graph".equals(e.getActionCommand())) {
        animateGraph = !animateGraph;
        return;
    }

    if (e.getActionCommand().startsWith("open 3D map manipulator")) {
        open3DMapManipulatorControlPanel(e.getActionCommand());
        return;
    }
    
    if (e.getActionCommand().startsWith("Display Controls Panel")) {
        displayControlsPanel();
        return;
    }

    if ("Timer".equals(e.getActionCommand())) {doTimerActions(); return;}


}//end of MainView::actionPerformedLocal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::doTimerActions
//
// Performs any actions required by the timer for this object before passing
// the call on to the parent action handler.
//

public void doTimerActions()
{

    if (animateGraph){
        if (animateGraphTimer++ > 36){
            animateGraph(); animateGraphTimer = 0;
        }
    }

}//end of MainView::doTimerActions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::open3DMapManipulatorControlPanel
//
// Displays a control panel for manipulating 3D maps.
//
// The chart group and chart number which sent the command to open the
// panel will be appended to pActionCommand from whence it will be extracted
// to link the panel to the appropriate chart.
//

public void open3DMapManipulatorControlPanel(String pActionCommand)
{

    String[] split = pActionCommand.split(",");

    //extract the chart group and chart number of the invoking chart from
    //the end of the action command string

    int invokingChartGroupNum = 0;
    if(split.length > 0){invokingChartGroupNum = Integer.valueOf(split[1]);}
    
    int invokingChartNum = 0;
    if(split.length > 2){invokingChartNum = Integer.valueOf(split[2]);}
    
    int mapGraphNumber = 0; //graph of 3D map always expected to be first
    
    ArrayList<Object> graphParams = getGraphParameters(invokingChartGroupNum, 
                                                        invokingChartNum, 
                                                        mapGraphNumber);

    controlsPanel.display3DMapManipulatorControlPanel(invokingChartGroupNum, 
                                                invokingChartNum, graphParams);

    mainFrame.pack();

}//end of MainView::open3DMapManipulatorControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayCalibrationPanel
//
// Displays a calibration panel appropriate for pChartNum of pChartGroupNum with
// name of pPanelName with all channels in pChannelList displayed on the panel.
//
// The ChannelInfo objects in list pChannelList provide the necessary
// information to link the GUI controls to the channels and traces.
//

public void displayCalibrationPanel(int pChartGroupNum, int pChartNum, 
                                        String pPanelTitle, 
                                        ArrayList<ChannelInfo> pChannelList)
{
    
    int mapGraphNumber = 0; //graph of 3D map always expected to be first

    LinkedHashSet<String> groupTitles = getListOfGroups(pChannelList);
    ArrayList<Threshold[]> thresholds = getThresholdsForChart(pChartGroupNum,
                                                                    pChartNum);
    
    ArrayList<Object> graphParams = getGraphParameters(pChartGroupNum, 
                                                        pChartNum,
                                                        mapGraphNumber);

    Chart chart = chartGroups[pChartGroupNum].getChart(pChartNum);
    controlsPanel.displayCalibrationPanel(pChartGroupNum, pChartNum, chart,
                                            pPanelTitle, pChannelList, 
                                            groupTitles, thresholds, 
                                            graphParams);
    
    mainFrame.pack();

}//end of MainView::displayCalibrationPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::displayControlsPanel
//
// Displays the controls panel, which contains job #, scan speed, etc.
//

public void displayControlsPanel()
{

    controlsPanel.displayControlsPanel();

    mainFrame.pack();

}//end of MainView::displayControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setChartVisible
//
// Sets the specified Chart visible or hidden.
//

public void setChartVisible(int pChartGroupNum, int pChartNum, boolean pVisible)
{

    chartGroups[pChartGroupNum].setChartVisible(pChartNum, pVisible);

}//end of MainView::setChartVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::updateDimensions
//
// Adjusts all width and height variables for the panels along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//

public void updateDimensions()
{

    for (ChartGroup chartGroup : chartGroups){ chartGroup.updateDimensions(); }

}// end of MainView::updateDimensions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getListOfGroups
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

}//end of MainView::getListOfGroups
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getThresholdsForChart
//
// Returns an array of thresholds for pChartNum found in pChartGroupNu.
//

private ArrayList<Threshold[]> getThresholdsForChart(int pChartGroupNum,
                                                                int pChartNum)
{

    return chartGroups[pChartGroupNum].getThresholdsForChart(pChartNum);

}//end of MainView::getThresholdsForChart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::openTransverseControlPanel
//
// Displays a control panel for calibrating the Transverse system.
//
// The panel will be linked to the pChartNum of pChartGroupNum.
//

public void openTransverseControlPanel(int pChartGroupNum, int pChartNum,
                        String pPanelName, ArrayList<ChannelInfo> pChCalList)
{

    //debug mks -- delete this????
    // makes more sense to make functions which add specific option to panels?
    // such as reject thresholds, etc.??


}//end of MainView::openTransverseControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::expandChartHeight
//
// Expands the height of the chart associated with the current controls panel
// while minimizing all others.
//

public void expandChartHeight()
{

    int chartGroupNum = controlsPanel.getChartGroupNum();
    int chartNum = controlsPanel.getChartNum();

    chartGroups[chartGroupNum].expandChartHeight(chartNum, GRAPH_NUM_TO_EXPAND);

    //update the GUI controls to reflect the new view parameters

    controlsPanel.setAllValuesInCurrentControlPanel(getGraphParameters(
                             chartGroupNum, chartNum, GRAPH_NUM_TO_EXPAND));

    mainFrame.pack();

}//end of MainView::expandChartHeight
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setNormalChartHeight
//
// Sets the height of the chart associated with the current controls panel to
// the normal viewing height while maximizing all other charts.
//

public void setNormalChartHeight()
{

    int chartGroupNum = controlsPanel.getChartGroupNum();
    int chartNum = controlsPanel.getChartNum();

    chartGroups[chartGroupNum].setNormalChartHeight(
                                                chartNum, GRAPH_NUM_TO_EXPAND);

    //update the GUI controls to reflect the new view parameters

    controlsPanel.setAllValuesInCurrentControlPanel(getGraphParameters(
                                chartGroupNum, chartNum, GRAPH_NUM_TO_EXPAND));

    mainFrame.pack();

}//end of MainView::setNormalChartHeight
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::animateGraph
//
// Animates a graph. What the graph does for animation is dependent on the
// type of graph.
//

public void animateGraph()
{

    chartGroups[controlsPanel.getChartGroupNum()].animateGraph(
                      controlsPanel.getChartNum(), GRAPH_NUM_TO_EXPAND);

}//end of MainView::animateGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createDeviceLog
//
// Creates the Device Log window without displaying it.
//

public void createDeviceLog()
{

    if (deviceLog == null){
        deviceLog = new DeviceLog(mainFrame);
        deviceLog.init();
    }

}//end of MainView::createDeviceLog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::showDeviceLog
//
// Display the device info log window. The window will be created if necessary.
//

public void showDeviceLog()
{

    deviceLog.setVisible(true);

}//end of MainView::showDeviceLog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::addTextPanelToDeviceLogWindow
//
// Adds a text panel to the device log window with title pTitle.
//
// If pSetMasterPanel is true, the first panel added in this group is designated
// as the master panel. Adding a master panel is optional.
//
// Returns a reference to the new LogPanel object.
//

public LogPanel addTextPanelToDeviceLogWindow(String pTitle,
                                                       boolean pSetMasterPanel)
{

    return(deviceLog.addPanel(pTitle, pSetMasterPanel));

}//end of MainView::addTextPanelToDeviceLogWindow
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::removeMasterPanel
//
// If a master panel has been added, it is removed from its container and the
// masterPanelAdded flags is set false.
//

public void removeMasterPanel()
{

    deviceLog.removeMasterPanel();

}// end of MainView::removeMasterPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::packDeviceLogWindow
//
// Calls the pack() method on the Device Log window so it will be layed out
// properly. Use this after a panel(s) has been added or removed.
//

public void packDeviceLogWindow()
{

    deviceLog.pack();

}// end of MainView::packDeviceLogWindow
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile(IniFile pCalFile)
{

    for (ChartGroup c : chartGroups) { c.loadCalFile(pCalFile); }

}//end of MainView::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::saveCalFile
//

public void saveCalFile(IniFile pCalFile) {

    for (ChartGroup c : chartGroups) { c.saveCalFile(pCalFile); }

}//end of MainView::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::saveSegment
//

public void saveSegment(BufferedWriter pOut) throws IOException {

    //write the header information - this portion can be read by the iniFile
        //class which will only read up to the "[Header End]" tag - this allows
        //simple parsing of the header information while ignoring the data
        //stream which  follows the header

        pOut.write("[Header Start]"); pOut.newLine();
        pOut.newLine();
        pOut.write("Segment Data Version=" + SharedSettings.SEGMENT_DATA_VERSION);
        pOut.newLine();
        pOut.write("Measured Length=" + sharedSettings.getMeasuredLength());
        pOut.newLine();
        pOut.write("Inspection Direction="
                                     + sharedSettings.inspectionDirectionDescription);
        pOut.newLine();
        pOut.write("[Header End]"); pOut.newLine(); pOut.newLine();

    for (ChartGroup c : chartGroups) { c.saveSegment(pOut); }

}//end of MainView::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::saveSegmentInfo
//

public void saveSegmentInfo(BufferedWriter pOut) throws IOException {
    
    //write the header information - this portion can be read by the iniFile
    //class which will only read up to the "[Header End]" tag - this allows
    //simple parsing of the header information while ignoring the data
    //stream which  follows the header

    pOut.write("[MetaData]"); pOut.newLine();
    pOut.newLine();
    pOut.write("Segment Data Version=" + SharedSettings.SEGMENT_DATA_VERSION);
    pOut.newLine();
    pOut.newLine();
    pOut.write("[MetaData End]"); pOut.newLine(); pOut.newLine();

    pieceIDInfo.saveDataToStream(pOut);

}//end of MainView::saveSegmentInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::shutDown
//

public void shutDown() {

    sharedSettings.isViewShutDown = false; //just now beginning shut down process

    //dispose of the mainframe
    mainFrame.setVisible(false);
    mainFrame.dispose();
    mainFrame = null;

    sharedSettings.isViewShutDown = true; //view is now shut down

}//end of MainView::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::stateChanged
//

@Override
public void stateChanged(ChangeEvent ce) {

    eventHandler.stateChanged(ce);

}//end of MainView::stateChanged
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::windowClosing
//
// Handles actions necessary when the window is closing
//

@Override
public void windowClosing(WindowEvent e)
{

    eventHandler.windowClosing(e);

}//end of Controller::windowClosing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::(various window listener functions)
//
// These functions are implemented per requirements of interface WindowListener
// but do nothing at the present time.  As code is added to each function, it
// should be moved from this section and formatted properly.
//

@Override
public void windowActivated(WindowEvent e){}
@Override
public void windowDeactivated(WindowEvent e){}
@Override
public void windowOpened(WindowEvent e){}
//@Override
//public void windowClosing(WindowEvent e){}
@Override
public void windowClosed(WindowEvent e){}
@Override
public void windowIconified(WindowEvent e){}
@Override
public void windowDeiconified(WindowEvent e){}

//end of MainView::(various window listener functions)
//-----------------------------------------------------------------------------

}//end of class MainView
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class WaveFormControls
//

class WaveFormControls extends JPanel{

    String title;

    JTextField freqInput;
    JComboBox unitsInput;

    JTextField amplitudeInput;

//-----------------------------------------------------------------------------
// WaveFormControls::WaveFormControls (constructor)
//

public WaveFormControls(String pTitle)
{

    title = pTitle;

}//end of WaveFormControls::WaveFormControls (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// WaveFormControls::init
//

public void init()
{

    setBorder(BorderFactory.createTitledBorder(title));
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel subPanel;

    subPanel = new JPanel();

    subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));

    //add text field
    freqInput = new JTextField("");
    GUITools.setSizes(freqInput, 100, 24);
    //text fields don't have action commands or action listeners
    freqInput.setToolTipText("Waveform frequency.");
    subPanel.add(freqInput);

    MainView.addHorizontalSpacer(subPanel, 3);

    //add unit selection drop box
    String[] units = { "Hz", "kHz", "MHz" };
    unitsInput = new JComboBox<>(units);
    unitsInput.setSelectedIndex(0);
    GUITools.setSizes(unitsInput, 60, 24);
    subPanel.add(unitsInput);

    add(subPanel);

    subPanel = new JPanel();
    subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
    GUITools.setSizes(subPanel, 163, 24);

    //add text field
    amplitudeInput = new JTextField("");
    GUITools.setSizes(amplitudeInput, 100, 24);
    //text fields don't have action commands or action listeners
    amplitudeInput.setToolTipText("Waveform amplitude.");
    subPanel.add(amplitudeInput);

    MainView.addHorizontalSpacer(subPanel, 3);

    //add unit selection drop box
    subPanel.add(new JLabel("amplitude"));

    subPanel.add(Box.createVerticalGlue());

    add(subPanel);

}// end of WaveFormControls::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// WaveFormControls::getAllUserInputData
//
// Adds the values currently in all of the user input GUI controls to pList.
//
// This is useful for saving the data to a text file.
//

public void getAllUserInputData(ArrayList<String> pList)
{

    pList.add(freqInput.getText());
    pList.add("" + unitsInput.getSelectedIndex());
    pList.add(amplitudeInput.getText());

}//end of WaveFormControls::getAllUserInputData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// WaveFormControls::setAllUserInputData
//
// Sets the values for all of the user input GUI controls to values read via
// pIter.
//
// This is useful after loading the data from a text file.
//

public void setAllUserInputData(ListIterator pIter)
{

    if (!pIter.hasNext()){ return; }
    freqInput.setText((String)pIter.next());
    if (!pIter.hasNext()){ return; }
    unitsInput.setSelectedIndex(Integer.parseInt((String)pIter.next()));
    if (!pIter.hasNext()){ return; }
    amplitudeInput.setText((String)pIter.next());

}//end of WaveFormControls::setAllUserInputData
//-----------------------------------------------------------------------------

}//end of class WaveFormControls
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

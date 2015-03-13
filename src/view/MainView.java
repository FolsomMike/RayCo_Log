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
import controller.GUIDataSet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import model.MainDataClass;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;

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
    
    private final MainDataClass aDataClass;

    private MainMenu mainMenu;

    private ControlsGroup currentControlPanel;
    
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
    private Help help;
    private About about;

    private javax.swing.Timer mainTimer;

    private final EventHandler eventHandler;

    private Font blackSmallFont, redSmallFont;
    private Font redLargeFont, greenLargeFont, yellowLargeFont, blackLargeFont;

    private JLabel statusLabel, infoLabel;
    private JLabel progressLabel;

    private Dimension totalScreenSize, usableScreenSize;
    
    private JButton scanBtn, inspectBtn, stopBtn;
    
    private static final int CHART_WIDTH = 1000; //1670 for LG screen at RGNDT
    private static final int CHART_HEIGHT = 100;
    
    public static final int NUM_CHARTS = 3;
    
    public static final int LONG_CHART = 0;
    public static final int TRANS_CHART = 1;
    public static final int WALL_CHART = 2;
    
    public static final int SAMPLE_TRACE = 0;
    
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
    mainFrame.setJMenuBar(mainMenu = new MainMenu(this));

    //create various fonts for use by the program
    createFonts();

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

    //set the min/max/preferred sizes of the panel to set the size of the frame
    //not used here -- frame is packed around contents
    //Tools.setSizes(mainPanel, 200, 375);

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

    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    //    setLocation((int)screenSize.getWidth() - getWidth(), 0);

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


    GraphicsConfiguration gc = mainFrame.getGraphicsConfiguration();

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
     
    mainPanel.add(createControlsPanel());
    
    createChartGroups();
    
}// end of MainView::setupGui
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createControlsPanel
//
// Returns a JPanel containing the control GUI items.
//

private JPanel createControlsPanel()
{
        
    JPanel panel = new JPanel();    
    panel.setBorder(BorderFactory.createTitledBorder("Controls"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    
    addVerticalSpacer(panel, 10);

    Map3DManipulator map3DManip = new Map3DManipulator(this);
    map3DManip.init();
    panel.add(map3DManip);

    currentControlPanel = map3DManip;
    
    panel.add(Box.createVerticalGlue());
    
    panel.add(createModeButtonPanel());
        
    return(panel);
        
}// end of MainView::createControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getAllValuesFromCurrentControlPanel
//
// 

public ArrayList<Object> getAllValuesFromCurrentControlPanel()
{

    return(currentControlPanel.getAllValues());
    
}// end of MainView::getAllValuesFromCurrentControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setAllValuesInCurrentControlPanel
//
// 

public void setAllValuesInCurrentControlPanel(ArrayList<Object> pValues)
{

    currentControlPanel.setAllValues(pValues);
    
}// end of MainView::setAllValuesInCurrentControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createModeButtonPanel
//
// Returns a JPanel containing the mode buttons, such as Scan, Inspect, Stop.
//

private JPanel createModeButtonPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(panel, 202, 30);
  
    JButton button;
    
    //add button
    inspectBtn = new JButton("Inspect");
    inspectBtn.setActionCommand("Start Inspect Mode");
    inspectBtn.addActionListener(this);
    inspectBtn.setToolTipText("Start Inspect mode.");        
    panel.add(inspectBtn);

    addHorizontalSpacer(panel, 3);
    
    //add button
    scanBtn = new JButton("Scan");
    scanBtn.setActionCommand("Start Scan Mode");
    scanBtn.addActionListener(this);
    scanBtn.setToolTipText("Start Scan mode.");        
    panel.add(scanBtn);

    addHorizontalSpacer(panel, 3);    
    
    //add button
    stopBtn = new JButton("Stop");
    stopBtn.setActionCommand("Start Stop Mode");
    stopBtn.addActionListener(this);
    stopBtn.setToolTipText("Start Stop mode.");        
    panel.add(stopBtn);
        
    return(panel);
        
}// end of MainView::createModeButtonPanel
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
        chartGroups[i] = new ChartGroup(i, configFile, usableScreenSize);
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
// MainView::updateChild
//
// Plots all data added to transfer data buffer and erases any data which has
// been marked as erased for pTrace of pGraph of pChart of pChartGroup.
//

public void updateChild(int pChartGroup, int pChart, int pGraph, int pTrace)
{

    chartGroups[pChartGroup].updateChild(pChart, pGraph, pTrace);

}// end of MainView::updateChild
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
// MainView::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

public void displayErrorMessage(String pMessage)
{

    Tools.displayErrorMessage(pMessage, mainFrame);

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

    numChartGroups = configFile.readInt(
                                "Main Settings", "number of chart groups", 0);
        
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

    if ("Start Stop Mode".equals(e.getActionCommand())) {
        inspectBtn.setEnabled(true); scanBtn.setEnabled(true);
        return;
    }
    
    if ("Start Scan Mode".equals(e.getActionCommand())) {
        inspectBtn.setEnabled(false); scanBtn.setEnabled(false);
        return;
    }
    
    if ("Start Inspect Mode".equals(e.getActionCommand())) {
        inspectBtn.setEnabled(false); scanBtn.setEnabled(false);
        return;
    }

}//end of MainView::actionPerformedLocal
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
    Tools.setSizes(freqInput, 100, 24);
    //text fields don't have action commands or action listeners
    freqInput.setToolTipText("Waveform frequency.");
    subPanel.add(freqInput);

    MainView.addHorizontalSpacer(subPanel, 3);    
    
    //add unit selection drop box
    String[] units = { "Hz", "kHz", "MHz" };
    unitsInput = new JComboBox<>(units);
    unitsInput.setSelectedIndex(0);
    Tools.setSizes(unitsInput, 60, 24);        
    subPanel.add(unitsInput);
    
    add(subPanel);    

    subPanel = new JPanel();
    subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));    
    Tools.setSizes(subPanel, 163, 24);
    
    //add text field
    amplitudeInput = new JTextField("");
    Tools.setSizes(amplitudeInput, 100, 24);
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

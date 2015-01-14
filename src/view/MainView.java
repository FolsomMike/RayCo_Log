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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mksystems.mswing.MFloatSpinner;
import model.ADataClass;
import toolkit.Tools;

//-----------------------------------------------------------------------------
// class MainView
//

public class MainView implements ActionListener, WindowListener, ChangeListener
{

    private JFrame mainFrame;
    private JPanel mainPanel;

    private Chart[] charts;
    
    private final ADataClass aDataClass;

    private MainMenu mainMenu;

    private JTextField dataVersionTField;
    private JTextField dataTArea1;
    private JTextField dataTArea2;

    private JTextField timeScaleInput;
    private JTextField upSampleMultiplierInput;
    private JTextField sampleFreqInput;    
    private JComboBox sampleFreqUnitsInput;
    
    private WaveFormControls waveForm1Controls;
    private WaveFormControls waveForm2Controls;   
    
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

    private static final int CHART_WIDTH = 1650;
    private static final int CHART_HEIGHT = 100;
    
    public static final int NUM_CHARTS = 3;
    
    public static final int LONG_CHART = 0;
    public static final int TRANS_CHART = 1;
    public static final int WALL_CHART = 2;
    
    public static final int SAMPLE_TRACE = 0;
    
//-----------------------------------------------------------------------------
// MainView::MainView (constructor)
//

public MainView(EventHandler pEventHandler, ADataClass pADataClass)
{

    eventHandler = pEventHandler;
    aDataClass = pADataClass;

}//end of MainView::MainView (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    setupMainFrame();

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

    mainFrame = new JFrame("RayCo Log IV");

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

}// end of MainView::setupMainFrame
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
    
    mainPanel.add(createGraphingPanel());    
    
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
    
    panel.add(createTimeScaleControlPanel());
    
    panel.add(createUpSamplingControlPanel());
    
    panel.add(createSampleFrequencyPanel());    
    
    panel.add(waveForm1Controls = new WaveFormControls("Input WaveForm 1"));
    waveForm1Controls.init();
    panel.add(waveForm2Controls = new WaveFormControls("Input WaveForm 2"));
    waveForm2Controls.init();

    panel.add(createOutputControlPanel());

    panel.add(createApplyButtonPanel());
            
    panel.add(Box.createVerticalGlue());
    
/*
    
   
    //create a label to display good/warning/bad system status
    statusLabel = new JLabel("Status");
    statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(statusLabel);

    addVerticalSpacer(panel, 20);

    //create a label to display miscellaneous info
    infoLabel = new JLabel("Info");
    infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(infoLabel);

    addVerticalSpacer(panel, 20);

    //add text field
    dataVersionTField = new JTextField("unknown");
    dataVersionTField.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(dataVersionTField, 100, 24);
    //text fields don't have action commands or action listeners
    dataVersionTField.setToolTipText("The data format version.");
    panel.add(dataVersionTField);

    addVerticalSpacer(panel, 3);
    
    //add text field
    dataTArea1 = new JTextField("");
    dataTArea1.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(dataTArea1, 100, 24);
    //text fields don't have action commands or action listeners
    dataTArea1.setToolTipText("A data entry.");
    panel.add(dataTArea1);
    
    addVerticalSpacer(panel, 3);    

    //add text field
    dataTArea2 = new JTextField("");
    dataTArea2.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(dataTArea2, 100, 24);
    //text fields don't have action commands or action listeners
    dataTArea2.setToolTipText("A data entry.");
    panel.add(dataTArea2);

    addVerticalSpacer(panel, 20);

    //add button
    JButton loadBtn = new JButton("Load");
    loadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    loadBtn.setActionCommand("Load Data From File");
    loadBtn.addActionListener(this);
    loadBtn.setToolTipText("Load data from file.");
    panel.add(loadBtn);

    addVerticalSpacer(panel, 10);
    
    //add a button
    JButton saveBtn = new JButton("Save");
    saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    saveBtn.setActionCommand("Save Data To File");
    saveBtn.addActionListener(this);
    saveBtn.setToolTipText("Save data to file.");
    panel.add(saveBtn);

    addVerticalSpacer(panel, 10);    

    progressLabel = new JLabel("Progress");
    progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(progressLabel);

    addVerticalSpacer(panel, 10);    

    //set this spinner up for use with doubles
    //the format string "##0" has decimal places
    //use intSpinner1.getIntValue() to retrieve the value as an integer
    
    MFloatSpinner doubleSpinner1 = 
            new MFloatSpinner(5.5, 1.1, 9.9, 0.1, "##0.0", 60, 20);
    doubleSpinner1.setName("Double Spinner 1 -- used for doubles");
    doubleSpinner1.addChangeListener(this);
    doubleSpinner1.setToolTipText("This is float spinner #1!");
    panel.add(doubleSpinner1);

    addVerticalSpacer(panel, 10);
    
    //set this spinner up for use with integers
    //the format string "##0" has no decimal places
    //use intSpinner1.getIntValue() to retrieve the value as an integer
    
    MFloatSpinner intSpinner1 = 
            new MFloatSpinner(1, 1, 100000, 1, "##0", 60, 20);
    intSpinner1.setName("Integer Spinner 1 -- used for integers");
    intSpinner1.addChangeListener(this);
    intSpinner1.setToolTipText("This is float spinner #1!");
    panel.add(intSpinner1);
   
        */
        
    return(panel);
        
}// end of MainView::createControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createTimeScaleControlPanel
//
// Returns a JPanel containing the controls for setting the time (Y axis)
// scale factor. Allows user to shrink or stretch the time scale.
//

private JPanel createTimeScaleControlPanel()
{
        
    JPanel panel = new JPanel();    
    panel.setBorder(BorderFactory.createTitledBorder("Time Scale"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //add text field
    timeScaleInput = new JTextField("");
    Tools.setSizes(timeScaleInput, 100, 24);
    //text fields don't have action commands or action listeners
    timeScaleInput.setToolTipText("Time zoom scale.");
    panel.add(timeScaleInput);
        
    JLabel unitsLabel = new JLabel(" scale ");
    panel.add(unitsLabel);
    
    return(panel);
        
}// end of MainView::createTimeScaleControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createUpSamplingControlPanel
//
// Returns a JPanel containing the controls for setting the up-sample factor.
//

private JPanel createUpSamplingControlPanel()
{
        
    JPanel panel = new JPanel();    
    panel.setBorder(BorderFactory.createTitledBorder("Up Sampling"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //add text field
    upSampleMultiplierInput = new JTextField("");
    Tools.setSizes(upSampleMultiplierInput, 100, 24);
    //text fields don't have action commands or action listeners
    upSampleMultiplierInput.setToolTipText("Multiplier for upsample rate.");
    panel.add(upSampleMultiplierInput);
        
    JLabel unitsLabel = new JLabel(" multiplier ");
    panel.add(unitsLabel);
    
    return(panel);
        
}// end of MainView::createUpSamplingControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createApplyButtonPanel
//
// Returns a JPanel containing the Apply button. It is placed in its own panel
// so that it can be centered.
//

private JPanel createApplyButtonPanel()
{
        
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(panel, 150, 30);
  
    //add button
    JButton button = new JButton("Apply");
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setActionCommand("Apply Settings to Waveforms");
    button.addActionListener(this);
    button.setToolTipText("Apply settings to waveforms.");
        
    panel.add(button);
    
    return(panel);
        
}// end of MainView::createApplyButtonPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createSampleFrequencyPanel
//
// Returns a JPanel containing the controls for setting the sample frequency.
//

private JPanel createSampleFrequencyPanel()
{
        
    JPanel panel = new JPanel();    
    panel.setBorder(BorderFactory.createTitledBorder("Sampling Frequency"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //add text field
    sampleFreqInput = new JTextField("");
    Tools.setSizes(sampleFreqInput, 100, 24);
    //text fields don't have action commands or action listeners
    sampleFreqInput.setToolTipText("Sampling frequency.");
    panel.add(sampleFreqInput);
    
    addHorizontalSpacer(panel, 3);
    
    //add unit selection drop box
    String[] units = { "Hz", "kHz", "MHz" };
    sampleFreqUnitsInput = new JComboBox(units);
    sampleFreqUnitsInput.setSelectedIndex(0);
    Tools.setSizes(sampleFreqUnitsInput, 60, 24);        
    panel.add(sampleFreqUnitsInput);
    
    return(panel);
        
}// end of MainView::createSampleFrequencyPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createOutputControlPanel
//
// Returns a JPanel containing the controls for controlling the output wave
// display.
//

private JPanel createOutputControlPanel()
{
        
    JPanel panel = new JPanel();    
    panel.setBorder(BorderFactory.createTitledBorder("Output Waveforms"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    displaySamplesInput = new JCheckBox("Display Sampled Data Points");
    displaySamplesInput.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(displaySamplesInput, 200, 24);
    displaySamplesInput.setToolTipText(
                        "Displays the data points sampled from the input.");
    panel.add(displaySamplesInput);
        
    displayZeroStuffedWaveFormInput = 
                                new JCheckBox("Display Zero-Stuffed Waveform");
    displayZeroStuffedWaveFormInput.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(displayZeroStuffedWaveFormInput, 200, 24);
    displayZeroStuffedWaveFormInput.setToolTipText(
                     "Displays the unfiltered, zero-stuffed output waveform.");
    panel.add(displayZeroStuffedWaveFormInput);

    displayFilteredWaveFormInput = new JCheckBox("Display Filtered Waveform");
    displayFilteredWaveFormInput.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(displayFilteredWaveFormInput, 175, 24);
    displayFilteredWaveFormInput.setToolTipText(
                                    "Displays the filtered output waveform.");
    panel.add(displayFilteredWaveFormInput);
    
    addHorizontalSpacer(panel, 3);

    panel.add(new JLabel("FIR Filter Coefficients"));
    
    filterCoeffInput = new JTextArea(19, 0);
    JScrollPane scrollPane = new JScrollPane(filterCoeffInput, 
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    Tools.setSizes(scrollPane, 160, 100);   
    scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);    
    panel.add(scrollPane);

    panel.add(createOutputScalingPanel());
    
    return(panel);
        
}// end of MainView::createOutputControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createOutputScalingPanel
//
// Returns a JPanel containing the controls for adjusting the output scaling
// value.
//

private JPanel createOutputScalingPanel()
{
        
    JPanel panel = new JPanel();    
    panel.setBorder(BorderFactory.createTitledBorder("Output Scaling"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    //set this spinner up for use with integers
    //the format string "##0" has no decimal places
    //use intSpinner1.getIntValue() to retrieve the value as an integer
    
    filteredOutputScaling = 
            new MFloatSpinner(50000, 1, 1000000, 1, "##0", 80, 20);
    filteredOutputScaling.setName("Output Waveform Scaling Factor");
    filteredOutputScaling.setToolTipText(
            "The amplitude of the filtered output waveform "
                                    + "attenuated by dividing by this value.");
    panel.add(filteredOutputScaling);
        
    JLabel unitsLabel = new JLabel(" divisor ");
    panel.add(unitsLabel);
    
    return(panel);
        
}// end of MainView::createOutputScalingPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::createGraphingPanel
//
// Returns a JPanel containing the graph display GUI items.
//

private JPanel createGraphingPanel()
{
        
    JPanel panel = new JPanel(); 
    //no border -- panel.setBorder(BorderFactory.createTitledBorder("Graphs"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    charts = new Chart[NUM_CHARTS];
        
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
    charts[TRANS_CHART].setTraceConnectPoints(SAMPLE_TRACE, false);

    //create chart for Transverse
    charts[WALL_CHART] = new Chart();
    charts[WALL_CHART].init(
                    "Wall", WALL_CHART, 1, false, 0, CHART_WIDTH, CHART_HEIGHT);
    panel.add(charts[WALL_CHART]);

    return(panel);
     
}// end of MainView::createGraphingPanel
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
// MainView::insertDataPointInTrace
//
// Adds data point pData to pTrace of pChart.
//

public void insertDataPointInTrace(int pChart, int pTrace, int pData)
{        

    charts[pChart].insertDataPointInTrace(pTrace, pData);
    
}// end of MainView::insertDataPointInTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setTraceFlags
//
// Sets flag(s) pFlags at index pIndex of pTrace of pChart.
//
// Each bit of pFlags represents a different flag.
//

public void setTraceFlags(int pChart, int pTrace, int pIndex, int pFlags)
{        

    charts[pChart].setTraceFlags(pTrace, pIndex, pFlags);

}// end of MainView::setTraceFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setTraceFlagsAtCurrentInsertionPoint
//
// Sets flag(s) pFlags at the current insertion point of pTrace of pChart.
//
// Each bit of pFlags represents a different flag.
//

public void setTraceFlagsAtCurrentInsertionPoint(
                                            int pChart, int pTrace, int pFlags)
{        

    charts[pChart].setTraceFlagsAtCurrentInsertionPoint(pTrace, pFlags);

}// end of MainView::setTraceFlagsAtCurrentInsertionPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::resetAllTraceDataForChart
//
// Clears all data from all traces of pChart and resets insertion pointer.
//

public void resetAllTraceDataForChart(int pChart)
{        
    
    charts[pChart].resetAllTraceData();
    
}// end of MainView::resetAllTraceDataForChart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setAllChartAllTraceXScale
//
// Sets the display horizontal scale for all traces of all charts to pScale.
//

public void setAllChartAllTraceXScale(double pScale)
{
    
    for (Chart chart : charts) { chart.setAllTraceXScale(pScale); }

}// end of MainView::setAllChartAllTraceXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::repaintCharts
//
// Forces all charts to be repainted.
//

public void repaintCharts()
{
    
    for (Chart chart : charts) {
        chart.repaint();
    }

}// end of MainView::repaintCharts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::getChartWidth
//
// Returns the chart width...the number of data points in the horizontal axis.
//

public int getChartWidth()
{

    return(CHART_WIDTH);
    
}// end of MainView::getChartWidth
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
    timeScaleInput.setText((String)iter.next());    
    if (!iter.hasNext()){ return; }
    upSampleMultiplierInput.setText((String)iter.next());
    if (!iter.hasNext()){ return; }
    sampleFreqInput.setText((String)iter.next());
    if (!iter.hasNext()){ return; }
    sampleFreqUnitsInput.setSelectedIndex(Integer.valueOf((String)iter.next()));

    //allow other objects to add their data to the list
    waveForm1Controls.setAllUserInputData(iter);
    waveForm2Controls.setAllUserInputData(iter);

    if (!iter.hasNext()){ return; }    
    displaySamplesInput.setSelected(Boolean.parseBoolean((String)iter.next()));
        
    if (!iter.hasNext()){ return; }    
    displayZeroStuffedWaveFormInput.setSelected(
                            Boolean.parseBoolean((String)iter.next()));
        
    if (!iter.hasNext()){ return; }    
    displayFilteredWaveFormInput.setSelected(
                            Boolean.parseBoolean((String)iter.next()));

  if (!iter.hasNext()){ return; }    
    filteredOutputScaling.setValue(Integer.parseInt((String)iter.next()));
    
    setUserFilterCoeffInput(iter);
    
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
    
    pList.add(timeScaleInput.getText());        
    pList.add(upSampleMultiplierInput.getText());
    pList.add(sampleFreqInput.getText());
    pList.add("" + sampleFreqUnitsInput.getSelectedIndex());

    //allow other objects to add their data to the list
    waveForm1Controls.getAllUserInputData(pList);
    waveForm2Controls.getAllUserInputData(pList);
    
    pList.add("" + displaySamplesInput.isSelected());
    
    pList.add("" + displayZeroStuffedWaveFormInput.isSelected());
    
    pList.add("" + displayFilteredWaveFormInput.isSelected());

    pList.add("" + filteredOutputScaling.getIntValue());    
    
    getUserFilterCoeffInput(pList);
    
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

    //main timer has 2 second period
    mainTimer = new javax.swing.Timer (2000, this);
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
// MainView::actionPerformed
//
// Responds to events and passes them on to the "Controller" (MVC Concept)
// objects.
//

@Override
public void actionPerformed(ActionEvent e)
{

    eventHandler.actionPerformed(e);

}//end of MainView::actionPerformed
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
    unitsInput = new JComboBox(units);
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

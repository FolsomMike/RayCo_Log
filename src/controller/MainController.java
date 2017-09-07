/******************************************************************************
* Title: MainController.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class is the Main Controller in a Model-View-Controller architecture.
* It creates the Model and the View.
* It tells the View to update its display of the data in the model.
* It handles user input from the View (button pushes, etc.)*
* It tells the Model what to do with its data based on these inputs and tells
*   the View when to update or change the way it is displaying the data.
*
* There may be many classes in the controller package which handle different
* aspects of the control functions.
*
* In this implementation:
*   the Model knows only about itself
*   the View knows only about the Model and can get data from it
*   the Controller knows about the Model and the View and interacts with both
*
* The View sends messages to the Controller in the form of action messages
* to an EventHandler object -- in this case the Controller is designated to the
* View as the EventHandler.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package controller;

import hardware.Channel;
import hardware.Device;
import view.GUIDataSet;
import hardware.MainHandler;
import hardware.PeakData;
import hardware.PeakMapData;
import hardware.PeakSnapshotData;
import hardware.SampleMetaData;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import mksystems.mswing.MFloatSpinner;
import model.MainDataClass;
import model.DataTransferIntBuffer;
import model.DataTransferIntMultiDimBuffer;
import model.DataTransferSnapshotBuffer;
import model.IniFile;
import model.Options;
import model.SharedSettings;
import view.ChannelInfo;
import view.GUITools;
import view.LogPanel;
import view.MKSTools;
import view.MainView;
import view.Map3D;
import view.Map3DGraph;
import view.Trace;
import view.ZoomGraph;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainController
//

public class MainController implements EventHandler, Runnable
{

    private IniFile configFile;

    private SharedSettings sharedSettings;

    private MainHandler mainHandler;

    private PeakData peakData;
    private PeakSnapshotData peakSnapshotData;
    private PeakMapData peakMapData;

    private MainDataClass mainDataClass;

    private MainView mainView;

    private Options options;

    private final Boolean blinkStatusLabel = false;

    private String errorMessage;

    private SwingWorker workerThread;

    private final DecimalFormat decimalFormat1 = new DecimalFormat("#.0");

    private Font tSafeFont;
    private String tSafeText;

    private boolean devicesConnected = false;

    private int displayUpdateTimer = 0;

    private int mapUpdateRateTrigger = 0;

    private String XMLPageFromRemote;

    private boolean shutDown = false;

    private final JFileChooser fileChooser = new JFileChooser();

    private final String newline = "\n";

    private final GUIDataSet guiDataSet = new GUIDataSet();

    private int numDataBuffers;
    private DataTransferIntBuffer dataBuffers[];
    private int numSnapshotBuffers;
    private DataTransferSnapshotBuffer snapshotBuffers[];
    private int numMapBuffers;
    private DataTransferIntMultiDimBuffer mapBuffers[];

    private int mode;

    public static final int STOP_MODE = 0;
    public static final int SCAN_MODE = 1;
    public static final int INSPECT_MODE = 2;

//-----------------------------------------------------------------------------
// MainController::MainController (constructor)
//

public MainController()
{

}//end of MainController::MainController (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    mode = STOP_MODE;

    sharedSettings = new SharedSettings();
    //main frame is not yet created, so pass null
    sharedSettings.init(null);

    loadConfigSettings();

    peakData = new PeakData(0);
    peakMapData = new PeakMapData(0, 48); //debug mks -- this needs to be loaded from config file!!!
    peakSnapshotData = new PeakSnapshotData(0, 128); //DEBUG HSS// this needs to be loaded from config

    mainDataClass = new MainDataClass();
    mainDataClass.init();

    mainView = new MainView(this, mainDataClass, sharedSettings, configFile);
    mainView.init();

    loadUserSettingsFromFile();

    //create and load the program options
    options = new Options();

    mainHandler = new MainHandler(0, this, sharedSettings, configFile);
    mainHandler.init();

    //create data transfer buffers
    setUpDataTransferBuffers();

    //start the timer and control thread after everything else created
    mainView.setupAndStartMainTimer();
    new Thread(this).start();

}// end of MainController::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::setupDeviceLogPanels
//
// Creates multiple text logging panels in the Device Log window. Typically,
// this method is called back by MainHandler after it has loaded the number of
// devices listed in the config file.
//
// If the Device Log window itself does not yet exist, it will be created.
//
// The number of panels to create is passed via pNumDevices. Each panel will
// be given a placeholder name of "Device 0", "Device 1", etc.
//
// If pSetMasterPanel is true, the first panel added in this group is designated
// as the master panel. Adding a master panel is optional.
//
// An  ArrayList of the panels is returned.
//

public ArrayList<LogPanel> setupDeviceLogPanels(int pNumDevices,
                                                       boolean pSetMasterPanel)
{

    ArrayList<LogPanel> logPanels = new ArrayList<>();

    mainView.createDeviceLog(); //only creates if not already created

    for(int i=0; i<pNumDevices; i++){
        logPanels.add(mainView.addTextPanelToDeviceLogWindow("Device " + i,
                                                             pSetMasterPanel));
    }

    mainView.showDeviceLog();

    mainView.packDeviceLogWindow(); //layout out window after all panels added

    return(logPanels);

}// end of MainController::setupDeviceLogPanels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::removeMasterPanel
//
// If a master log panel has been added, it is removed from its container and
// the masterPanelAdded flags is set false.
//

public void removeMasterPanel()
{

    mainView.removeMasterPanel();

}// end of MainController::removeMasterPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::loadConfigSettings
//
// Loads settings from a config file.
//
// The config file is left open so that it can be passed to other objects to
// allow them to load their settings as well.
//

public void loadConfigSettings()
{

    String filename = sharedSettings.jobPathPrimary + "00 - " +
                sharedSettings.currentJobName + " Main Configuration.ini";

    try {
        configFile = new IniFile(filename, sharedSettings.mainFileFormat);
        configFile.init();
    }
    catch(IOException e){
        MKSTools.logSevere(
                      getClass().getName(), e.getMessage() + " - Error: 1103");
        return;
    }

}// end of MainController::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::setUpDataTransferBuffers
//
// Creates and initializes the buffers used to store incoming data for access
// by other threads for processing and display.
//

private void setUpDataTransferBuffers()
{

    //create a buffer for each trace
    createAndAssignDataBuffersToTraces();

    //create a buffer for each snapshot/zoom graph
    createAndAssignDataBuffersToSnapshots();

    //create a buffer for each map
    createAndAssignDataBuffersToMaps();

    mainView.resetAll();

    //link each channel with the appropriate data buffer
    setChannelDataBuffers();

    //link each device with the appropriate snapshot buffer
    setDeviceSnapshotDataBuffers();

    //link each device with the appropriate map buffer
    setDeviceMapDataBuffers();

}// end of MainController::setUpDataTransferBuffers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::createAndAssignDataBuffersToTraces
//
// Scans through all traces and creates a DataTransferIntBuffer for each
// one.
//

private void createAndAssignDataBuffersToTraces()
{

    ArrayList<Object> traces = new ArrayList<>();

    //prepare to iterate through all traces
    mainView.scanForGUIObjectsOfAType(traces, "trace");

    numDataBuffers = traces.size();
    dataBuffers = new DataTransferIntBuffer[numDataBuffers];

    int i = 0;

    ListIterator iter = traces.listIterator();

    while(iter.hasNext()){

        Trace trace = (Trace)iter.next();

        dataBuffers[i] = new DataTransferIntBuffer(
                        trace.getNumDataPoints(), trace.getPeakType());
        dataBuffers[i].init(0); dataBuffers[i].reset();

        trace.setDataBuffer(dataBuffers[i]);

        dataBuffers[i].chartGroupNum = trace.chartGroupNum;
        dataBuffers[i].chartNum = trace.chartNum;
        dataBuffers[i].graphNum = trace.graphNum;
        dataBuffers[i].traceNum = trace.traceNum;

        i++;
    }

}// end of MainController::createAndAssignDataBuffersToTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::createAndAssignDataBuffersToSnapshots
//
// Scans through all snapshot graphs and creates a DataTransferIntBuffer for
// each one.
//

private void createAndAssignDataBuffersToSnapshots()
{

    ArrayList<Object> snaps = new ArrayList<>();

    //prepare to iterate through all traces
    mainView.scanForGUIObjectsOfAType(snaps, "zoom graph");

    numSnapshotBuffers = snaps.size();
    snapshotBuffers = new DataTransferSnapshotBuffer[numSnapshotBuffers];

    int i = 0;

    ListIterator iter = snaps.listIterator();

    while(iter.hasNext()){

        ZoomGraph zoomGraph = (ZoomGraph)iter.next();

        snapshotBuffers[i] = new DataTransferSnapshotBuffer(
              200, //WIP HSS// both of these need to be determined in a different way
              128,
              zoomGraph.getPeakType());
        snapshotBuffers[i].init(0); //init requires default data value
        snapshotBuffers[i].reset();

        zoomGraph.setSnapshotBuffer(snapshotBuffers[i]);

        snapshotBuffers[i].chartGroupNum = zoomGraph.getChartGroupNum();
        snapshotBuffers[i].chartNum = zoomGraph.getChartNum();
        snapshotBuffers[i].graphNum = zoomGraph.getGraphNum();

        i++;

    }

}// end of MainController::createAndAssignDataBuffersToSnapshots
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::createAndAssignDataBuffersToMaps
//
// Scans through all maps and creates a DataTransferIntMultiDimBuffer for each
// one.
//
// Unlike graphs containing multiple traces, all the settings for 3D maps are
// loaded and handled by the containing graph since there is only one map per
// graph. In the case of 3D maps this is a Map3DGraph object.
//

private void createAndAssignDataBuffersToMaps()
{

    ArrayList<Object> mapGraphs = new ArrayList<>();

    //prepare to iterate through all traces
    mainView.scanForGUIObjectsOfAType(mapGraphs, "3D map graph");

    numMapBuffers = mapGraphs.size();
    mapBuffers = new DataTransferIntMultiDimBuffer[numMapBuffers];

    int i = 0;

    ListIterator iter = mapGraphs.listIterator();

    while(iter.hasNext()){

        Map3DGraph mapGraph = (Map3DGraph)iter.next();

        mapBuffers[i] = new DataTransferIntMultiDimBuffer(
              mapGraph.getBufferLengthInDataPoints(),
              mapGraph.getMapWidthInDataPoints(),
              mapGraph.getPeakType());
        mapBuffers[i].init(0, Map3D.NO_SYSTEM);
        mapBuffers[i].reset();

        mapGraph.setMapBuffer(mapBuffers[i]);

        mapBuffers[i].chartGroupNum = mapGraph.getChartGroupNum();
        mapBuffers[i].chartNum = mapGraph.getChartNum();
        mapBuffers[i].graphNum = mapGraph.getGraphNum();

        i++;
    }

}// end of MainController::createAndAssignDataBuffersToMaps
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::setChannelDataBuffers
//
// Scans through all channels and links each to the DataTransferIntBuffer to
// which the trace associated with that channel has been linked. This allows
// data from a channel to be passed to its associated trace.
//
// The getNextPeakData method is used to scan through the channels. The
// PeakData object returned contains a pointer to the channel.
//

private void setChannelDataBuffers()
{

    //prepares to scan through all channels
    mainHandler.initForPeakScan();

    //traverse all the channels
    while (mainHandler.getNextPeakData(peakData) != -1){

        try{
            if(channelGraphingEnabled()){
                peakData.meta.channel.setDataBuffer(mainView.getTrace(
                    peakData.meta.chartGroup, peakData.meta.chart,
                    peakData.meta.graph, peakData.meta.trace).getDataBuffer());
            }
        }catch(NullPointerException e){

            GUITools.displayErrorMessage(
                "Error Linking Data Buffer/Trace to Channel...\n"
                + "Peak Data Object Number : " + peakData.peakDataNum + "\n"
                + "Device: " + peakData.meta.deviceNum + "\n"
                + "Channel: " + peakData.meta.channelNum + "\n"
                + "Chart Group: " + peakData.meta.chartGroup + "\n"
                + "Chart : " + peakData.meta.chart + "\n"
                + "Graph : " + peakData.meta.graph + "\n"
                + "Trace : " + peakData.meta.trace
                ,null);
        }

    }

}// end of MainController::setChannelDataBuffers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::channelGraphingEnabled
//
// Channel graphing is enabled if the chart group, chart, graph, and trace are
// all set to values other than -1.
//
// If any one of those is -1, the channel is not linked to a graphing element
// and its data will be ignored for graphing purposes. The data may be still be
// used for other purposes in the program.
//
// Typically, the user will specify a channel to be ignored for graphing by
// setting all of it's group/chart/graph/trace values to -1 in the config file.
// Alternatively, simply ommitting a channel section will cause all these
// values to default to -1.
//

private boolean channelGraphingEnabled()
{

    if (peakData.meta.chartGroup != -1
         && peakData.meta.chart != -1
            && peakData.meta.graph != -1
             && peakData.meta.trace != -1){

        return(true);
    }
    else{
        return(false);
    }

}// end of MainController::channelGraphingEnabled
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayCalibrationPanel
//
// Invokes MainView to display a calibration panel, providing it with the
// appropriate group and chart number and the channels and their groups and
// names.
//
// The channel info is passed to MainView via an ArrayList of ChannelInfo
// objects.
//
// This method is usually called after a message is received from the MainView
// in the format:
//
//   display calibration panel,<chart name>,<chart group num>,<chart num>
//
// where the items in <> are replaced with the appropriate values. The message
// is passed in via pCalPanelInfo
//

private void displayCalibrationPanel(String pCalPanelInfo)
{

    String[] infoSplits = pCalPanelInfo.split(",");

    String panelTitle = infoSplits[1];

    int chartGroupNum = Integer.parseInt(infoSplits[2]);

    int chartNum = Integer.parseInt(infoSplits[3]);

    //get list of all channels relevant to the specified chart
    ArrayList<ChannelInfo> chCalList =
                            getChannelCalPanelInfo(chartGroupNum, chartNum);

    //invoke MainView to display the panel
    mainView.displayCalibrationPanel(
                                chartGroupNum, chartNum, panelTitle, chCalList);

}// end of MainController::displayCalibrationPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::getChannelCalPanelInfo
//
// Returns an ArrayList of all channels (via ChannelInfo objects) with their
// name and group as they should appear in a calibration panel which is
// displaying all channels linked with pChartGroupNum/pChartNum.
//
// The list contains channelCalInfo objects which also provide the hardware
// device number and channel number so changes to the GUI controls can be
// sent to the appropriate object.
//
// If a channel's "calibration panel name" in the config file is "paired", that
// channel won't be added to the list. This is done when multiple software
// channels are tied to the same hardware channel, such as for positive and
// negative pairs. Only one control is necessary for such pairs as there is
// only one hardware channel shared by both.
//
// The getNextPeakData method is used to scan through the channels. The
// PeakData object returned contains a pointer to the channel.
//

private ArrayList<ChannelInfo> getChannelCalPanelInfo(
                                             int pChartGroupNum, int pChartNum)
{

    ArrayList<PeakData> chList = getChannelList();
    ArrayList<ChannelInfo> chCalList = new ArrayList<>();

    ListIterator iter = chList.listIterator();

    while(iter.hasNext()){

        PeakData ch = (PeakData)iter.next();

        if(ch.meta.chartGroup == pChartGroupNum && ch.meta.chart == pChartNum){

            if(!ch.meta.channel.getCalPanelName().equals("paired")){
                chCalList.add(new ChannelInfo(
                ch.meta.deviceNum, ch.meta.channelNum,
                ch.meta.channel.getCalPanelGroup(),
                ch.meta.channel.getCalPanelName()
                ));
            }
        }
    }

    return(chCalList);

}// end of MainController::getChannelCalPanelInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::getChannelList
//
// Returns an ArrayList of PeakData objects...one for each channel in the
// system.
//
// The getNextPeakData method is used to scan through the channels. The
// PeakData object returned contains a pointer to the channel.
//

private ArrayList<PeakData> getChannelList()
{

    ArrayList<PeakData> chList = new ArrayList<>();

    //prepares to scan through all channels
    mainHandler.initForPeakScan();

    PeakData pd;

    //traverse all the channels
    while (mainHandler.getNextPeakData(pd = new PeakData(0)) != -1){
        chList.add(pd);
    }

    return(chList);

}// end of MainController::getChannelList
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::setDeviceSnapshotDataBuffers
//
// Scans through all devices and links each to the DataTransferIntBuffer
// to which the map associated with that device has been linked. This allows
// data from a device to be passed to its associated snapshot.
//
// wip mks -- check if the target link object is actually a snap object of some
// type and generate the error in that case as well
//

private void setDeviceSnapshotDataBuffers()
{

    //traverse all the devices
    for(Device device : mainHandler.getDevices()){

        SampleMetaData snapshotMeta = device.getSnapshotMeta();

        try{
            device.setSnapshotDataBuffer(mainView.getGraph(
               snapshotMeta.chartGroup, snapshotMeta.chart,
                   snapshotMeta.graph).getSnapshotBuffer());
        }catch(NullPointerException e){

            GUITools.displayErrorMessage(
                "Error Linking Snapshot Data Buffer/Snapshot to Device...\n"
                + "Device: " + snapshotMeta.deviceNum + "\n"
                + "Channel: " + snapshotMeta.channelNum + "\n"
                + "Chart Group: " + snapshotMeta.chartGroup + "\n"
                + "Chart : " + snapshotMeta.chart + "\n"
                + "Graph : " + snapshotMeta.graph + "\n"
                ,null);
        }
    }

}// end of MainController::setDeviceSnapshotDataBuffers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::setDeviceMapDataBuffers
//
// Scans through all devices and links each to the DataTransferIntMultiDimBuffer
// to which the map associated with that device has been linked. This allows
// data from a device to be passed to its associated map.
//
// wip mks -- check if the target link object is actually a map object of some
// type and generate the error in that case as well
//

private void setDeviceMapDataBuffers()
{

    //traverse all the devices

    for(Device device : mainHandler.getDevices()){

        SampleMetaData mapMeta = device.getMapMeta();

        //skip devices which do not map
        if(mapMeta.numClockPositions <= 0) { continue; }

        try{
            device.setMapDataBuffer(mainView.getGraph(
               mapMeta.chartGroup, mapMeta.chart,
                   mapMeta.graph).getMapBuffer());
        }catch(NullPointerException e){

            GUITools.displayErrorMessage(
                "Error Linking Map Data Buffer/Map to Device...\n"
                + "Device: " + mapMeta.deviceNum + "\n"
                + "Channel: " + mapMeta.channelNum + "\n"
                + "Chart Group: " + mapMeta.chartGroup + "\n"
                + "Chart : " + mapMeta.chart + "\n"
                + "Graph : " + mapMeta.graph + "\n"
                ,null);
        }
    }

}// end of MainController::setDeviceMapDataBuffers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::actionPerformed
//
// Responds to events.
//
// This is identical to the method employed by ActionListener objects. This
// object is not an ActionListener, but uses the same concept for clarity. The
// "MainView" (MVC Concept) objects catch GUI events and call this method to
// pass those events to this "MainController" object.
//

@Override
public void actionPerformed(ActionEvent e)
{

    if ("Timer".equals(e.getActionCommand())) {doTimerActions(); return;}

    if ("Display Log".equals(e.getActionCommand())) {displayLog(); return;}

    if ("Display Help".equals(e.getActionCommand())) {displayHelp(); return;}

    if ("Display About".equals(e.getActionCommand())) {displayAbout(); return;}

    if ("New File".equals(e.getActionCommand())) {doSomething1(); return;}

    if ("Open File".equals(e.getActionCommand())) {
        doSomething2();
        return;
    }

    if ("Load Data From File".equals(e.getActionCommand())){
        loadUserSettingsFromFile();
        return;
    }

    if ("Save Data To File".equals(e.getActionCommand())){
        saveUserSettingsToFile();
        return;
    }

    if ("Start Stop Mode".equals(e.getActionCommand())) {
        mode = STOP_MODE;
        return;
    }

    if ("Start Scan Mode".equals(e.getActionCommand())) {
        mode = SCAN_MODE;
        return;
    }

    if ("Start Inspect Mode".equals(e.getActionCommand())) {
        mode = INSPECT_MODE;
        return;
    }

    if ("Handle 3D Map Control Change".equals(e.getActionCommand())) {
        handle3DMapManipulation();
        return;
    }

    if (e.getActionCommand().startsWith("display calibration panel")) {
        displayCalibrationPanel(e.getActionCommand());
        return;
    }

    if (e.getActionCommand().startsWith("Update Channel")) {
        mainHandler.updateChannelParameters(e.getActionCommand(), true);
        return;
    }

}//end of MainController::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::handle3DMapManipulation
//
// Applies values from the 3D map controls panel to the map.
//

public void handle3DMapManipulation()

{

    ArrayList <Object> values = mainView.getAllValuesFromCurrentControlPanel();

    mainView.updateGraph(0, 1, 0, values); //debug mks -- the 3 here hardcodes the map at graph 3 -- ??? need to load from config???

}//end of MainController::handle3DMapManipulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::stateChanged
//

@Override
public void stateChanged(ChangeEvent ce)
{

    //if for some reason the object which changed state is not a subclass of
    //of Component, do nothing as this code only handles Components

    if (!(ce.getSource() instanceof Component)) {
        return;
    }

    //cast the object to a Component so it's methods can be accessed
    Component c = (Component)ce.getSource();

    String name = c.getName();

    if (name.startsWith("Double Spinner 1")){

        //Since we know that the Component with the name starting with
        //"Double Spinner 1" is an MFloatSpinner (because we created it and
        // used that name for it), it can safely be cast to an MFloatSpinner.
        //Since the values in that spinner are meant to be doubles, the
        //getDoubleValue method is used to retrieve the value.

        double value = ((MFloatSpinner)c).getDoubleValue();

        mainView.setTextForDataTArea1("" + value);

        //using getDoubleValue as above will often return a value with a long
        //fractional portion due to binary floating point conversion
        //imprecision -- using getText returns the value as a string formatted
        //exactly as that shown in the spinner's text box and will be rounded
        //off and truncated in the same manner

        String textValue = ((MFloatSpinner)c).getText();

        mainView.setTextForDataTArea2(textValue);

    }

    if (name.startsWith("Integer Spinner 1")){

        //Since we know that the Component with the name starting with
        //"Integer Spinner 1" is an MFloatSpinner (because we created it and
        // used that name for it), it can safely be cast to an MFloatSpinner.
        //Since the values in that spinner are meant to be integers, the
        //getIntValue method is used to retrieve the value.

        int value = ((MFloatSpinner)c).getIntValue();

        mainView.setTextForDataTArea2("" + value);

    }


}//end of MainController::stateChanged
//-----------------------------------------------------------------------------


/*
//-----------------------------------------------------------------------------
// MainController::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

}// end of MainController::paintComponent
//-----------------------------------------------------------------------------

*/

//-----------------------------------------------------------------------------
// MainController::loadUserSettingsFromFile
//
// Loads user settings from a file.
//

public void loadUserSettingsFromFile()
{

    mainView.setAllUserInputData(mainDataClass.loadUserSettingsFromFile());

}//end of MainController::loadUserSettingsFromFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::saveUserSettingsToFile
//
// Saves user settings to a file.
//

public void saveUserSettingsToFile()
{

    ArrayList<String> list = new ArrayList<>();

    mainView.getAllUserInputData(list);

    mainDataClass.saveUserSettingsToFile(list);

}//end of MainController::saveUserSettingsToFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::doTimerActions
//
// Performs actions driven by the timer.
//
// Not used for accessing network -- see run function for details.
//

public void doTimerActions()
{

    updateGUIPeriodically();

}//end of MainController::doTimerActions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::updateGUIPeriodically
//
// Handles updating the GUI with data in a timer loop. Used to transfer data
// collected from the hardware to the screen display controls such as traces,
// numeric displays, graphs, etc.
//
// Also updates all other data which does not originate from devices.
//

private void updateGUIPeriodically()
{
    //periodically collect all data from input sources
    if(mainHandler != null && mainHandler.ready){ displayDataFromDevices(); }

}// end of MainController::updateGUIPeriodically
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayDataFromDevices
//
// Handles updating the GUI with data in a timer loop. Used to transfer data
// collected from the hardware to the screen display controls such as traces,
// numeric displays, graphs, etc.
//

private void displayDataFromDevices()
{

    if(mode == STOP_MODE) { return; }

    displayDataFromDeviceChannels();

    displayDataFromDeviceSnapshots();

    displayDataFromDeviceMaps();

}// end of MainController::displayDataFromDevices
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayDataFromDeviceChannels
//
// Handles data display of peak data from all channels of all devices
//

private void displayDataFromDeviceChannels()
{

    //prepares to scan through all channels
    mainHandler.initForPeakScan();

    //get peak data for each channel and insert it into the transfer buffer
    for (Device device : mainHandler.getDevices()){
        for (Channel channel : device.getChannels()){
            if (channel.getPeakDataAndReset(peakData) == true
                 && peakData.meta.dataBuffer != null)
            {
                peakData.meta.dataBuffer.putData(peakData.peak);
            }
        }
    }

    //update display objects from transfer buffers
    for(DataTransferIntBuffer dataBuffer: dataBuffers){
        //pace this with timer to control scan speed
        dataBuffer.incPutPtrAndSetReadyAfterDataFill();
        //update trace with all data changes
        mainView.updateChild(dataBuffer.chartGroupNum, dataBuffer.chartNum,
                                     dataBuffer.graphNum, dataBuffer.traceNum);
    }

}// end of MainController::displayDataFromDeviceChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayDataFromDeviceSnapshots
//
// Handles data display of snapshots from all channels of all devices
//

private void displayDataFromDeviceSnapshots()
{

    //prepares to scan through all channels
    mainHandler.initForPeakScan();

    //if there was a peak stored, store the snapshot data for that peak
    for (Device device : mainHandler.getDevices()){
        if (device.getPeakSnapshotDataAndReset(peakSnapshotData) == true) {

            peakSnapshotData.meta
                    .dataSnapshotBuffer.putData(peakSnapshotData.peak,
                                                    peakSnapshotData.peakArray);
        }
    }

    //update display objects from transfer buffers
    for(DataTransferSnapshotBuffer snapBuffer: snapshotBuffers){
        //pace this with timer to control scan speed
        snapBuffer.incPutPtrAndSetReadyAfterDataFill();
        //update trace with all data changes
        mainView.updateAnnotationGraphs(snapBuffer.chartGroupNum);
    }

}// end of MainController::displayDataFromDeviceSnapshots
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayDataFromDeviceMaps
//
// Handles data display of peak data from all map datasets from all devices.
//

private void displayDataFromDeviceMaps()
{

    if (mapUpdateRateTrigger++ < 9){ return; } else { mapUpdateRateTrigger = 0; }    //debug mks -- does this belong here?

    //prepares to scan through all channels
    mainHandler.initForPeakScan();

    //get peak map data for each device and insert it into the transfer buffer

    for (Device device : mainHandler.getDevices()){
        if (device.getPeakMapDataAndReset(peakMapData) == true){
            peakMapData.meta.dataMapBuffer.putData(
                            peakMapData.peakArray, peakMapData.peakMetaArray);
        }
    }

    //update display objects from transfer buffers
    for(DataTransferIntMultiDimBuffer mapBuffer: mapBuffers){
        //pace this with timer to control scan speed
        mapBuffer.incPutPtrAndSetReadyAfterDataFill();
        //update trace with all data changes
        mainView.updateChild(mapBuffer.chartGroupNum, mapBuffer.chartNum,
                                       mapBuffer.graphNum, mapBuffer.traceNum);
    }

}// end of MainController::displayDataFromDeviceMaps
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayLog
//
// Displays the log window. It is not released after closing as the information
// is retained so it can be viewed the next time the window is opened.
//

private void displayLog()
{

    mainView.displayLog();

}//end of MainController::displayLog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayHelp
//
// Displays help information.
//

private void displayHelp()
{

    mainView.displayHelp();

}//end of MainController::displayHelp
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayAbout
//
// Displays about information.
//

private void displayAbout()
{

    mainView.displayAbout();

}//end of MainController::displayAbout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::doSomething1
//

private void doSomething1()
{


}//end of MainController::doSomething1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::doSomethingInWorkerThread
//
// Does nothing right now -- modify it to call a function which takes a long
// time to finish. It will be run in a background thread so the GUI is still
// responsive.
// -- CHANGE THE NAME TO REFLECT THE ACTION BEING DONE --
//

private void doSomethingInWorkerThread()
{

    //define and instantiate a worker thread to create the file


    //----------------------------------------------------------------------
    //class SwingWorker
    //

    workerThread = new SwingWorker<Void, String>() {
        @Override
        public Void doInBackground() {

            //do the work here by calling a function

            return(null);

        }//end of doInBackground

        @Override
        public void done() {

            //clear in progress message here if one is being displayed

            try {

                //use get(); function here to retrieve results if necessary
                //note that Void type here and above would be replaced with
                //the type of variable to be returned

                Void v = get();

            } catch (InterruptedException ignore) {}
            catch (java.util.concurrent.ExecutionException e) {
                String why;
                Throwable cause = e.getCause();
                if (cause != null) {
                    why = cause.getMessage();
                } else {
                    why = e.getMessage();
                }
                System.err.println("Error creating file: " + why);
            }//catch

        }//end of done

        @Override
        protected void process(java.util.List <String> pairs) {

            //this method is not used by this application as it is limited
            //the publish method cannot be easily called outside the class, so
            //messages are displayed using a ThreadSafeLogger object and status
            //components are updated using a GUIUpdater object

        }//end of process

    };//end of class SwingWorker
    //----------------------------------------------------------------------

}//end of MainController::doSomethingInWorkerThread
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::doSomething2
//

private void doSomething2()
{


}//end of MainController::doSomething2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::run
//
// This is the part which runs as a separate thread.  The actions of accessing
// remote devices occur here.  If they are done in a timer call instead, then
// buttons and displays get frozen during the sometimes lengthy calls to access
// the network.
//
// NOTE:  All functions called by this thread must wrap calls to alter GUI
// components in the invokeLater function to be thread safe.
//

@Override
public void run()
{

    //call the control method repeatedly
    while(true){

        if(!devicesConnected){
            mainHandler.connectToDevices(); devicesConnected = true;
        }

        control();

        //sleep for a bit
        threadSleep(10);

    }

}//end of MainController::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::threadSleep
//
// Calls the Thread.sleep function. Placed in a function to avoid the
// "Thread.sleep called in a loop" warning -- yeah, it's cheezy.
//

public void threadSleep(int pSleepTime)
{

    try {Thread.sleep(pSleepTime);} catch (InterruptedException e) { }

}//end of MainController::threadSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::control
//
// Performs all display and control.  Call this from a thread.
//

public void control()
{

    //update the display every 30 seconds with data collected by this thread
    if (displayUpdateTimer++ == 14){
        displayUpdateTimer = 0;
        //call function to update stuff here
    }

    //periodically collect all data from input sources

   //debug mks -- move this catch to Device -- always collect data but only
    //request inspection packets if in scan or inspect mode...this allows
    //data sent by devices to always be handled
    //if((mode==SCAN_MODE || mode==INSPECT_MODE)

    if(mainHandler != null && mainHandler.ready){
        mainHandler.collectData();
    }

    //If a shut down is initiated, clean up and exit the program.

    if(shutDown){
        //exit the program
        System.exit(0);
    }

}//end of MainController::control
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

public void displayErrorMessage(String pMessage)
{

    mainView.displayErrorMessage(pMessage);

}//end of MainController::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::waitSleep
//
// Sleeps for pTime milliseconds.
//

void waitSleep(int pTime)
{

    try{ Thread.sleep(pTime); } catch(InterruptedException e){}

}//end of MainController::waitSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::shutDown
//
// Disables chassis power and performs any other appropriate shut down
// operations.
//
// This is done by setting a flag so that this class's thread can do the
// actual work, thus avoiding thread contention.
//

public void shutDown()
{

    saveUserSettingsToFile();

    shutDown = true;

}//end of MainController::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::windowClosing
//
// Handles actions necessary when the window is closing
//

@Override
public void windowClosing(WindowEvent e)
{

    //perform all shut down procedures

    shutDown();

}//end of MainController::windowClosing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::(various window listener functions)
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

//end of MainController::(various window listener functions)
//-----------------------------------------------------------------------------


}//end of class MainController
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

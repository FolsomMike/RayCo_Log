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

import hardware.MainHandler;
import hardware.PeakData;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import mksystems.mswing.MFloatSpinner;
import model.MainDataClass;
import model.DataTransferIntBuffer;
import model.IniFile;
import model.Options;
import model.SharedSettings;
import view.MKSTools;
import view.MainView;
import view.Trace;

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
    
    private MainDataClass mainDataClass;

    private MainView mainView;

    private Options options;

    private final Boolean blinkStatusLabel = false;

    private String errorMessage;

    private SwingWorker workerThread;

    private final DecimalFormat decimalFormat1 = new DecimalFormat("#.0");

    private Font tSafeFont;
    private String tSafeText;

    private int displayUpdateTimer = 0;

    private String XMLPageFromRemote;

    private boolean shutDown = false;

    private final JFileChooser fileChooser = new JFileChooser();

    private final String newline = "\n";

    private final GUIDataSet guiDataSet = new GUIDataSet();

    private int numTraces;
    private Trace traces[];
        
    private int numDataBuffers;
    private DataTransferIntBuffer dataBuffers[];
    
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

    sharedSettings = new SharedSettings();
    //main frame is not yet created, so pass null
    sharedSettings.init(null);

    loadConfigSettings();

    peakData = new PeakData(0);
    
    mainDataClass = new MainDataClass();
    mainDataClass.init();

    mainView = new MainView(this, mainDataClass, sharedSettings, configFile);
    mainView.init();
    
    loadUserSettingsFromFile();    
    
    //create and load the program options
    options = new Options();    
    
    //start the control thread
    new Thread(this).start();

    mainView.setupAndStartMainTimer();

    mainHandler = new MainHandler(0, sharedSettings, configFile);
    mainHandler.init();

    //create data transfer buffers
    setUpDataTransferBuffers();
    
}// end of MainController::init
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
    
    //link each channel with the appropriate data buffer
    setChannelDataBuffers();
    
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

    //prepare to iterate through all traces
    mainView.initForGUIChildrenScan();
    
    //set all to zero so every child type will be scanned
    guiDataSet.setGUIChildNums(0);
    
    //scan all traces to get total number of traces
    numTraces = 0;
    while (mainView.getNextGUIChild(guiDataSet) != -1){ numTraces++; }

    numDataBuffers = numTraces;
    traces = new Trace[numTraces];
    dataBuffers = new DataTransferIntBuffer[numDataBuffers];

    //prepare to iterate through all traces
    mainView.initForGUIChildrenScan();
    
    //set all to zero so every child type will be scanned
    guiDataSet.setGUIChildNums(0);
    
    int i = 0;
    
    //scan all traces again to set up a data transfer buffer for each
    while (mainView.getNextGUIChild(guiDataSet) != -1){

        traces[i] = mainView.getTrace(guiDataSet);
        
        dataBuffers[i] = new DataTransferIntBuffer(
                        traces[i].getNumDataPoints(), traces[i].getPeakType());
        dataBuffers[i].init(); dataBuffers[i].reset();
        
        traces[i].setDataBuffer(dataBuffers[i]);
        
        dataBuffers[i].chartGroupNum = traces[i].chartGroupNum;
        dataBuffers[i].chartNum = traces[i].chartNum;
        dataBuffers[i].graphNum = traces[i].graphNum;
        dataBuffers[i].traceNum = traces[i].traceNum;
        
        i++;
    }
    
}// end of MainController::createAndAssignDataBuffersToTraces
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::setChannelDataBuffers
//
// Scans through all channels and links each to the DataTransferIntBuffer to
// which the trace associated with that channel has been linked. This allows
// data from a channel to be passed to its associated trace.
//
// The getNextPeakData methods are used to scan through the channels as the
// PeakData object returned contains a pointer to the channel.
//

private void setChannelDataBuffers()
{
  
    //prepares to scan through all channels
    mainHandler.initForPeakScan();
    
    //get peak data for each channel
    while (mainHandler.getNextPeakData(peakData) != -1){
        
        peakData.channel.setDataBuffer(mainView.getTrace(peakData.chartGroup,
               peakData.chart, peakData.graph, peakData.trace).getDataBuffer());
                      
    }
    
}// end of MainController::setChannelDataBuffers
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

    if ("Timer".equals(e.getActionCommand())) {doTimerActions();}

    if ("Display Log".equals(e.getActionCommand())) {displayLog();}

    if ("Display Help".equals(e.getActionCommand())) {displayHelp();}

    if ("Display About".equals(e.getActionCommand())) {displayAbout();}

    if ("New File".equals(e.getActionCommand())) {doSomething1();}

    if ("Open File".equals(e.getActionCommand())) {
        doSomething2();
    }

    if ("Load Data From File".equals(e.getActionCommand())){
        loadUserSettingsFromFile();
    }

    if ("Save Data To File".equals(e.getActionCommand())){
        saveUserSettingsToFile();
    }
    
}//end of MainController::actionPerformed
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
  
    //prepares to scan through all channels
    mainHandler.initForPeakScan();
    
    //get peak data for each channel
    while (mainHandler.getNextPeakData(peakData) != -1){

        //put data in the transfer buffer
        peakData.dataBuffer.putData(peakData.peak);
                      
    }

    for(DataTransferIntBuffer dataBuffer: dataBuffers){
        //pace this with timer to control scan speed
        dataBuffer.incrementPutPointerAndSetReadyFlag();
        //update trace with all data changes
        mainView.updateTrace(dataBuffer.chartGroupNum, dataBuffer.chartNum,
                                     dataBuffer.graphNum, dataBuffer.traceNum);
    }
    
}// end of MainController::displayDataFromDevices
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

    //update the display every 30 seconds
    if (displayUpdateTimer++ == 14){
        displayUpdateTimer = 0;
        //call function to update stuff here
    }

    //periodically collect all data from input sources
    if(mainHandler != null && mainHandler.ready){ mainHandler.collectData(); }
    
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

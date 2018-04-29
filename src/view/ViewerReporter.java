/******************************************************************************
* Title: ViewerReporter.java
* Author: Mike Schoonover
* Modified by: Hunter Schoonover
* Date: 10/02/17
*
* Purpose:
*
* This class provides common functionality for viewing a piece, printing its
* graphs, and printing reports.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;



//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ViewerReporter
//

public class ViewerReporter implements ActionListener {

    SharedSettings settings;
    JobInfo jobInfo;
    String jobPrimaryPath, jobBackupPath;
    String currentJobName, currentJobNamePathFriendly;
    JScrollPane scrollPane;
    JPanel chartGroupPanel;
    
    JDialog errorMessageDialog;

    javax.swing.Timer timer;

    int pieceToPrint;
    boolean isCalPiece;

    ChartGroup mainFrame;
    int loadSegmentError;
    String segmentDataVersion;

    String inspectionDirection = "";

    String measuredLengthText;
    double measuredLength;

    int numberOfChartGroups;
    ChartGroup[] chartGroups;

    DecimalFormat[] decimalFormats;
    int currentSegmentNumber;
    
    PieceInfo pieceIDInfo;

    ViewerControlPanel controlPanel;

    //WIP HSS// not used at moment //PrintProgress printProgress;

    PrintRequestAttributeSet aset;
    PrinterJob job;

    String fileCreationTimeStamp = "";

    PrintRunnable printRunnable;

    int startPiece = 0, endPiece = 0, pieceTrack = 0;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class PrintRunnable
//
// This is run by the thread which handles printing.
//

class PrintRunnable implements Runnable {

    PrintRequestAttributeSet aset;

    boolean inJobPrintMethod = false;
    boolean startPrint = false;
    boolean pauseThreadFlag = false;

//-----------------------------------------------------------------------------
// PrintRunnable::PrintRunnable (constructor)
//

public PrintRunnable(PrintRequestAttributeSet pAset)
{

    aset = pAset;

}//end of PrintRunnable::PrintRunnable (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PrintRunnable::run
//

@Override
public void run() {

    while (true){

        try{
            waitForPrintTrigger();
            }
        catch (InterruptedException e) {
            //kill the thread if interrupted from another object during wait
            return;
            }

        //start printing - Java will call the print function of the object
        //specified in the call to job.setPrintable (done above) which must
        //implement the Printable interface

        try{

            //disable the print buttons using thread safe code
            controlPanel.setEnabledButtonsThreadSafe(false);

            inJobPrintMethod = true;
            job.print(aset);
            inJobPrintMethod = false;

            //if the thread was interrupted while in the job.print method, kill
            //the thread immediately
            if (Thread.interrupted()) {return;}

            //set label to default and close the printProgress window
            /*//WIP HSS// -- may need later // printProgress.setLabel("Printing...");
            printProgress.setVisible(false);*/

            //enable the print buttons using thread safe code
            controlPanel.setEnabledButtonsThreadSafe(true);

            }
        catch (PrinterException e) {
            displayErrorMessage("Error sending to printer.", false); //debug mks wrap this in thread safe code
            }

        }//while(true)


}//end of PrintRunnable::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PrintRunnable::waitForPrintTrigger
//
// Enters a wait state until another thread calls triggerPrint which sets
// startPrint true and uses notifyAll to wake this thread up.
//

public synchronized void waitForPrintTrigger() throws InterruptedException {

    startPrint = false;

    while (!startPrint) {
        try {
            wait();
        }
        catch (InterruptedException e) {
            throw new InterruptedException();
        }

    }//while (!printTrigger)

}//end of PrintRunnable::waitForPrintTrigger
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PrintRunnable::triggerPrint
//
// Sets startPrint true and calls notifyAll to wake up the print thread.
//

public synchronized void triggerPrint() {

    startPrint = true;

    notifyAll();

}//end of PrintRunnable::triggerPrint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PrintRunnable::pauseThread
//
// Enters a wait state until another thread calls unPauseThread which sets
// pauseThreadFlag false and uses notifyAll to wake this thread up.
//
// During printing, this thread must call loadSegment to load and display each
// segment and then printChartGroup to print them.  As these functions are not
// thread safe (partly because of Swing usage), they should be called in the
// main event thread using invokeLater.  Since invokeLater results in a delayed
// call to load the segment, this thread must wait until that operation is
// completed.  Using this method to pause the thread and unPauseThread to
// release it allows the thread to wait for the functions to complete.
//

public synchronized void pauseThread() throws InterruptedException {

    pauseThreadFlag = true;

    while (pauseThreadFlag) {
        try {
            wait();
        }
        catch (InterruptedException e) {
            throw new InterruptedException();
        }
    }//while (pausePrintThread)

}//end of PrintRunnable::pauseThread
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PrintRunnable::unPauseThread
//
// Sets pauseThreadFlag false and calls notifyAll to wake up the print thread.
//

public synchronized void unPauseThread() {

    pauseThreadFlag = false;

    notifyAll();

}//end of PrintRunnable::unPauseThread
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PrintRunnable::enablePrintButtonsThreadSafe
//
// Enables the "Print" and "Print Multiple" buttons.
//

public synchronized void enablePrintButtonsThreadSafe()
{

    controlPanel.setEnabledButtonsThreadSafe(true);

}//end of PrintRunnable::enablePrintButtonsThreadSafe
//-----------------------------------------------------------------------------


}//end of class PrintRunnable
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------
// ViewerReporter::ViewerReporter (constructor)
//

public ViewerReporter(SharedSettings pSettings, JobInfo pJobInfo,
                        String pJobPrimaryPath, String pJobBackupPath,
                        String pCurrentJobName,
                        String pCurrentJobNamePathFriendly)
{

    settings = pSettings; jobInfo = pJobInfo;
    jobPrimaryPath = pJobPrimaryPath; jobBackupPath = pJobBackupPath;
    currentJobName = pCurrentJobName;
    currentJobNamePathFriendly = pCurrentJobNamePathFriendly;

    //create various decimal formats
    decimalFormats = new DecimalFormat[4];
    decimalFormats[0] = new  DecimalFormat("0000000");
    decimalFormats[1] = new  DecimalFormat("0.0");
    decimalFormats[2] = new  DecimalFormat("0.000");
    decimalFormats[3] = new  DecimalFormat("0.00");

}//end of Viewer::Viewer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::init
//
//

public void init()
{
    
    //create an object to hold info about each piece
    pieceIDInfo = new PieceInfo(mainFrame, jobPrimaryPath, jobBackupPath,
                                currentJobName, currentJobNamePathFriendly, 
                                this, true, settings.mainFileFormat);
    pieceIDInfo.init();

}//end of ViewerReporter::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::resetChartGroups
//
// Erases the chart groups and clears all data.
//

void resetChartGroups()
{

    for (int i = 0; i < numberOfChartGroups; i++) {
        chartGroups[i].resetAll();
    }

}//end of ViewerReporter::resetChartGroups
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::loadSegment
//
// Loads the data for a segment from the primary job folder.  The calibration
// and piece info are also loaded from the associated info file.
//
// This function should be called whenever a new segment is loaded for
// viewing or processing - each segment could represent a piece being monitored,
// a time period, etc.
//
// If pQuietMode is true, no error message is displayed if a file cannot be
// loaded.  This is useful for the print function which can then continue on
// to the next piece instead of freezing until the user clears the dialog
// window.
//
// If no error, returns the filename extension.
// On error loading the chart data, returns "Error: <message>".
// Error on loading piece id info or calibration data returns empty string.
//

String loadSegment(boolean pQuietMode)
{

    String segmentFilename;

    //reset the charts
    resetChartGroups();

    //inspected pieces are saved with the prefix 20 while calibration pieces are
    //saved with the prefix 30 - this forces them to be grouped together and
    //controls the order in which the types are listed when the folder is viewed
    //in alphabetical order in an explorer window

    String prefix, ext, infoExt;

    prefix = isCalSelected() ? "30 - " : "20 - ";
    ext = isCalSelected() ? ".cal" : ".dat";
    infoExt = isCalSelected() ? ".cal info" : ".info";

    segmentFilename = prefix +
                            decimalFormats[0].format(currentSegmentNumber);

    //load the cal file first so its settings can be overridden by any
    //settings in the data file which might have been different at the time
    //the data file was saved
    loadCalFile(); //load calibration settings needed for viewing

    String fullPath = jobPrimaryPath + segmentFilename + ext;

    fileCreationTimeStamp = getFileCreationDateTimeString(fullPath);

    //load the graph data
    String errorMsg = loadSegmentHelper(fullPath);

    //on error, display the message, repaint with empty chart, and exit
    if (!errorMsg.isEmpty()){
        return("Error: " + errorMsg);
        }

    //load piece info
    loadInfoHelper(jobPrimaryPath + segmentFilename + infoExt);

    return(ext);

}//end of ViewerReporter::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::loadSegmentHelper
//
// Loads the data for a segment from the specified file.  See the loadSegment
// function for more info.
//
// If there is no error, returns empty String ""
// ON error, returns the appropriate error message
//

private String loadSegmentHelper(String pFilename)
{

    //create a buffered writer stream

    FileInputStream fileInputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader in = null;

    try{

        fileInputStream = new FileInputStream(pFilename);
        inputStreamReader = new InputStreamReader(fileInputStream,
                                                       settings.mainFileFormat);

        in = new BufferedReader(inputStreamReader);

        processHeader(in); //handle the header section

        String line = "";

        //allow each chart group to load data, pass blank line in first time,
        //thereafter it will contain the last line read from the call to
        //loadSegment and will be passed on to the following call

        for (int i = 0; i < numberOfChartGroups; i++) {
            line = chartGroups[i].loadSegment(in, line);
        }

        }// try
    catch (FileNotFoundException e){
        return("Could not find the requested file.");
        }
    catch(IOException e){
        return(e.getMessage());
        }
    finally{
        try{if (in != null) {in.close();}}
        catch(IOException e){}
        try{if (inputStreamReader != null) {inputStreamReader.close();}}
        catch(IOException e){}
        try{if (fileInputStream != null) {fileInputStream.close();}}
        catch(IOException e){}
        }

    return("");

}//end of ViewerReporter::loadSegmentHelper
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::getFileCreationDateTimeString
//
// Returns the file creation timestamp for pFilename as a formatted String.
//
// On Linux systems, this returns the last modified date.
//

private String getFileCreationDateTimeString(String pFilename)
{

    Path path = Paths.get(pFilename);

    BasicFileAttributes attr;

    try{
        attr = Files.readAttributes(path, BasicFileAttributes.class);
    }catch (IOException e){
        return("");
    }

    Date date = new Date(attr.lastModifiedTime().toMillis());

    SimpleDateFormat simpleDateFormat =
                                   new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    String timeStamp = simpleDateFormat.format(date);

    return(timeStamp);

}//end of ViewerReporter::getFileCreationDateTimeString
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::loadInfoHelper
//
// Loads the info for a segment from the specified file.  See the loadSegment
// function for more info.
//

private void loadInfoHelper(String pFilename)
{

    pieceIDInfo.loadData(pFilename);

}//end of ViewerReporter::loadInfoHelper
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::processHeader
//
// Processes the header section of a segment data file via pIn.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//

private String processHeader(BufferedReader pIn) throws IOException

{

    String line;
    boolean success;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //read until end of file reached or "[Header Start]" section tag reached

    success = false;
    while ((line = pIn.readLine()) != null){
        if (Tools.matchAndParseString(line, "[Header Start]", "", matchSet)){
            success = true; break;
            }
        }

    if (!success) {
        throw new IOException(
                     "The file could not be read - missing header.");
    }

    //scan the header section and parse its entries

    //defaults in case not found
    segmentDataVersion = "0.0";
    measuredLengthText = "";
    inspectionDirection = "Unknown";

    success = false;
    while ((line = pIn.readLine()) != null){

        //stop if the end of the header is reached - header read is successful
        if (Tools.matchAndParseString(line, "[Header End]", "", matchSet)){
            success = true;
            break;
            }

        //this code is SUSPECT -- entries probably have to be in same order
        //for this to work -- needs to only set variable if line matches

        //read the "Segment Data Version" entry
        if (Tools.matchAndParseString(line, "Segment Data Version", "0.0", matchSet)) {
            segmentDataVersion = matchSet.rString1;
        }

        //read the "Measured Length" entry
        if (Tools.matchAndParseString(line, "Measured Length", "0.0", matchSet)){
            measuredLengthText = matchSet.rString1;
            try{measuredLength = Double.valueOf(measuredLengthText);}
            catch(NumberFormatException nfe){ measuredLength = 0;}
        }

        //read the "Inspection Direction" entry
        if (Tools.matchAndParseString(
                           line, "Inspection Direction", "Unknown", matchSet)) {
            inspectionDirection = matchSet.rString1;
        }

    }//while ((line = pIn.readLine()) != null)


    if (!success) {
        throw new IOException(
              "The file could not be read - missing end of header.");
    }

    return(line); //should be "[Header End]" tag on success, unknown value if not

}//end of ViewerReporter::processHeader
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile()
{

    IniFile calFile;

    //if the ini file cannot be opened and loaded, exit without action
    try {

        calFile = new IniFile(jobPrimaryPath + "00 - "
                            + currentJobNamePathFriendly
                            + " Calibration File.ini", settings.mainFileFormat);
        calFile.init();
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 712");
        return;
    }

    //since the Viewer does not create a Hardware object, load in any values
    //which are needed for viewing which would normally be loaded by the
    //Hardware class

    //NOTE -- debug MKS
    // THESE VALUES NEED TO BE SAVED WITH THE JOINT DATA FILE
    // AND READ FROM THERE INSTEAD OF THE CALIBRATION FILE
    // Each chart needs to store the variables in case there are multiple wall
    // charts.  If a chart doesn't use these values, then they can save random
    // values -- will be ignored when loaded for viewing/reporting.
    //WIP HSS// -- these may be needed later,
    //              but not at moment, only trying to load a single job
    /*hdwVs.nominalWall = calFile.readDouble("Hardware", "Nominal Wall", 0.250);

    hdwVs.nominalWallChartPosition =
                calFile.readInt("Hardware", "Nominal Wall Chart Position", 50);

    hdwVs.wallChartScale =
                     calFile.readDouble("Hardware", "Wall Chart Scale", 0.002);*/


    //don't load settings -- a pointer to the settings is passed to the Viewer
    // and these values are shared with the main program and other viewers --
    // the settings are already loaded by the main program

    //load info for all charts
    for (int i=0; i < numberOfChartGroups; i++) {
        chartGroups[i].loadCalFile(calFile);
    }

}//end of ViewerReporter::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//
// Should be overridden by subclasses to provide custom message handling.
//

public void displayErrorMessage(String pMessage, boolean pAutoDisappear)
{

}//end of ViewerReporter::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::closeErrorMessage
//
// Closes the active error message dialog.
//
// Should be overridden by subclasses to provide custom message handling.
//

public void closeErrorMessage()
{

}//end of ViewerReporter::closeErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::configure
//
// Creates a panel containing the appropriate chart groups to hold the data
// from a saved file.
//
// The panel is not added to any container -- the chart groups and their
// associated charts and traces can thus be used stand alone for printing
// or analyizing their data.
//

public void configure()
{

    IniFile configFile;

    //if the ini file cannot be opened and loaded, exit without action
    try {
        configFile = new IniFile(jobPrimaryPath + "01 - "
                                + currentJobNamePathFriendly
                                + " Configuration.ini",
                                settings.mainFileFormat);
        configFile.init();
        }
        catch(IOException e){
            logSevere(e.getMessage() + " - Error: 815");
            return;
        }

    numberOfChartGroups =
         configFile.readInt("Main Settings", "Number of Chart Groups", 1);

    //this panel will be used to hold the chart group so that they can easily
    //be added to a scrollpane
    chartGroupPanel = new JPanel();

    //create an array of chart groups per the config file setting
    if (numberOfChartGroups > 0){

        //protect against too many groups
        if (numberOfChartGroups > 10) {numberOfChartGroups = 10;}

        chartGroups = new ChartGroup[numberOfChartGroups];

        //pass null for the hardware object as that object is not needed for
        //viewing
        
        int i=0;
        do {
            chartGroups[i] = new ChartGroup(i, configFile, settings, 
                                                this, null);
            chartGroups[i].init();
            chartGroupPanel.add(chartGroups[i]);
        } while (i++<numberOfChartGroups);

        //first group will serve as the main frame/window
        mainFrame = chartGroups[0];

    }//if (numberOfChartGroups > 0)

    //NOTE -- debug MKS
    // pixelsPerInch VALUES NEED TO BE SAVED WITH THE JOINT DATA FILE
    // AND READ FROM THERE INSTEAD OF THE CONFIG FILE

    //WIP HSS//  may be used in future, but not at moment
    /*hdwVs.pixelsPerInch =
                    configFile.readDouble("Hardware", "Pixels per Inch", 1.0);

    hdwVs.decimalFeetPerPixel = 1/(hdwVs.pixelsPerInch * 12);*/

}//end of ViewerReporter::configure
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::actionPerformed
//
// Responds to button events.
//

@Override
public void actionPerformed(ActionEvent e)
{
    
    if ("Close Error Message".equals(e.getActionCommand())) {

        closeErrorMessage();

    }// if ("Close Error Message".equals(e.getActionCommand()))


}//end of ViewerReporter::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::isCalSelected
//
// Gets the state of the Cal joint switch.  This function should be overridden
// by subclasses to return the value of whatever object they are using to
// select the Cal state.
//

public boolean isCalSelected()
{

    return(false);

}//end of ViewerReporter::isCalSelected
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of ViewerReporter::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ViewerReporter::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of ViewerReporter::logStackTrace
//-----------------------------------------------------------------------------

}//end of class ViewerReporter
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

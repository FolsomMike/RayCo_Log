/******************************************************************************
* Title: SharedSettings.java
* Author: Mike Schoonover
* Date: 01/14/15
*
* Purpose:
*
* This class handles values shared amongst various classes.
*
* Init method loads the basic configuration settings from a file.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package model;

//-----------------------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import toolkit.Tools;

//-----------------------------------------------------------------------------
// class SharedSettings
//

public class SharedSettings{

    private final Object lock1 = new Object();

    JFrame mainFrame;

    public String appTitle;

    public String currentJobName = "";
    //illegal windows file chars escaped
    public String currentJobNamePathFriendly = "";
    public String jobPathPrimary = "";
    public String jobPathSecondary = "";
    public String dataPathPrimary = "", dataPathSecondary = "";
    public String calFileName = "";

    public boolean beginShutDown = false;
    public boolean isViewShutDown = false;
    public boolean beginHardwareShutDown = false;
    public boolean isHardwareShutDown = false;
    public boolean restartProgram = false;

    public boolean isCalDataSaved = false;

    public int lastPieceNumber;
    public int lastCalPieceNumber;
    public boolean startNewPieceAtLeftEdge = true;

    private ArrayList<ThresholdInfo> thresholdInfos = new ArrayList<>(10);
    public void addThresholdInfo(ThresholdInfo pInfo) { thresholdInfos.add(pInfo); }
    
    public static final int STOP_MODE = 0;
    public static final int SCAN_MODE = 1;
    public static final int INSPECT_MODE = 2;
    public int opMode = STOP_MODE;

    public String mainFileFormat = "UTF-8";

    static final String MAIN_CONFIG_SETTINGS_FILENAME =
                                             "Main Configuration Settings.ini";
    static final String MAIN_SETTINGS_FILENAME = "Main Settings.ini";
    static final String DEFAULT_PRIMARY_DATA_PATH = "Data Folder - Primary";
    static final String DEFAULT_SECONDARY_DATA_PATH = "Data Folder - Secondary";

    //This is the version of the format used to save the data for a segment which
    //holds data for an inspected piece.
    //version 1.0 saved with the "Threshold" tag misspelled as "Theshold"
    public static String SEGMENT_DATA_VERSION = "1.1";

    private static final int ERROR_LOG_MAX_SIZE = 10000;

//-----------------------------------------------------------------------------
// SharedSettings::SharedSettings (constructor)
//
//

public SharedSettings()
{

}//end of SharedSettings::SharedSettings (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::init
//

public void init(JFrame pMainFrame)
{

    mainFrame = pMainFrame;

    loadMainConfigSettings();

    verifyDataPathsAndCreateIfMissing();

    loadMainSettings();

    createJobPaths();

    calFileName = "00 - " + currentJobNamePathFriendly + " Calibration File.ini";

}// end of SharedSettings::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::createJobPaths
//
// Create paths to the primary and secondary job folders.
//

private void createJobPaths()
{

    jobPathPrimary = dataPathPrimary + currentJobNamePathFriendly;
    jobPathPrimary = trimAndAppendFileSeparatorIfMissing(jobPathPrimary);

    jobPathSecondary = dataPathSecondary + currentJobNamePathFriendly;
    jobPathSecondary = trimAndAppendFileSeparatorIfMissing(jobPathSecondary);

}// end of SharedSettings::createJobPaths
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::loadMainConfigSettings
//

private void loadMainConfigSettings()
{

    IniFile configFile;

    try {
        configFile = new IniFile(MAIN_CONFIG_SETTINGS_FILENAME, mainFileFormat);
        configFile.init();
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 78");
        return;
    }

    appTitle = configFile.readString(
                                "Main Settings", "application title", "Chart");

    dataPathPrimary = configFile.readString(
               "Main Settings", "primary data path", DEFAULT_PRIMARY_DATA_PATH);

    dataPathPrimary = trimAndAppendFileSeparatorIfMissing(dataPathPrimary);

    dataPathSecondary = configFile.readString(
           "Main Settings", "secondary data path", DEFAULT_SECONDARY_DATA_PATH);

    dataPathSecondary = trimAndAppendFileSeparatorIfMissing(dataPathSecondary);

}// end of SharedSettings::loadMainConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::loadMainSettings
//
// Loads settings which change frequently.
//

private void loadMainSettings()
{

    IniFile configFile;

    try {
        configFile = new IniFile(MAIN_SETTINGS_FILENAME, mainFileFormat);
        configFile.init();
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 142");
        return;
    }

    currentJobName = configFile.readString(
                                     "Main Settings", "current job name", "");
    currentJobNamePathFriendly = Tools.escapeIllegalFilenameChars(currentJobName);

    lastPieceNumber = configFile.readInt(
                 "Main Settings", "number of last piece processed", 0);

    lastCalPieceNumber = configFile.readInt(
             "Main Settings", "number of last calibration piece processed", 0);

}// end of SharedSettings::loadMainSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::getThresholdInfo
//
// Returns the ThresholdInfo object whose info matches the parameters.
//

public ThresholdInfo getThresholdInfo(int pChartGroup, int pChart, int pGraph,
                                        int pThreshold)
{

    for (ThresholdInfo info : thresholdInfos) {
        if (pChartGroup==info.getChartGroupNum()
                && pChart==info.getChartNum()
                && pGraph==info.getGraphNum()
                && pThreshold==info.getThresholdNum())
        { return info; }
    }

    return null;

}// end of SharedSettings::getThresholdInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::getThresholdInfosForGraph
//
// Returns a list of ThresholdInfo objects for pGraph of pChart of pChartGroup.
//

public ArrayList<ThresholdInfo> getThresholdInfosForGraph(int pChartGroup,
                                                            int pChart,
                                                            int pGraph)
{

    ArrayList<ThresholdInfo> infos = new ArrayList<>(thresholdInfos.size());

    for (ThresholdInfo info : thresholdInfos) {
        if (pChartGroup==info.getChartGroupNum()
                && pChart==info.getChartNum()
                && pGraph==info.getGraphNum())
        { infos.add(info); }
    }

    return infos;

}// end of SharedSettings::getThresholdInfosForGraph
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::save
//
// Saves all of the settings.
//

public void save()
{

    IniFile configFile;

    try {
        configFile = new IniFile(MAIN_SETTINGS_FILENAME, mainFileFormat);
        configFile.init();
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 142");
        return;
    }

    configFile.writeString("Main Settings", "current job name", currentJobName);
    configFile.writeInt("Main Settings", "number of last piece processed",
                                                                lastPieceNumber);
    configFile.writeInt("Main Settings",
                            "number of last calibration piece processed",
                            lastCalPieceNumber);
    configFile.save();

}//end of SharedSettings::save
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::verifyDataPathsAndCreateIfMissing
//
// Verifies that the data paths exist and creates them if not.
//

private void verifyDataPathsAndCreateIfMissing()
{

    if (!Files.exists(Paths.get(dataPathPrimary))) {
        logSevere("Primary Data Path not found, creating now - Error: 122");
        createDataFolder(dataPathPrimary, "primary");
    }

    if (!Files.exists(Paths.get(dataPathSecondary))) {
        logSevere("Secondary Data Path not found, creating now - Error: 136");
        createDataFolder(dataPathSecondary, "secondary");
    }

}// end of SharedSettings::verifyDataPathsAndCreateIfMissing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings:createDataFolder
//
// Creates data folder named pPath. On error, displays message using
// pErrorMsgID and logs that message.
//

private void createDataFolder(String pPath, String pErrorMsgID)
{

    File folder = new File(pPath);

    if (!folder.exists() && !folder.mkdirs()){
        displayErrorMessage("Could not create the " + pErrorMsgID
                                                        + " data directory.");
        logSevere("Could not create the " + pErrorMsgID
                                              + " data directory - Error: 160");
        return;
    }

}// end of SharedSettings::verifyDataPathsAndCreateIfMissing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::trimAndAppendFileSeparatorIfMissing
//
// pPath is trimmed of whitespace.
// If pPath does not already end with a file separator, one is added.
// The path, modified or not, is returned.
//

private String trimAndAppendFileSeparatorIfMissing(String pPath)
{

    pPath = pPath.trim();

    //add a separator if not one already at the end
    if (!pPath.endsWith(File.separator)) { pPath += File.separator; }

    return(pPath);

}// end of SharedSettings::trimAndAppendFileSeparatorIfMissing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::deleteFileIfOverSizeLimit
//
// If file pFilename is larger than pLimit, the file is deleted.
//

private void deleteFileIfOverSizeLimit(String pFilename, int pLimit)
{

    //delete the logging file if it has become too large

    Path p1 = Paths.get(pFilename);

    try {
        if (Files.size(p1) > pLimit){
            Files.delete(p1);
        }
    }
    catch(NoSuchFileException nsfe){
        //do nothing if file not found -- will be recreated as needed
    }
    catch (IOException e) {
        //do nothing if error on deletion -- will be deleted next time
        logStackTrace("Error: 152", e);
    }

}//end of SharedSettings::deleteFileIfOverSizeLimit
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::setupJavaLogger
//
// Prepares the Java logging system for use. Output is directed to a file.
//
// Each time the method is called, it checks to see if the file is larger
// than the maximum allowable size and deletes the file if so.
//

private void setupJavaLogger()
{

    String logFilename = "Java Logger File.txt";

    //prevent the logging file from getting too big
    deleteFileIfOverSizeLimit(logFilename, ERROR_LOG_MAX_SIZE);

    //remove all existing handlers from the root logger (and thus all child
    //loggers) so the output is not sent to the console

    Logger rootLogger = Logger.getLogger("");
    Handler[] handlers = rootLogger.getHandlers();
    for(Handler handler : handlers) {
        rootLogger.removeHandler(handler);
    }

    //add a new handler to send the output to a file

    Handler fh;

    try{

        //write log to logFilename, 10000 byte limit on each file, rotate
        //between two files, append the the current file each startup

        fh = new FileHandler(logFilename, 10000, 2, true);

        //direct output to a file for the root logger and  all child loggers
        Logger.getLogger("").addHandler(fh);

        //use simple text output rather than default XML format
        fh.setFormatter(new SimpleFormatter());

        //record all log messages
        Logger.getLogger("").setLevel(Level.WARNING);

    }
    catch(IOException e){
        logStackTrace("Error: 2539", e);
    }

}//end of SharedSettings::setupJavaLogger
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of SharedSettings::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of SharedSettings::logStackTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

private void displayErrorMessage(String pMessage)
{

    JOptionPane.showMessageDialog(mainFrame, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of SharedSettings::displayErrorMessage
//-----------------------------------------------------------------------------


}//end of class SharedSettings
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

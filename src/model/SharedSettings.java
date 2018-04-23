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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import toolkit.Tools;

//-----------------------------------------------------------------------------
// class SharedSettings
//

public class SharedSettings{

    private final Object lock1 = new Object();

    JFrame mainFrame;
    public String msg = "";
    private JLabel msgLabel;
    public void setMsgLabel(JLabel pM) { msgLabel = pM; }

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
    public boolean saveOnExit = false;

    public boolean isCalDataSaved = false;

    public boolean flaggingEnabled = false;
    
    public boolean calMode = false;
    public int nextPieceNumber = 1;
    public int nextCalPieceNumber = 1;
    public int lastPieceNumber;
    public int lastCalPieceNumber;
    public boolean startNewPieceAtLeftEdge = true;
    
    public double measuredLength;
    synchronized public double getMeasuredLength() { return measuredLength; }
    synchronized public void setMeasuredLength(double pV) { measuredLength = pV; }
    
    static public final int STOP_MODE = 0;
    static public final int SCAN_MODE = 1;
    static public final int INSPECT_MODE = 2;
    static public final int INSPECT_WITH_TIMER_TRACKING_MODE = 3;
    static public final int PAUSE_MODE = 4;
    public int opMode = STOP_MODE;
    public int opModePrev = STOP_MODE;
    
    public boolean updateView;
    
    public int scanSpeed = 1;
    public boolean timerDrivenTracking;
    public boolean timerDrivenTrackingInCalMode;
    
    //these will likely be changed by values read from IniFile
    public String pieceDescription = "Piece";
    //lower case of the above
    public String pieceDescriptionLC = "piece";
    //lower case of the above
    public String pieceDescriptionPlural = "Pieces";
    //lower case of the above
    public String pieceDescriptionPluralLC = "pieces";
    
    //these are the descriptions to be used for the direction the piece was
    //inspected -- towards home is towards the operator's compartment, away from
    //home is away from the operator's compartment -- these are loaded from the
    //configuration file so that they can be customized
    public String towardsHome, awayFromHome;
    public String inspectionDirectionDescription = "unknown";

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
    
    loadPieceNumberInfo();

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

}// end of SharedSettings::loadMainSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::loadPieceNumberInfo
//
// Loads settings such as the next piece and calibration piece numbers.
//

private void loadPieceNumberInfo()
{

    IniFile settingsFile;

    //if the ini file cannot be opened and loaded, exit without action
    try {
        
        String primaryPath = dataPathPrimary
                                + "02 - " + currentJobName 
                                + " Piece Number File.ini";
        
        settingsFile = new IniFile(primaryPath, mainFileFormat);
        settingsFile.init();
        
        }
        catch(IOException e){
            logSevere(e.getMessage() + " - Error: 268");
            return;
        }

    nextPieceNumber = settingsFile.readInt(
                                 "General", "Next Inspection Piece Number", 1);

    if (nextPieceNumber < 1) {nextPieceNumber = 1;}

    nextCalPieceNumber = settingsFile.readInt(
                                "General", "Next Calibration Piece Number", 1);

    if (nextCalPieceNumber < 1) {nextCalPieceNumber = 1;}

}// end of SharedSettings::loadPieceNumberInfo
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
    configFile.save();
    
    savePieceNumberInfo();

}//end of SharedSettings::save
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainController::savePieceNumberInfo
//
// Saves settings such as the next piece and calibration piece numbers.
//
// These settings often change as part of normal operation and so are saved
// separately from other settings which are rarely changed.
//

private void savePieceNumberInfo()
{

    //if the job path has not been set, don't save anything or it will be saved
    //int the program root folder -- this occurs when the current job path
    //specified in the Main Settings.ini

    String jobName = currentJobName;
    
    if (jobName.equals("")) { return; }
    
    try {
        
        String primaryPath = dataPathPrimary
                                + "02 - " + jobName + " Piece Number File.ini";
        String secondaryPath = dataPathSecondary
                                + "02 - " + jobName + " Piece Number File.ini";

        //save to primary data folders
        savePieceNumberInfoToPath(primaryPath);

        //save to secondary data folders
        savePieceNumberInfoToPath(secondaryPath);
    }
    catch(IOException e){ logSevere(e.getMessage()); }

}//end of MainController::savePieceNumberInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::savePieceNumberInfoToPath
//
// Saves settings such as the next piece and calibration piece numbers.
// The file is saved to the path specified by pPath.
//
// These settings often change as part of normal operation and so are saved
// separately from other settings which are rarely changed.
//

private void savePieceNumberInfoToPath(String pPath)
    throws IOException
{

    IniFile settingsFile;

    //if the ini file cannot be opened and loaded, exit without action
    try {
        settingsFile = new IniFile(pPath, mainFileFormat);
        settingsFile.init();
    }
    catch(IOException e){
        throw new IOException(e.getMessage() + " " 
                                + SharedSettings.class.getName() 
                                + " - Error: 344");
    }

    settingsFile.writeInt("General", "Next Inspection Piece Number", 
                            nextPieceNumber);

    settingsFile.writeInt("General", "Next Calibration Piece Number", 
                            nextCalPieceNumber);

    settingsFile.save(); //force save

}//end of SharedSettings::savePieceNumberInfoToPath
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
// SharedSettings::displayMsg
//
// Displays a message on the msgLabel using a threadsafe method.
//
// There is no bufferering, so if this function is called again before
// invokeLater calls displayMsgThreadSafe, the prior message will be
// overwritten.
//

public void displayMsg(String pMessage)
{

    msg = pMessage;

    javax.swing.SwingUtilities.invokeLater(this::displayMsgThreadSafe);

}//end of SharedSettings::displayMsg
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::displayMsgThreadSafe
//
// Displays a message on the msgLabel and should only be called from
// invokeLater.
//

private void displayMsgThreadSafe()
{

    if (msgLabel!=null) { msgLabel.setText(msg); }

}//end of SharedSettings::displayMsgThreadSafe
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

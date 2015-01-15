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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//-----------------------------------------------------------------------------
// class SharedSettings
//

public class SharedSettings{


    public String appTitle;
    
    String currentJobName = "";
    String primaryJobPath = ""; String secondaryJobPath = "";

    String mainFileFormat = "UTF-8";
    
    static final String MAIN_CONFIG_SETTINGS_FILENAME =
                                             "Main Configuration Settings.ini";
    static final String DEFAULT_PRIMARY_DATA_PATH = "Primary Data Folder";
    static final String DEFAULT_SECONARY_DATA_PATH = "Secondary Data Folder";

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

public void init()
{

    loadMainConfigSettings();
    
}// end of SharedSettings::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SharedSettings::loadMainConfigSettings
//

public void loadMainConfigSettings()
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
                                "Main Settings", "Application Title", "Chart");
    
}// end of SharedSettings::loadMainConfigSettings
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
        logStackTrace("Error: 2488", e);
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
// MainWindow::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of MainWindow::logStackTrace
//-----------------------------------------------------------------------------


}//end of class SharedSettings
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

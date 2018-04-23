/******************************************************************************
* Title: FileConverter.java
* Author: Mike Schoonover
* Date: 6/16/12
*
* Purpose:
*
* This is a parent class for converting files from one format to another.  It
* can convert multiple files in multiple folders.  It provides logging, error
* detection, error recovery.
*
* This is an abstract class and must be sub-classed for use.
*
* The sub class should specify a list of folders and the file extension of the
* files to be converted in each folder.  If multiple extensions need to be
* converted in the same folder, the folder is added to the list multiple times
* with a different extension specified with each entry.
*
* The sub class also specifies the suffix added to the end of each filename
* for the temporary file used for each -- this prevents collision with other
* temporary files.  It also specifies the name of the log file to be used for
* recording the operations and errors related to the conversion.  This way,
* each conversion process will have its own log file to help with debugging.
*
* Once all files are converted, which may require more than one start of the
* program in the event of an error, a flag file will be created.  Once this
* file is created, the particular conversion which created that file will not
* run again.  The sub class specifies the name to use for this flag file so
* that each type of conversion can operate independently.
*
* The sub classes should override these abstract methods to provide routines
* specific to the type of file being converted:
*
*   convertFile
*   compareFile
*
* -- Process Overview --
*
* Subclass initializes two arrays, one to hold a list of folders and the other
* to hold a list of extensions -- arrays should be the same size.
*
* Subclass sets up up log file name, temp file suffixes, etc.
*
* The subclass calls super.init to perform all conversion processes.
*
* The object iterates through the array, converting each file to a temp file.
*
* The object compares each temp file with its source file to verify.  At the
* same time on each successful compare, the old file is deleted and the new
* temp file is renamed to the name of the old file.
*
* If no error occurred at any time, a flag file is created.  On next startup,
* the conversion will not occur if that flag file is found.
*
* On error, no flag file is saved.  On next startup, conversion will be
* attempted again.  Any files already successfully converted will be ignored,
* any old version files found will be converted again.  If a temp file is found
* with no matching old version, it is assumed that an error occurred after the
* delete but before the rename, in which case the new file is simply renamed.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package model;

import model.LogFile;
import java.io.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MKSFilenameFilter
//
// This class is used to filter files by their extension.  The case of the
// extension is ignored.
//

class MKSFilenameFilter implements FilenameFilter
{

    String extensionUC = "";

//-----------------------------------------------------------------------------
// MKSFilenameFilter::MKSFilenameFilter
//
// Returns true if the name parameter meets the filter requirements, false
// otherwise.
//

public MKSFilenameFilter(String pExtension)
{

    extensionUC = "." + pExtension.toUpperCase();

}//end of MKSFilenameFilter::MKSFilenameFilter
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MKSFilenameFilter::accept
//
// Returns true if the name parameter meets the filter requirements, false
// otherwise.
//

@Override
public boolean accept(File dir, String name)
{

    //the file satisfies the filter if it ends with the extension value,
    //ignoring case
    if (name.toUpperCase().endsWith(extensionUC)) {
        return(true);
    }
    else {
        return(false);
    }

}//end of MKSFilenameFilter::accept
//-----------------------------------------------------------------------------

}//end of class MKSFilenameFilter
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class FileConverter
//
// See notes at top of page for details.
//

public abstract class FileConverter extends Object {

    LogFile logFile;
    boolean processGood = true;
    
    String logFilePath = "";

    static String newFormat = "UTF-8";    
    
    String tempFileSuffix;
    String logFilename;
    String conversionCompletedFlagFileName;

    String[] pathList;
    String[] extList;
    
    boolean convertEvenIfPreviouslyConverted = false;

//-----------------------------------------------------------------------------
// FileConverter::FileConverter (constructor)
//
// Method init() MUST be called after instantiation.
//

public FileConverter()
{


}//end of FileConverter::FileConverter (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileConverter::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Before calling, the subclass should initialize:
//  file extension and folder list
//  temporary conversion file suffix
//  log file name
//  name to use for the "conversion completed" flag file
//

protected void init()
{

    tempFileSuffix = " " + newFormat;
    logFilename = logFilePath + "File Format Conversion Log.txt";
    conversionCompletedFlagFileName = 
       logFilePath + "Config and Ini Files Have Been Converted To " + newFormat;
        
    processGood = true;

    //if the already converted flag file exists, exit without converting as the
    //files have already been converted
    //ignore this check if convertEvenIfPreviouslyConverted is true

    File file;
    
    if (!convertEvenIfPreviouslyConverted){
        file = new File(conversionCompletedFlagFileName);
        if (file.exists()) {return;}
    }

    //track all changes in a log file
    logFile = new LogFile();
    logFile.init(logFilename);
    logFile.section();

    //convert all the different file combinations
    logFile.log("Converting files to " + newFormat + ":");
    logFile.log(""); // blank line

    for (int x = 0; x < pathList.length; x++){
        if (!convertFiles(extList[x], pathList[x])) {processGood = false;}
        logFile.log(""); //log a blank line between each path/ext group
    }

    //verify all the converted files, delete old version, rename new to old
    //do clean up even if no files were converted in order to clean up after
    //past failed attempts
    
    logFile.log("Validating conversions and cleaning up:");
    logFile.log(""); // blank line

    for (int x = 0; x < pathList.length; x++){
        if (!compareFilesAndCleanUp(extList[x], pathList[x])) {
            processGood = false;
        }
        logFile.log(""); //log a blank line between each path/ext group
    }

    //if no errors encountered while converting, comparing, deleting, and
    //renaming the files, create a flag file to signal that the files have all
    //been converted to UTF-8 format -- this class checks for this flag file and
    //will do nothing if it is found so the conversion is not attempted when
    //all files have already been converted

    //see notes at top of this method for an overview of partial success

    if (processGood) {
        file = new File(conversionCompletedFlagFileName);
        try{file.createNewFile();}catch(IOException e){processGood = false;}
        logFile.log("All files converted successfully.");
    }
    else{
        logFile.log("One or more errors occurred during the conversion.");
    }

    logFile.close();

}//end of FileConverter::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileConverter::convertFiles
//
// Converts all files with extension pExtension in folder pPath.  The old files
// are not converted -- the new version is stored in a temp file with the same
// name as the old file but with a suffix appended.
//
// Returns true if no error, false on error.
//

private boolean convertFiles(String pExtension, String pPath)
{

    File file;
    String filename;
    boolean convertFilesGood = true;

    File jobDir = new File(pPath);
    //get a list of the files/folders in the directory
    String[] files = jobDir.list(new MKSFilenameFilter(pExtension));

    //if no files found, then do nothing
    if (files == null){
        logFile.log("No files found with extension '" + pExtension
                + "' in folder '" + pPath + "'");
        return(convertFilesGood); //this is not an error
    }
    
    for (String file1 : files) {
        filename = pPath + "/" + file1;
        file = new File(filename);
        //convert only normal files -- ignore directories -- if an error occurs
        //with any file, record the error
        if (file.isFile()) {
            if (convertFile(filename, filename + tempFileSuffix) == false) {
                convertFilesGood = false;
            }
        }
    } //for (String file1 : files)

    return(convertFilesGood);

}//end of FileConverter::convertFiles
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// UTF16LEToUTF8Converter::compareFilesAndCleanUp
//
// The old format and the new format versions for each file in the specified
// folder with the specified extension are compared. Only the last non-blank 
// lines are actually compared -- see compareFileLastLines for more expanation.
//
// For each set that match, the old version is deleted and the new version is
// renamed to the name of the old version.  If the old version does not exist
// due to an error during a previous attempt at cleanup and delete, then the
// temp file is simply renamed.
//
// It is expected that the new version of each file will have the same name
// as the old version with the name of the new format appended (" UTF-8,
// UTF-16LE, etc).
//
// Returns true if the files are identical, false if not identical or if an
// error is encountered.
//

private boolean compareFilesAndCleanUp(String pExtension, String pPath)
{

    String oldFile, tempFile;
    String tempFileExt = pExtension + tempFileSuffix;
    boolean compareFilesGood = true;
    

    File jobDir = new File(pPath);
    //get a list of the files/folders in the directory
    String[] files = jobDir.list(new MKSFilenameFilter(tempFileExt));

    //if no temp  files found, then do nothing -- this is not an error
    if (files == null){
        logFile.log("No temp files found with extension '" + tempFileExt
         + "' in folder '" + pPath + "'");
        return(compareFilesGood);
    }

    for (int i = 0; i < files.length; i++){

        //remove the tempFileSuffix in order to load old file version
        
        if(files[i].endsWith(tempFileSuffix)){
           //strip off the suffix
           files[i] = files[i].substring(0,
                                          files[i].lastIndexOf(tempFileSuffix));
        }

        oldFile = pPath + "/" + files[i];
        tempFile = oldFile + tempFileSuffix;

        //the new and old versions have identical content, then delete the old
        //version and rename the new version with the old filename

        if (compareFileLastLines(oldFile, tempFile)){
            if (deleteOldRenameNew(oldFile, tempFile) == false) {
                compareFilesGood = false;
            }
        }//if (compareFile(filename))
        else{
            //any compare error prevents the flag file from being created so
            //the conversion will run again on next start up (error is logged
            //by the compareFile function)
            compareFilesGood = false;
        }

    }//for (int i = 0...

    return(compareFilesGood);

}//end of Converter::compareFilesAndCleanUp
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileConverter::deleteOldRenameNew
//
// This function deletes pOldFile and renames pTempFile with the name for
// pOldFile.
//
// If no temp file is present the old file is NOT deleted.
//
// If the old version does not exist due to an error during a previous attempt
// at cleanup and delete, then the temp file is simply renamed.
//
// Returns true if no error, false on error.
//

protected boolean deleteOldRenameNew(String pOldFile, String pTempFile)
{

    boolean cleanUpGood = true;

    File oldFile = new File(pOldFile); File tempFile = new File(pTempFile);

    //if a new temp file is not present, then exit without error -- the temp
    //file was probably already renamed for this set on a previous conversion
    //attempt

    if (!tempFile.exists()){
        logFile.log("Delete and rename ignored -- there is no temp file: "
                                                                    + tempFile);
        return(cleanUpGood);
    }

    //if the old file exists, delete it -- bail out on error
    if (oldFile.exists()) {
        if(!oldFile.delete()){
            logFile.log("Error - could not delete: " + oldFile);
            cleanUpGood = false;
            return(cleanUpGood);
        }
    }else{     
        logFile.log(
        "Old file does not exist, temp file renamed to replace it: " + oldFile);        
    }

    //if old file successfully deleted, rename new file to old name
    if (tempFile.renameTo(oldFile)){
        logFile.log("Success -- temp file renamed: " + oldFile);
    }
    else{
        logFile.log("Error - could not rename: " + tempFile);
        cleanUpGood = false;
    }//if (newFile.renameTo(file))

return(cleanUpGood);

}//end of FileConverter::deleteOldRenameNew
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileConverter::convertFile
//
// This file must be implemented by the subclass.  The implementation should
// create a new file pTempFile from the old version pOldFile.  The old version
// should not be deleted in this method.
//
// Should return true if no error, false on error.
//

protected abstract boolean convertFile(String pOldFile, String pTempFile);


//end of FileConverter::convertFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileConverter::compareFileLastLines
//
// This file must be implemented by the subclass.  The implementation should
// compare the last line of pOldFile with pTempFile taking into account that
// each file might be of a different format.
//
// The old version should not be deleted in this method.
//
// Should return true if last lines are identical in content and there is no
// error, false if lines are not identical or on encountering an error.
//

protected abstract boolean compareFileLastLines(
                                            String pOldFile, String pTempFile);


//end of FileConverter::compareFileLastLines
//-----------------------------------------------------------------------------


}//end of class FileConverter
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

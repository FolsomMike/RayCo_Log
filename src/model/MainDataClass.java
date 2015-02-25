/******************************************************************************
* Title: MainDataClass.java - Main Source File for DSP Tools
* Author: Mike Schoonover
* Date: 9/30/13
*
* Purpose:
*
* This is a sample "model" class which handles some data: loads, saves, etc.
*
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package model;

//-----------------------------------------------------------------------------

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.ListIterator;

//-----------------------------------------------------------------------------
// class MainDataClass

public class MainDataClass extends Object{

    private String dataVersion = "1.0";

    private String [] data;

    private FileInputStream fileInputStream = null;
    private InputStreamReader inputStreamReader = null;
    private BufferedReader in = null;

    private FileOutputStream fileOutputStream = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedWriter out = null;
    
//-----------------------------------------------------------------------------
// MainDataClass::MainDataClass (constructor)
//
//

public MainDataClass()
{

}//end of MainDataClass::MainDataClass (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

public void init()
{


}//end of MainDataClass::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::loadUserSettingsFromFile
//
// Loads user settings from a file.
//

public ArrayList<String> loadUserSettingsFromFile()
{

    ArrayList<String> list = new ArrayList<>();
    
    loadFromTextFile("User Settings.txt", list);
    
    return(list);
    
}//end of MainDataClass::loadUserSettingsFromFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::saveUserSettingsToFile
//
// Saves user settings to a file.
//

public void saveUserSettingsToFile(ArrayList<String> pList)
{

    saveToTextFile("User Settings.txt", pList);

}//end of MainDataClass::saveUserSettingsToFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::getDataVersion
//
// Returns dataVersion.
//

public String getDataVersion()
{

    return dataVersion;

}//end of MainDataClass::getDataVersion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::getDataItem
//
// Returns item indexed by pIndex from array data.
//

public String getDataItem(int pIndex)
{

    return data[pIndex];

}//end of MainDataClass::getDataItem
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::setDataVersion
//
// Sets dataVersion to pValue.
//

public void setDataVersion(String pValue)
{

    dataVersion = pValue;

}//end of MainDataClass::setDataVersion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::setDataItem
//
// Sets data array element indexed by pIndex to pValue.
//

public void setDataItem(int pIndex, String pValue)
{

    data[pIndex] = pValue;

}//end of MainDataClass::setDataItem
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::updateSettingsFromUserInputs
//
// Updates all settings with the values currenly in the user input controls.
//

private void updateSettingsFromUserInputs(ArrayList<String>pList)
{
    
    
}// end of MainDataClass::updateSettingsFromUserInputs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::isLegalNumericChar
//
// Returns true if pChar is a numeric digit, a negative sign, or a decimal
// point. Returns false otherwise.
//

private boolean isLegalNumericChar(char pChar)
{

    switch(pChar){        
        case '0': return(true);
        case '1': return(true);
        case '2': return(true);
        case '3': return(true);
        case '4': return(true);
        case '5': return(true);
        case '6': return(true);
        case '7': return(true);
        case '8': return(true);
        case '9': return(true);
        case '-': return(true);
        case '.': return(true);
        default: return(false);
    }

}// end of MainDataClass::isLegalNumericChar
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::getIntFromListAtIndex
//
// Returns an integer parsed from a text line in pList at position pIndex.
// If an error occurs during parsing, zero is returned.
//

private int getIntFromListAtIndex(ArrayList pList, int pIndex)
{
   
    int value = 0;
    
    try{
        value = Integer.parseInt(((String)pList.get(pIndex)).trim());
    }
    catch(NumberFormatException e){
        //allow value to be returned as 0
    }
    
    return(value);
    
}// end of MainDataClass::getIntFromListAtIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::getDoubleFromListAtIndex
//
// Returns a double parsed from a text line in pList at position pIndex.
// If an error occurs during parsing, zero is returned.
//

private double getDoubleFromListAtIndex(ArrayList pList, int pIndex)
{
   
    double value = 0;
    
    try{
        value = Double.parseDouble(((String)pList.get(pIndex)).trim());
    }
    catch(NumberFormatException e){
        //allow value to be returned as 0
    }
    
    return(value);
    
}// end of MainDataClass::getDoubleFromListAtIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::getBooleanFromListAtIndex
//
// Returns a boolean parsed from a text line in pList at position pIndex.
// If an error occurs during parsing, false is returned.
//

private boolean getBooleanFromListAtIndex(ArrayList pList, int pIndex)
{
   
    boolean value = false;
    
    try{
        value = Boolean.parseBoolean(((String)pList.get(pIndex)).trim());
    }
    catch(NumberFormatException e){
        //allow value to be returned as false
    }
    
    return(value);
    
}// end of MainDataClass::getBooleanFromListAtIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::applyMultiplierPerUnits
//
// Multiplies pValue with the multiplier appropriate for the units specified
// by pUnits.
//
// pUnits Value     Units       Multiplier
//
//      0            Hz             1
//      1           kHz             1000
//      2           MHz             1000000
//

private double applyMultiplierPerUnits(double pValue, int pUnits)
{
   
    switch (pUnits) {
        case 0:  return( pValue * 1);       // Hz
        case 1:  return( pValue * 1000);    // kHz
        case 2:  return( pValue * 1000000); // MHz
        default: return( pValue * 1);
    }
    
}// end of MainDataClass::applyMultiplierPerUnits
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::loadFromTextFile
//
// Loads all text strings from  text file pFilename into pList.
//

public void loadFromTextFile(String pFilename, ArrayList<String> pList)
{

    try{

        openTextInFile(pFilename);

        readDataFromTextFile(pList);

    }
    catch (IOException e){
        //display an error message and/or log the message
    }
    finally{
        closeTextInFile();
    }

}//end of MainDataClass::loadFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::openTextInFile
//
// Opens text file pFilename for reading.
//

private void openTextInFile(String pFilename) throws IOException
{

    fileInputStream = new FileInputStream(pFilename);
    inputStreamReader = new InputStreamReader(fileInputStream);
    in = new BufferedReader(inputStreamReader);

}//end of MainDataClass::openTextInFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::readDataFromTextFile
//
// Reads all strings from the text file into pList.
//

private void readDataFromTextFile(ArrayList<String> pList) throws IOException
{

    //read each data line or until end of file reached

    String line;

    if ((line = in.readLine()) != null){
        dataVersion = line;
    }
    else{
        return;
    }

    while((line = in.readLine()) != null){
        pList.add(line);
    }

}//end of MainDataClass::readDataFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::closeTextInFile
//
// Closes the text input file.
//

private void closeTextInFile()
{

    try{

        if (in != null) {in.close();}
        if (inputStreamReader != null) {inputStreamReader.close();}
        if (fileInputStream != null) {fileInputStream.close();}

    }
    catch(IOException e){

        //ignore error while trying to close the file
        //could log the error message in the future

    }

}//end of MainDataClass::closeTextInFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::saveToTextFile
//
// Saves all text strings in pList to text file pFilename.
//

public void saveToTextFile(String pFilename, ArrayList<String> pList)
{

    try{

        openTextOutFile(pFilename);

        saveDataToTextFile(pList);

    }
    catch (IOException e){
        //display an error message and/or log the message
    }
    finally{
        closeTextOutFile();
    }

}//end of MainDataClass::saveToTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::openTextOutFile
//
// Opens text file pFilename for writing.
//

private void openTextOutFile(String pFilename) throws IOException
{

    fileOutputStream = new FileOutputStream(pFilename);
    outputStreamWriter = new OutputStreamWriter(fileOutputStream);
    out = new BufferedWriter(outputStreamWriter);

}//end of MainDataClass::openTextOutFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::saveDataToTextFile
//
// Saves the data in pList to the text file.
//

private void saveDataToTextFile(ArrayList<String> pList) throws IOException
{

    String line;

    //write the data version number
    out.write("Data Version: " + dataVersion);
    out.newLine();

    //write each data line
    
    ListIterator i;

    for (i = pList.listIterator(); i.hasNext(); ){
        out.write((String)i.next());
        out.newLine();
    }        

}//end of MainDataClass::readDataFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainDataClass::closeTextOutFile
//
// Closes the text output file.
//

private void closeTextOutFile()
{

    try{

        if (out != null) {out.close();}
        if (outputStreamWriter != null) {outputStreamWriter.close();}
        if (fileOutputStream != null) {fileOutputStream.close();}

    }
    catch(IOException e){
        //ignore error while trying to close the file
        //could log the error message in the future
    }

}//end of MainDataClass::closeTextOutFile
//-----------------------------------------------------------------------------


}//end of MainDataClass
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

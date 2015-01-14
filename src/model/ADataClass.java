/******************************************************************************
* Title: ADataClass.java - Main Source File for DSP Tools
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
import view.MainView;
import view.Trace;

//-----------------------------------------------------------------------------
// class ADataClass

public class ADataClass extends Object{

    private String dataVersion = "1.0";

    private String [] data;

    ArrayList <Integer> coefficientList = new ArrayList<>();
    private int [] coefficients;
    private int [] FIRInputSamples;
    
    MainView mainView;
    
    static final int DATA_SIZE = 2;

    private FileInputStream fileInputStream = null;
    private InputStreamReader inputStreamReader = null;
    private BufferedReader in = null;

    private FileOutputStream fileOutputStream = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedWriter out = null;

    int timeScale;
    int upSampleMultiplier; double upSamplesPerSec; double secsPerUpSample;

    double sampleFrequency, samplePeriod;
    int sampleFreqUnits;
    int waveForm1Amplitude, waveForm2Amplitude;
    double waveForm1Frequency, waveForm2Frequency;
    int waveForm1FreqUnits, waveForm2FreqUnits;
    double waveForm1DegreesPerSec, waveForm2DegreesPerSec;
    boolean displaySamples;
    boolean displayZeroStuffedWaveForm;
    boolean displayFilteredWaveForm;
    int filterOutputScalingDivisor;

    ArrayList<Integer> samples = new ArrayList();    
    
    // position in list of strings for each setting as returned from the View

    private static final int TIME_SCALE_SETTING_INDEX = 0;
    private static final int UPSAMPLE_MULTIPLIER_SETTING_INDEX = 1;
    private static final int SAMPLE_FREQUENCY_SETTING_INDEX = 2;
    private static final int SAMPLE_FREQUENCY_UNITS_SETTING_INDEX = 3;
    private static final int WAVEFORM1_FREQUENCY_SETTING_INDEX = 4;
    private static final int WAVEFORM1_FREQUENCY_UNITS_SETTING_INDEX = 5;
    private static final int WAVEFORM1_AMPLITUDE_SETTING_INDEX = 6;
    private static final int WAVEFORM2_FREQUENCY_SETTING_INDEX = 7;
    private static final int WAVEFORM2_FREQUENCY_UNITS_SETTING_INDEX = 8;
    private static final int WAVEFORM2_AMPLITUDE_SETTING_INDEX = 9;
    private static final int DISPLAY_SAMPLES_SETTING_INDEX = 10;
    private static final int DISPLAY_ZERO_STUFFED_WAVEFORM_SETTING_INDEX = 11;
    private static final int DISPLAY_FILTERED_WAVEFORM_SETTING_INDEX = 12;
    private static final int SCALING_DIVISOR_SETTING_INDEX = 13;
    
    // output chart trace definitions
    private static final int INPUT_SIGNAL_TRACE = 0;
    // input chart trace definitions    
    private static final int SAMPLE_TRACE = 0;
    private static final int ZERO_STUFFED_TRACE = 1;        
    private static final int FILTERED_TRACE = 2;
    
//-----------------------------------------------------------------------------
// ADataClass::ADataClass (constructor)
//
//

public ADataClass()
{

}//end of ADataClass::ADataClass (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

public void init()
{

    //allocate the array and fill it with strings

    data = new String[DATA_SIZE];

    for (int i=0; i<DATA_SIZE; i++){

        data[i] = new String();
        data[i] = "";

    }

}//end of ADataClass::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::setMainView
//
// Sets a pointer to the MainView so that the Model can interract with it.
//

public void setMainView(MainView pMainView)
{

    mainView = pMainView;

}//end of ADataClass::setMainView
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::loadUserSettingsFromFile
//
// Loads user settings from a file.
//

public void loadUserSettingsFromFile()
{

    ArrayList<String> list = new ArrayList();
    
    loadFromTextFile("User Settings.txt", list);
    
    mainView.setAllUserInputData(list);    
    
}//end of ADataClass::loadUserSettingsFromFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::saveUserSettingsToFile
//
// Saves user settings to a file.
//

public void saveUserSettingsToFile()
{

    ArrayList<String> list = new ArrayList();    
    
    mainView.getAllUserInputData(list);
    
    saveToTextFile("User Settings.txt", list);

}//end of ADataClass::saveUserSettingsToFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getDataVersion
//
// Returns dataVersion.
//

public String getDataVersion()
{

    return dataVersion;

}//end of ADataClass::getDataVersion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getDataItem
//
// Returns item indexed by pIndex from array data.
//

public String getDataItem(int pIndex)
{

    return data[pIndex];

}//end of ADataClass::getDataItem
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::setDataVersion
//
// Sets dataVersion to pValue.
//

public void setDataVersion(String pValue)
{

    dataVersion = pValue;

}//end of ADataClass::setDataVersion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::setDataItem
//
// Sets data array element indexed by pIndex to pValue.
//

public void setDataItem(int pIndex, String pValue)
{

    data[pIndex] = pValue;

}//end of ADataClass::setDataItem
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::updateWaveforms
//
// Generates the input waveform from the user settings and displays
// it. Takes samples of the raw wave, stores them, and displays them.
//

public void updateWaveforms()
{

    updateSettingsFromUserInputs();    
    
    generateInputWaveform(); //creates the input signal

    displayProcessedWaveforms(); //displays processed samples of the input
    
    mainView.repaintCharts();
    
}//end of ADataClass::updateWaveforms
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::generateInputWaveform
//
// Generates the input waveform from the user settings.
//

public void generateInputWaveform()
{
    
    mainView.resetAllTraceDataForChart(MainView.LONG_CHART);

    samples.clear();
    boolean storeSample = false;
    double sampleTimePoint = 0;
    int sampleCount = 0;
    
    for (int i = 0; i < mainView.getChartWidth(); i++){

        //when the time for a sample reached, draw a circle on the data point
        if (i * secsPerUpSample >= sampleTimePoint){
          
            storeSample = true;
            
            sampleTimePoint = ++sampleCount * samplePeriod;
            
            //do this before inserting the point as insertion moves the
            //insertion point to the next buffer location
            mainView.setTraceFlagsAtCurrentInsertionPoint(
                       MainView.LONG_CHART, INPUT_SIGNAL_TRACE, Trace.CIRCLE);        
        }
        
        int point;

        point = generateWaveform1(i);
        
        point += generateWaveform2(i);
        
        mainView.insertDataPointInTrace(
                              MainView.LONG_CHART, INPUT_SIGNAL_TRACE, point);

        if (storeSample){ samples.add(point); storeSample = false; }

    }
    
}// end of ADataClass::generatInputWaveform
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::displayProcessedWaveforms
//
// Displays the samples taken of the input signal and waveforms created by
// applying processing to those samples.
//
// If displaySamples is true, then the samples will be inserted into
// the SAMPLE_TRACE trace and zero stuffed so that the processed samples will
// be visible. This trace does not connect the points, so the samples will be
// shown as discrete points.
//
// If displaySamples is false, all data points in SAMPLE_TRACE trace will be
// left zeroed so a flat, centered line will be displayed.
//
// If displayZeroStuffedWaveForm is true, then the samples will be inserted into
// the ZERO_STUFFED_TRACE trace and zero stuffed so that the processed
// waveform will be visible. This trace connects the points, so the entire
// zero stuffed trace will be shown.
//
// If displayZeroStuffedWaveForm is false, all data points in ZERO_STUFFED_TRACE
// trace will be left zeroed so a flat, centered line will be displayed.
//
// If displayFilteredWaveForm is true, then the samples will be inserted into
// the FILTERED_TRACE trace, zero stuffed, and filtered so that the processed
// waveform will be visible.
//
// If displayFilteredWaveForm is false, all data points in FILTERED_TRACE trace
// will be left zeroed so a flat, centered line will be displayed.
//

public void displayProcessedWaveforms()
{
    
    mainView.resetAllTraceDataForChart(MainView.TRANS_CHART);

    double sampleTimePoint = 0;
    
    ListIterator si = samples.listIterator();
    
    for (int i = 0; i < mainView.getChartWidth(); i++){
        
        //when the time for a sample reached, add the next sample to the trace
        
        if (i * secsPerUpSample >= sampleTimePoint && si.hasNext()){
        
            sampleTimePoint += samplePeriod;

            int dataPoint = (int)si.next();            
            
            if(displaySamples){
                //do this before inserting the point as insertion moves the
                //insertion point to the next buffer location
                mainView.setTraceFlagsAtCurrentInsertionPoint(
                          MainView.TRANS_CHART, SAMPLE_TRACE, Trace.CIRCLE);
            
                addDataPointToSampleWaveForm(dataPoint);
            }

            if (displayZeroStuffedWaveForm){
                addDataPointToZeroStuffedWaveForm(dataPoint);
            }
            
            if (displayFilteredWaveForm){
                addDataPointToFilteredWaveForm(dataPoint);
            }
            
        }else {
                
            //for points between samples, insert zeros so they will appear
            //as part of the baseline
            if (displaySamples){
                addDataPointToSampleWaveForm(0);
            }

            //zero stuff datapoints to display a zero-stuffed waveform
            if (displayZeroStuffedWaveForm){
                addDataPointToZeroStuffedWaveForm(0);
            }

            //zero stuff datapoints in between the samples so the FIR filter
            //can reconstruct the original waveform
                        
            if (displayFilteredWaveForm){
                addDataPointToFilteredWaveForm(0);
            }
                        
        }
    }
    
}// end of ADataClass::displayProcessedWaveforms
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::addDataPointToSampleWaveForm
//
// Adds pValue to the SAMPLE_TRACE trace.
// 

private void addDataPointToSampleWaveForm(int pValue)
{
   
    mainView.insertDataPointInTrace(
                                MainView.TRANS_CHART, SAMPLE_TRACE, pValue);
        
}// end of ADataClass::addDataPointToSampleWaveForm
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// ADataClass::addDataPointToZeroStuffedWaveForm
//
// Adds pValue to the ZERO_STUFFED_TRACE trace.
// 

private void addDataPointToZeroStuffedWaveForm(int pValue)
{
   
    mainView.insertDataPointInTrace(
                            MainView.TRANS_CHART, ZERO_STUFFED_TRACE, pValue);
        
}// end of ADataClass::addDataPointToZeroStuffedWaveForm
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::addDataPointToFilteredWaveForm
//
// Runs pValue through a FIR filter and adds the output to the FILTERED_TRACE
// trace.
// 

private void addDataPointToFilteredWaveForm(int pValue)
{

    //shift the old samples one place up in the buffer
    for(int i=FIRInputSamples.length-1; i>0; i--){
       FIRInputSamples[i] = FIRInputSamples[i-1];
    }
    
    //add new sample to the buffer
    FIRInputSamples[0] = pValue;
    
    long y = 0;
    
    //calculate the new output by running through the FIR filter
    
    for(int i=0; i<FIRInputSamples.length; i++){
        
        y += coefficients[i] * FIRInputSamples[i];
    
    }
        
    y = y / filterOutputScalingDivisor;
    
    mainView.insertDataPointInTrace(
                               MainView.TRANS_CHART, FILTERED_TRACE, (int)y);
    
}// end of ADataClass::addDataPointToFilteredWaveForm
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::generateWaveform1
//
// Generates one datapoint for waveform 1 from the user settings for x location
// pIndex.
// 

private int generateWaveform1(int pIndex)
{
        
    int degrees = calculateDegreesFromIndex(pIndex, waveForm1DegreesPerSec);    
    
    return( (int)( waveForm1Amplitude * Math.sin(Math.toRadians(degrees))) );
        
}// end of ADataClass::generateWaveform1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::generateWaveform2
//
// Generates one datapoint for waveform 2 from the user settings for x location
// pIndex.
// 

private int generateWaveform2(int pIndex)
{
    
    int degrees = calculateDegreesFromIndex(pIndex, waveForm2DegreesPerSec);    
    
    return( (int)( waveForm2Amplitude * Math.sin(Math.toRadians(degrees))) );

}// end of ADataClass::generateWaveform2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::calculateDegreesFromIndex
//
// Calculates the angular degree from pIndex which is also the x pixel location.
// 


private int calculateDegreesFromIndex(int pIndex, double pDegreesPerSec)
{
    
    return (int)( pIndex * secsPerUpSample * pDegreesPerSec);
            
}// end of ADataClass::calculateDegreesFromIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::updateSettingsFromUserInputs
//
// Updates all settings with the values currenly in the user input controls.
//

private void updateSettingsFromUserInputs()
{

    formatFIRCoefficientsFromUserInputs(); //reformat coefficient inputs
    
    ArrayList<String> list = new ArrayList();    
    
    mainView.getAllUserInputData(list);
    
    timeScale = getIntFromListAtIndex(list, TIME_SCALE_SETTING_INDEX);

    mainView.setAllChartAllTraceXScale(timeScale);
    
    upSampleMultiplier =
            getIntFromListAtIndex(list, UPSAMPLE_MULTIPLIER_SETTING_INDEX);

    sampleFrequency =    
            getDoubleFromListAtIndex(list, SAMPLE_FREQUENCY_SETTING_INDEX);

    sampleFreqUnits =
           getIntFromListAtIndex(list, SAMPLE_FREQUENCY_UNITS_SETTING_INDEX);
            
    sampleFrequency = 
                applyMultiplierPerUnits(sampleFrequency, sampleFreqUnits);

    if (sampleFrequency <= 0) { sampleFrequency = 1; }
    
    samplePeriod = 1 / sampleFrequency;

    //samples per second after up-sampling by inserting zeroed samples

    upSamplesPerSec = sampleFrequency * upSampleMultiplier;
    
    //protect against divide by zero
    if (upSamplesPerSec <= 0){ upSamplesPerSec = 1; } 
    
    secsPerUpSample = 1.0 / upSamplesPerSec;
        
    waveForm1Amplitude =
            getIntFromListAtIndex(list, WAVEFORM1_AMPLITUDE_SETTING_INDEX);

    waveForm2Amplitude =
            getIntFromListAtIndex(list, WAVEFORM2_AMPLITUDE_SETTING_INDEX);    
    
    waveForm1Frequency =    
            getDoubleFromListAtIndex(list, WAVEFORM1_FREQUENCY_SETTING_INDEX);

    waveForm1FreqUnits =
           getIntFromListAtIndex(list, WAVEFORM1_FREQUENCY_UNITS_SETTING_INDEX);
            
    waveForm1Frequency = 
                applyMultiplierPerUnits(waveForm1Frequency, waveForm1FreqUnits);
    
    if (waveForm1Frequency <= 0) { waveForm1Frequency = 1; }
    
    waveForm1DegreesPerSec = waveForm1Frequency * 360;
    
    waveForm2Frequency =
            getDoubleFromListAtIndex(list, WAVEFORM2_FREQUENCY_SETTING_INDEX);

    waveForm2FreqUnits =
           getIntFromListAtIndex(list, WAVEFORM2_FREQUENCY_UNITS_SETTING_INDEX);
    
    waveForm2Frequency = 
                applyMultiplierPerUnits(waveForm2Frequency, waveForm2FreqUnits);
    
    if (waveForm2Frequency <= 0) { waveForm2Frequency = 1; }
    
    waveForm2DegreesPerSec = waveForm2Frequency * 360;

    displaySamples = getBooleanFromListAtIndex(
                                           list, DISPLAY_SAMPLES_SETTING_INDEX);
        
    displayZeroStuffedWaveForm = getBooleanFromListAtIndex(
                            list, DISPLAY_ZERO_STUFFED_WAVEFORM_SETTING_INDEX);
                        
    displayFilteredWaveForm =
      getBooleanFromListAtIndex(list, DISPLAY_FILTERED_WAVEFORM_SETTING_INDEX);
    
    updateFIRCoefficientsFromUserInputs(list);
    
    FIRInputSamples = new int[coefficients.length];
    
    for(int i=0; i<FIRInputSamples.length; i++){ 
        FIRInputSamples[i] = 0;
    }
    
    filterOutputScalingDivisor =
                   getIntFromListAtIndex(list, SCALING_DIVISOR_SETTING_INDEX);
    
    if (filterOutputScalingDivisor <= 0) { filterOutputScalingDivisor = 1; }
    
}// end of ADataClass::updateSettingsFromUserInputs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::updateFIRCoefficientsFromUserInputs
//
// Extracts the user's FIR filter coefficients from pSettingsList, stores them
// in ArrayList coefficientList and as integers in array coefficients.
//    
//

private void updateFIRCoefficientsFromUserInputs(ArrayList pSettingsList)
{

    //clear the list and release the old coefficients array
    coefficientList.clear();
    coefficients = null;
    
    ListIterator iter = pSettingsList.listIterator();

    //search for the start of the coefficients block
    
    while(iter.hasNext()){

        String input = (String)iter.next();
        
        if (input.equals("<start of coefficients>")){ break; }

    }
    
    if(!iter.hasNext()) return; //bail out if section title not found
    
    Integer value;
    
    while(iter.hasNext()){
    
        String input = ((String)iter.next()).trim();

        if (input.equals("<end of coefficients>")){ break; }        
        
        try{
            value = Integer.valueOf(input);
        }
        catch(NumberFormatException e){
            value = 0;
        }
                
        coefficientList.add(value);
        
    }

    //create an integer array to hold the coefficients for faster processing
    coefficients = new int[coefficientList.size()];
    
    int index = 0;
    
    for (iter = coefficientList.listIterator(); iter.hasNext();){
        coefficients[index++] = (int)iter.next();
    }

}// end of ADataClass::updateFIRCoefficientsFromUserInputs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::formatFIRCoefficientsFromUserInputs
//
// Retrieves the list of coefficients from the GUI input, removes extraneous
// characters (anything not a number, negative sign, or decimal point) from
// the beginning and the end of each coefficient string and stores the new
// values back into the GUI control.
//
// Any blank lines in the list will also be removed.
//

private void formatFIRCoefficientsFromUserInputs()
{

    ArrayList <String> inList = new ArrayList<>();
    ArrayList <String> outList = new ArrayList<>();

    outList.add("<start of coefficients>");
    
    //retrieve user input values from GUI control
    mainView.getUserFilterCoeffInput(inList);    
    
    ListIterator iter;

    //search for the start of the coefficients block
    
    for (iter = inList.listIterator(); iter.hasNext();){

        String input = ((String)iter.next()).trim();
        
        //ignore header lines
        if (input.equals("<start of coefficients>")){ continue; }
        if (input.equals("<end of coefficients>")){ continue; }

        String formatted = formatCoeff(input);

        //ignore any line which ends up being blank
        if (formatted.isEmpty()){ continue; }        
        
        outList.add(formatted);
        
    }
    
    //set the GUI control with the newly formatted text
    
    outList.add("<end of coefficients>");
    
    iter = outList.listIterator();
    mainView.setUserFilterCoeffInput(iter);
    
}// end of ADataClass::formatFIRCoefficientsFromUserInputs
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::formatCoeff(input)
//
// Strips pInput of any illegal characters. Illegal characters are any other
// than numeric digits, the negative sign, and the decimal point.
//
// A blank line may be returned if no legal characters are found. 
//

private String formatCoeff(String pInput)
{

    String formatted = "";
    
    //scan input, transferring all legal characters to formatted string
    
    for (int i=0; i<pInput.length(); i++){
        if (isLegalNumericChar(pInput.charAt(i))){
            formatted = formatted + pInput.charAt(i);
        }        
    }
    
    return(formatted);

}// end of ADataClass::formatCoeff
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::isLegalNumericChar
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

}// end of ADataClass::isLegalNumericChar
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getIntFromListAtIndex
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
    
}// end of ADataClass::getIntFromListAtIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getDoubleFromListAtIndex
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
    
}// end of ADataClass::getDoubleFromListAtIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getBooleanFromListAtIndex
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
    
}// end of ADataClass::getBooleanFromListAtIndex
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::applyMultiplierPerUnits
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
    
}// end of ADataClass::applyMultiplierPerUnits
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::loadFromTextFile
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

}//end of ADataClass::loadFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::openTextInFile
//
// Opens text file pFilename for reading.
//

private void openTextInFile(String pFilename) throws IOException
{

    fileInputStream = new FileInputStream(pFilename);
    inputStreamReader = new InputStreamReader(fileInputStream);
    in = new BufferedReader(inputStreamReader);

}//end of ADataClass::openTextInFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::readDataFromTextFile
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

}//end of ADataClass::readDataFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::closeTextInFile
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

}//end of ADataClass::closeTextInFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::saveToTextFile
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

}//end of ADataClass::saveToTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::openTextOutFile
//
// Opens text file pFilename for writing.
//

private void openTextOutFile(String pFilename) throws IOException
{

    fileOutputStream = new FileOutputStream(pFilename);
    outputStreamWriter = new OutputStreamWriter(fileOutputStream);
    out = new BufferedWriter(outputStreamWriter);

}//end of ADataClass::openTextOutFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::saveDataToTextFile
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

}//end of ADataClass::readDataFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::closeTextOutFile
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

}//end of ADataClass::closeTextOutFile
//-----------------------------------------------------------------------------


}//end of ADataClass
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

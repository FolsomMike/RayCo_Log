/******************************************************************************
* Title: Threshold.java
* Author: Hunter Schoonover & Mike Schoonover
* Date: 09/19/2017
*
* Purpose:
*
* This class handles a single threshold.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import java.awt.*;
import java.io.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Threshold
//
// This class creates and controls a trace.
//

public class Threshold extends Object{

    IniFile configFile;
    private final String section;
    public int chartGroupNum, chartNum, graphNum, thresholdNum;
    private int width, height;
    private int xMax, yMax;
    private Color backgroundColor;

    private double xScale = 1.0, yScale = 1.0;
    private int offset = 0;
    private int baseLine = 0;

    public static int flagWidth = 5;
    public static int flagHeight = 7;

    public boolean okToMark = true;

    public String title;
    String shortTitle;
    boolean doNotFlag, flagOnOver;
    public Color thresholdColor;

    public int thresholdLevel;
    int plotThresholdLevel;
    public int alarmChannel;
    boolean invert;

    // references to point at the controls used to adjust the values - these
    // references are set up by the object which handles the adjusters and are
    // only used temporarily

    public Object levelAdjuster;


//-----------------------------------------------------------------------------
// Threshold::Threshold (constructor)
//
// The parameter configFile is used to load configuration data.  The IniFile
// should already be opened and ready to access.
//

public Threshold(IniFile pConfigFile, int pChartGroupNum, int pChartNum,
                    int pGraphNum, int pThresholdNum, int pWidth, int pHeight,
                    Color pBackgroundColor)
{

    configFile = pConfigFile;
    chartGroupNum = pChartGroupNum;
    chartNum = pChartNum;
    graphNum = pGraphNum;
    thresholdNum = pThresholdNum;
    width = pWidth; height = pHeight;
    xMax = width - 1; yMax = height - 1;
    backgroundColor = pBackgroundColor;

    section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                + " Graph " + graphNum + " Threshold " + thresholdNum;

}//end of Threshold::Threshold (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init()
{

    //read the configuration file and create/setup the charting/control elements
    configure(configFile);

}// end of Threshold::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::configure
//
// Loads configuration settings from the configuration.ini file.
//

private void configure(IniFile pConfigFile)
{

    title = pConfigFile.readString(section, "Title", "*");

    shortTitle = pConfigFile.readString(section, "Short Title", "*");

    doNotFlag = pConfigFile.readBoolean(
                            section, "Do Not Flag - For Reference Only", false);

    flagOnOver = pConfigFile.readBoolean(section, "Flag On Over", true);

    thresholdColor = pConfigFile.readColor(section, "Color", Color.RED);

    invert = pConfigFile.readBoolean(section, "Invert Threshold", true);

    thresholdLevel = pConfigFile.readInt(section, "Default Level", 50);

    alarmChannel = pConfigFile.readInt(section, "Alarm Channel", 0);

    offset = configFile.readInt(section, "offset", 0);
    xScale = configFile.readDouble(section, "x scale", 1.0);
    yScale = configFile.readDouble(section, "y scale", 1.0);
    baseLine = configFile.readInt(section, "baseline", 0);

    setThresholdLevel(thresholdLevel);

}//end of Threshold::configure
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::loadCalFile
//
// This loads the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile(IniFile pCalFile)
{

    thresholdLevel = pCalFile.readInt(section, "Threshold Level", thresholdLevel);

}//end of Threshold::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::saveCalFile
//
// This saves the file used for storing calibration information pertinent to a
// job, such as gains, offsets, thresholds, etc.
//
// Each object is passed a pointer to the file so that they may save their
// own data.
//

public void saveCalFile(IniFile pCalFile)
{

    pCalFile.writeInt(section, "Threshold Level", thresholdLevel);

}//end of Threshold::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::calculateY
//
// Calculates and returns the scaled and offset y derived from pY.
//

private int calculateY(int pY)
{

    return (int)((pY - baseLine) * yScale) + offset;

}// end of Threshold::calculateY
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::setThresholdLevel
//
// Sets the level for the threshold indexed by pWhich.
//

public void setThresholdLevel(int pLevel)
{

    thresholdLevel = pLevel;

    plotThresholdLevel = calculateY(thresholdLevel);
    if(plotThresholdLevel < 0) {plotThresholdLevel = 0;}
    if(plotThresholdLevel > height) {plotThresholdLevel = height;}

    //invert the y position if specified
    if (invert){ plotThresholdLevel = height - plotThresholdLevel; }

}//end of Threshold::setThresholdLevel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::paintThresholdLine
//
// Draws the threshold from pixel pStart to pixel pEnd.
//

public void paintThresholdLine(Graphics2D pG2)

{

    pG2.setColor(thresholdColor);
    pG2.drawLine(0, plotThresholdLevel, xMax, plotThresholdLevel);

}//end of Threshold::paintThresholdLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::drawSlice
//
// Draws one dot of the threshold.
//

public void drawSlice(Graphics2D pG2, int xPos)

{

    pG2.setColor(thresholdColor);
    //draw a dot to make the threshold
    pG2.drawRect(xPos, plotThresholdLevel, 0, 0);

}//end of Threshold::drawSlice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::checkViolation
//
// Returns true signal exceeds the threshold level.  Whether this is above or
// below the threshold is determined by flagOnOver.
//
// The threshold with the lowest (0) thresholdIndex is the highest severity
// threshold, highest index is lowest.  This function should be called for the
// thresholds in order of their index which happens automatically if they are
// stored in an array in this order.  If called in this order, no more
// thresholds should be checked after one returns true because lower severity
// thresholds should not override higher ones.
//
// If doNotFlag is true, the threshold is for reference purposes only and no
// violations will ever be recorded.
//
// NOTE: For this function, the threshold is not inverted and the pSigHeight
// should not be inverted as well.
//

public boolean checkViolation(int pSigHeight)

{

    //if the threshold is non-flagging, return without action
    if (doNotFlag) {return(false);}

    //if the signal level exceeds the threshold, draw a flag - if flagOnOver is
    //true check for signal above, if false check for signal below
    if (flagOnOver){
        if (pSigHeight >= thresholdLevel) {return(true);}
    }
    else{
        if (pSigHeight <= thresholdLevel) {return(true);}
    }

    return(false); //no flag set

}//end of Threshold::checkViolation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::drawFlag
//
// Draws a flag with the threshold color at location xPos,pSigHeight.
//

public void drawFlag(Graphics2D pPG2, int pXPos, int pYPos)
{

    //if flag would be drawn above or below the screen, force on screen
    if (pYPos < 0) {pYPos = 0;}
    if (pYPos > height) {pYPos = height - flagHeight;}

    //add 1 to xPos so flag is drawn to the right of the peak

    pPG2.setColor(thresholdColor);
    pPG2.fillRect(pXPos+1, pYPos, flagWidth, flagHeight);

}//end of Threshold::drawFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::saveSegment
//
// Saves the thresholds settings to the open file pOut.
//

public void saveSegment(BufferedWriter pOut) throws IOException
{

    pOut.write("[Threshold]"); pOut.newLine();
    pOut.write("Threshold Index=" + thresholdNum); pOut.newLine();
    pOut.write("Threshold Title=" + title); pOut.newLine();
    pOut.write("Threshold Short Title=" + shortTitle); pOut.newLine();
    pOut.newLine();

    pOut.write("Threshold Level=" + thresholdLevel); //save the threshold level
    pOut.newLine(); pOut.newLine();

}//end of Threshold::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//

public void updateDimensions(int pNewWidth, int pNewHeight)
{

    width = pNewWidth; height = pNewHeight;

    xMax = width - 1; yMax = height - 1;

}// end of Threshold::updateDimensions
//-----------------------------------------------------------------------------

}//end of class Threshold
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
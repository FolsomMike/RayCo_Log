/******************************************************************************
* Title: Threshold.java
* Author: Hunter Schoonover
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
import java.util.ArrayList;
import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Threshold
//
// This class creates and controls a trace.
//

public class Threshold extends Object{

    private final SharedSettings sharedSettings;

    private String title;
    private String shortTitle;
    private boolean doNotFlag, flagOnOver;
    private Color thresholdColor;
    private final IniFile configFile;
    private GraphInfo graphInfo;
    private final String section;
    private final int chartGroupNum, chartNum, graphNum, thresholdNum;
    private boolean visible = true;
    private Color backgroundColor;
    private int width, height;
    private int flagWidth, flagHeight;

    private int xMax, yMax;
    private int prevX = -1, prevY = Integer.MAX_VALUE;

    private double xScale = 1.0, yScale = 1.0;
    private int offset = 0;
    private int baseLine = 0;

    private boolean okToMark = true;

    private int plotThresholdLevel;
    private int alarmChannel;
    private boolean invert;

    // references to point at the controls used to adjust the values - these
    // references are set up by the object which handles the adjusters and are
    // only used temporarily

    private Object levelAdjuster;


//-----------------------------------------------------------------------------
// Threshold::Threshold (constructor)
//
// The parameter configFile is used to load configuration data.  The IniFile
// should already be opened and ready to access.
//

public Threshold(SharedSettings pSettings, IniFile pConfigFile,
                    GraphInfo pGraphInfo, int pChartGroupNum,
                    int pChartNum, int pGraphNum, int pThresholdNum, int pWidth,
                    int pHeight, Color pBackgroundColor)
{

    sharedSettings = pSettings;
    configFile = pConfigFile;
    graphInfo = pGraphInfo;
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

    title = pConfigFile.readString(section, "title", "*");

    shortTitle = pConfigFile.readString(section, "short title", "*");

    doNotFlag = pConfigFile.readBoolean(
                            section, "do not flag - for reference only", false);

    thresholdColor = pConfigFile.readColor(section, "color", Color.RED);

    invert = pConfigFile.readBoolean(section, "invert threshold", true);

    int thres = pConfigFile.readInt(section, "default level", 50);
    sharedSettings.setThresholdLevel(section, thres);
    setThresholdLevel(thres);

    alarmChannel = pConfigFile.readInt(section, "alarm channel", 0);

    flagOnOver = pConfigFile.readBoolean(section, "flag on over", true);
    flagWidth = pConfigFile.readInt(section, "flag width", 5);
    flagHeight = pConfigFile.readInt(section, "flag height", 7);

    offset = configFile.readInt(section, "offset", 0);
    xScale = configFile.readDouble(section, "x scale", 1.0);
    yScale = configFile.readDouble(section, "y scale", 1.0);
    baseLine = configFile.readInt(section, "baseline", 0);

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
// Threshold::drawFlag
//
// Draws a flag with the threshold color at location xPos,pSigHeight.
//

private void drawFlag(Graphics2D pPG2, int pXPos, int pYPos)
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
// Threshold::paintSingleDataPoint
//
// Draws the threshold line, and if necessary the flag.
//

public void paintSingleDataPoint(Graphics2D pG2, int pDataIndex)
{

    if(!visible) { return; }

    //calculate the x position in pixels
    int x = (int)Math.round(pDataIndex * xScale);

    //adjust for any scrolling that has occurred before plotting
    int xAdj = x - graphInfo.scrollOffset;
    int prevXAdj = prevX - graphInfo.scrollOffset;

    //draw threshold line
    pG2.setColor(thresholdColor);
    pG2.drawLine(xAdj, plotThresholdLevel, xAdj, plotThresholdLevel);

}// end of Threshold::paintSingleDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::paintThresholdLine
//
// Draws the threshold line all the way across the graph.
//

public void paintThresholdLine(Graphics2D pG2)

{

    pG2.setColor(thresholdColor);
    pG2.drawLine(0, plotThresholdLevel, xMax, plotThresholdLevel);

}//end of Threshold::paintThresholdLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::saveSegment
//
// Saves the thresholds settings to the open file pOut.
//

public void saveSegment(BufferedWriter pOut) throws IOException
{

    /*//DEBUG HSS//pOut.write("[Threshold]"); pOut.newLine();
    pOut.write("Threshold Index=" + thresholdNum); pOut.newLine();
    pOut.write("Threshold Title=" + title); pOut.newLine();
    pOut.write("Threshold Short Title=" + shortTitle); pOut.newLine();
    pOut.newLine();

    pOut.write("Threshold Level=" + thresholdLevel); //save the threshold level
    pOut.newLine(); pOut.newLine();*/

}//end of Threshold::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::setThresholdLevel
//
// Sets the level for the threshold indexed by pWhich.
//

public void setThresholdLevel(int pLevel)
{

    plotThresholdLevel = calculateY(pLevel);
    if(plotThresholdLevel < 0) {plotThresholdLevel = 0;}
    if(plotThresholdLevel > height) {plotThresholdLevel = height;}

    //invert the y position if specified
    if (invert){ plotThresholdLevel = height - plotThresholdLevel; }

}//end of Threshold::setThresholdLevel
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
//-----------------------------------------------------------------------------\

}//end of class Threshold
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
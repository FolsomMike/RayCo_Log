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
import model.IniFile;
import model.SharedSettings;
import model.ThresholdInfo;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Threshold
//
// This class creates and controls a trace.
//

public class Threshold extends Object{

    private final SharedSettings sharedSettings;
    public int getLevel() { return thresholdInfo.getLevel(); }
    public void setLevel(int pLvl) { thresholdInfo.setLevel(pLvl); }

    private final ThresholdInfo thresholdInfo;
    public ThresholdInfo getThresholdInfo() { return thresholdInfo; }

    private final IniFile configFile;
    private GraphInfo graphInfo;
    public GraphInfo getGraphInfo() { return graphInfo; }
    private final String section;
    public String getSection() { return section; }

    private int width, height;
    private int flagWidth, flagHeight;

    private int xMax, yMax;
    private int prevX = -1, prevY = Integer.MAX_VALUE;

    private double xScale = 1.0, yScale = 1.0;
    private int offset = 0;
    private int baseLine = 0;

    private Color backgroundColor;

    private boolean okToMark = true;

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

    thresholdInfo = new ThresholdInfo();
    thresholdInfo.setChartGroupNum(pChartGroupNum);
    thresholdInfo.setChartNum(pChartNum);
    thresholdInfo.setGraphNum(pGraphNum);
    thresholdInfo.setThresholdNum(pThresholdNum);

    width = pWidth; height = pHeight;
    xMax = width - 1; yMax = height - 1;
    backgroundColor = pBackgroundColor;

    section = "Chart Group " + thresholdInfo.getChartGroupNum()
                + " Chart " + thresholdInfo.getChartNum()
                + " Graph " + thresholdInfo.getGraphNum()
                + " Threshold " + thresholdInfo.getThresholdNum();

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

    //add this threshold's ThresholdInfo to the list in SharedSettings
    sharedSettings.addThresholdInfo(thresholdInfo);

}// end of Threshold::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::configure
//
// Loads configuration settings from the configuration.ini file.
//

private void configure(IniFile pConfigFile)
{

    thresholdInfo.setTitle(pConfigFile.readString(section, "title", "*"));

    thresholdInfo.setShortTitle(pConfigFile.readString(section, "short title",
                                                                        "*"));

    thresholdInfo.setDoNotFlag(pConfigFile.readBoolean(section,
                                    "do not flag - for reference only", false));

    thresholdInfo.setThresholdColor(pConfigFile.readColor(section, "color",
                                                                    Color.RED));

    thresholdInfo.setInvert(pConfigFile.readBoolean(section, "invert threshold",
                                                                        true));

    int lvl = pConfigFile.readInt(section, "default level", 50);
    thresholdInfo.setLevel(lvl);

    thresholdInfo.setAlarmChannel(pConfigFile.readInt(section, "alarm channel",
                                                        0));

    thresholdInfo.setFlagOnOver(pConfigFile.readBoolean(section, "flag on over",
                                                        true));

    //stuff that is only used for gui
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

    setLevel(pCalFile.readInt(section, "threshold level", getLevel()));

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

    pCalFile.writeInt(section, "threshold level", getLevel());

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
// Note that pX and pY should already be scaled and the scroll offset should
// have already been calculated.
//

public void drawFlag(Graphics2D pPG2, int pX, int pY)
{

    //if flag would be drawn above or below the screen, force on screen
    if (pY < 0) {pY = 0;}
    if (pY+flagHeight > height) {pY = height - flagHeight;}

    pPG2.setColor(thresholdInfo.getThresholdColor());

    //add 1 to xPos so flag is drawn to the right of the peak
    pPG2.fillRect(pX-1-flagWidth, pY, flagWidth, flagHeight);

}//end of Threshold::drawFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::drawNextSlice
//
// Draws the threshold line up to pX. Note that pX should already be scaled
// before it is passed to this function. However, scroll offset should not have
// been calculated yet.
//

public void drawNextSlice(Graphics2D pG2, int pX)
{

    //adjust for any scrolling that has occurred before plotting
    int xAdj = pX - graphInfo.scrollOffset;
    int prevXAdj = prevX - graphInfo.scrollOffset;

    //draw threshold line
    int lvl = getPlotThresholdLevel();
    pG2.setColor(thresholdInfo.getThresholdColor());
    pG2.drawLine(prevXAdj, lvl, xAdj, lvl);

}// end of Threshold::drawNextSlice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::getPlotThresholdLevel
//
// Calculates and returns the y point at which to plot the threshold level.
//
// This is not done once and stored in a class variable because sharedSettings
// is shared with multiple threads and objects; the threshold level can change
// at any time.
//

private int getPlotThresholdLevel()
{

    int plotThresholdLevel = calculateY(getLevel());
    if(plotThresholdLevel < 0) {plotThresholdLevel = 0;}
    if(plotThresholdLevel > height) {plotThresholdLevel = height;}

    //invert the y position if specified
    if (thresholdInfo.getInvert()){
        plotThresholdLevel = height - plotThresholdLevel;
    }

    return plotThresholdLevel;

}//end of Threshold::getPlotThresholdLevel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Threshold::paintThresholdLine
//
// Draws the threshold line all the way across the graph.
//

public void paintThresholdLine(Graphics2D pG2)

{

    int lvl = getPlotThresholdLevel();
    pG2.setColor(thresholdInfo.getThresholdColor());
    pG2.drawLine(0, lvl, xMax, lvl);

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
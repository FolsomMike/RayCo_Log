/******************************************************************************
* Title: ThresholdInfo.java
* Author: Hunter Schoonover
* Date: 09/21/17
*
* Purpose:
*
* This class handles threshold values shared among classes.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package model;

//-----------------------------------------------------------------------------

import java.awt.Color;

// class ThresholdInfo
//

public class ThresholdInfo {

private String title;
public String getTitle() { return title; }
public void setTitle(String pTitle) { title = pTitle; }

private String shortTitle;
public String getShortTitle() { return shortTitle; }
public void setShortTitle(String pTitle) { shortTitle = pTitle; }

private int chartGroupNum = -1;
public int getChartGroupNum() { return chartGroupNum; }
public void setChartGroupNum(int pV) { chartGroupNum = pV; }

private int chartNum = -1;
public int getChartNum() { return chartNum; }
public void setChartNum(int pV) { chartNum = pV; }

private int graphNum = -1;
public int getGraphNum() { return graphNum; }
public void setGraphNum(int pV) { graphNum = pV; }

private int thresholdNum = -1;
public int getThresholdNum() { return thresholdNum; }
public void setThresholdNum(int pV) { thresholdNum = pV; }

private boolean flagOnOver = true;
public boolean getFlagOnOver() { return flagOnOver; }
public void setFlagOnOver(boolean pV) { flagOnOver = pV; }

private boolean doNotFlag = false;
public boolean getDoNotFlag() { return doNotFlag; }
public void setDoNotFlag(boolean pV) { doNotFlag = pV; }

private Color thresholdColor;
public Color getThresholdColor() { return thresholdColor; }
public void setThresholdColor(Color pV) { thresholdColor = pV; }

private int alarmChannel;
public int getAlarmChannel() { return alarmChannel; }
public void setAlarmChannel(int pV) { alarmChannel = pV; }

private boolean invert;
public boolean getInvert() { return invert; }
public void setInvert(boolean pV) { invert = pV; }

private int level = -1;
synchronized public void setLevel(int pLvl)  { level = pLvl; }
synchronized public int getLevel()  { return level; }

//-----------------------------------------------------------------------------
// ThresholdInfo::ThresholdInfo (constructor)
//
//

public ThresholdInfo()
{

}//end of ThresholdInfo::ThresholdInfo (constructor)
//-----------------------------------------------------------------------------

}//end of class ThresholdInfo
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

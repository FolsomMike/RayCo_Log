/******************************************************************************
* Title: Trace.java
* Author: Mike Schoonover
* Date: 11/13/13
*
* Purpose:
*
* This class draws a trace
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import controller.GUIDataSet;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import model.DataSetInt;
import model.DataTransferIntBuffer;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Trace
//

public class Trace{

    private IniFile configFile;

    DataTransferIntBuffer dataBuffer;
    DataSetInt dataSet = new DataSetInt();
    
    public void setDataBuffer(DataTransferIntBuffer pV) { dataBuffer = pV; }
    public DataTransferIntBuffer getDataBuffer() { return(dataBuffer); }
    
    private String title, shortTitle;
    public Color traceColor;    
    public String colorKeyText;
    public int colorKeyXPos;
    public int colorKeyYPos;    
    public int chartGroupNum, chartNum, graphNum, traceNum;
    private int width, height;
    Color backgroundColor;
    Color gridColor;
    private int dataIndex = 0;
    private int prevX = Integer.MAX_VALUE, prevY = Integer.MAX_VALUE;
    private int xMax, yMax;
    private int numDataPoints;
    private double xScale = 1.0;    
    private double yScale = 1.0;
    private int offset = 0;
    private int baseLine = 0;
    private final Color circleColor = DEFAULT_CIRCLE_COLOR;
    private boolean visible = true;
    private boolean connectPoints = true;
    private boolean invertTrace;
    private boolean leadDataPlotter;
    private int gridTrigger = 0;
    private int peakType;
    boolean drawGridBaseline;
    int gridXSpacing = 10;
    int gridYSpacing;
    int gridY1; 
    
    //simple getters & setters
    
    public int getWidth(){ return(width); }
    public int getPeakType(){ return(peakType); }
    public int getNumDataPoints() { return(numDataPoints); }
    
    //constants
    
    public static final int CATCH_HIGHEST = 0;
    public static final int CATCH_LOWEST = 1;

    private static final Color VERTICAL_BAR_COLOR = Color.DARK_GRAY;
    private static final Color DEFAULT_CIRCLE_COLOR = Color.BLACK;
    
    public static final boolean CONNECT_POINTS = true;
    public static final boolean DO_NOT_CONNECT_POINTS = false;
    
//-----------------------------------------------------------------------------
// Trace::Trace (constructor)
//
//

public Trace()
{

}//end of Trace::Trace (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// pIndex is a unique identifier for the object -- usually it's index position
// in an array of the creating object.
//

public void init(int pChartGroupNum, int pChartNum, int pGraphNum,
              int pTraceNum, int pWidth, int pHeight, Color pBackgroundColor,
              boolean pDrawGridBaseline, Color pGridColor, int pGridXSpacing,
              int pGridYSpacing, IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    graphNum = pGraphNum; traceNum = pTraceNum;
    width = pWidth; height = pHeight;
    backgroundColor = pBackgroundColor;
    drawGridBaseline = pDrawGridBaseline; gridColor = pGridColor;
    gridXSpacing = pGridXSpacing; gridYSpacing = pGridYSpacing;
    
    gridY1 = gridYSpacing-1; //do math once for repeated use
    
    configFile = pConfigFile;

    loadConfigSettings();

    xMax = width - 1; yMax = height - 1;

}// end of Trace::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                + " Graph " + graphNum + " Trace " + traceNum;

    title = configFile.readString(
                        section, "title", "Trace " + (traceNum + 1));

    shortTitle = configFile.readString(
                            section, "short title", "trace" + (traceNum + 1));

    traceColor = configFile.readColor(section, "color", Color.BLACK);    
    
    colorKeyText = configFile.readString(section, "color key text", "hidden");
    colorKeyXPos = configFile.readInt(section, "color key x position", 0);
    colorKeyYPos = configFile.readInt(section, "color key y position", 0);    
        
    int configWidth = configFile.readInt(section, "width", 0);

    if (configWidth > 0) width = configWidth; //override if > 0
    
    int configHeight = configFile.readInt(section, "height", 0);

    if (configHeight > 0) height = configHeight; //override if > 0

    connectPoints = configFile.readBoolean(
                            section, "connect data points with line", false);
    
    invertTrace = configFile.readBoolean(section, "invert trace", true);

    numDataPoints = configFile.readInt(section, "number of data points", width);
    
    offset = configFile.readInt(section, "offset", 0);
    xScale = configFile.readDouble(section, "x scale", 1.0);
    yScale = configFile.readDouble(section, "y scale", 1.0);
    baseLine = configFile.readInt(section, "baseline", 0);

    String peakTypeText = configFile.readString(
                                        section, "peak type", "catch highest");
    parsePeakType(peakTypeText);
    
    leadDataPlotter = configFile.readBoolean(
                                          section, "lead data plotter", true);    
    
}// end of Trace::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::parsePeakType
//
// Converts the descriptive string loaded from the config file for the peak
// type (catch highest, lowest value, etc.) into the corresponding constant.
//

public void parsePeakType(String pValue)
{

    switch (pValue) {
         case "catch highest": peakType = CATCH_HIGHEST; break;
         case "catch lowest" : peakType = CATCH_LOWEST;  break;
         default : peakType = CATCH_LOWEST;  break;
    }
    
}// end of Trace::parsePeakType
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// Trace::setXScale
//
// Sets the trace display horizontal scale to pScale.
//

public void setXScale(double pScale)
{

    xScale = pScale;

}// end of Trace::setXScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setYScale
//
// Sets the trace display vertical scale to pScale.
//

public void setYScale(double pScale)
{

    yScale = pScale;

}// end of Trace::setYScale
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setOffset
//
// Sets the display offset for Trace pTrace to pOffset.
//

public void setOffset(int pOffset)
{

    offset = pOffset;

}// end of Trace::setOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setBaseLine
//
// Sets the baseLine value to pBaseLine. This will cause the pBaseline
// value to be shifted to zero when the trace is drawn.
//

public void setBaseLine(int pBaseLine)
{

    baseLine = pBaseLine;

}// end of Trace::setBaseLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setConnectPoints
//
// Sets the connectPoints flag. If true, points will be connected by a line.
//

public void setConnectPoints(boolean pValue)
{

    connectPoints = pValue;

}// end of Trace::setConnectPoints
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::resetData
//
// Resets all data to zero and all flags to default. Resets all buffer pointers
// to starting positions.
//

public void resetData()
{
    
    dataBuffer.reset();
    
    dataIndex = 0;
    prevX = Integer.MAX_VALUE; prevY = Integer.MAX_VALUE;
    gridTrigger = 0;
    
}// end of Trace::resetData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::setVisible
//
// Sets the visible flag to pVisible.
//

public void setVisible(boolean pVisible)
{

    visible = pVisible;

}// end of Trace::setVisible
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::checkForMatch
//
// Tests to see if this trace matches the trace specified by pGuiDataSet by
// comparing the chartGroupNum, chartNum, graphNum, and traceNum.
//
// If all values match, returns true. Otherwise returns false.
//

public boolean checkForMatch(GUIDataSet pGuiDataSet)
{

    return(
            (chartGroupNum == pGuiDataSet.chartGroupNum)
            && (chartNum == pGuiDataSet.chartNum)
            && (graphNum == pGuiDataSet.graphNum) 
            && (traceNum == pGuiDataSet.traceNum)            
            );

}// end of Trace::checkForMatch
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::paintTrace
//
// Draws the entire trace.
//

public void paintTrace(Graphics2D pG2)
{

    if(!visible) { return; }
    
    for(int i = 0; i<width-1; i++){

//debug mks        paintSingleTraceDataPoint(pG2, i);
        
    }

}// end of Trace::paintTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::drawGrid
//
// Draw grid lines and dots and other related objects.
//
// Note that gridTrigger is never reset while grid is being drawn, but it will
// never reach Integer.MAX_VALUE as that would take a long, long time. It is
// more efficient not to reset it during use.
//

public void drawGrid (Graphics2D pG2, int pX)
{
    
    pG2.setColor(gridColor);
    
    for(int i=0; i<pX-prevX; i++){
        
        int x=pX+i;

        if (drawGridBaseline) { 
            int y;
            if(invertTrace) { y=yMax; } else { y=0; }
            pG2.drawLine(x, y, x, y);
        }
        
        if((gridTrigger++ % 10) == 0){        
            for(int j=gridY1; j<yMax; j+=gridYSpacing){
                pG2.drawLine(x, j, x, j);
            }
        }        
    }

}// end of Trace::drawGrid
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::paintSingleTraceDataPoint
//
// Draws line from the last point drawn to data point pX, pY and processes
// pFlags as appropriate.
//

public void paintSingleTraceDataPoint(Graphics2D pG2,int pX, int pY, int pFlags)
{

    if(!visible) { return; }
    
    int x = (int)Math.round(pX * xScale);
    
    int xDelta = prevX-x; //number of pixels between current and previous x
    
    //scroll chart left if enabled and new point is off the chart
    if(x > width){

        if (leadDataPlotter){
            

                //if this is lead Plotter object, shift chart left and erase right slice

                //scroll the screen to the left
                pG2.copyArea(1, 0, width, height, -1, 0);
                //erase the line at the far right
                pG2.setColor(backgroundColor);
                pG2.drawLine(xMax, 0, xMax, height);
                
        }
        x = xMax; prevX--;
    }
    
    if (leadDataPlotter){ drawGrid(pG2, x); }
    
    //draw a vertical line if the flag is set
    if ((pFlags & DataTransferIntBuffer.VERTICAL_BAR) != 0){
        pG2.setColor(VERTICAL_BAR_COLOR);
        pG2.drawLine(x, 0, x, yMax);
    }

    pG2.setColor(traceColor);

    int y = (int)(pY - baseLine);

    y = (int)(y * yScale) + offset;

    //if so configured, invert y so zero is at the bottom of the chart

    if(invertTrace){
        if(y > yMax){ y = 0; }
        else { y = yMax - y; }
    }
    
    //draw between each two points
    if(connectPoints) { 
        pG2.drawLine(prevX, prevY, x, y);
    }
    else{
        pG2.drawLine(x, y, x, y);
    }

    prevX = x; prevY = y;
    
    //draw a circle on the datapoint if the CIRCLE flag is set
    if ((pFlags & DataTransferIntBuffer.CIRCLE) != 0){
        pG2.setColor(circleColor);
        pG2.draw(new Ellipse2D.Double(x-3, y-3, 6, 6));
    }

}// end of Trace::paintSingleTraceDataPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Trace::updateTrace
//
// Plots all data added to dataBuffer and erases any data which has been
// marked as erased.
//

public void updateTrace(Graphics2D pG2)
{
    
    int r;
    
    while((r = dataBuffer.getDataChange(dataSet)) != 0){
            
        paintSingleTraceDataPoint(pG2, dataIndex, dataSet.d, dataSet.flags);

        if(r == 1){ dataIndex++; }
        else if(r == -1){ dataIndex--; }
        
    }
        
}// end of Trace::updateTrace
//-----------------------------------------------------------------------------

}//end of class Trace
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

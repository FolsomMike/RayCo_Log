/******************************************************************************
* Title: ZoomGraph.java
* Author: Mike Schoonover
* Date: 01/14/15
*
* Purpose:
*
* This class subclasses a JPanel to display zoomed views of signal indications.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import model.DataFlags;
import model.DataSetSnapshot;
import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomGraph
//

public class ZoomGraph extends Graph{

    private final ArrayList<ZoomBox> zoomBoxes = new ArrayList<>();
    private int lastUpdatedZoomBoxDataIndex = 0;
    private ZoomBox lastUpdatedZoomBox = null;

    private int annoX = 0;
    public int getNextBoxStartX() { return annoX; }
    public int getNextBoxEndX() { return annoX+annoWidth+gap; }
    private final int annoY = 10;
    private final int annoWidth = 128, annoHeight = 50; //WIP HSS// read in from ini file
    private int gap;
    private int maxNumZoomBoxes;

    ArrayList<int[]> data = new ArrayList<>(10000);
    public int getDataSize() { return data.size(); }
    ArrayList<Integer> dataFlags = new ArrayList<>(10000);
    DataSetSnapshot dataSet;

    //length is the x axis, width is the y axis (o'clock position)
    private int lengthInDataPoints;
    public int getLengthInDataPoints(){return(lengthInDataPoints);}
    private int widthInDataPoints;
    public int getWidthInDataPoints(){return(widthInDataPoints);}
    private int bufferLengthInDataPoints;
    public int getBufferLengthInDataPoints(){return(bufferLengthInDataPoints);}

    private int lastSegmentStartIndex = -1;
    private int lastSegmentEndIndex = -1;

    //WIP HSS// all of these should be read from inifile (except x&y)
    private boolean hasArrows = true;
    private int defaultArrowX = 0;
    private int arrowY = 1;
    private int arrowWidth = 10;
    private int arrowHeight = 6;

//-----------------------------------------------------------------------------
// ZoomGraph::ZoomGraph (constructor)
//
//

public ZoomGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
                    int pWidth, int pHeight, ChartInfo pChartInfo,
                    IniFile pConfigFile, SharedSettings pSettings)
{

    super(pChartGroupNum, pChartNum, pGraphNum, pWidth, pHeight,
                    pChartInfo, pConfigFile, pSettings);

}//end of Chart::ZoomGraph (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    dataSet = new DataSetSnapshot(128); //WIP HSS// determine another way

}// end of ZoomGraph::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);

    ListIterator iter = zoomBoxes.listIterator();

    while(iter.hasNext()){
        ((ZoomBox)iter.next()).paint((Graphics2D)g);
    }

}// end of ZoomGraph::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::retrieveDataChanges
//
// Snags all of the data changes in the buffer.
//

public void retrieveDataChanges()
{

    while(snapshotBuffer.getDataChange(dataSet) != 0){

        //store for future use
        data.add(dataSet.d.clone());
        dataFlags.add(dataSet.flags);

        //if segment start/end flag set, draw a vertical separator bar, store index
        int index = data.size()-1;
        if ((dataSet.flags & DataFlags.SEGMENT_START_SEPARATOR) != 0) {
            lastSegmentStartIndex = index;
        }
        if ((dataSet.flags & DataFlags.SEGMENT_END_SEPARATOR) != 0) {
            lastSegmentEndIndex = index;
        }
    }

}// end of ZoomGraph::retrieveDataChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::addZoomBox
//
// Adds a ZoomBox to the graph at pX, using the data found at the specified
// index.
//

int debugHss = -1;//DEBUG HSS//
public void addZoomBox(int pIndex)
{

    zoomBoxes.add(new ZoomBox(chartGroupNum, chartNum, graphNum, 0,
                    graphInfo, annoX, annoX+annoWidth+gap, annoY, annoWidth,
                    annoHeight, hasArrows, defaultArrowX, arrowY, arrowWidth,
                    arrowHeight));

    //prepare x to add next zoom box to the right -- scroll offset not
    //subtracted because it may change
    annoX += annoWidth + gap + 1;

    //use the last data set collected if index out of bounds
    int [] zoomData;
    if (pIndex<0||pIndex>=data.size()) {
        zoomData = data.get(data.size()-1); pIndex = data.size()-1;
    }
    else { zoomData = data.get(pIndex); }

    //DEBUG HSS//
    int p=-1;
    for (int d : zoomData) {
        int absD = Math.abs(d);
        if (absD>p) { p = absD; }
    }
    debugHss = p;
    //DEBUG HSS//

    //set zoombox stuff
    zoomBoxes.get(zoomBoxes.size()-1).setData(zoomData, pIndex);
    zoomBoxes.get(zoomBoxes.size()-1).paint((Graphics2D)getGraphics());

    //limit number of boxes
    if (zoomBoxes.size() > maxNumZoomBoxes){ zoomBoxes.remove(0); }

}// end of ZoomGraph::addZoomBox
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::updateZoomBox
//
// Determines which ZoomBox is used to display data at pX and sets the data to
// display the snapshot at pX.
//

void updateZoomBox(int pX)
{

    for (ZoomBox b : zoomBoxes) {
        if ((b.getX())<=pX && pX<=(b.getXEnd())){

            //store this zoombox info so data can be reset later
            if (lastUpdatedZoomBox != b) {
                resetLastUpdatedZoomBox();
                lastUpdatedZoomBoxDataIndex = b.getArrowX();
                lastUpdatedZoomBox = b;
            }

            b.setData(data.get(pX), pX);
            b.paint((Graphics2D)getGraphics());

        }
    }

}// end of ZoomGraph::updateZoomBox
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::resetLastUpdatedZoomBox
//
// Resets the last updated ZoomBox back its original data.
//

void resetLastUpdatedZoomBox()
{

    if (lastUpdatedZoomBox!=null) {
        lastUpdatedZoomBox.setData(data.get(lastUpdatedZoomBoxDataIndex),
                                    lastUpdatedZoomBoxDataIndex);
        lastUpdatedZoomBox.paint((Graphics2D)getGraphics());
        lastUpdatedZoomBox = null;
    }

}// end of ZoomGraph::resetLastUpdatedZoomBox
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::resetAll
//
// Resets all values and child values to default.
//

@Override
public void resetAll()
{

    super.resetAll();

    annoX = 0; //adding anno objects starts over at left edge

    snapshotBuffer.reset();

    data = new ArrayList<>(10000); //DEBUG HSS// shouln't even exist

}// end of ZoomGraph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadSegment
//
// Saves all of the zoom data.
//

@Override
public void loadSegment(IniFile pFile)
{

    data.clear();

    ArrayList<String> fileLines = new ArrayList<>(5000);
    pFile.getSection(configFileSection+" Data Set", fileLines);

    for (String line : fileLines) {

        String[] pointStrings = line.split(",");
        int[] dataPoints = new int[pointStrings.length];
        for (int i=0; i<pointStrings.length; i++) {

            //try to convert to an integer, do nothing on failure
            try{ dataPoints[i] = Integer.parseInt(pointStrings[i]); }
            catch(NumberFormatException e){ }

        }

        data.add(dataPoints);

    }

}//end of ZoomGraph::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::saveSegment
//
// Saves all of the zoom data.
//

@Override
public void saveSegment(BufferedWriter pOut) throws IOException
{

    pOut.write("["+configFileSection+"]"); pOut.newLine();
    pOut.write("Zoom Title=" + title); pOut.newLine();
    pOut.write("Zoom Short Title=" + shortTitle); pOut.newLine();

    pOut.write("["+configFileSection+" Data Set]"); pOut.newLine(); //save data set

    //save the data
    for (int i=lastSegmentStartIndex; i<=lastSegmentEndIndex; i++) {
        for (int d : data.get(i)) { pOut.write(Integer.toString(d)+","); }

        pOut.newLine();
    }

    pOut.write("[/"+configFileSection+" Data Set]"); pOut.newLine();

}//end of ZoomGraph::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    configFileSection =
            "Chart Group " + chartGroupNum + " Chart " + chartNum
                                            + " Graph " + graphNum;

    super.loadConfigSettings();

    gap = configFile.readInt(configFileSection, "gap between annotations", 4);

    maxNumZoomBoxes = configFile.readInt(
            configFileSection, "maximum number of annotation objects", 20);

}// end of ZoomGraph::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class ZoomGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

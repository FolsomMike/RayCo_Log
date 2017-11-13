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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import model.DataFlags;
import model.DataSetSnapshot;
import model.IniFile;
import model.SharedSettings;
import toolkit.Tools;

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

    metaDataSectionName = "Zoom Graph";

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

    snapshotBuffer.incPutPtrAndSetReadyAfterDataFill();
    
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

    if (lastUpdatedZoomBox!=null && lastUpdatedZoomBoxDataIndex<data.size()) {
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

    if (snapshotBuffer!=null) { snapshotBuffer.reset(); }

    zoomBoxes.clear(); data.clear(); dataFlags.clear();

    //reset segment starts and ends
    lastSegmentStartIndex = -1; lastSegmentEndIndex = -1;

    repaint();

}// end of ZoomGraph::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::markSegmentStart
//
// Sets the flag of the last read data point to indicate that the data point
// assoicated with a segment start.
//

@Override
public void markSegmentStart()
{
    
    //bail if no data stored yet, just use flag read in from dataBuffer later
    if (dataFlags.size()<=0) { return; }
    
    //set flag at last data flag retrieved
    lastSegmentStartIndex = dataFlags.size()-1;
    int newFlag = dataFlags.get(lastSegmentStartIndex) | DataFlags.SEGMENT_START_SEPARATOR;
    dataFlags.set(lastSegmentStartIndex, newFlag);
    
}//end of ZoomGraph::markSegmentStart
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::markSegmentEnd
//
// Sets the flag of the last read data point to indicate that the data point
// assoicated with a segment end.
//

@Override
public void markSegmentEnd()
{
    
    //bail if no data stored yet, just use flag read in from dataBuffer later
    if (dataFlags.size()<=0) { return; }
    
    //set flag at last data flag retrieved
    lastSegmentEndIndex = dataFlags.size()-1;
    int newFlag = dataFlags.get(lastSegmentEndIndex) | DataFlags.SEGMENT_END_SEPARATOR;
    dataFlags.set(lastSegmentEndIndex, newFlag);

}//end of ZoomGraph::markSegmentEnd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::isSegmentStarted
//
// Checks to see if a segment has been started.  If the insertion point has
// moved a predetermined amount after the current segment was initiated, it is
// assumed that a segment has been started.
//
// The insertion point must move more than a few counts to satisfy the start
// criteria. This is to ignore any small errors.
//

@Override
public boolean isSegmentStarted()
{

    return lastSegmentStartIndex>-1 && data.size()>10;

}//end of ZoomGraph::isSegmentStarted
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::saveSegment
//
// Saves all of the zoom data.
//

@Override
public void saveSegment(BufferedWriter pOut) throws IOException
{

    super.saveSegment(pOut);

    //catch unexpected case where start/stop are invalid and bail
    if (lastSegmentStartIndex < 0 || lastSegmentEndIndex < 0){
        pOut.write("Segment start and/or start invalid - no data saved.");
        pOut.newLine(); pOut.newLine();
        return;
    }

    //save data points
    pOut.write("[Data Set 1]"); pOut.newLine();
    for (int i=lastSegmentStartIndex; i<=lastSegmentEndIndex; i++) {
        for (int d : data.get(i)) { pOut.write(Integer.toString(d)+","); }
        pOut.newLine();
    }
    pOut.write("[End of Set]"); pOut.newLine();

    //save data flags
    pOut.write("[Flags]"); pOut.newLine();
    for (int i=lastSegmentStartIndex; i<=lastSegmentEndIndex; i++) {
        pOut.write(Integer.toString(dataFlags.get(i))); //write to file
        pOut.newLine();
    }
    pOut.write("[End of Set]"); pOut.newLine();

}//end of ZoomGraph::saveSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadSegment
//
// Saves all of the zoom data.
//

@Override
public String loadSegment(BufferedReader pIn, String pLastLine)
        throws IOException
{

    String line = super.loadSegment(pIn, pLastLine);

    //clear previous data & flags
    zoomBoxes.clear(); data.clear(); dataFlags.clear();

    //load data points
    boolean multipleDataPointsPerLine = true;
    line = loadDataSeries(pIn, line, "[Data Set 1]",
                                data, multipleDataPointsPerLine, 0);

    //load flags
    multipleDataPointsPerLine = false;
    line = loadDataSeries(pIn, line, "[Flags]",
                                dataFlags, multipleDataPointsPerLine, 0);

    return line;

}//end of ZoomGraph::loadSegment
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadDataSeries
//
// Loads a data series into a one dimensional array from pIn.  The series could
// be "Data Set 1", "Data Set 2", or "Flags", etc. depending on the parameters
// passed in.
//
// The pStartTag string specifies the section start tag for the type of data
// expected and could be: "[Data Set 1]", "[Data Set 2]", or "[Flags]".  The
// pBuffer pointer should be set to the buffer associated with the data type.
//
// Returns the last line read from the file so that it can be passed to the
// next process.
//
// For these sections, the [xxx] section start tag may or may not have already
// been read from the file by the code handling the previous section.  If it has
// been read, the line containing the tag should be passed in via pLastLine.
//
// Value pDataModifier1 will be ORed with each data point as it is stored in
// the buffer. This allows any bit(s) to be forced to 1 if they are used as
// flag bits. If no bits are to be forced, pDataModifier1 should be 0.
//

public String loadDataSeries(BufferedReader pIn, String pLastLine,
                            String pStartTag, ArrayList pBuffer,
                            boolean pMultiDataPointsPerFileLine,
                            int pDataModifier1) throws IOException
{

    String line;
    Xfer matchSet = new Xfer(); //for receiving data from function calls

    //attempt to find section start, will throw exception on failure
    line = findSectionStart(pIn, pLastLine, pStartTag, matchSet);

    //read in the section and store the data
    int i = 0; boolean success = false;
    while ((line = pIn.readLine()) != null){

        //stop when next section end tag reached (will start with [)
        if (Tools.matchAndParseString(line, "[", "",  matchSet)){
            success = true; break;
        }

        try{

            //true means each file line reps an array of data points
            if (pMultiDataPointsPerFileLine) {
                //data is serarated by commas
                String[] dataSplit = line.split(",");

                //arrays to hold integers after conversion
                int[] dataPoints = new int[dataSplit.length];

                //convert and store all data as integers
                for (int j=0; j<dataSplit.length; j++) {

                    dataPoints[j] = Integer.parseInt(dataSplit[j]) | pDataModifier1;

                }

                //store in buffer
                pBuffer.add(dataPoints);
            }
            //false means each file line represents one data point
            else {
                int dataInt = Integer.parseInt(line);
                pBuffer.add(dataInt | pDataModifier1);
            }

        } catch(NumberFormatException e){
            //catch error translating the text to an integer
            throw new IOException(
                            "The file could not be read - corrupt data for "
                                    + pStartTag + " at data point " + i++);
        }


    }//while ((line = pIn.readLine()) != null)

    if (!success) {
        throw new IOException(
         "The file could not be read - missing end of section for "
                                                                + pStartTag);
    }

    return(line); //should be "[xxxx]" tag on success, unknown value if not

}//end of ZoomGraph::loadDataSeries
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadDataSeries
//
// If pLastLine contains the [pStartTag] tag, then skip ahead else read until
// end of file reached or "[pStrat]" section tag reached.
//
// Returns true if pStartTag found, false if not.
//

public String findSectionStart(BufferedReader pIn, String pLastLine,
                                String pStartTag, Xfer pMatchSet)
        throws IOException
{

    boolean success = false;

    String line = pLastLine;
    if (Tools.matchAndParseString(pLastLine, pStartTag, "",  pMatchSet)) {
        success = true;  //tag already found
    }
    else {
        while ((line = pIn.readLine()) != null){  //search for tag
            if (Tools.matchAndParseString(line, pStartTag, "",  pMatchSet)){
                success = true; break;
            }
        }//while
    }//else

    if (!success) {
        throw new IOException(
           "The file could not be read - section not found for " + pStartTag);
    }

    return line;

}//end of ZoomGraph::loadDataSeries
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    fileSection =
            "Chart Group " + chartGroupNum + " Chart " + chartNum
                                            + " Graph " + graphNum;

    super.loadConfigSettings();

    gap = configFile.readInt(fileSection, "gap between annotations", 4);

    maxNumZoomBoxes = configFile.readInt(
            fileSection, "maximum number of annotation objects", 20);

}// end of ZoomGraph::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class ZoomGraph
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

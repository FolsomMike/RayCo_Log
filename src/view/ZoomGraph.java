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
import java.util.ArrayList;
import java.util.ListIterator;
import model.DataSetSnapshot;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ZoomGraph
//

public class ZoomGraph extends Graph{

    private final ArrayList<ZoomBox> zoomBoxes = new ArrayList<>();

    private int annoX = 0;
    public int getNextBoxStartX() { return annoX; }
    public int getNextBoxEndX() { return annoX+annoWidth+gap; }
    private final int annoY = 10;
    private final int annoWidth = 128, annoHeight = 50; //WIP HSS// read in from ini file
    private int annoIndex = 0, annoNumIndexes = 132; //WIP HSS// read in from ini file
    public int getNextBoxStartIndex() { return annoIndex; }
    public int getNextBoxEndIndex() { return annoIndex+annoNumIndexes-1; }
    private int gap;
    private int maxNumZoomBoxes;

    ArrayList<int[]> data = new ArrayList<>(10000);
    ArrayList<Integer> dataFlags = new ArrayList<>(10000);
    DataSetSnapshot dataSet;

    //length is the x axis, width is the y axis (o'clock position)
    private int lengthInDataPoints;
    public int getLengthInDataPoints(){return(lengthInDataPoints);}
    private int widthInDataPoints;
    public int getWidthInDataPoints(){return(widthInDataPoints);}
    private int bufferLengthInDataPoints;
    public int getBufferLengthInDataPoints(){return(bufferLengthInDataPoints);}

//-----------------------------------------------------------------------------
// ZoomGraph::ZoomGraph (constructor)
//
//

public ZoomGraph(int pChartGroupNum, int pChartNum, int pGraphNum,
            int pWidth, int pHeight, ChartInfo pChartInfo, IniFile pConfigFile)
{

    super(pChartGroupNum, pChartNum, pGraphNum,
                                     pWidth, pHeight, pChartInfo, pConfigFile);

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

        //store for future use -- clone used so that all objects in data
        //do not point to dataSet object
        data.add(dataSet.d.clone());

    }

}// end of ZoomGraph::retrieveDataChanges
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ZoomGraph::addZoomBox
//
// Adds a ZoomBox to the graph at pX, using the data found at the specified
// index.
//

public void addZoomBox(int pIndex)
{

    //scroll offset may have changed since annoX was updated
    //so we subtract it now
    int zoomX = annoX - graphInfo.scrollOffset;

    //prepare x to add next zoom box to the right -- scroll offset not
    //subtracted because it may change
    annoX += annoWidth + gap;

    int indexStart = annoIndex;
    int indexEnd = annoIndex + annoNumIndexes - 1;
    //prepare next index start
    annoIndex += annoNumIndexes;

    zoomBoxes.add(new ZoomBox(chartGroupNum, chartNum, graphNum, 0,
                    zoomX, annoY, annoWidth, annoHeight));

    //use the last data set collected if index out of bounds
    int [] zoomData; int arrowX; int arrowY=1;
    if (pIndex>=data.size()) {
        zoomData = data.get(data.size()-1);
        arrowX = data.size()-1 - graphInfo.scrollOffset;;
    }
    else {
        zoomData = data.get(pIndex);
        arrowX = pIndex - graphInfo.scrollOffset;
    }

    //set zoombox stuff
    zoomBoxes.get(zoomBoxes.size()-1).setArrowLocation(arrowX, arrowY);
    zoomBoxes.get(zoomBoxes.size()-1).setData(zoomData);
    zoomBoxes.get(zoomBoxes.size()-1).setDataStartIndex(indexStart);
    //annoX should be set to start of next zoomBox/end of this one
    zoomBoxes.get(zoomBoxes.size()-1).setDataEndIndex(indexEnd);

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
        if ((b.getDataStartIndex())<=pX && pX<=(b.getDataEndIndex())){

            b.setData(data.get(pX));

            b.setX(b.getDataStartIndex()-graphInfo.scrollOffset);
            b.setArrowLocation(pX-graphInfo.scrollOffset, 1);

            b.paint((Graphics2D)getGraphics());
        }
    }

}// end of ZoomGraph::updateZoomBox
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

}// end of ZoomGraph::resetAll
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

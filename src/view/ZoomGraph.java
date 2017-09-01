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
// Adds a ZoomBox to the graph at pX, using the snapshot found at the index for
// data.
//

public void addZoomBox(int pSnapshotIndex)
{

    int zoomX = annoX - graphInfo.scrollOffset;
    int zoomY = annoY;

    annoX += annoWidth + gap; //prepare x to add next anno object to the right

    zoomBoxes.add(new ZoomBox(chartGroupNum, chartNum, graphNum, 0,
                    zoomX, zoomY, annoWidth, annoHeight));

    //use the last data set collected if index out of bounds
    int [] zoomData; int arrowX; int arrowY=1;
    if (pSnapshotIndex>=data.size()) {
        zoomData = data.get(data.size()-1); arrowX = data.size()-1;
    }
    else { zoomData = data.get(pSnapshotIndex); arrowX = pSnapshotIndex; }

    //set zoombox stuff
    zoomBoxes.get(zoomBoxes.size()-1).setArrowLocation(arrowX, arrowY);
    zoomBoxes.get(zoomBoxes.size()-1).setData(zoomData);
    zoomBoxes.get(zoomBoxes.size()-1).setDataStartIndex(zoomX);
    //annoX should be set to start of next zoomBox/end of this one
    zoomBoxes.get(zoomBoxes.size()-1).setDataEndIndex(annoX-1);

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
        if (b.getDataStartIndex()<=pX&&pX<=b.getDataEndIndex()){
            b.setArrowLocation(pX, 1);
            b.setData(data.get(pX));
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

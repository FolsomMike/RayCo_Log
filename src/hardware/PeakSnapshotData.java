/******************************************************************************
* Title: PeakSnapshotData.java
* Author: Hunter Schoonover
* Date: 08/14/17
*
* Purpose:
*
* This class is used to transfer information related to a peak snapshot data
* array collected from a hardware device. This information includes the device
* number, the chart group/chart/graph to which the data should be applied,
* and any other pertinent information.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class PeakSnapshotData
//

public class PeakSnapshotData
{

public int peakMapDataNum = 0;

public SampleMetaData meta;

public int[] peakArray;
public int[] peakMetaArray;

//-----------------------------------------------------------------------------
// PeakSnapshotData::PeakSnapshotData (constructor)
//

public PeakSnapshotData(int pPeakSnapshotDataNum, int pArraySize)
{

    peakMapDataNum = pPeakSnapshotDataNum;

    peakArray = new int[pArraySize]; peakMetaArray = new int[pArraySize];

}//end of PeakSnapshotData::PeakSnapshotData (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakSnapshotData::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

}// end of PeakSnapshotData::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakSnapshotData::setData
//
// Copies the values in pInputArray to peakArray and pInputMetaArray to
// peakMetaArray.
//

public void setData(int[] pInputArray, int[] pInputMetaArray)
{

    System.arraycopy(pInputArray,0,peakArray,0,pInputArray.length);
    System.arraycopy(pInputMetaArray,0,peakMetaArray,0,pInputMetaArray.length);

}// end of PeakSnapshotData::setData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakSnapshotData::setData
//
// Copies the values in pInputArray to peakArray and copies the single value of
// pInputMetaValue to all positions in peakMetaArray.
//

public void setData(int[] pInputArray, int pInputMetaValue)
{

    System.arraycopy(pInputArray,0,peakArray,0,pInputArray.length);

    for(int i=0; i<peakMetaArray.length; i++){
        peakMetaArray[i] = pInputMetaValue;
    }

}// end of PeakSnapshotData::setData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakSnapshotData::setMetaArray
//
// Copies value of pInputMetaValue to all positions in peakMetaArray.
//

public void setMetaArray(int pInputMetaValue)
{

    for(int i=0; i<peakMetaArray.length; i++){
        peakMetaArray[i] = pInputMetaValue;
    }

}// end of PeakSnapshotData::setMetaArray
//-----------------------------------------------------------------------------

}//end of class PeakSnapshotData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

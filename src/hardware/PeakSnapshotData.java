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

public int peakDataNum = 0;

public SampleMetaData meta;

public int peak;
public int[] peakArray;

//-----------------------------------------------------------------------------
// PeakSnapshotData::PeakSnapshotData (constructor)
//

public PeakSnapshotData(int pPeakDataNum, int pArraySize)
{

    peakDataNum = pPeakDataNum;

    peakArray = new int[pArraySize];

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
// Copies the values in pInputArray to peakArray and stores pPeak.
//

public void setData(int pPeak, int[] pInputArray)
{

    peak = pPeak;
    System.arraycopy(pInputArray,0,peakArray,0,pInputArray.length);

}// end of PeakSnapshotData::setData
//-----------------------------------------------------------------------------

}//end of class PeakSnapshotData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

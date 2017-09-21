/******************************************************************************
* Title: PeakData.java
* Author: Mike Schoonover
* Date: 01/18/15
*
* Purpose:
*
* This class is used to transfer information related to a peak data point
* collected from a hardware channel. This information includes the channel
* number, the chart group/chart/graph/trace to which the data should be applied,
* and any other pertinent information.
*
* The class is a not a Generic because that comes with a lot of overhead since
* Generic classes can only handle Objects and not primitives. Classes such as
* Integer are immutable and must be recreated each time they are changed.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class PeakData
//

public class PeakData
{

public int peakDataNum = 0;

public int peak = 0;
public SampleMetaData meta;

//the arrays allow us to store info for multiple channels
public int[] peakArray;
public int[] thresholdViolationArray;
public SampleMetaData[] metaArray;

//-----------------------------------------------------------------------------
// PeakData::PeakData (constructor)
//

public PeakData(int pPeakDataNum, int pNumChannels)
{

    peakDataNum = pPeakDataNum;
    peakArray = new int[pNumChannels];
    thresholdViolationArray = new int[pNumChannels];
    metaArray = new SampleMetaData[pNumChannels];

}//end of PeakData::PeakData (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakData::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{


}// end of PeakData::init
//-----------------------------------------------------------------------------

}//end of class PeakData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

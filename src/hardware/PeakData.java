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

import model.DataTransferIntBuffer;


//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class PeakData
//

public class PeakData
{

int index = 0;

public Channel channel;
public DataTransferIntBuffer dataBuffer;

public int chartGroup = -1;
public int chart = -1;
public int graph = -1;
public int trace = -1;

public int peak = 0;
    
//-----------------------------------------------------------------------------
// PeakData::PeakData (constructor)
//

public PeakData(int pIndex)
{

    index = pIndex;
    
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

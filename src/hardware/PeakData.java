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
* The class is a Generic so that the type of the data peak can be specified.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class PeakData
//

public class PeakData<T>
{

int index = 0;

public int chartGroup = -1;
public int chart = -1;
public int graph = -1;
public int trace = -1;

public T peak;
    
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

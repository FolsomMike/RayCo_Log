/******************************************************************************
* Title: SampleMetaData.java
* Author: Mike Schoonover
* Date: 03/18/15
*
* Purpose:
*
* This class is used to transfer meta data related to a data sample,
* especially information which doesn't change often after initialization. This
* allows the information to be transferred efficiently as a pointer to an
* object of this class can be returned rather than returning each individual
* value.
*
* This information includes the channel number, the chart group/chart/graph/
* trace to which the data should be applied, and any other pertinent
* information which doesn't change often after initialization.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.DataTransferIntBuffer;
import model.DataTransferIntMultiDimBuffer;
import model.DataTransferSnapshotBuffer;



//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SampleMetaData
//

public class SampleMetaData
{

public int sampleMetaDataNum = 0;

public int numClockPositions;

public int deviceNum = -1;
public int channelNum = -1;
public int chartGroup = -1;
public int chart = -1;
public int graph = -1;
public int trace = -1;
public int system = -1;

public Channel channel;
public DataTransferIntBuffer dataBuffer;
public DataTransferSnapshotBuffer dataSnapshotBuffer;
public DataTransferIntMultiDimBuffer dataMapBuffer;

//-----------------------------------------------------------------------------
// SampleMetaData::SampleMetaData (constructor)
//

public SampleMetaData(int pSampleMetaDataNum)
{

    sampleMetaDataNum = pSampleMetaDataNum;

}//end of SampleMetaData::SampleMetaData (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SampleMetaData::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{


}// end of SampleMetaData::init
//-----------------------------------------------------------------------------

}//end of class SampleMetaData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

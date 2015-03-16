/******************************************************************************
* Title: DataSetIntMultiDim.java
* Author: Mike Schoonover
* Date: 03/16/15  
* 
* Purpose:
*
* This class encapsulates a row of data points, a row of meta data points,
* and associated data such as flags.
*
*/

//-----------------------------------------------------------------------------

package model;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class DataSetInt
//

public class DataSetIntMultiDim
{

    public int[] d; //data buffer
    public int[] m; //meta data buffer
    public int flags = 0;

    public int length;
    
//-----------------------------------------------------------------------------
// DataSetIntMultiDim::DataSetIntMultiDim (constructor)
//

public DataSetIntMultiDim(int pBufferSize)
{
    
    length = pBufferSize;
    
    d = new int[pBufferSize];
    m = new int[pBufferSize];

}//end of DataSetIntMultiDim::DataSetIntMultiDim (constructor)
//-----------------------------------------------------------------------------
    
}//end of class DataSetIntMultiDim
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

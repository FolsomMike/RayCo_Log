/******************************************************************************
* Title: DataSetSnapshot.java
* Author: Hunter Schoonover
* Date: 09/01/2017  
* 
* Purpose:
*
* This class encapsulates a snapshot array and the peak it is associated with.
*
*/

//-----------------------------------------------------------------------------

package model;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class DataSetSnapshot
//

public class DataSetSnapshot
{

    public int p; //peak
    public int[] d; //data buffer
    public int flags = 0;

    public int length;
    
//-----------------------------------------------------------------------------
// DataSetSnapshot::DataSetSnapshot (constructor)
//

public DataSetSnapshot(int pBufferSize)
{
    
    length = pBufferSize;
    
    p = 0;
    d = new int[pBufferSize];

}//end of DataSetSnapshot::DataSetSnapshot (constructor)
//-----------------------------------------------------------------------------
    
}//end of class DataSetSnapshot
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

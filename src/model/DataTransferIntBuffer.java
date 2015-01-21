/******************************************************************************
* Title: DataTransferBuffer.java
* Author: Mike Schoonover
* Date: 01/19/15
*
* Purpose:
*
* This class handles storing of integer data by one thread in a circular buffer
* for retrieval by another thread.
* 
* For any location which has not yet had data stored, the first data value will
* be stored without testing. Subsequent data stored at the same location will
* be tested against the data already in the location and the new data will only
* replace the existing data if the new data is a greater or lesser value,
* depending on the type of peak being stored (high or low).
*
* NOTE: The class is not Generic as Generic classes do not allow primitives
* for use as generic types.
* 
* The methods to store and retrieve the data are not synchronized, but the
* methods to adjust and retrieve the data pointers for each side are
* synchronized. Data access collision is prevented by the fact that the
* storage pointer and the retrieval pointer never point to the same location
* at the same time.
* 
* debug mks -- store and retrieve are now synced? fix above comment?
* 
* The data buffer is circular. When the end is reached, storage starts back
* over at the beginning.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package model;

//-----------------------------------------------------------------------------
// class DataTransferIntBuffer
//

public class DataTransferIntBuffer<T>{
    
    
int putPointer;
int getPointer;

int bufSize;
int buffer[];
int flags[];

int peakType;

//simple getters & setters

//constants    

public static final int CATCH_HIGHEST = 0;
public static final int CATCH_LOWEST = 1;

private static int DATA_RESET_VALUE = 0;
private static final int FLAG_RESET_VALUE = 0;


public final static int DATA_VALID = 0x0000000000000001;

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::DataTransferIntBuffer (constructor)
//
// Parameter pBufSize specifies the size of the data buffer.
// If parameter pPeakIsHigher is true, then a peak is determined by one value
// being higher than another. If false, a peak reflects the lowest value.
//

public DataTransferIntBuffer(int pBufSize, int pPeakType)
{

    bufSize = pBufSize;
    peakType = pPeakType;
    
}//end of DataTransferIntBuffer::DataTransferIntBuffer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::init
//
// Creates the data buffer of size pBufSize and prepares for use.
//

public void init()
{

    if (peakType == CATCH_HIGHEST){
        DATA_RESET_VALUE = Integer.MIN_VALUE;
    }
    else{
        DATA_RESET_VALUE = Integer.MAX_VALUE;        
    }

    buffer = new int[bufSize];
    flags = new int[bufSize];
    
}// end of DataTransferIntBuffer::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::reset
//
// Resets all data in the buffer and the put/get pointers to default values.
//

synchronized public void reset()
{

    for(int i=0; i<buffer.length; i++){
        buffer[i] = DATA_RESET_VALUE;
        flags[i] = FLAG_RESET_VALUE;
    }

    putPointer = 0;
    getPointer = -1;

}// end of DataTransferIntBuffer::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::putData
//
// Stores pData at location pointed by putPointer. If no data has been
// previously stored at that location, the location is simple set equal to
// pData. If location has been stored at that location, it is only updated
// with pData if pData is greater or lesser than the old data, depending on
// the state of peakIsHigher (true means higher data is a peak, false means
// lower data is a peak).
//

synchronized public void putData(int pData)
{
    
    if ((flags[putPointer] & DATA_VALID) != 0){
        //no data previously stored, so store new data
        buffer[putPointer] = pData;
        flags[putPointer] |= DATA_VALID;
    }else{
        //only store if new data is a new peak
        if(peakType == CATCH_HIGHEST){
            if (pData > buffer[putPointer]) buffer[putPointer] = pData;
        }
        else{
            if (pData < buffer[putPointer]) buffer[putPointer] = pData;            
        }        
    }
    
}// end of DataTransferIntBuffer::putData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::getData
//
// Retrieves the data and flags at location pointed by getPointer and returns
// it via the pDataSet object. If the data is still at the reset value, method
// returns false, if the data is valid, returns true.
//
// Regardless of whether the data is at reset value or valid, the data at
// the location is returned in pDataSet.
//

synchronized public boolean getData(DataSetInt pDataSet)
{
    
    pDataSet.d = buffer[getPointer];
    pDataSet.flags = buffer[getPointer];
    
    return( (flags[getPointer] & DATA_VALID) != 0 );
    
}// end of DataTransferIntBuffer::getData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::incrementPutPointer
//
// Increments the putPointer. If the new value is past the end of the buffer,
// it is restarted at zero.
//

synchronized public void incrementPutPointer()
{

    putPointer++;
    
    if(putPointer >= bufSize) putPointer = 0;
    
}// end of DataTransferIntBuffer::incrementPutPointer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::decrementPutPointer
//
// Decrements the putPointer. If the new value is less than zero, it is
// restarted at the end of the buffer.
//

synchronized public void decrementPutPointer()
{

    putPointer--;
    
    if(putPointer < 0) putPointer = bufSize-1;
    
}// end of DataTransferIntBuffer::decrementPutPointer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::incrementGetPointer
//
// Increments the getPointer. If the new value is past the end of the buffer,
// it is restarted at zero.
//

synchronized public void incrementGetPointer()
{

    getPointer++;
    
    if(getPointer >= bufSize) getPointer = 0;
    
}// end of DataTransferIntBuffer::incrementGetPointer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::decrementGetPointer
//
// Decrements the getPointer. If the new value is less than zero, it is
// restarted at the end of the buffer.
//

synchronized public void decrementGetPointer()
{

    getPointer--;
    
    if(getPointer < 0) getPointer = bufSize-1;
    
}// end of DataTransferIntBuffer::decrementGetPointer
//-----------------------------------------------------------------------------


}//end of class DataTransferIntBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

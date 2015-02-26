/******************************************************************************
* Title: DataTransferIntBuffer.java
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

public class DataTransferIntBuffer{

    
public int chartGroupNum = -1;
public int chartNum = -1;
public int graphNum = -1;
public int traceNum = -1;
    
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


public final static int DATA_VALID =    0x0000000000000001;
public final static int DATA_READY =    0x0000000000000002;
public final static int DATA_ERASED =   0x0000000000000004;
public static final int VERTICAL_BAR =  0x0000000000000008;
public static final int CIRCLE =        0x0000000000000010;


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
    getPointer = 0;

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
    
    if ((flags[putPointer] & DATA_VALID) == 0){
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
// DataTransferIntBuffer::setFlags
//
// OR's pFlags with flags[pIndex] to set one or more flag bits in the flags
// array at the specified position pIndex.
//

synchronized public void setFlags(int pIndex, int pFlags)
{

    flags[pIndex] |= pFlags;

}// end of DataTransferIntBuffer::setFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::setFlagsAtCurrentInsertionPoint
//
// OR's pFlags with flags[getPointer] to set one or more flag bits in the flags
// array at the current data insertion (put) point.
//

synchronized public void setFlagsAtCurrentInsertionPoint(int pFlags)
{

    flags[getPointer] |= pFlags;

}// end of DataTransferIntBuffer::setFlagsAtCurrentInsertionPoint
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
// DataTransferIntBuffer::getDataChange
//
// If new data is ready in the buffer or data has been marked for erasure, the
// data and flags at getPointer are returned via pDataSet and getPointer is
// incremented or decremented as appropriate.
//
// Note that the same data location can be marked ERASED and READY which means
// that the old data was erased and new data already added at the same spot.
// This happens when the producer thread erases and adds data before the
// consumer thread can respond. In this case, with repeated calls, the
// pointer will be decremented until the erased section has been passed and then
// subsequent calls will return the new data.
//
// Returns:
//
// +1 if data has been added and getPointer incremented
// 0 if no data has been added or removed
// -1 if data has been removed and getPointer decremented
//

synchronized public int getDataChange(DataSetInt pDataSet)
{

    //if data at current location has been marked erased, return that data and
    //move pointer to previous location
    
    if ((flags[getPointer] & DATA_ERASED) != 0){
        flags[getPointer] &= ~DATA_ERASED; //remove ERASED flag
        pDataSet.d = buffer[getPointer];
        pDataSet.flags = buffer[getPointer];        
        getPointer--;
        if(getPointer < 0) getPointer = bufSize-1;
        return(-1);
    }
        
    //if data at current location has been marked ready, return that data and
    //move pointer to next location
    
    if ((flags[getPointer] & DATA_READY) != 0){    
        pDataSet.d = buffer[getPointer];
        pDataSet.flags = flags[getPointer];        
        getPointer++;
        if(getPointer >= bufSize) getPointer = 0;
        return(1);
    }    
            
    return(0); //no data newly ready or removed
    
}// end of DataTransferIntBuffer::getDataChange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::incrementPutPointer
//
// Increments the putPointer. If the new value is past the end of the buffer,
// it is restarted at zero.
//
// Since the buffer is circular and data slots will be reused, the slot
// pointed to by putPointer is reset to be ready for new data.
//

synchronized public void incrementPutPointer()
{

    putPointer++;    
    if(putPointer >= bufSize) putPointer = 0;

    buffer[putPointer] = DATA_RESET_VALUE;
    flags[putPointer] = FLAG_RESET_VALUE;    
    
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
// DataTransferIntBuffer::incrementPutPointerAndSetReadyFlag
//
// Increments the putPointer. If the new value is past the end of the buffer,
// it is restarted at zero.
//
// The DATA_READY flag is set before the pointer is incremented.
//
// Since the buffer is circular and data slots will be reused, the slot
// pointed to by putPointer is reset to be ready for new data.
//

synchronized public void incrementPutPointerAndSetReadyFlag()
{

    flags[putPointer] |= DATA_READY;
    putPointer++;    
    if(putPointer >= bufSize) putPointer = 0;

    buffer[putPointer] = DATA_RESET_VALUE;
    flags[putPointer] = FLAG_RESET_VALUE;    

}// end of DataTransferIntBuffer::incrementPutPointerAndSetReadyFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntBuffer::decrementPutPointerAndSetErasedFlag
//
// Decrements the putPointer. If the new value is less than zero, it is
// restarted at the end of the buffer.
//
// The DATA_ERASED flag is set before the pointer is deccremented.
//

synchronized public void decrementPutPointerAndSetErasedFlag()
{

    flags[putPointer] |= DATA_ERASED;
    putPointer--;
    if(putPointer < 0) putPointer = bufSize-1;
    
}// end of DataTransferIntBuffer::decrementPutPointerAndSetErasedFlag
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


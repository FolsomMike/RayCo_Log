/******************************************************************************
* Title: DataTransferIntMultiDimBuffer.java
* Author: Mike Schoonover
* Date: 03/16/15
*
* Purpose:
*
* This class handles storing of integer data by one thread in a circular buffer
* for retrieval by another thread.
* 
* The buffer is a multi-dimensional array. Data is stored and retrieved one
* slice at a time.
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
* Because data can be erased, at which time the put and get pointers might
* end up pointing at the same place simultaneously, all data storage and
* retrieval as well as pointer manipulation must be synchronized.
* 
* The data buffer dataBuf is circular. When the end is reached, storage starts
* back over at the beginning.
*
* A separate array named metaBuf is used to store information about each
* point in buffer array, such as the system which generated the point, the
* point's color, or any other information.
* 
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package model;

//-----------------------------------------------------------------------------
// class DataTransferIntMultiDimBuffer
//

public class DataTransferIntMultiDimBuffer{

    
public int chartGroupNum = -1;
public int chartNum = -1;
public int graphNum = -1;
public int traceNum = -1;
    
int putPointer;
int getPointer;

int bufLength;
int bufWidth;
int dataBuf[][];
int metaBuf[][];
int flags[];

int peakType;

//simple getters & setters

//constants    

public static final int CATCH_HIGHEST = 0;
public static final int CATCH_LOWEST = 1;

private static int DATA_RESET_VALUE = 0;
private static final int META_RESET_VALUE = 0;

private static final int FLAG_RESET_VALUE = 0x0000000000000000;
public final static int DATA_VALID =        0x0000000000000001;
public final static int DATA_READY =        0x0000000000000002;
public final static int DATA_ERASED =       0x0000000000000004;
public static final int VERTICAL_BAR =      0x0000000000000008;
public static final int CIRCLE =            0x0000000000000010;


//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::DataTransferIntMultiDimBuffer (constructor)
//
// Parameters pBufLength, pBufWidth specify the size of the data buffer array.
// If parameter pPeakIsHigher is true, then a peak is determined by one value
// being higher than another. If false, a peak reflects the lowest value.
//

public DataTransferIntMultiDimBuffer(int pBufLength, int pBufWidth,
                                                                 int pPeakType)
{

    bufLength = pBufLength; bufWidth = pBufWidth;
    peakType = pPeakType;
    
}//end of DataTransferIntMultiDimBuffer::DataTransferIntMultiDimBuffer (constr)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::init
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

    dataBuf = new int[bufLength][bufWidth];
    metaBuf = new int[bufLength][bufWidth];
    flags = new int[bufLength];
    
}// end of DataTransferIntMultiDimBuffer::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::reset
//
// Resets all data in the buffer and the put/get pointers to default values.
//

synchronized public void reset()
{

    for(int i=0; i<dataBuf.length; i++){
        for(int j=0; j<dataBuf[i].length; j++){
        dataBuf[i][j] = DATA_RESET_VALUE;
        metaBuf[i][j] = META_RESET_VALUE;
        }
    }
        
    for(int k=0; k<dataBuf.length; k++){
        flags[k] = FLAG_RESET_VALUE;
    }

    putPointer = 0;
    getPointer = 0;

}// end of DataTransferIntMultiDimBuffer::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::putData
//
// Stores a slice of pData at row location pointed by putPointer. If no data
// has been previously stored at that row, the locations are simple set equal
// to pData. If data has been stored at that row, it is only updated
// with the values in pData if the values in pData are greater or lesser than
// the old data in each corresponding position in the row, depending on the
// state of peakIsHigher (true means higher data is a peak, false means lower
// data is a peak).
//
// For every new data point stored in buffer, the corresponding meta data in
// pMetaData will be stored in metaBuf.
//

synchronized public void putData(int[] pData, int[] pMetaData)
{
    
    if ((flags[putPointer] & DATA_VALID) == 0){

        //no data previously stored, so store new data
        System.arraycopy(pData,0, dataBuf[putPointer], 0, pData.length);
        System.arraycopy(pMetaData,0,metaBuf[putPointer], 0, pMetaData.length);        
        flags[putPointer] |= DATA_VALID;
        
    }else{
        //only store if new data is a new peak
        if(peakType == CATCH_HIGHEST){            
            for(int i=0; i<pData.length; i++){           
                if (pData[i] > dataBuf[putPointer][i]){
                    dataBuf[putPointer][i] = pData[i];
                    metaBuf[putPointer][i] = pMetaData[i];                
                }            
            } 
        }
        else{            
            for(int i=0; i<pData.length; i++){           
                if (pData[i] < dataBuf[putPointer][i]){
                    dataBuf[putPointer][i] = pData[i];
                    metaBuf[putPointer][i] = pMetaData[i];                
                }            
            }            
        }        
    }
    
}// end of DataTransferIntMultiDimBuffer::putData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::setFlags
//
// OR's pFlags with flags[pIndex] to set one or more flag bits in the flags
// array at the specified position pIndex.
//

synchronized public void setFlags(int pIndex, int pFlags)
{

    flags[pIndex] |= pFlags;

}// end of DataTransferIntMultiDimBuffer::setFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::setFlagsAtCurrentInsertionPoint
//
// OR's pFlags with flags[getPointer] to set one or more flag bits in the flags
// array at the current data insertion (put) point.
//

synchronized public void setFlagsAtCurrentInsertionPoint(int pFlags)
{

    flags[getPointer] |= pFlags;

}// end of DataTransferIntMultiDimBuffer::setFlagsAtCurrentInsertionPoint
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::getData
//
// Retrieves the row of data and meta data and flags at location pointed by
// getPointer and returns it via the pDataSet object. If the data is still at
// the reset value, method returns false, if the data is valid, returns true.
//
// Regardless of whether the data is at reset value or valid, the data at
// the location is returned in pDataSet.
//

synchronized public boolean getData(DataSetIntMultiDim pDataSet)
{
    
    System.arraycopy(dataBuf[putPointer], 0, pDataSet.d, 0, pDataSet.length);
    System.arraycopy(metaBuf[putPointer], 0, pDataSet.m, 0, pDataSet.length);
    
    pDataSet.flags = flags[getPointer];
    
    return( (flags[getPointer] & DATA_VALID) != 0 );
    
}// end of DataTransferIntMultiDimBuffer::getData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::getDataChange
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
// subsequent calls will return the new data. That is why the ERASED flag is
// checked first and takes precedent over the READY flag.
//
// Returns:
//
// +1 if data has been added and getPointer incremented
// 0 if no data has been added or removed
// -1 if data has been removed and getPointer decremented
//

synchronized public int getDataChange(DataSetIntMultiDim pDataSet)
{

    //if data at current location has been marked erased, return that data and
    //move pointer to previous location
    
    if ((flags[getPointer] & DATA_ERASED) != 0){
        flags[getPointer] &= ~DATA_ERASED; //remove ERASED flag    
        System.arraycopy(dataBuf[putPointer],0, pDataSet.d,0, pDataSet.length);
        System.arraycopy(metaBuf[putPointer],0, pDataSet.m,0, pDataSet.length);
        pDataSet.flags = flags[getPointer];        
        getPointer--;
        if(getPointer < 0) getPointer = bufLength-1;
        return(-1);
    }
        
    //if data at current location has been marked ready, return that data and
    //move pointer to next location
    
    if ((flags[getPointer] & DATA_READY) != 0){    
        System.arraycopy(dataBuf[putPointer],0, pDataSet.d,0, pDataSet.length);
        System.arraycopy(metaBuf[putPointer],0, pDataSet.m,0, pDataSet.length);
        pDataSet.flags = flags[getPointer];        
        getPointer++;
        if(getPointer >= bufLength) getPointer = 0;
        return(1);
    }    
            
    return(0); //no data newly ready or removed
    
}// end of DataTransferIntMultiDimBuffer::getDataChange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::incrementPutPointer
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
    if(putPointer >= bufLength) putPointer = 0;

    for (int i=0; i<bufWidth; i++){
        dataBuf[putPointer][i] = DATA_RESET_VALUE;
        metaBuf[putPointer][i] = META_RESET_VALUE;        
    }
    
    flags[putPointer] = FLAG_RESET_VALUE;    
    
}// end of DataTransferIntMultiDimBuffer::incrementPutPointer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::decrementPutPointer
//
// Decrements the putPointer. If the new value is less than zero, it is
// restarted at the end of the buffer.
//

synchronized public void decrementPutPointer()
{

    putPointer--;
    if(putPointer < 0) putPointer = bufLength-1;
    
}// end of DataTransferIntMultiDimBuffer::decrementPutPointer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::incrementPutPointerAndSetReadyFlag
//
// Increments the putPointer. If the new value is past the end of the buffer,
// it is restarted at zero.
//
// The DATA_READY flag is set before the pointer is incremented to signal that
// the data in the current position is ready for extraction.
//
// Since the buffer is circular and data slots will be reused, the slot
// pointed to by putPointer is reset to be ready for new data.
//

synchronized public void incrementPutPointerAndSetReadyFlag()
{

    flags[putPointer] |= DATA_READY;
    
    putPointer++;    
    if(putPointer >= bufLength) putPointer = 0;

    for (int i=0; i<bufWidth; i++){
        dataBuf[putPointer][i] = DATA_RESET_VALUE;
        metaBuf[putPointer][i] = META_RESET_VALUE;        
    }
    
    flags[putPointer] = FLAG_RESET_VALUE;    

}// end of DataTransferIntMultiDimBuffer::incrementPutPointerAndSetReadyFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::decrementPutPointerAndSetErasedFlag
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
    if(putPointer < 0) putPointer = bufLength-1;
    
}// end of DataTransferIntMultiDimBuffer::decrementPutPointerAndSetErasedFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::incrementGetPointer
//
// Increments the getPointer. If the new value is past the end of the buffer,
// it is restarted at zero.
//

synchronized public void incrementGetPointer()
{

    getPointer++;    
    if(getPointer >= bufLength) getPointer = 0;
    
}// end of DataTransferIntMultiDimBuffer::incrementGetPointer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DataTransferIntMultiDimBuffer::decrementGetPointer
//
// Decrements the getPointer. If the new value is less than zero, it is
// restarted at the end of the buffer.
//

synchronized public void decrementGetPointer()
{

    getPointer--;    
    if(getPointer < 0) getPointer = bufLength-1;
    
}// end of DataTransferIntMultiDimBuffer::decrementGetPointer
//-----------------------------------------------------------------------------

}//end of class DataTransferIntMultiDimBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------


/******************************************************************************
* Title: HighPeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* -- WARNING --
* Do not use this class or its subclasses for time sensitive loops where a
* lot of data is being processed at hight speed. Generics cannot use primitives
* so every primitive value (int, double, etc.) must be autoboxed/unboxed into
* its corresponding object (Integer, Double, etc.) when methods in this class
* are called. The autoboxing/unboxing process causes overhead since an object
* is created and released each time.
*  
* 
* Purpose:
* 
* This is a Generic class used to detect and store highest peak values.
* A new value replaces the previously stored peak if the new value is greater
* than the old peak.
* 
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import toolkit.MKSInteger;
import toolkit.MKSWrapper;

//-----------------------------------------------------------------------------
// class HighPeakBuffer
//

public class HighPeakBuffer<T extends MKSWrapper> extends PeakBuffer<T>
{
    
//-----------------------------------------------------------------------------
// HighPeakBuffer::HighPeakBuffer (constructor)
//

public HighPeakBuffer(int pIndex)
{

    super(pIndex);
    
}//end of HighPeakBuffer::HighPeakBuffer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// HighPeakBuffer::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pValue > old peak, pValue is stored as the new peak.
//
// Must use compareTo because == only works with primitives and since the
// class is Generic, Java can't be sure what types will be compared.
//

@Override
public synchronized void catchPeak(T pValue)
{
   
    if (pValue.compareTo(peak) > 0){ peak = pValue; }
    
}// end of HighPeakBuffer::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// HighPeakBuffer::setResetValue
//

@Override
public synchronized void setResetValue(Object pO)
{

    ((MKSInteger)peakReset).x = ((MKSInteger)pO).x;
    
}// end of HighPeakBuffer::setResetValue
//-----------------------------------------------------------------------------

}//end of class HighPeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

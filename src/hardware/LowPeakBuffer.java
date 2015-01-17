/******************************************************************************
* Title: LowPeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This is a Generic class used to detect and store lowest peak values.
* A new value replaces the previously stored peak if the new value is lesser
* than the old peak.
* 
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class LowPeakBuffer
//

public class LowPeakBuffer<T extends Comparable<T>> extends PeakBuffer<T>
{
    
//-----------------------------------------------------------------------------
// LowPeakBuffer::LowPeakBuffer (constructor)
//

public LowPeakBuffer(int pIndex)
{

    super(pIndex);
    
}//end of LowPeakBuffer::LowPeakBuffer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LowPeakBuffer::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

}// end of LowPeakBuffer::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LowPeakBuffer::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pValue < old peak, pValue is stored as the new peak.
//
// Must use compareTo because == only works with primitives and since the
// class is Generic, Java can't be sure what types will be compared.
//

@Override
public synchronized void catchPeak(T pValue)
{
   
    if (pValue.compareTo(peak) < 0){ peak = pValue; }
    
}// end of LowPeakBuffer::catchPeak
//-----------------------------------------------------------------------------

}//end of class LowPeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

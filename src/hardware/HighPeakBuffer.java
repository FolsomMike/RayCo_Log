/******************************************************************************
* Title: HighPeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
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
//-----------------------------------------------------------------------------
// class HighPeakBuffer
//

public class HighPeakBuffer<T extends Comparable<T>> extends PeakBuffer<T>
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
// HighPeakBuffer::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

}// end of HighPeakBuffer::init
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

}//end of class HighPeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

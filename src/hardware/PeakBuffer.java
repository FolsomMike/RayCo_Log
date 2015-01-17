/******************************************************************************
* Title: PeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This is a Generic parent class used to detect and store peak values.
* The subclasses override the catchPeak method to provide specific code
* for catching different types of peaks, such as highest value, lowest value,
* closest to a target value, etc...
* 
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class PeakBuffer
//

public class PeakBuffer<T extends Comparable<T>>
{

    private final int index;

    T peak;
    private T peakReset;
    
//-----------------------------------------------------------------------------
// PeakBuffer::PeakBuffer (constructor)
//

public PeakBuffer(int pIndex)
{

    index = pIndex;
    
}//end of PeakBuffer::PeakBuffer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{


}// end of PeakBuffer::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchPeak
//
// This method must be overridden by subclasses to provide the specific
// comparison to a catch the desire type of peak, such as the highest value,
// lowest value, closest to a target value, etc.
//

public synchronized void catchPeak(T pValue)
{
    
}// end of PeakBuffer::catchPeak
//-----------------------------------------------------------------------------

/*

//-----------------------------------------------------------------------------
// PeakBuffer::catchHighPeak
//
// If pValue > old peak, pValue is stored as the new peak.
//
// Must use compareTo because == only works with primitives and since the
// class is Generic, Java can't be sure what types will be compared.
//

public synchronized void catchHighPeak(T pValue)
{
    
    if (peak.compareTo(pValue) > 0){ peak = pValue; }
    
}// end of PeakBuffer::catchHighPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchLowPeak
//
// If pValue < old peak, pValue is stored as the new peak.
//
// Must use compareTo because == only works with primitives and since the
// class is Generic, Java can't be sure what types will be compared.
//

public synchronized void catchLowPeak(T pValue)
{
    
    if (peak.compareTo(pValue) < 0){ peak = pValue; }
    
}// end of PeakBuffer::catchLowPeak
//-----------------------------------------------------------------------------

*/

//-----------------------------------------------------------------------------
// PeakBuffer::setPeak
//
// Forces peak to pValue.
//

public synchronized void setPeak(T pValue)
{

    peak = pValue;
    
}// end of PeakBuffer::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::reset
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public synchronized void reset()
{

    peak = peakReset;
    
}// end of PeakBuffer::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setResetValue
//
// Sets the value to which the peak is reset when the previous peak has been
// retrieved and a new peak is to be found.
//

public synchronized void setResetValue(T pValue)
{

    peakReset = pValue;
    
}// end of PeakBuffer::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeak
//
// Retrieves the current value of the peak without resetting it.
//

public synchronized T getPeak()
{

    return(peak);
    
}// end of PeakBuffer::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//

public synchronized T getPeakAndReset()
{

    T value = peak; peak = peakReset;
    
    return(value);
    
}// end of PeakBuffer::getPeakAndReset
//-----------------------------------------------------------------------------

}//end of class PeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

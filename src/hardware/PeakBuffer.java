/******************************************************************************
* Title: PeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class is the parent class for subclasses which store peak values.
* Various types of variables are handled as well as different methods of
* determining a peak, i.e. larger values, smaller values, or values closest to
* a target value.
* 
* Since each data type is handled separately, a single PeakBuffer object could
* be used to handle one integer peak, one double peak, and so.
* 
* New data types and peak algorithms can be added as required.
* 
* This class could probably simplified by converting it to a Generic.
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

public class PeakBuffer
{

    private final int index;

    private int peakInt = 0;
    private double peakDouble = 0;
    
    private int peakIntReset = 0;
    private double peakDoubleReset = 0;

    
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
// PeakBuffer::catchHighPeak
//
// If pValue > old peak, pValue is stored as the new peak.
//

public synchronized void catchHighPeak(double pValue)
{

    if (pValue > peakDouble) { peakDouble = pValue; }
    
}// end of PeakBuffer::catchHighPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchLowPeak
//
// If pValue < old peak, pValue is stored as the new peak.
//

public synchronized void catchLowPeak(double pValue)
{

    if (pValue < peakDouble) { peakDouble = pValue; }
    
}// end of PeakBuffer::catchLowPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchHighPeak
//
// If pValue > old peak, pValue is stored as the new peak.
//

public synchronized void catchHighPeak(int pValue)
{

    if (pValue > peakInt) { peakInt = pValue; }
    
}// end of PeakBuffer::catchHighPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchLowPeak
//
// If pValue < old peak, pValue is stored as the new peak.
//

public synchronized void catchLowPeak(int pValue)
{

    if (pValue < peakInt) { peakInt = pValue; }
    
}// end of PeakBuffer::catchLowPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setPeak
//
// Forces peak to pValue.
//

public synchronized void setPeak(double pValue)
{

    peakDouble = pValue;
    
}// end of PeakBuffer::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setPeak
//
// Forces peak to pValue.
//

public synchronized void setPeak(int pValue)
{

    peakInt = pValue;
    
}// end of PeakBuffer::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::resetDouble
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public synchronized void resetDouble()
{

    peakDouble = peakDoubleReset;
    
}// end of PeakBuffer::resetDouble
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::resetInt
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public synchronized void resetInt()
{

    peakInt = peakIntReset;
    
}// end of PeakBuffer::resetInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setResetValue
//
// Sets the value to which the peak is reset when the previous peak has been
// retrieved and a new peak is to be found.
//

public synchronized void setResetValue(double pValue)
{

    peakDoubleReset = pValue;
    
}// end of PeakBuffer::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setResetValue
//
// Sets the value to which the peak is reset when the previous peak has been
// retrieved and a new peak is to be found.
//

public synchronized void setResetValue(int pValue)
{

    peakIntReset = pValue;
    
}// end of PeakBuffer::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakDbl
//
// Retrieves the current value of the peak without resetting it.
//

public synchronized double getPeakDbl()
{

    return(peakDouble);
    
}// end of PeakBuffer::getPeakDbl
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakInt
//
// Retrieves the current value of the peak without resetting it.
//

public synchronized double getPeakInt()
{

    return(peakDouble);
    
}// end of PeakBuffer::getPeakInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakDblAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//

public synchronized double getPeakDblAndReset()
{

    double value = peakDouble; peakDouble = peakDoubleReset;
    
    return(value);
    
}// end of PeakBuffer::getPeakDblAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakIntAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//

public synchronized int getPeakIntAndReset()
{

    int value = peakInt; peakInt = peakIntReset;
    
    return(value);
    
}// end of PeakBuffer::getPeakIntAndReset
//-----------------------------------------------------------------------------

}//end of class PeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: PeakBufferInteger.java
* Author: Mike Schoonover
* Date: 01/17/15
* 
* Purpose:
* 
* This class is the parent class for classes which store and detect peaks
* using Integer values.
* 
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import toolkit.MKSInteger;

//-----------------------------------------------------------------------------
// class PeakBufferInteger
//

public class PeakBufferInteger extends PeakBuffer
{

    MKSInteger peak;
    MKSInteger peakReset;
    
//-----------------------------------------------------------------------------
// PeakBufferInteger::PeakBufferInteger (constructor)
//

public PeakBufferInteger(int pIndex)
{

    super(pIndex);
    
    peak = new MKSInteger(0);
    peakReset = new MKSInteger(0);
    
}//end of PeakBufferInteger::PeakBufferInteger (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInteger::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//

@Override
public synchronized void catchPeak(Object pO)
{

    // This method must be overridden by subclasses.    

}// end of PeakBufferInteger::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInteger::setPeak
//
// Forces peak to pValue.
//

@Override
public synchronized void setPeak(Object pO)
{
    
    ((MKSInteger)peak).x = ((MKSInteger)pO).x;
    
}// end of PeakBuffer::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInteger::reset
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

@Override
public synchronized void reset()
{

    peak.x = peakReset.x;
    
}// end of PeakBufferInteger::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInteger::getPeak
//
// Retrieves the current value of the peak without resetting it.
//

@Override
public synchronized void getPeak(Object pO)
{

    ((MKSInteger)pO).x = peak.x;
    
}// end of PeakBufferInteger::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInteger::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//

@Override
public synchronized void getPeakAndReset(Object pO)
{

    ((MKSInteger)pO).x = peak.x;
    
    peak.x = peakReset.x;
    
}// end of PeakBufferInteger::getPeakAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInteger::setResetValue
//
// Sets the value to which the peak value will be set when it is "reset".
//

@Override
public synchronized void setResetValue(Object pO)
{

    ((MKSInteger)peakReset).x = ((MKSInteger)pO).x;
    
}// end of PeakBufferInteger::setResetValue
//-----------------------------------------------------------------------------

}//end of class PeakBufferInteger
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

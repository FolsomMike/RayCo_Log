/******************************************************************************
* Title: PeakBufferInt.java
* Author: Mike Schoonover
* Date: 03/18/15
*
* Purpose:
*
* This is a parent class used to detect and store peak values in an int.
* The subclasses override the catchPeak method to provide specific code
* for catching different types of peaks, such as highest value, lowest value,
* closest to a target value, etc...
*
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
* -- Note --
*
* An attempt was made to make this class a Generic, but Generics only work
* with Objects and not primitives. If an Object wrapper (Integer, Double, etc.)
* was used to store the peak, it would have been inefficient as those objects
* must be recreated each time their value is changed as they are immutable.
* It became very convoluted.
*
*/

//-----------------------------------------------------------------------------

package hardware;

import toolkit.MKSInteger;

//-----------------------------------------------------------------------------
// class PeakBufferInt
//

public class PeakBufferInt
{

    int peak;
    int peakReset;

    final int peakBufferNum;

    boolean peakUpdated;

//-----------------------------------------------------------------------------
// PeakBufferInt::PeakBufferInt (constructor)
//

public PeakBufferInt(int pPeakBufferNum)
{

    peakBufferNum = pPeakBufferNum;

    peakUpdated = false;

}//end of PeakBufferInt::PeakBufferInt (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInt::catchPeak
//
// This method must be overridden by subclasses to provide the specific
// comparison to a catch the desire type of peak, such as the highest value,
// lowest value, closest to a target value, etc.
//

public synchronized void catchPeak(int pNewData)
{

    // This method must be overridden by subclasses.

}// end of PeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInt::setPeak
//
// Forces peak to pValue.
//

public synchronized void setPeak(int pValue)
{

    peak = pValue;

}// end of PeakBufferInt::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInt::reset
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public synchronized void reset()
{

    peak = peakReset;

    peakUpdated = false;

}// end of PeakBufferInt::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInt::setResetValue
//
// Sets the value to which the peak is reset when the previous peak data
// has been retrieved and a new peak is to be found.
//

public synchronized void setResetValue(int pValue)
{

    peakReset = pValue;

}// end of PeakBufferInt::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInt::getPeak
//
// Retrieves the current value of the peak without resetting it.
//

public synchronized void getPeak(MKSInteger pPeakData)
{

    pPeakData.x = peak;

}// end of PeakBufferInt::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBufferInt::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//
// Returns true if the peak has been updated since the last call to this method
// or false otherwise.
//

public synchronized boolean getPeakAndReset(MKSInteger pPeakData)
{

    System.out.println("peak data grabbed");//DEBUG HSS//

    boolean lPeakUpdated = peakUpdated;

    pPeakData.x = peak;

    reset();

    return(lPeakUpdated);

}// end of PeakBufferInt::getPeakAndReset
//-----------------------------------------------------------------------------

}//end of class PeakBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

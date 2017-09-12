/******************************************************************************
* Title: PeakArrayBufferInt.java
* Author: Mike Schoonover
* Date: 03/18/15
*
* Purpose:
*
* This is a parent class used to detect and store peak values in an int array.
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

//-----------------------------------------------------------------------------
// class PeakArrayBufferInt
//

public class PeakArrayBufferInt
{

    int[] peakArray;
    int[] peakArrayReset;
    public int peak = 0;

    final int peakArrayBufferNum;
    final int arraySize;

    boolean peakUpdated;

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::PeakArrayBufferInt (constructor)
//

public PeakArrayBufferInt(int pPeakArrayBufferNum, int pArraySize)
{

    peakArrayBufferNum = pPeakArrayBufferNum;
    arraySize = pArraySize;

    peakArray = new int[arraySize];
    peakArrayReset = new int[arraySize];

    peakUpdated = false;

}//end of PeakArrayBufferInt::PeakArrayBufferInt (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::catchPeak
//
// This method must be overridden by subclasses to provide the specific
// comparison to a catch the desire type of peak, such as the highest value,
// lowest value, closest to a target value, etc.
//

public void catchPeak(int[] pNewData)
{

    // This method must be overridden by subclasses.

}// end of PeakArrayBufferInt::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::catchPeak
//
// This method must be overridden by subclasses to provide the specific
// comparison to a catch the desire type of peak, such as the highest value,
// lowest value, closest to a target value, etc.
//

public void catchPeak(int pNewPeak, int[] pNewData)
{

    // This method must be overridden by subclasses.

}// end of PeakArrayBufferInt::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::setPeak
//
// Forces peak array to pValue.
//

public void setPeak(int[] pValueArray)
{

    peakUpdated = true;

    System.arraycopy(pValueArray, 0, peakArray, 0, arraySize);

}// end of PeakArrayBufferInt::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::reset
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public void reset()
{

    System.arraycopy(peakArrayReset, 0, peakArray, 0, arraySize);
    peak = 0;
    peakUpdated = false;

}// end of PeakArrayBufferInt::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::setResetValue
//
// Sets the value to which the peak array is reset when the previous peak data
// has been retrieved and a new peak is to be found.
//
// This version of the method allows each position in the array to be set
// individually.
//

public void setResetValue(int[] pValueArray)
{

    System.arraycopy(pValueArray, 0, peakArrayReset, 0, arraySize);

}// end of PeakArrayBufferInt::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::setResetValue
//
// Sets the value to which the peak array is reset when the previous peak data
// has been retrieved and a new peak is to be found.
//
// This version of the method sets all positions in the reset array to pValue.
//

public void setResetValue(int pValue)
{

    for(int i=0; i<arraySize; i++){ peakArrayReset[i] = pValue; }

}// end of PeakArrayBufferInt::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::getPeak
//
// Retrieves the current value of the peak array without resetting it.
//

public void getPeak(int[] pPeakData)
{

    System.arraycopy(peakArray, 0, pPeakData, 0, arraySize);

}// end of PeakArrayBufferInt::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakArrayBufferInt::getPeakAndReset
//
// Retrieves the current value of the peak array and resets the peak to the
// reset value.
//
// Returns true if the peak has been updated since the last call to this method
// or false otherwise.
//

public boolean getPeakAndReset(int[] pPeakData)
{

    boolean lPeakUpdated = peakUpdated;

    System.arraycopy(peakArray, 0, pPeakData, 0, arraySize);
    reset();

    return(lPeakUpdated);

}// end of PeakArrayBufferInt::getPeakAndReset
//-----------------------------------------------------------------------------

}//end of class PeakArrayBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

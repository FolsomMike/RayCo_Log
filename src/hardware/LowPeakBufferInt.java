/******************************************************************************
* Title: LowPeakBufferInt.java
* Author: Mike Schoonover
* Date: 03/18/15
*
* Purpose:
*
* This class is used to detect and store the lowest peak value of type int. A
* new value replaces the previously stored peak if the new value is lesser than
* the old peak.
*
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class LowPeakBufferInt
//

public class LowPeakBufferInt extends PeakBufferInt
{

//-----------------------------------------------------------------------------
// LowPeakBufferInt::LowPeakBufferInt (constructor)
//

public LowPeakBufferInt(int pIndex)
{

    super(pIndex);

}//end of LowPeakBufferInt::LowPeakBufferInt (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LowPeakBufferInt::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pNewData < old peak, pNewData is stored as the new peak.
//

@Override
public void catchPeak(int pNewData)
{

    if(pNewData < peak) { peak = pNewData; peakUpdated = true; }

}// end of LowPeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

}//end of class LowPeakBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

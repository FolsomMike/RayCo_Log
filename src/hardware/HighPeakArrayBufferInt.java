/******************************************************************************
* Title: HighPeakBufferArrayInt.java
* Author: Mike Schoonover
* Date: 03/18/15
*
* Purpose:
*
* This class used to detect and store an array of highest peak values of type
* integer. A new value replaces the previously stored peak if the new value is
* greater than the old peak.
*
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class HighPeakBufferInt
//

public class HighPeakArrayBufferInt extends PeakArrayBufferInt
{

//-----------------------------------------------------------------------------
// HighPeakBufferInt::HighPeakBufferInt (constructor)
//

public HighPeakArrayBufferInt(int pIndex, int pArraySize)
{

    super(pIndex, pArraySize);

}//end of HighPeakBufferInt::HighPeakBufferInt (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// HighPeakBufferInt::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pNewData > old peak, pNewData is stored as the new peak.
//

@Override
public void catchPeak(int[] pNewData)
{

    for(int i=0; i<arraySize; i++){
        if(pNewData[i] > peakArray[i]) {
            peakArray[i] = pNewData[i];
            peakUpdated = true;
        }
    }

}// end of HighPeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// HighPeakBufferInt::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pNewPeak > old peak, pNewData is stored.
//

@Override
public void catchPeak(int pNewPeak, int[] pNewData)
{

    if (pNewPeak>peak) { peak=pNewPeak; setPeak(pNewData); }

}// end of HighPeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

}//end of class HighPeakBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

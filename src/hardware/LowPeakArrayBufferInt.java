/******************************************************************************
* Title: LowPeakBufferArrayInt.java
* Author: Mike Schoonover
* Date: 03/18/15
* 
* Purpose:
* 
* This class used to detect and store an array of highest peak values of type
* integer. A new value replaces the previously stored peak if the new value is
* lesser than the old peak.
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

public class LowPeakArrayBufferInt extends PeakArrayBufferInt
{
    
//-----------------------------------------------------------------------------
// LowPeakBufferInt::LowPeakBufferInt (constructor)
//

public LowPeakArrayBufferInt(int pIndex, int pArraySize)
{

    super(pIndex, pArraySize);
    
}//end of LowPeakBufferInt::LowPeakBufferInt (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LowPeakBufferInt::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pNewData > old peak, pNewData is stored as the new peak.
//

@Override
public synchronized void catchPeak(int[] pNewData)
{    
    
    for(int i=0; i<arraySize; i++){
        if(pNewData[i] < peakArray[i]) { 
            peakArray[i] = pNewData[i];
            peakUpdated = true;
        }
    }

}// end of LowPeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

}//end of class LowPeakBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

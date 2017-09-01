/******************************************************************************
* Title: HighPeakSnapshotBuffer.java
* Author: Hunter Schoonover
* Date: 09/01/2017
*
* Purpose:
*
* This class used to detect and store an array of highest peak values for a 
* snapshot. A new array replaces the previously stored array if the new peak is
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

public class HighPeakSnapshotBuffer extends PeakSnapshotBuffer
{

//-----------------------------------------------------------------------------
// HighPeakBufferInt::HighPeakBufferInt (constructor)
//

public HighPeakSnapshotBuffer(int pIndex, int pArraySize)
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
// If pNewPeak > old peak, pNewData is stored.
//

@Override
public synchronized void catchPeak(int pNewPeak, int[] pNewData)
{

    if (pNewPeak>peak) { peak=pNewPeak; setPeak(pNewData); }

}// end of HighPeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

}//end of class HighPeakBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

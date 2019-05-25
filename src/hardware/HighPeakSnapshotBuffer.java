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

public String title = "";//DEBUG HSS// remove later

@Override
public void catchPeak(int pNewPeak, int[] pNewData)
{
    
    if (pNewPeak>peak) { 
        
        //DEBUG HSS// remove later
        if ("Longitudianl Multi-IO Config Board".equals(title)) {
            //DEBUG HSS//System.out.println("Storing peak from device:: peak=" + peak + ", New peak=" + pNewPeak);
        }
        //System.out.print(", Peak in array = " + pNewData[pNewData.length/2]);
        //DEBUG HSS// remove later end
        
        peak=pNewPeak; setPeak(pNewData); }

}// end of HighPeakBufferInt::catchPeak
//-----------------------------------------------------------------------------

}//end of class HighPeakBufferInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

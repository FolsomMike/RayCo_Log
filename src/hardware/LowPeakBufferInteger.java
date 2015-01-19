/******************************************************************************
* Title: LowPeakBufferInteger.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* 
* Purpose:
*
* This class used to detect and store lowest peak values of type Integer.
* A new value replaces the previously stored peak if the new value is lesser
* than the old peak.
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
//-----------------------------------------------------------------------------
// class LowPeakBufferInteger
//

public class LowPeakBufferInteger extends PeakBufferInteger
{
    
//-----------------------------------------------------------------------------
// LowPeakBufferInteger::LowPeakBufferInteger (constructor)
//

public LowPeakBufferInteger(int pIndex)
{

    super(pIndex);
    
}//end of LowPeakBufferInteger::LowPeakBufferInteger (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LowPeakBufferInteger::catchPeak
//
// This method overrides that in the parent class to provide the specific
// type of comparison for the peak type being captured.
//
// If pO.x < old peak, pO.x is stored as the new peak.
//

@Override
public synchronized void catchPeak(Object pO)
{
   
    int v = ((MKSInteger)pO).x;
    
    if (v < peak.x) { peak.x = v; }

}// end of LowPeakBufferInteger::catchPeak
//-----------------------------------------------------------------------------

}//end of class LowPeakBufferInteger
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

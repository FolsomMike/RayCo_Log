/******************************************************************************
* Title: PeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
* 
* 
* -- Note --
* 
* An attempt was made to make this class a Generic, but Generics only work
* with Objects and not primitives. If an Object wrapper (Integer, Double, etc.)
* was used to store the peak, it would have been inefficient as those objects
* must be recreated each time their value is changed as they are immutable.
* It became very convoluted.
* 
* This version accepts Objects for all parameters and the subclasses cast the
* Objects to the desired class before processing. This allows the subclasses
* to override methods in the PeakBuffer class and then work with them as
* different types.
* 
* There is overhead with the casting...but the overhead of "if" statements is
* no longer expended in the main code which were required to differentiate
* between high/low peaks and data types. The tradeoff is probably worth the
* cleaner code.
* 
* To optimize the class for speed, it accepts and returns data through objects
* rather than using any primitives. Any primitives would require boxing/unboxing
* to compare with, be set equal to, or applied to the objects used in the class
* for storing the peak value. Any objects used in the class are created one
* time to avoid repeated object allocation. That is also why the Java wrapper
* classes are not used (Integer, Double, etc.) as they are immutable and require
* new object creation each time their value is changed.
* 
* Purpose:
*  
* This is a parent class used to detect and store peak values.
* The subclasses override the catchPeak method to provide specific code
* for catching different types of peaks, such as highest value, lowest value,
* closest to a target value, etc...
* 
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
// class PeakBuffer
//

public class PeakBuffer
{

    private final int index;
    
//-----------------------------------------------------------------------------
// PeakBuffer::PeakBuffer (constructor)
//

public PeakBuffer(int pIndex)
{

    index = pIndex;
    
}//end of PeakBuffer::PeakBuffer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchPeak
//
// This method must be overridden by subclasses to provide the specific
// comparison to a catch the desire type of peak, such as the highest value,
// lowest value, closest to a target value, etc.
//

public synchronized void catchPeak(Object pO)
{
    
    // This method must be overridden by subclasses.
    
}// end of PeakBuffer::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setPeak
//
// Forces peak to pValue.
//

public synchronized void setPeak(Object pO)
{
    
    // This method must be overridden by subclasses.    
    
}// end of PeakBuffer::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::reset
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public synchronized void reset()
{

    // This method must be overridden by subclasses.    
    
}// end of PeakBuffer::reset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setResetValue
//
// Sets the value to which the peak is reset when the previous peak has been
// retrieved and a new peak is to be found.
//

public synchronized void setResetValue(Object pO)
{

    // This method must be overridden by subclasses.    
    
}// end of PeakBuffer::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeak
//
// Retrieves the current value of the peak without resetting it.
//

public synchronized void getPeak(Object pO)
{

    // This method must be overridden by subclasses.
    
}// end of PeakBuffer::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//

public synchronized void getPeakAndReset(Object pO)
{

    // This method must be overridden by subclasses.
    
}// end of PeakBuffer::getPeakAndReset
//-----------------------------------------------------------------------------

}//end of class PeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

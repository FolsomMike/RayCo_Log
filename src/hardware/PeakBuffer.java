/******************************************************************************
* Title: PeakBuffer.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* NOTE: there is no clean way to create this class using Generics which don't
* require that the peak value is an object which must be recreated each time a
* peak is stored, since Integer and Double type wrappers are immutable.
* Generics cannot be used with primitives (int , double) as the parameterized
* class so that requires auto boxing/unboxing to compare and set the peak
* object. In the end this version compiles but HAS NOT been tested...the
* concept is being checked into Git and abandoned. Classes using Objects and
* casting by subclasses will be used instead even though the casting does cost
* overhead. Note that the casting is required in this Generic version anyway.
* 
* 
* -- WARNING --
* Do not use this class or its subclasses for time sensitive loops where a
* lot of data is being processed at hight speed. Generics cannot use primitives
* so every primitive value (int, double, etc.) must be autoboxed/unboxed into
* its corresponding object (Integer, Double, etc.) when methods in this class
* are called. The autoboxing/unboxing process causes overhead since an object
* is created and released each time.
*
* 
* Purpose:
*  
* This is a Generic parent class used to detect and store peak values.
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

import toolkit.MKSWrapper;

//-----------------------------------------------------------------------------
// class PeakBuffer
//

public class PeakBuffer<T extends MKSWrapper>
{

    private final int index;

    T peak;
    T peakReset;
    
//-----------------------------------------------------------------------------
// PeakBuffer::PeakBuffer (constructor)
//

public PeakBuffer(int pIndex)
{

    index = pIndex;
    
}//end of PeakBuffer::PeakBuffer (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// The peak object needs to be a persistent object during the life of the
// PeakBuffer object. Java Generics does not allow simple creation of a
// parameter type, such as:
//      peak = new T();  // not allowed!
// Rather the object must be created using "reflection" (yucky but necessary).
// This can be done by having the creating object call this init method with
// the type the PeakBuffer subclass will be used with:
//      peakBuffer = new HighPeakBuffer<>(0);
//      peakBuffer.init(Integer.class); //or whatever type will be used
//
// This workaround is described in the Oracle Java Tutorial.
//

public void init(Class<T> cls)
{

    try{peak = cls.newInstance();}
    catch(InstantiationException | IllegalAccessException e){}
    
}// end of PeakBuffer::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::catchPeak
//
// This method must be overridden by subclasses to provide the specific
// comparison to a catch the desire type of peak, such as the highest value,
// lowest value, closest to a target value, etc.
//

public synchronized void catchPeak(T pValue)
{
    
}// end of PeakBuffer::catchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::setPeak
//
// Forces peak to pValue.
//

public synchronized void setPeak(T pValue)
{

    peak = pValue;
    
}// end of PeakBuffer::setPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::reset
//
// Forces peak to the reset value, usually in preparation to find a new peak.
//

public synchronized void reset()
{

    peak = peakReset;
    
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

    
}// end of PeakBuffer::setResetValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeak
//
// Retrieves the current value of the peak without resetting it.
//

public synchronized T getPeak()
{

    return(peak);
    
}// end of PeakBuffer::getPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// PeakBuffer::getPeakAndReset
//
// Retrieves the current value of the peak and resets the peak to the reset
// value.
//

public synchronized T getPeakAndReset()
{

    T value = peak; peak = peakReset;
    
    return(value);
    
}// end of PeakBuffer::getPeakAndReset
//-----------------------------------------------------------------------------

}//end of class PeakBuffer
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: MKSWrapper.java
* Author: Mike Schoonover
* Date: 01/16/15  
* 
* Purpose:
*
* This is the parent class for subclasses which wrap primitives in an object so
* they can be passed to methods which can then modify the value so it can be
* passed back to the caller.
* 
* This class is useful for implementing efficient Generic classes since those
* classes cannot use primitives directly. The standard Java wrapper classes
* (Integer, Double, etc.) are inefficient because they are immutable and must
* be recreated each time their value is changed.
* 
* It also implements Comparable so it can be used in Generic classes which
* compare values. Generics cannot use primitives directly and if they use
* the wrapper classes (Integer, Double, etc.) then this requires time costing
* boxing/unboxing when passing primitives into the class.
* 
* The Java Integer class is immutable and does not allow the wrapped value to
* be changed after creation so a new object must be created each time the value
* is changed. This is inefficient for use in speed sensitive code.
* 
* Java does provide mutable versions such as AtomicInteger, but these are all
* synchronized and thus also have added overhead. If thread safety is needed,
* then these atomic classes should be used.
* 
* The methods to store a peak, retrieve a peak, and set a peak are all
* synchronized so they are thread safe.
*
*/

//-----------------------------------------------------------------------------

package toolkit;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MKSWrapper
//

public class MKSWrapper implements Comparable<MKSWrapper>
{
    

//-----------------------------------------------------------------------------
// MKSWrapper::compareTo
//
// Compares this object to another one.
//
// Returns -1 if this object's wrapped value is less than pX's wrapped value.
// Returns  0 if this object's wrapped value is equal to pX's wrapped value.    
// Returns  1 if this object's wrapped value is greater than pX's wrapped value.    
//
// NOTE: This method should be overridden by subclasses.
//
    
    @Override
    public int compareTo(MKSWrapper pX)
{

    return(0);
    
}//end of MKSWrapper::compareTo
//-----------------------------------------------------------------------------
    
}//end of class MKSWrapper
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: MKSInteger.java
* Author: Mike Schoonover
* Date: 01/16/15  
* 
* Purpose:
*
* This class wraps an integer in an object so it can be passed to methods which
* can then modify the value so it can be passed back to the caller.
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
// class MKSInteger
//

public class MKSInteger extends MKSWrapper
{
    
    public int x = 0;

//-----------------------------------------------------------------------------
// MKSInteger::MKSInteger (constructor)
//

public MKSInteger(int pInitialValue)
{

    x = pInitialValue;
    
}//end of MKSInteger::MKSInteger (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MKSInteger::compareTo
//
// Compares this object to another one.
//
// Returns -1 if this object's wrapped value is less than pX's wrapped value.
// Returns  0 if this object's wrapped value is equal to pX's wrapped value.    
// Returns  1 if this object's wrapped value is greater than pX's wrapped value.    
//
    

@Override
public int compareTo(Object pO)
{

    if (x < ((MKSInteger)pO).x) return(-1);
    else
    if (x > ((MKSInteger)pO).x) return(1);
    
    return(0);
    
}//end of MKSInteger::compareTo
//-----------------------------------------------------------------------------
    
}//end of class MKSInteger
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: MKSBoolean.java
* Author: Hunter Schoonover
* Date: 09/21/2017
*
* Purpose:
*
* This class wraps a boolean in an object so it can be passed to methods which
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
// class MKSBoolean
//

public class MKSBoolean extends MKSWrapper
{

    public boolean bool = false;

//-----------------------------------------------------------------------------
// MKSBoolean::MKSBoolean (constructor)
//

public MKSBoolean(boolean pInitialValue)
{

    bool = pInitialValue;

}//end of MKSBoolean::MKSBoolean (constructor)
//-----------------------------------------------------------------------------

}//end of class MKSBoolean
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

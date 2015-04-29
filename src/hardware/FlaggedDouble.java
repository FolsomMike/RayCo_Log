/******************************************************************************
* Title: FlaggedDouble.java
* Author: Mike Schoonover
* Date: 04/29/15
*
* Purpose:
*
* This class handle an double variable paired with a "dirty" flag which tracks
* if the variable has been updated since the last read.
* 
* The class is useful for setting a value in one method/thread which will
* later be applied by another method/thread.
*
* NOTE: No methods in this class are synchronized. If required, all calls to
*  these methods must be done in synchronized wrappers by the caller. 
* 
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
// class FlaggedDouble
//

public class FlaggedDouble extends FlaggedVariable
{

    private double value;
    
//-----------------------------------------------------------------------------
// FlaggedDouble::FlaggedDouble (constructor)
//

public FlaggedDouble(double pValue)
{

    value = pValue; setDirty(false);
    
}//end of FlaggedDouble::FlaggedDouble (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedDouble::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

}// end of FlaggedDouble::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedDouble::setValue
//
// Sets the value and the dirty flag.
//
// If pForceUpdate is false, the value and the dirty flag will only be modified
// if the new value is different than the old value.
//
// If pForceUpdate is true, the value and the dirty flag will always be
// modified.
//
// Returns true if the value was updated, false otherwise.
//

public boolean setValue(double pValue, boolean pForceUpdate)
{

    if (value != pValue || pForceUpdate){
        value = pValue; setDirty(true);
        return(true);
    }
    
    return(false);
    
}// end of FlaggedDouble::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedDouble::setValue
//
// Sets the value from a String input and the dirty flag.
//
// If pForceUpdate is false, the value and the dirty flag will only be modified
// if the new value is different than the old value.
//
// If pForceUpdate is true, the value and the dirty flag will always be
// modified.
//
// Returns true if the value was updated, false otherwise.
//

public boolean setValue(String pValue, boolean pForceUpdate)
{

    double dValue = Double.parseDouble(pValue);
    
    if (value != dValue || pForceUpdate){
        value = dValue; setDirty(true);
        return(true);
    }
    
    return(false);
    
}// end of FlaggedDouble::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedDouble::getValue
//
// Gets the value and clears the dirty flag.
//

public double getValue()
{

    setDirty(false); return(value);
        
}// end of FlaggedDouble::getValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedDouble::sniffValue
//
// Gets the value WITHOUT clearing the dirty flag.
//

public double sniffValue()
{

    return(value); 
        
}// end of FlaggedDouble::sniffValue
//-----------------------------------------------------------------------------

}//end of class FlaggedDouble
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

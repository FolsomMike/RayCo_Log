/******************************************************************************
* Title: FlaggedInt.java
* Author: Mike Schoonover
* Date: 04/29/15
*
* Purpose:
*
* This class handle an int variable paired with a "dirty" flag which tracks
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
// class FlaggedInt
//

public class FlaggedInt extends FlaggedVariable
{

    private int value;
    
//-----------------------------------------------------------------------------
// FlaggedInt::FlaggedInt (constructor)
//

public FlaggedInt(int pValue)
{

    value = pValue; setDirty(false);
    
}//end of FlaggedInt::FlaggedInt (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedInt::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

}// end of FlaggedInt::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedInt::setValue
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

public boolean setValue(int pValue, boolean pForceUpdate)
{

    if (value != pValue || pForceUpdate){
        value = pValue; setDirty(true);
        return(true);
    }
    
    return(false);
    
}// end of FlaggedInt::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedInt::setValue
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

    int iValue = Integer.parseInt(pValue);
    
    if (value != iValue || pForceUpdate){
        value = iValue; setDirty(true);
        return(true);
    }
    
    return(false);
    
}// end of FlaggedInt::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedInt::getValue
//
// Gets the value and clears the dirty flag.
//

public int getValue()
{

    setDirty(false); return(value);
        
}// end of FlaggedInt::getValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedInt::sniffValue
//
// Gets the value WITHOUT clearing the dirty flag.
//

public int sniffValue()
{

    return(value); 
        
}// end of FlaggedInt::sniffValue
//-----------------------------------------------------------------------------

}//end of class FlaggedInt
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: FlaggedBoolean.java
* Author: Mike Schoonover
* Date: 04/29/15
*
* Purpose:
*
* This class handle a boolean variable paired with a "dirty" flag which tracks
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
// class FlaggedBoolean
//

public class FlaggedBoolean extends FlaggedVariable
{

    private boolean value;
    
//-----------------------------------------------------------------------------
// FlaggedBoolean::FlaggedBoolean (constructor)
//

public FlaggedBoolean(boolean pValue)
{

    value = pValue; setDirty(false);
    
}//end of FlaggedBoolean::FlaggedBoolean (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedBoolean::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

}// end of FlaggedBoolean::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedBoolean::setValue
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

public boolean setValue(boolean pValue, boolean pForceUpdate)
{

    if (value != pValue || pForceUpdate){
        value = pValue; setDirty(true);
        return(true);
    }
    
    return(false);
    
}// end of FlaggedBoolean::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedBoolean::setValue
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

    boolean bValue = Boolean.parseBoolean(pValue);
    
    if (value != bValue || pForceUpdate){
        value = bValue; setDirty(true);
        return(true);
    }
    
    return(false);
    
}// end of FlaggedBoolean::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedBoolean::getValue
//
// Gets the value and clears the dirty flag.
//

public boolean getValue()
{

    setDirty(false); return(value);
        
}// end of FlaggedBoolean::getValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedBoolean::sniffValue
//
// Gets the value WITHOUT clearing the dirty flag.
//

public boolean sniffValue()
{

    return(value); 
        
}// end of FlaggedBoolean::sniffValue
//-----------------------------------------------------------------------------

}//end of class FlaggedBoolean
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

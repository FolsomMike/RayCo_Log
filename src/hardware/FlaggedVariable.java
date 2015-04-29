/******************************************************************************
* Title: FlaggedVariables.java
* Author: Mike Schoonover
* Date: 04/29/15
*
* Purpose:
*
* This class is the parent class for subclasses which handle variables which
* have a "dirty" flag which tracks if they have been updated since the last
* read.
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
// class FlaggedVariable
//

public class FlaggedVariable
{

    private boolean dirty;
    public boolean isDirty(){ return(dirty); }
    public final void setDirty(boolean pState){ dirty = pState; }

//-----------------------------------------------------------------------------
// FlaggedVariable::FlaggedVariable (constructor)
//

public FlaggedVariable()
{

    setDirty(false);
    
}//end of FlaggedVariable::FlaggedVariable (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FlaggedVariable::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

}// end of FlaggedVariable::init
//-----------------------------------------------------------------------------

}//end of class FlaggedVariable
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

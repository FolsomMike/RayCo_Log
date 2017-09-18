/******************************************************************************
* Title: BoardChannelParameters.java
* Author: Hunter Schoonover
* Date: 09/18/17
*
* Purpose:
*
* This class stores parameters for a board channel. It exists so that multiple
* instances of the class Channel, which can all refer to the same board channel
* all have access to the same parameters.
*
* Values such as gain, offset, on/off status, etc. belong in this class.
*
* One instance of this class per Board Channel, NOT per software Channel.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
// class BoardChannelParameters
//

public class BoardChannelParameters
{

    private boolean hdwParamsDirty;
    public boolean getHdwParamsDirty() { return hdwParamsDirty; }
    public void setHdwParamsDirty(boolean pDirty) { hdwParamsDirty=pDirty; }

    FlaggedBoolean onOff = new FlaggedBoolean(false);
    FlaggedInt gain = new FlaggedInt(0);
    FlaggedInt offset = new FlaggedInt(0);

//-----------------------------------------------------------------------------
// BoardChannelParameters::BoardChannelParameters (constructor)
//

public BoardChannelParameters()
{

}//end of BoardChannelParameters::BoardChannelParameters (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

}// end of BoardChannelParameters::init
//-----------------------------------------------------------------------------

///-----------------------------------------------------------------------------
// BoardChannelParameters::setGain
//
// Sets the gain value from a String input.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// Returns true if the value was updated, false otherwise.
//

public boolean setGain(String pValue, boolean pForceUpdate)
{

    boolean result = gain.setValue(pValue, pForceUpdate);

    if(result){ setHdwParamsDirty(true); }

    return(result);

}// end of BoardChannelParameters::setGain
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::setOffset
//
// Sets the offset value from a String input.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// Returns true if the value was updated, false otherwise.
//

public boolean setOffset(String pValue, boolean pForceUpdate)
{

    boolean result = offset.setValue(pValue, pForceUpdate);

    if(result){ setHdwParamsDirty(true); }

    return(result);

}// end of BoardChannelParameters::setOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::setOnOff
//
// Sets the OnOff value from a String input.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// Returns true if the value was updated, false otherwise.
//

public boolean setOnOff(String pValue, boolean pForceUpdate)
{

    boolean result = onOff.setValue(pValue, pForceUpdate);

    if(result){ setHdwParamsDirty(true); }

    return(result);

}// end of BoardChannelParameters::setOnOff
//-----------------------------------------------------------------------------

}//end of class BoardChannelParameters
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

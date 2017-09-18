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
    synchronized public boolean getHdwParamsDirty() { return hdwParamsDirty; }
    synchronized  public void setHdwParamsDirty(boolean pDirty) { hdwParamsDirty=pDirty; }

    private final FlaggedBoolean onOff = new FlaggedBoolean(false);
    synchronized public boolean isOnOffDirty() { return onOff.isDirty(); }

    private final FlaggedInt gain = new FlaggedInt(0);
    synchronized public boolean isGainDirty() { return gain.isDirty(); }

    private final FlaggedInt offset = new FlaggedInt(0);
    synchronized public boolean isOffsetDirty() { return offset.isDirty(); }

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

//-----------------------------------------------------------------------------
// BoardChannelParameters::setParameters
//
// Sets the gain value from a String input.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// Returns true if any of the values were updated, false otherwise.
//

synchronized public boolean setParameters(String pOnOff, String pGain,
                                            String pOffset,
                                            boolean pForceUpdate)
{

    boolean result = false;

    if (onOff.setValue(pOnOff, pForceUpdate)) { result = true; }
    if (gain.setValue(pGain, pForceUpdate)) { result = true; }
    if (offset.setValue(pOffset, pForceUpdate)) { result = true; }

    if (result) { setHdwParamsDirty(true); }

    return result;

}// end of BoardChannelParameters::setParameters
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::setGain
//
// Sets the gain value from a String input.
//
// If pForceUpdate is true, the value will always be updated and the dirty flag
// set true.
//
// Returns true if the value was updated, false otherwise.
//

synchronized public boolean setGain(String pValue, boolean pForceUpdate)
{

    boolean result = gain.setValue(pValue, pForceUpdate);

    if(result){ setHdwParamsDirty(true); }

    return(result);

}// end of BoardChannelParameters::setGain
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::getGain
//
// Returns the gain value. If pSniff is true, the value is retrieved WITHOUT
// clearing the dirty flag.
//

synchronized public int getGain(boolean pSniff)
{

    return pSniff?gain.sniffValue():gain.getValue();

}// end of BoardChannelParameters::getGain
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

synchronized public boolean setOffset(String pValue, boolean pForceUpdate)
{

    boolean result = offset.setValue(pValue, pForceUpdate);

    if(result){ setHdwParamsDirty(true); }

    return(result);

}// end of BoardChannelParameters::setOffset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::getOffset
//
// Returns the offset value. If pSniff is true, the value is retrieved WITHOUT
// clearing the dirty flag.
//

synchronized public int getOffset(boolean pSniff)
{

    return pSniff?offset.sniffValue():offset.getValue();

}// end of BoardChannelParameters::getOffset
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

synchronized public boolean setOnOff(String pValue, boolean pForceUpdate)
{

    boolean result = onOff.setValue(pValue, pForceUpdate);

    if(result){ setHdwParamsDirty(true); }

    return(result);

}// end of BoardChannelParameters::setOnOff
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// BoardChannelParameters::getOnOff
//
// Returns the on/off value. If pSniff is true, the value is retrieved WITHOUT
// clearing the dirty flag.
//

synchronized public boolean getOnOff(boolean pSniff)
{

    return pSniff?onOff.sniffValue():onOff.getValue();

}// end of BoardChannelParameters::getOnOff
//-----------------------------------------------------------------------------

}//end of class BoardChannelParameters
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

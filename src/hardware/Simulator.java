/******************************************************************************
* Title: Simulator.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class is the parent class for subclasses which provide simulated data.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
// class Simulator
//

public class Simulator
{

    private final int index;
    private final IniFile configFile;    
    private final SharedSettings sharedSettings;

    
//-----------------------------------------------------------------------------
// Simulator::Simulator (constructor)
//

public Simulator(int pIndex, SharedSettings pSharedSettings,
                                                        IniFile pConfigFile)
{

    index = pIndex; sharedSettings = pSharedSettings;
    configFile = pConfigFile;
    
}//end of Simulator::Simulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{


}// end of Simulator::init
//-----------------------------------------------------------------------------

}//end of class Simulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: SimulatorTransverse.java
* Author: Mike Schoonover
* Date: 02/20/15
*
* Purpose:
*
* This class provides simulation data for the EMI Transverse system.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.IniFile;
import model.SharedSettings;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SimulatorTransverse
//

public class SimulatorTransverse extends Simulator
{
    
//-----------------------------------------------------------------------------
// SimulatorTransverse::SimulatorTransverse (constructor)
//
    
public SimulatorTransverse(int pSimulatorNum, SharedSettings pSharedSettings,
                                                        IniFile pConfigFile)
{

    super(pSimulatorNum);
    
}//end of SimulatorTransverse::SimulatorTransverse (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

}// end of SimulatorTransverse::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::getRunPacket
//
// Returns a run-time packet of simulated data.
//
// The data range is 0 ~ 1,023 with zero volts at approximately 511.
//

@Override
public void getRunPacket(byte[] pPacket)
{

    int index = 0;
    
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+1
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-1
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+2
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-2
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+3
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-3
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+4
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-4
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+5
    index += 2;    
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-5
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+6
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-6
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+7
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-7
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal()); //+8
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal()); //-8
    
}// end of SimulatorTransverse::getRunPacket
//-----------------------------------------------------------------------------

}//end of class SimulatorTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

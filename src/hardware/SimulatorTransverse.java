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

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SimulatorTransverse
//

public class SimulatorTransverse extends Simulator
{
    
//-----------------------------------------------------------------------------
// SimulatorTransverse::SimulatorTransverse (constructor)
//
    
public SimulatorTransverse(int pSimulatorNum)
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
    
    addUnsignedShortToPacket(pPacket, index, 561 /*simulatePositiveSignal()*/); //+1
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 461/*simulateNegativeSignal()*/); //-1
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+2
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-2
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+3
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-3
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+4
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-4
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+5
    index += 2;    
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-5
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+6
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-6
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+7
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-7
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulatePositiveSignal()*/); //+8
    index += 2;
    addUnsignedShortToPacket(pPacket, index, 511/*simulateNegativeSignal()*/); //-8
    
}// end of SimulatorTransverse::getRunPacket
//-----------------------------------------------------------------------------

}//end of class SimulatorTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: SimulatorLongitudinal.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class provides simulation data for the EMI Longitudinal system.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SimulatorLongitudinal
//

public class SimulatorLongitudinal extends Simulator
{
    
//-----------------------------------------------------------------------------
// SimulatorLongitudinal::SimulatorLongitudinal (constructor)
//
    
public SimulatorLongitudinal(int pSimulatorNum)
{

    super(pSimulatorNum);
    
}//end of SimulatorLongitudinal::SimulatorLongitudinal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorLongitudinal::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    numClockPositions = 24;
    
    spikeOdds = 100;
    
}// end of SimulatorLongitudinal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorLongitudinal::getRunPacket
//
// Returns a run-time packet of simulated data.
//
// The data range is 0 ~ 1,023 with zero volts at approximately 511.
//

@Override
public void getRunPacket(byte[] pPacket)
{

    //getRunPacket2(pPacket); //debug mks remove this
    
    int index = 0;
    
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal());
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal());
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal());
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateNegativeSignal());
    index += 2;
    
    //add map data
    for(int i=0; i<numClockPositions; i++){
        addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal());
        index += 2;
    }
    
}// end of SimulatorLongitudinal::getRunPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorLongitudinal::getRunPacket2
//
// debug mks -- remove this method

int sawtooth = 0;

public void getRunPacket2(byte[] pPacket)
{

    int index = 0;
    
    int posSawtooth = AD_ZERO_OFFSET + sawtooth;
    int negSawtooth = AD_ZERO_OFFSET - sawtooth;
            
    addUnsignedShortToPacket(pPacket, index, posSawtooth); // +shoe1
    index += 2;
    addUnsignedShortToPacket(pPacket, index, negSawtooth); // -shoe1
    index += 2;
    addUnsignedShortToPacket(pPacket, index, posSawtooth + 5); // +shoe2
    index += 2;
    addUnsignedShortToPacket(pPacket, index, negSawtooth + 5); // -shoe2

    sawtooth++;
    if(sawtooth > 99) { sawtooth = 0; }
    
}// end of SimulatorLongitudinal::getRunPacket2
//-----------------------------------------------------------------------------


}//end of class SimulatorLongitudinal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

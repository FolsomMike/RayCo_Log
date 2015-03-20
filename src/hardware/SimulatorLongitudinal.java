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
        
    addMapData(pPacket, index);
    
}// end of SimulatorLongitudinal::getRunPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorLongitudinal::addMapData
//
// Adds simulated map data to pPacket starting at position pIndex.
//
// The data range is 0 ~ 1,023 with zero volts at approximately 511.
//

public void addMapData(byte[] pPacket, int pIndex)
{

    int count = 0;
    
    //add map data
    for(int i=0; i<numClockPositions; i++){
        addUnsignedShortToPacket(pPacket, pIndex, AD_ZERO_OFFSET + count++);
        pIndex += 2;
    }


/*    
    
    int[] dataRow = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                       13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
    
    
    //debug mks -- remove this after added to simulation class
    for (int i=0; i<dataRow.length; i++){

        dataRow[i] = 1 + (int)(2 * Math.random());

        if((int)(100 * Math.random()) < 1){
            dataRow[i] = 1 + (int)(25 * Math.random());
        }

        //simulate the weldline
        if(i == 4){
            dataRow[i] = 5 + (int)(3 * Math.random());
        }
    }
    //debug mks end
  
*/        
    
    
}// end of SimulatorLongitudinal::addMapData
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

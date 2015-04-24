/******************************************************************************
* Title: SimulatorTransverse.java
* Author: Mike Schoonover
* Date: 04/22/15
*
* Purpose:
*
* This class provides simulation data for the EMI Transverse system.
* 
* This class simulates a TCP/IP connection between the host and the remote
* device.
*
* This is a subclass of Simulator which subclasses Socket and can be substituted
* for an instance of the Socket class when simulated data is needed.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import java.net.InetAddress;
import java.net.SocketException;


//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SimulatorTransverse
//

public class SimulatorTransverse extends Simulator
{

    
//-----------------------------------------------------------------------------
// SimulatorTransverse::SimulatorTransverse (constructor)
//
    
public SimulatorTransverse(InetAddress pIPAddress, int pPort,
     String pTitle, String pSimulationDataSourceFilePath) throws SocketException
{

    super(pIPAddress, pPort, pTitle, pSimulationDataSourceFilePath);
    
}//end of SimulatorTransverse::SimulatorTransverse (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Parameter pBoardNumber is used to find info for the simulated board in a
// config file.
//

@Override
public void init(int pBoardNumber)
{

    super.init(pBoardNumber);

    numClockPositions = 24;    
    
    spikeOdds = 20;
        
}// end of SimulatorTransverse::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::handlePacket
//
// Performs the processing and returns data appropriate for the packet
// identifier/command byte passed in via pCommand.
//

@Override
public void handlePacket(byte pCommand)
{

    super.handlePacket(pCommand);
    
}//end of SimulatorTransverse::handlePacket
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
    index += 2;
    
    index = addMapData(pPacket, index);
    
}// end of SimulatorTransverse::getRunPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::addMapData
//
// Adds simulated map data to pPacket starting at position pIndex.
//
// The data range is 0 ~ 1,023 with zero volts at approximately 511.
//
// Returns the updated value of pIndex, pointing to the next empty position in
// pPacket.
//
// Note: Currently, any random data generated must be gated by a random switch
// so that the data is only generated infrequently...the simulation function
// gets called so frequently between collection that the highest peak will
// always be generated in that time frame so the data ends up being a straight
// line at the max possible random value. After reducing the number of times
// the data is generated, such as with a timer, to mimic the actual devices,
// the gating can be removed.
//

public int addMapData(byte[] pPacket, int pIndex)
{

/*  
    //use this code to create a wedge shape on the 3D map

    int count = 0;
    
    //add map data
    for(int i=0; i<numClockPositions; i++){
        addUnsignedShortToPacket(pPacket, pIndex, AD_ZERO_OFFSET + count++);
        pIndex += 2;
    }
*/
        
    for (int i=0; i<numClockPositions; i++){

        int simData = 1;
        
        if((int)(100 * Math.random()) < 7){        
            simData = 1 + (int)(2 * Math.random());
        }
        
        if((int)(5000 * Math.random()) < 1){
            simData = 1 + (int)(25 * Math.random());
        }
        
        addUnsignedShortToPacket(pPacket, pIndex, AD_ZERO_OFFSET + simData);
        pIndex += 2;
    }

    return(pIndex);
    
}// end of SimulatorTransverse::addMapData
//-----------------------------------------------------------------------------

}//end of class SimulatorTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

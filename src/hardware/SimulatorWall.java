/******************************************************************************
* Title: SimulatorWall.java
* Author: Mike Schoonover
* Date: 03/16/15
*
* Purpose:
*
* This class provides simulation data for the EMI Wall system.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import java.net.InetAddress;
import java.net.SocketException;

//-----------------------------------------------------------------------------
// class SimulatorWall
//

public class SimulatorWall extends Simulator
{
        
    int avgWallSpikeLength = 0;
    int pulseWallSpikeLength = 0;
    int intCoilSpikeLength = 0;
    
//-----------------------------------------------------------------------------
// SimulatorWall::SimulatorWall (constructor)
//
    
public SimulatorWall(InetAddress pIPAddress, int pPort,
     String pTitle, String pSimulationDataSourceFilePath) throws SocketException
{

    super(pIPAddress, pPort, pTitle, pSimulationDataSourceFilePath);
    
}//end of SimulatorWall::SimulatorWall (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorWall::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init(int pBoardNumber)
{

    super.init(pBoardNumber);

    numClockPositions = 0;
    
    spikeOdds = 100;
    
}// end of SimulatorWall::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorWall::handlePacket
//
// Performs the processing and returns data appropriate for the packet
// identifier/command byte passed in via pCommand.
//

@Override
public void handlePacket(byte pCommand)
{

    super.handlePacket(pCommand);
    
}//end of SimulatorWall::handlePacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorWall::getRunPacket
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
    
    //rabbit rundata pkt count
    pPacket[index++] = (byte)(rbtRunDataPktCount++ &0xff);
    
    //pic rundata pkt count
    pPacket[index++] = (byte)(picRunDataPktCount++ &0xff);
    
    addUnsignedShortToPacket(pPacket, index, simulateAverageWall());
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulatePulseWall());
    index += 2;
    addUnsignedShortToPacket(pPacket, index, simulateIntelligentCoil());
    index += 2;
      
    //add map data
    for(int i=0; i<numClockPositions; i++){
        addUnsignedShortToPacket(pPacket, index, simulatePositiveSignal());
        index += 2;
    }
    
    
}// end of SimulatorWall::getRunPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorWall::simulateAverageWall
//
// Simulates an average wall trace.
//
// More than one of the simulate methods shares lastSpikeValue so each can
// mess with other if one happens to spike when another is still in a spike.
// Won't happen very often and not really a problem.
//

int simulateAverageWall()
{

    int value = AD_ZERO_OFFSET;
    
    value += 45;
    
    value += (int)(WALL_SIM_NOISE * Math.random());
    
    if ((int)(WALL_SPIKE_ODDS_RANGE*Math.random()) < spikeOdds){
        lastSpikeValue = (int)(100 * Math.random());
        avgWallSpikeLength = 4 + (int)(10 * Math.random());
        value -= lastSpikeValue;
    }else{
        if (avgWallSpikeLength > 0){
            value -= lastSpikeValue; avgWallSpikeLength--;
        }
    }

    if (value > AD_MAX_VALUE) { value = AD_MAX_VALUE; }
    
    return(value);

}//end of SimulatorWall::simulateAverageWall
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorWall::simulatePulseWall
//
// Simulates a Pulse Wall trace.
//
// More than one of the simulate methods shares lastSpikeValue so each can
// mess with other if one happens to spike when another is still in a spike.
// Won't happen very often and not really a problem.
//

int simulatePulseWall()
{

    int value = AD_ZERO_OFFSET;
    
    value += 50;
    
    value += (int)(WALL_SIM_NOISE * Math.random());
    
    if ((int)(WALL_SPIKE_ODDS_RANGE*Math.random()) < spikeOdds){
        lastSpikeValue = (int)(100 * Math.random());
        pulseWallSpikeLength = 4 + (int)(10 * Math.random());
        value -= lastSpikeValue;
    }else{
        if (pulseWallSpikeLength > 0){
            value -= lastSpikeValue; pulseWallSpikeLength--;
        }
    }

    if (value > AD_MAX_VALUE) { value = AD_MAX_VALUE; }
    
    return(value);

}//end of SimulatorWall::simulatePulseWall
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorWall::simulateIntelligentCoil
//
// Simulates an Intelligent Coil wall trace.
//
// More than one of the simulate methods shares lastSpikeValue so each can
// mess with other if one happens to spike when another is still in a spike.
// Won't happen very often and not really a problem.
//

int simulateIntelligentCoil()
{

    int value = AD_ZERO_OFFSET;
    
    value += 55;
    
    value += (int)(WALL_SIM_NOISE * Math.random());
    
    if ((int)(WALL_SPIKE_ODDS_RANGE*Math.random()) < spikeOdds){
        lastSpikeValue = (int)(100 * Math.random());
        intCoilSpikeLength = 4 + (int)(10 * Math.random());
        value -= lastSpikeValue;
    }else{
        if (intCoilSpikeLength > 0){
            value -= lastSpikeValue; intCoilSpikeLength--;
        }
    }

    if (value > AD_MAX_VALUE) { value = AD_MAX_VALUE; }
    
    return(value);

}//end of SimulatorWall::simulateIntelligentCoil
//-----------------------------------------------------------------------------


}//end of class SimulatorWall
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

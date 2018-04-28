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

    private final int ADzeroOffset = 0;
    private final int ADnominalWall;
    
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
    
    //Nominal wall on the -12V to 12V range is 1V, before scaling down to 0-3.3V
    //[1V / (12V /3.3V)] * (255 byte max / 3.3V)
    ADnominalWall = (int)((1/(12/3.3))*(255/3.3));
    
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
// SimulatorWall::handleGetRunData
//
// Handles GET_RUN_DATA_CMD packet requests. Sends appropriate packet via
// the socket.
//
// This method should be overridden by child classes to provide appropriate
// processing.
//

@Override
public int handleGetRunData()
{
    
    int numBytesInPkt = 2;  //includes the checksum byte

    int result = readBytesAndVerify(
                       inBuffer, numBytesInPkt, MultiIODevice.GET_RUN_DATA_CMD);
    if (result != numBytesInPkt){ return(result); }
    
    if (activeChannels==null) { return result; }
    
    //initialize packeet
    byte packet[] = new byte[211];
    for (byte b : packet) { b = 0; }
    
    int index = 0;
    
    //rabbit rundata pkt count
    packet[index++] = (byte)(rbtRunDataPktCount++ &0xff);
    //pic rundata pkt count
    packet[index++] = (byte)(picRunDataPktCount++ &0xff);
    
    //Average Wall
    int avgWall = simulateAverageWall();
    packet[index++] = (byte)((avgWall >> 8) & 0xff);
    packet[index++] = (byte)(avgWall & 0xff);

    //Pulse Wall
    int pulseWall = simulatePulseWall();
    packet[index++] = (byte)((pulseWall >> 8) & 0xff);
    packet[index++] = (byte)(pulseWall & 0xff);
    
    //Intelligent Wall
    int intWall = simulateIntelligentCoil();
    packet[index++] = (byte)((intWall >> 8) & 0xff);
    packet[index++] = (byte)(intWall & 0xff);
    
    //send run packet -- sendPacket appends Rabbit's checksum
    sendPacket(MultiIODevice.GET_RUN_DATA_CMD, packet);

    return(result);
    
    
}// end of SimulatorWall::handleGetRunData
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

    int value = ADnominalWall;
    //DEBUG HSS//
    //DEBUG HSSvalue += (int)(WALL_SIM_NOISE * Math.random());
    
    /*if ((int)(WALL_SPIKE_ODDS_RANGE*Math.random()) < spikeOdds){
        lastSpikeValue = (int)(100 * Math.random());
        avgWallSpikeLength = 4 + (int)(10 * Math.random());
        value -= lastSpikeValue;
    }else{
        if (avgWallSpikeLength > 0){
            value -= lastSpikeValue; avgWallSpikeLength--;
        }
    }*/

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

    int value = ADnominalWall;
    
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

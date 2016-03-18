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

    numClockPositions = 48;    
    
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
// SimulatorTransverse::handleGetRunData
//
// Handles GET_RUN_DATA_CMD packet requests. Sends appropriate packet via
// the socket.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//
// The data range is 0 ~ 1,023 for the data peaks.
// The data range is 0 ~ 255 for the map.
//

@Override
public int handleGetRunData()
{
 
    int numBytesInPkt = 2;  //includes the checksum byte
    
    int result = readBytesAndVerify(
                       inBuffer, numBytesInPkt, Device.GET_RUN_DATA_CMD);
    if (result != numBytesInPkt){ return(result); }
    
    int posSignals[] = new int[8];
    int negSignals[] = new int[8];
    
    for(int i=0; i<posSignals.length; i++){
        posSignals[i] = simulatePositiveSignal();
        negSignals[i] = simulateNegativeSignal();        
    }
    
    simulateMapData(dataBuffer, 0);    
        
    int p = 0, n = 0, m = 0;
    
    //send run packet -- sendPacket appends Rabbit's checksum
    
    sendPacket(Device.GET_RUN_DATA_CMD,
            
        (byte)(rbtRunDataPktCount++ &0xff), //rabbit rundata pkt count
        (byte)(picRunDataPktCount++ &0xff), //pic rundata pkt count
            
        (byte)((posSignals[p] >> 8) & 0xff), //+1
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-1
        (byte)(negSignals[n++] & 0xff),
        
        (byte)((posSignals[p] >> 8) & 0xff), //+2
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-2
        (byte)(negSignals[n++] & 0xff),

        (byte)((posSignals[p] >> 8) & 0xff), //+3
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-3
        (byte)(negSignals[n++] & 0xff),
        
        (byte)((posSignals[p] >> 8) & 0xff), //+4
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-4
        (byte)(negSignals[n++] & 0xff),
        
        (byte)((posSignals[p] >> 8) & 0xff), //+5
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-5
        (byte)(negSignals[n++] & 0xff),
        
        (byte)((posSignals[p] >> 8) & 0xff), //+6
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-6
        (byte)(negSignals[n++] & 0xff),

        (byte)((posSignals[p] >> 8) & 0xff), //+7
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-7
        (byte)(negSignals[n++] & 0xff),
        
        (byte)((posSignals[p] >> 8) & 0xff), //+8
        (byte)(posSignals[p++] & 0xff),
    
        (byte)((negSignals[n] >> 8) & 0xff), //-8
        (byte)(negSignals[n++] & 0xff),
        
        //Clock map
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],    
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
        (byte)dataBuffer[m++], (byte)dataBuffer[m++], (byte)dataBuffer[m++],
            
        //Snapshot buffer -- //WIP HSS// -- use better values
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00
        );
    
    return(result);
    
}//end of SimulatorTransverse::handleGetRunData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::simulateMapData
//
// Adds simulated map data to pPacket starting at position pIndex.
//
// The data range is 0 ~ 127 with zero volts at value of 0.
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

public int simulateMapData(byte[] pPacket, int pIndex)
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
        
        addByteToPacket(pPacket, pIndex, simData);
        pIndex++;
    }

    return(pIndex);
    
}// end of SimulatorTransverse::simulateMapData
//-----------------------------------------------------------------------------

}//end of class SimulatorTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

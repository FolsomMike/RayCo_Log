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

    int count=0;

    protected int posTest = 128; protected int negTest = 127; //DEBUG HSS// remove later

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

    //transverse devices have a max of 8 pos channels & 8 neg channels
    //set each channel to the default values. they will be changed to
    //simulated values if their channels are active
    int posSignals[] = new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    int negSignals[] = new int[]{0x7f,0x7f,0x7f,0x7f,0x7f,0x7f,0x7f,0x7f};

    int clockMap[] = new int[] {0,0,0,0,0,0,0,0,0,0, //clock map makes up 48
                                0,0,0,0,0,0,0,0,0,0, //bytes in run data pkt
                                0,0,0,0,0,0,0,0,0,0, //only clock positions
                                0,0,0,0,0,0,0,0,0,0, //marked as used will be
                                0,0,0,0,0,0,0,0};    //changed

    //iterate through all of the active channels and simulate values -- unactive
    //channels will not be simulated
    for(int i=0; i<activeChannels.length; i+=2) {
        posSignals[i] = simulatePositiveSignal();
        negSignals[i] = simulateNegativeSignal();
        posSignals[i] = 130; negSignals[i] = 125;
        if (count++==500) { posSignals[i] = 134; negSignals[i] = 50; count=0; }//DEBUG HSS//
        //DEBUG HSS// uncomment this line to test posSignals[i] = posTest; negSignals[i] = negTest;
        //if (count++==100) { posTest++; negTest++; count=0;}//DEBUG HSS//
        if (posTest>=255) { posTest = 128; } if (negTest<=0) { negTest = 127; }//DEBUG HSS//
        clockMap[activeChannels[i].getClockPosition()]
                = simulateMapData(posSignals[i], negSignals[i]);
    }

    //DEBUG HSS// remove
    /*if (clockMap[47]>20) {
        System.out.println("Pos signal: " + posSignals[0] + " --- "
                            + "Neg signal: " + negSignals[0] + " --- "
                            + "Clock map: " + clockMap[47]);
        System.out.println("----------------------------------------------");
    }*/
    //DEBUG HSS//

    int snapshot[] = simulateSnapshot();

    //send run packet -- sendPacket appends Rabbit's checksum
    int p = 0, n = 0, m = 0, s=0;
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
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),
        (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff), (byte)(clockMap[m++]& 0xff),

        //Address of last A/D value stored in Snapshot buffer
        (byte)0x00,

        //Snapshot buffer
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),

        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),

        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),

        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),

        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),

        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),

        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff),
        (byte)(snapshot[s++]& 0xff), (byte)(snapshot[s++]& 0xff)
        );

    return(result);

}//end of SimulatorTransverse::handleGetRunData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::simulateMapData
//
// Compares the two signals passed in and returns the greater of the two.
//

private int simulateMapData(int pPosSignal, int pNegSignal)
{

    pPosSignal = Math.abs(pPosSignal -= AD_ZERO_OFFSET);
    pNegSignal = Math.abs(pNegSignal -= AD_ZERO_OFFSET);

    return pPosSignal>pNegSignal?pPosSignal:pNegSignal;

}// end of SimulatorTransverse::simulateMapData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::simulateSnapshot
//
// Creates a simulated data stream representing a snapshot of a peak.
//

private int[] simulateSnapshot()
{

    int data[] = new int[128];

    for(int i=0; i<data.length; i++){
        data[i] = (int)(5 * Math.random());
    }

    int spikeLoc = (int)(40 + 20 * Math.random());

    data[spikeLoc-2] = 20 + (int)(5 * Math.random());
    data[spikeLoc] = - 20 - (int)(5 * Math.random());
    data[spikeLoc+2] = 20 + (int)(5 * Math.random());

    //DEBUG HSS//
    //int data[] = new int[128]; for(int i=0; i<data.length; i++){ data[i] = 0; }
    //data[10] = 10; data[11] = 10; data[12] = 10; data[13] = 10;
    //data[90] = 150; data[91] = 150; data[92] = 150; data[93] = 150;
    //data[120] = -10; data[121] = -10; data[122] = -10; data[123] = -10;
    //DEBUG HSS// end

    return(data);

}// end of SimulatorTransverse::simulateSnapshot
//-----------------------------------------------------------------------------

}//end of class SimulatorTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

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
    
    //control vars
    boolean onPipeFlag = false;
    boolean inspectMode = false;
    //DEBUG HSS//int simulationMode = MessageLink.STOP;
    int encoder1 = 0, encoder2 = 0;
    int encoder1DeltaTrigger = 1000, encoder2DeltaTrigger = 1000;
    int inspectPacketCount = 0;

    byte controlFlags = 0, portE = 0;

    int positionTrack; // this is the number of packets sent, not the encoder
                       // value

    public static int DELAY_BETWEEN_INSPECT_PACKETS = 5;
    int delayBetweenPackets = DELAY_BETWEEN_INSPECT_PACKETS;

    //This mimics the 7-5/8 IRNDT test joint used at Tubo Belle Chasse
    //Number of encoder counts (using leading eye for both ends): 90107
    //The PLC actually gives pipe-on when the lead eye hits the pipe and
    //pipe-off when the trailing eye leaves the pipe:
    // eye to eye distance is 53.4375", or 17,653 encoder counts.
    //Number of encoder counts for simulated joint: 90107 + 17,653 = 107,760
    // (this assumes starting with lead eye and ending with trail eye)
    //Number of counts per packet: 83
    //Number of packets for complete simulated joint: 107,760 / 83 = 1298

    //number of packets to skip before simulating lead eye reaching pipe
    //this simulates the head moving from the off-pipe position to on-pipe
    public final static int START_DELAY_IN_PACKETS = 10;

    //number of packets for length of tube -- take into account the start delay
    //packets as inspection does not occur during that time
    public static int LENGTH_OF_JOINT_IN_PACKETS =
                                                1400 + START_DELAY_IN_PACKETS;

    byte enc1AInput = 0, enc1BInput = 1, enc2AInput = 1, enc2BInput = 0;
    byte padrUnused1 = 0, padrUnused2 = 0, padrUnused3 = 0, padrUnused4 = 0;
    byte padrInspect = 0, pedrTDC = 0;
    byte inspectionStatus = 0;
    short rpm = 123, rpmVariance = 2;
    int enc1Count = 0, enc2Count = 0;
    short enc1CountsPerSec = 0, enc2CountsPerSec = 0;
    int monitorSimRateCounter = 0;

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

    spikeOdds = 50;

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
                       inBuffer, numBytesInPkt, MultiIODevice.GET_RUN_DATA_CMD);
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
    int snap=127; int abs=0; boolean channelsOn = false;
    for(int i=0; i<activeChannels.length/2; i++) {

        //made it through once, so at least one channel is on
        channelsOn = true;

        //simulate pos/neg signals
        posSignals[i] = simulatePositiveSignal();
        negSignals[i] = simulateNegativeSignal();

        //determine greatest absolute value and which signal used for snapshot
        //snapshot uses the signal whose abs value is greatest
        int posAbs = Math.abs(posSignals[i] - AD_ZERO_OFFSET);
        int negAbs = Math.abs(negSignals[i] - AD_ZERO_OFFSET);
        if (posAbs>=negAbs&&posAbs>abs) {
            snap = posSignals[i]; abs = posAbs;
        }
        else if (negAbs>posAbs&&negAbs>abs) {
            snap = negSignals[i]; abs = negAbs;
        }

        //simulate clock map (greatest absolute value of pos/neg signals)
        clockMap[activeChannels[i].getClockPosition()] = abs;

    }

    int snapshot[] = simulateSnapshot(snap, channelsOn);

    //send run packet -- sendPacket appends Rabbit's checksum
    int p = 0, n = 0, m = 0, s=0;
    sendPacket(MultiIODevice.GET_RUN_DATA_CMD,

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
// SimulatorTransverse::handleGetInspectPacket
//
// Handles GET_INSPECT_PACKET_CMD packet requests. Sends appropriate packet via
// the socket.
//

@Override
public int handleGetInspectPacket()
{
    
    int numBytesInPkt = 2;  //includes the checksum byte

    int result = readBytesAndVerify(
                       inBuffer, numBytesInPkt, MultiIODevice.GET_INSPECT_PACKET_CMD);
    if (result != numBytesInPkt){ return(result); }
    
    simulateInspection();
    
    return result;

}//end of SimulatorTransverse::handleGetInspectPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlSimulator::handleSetEncodersDeltaTrigger
//
// Handles GET_INSPECT_PACKET_CMD packet requests. Sends appropriate packet via
// the socket.
//
// Tells the Control board how many encoder counts to wait before sending
// an encoder value update.  The trigger value for each encoder is sent.
//
// Normally, this value will be set to something reasonable like .25 to 1.0
// inch of travel of the piece being inspected. Should be no larger than the
// distance represented by a single pixel.
//

@Override
public int handleSetEncodersDeltaTrigger()
{
    
    int numBytesInPkt = 5;  //includes the checksum byte

    int result = readBytesAndVerify(inBuffer, numBytesInPkt, 
                                MultiIODevice.SET_ENCODERS_DELTA_TRIGGER_CMD);
    if (result != numBytesInPkt){ return result; }

    encoder1DeltaTrigger =
                   (int)((inBuffer[0]<<8) & 0xff00) + (int)(inBuffer[1] & 0xff);

    encoder2DeltaTrigger =
                   (int)((inBuffer[2]<<8) & 0xff00) + (int)(inBuffer[3] & 0xff);

    return result;

}//end of ControlSimulator::handleSetEncodersDeltaTrigger
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlSimulator::handleStartInspect
//
// Handles START_INSPECT_CMD packet commands from host.
//

@Override
public int handleStartInspect()
{
    
    int numBytesInPkt = 2; //includes the checksum byte

    int result = readBytesAndVerify(inBuffer, numBytesInPkt, 
                                        MultiIODevice.START_INSPECT_CMD);
    if (result != numBytesInPkt){ return(result); }
    
    inspectMode = true;
    
    sendACK();

    return result;

}//end of ControlSimulator::handleStartInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlSimulator::handleStopInspect
//
// Handles STOP_INSPECT_CMD packet commands from host.
//

@Override
public int handleStopInspect()
{
    
    int numBytesInPkt = 2; //includes the checksum byte

    int result = readBytesAndVerify(inBuffer, numBytesInPkt, 
                                        MultiIODevice.STOP_INSPECT_CMD);
    if (result != numBytesInPkt){ return(result); }
    
    inspectMode = false;
    
    sendACK();

    return result;

}//end of ControlSimulator::handleStopInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlSimulator::resetAllInspectionValues
//
// Resets all variables in preparation to simulate a new piece.
//

private void resetAllInspectionValues()
{

    positionTrack = 0;
    onPipeFlag = false;
    encoder1 = 0; encoder2 = 0;
    inspectPacketCount = 0;
    delayBetweenPackets = DELAY_BETWEEN_INSPECT_PACKETS;

}//end of ControlSimulator::resetAllInspectionValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlSimulator::simulateInspection
//
// Simulates signals expected by the host in inspect mode.
//

@Override
public void simulateInspection()
{

    //do nothing if not in inspect mode
    if (!inspectMode) { return; }
    
    //do nothing if in STOP mode
    //DEBUG HSS//if (simulationMode == MessageLink.STOP) {return;}

    //delay between sending inspect packets to the host
    if (delayBetweenPackets-- != 0) {return;}
    //restart time for next packet send
    delayBetweenPackets = DELAY_BETWEEN_INSPECT_PACKETS;

    inspectPacketCount++; //count packets sent to host

    //track distance moved by number of packets sent
    /*if (simulationMode == MessageLink.FORWARD){
        positionTrack++;
    }
    else if (simulationMode == MessageLink.REVERSE){
        positionTrack--;
    }*/
    //DEBUG HSS// //WIP HSS// assume in forward mode for now
    positionTrack++;

    //the position track will run negative if inspection is occurring in the
    //reverse direction -- use absolute value to find trigger points for both
    //directions

    int triggerTrack = Math.abs(positionTrack);

    //after photo eye reaches piece, give "on pipe" signal
    onPipeFlag = triggerTrack >= 10;

    //after photo eye reaches end of piece, turn off "on pipe" signal
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS) {onPipeFlag = false;}

    //wait a bit after head has passed the end and prepare for the next piece
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS + 10) {
        resetAllInspectionValues();
    }

    //start with all control flags set to 0
    controlFlags = (byte)0x00;
    //start with portE bits = 1, they are changed to zero if input is active
    portE = (byte)0xff;

    //set appropriate bit high for each flag which is active low
    if (onPipeFlag) {
        controlFlags = (byte)(controlFlags | MultiIODevice.ON_PIPE_CTRL);
    }

    //the following allows the dual linear encoder simulation to work, but
    // should actually only turn encoder 2 after the tube reaches it and
    // stop turning encoder 1 after the tube passes it

    //for trolley units, encoder 2 should turn as tube moves but encoder 1
    //should be incremented to reflect tube rotation -- most trolley units
    //don't have encoder 1 but instead use a once-per-rev TDC signal

    //move the encoders the forward or backward the amount expected by the host
    /*if (simulationMode == MessageLink.FORWARD){
        encoder1 += encoder1DeltaTrigger;
        encoder2 += encoder2DeltaTrigger;
    }
    else if (simulationMode == MessageLink.REVERSE){
        encoder1 -= encoder1DeltaTrigger;
        encoder2 -= encoder2DeltaTrigger;
    }*/
    //DEBUG HSS// //WIP HSS// assume moving forward
    
    //DEBUG HSS// remove later
    encoder1 += encoder1DeltaTrigger;
    encoder2 += encoder2DeltaTrigger;
    //DEBUG HSS// end remove later
    
    sendPacket(MultiIODevice.GET_INSPECT_PACKET_CMD,
                
                //packet count, MSB followed by LSB
                (byte)((inspectPacketCount >> 8) & 0xff),
                (byte)(inspectPacketCount++ & 0xff),
                
                //encoder 1 values by byte, MSB first
                (byte)((encoder1 >> 24) & 0xff),
                (byte)((encoder1 >> 16) & 0xff),
                (byte)((encoder1 >> 8) & 0xff),
                (byte)(encoder1 & 0xff),
            
                //encoder 2 values by byte, MSB first
                (byte)((encoder2 >> 24) & 0xff),
                (byte)((encoder2 >> 16) & 0xff),
                (byte)((encoder2 >> 8) & 0xff),
                (byte)(encoder2 & 0xff),
                
                //control flags
                controlFlags,
                
                //port E
                portE
                
            );

}//end of ControlSimulator::simulateInspection
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorTransverse::simulateSnapshot
//
// Creates a simulated data stream representing a snapshot of a peak.
//
// If no channels are on, snapshot is filled with 0s (0x7f due to pos/neg
// offset).
//

private int[] simulateSnapshot(int pPeak, boolean pChannelsOn)
{

    int signal = pPeak;

    //simulate noise or 0s if no channels on
    int data[] = new int[128];
    for(int i=0; i<data.length; i++){
        data[i] = 0x7f;
        //debug hss//if (pChannelsOn) { data[i] += (int)(SIM_NOISE * Math.random()); }
    }

    int spikeLoc = (int)(data.length/2);
    data[spikeLoc] = signal;

    return(data);

}// end of SimulatorTransverse::simulateSnapshot
//-----------------------------------------------------------------------------

}//end of class SimulatorTransverse
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

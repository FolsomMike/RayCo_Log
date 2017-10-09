/******************************************************************************
* Title: SimulatorControl.java
* Author: Mike Schoonover
* Date: 5/24/09
*
* Purpose:
*
* This class simulates a TCP/IP connection between the host and UT boards.
*
* This is a subclass of Socket and can be substituted for an instance
* of that class when simulated data is needed.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package hardware;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class SimulatorControl
//
// This class simulates data from a TCP/IP connection between the host computer
// and Control boards.
//

public class SimulatorControl extends Simulator {

    //simulates the default size of a socket created for ethernet access
    // NOTE: If the pipe size is too small, the outside object can fill the
    // buffer and have to wait until the thread on this side catches up.  If the
    // outside object has a timeout, then data will be lost because it will
    // continue on without writing if the timeout occurs.
    // In the future, it would be best if ControlBoard object used some flow
    // control to limit overflow in case the default socket size ends up being
    // too small.

    byte status = 0;

    public static int controlBoardCounter = 0;
    int controlBoardNumber;

    int chassisAddr, slotAddr;

    boolean onPipeFlag = false;
    boolean head1Down = false;
    boolean head2Down = false;
    boolean head3Down = false;
    boolean inspectMode = false;
    int simulationMode = Multi_IO_A_Control.STOP;
    int encoder1 = 0, encoder2 = 0;
    int encoder1DeltaTrigger = 1000, encoder2DeltaTrigger = 1000;
    int inspectPacketCount = 0;

    byte controlFlags = 0, portE = 0;

    int positionTrack; // this is the number of packets sent, not the encoder
                       // value

    public static int DELAY_BETWEEN_INSPECT_PACKETS = 1;
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
// SimulatorControl::SimulatorControl (constructor)
//

public SimulatorControl(InetAddress pIPAddress, int pPort,
                        String pTitle, String pSimulationDataSourceFilePath)
        throws SocketException
{

    //call the parent class constructor
    super(pIPAddress, pPort, pTitle, pSimulationDataSourceFilePath);

    //create an out writer from this class - will be input for some other class
    //this writer is only used to send the greeting back to the host

    PrintWriter out = new PrintWriter(localOutStream, true);
    out.println("Hello from Control Board Simulator!");

}//end of SimulatorControl::SimulatorControl (constructor)
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

}// end of SimulatorTransverse::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::processDataPackets
//
// See processDataPacketsHelper notes for more info.
//

@Override
public int processDataPackets(boolean pWaitForPkt)
{

    int x = 0;

    //process packets until there is no more data available

    // if pWaitForPkt is true, only call once or an infinite loop will occur
    // because the subsequent call will still have the flag set but no data
    // will ever be coming because this same thread which is now blocked is
    // sometimes the one requesting data

    if (pWaitForPkt) {
        return processDataPacketsHelper(pWaitForPkt);
    }
    else {
        while ((x = processDataPacketsHelper(pWaitForPkt)) != -1){}
    }

    return x;

}//end of SimulatorControl::processDataPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::processDataPacketsHelper
//
// Drive the simulation functions.  This function is usually called from a
// thread.
//

@Override
public int processDataPacketsHelper(boolean pWaitForPkt)
{

    if (byteIn == null) {return 0;}  //do nothing if the port is closed

    //simulate the inspection signals and send back packets to the host
    if (inspectMode == true) {simulateInspection();}

    try{

        int x;

        //wait until 5 bytes are available - this should be the 4 header bytes,
        //and the packet identifier/command
        if ((x = byteIn.available()) < 5) {return -1;}

        //read the bytes in one at a time so that if an invalid byte is
        //encountered it won't corrupt the next valid sequence in the case
        //where it occurs within 3 bytes of the invalid byte

        //check each byte to see if the first four create a valid header
        //if not, jump to resync which deletes bytes until a valid first header
        //byte is reached

        //if the reSynced flag is true, the buffer has been resynced and an 0xaa
        //byte has already been read from buffer so it shouldn't be read again

        //after a resync, the function exits without processing any packets

        if (!reSynced){
            //look for the 0xaa byte unless buffer just resynced
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] != (byte)0xaa) {reSync(); return 0;}
        }
        else {
            reSynced = false;
        }

        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x55) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0xbb) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x66) {reSync(); return 0;}

        //read the packet ID
        byteIn.read(inBuffer, 0, 1);

        byte pktID = inBuffer[0];

        if (pktID == MultiIODevice.GET_ALL_STATUS_CMD)
            {getStatus();}
        else if (pktID == MultiIODevice.SET_ENCODERS_DELTA_TRIGGER_CMD)
            {setEncodersDeltaTrigger(pktID);}
        else if (pktID == MultiIODevice.START_INSPECT_CMD) {startInspect(pktID);}
        else if (pktID == MultiIODevice.STOP_INSPECT_CMD) {stopInspect(pktID);}
        else if (pktID == MultiIODevice.GET_CHASSIS_SLOT_ADDRESS_CMD)
            {getChassisSlotAddress();}
        else if (pktID == MultiIODevice.GET_MONITOR_PACKET_CMD)
            {getMonitorPacket();}
        else if (pktID == MultiIODevice.ZERO_ENCODERS_CMD)
            {zeroEncoderCounts();}

        return 0;

    }//try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 221");
    }

    return 0;

}//end of SimulatorControl::processDataPacketsHelper
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::getStatus
//
// Simulates returning of the status byte.
//

void getStatus()
{

    sendPacket(MultiIODevice.GET_ALL_STATUS_CMD, status, (byte)0);

}//end of SimulatorControl::getStatus
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::zeroEncoderCounts
//
// Sets the encoder counts to zero.
//

void zeroEncoderCounts()
{

    enc1Count = 0; enc2Count = 0;

}//end of SimulatorControl::zeroEncoderCounts
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::getMonitorPacket
//
// Simulates returning of a packet of monitor values.
//

void getMonitorPacket()
{

    simulateMonitorPacket();

    ArrayList<Byte> packet = new ArrayList<>();

    if (enc1AInput == 0) packet.add((byte)0); else packet.add((byte)1);

    if (enc1BInput == 0) packet.add((byte)0); else packet.add((byte)1);

    if (enc2AInput == 0) packet.add((byte)0); else packet.add((byte)1);

    if (enc2BInput == 0) packet.add((byte)0); else packet.add((byte)1);

    if (padrUnused1 == 0) packet.add((byte)0); else packet.add((byte)1);

    if (padrUnused2 == 0) packet.add((byte)0); else packet.add((byte)1);

    if (padrInspect == 0) packet.add((byte)0); else packet.add((byte)1);

    if (padrUnused3 == 0) packet.add((byte)0); else packet.add((byte)1);

    if (pedrTDC == 0) packet.add((byte)0); else packet.add((byte)1);

    if (padrUnused4 == 0) packet.add((byte)0); else packet.add((byte)1);

    packet.add((byte)chassisAddr);

    packet.add((byte)slotAddr);

    packet.add((byte)inspectionStatus);

    packet.add((byte)((rpm >> 8) & 0xff));
    packet.add((byte)(rpm & 0xff));

    packet.add((byte)((rpmVariance >> 8) & 0xff));
    packet.add((byte)(rpmVariance & 0xff));

    packet.add((byte)((enc1Count >> 8) & 0xff));
    packet.add((byte)(enc1Count & 0xff));

    packet.add((byte)((enc2Count >> 8) & 0xff));
    packet.add((byte)(enc2Count & 0xff));

    packet.add((byte)((enc1CountsPerSec >> 8) & 0xff));
    packet.add((byte)(enc1CountsPerSec & 0xff));

    packet.add((byte)((enc2CountsPerSec >> 8) & 0xff));
    packet.add((byte)(enc2CountsPerSec & 0xff));

    //send packet
    int i = 0; //25 bytes in array list for packet
    sendPacket(Multi_IO_A_Control.GET_MONITOR_PACKET_CMD,
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++), packet.get(i++), packet.get(i++),
                packet.get(i++));

}//end of SimulatorControl::getMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::simulateMonitorPacket
//
// Simulates data in values sent via the monitor packet.
//
// In the future, these should be simulated elsewhere so that the encoder
// values only change when the Demo controls are in forward mode, etc.
//

private void simulateMonitorPacket()
{

    enc1AInput = flip0And1(enc1AInput);
    enc1BInput = flip0And1(enc1BInput);
    enc2AInput = flip0And1(enc2AInput);
    enc2BInput = flip0And1(enc2BInput);

    if (monitorSimRateCounter++ == 10){ monitorSimRateCounter = 0;}

    if(monitorSimRateCounter == 5){ padrInspect = 1; } else{ padrInspect = 0; }

    if(monitorSimRateCounter == 10){ pedrTDC = 1; } else{ pedrTDC = 0; }

    inspectionStatus = 0;

    rpm = (short)getRandomValue(118, 5);

    rpmVariance = (short)getRandomValue(2, 3);

    //debug mks -- fix this to use same encoder value as other code in
    //this simulation

    enc1Count++; enc2Count = getRandomValue(enc1Count - 3, 6);

    enc1CountsPerSec = (short)getRandomValue(1700, 23);
    enc2CountsPerSec = (short)getRandomValue(1700, 23);

}//end of SimulatorControl::simulateMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::flip0And1
//
// If pV is 0 then function will return 1 and viceversa.
//

private byte flip0And1(byte pV)
{

    if(pV == 0) { return(1); } else { return(0); }

}//end of SimulatorControl::flip0And1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::getRandomValue
//
// Returns a random number from pBase to pBase plus random variance of
// pVariance.
//

private int getRandomValue(int pBase, int pVariance)
{

    return((int)Math.round(pBase + Math.random() * pVariance));

}//end of SimulatorControl::getRandomValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::setEncodersDeltaTrigger
//
// Tells the Control board how many encoder counts to wait before sending
// an encoder value update.  The trigger value for each encoder is sent.
//
// Normally, this value will be set to something reasonable like .25 to 1.0
// inch of travel of the piece being inspected. Should be no larger than the
// distance represented by a single pixel.
//

int setEncodersDeltaTrigger(byte pPktID)
{

    //read the databytes and checksum
    int bytesRead = readBlockAndVerify(5, pPktID);

    if (bytesRead < 0) {return(bytesRead);} //bail out if error

    encoder1DeltaTrigger =
                   (int)((inBuffer[0]<<8) & 0xff00) + (int)(inBuffer[1] & 0xff);

    encoder2DeltaTrigger =
                   (int)((inBuffer[2]<<8) & 0xff00) + (int)(inBuffer[3] & 0xff);

    return(bytesRead);

}//end of SimulatorControl::setEncodersDeltaTrigger
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::readBlockAndVerify
//
// Reads pNumberOfBytes from byteIn into inBuffer. The bytes (including the last
// one which is the checksum) are summed with pPktID and then compared with
// 0x00.
//
// The value pNumberOfBytes should be equal to the number of data bytes
// remaining in the packet plus one for the checksum.
//
// Returns the number of bytes read if specified number of bytes were read and
// the checksum verified. Returns -1 otherwise.
//

int readBlockAndVerify(int pNumberOfBytes, byte pPktID)
{

    int bytesRead;

    try{
        bytesRead = byteIn.read(inBuffer, 0, pNumberOfBytes);
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 299");
        return(-1);
    }

    if (bytesRead == pNumberOfBytes){

        byte sum = 0;
        for(int i = 0; i < pNumberOfBytes; i++) {sum += inBuffer[i];}

        //calculate checksum to check validity of the packet
        if ( (pPktID + sum & (byte)0xff) != 0) {return(-1);}
    }
    else{
        //error -- not enough bytes could be read
        return(-1);
    }

    return(bytesRead);

}//end of SimulatorControl::readBlockAndVerify
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::getChassisSlotAddress
//
// Simulates returning of the chassis and slot address byte.
// Also returns the status byte.
//

void getChassisSlotAddress()
{

    byte address =
            (byte)(((byte)chassisAddr<<4 & 0xf0) + ((byte)slotAddr & 0xf));

    //send standard packet header
    sendPacket(Multi_IO_A_Control.GET_CHASSIS_SLOT_ADDRESS_CMD, address, status);

}//end of SimulatorControl::getChassisSlotAddress
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::startInspect
//
// Starts the inspect mode -- simulated encoder and inspection control flag
// packets will be sent to the host.
//

int startInspect(byte pPktID)
{

    int bytesRead = 0;

    try{
        bytesRead = byteIn.read(inBuffer, 0, 2);
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 358");
    }

    if (bytesRead == 2){

        //calculate checksum to check validity of the packet
        if ( (pPktID + inBuffer[0] + inBuffer[1] & (byte)0xff) != 0) {
            return(-1);
        }
    }
    else{
        //("Error - Wrong sized packet header for startInspect!\n");
        return(-1);
    }

    resetAll();

    inspectMode = true;

    return(bytesRead);

}//end of SimulatorControl::startInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::resetAll
//
// Resets all variables in preparation to simulate a new piece.
//

void resetAll()
{

    positionTrack = 0;
    onPipeFlag = false;
    head1Down = false;
    head2Down = false;
    head3Down = false;
    encoder1 = 0; encoder2 = 0;
    inspectPacketCount = 0;
    delayBetweenPackets = DELAY_BETWEEN_INSPECT_PACKETS;

}//end of SimulatorControl::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::stopInspect
//
// Stops the inspect mode.
//

int stopInspect(byte pPktID)
{

    int bytesRead = 0;

    try{
        bytesRead = byteIn.read(inBuffer, 0, 2);
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 415");
    }

    if (bytesRead == 2){

        //calculate checksum to check validity of the packet
        if ( (pPktID + inBuffer[0] + inBuffer[1] & (byte)0xff) != 0) {
            return(-1);
        }
    }
    else{
        //("Error - Wrong sized packet header for startInspect!\n");
        return(-1);
    }

    inspectMode = false;

    return(bytesRead);

}//end of SimulatorControl::stopInspect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorControl::simulateInspection
//
// Simulates signals expected by the host in inspect mode.
//

void simulateInspection()
{

    //do nothing if in STOP mode
    if (simulationMode == Multi_IO_A_Control.STOP) {return;}

    //delay between sending inspect packets to the host
    if (delayBetweenPackets-- != 0) {return;}
    //restart time for next packet send
    delayBetweenPackets = DELAY_BETWEEN_INSPECT_PACKETS;

    inspectPacketCount++; //count packets sent to host

    //track distance moved by number of packets sent
    if (simulationMode == Multi_IO_A_Control.FORWARD){
        positionTrack++;
    }
    else if (simulationMode == Multi_IO_A_Control.REVERSE){
        positionTrack--;
    }

    //the position track will run negative if inspection is occurring in the
    //reverse direction -- use absolute value to find trigger points for both
    //directions

    int triggerTrack = Math.abs(positionTrack);

    //after photo eye reaches piece, give "on pipe" signal
    if (triggerTrack >= 10) {onPipeFlag = true;} else {onPipeFlag = false;}

    //after head 1 reaches position, give head 1 down signal
    if (triggerTrack >= 225) {head1Down = true;} else {head1Down = false;}

    //after head 2 reaches position, give head 2 down signal
    if (triggerTrack >= 250) {head2Down = true;} else {head2Down = false;}

    //after head 3 reaches position, give head 3 down signal
    if (triggerTrack >= 275) {head3Down = true;} else {head3Down = false;}

    //after head 1 reaches pick up position, give head 1 up signal
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS-75) {head1Down = false;}

    //after head 2 reaches pick up position, give head 2 up signal
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS-50) {head2Down = false;}

    //after head 3 reaches pick up position, give head 3 up signal
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS-25) {head3Down = false;}

    //after photo eye reaches end of piece, turn off "on pipe" signal
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS) {onPipeFlag = false;}

    //wait a bit after head has passed the end and prepare for the next piece
    if (triggerTrack >= LENGTH_OF_JOINT_IN_PACKETS + 10) {resetAll();}

    //start with all control flags set to 0
    controlFlags = (byte)0x00;
    //start with portE bits = 1, they are changed to zero if input is active
    portE = (byte)0xff;

    //set appropriate bit high for each flag which is active low
    if (onPipeFlag)
        {controlFlags = (byte)(controlFlags | Multi_IO_A_Control.ON_PIPE_CTRL);}
    if (head1Down)
        {controlFlags = (byte)(controlFlags | Multi_IO_A_Control.HEAD1_DOWN_CTRL);}
    if (head2Down)
        {controlFlags = (byte)(controlFlags | Multi_IO_A_Control.HEAD2_DOWN_CTRL);}
    if (head3Down)
        {controlFlags = (byte)(controlFlags | Multi_IO_A_Control.HEAD3_DOWN_CTRL);}

    //the following allows the dual linear encoder simulation to work, but
    // should actually only turn encoder 2 after the tube reaches it and
    // stop turning encoder 1 after the tube passes it

    //for trolley units, encoder 2 should turn as tube moves but encoder 1
    //should be incremented to reflect tube rotation -- most trolley units
    //don't have encoder 1 but instead use a once-per-rev TDC signal

    //move the encoders the forward or backward the amount expected by the host
    if (simulationMode == Multi_IO_A_Control.FORWARD){
        encoder1 += encoder1DeltaTrigger;
        encoder2 += encoder2DeltaTrigger;
    }
    else if (simulationMode == Multi_IO_A_Control.REVERSE){
        encoder1 -= encoder1DeltaTrigger;
        encoder2 -= encoder2DeltaTrigger;
    }

    int pktSize = 12;
    int x = 0;

    sendPacketHeader(Multi_IO_A_Control.GET_INSPECT_PACKET_CMD);

    //send the packet count back to the host, MSB followed by LSB
    outBuffer[x++] = (byte)((inspectPacketCount >> 8) & 0xff);
    outBuffer[x++] = (byte)(inspectPacketCount++ & 0xff);

    //place the encoder 1 values into the buffer by byte, MSB first
    outBuffer[x++] = (byte)((encoder1 >> 24) & 0xff);
    outBuffer[x++] = (byte)((encoder1 >> 16) & 0xff);
    outBuffer[x++] = (byte)((encoder1 >> 8) & 0xff);
    outBuffer[x++] = (byte)(encoder1 & 0xff);

    //place the encoder 2 values into the buffer by byte, MSB first
    //place the encoder 1 values into the buffer by byte, MSB first
    outBuffer[x++] = (byte)((encoder2 >> 24) & 0xff);
    outBuffer[x++] = (byte)((encoder2 >> 16) & 0xff);
    outBuffer[x++] = (byte)((encoder2 >> 8) & 0xff);
    outBuffer[x++] = (byte)(encoder2 & 0xff);


    outBuffer[x++] = controlFlags;
    outBuffer[x++] = portE;

    //send packet to the host
    if (byteOut != null) {
        try{
            byteOut.write(outBuffer, 0 /*offset*/, pktSize);
        }
        catch (IOException e) {
            logSevere(e.getMessage() + " - Error: 546");
        }
    }

}//end of SimulatorControl::simulateInspection
//-----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// SimulatorControl::sendPacketHeader
//
// Sends via the socket: 0xaa, 0x55, 0xaa, 0x55, packet identifier.
//
// Does not flush.
//

void sendPacketHeader(byte pPacketID)
{

    try{ super.sendPacketHeader(pPacketID); }
    catch (IOException e) { logSevere(e.getMessage() + " - Error: 573"); }

}//end of SimulatorControl::sendPacketHeader
//----------------------------------------------------------------------------

}//end of class SimulatorControl
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

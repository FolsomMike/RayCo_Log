/******************************************************************************
* Title: Simulator.java (Universal_Chart)
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This is the super class for various simulator classes which simulate a TCP/IP
* connection between the host and various types of hardware.
*
* This is a subclass of Socket and can be substituted for an instance
* of that class when simulated data is needed.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;


//-----------------------------------------------------------------------------
// class Simulator
//

public class Simulator extends Socket
{
    private int simulatorNum;

    String title;

    public InetAddress ipAddr;
    int port;
    String simulationDataSourceFilePath;
    boolean reSynced;
    int reSyncCount = 0;
    public static int instanceCounter = 0;

    byte dataBuffer[] = new byte[Device.RUN_DATA_BUFFER_SIZE];

    int packetErrorCnt = 0;

    int rbtRunDataPktCount = 0;
    int picRunDataPktCount = 0;

    //simulates the default size of a socket created for ethernet access
    // NOTE: If the pipe size is too small, the outside object can fill the
    // buffer and have to wait until the thread on this side catches up.  If
    // the outside object has a timeout, then data will be lost because it will
    // continue on without writing if the timeout occurs.
    // In the future, it would be best if UTBoard object used some flow control
    // to limit overflow in case the default socket size ends up being too
    // small.

    static int PIPE_SIZE = 8192;

    PipedOutputStream outStream;
    PipedInputStream  localInStream;

    PipedInputStream  inStream;
    PipedOutputStream localOutStream;

    DataOutputStream byteOut = null;
    DataInputStream byteIn = null;

    int IN_BUFFER_SIZE = 512;
    byte[] inBuffer;

    int OUT_BUFFER_SIZE = 512;
    byte[] outBuffer;

    int numClockPositions;
    protected Channel[] activeChannels;

    int spikeOdds = 20;
    int lastSpikeValue = 0;

    static final int AD_MAX_VALUE = 255;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 127;
    static final int AD_ZERO_OFFSET = 127;
    static final int SIM_NOISE = 10;
    static final int SPIKE_ODDS_RANGE = 10000;
    static final int WALL_SIM_NOISE = 3;
    static final int WALL_SPIKE_ODDS_RANGE = 100000;

//-----------------------------------------------------------------------------
// Simulator::Simulator (constructor)
//

public Simulator(InetAddress pIPAddress, int pPort, String pTitle,
                String pSimulationDataSourceFilePath) throws SocketException
{

    port = pPort; ipAddr = pIPAddress; title = pTitle;

    simulationDataSourceFilePath = pSimulationDataSourceFilePath;

    //give each instance of the class a unique number
    //this can be used to provide a unique simulated IP address
    simulatorNum = instanceCounter++;

    //create an input and output stream to simulate those attached to a real
    //Socket connected to a hardware board

    // four steams are used - two connected pairs
    // an ouptut and an input stream are created to hand to the outside object
    // (outStream & inStream) - the outside object writes to outStream and reads
    // from inStream
    // an input stream is then created using the outStream as it's connection -
    // this object reads from that input stream to receive bytes sent by the
    // external object via the attached outStream
    // an output stream is then created using the inStream as it's connection -
    // this object writes to that output stream to send bytes to be read by the
    // external object via the attached inStream

    //this end goes to the external object
    outStream = new PipedOutputStream();
    //create an input stream (localInStream) attached to outStream to read the
    //data sent by the external object
    try{localInStream = new PipedInputStream(outStream, PIPE_SIZE);}
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 112");
    }

    //this end goes to the external object
    inStream = new PipedInputStream(PIPE_SIZE);
    //create an output stream (localOutStream) attached to inStream to read the
    //data sent by the external object
    try{localOutStream = new PipedOutputStream(inStream);}
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 121");
    }

    inBuffer = new byte[IN_BUFFER_SIZE]; //used by various functions
    outBuffer = new byte[OUT_BUFFER_SIZE]; //used by various functions

    //create an output and input byte stream
    //out for this class is in for the outside classes and vice versa

    byteOut = new DataOutputStream(localOutStream);
    byteIn = new DataInputStream(localInStream);

}//end of Simulator::Simulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//
// Parameter pBoardNumber is used to find info for the simulated board in a
// config file.
//

public void init(int pBoardNumber)
{

    //send greeting to host which will wait for this line
    PrintWriter out = new PrintWriter(localOutStream, true);
    out.println("Hello from " + title);

    //load general configuration data from file
//    try{
//        configureMain(pBoardNumber);
//    }
//    catch(IOException e){
//       return;
//  }

    //load general configuration data
//    configureSimulationDataSet();

}//end of Simulator::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::setActiveChannels
//
// Active channels is used to determine which channels and values to simulate.
//

public void setActiveChannels(Channel[] pChannels)
{

    activeChannels = pChannels;

}//end of Simulator::setActiveChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::processDataPackets
//
// See processDataPacketsHelper notes for more info.
//

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

}//end of Simulator::processDataPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::processDataPacketsHelper
//
// Process incoming data.  This function is usually called from a
// thread.
//

public int processDataPacketsHelper(boolean pWaitForPkt)
{

    if (byteIn == null) {return(0);}  //do nothing if the port is closed

    try{

        int x;

        //wait until 5 bytes are available - this should be the 4 header bytes,
        //and the packet identifier/command
        if ((x = byteIn.available()) < 5) {return(-1);}

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

        handlePacket(inBuffer[0]);

        return 0;

    }//try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 519");
    }

    return 0;

}//end of Simulator::processDataPacketsHelper
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::handlePacket
//
// Performs the processing and returns data appropriate for the packet
// identifier/command byte passed in via pCommand.
//
// Should be overridden by child classes to provide custom handling.
// The child class should call this method from within the overriding method.
//

public void handlePacket(byte pCommand)
{

    if (pCommand == Device.GET_ALL_STATUS_CMD) { handleGetAllStatus(); }
    else
    if (pCommand == Device.SET_POT_CMD) { handleSetPot(); }
    else
    if (pCommand == Device.GET_ALL_LAST_AD_VALUES_CMD) {
                                            handleGetAllLastADValuesPacket(); }
    else
    if (pCommand == Device.GET_RUN_DATA_CMD) { handleGetRunData(); }

}//end of Simulator::handlePacket
//-----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// Simulator::readBytesAndVerify
//
// Attempts to read pNumBytes number of bytes from ethernet into pBuffer and
// verifies the data using the last byte as a checksum.
//
// Note: pNumBytes count should include the data bytes plus the checksum byte.
//
// The packet ID should be provided via pPktID -- it is only used to verify the
// checksum as it is included in that calculation by the sender.
//
// Returns the number of bytes read, including the checksum byte.
// On checksum error, returns -1.
// If pNumBytes are not available after waiting, returns -2.
// On IOException, returns -3.
//
// NOTE: The use of waitSleep in this function to wait for more bytes will not
//  be useful if the same thread running the simulation (and calling this
//  method) is the same thread putting the bytes into the socket. In such case,
//  both operations would be suspended at the same time. The call is left in
//  place regardless to maintain consistency.
//

int readBytesAndVerify(byte[] pBuffer, int pNumBytes, int pPktID)
{

    try{

        int timeOutProcess = 0;

        while(timeOutProcess++ < 2){
            if (byteIn.available() >= pNumBytes) {break;}
            waitSleep(10);
        }

        if (byteIn.available() >= pNumBytes) {
            byteIn.read(pBuffer, 0, pNumBytes);
        }else{
            packetErrorCnt++; return(-2);
        }

    }// try
    catch(IOException e){
        packetErrorCnt++;
        logSevere(e.getMessage() + " - Error: 281");
        return(-3);
    }

    byte sum = (byte)pPktID; //packet ID is included in the checksum

    //validate checksum by summing the packet id and all data

    for(int i = 0; i < pNumBytes; i++){ sum += pBuffer[i]; }

    if ( (sum & 0xff) == 0) { return(pNumBytes); }
    else{ packetErrorCnt++; return(-1); }

}//end of Simulator::readBytesAndVerify
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::handleGetAllStatus
//
// Handles GET_ALL_STATUS_CMD packet requests. Sends appropriate packet via
// the socket.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//
// See Device.handleAllStatusPacket method for details on the packet
// structure.
//

public int handleGetAllStatus()
{

    int numBytesInPkt = 2;  //includes the checksum byte

    int result = readBytesAndVerify(
                       inBuffer, numBytesInPkt, Device.GET_ALL_STATUS_CMD);
    if (result != numBytesInPkt){ return(result); }

    //send test data packet -- sendPacket appends Rabbit's checksum

    sendPacket(Device.GET_ALL_STATUS_CMD,
            //Rabbit Status (12 bytes)
            (byte)0x01,(byte)0x02,              // software version
            (byte)0x12,(byte)0x34,              // confrolFlags
            (byte)0x56,                         // system status
            (byte)0x00,(byte)0x00,              // host communication errors
            (byte)0x00,(byte)0x00,              // serial port com errors
            (byte)0x01,(byte)0x02,(byte)0x03,   // unused bytes
            //Rabbit Status end

            //Master Status (09 bytes)
            (byte)0x01,(byte)0x01,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //serial port error count
            (byte)0x00,                         //slave I2C error count
            (byte)0x01,(byte)0x02,(byte)0x03,   // unused bytes
            //Master Status end

            //Slave 0 Status (11 bytes)
            (byte)0x00,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //I2C communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,                         //checksum
            //end Slave 0 Status

            //Slave 1 Status (11 bytes)
            (byte)0x01,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 1 Status

            //Slave 2 Status (11 bytes)
            (byte)0x02,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 2 Status

            //Slave 3 Status (11 bytes)
            (byte)0x03,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 3 Status

            //Slave 4 Status (11 bytes)
            (byte)0x04,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 4 Status

            //Slave 5 Status (11 bytes)
            (byte)0x05,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 5 Status

            //Slave 6 Status (11 bytes)
            (byte)0x06,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 6 Status

            //Slave 7 Status (11 bytes)
            (byte)0x07,                         //I2C address
            (byte)0x01,(byte)0x02,              //software version
            (byte)0x00,                         //flags
            (byte)0x00,                         //status flags
            (byte)0x00,                         //communication error count
            (byte)0x7c,                         //last A/D sample
            (byte)0x01,(byte)0x02,(byte)0x03,   //unused
            (byte)0x7b,
            //end Slave 7 Status

            (byte)0x0f                          //Master PIC checksum

    );

    return(result);

}//end of Simulator::handleGetAllStatus
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::handleGetAllLastADValuesPacket
//
// Handles GET_ALL_LAST_AD_VALUES_CMD packet requests. Sends appropriate packet
// via the socket.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//
// See Device.handleAllLastADValuesPacket method for details on the packet
// structure.
//

public int handleGetAllLastADValuesPacket()
{

    int numBytesInPkt = 2;  //includes the checksum byte

    int result = readBytesAndVerify(
                   inBuffer, numBytesInPkt, Device.GET_ALL_LAST_AD_VALUES_CMD);
    if (result != numBytesInPkt){ return(result); }

    //send test data packet -- sendPacket appends Rabbit's checksum

    sendPacket(Device.GET_ALL_LAST_AD_VALUES_CMD,
    (byte)0x12,(byte)0x34,(byte)0xba,
    (byte)0x56,(byte)0x78,(byte)0x32,
    (byte)0x12,(byte)0x34,(byte)0xba,
    (byte)0x56,(byte)0x78,(byte)0x32,
    (byte)0x12,(byte)0x34,(byte)0xba,
    (byte)0x56,(byte)0x78,(byte)0x32,
    (byte)0x12,(byte)0x34,(byte)0xba,
    (byte)0x56,(byte)0x78,(byte)0x32,
    (byte)0x00
    );

    return(result);

}//end of Simulator::handleGetAllLastADValuesPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::handleGetRunData
//
// Handles GET_RUN_DATA_CMD packet requests. Sends appropriate packet via
// the socket.
//
// This method should be overridden by child classes to provide appropriate
// processing.
//

public int handleGetRunData()
{

    return(0);

}//end of Simulator::handleGetRunData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::handleSetPot
//
// Handles SET_POT_CMD packet requests. Sets the specified digital pot to the
// specified value and transmits an ACK packet.
//
// Returns the number of bytes this method extracted from the socket or the
// error code returned by readBytesAndVerify().
//


public int handleSetPot()
{

    int numBytesInPkt = 4; //includes the checksum byte

    int result = readBytesAndVerify(inBuffer,numBytesInPkt,Device.SET_POT_CMD);
    if (result != numBytesInPkt){ return(result); }

    //set pot here -- add code to do this later -- needs to affect the sim sig
    // inBuffer[0] is the I2C address of the PIC enabling the digital pot chip
    // inBuffer[1] is the pot number in the chip
    // inBuffer[2] is the pot value

    sendACK();

    return(result);

}//end of Simulator::handleSetPot
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::sendACK
//
// Transmits an ACK packet via the socket. Contains the ACK command and a
// single unused data byte.
//

public void sendACK()
{

    //sendPacket appends Rabbit's checksum

    sendPacket(Device.ACK_CMD, (byte)0);

}//end of Simulator::sendACK
//-----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// Simulator::sendPacketHeader
//
// Sends via the socket: 0xaa, 0x55, 0xaa, 0x55, and the packet command.
//
// The socket is not flushed in anticipation of more bytes being sent to
// complete the packet.
//

void sendPacketHeader(int pCommand) throws IOException
{

   byteOut.write(0xaa); byteOut.write(0x55);
   byteOut.write(0xbb); byteOut.write(0x66);

   byteOut.write(pCommand);

}//end of Simulator::sendPacketHeader
//----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::sendPacketViaSocket
//
// Sends a packet via pSocket with command code pCommand followed by a variable
//  number of bytes (one or more).
//
// A header is prepended and a checksum byte appended. The checksum includes
// the command byte and all bytes in the argument list, but not the header
// bytes.
//

void sendPacket(byte pCommand, byte... pBytes)

{

    int sum = pCommand;//command byte included in checksum

    //send packet to remote
    if (byteOut != null) {
        try{

              sendPacketHeader(pCommand);

              byteOut.write(pBytes, 0 /*offset*/, pBytes.length);

              for (int i=0; i<pBytes.length; i++){
                  sum += pBytes[i];
              }

            //calculate checksum and send it
            int checksum = (byte)(0x100 - (byte)(sum & 0xff));
            byteOut.write(checksum);

            byteOut.flush();
        }
        catch (IOException e) {
            logSevere(e.getMessage() + " - Error: 220");
        }
    }

}//end of Simulator::sendPacketViaSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::getInputStream()
//
// Returns an input stream for the calling object - it is an input to that
// object.
//

@Override
public InputStream getInputStream()
{

    return (inStream);

}//end of Simulator::getInputStream
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::getOutputStream()
//
// Returns an output stream for the calling object - it is an output from that
// object.
//

@Override
public OutputStream getOutputStream()
{

    return (outStream);

}//end of Simulator::getOutputStream
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::reSync
//
// Clears bytes from the socket buffer until 0xaa byte reached which signals
// the *possible* start of a new valid packet header or until the buffer is
// empty.
//
// If an 0xaa byte is found, the flag reSynced is set true to that other
// functions will know that an 0xaa byte has already been removed from the
// stream, signalling the possible start of a new packet header.
//
// There is a special case where a 0xaa is found just before the valid 0xaa
// which starts a new packet - the first 0xaa is the last byte of the previous
// packet (usually the checksum).  In this case, the next packet will be lost
// as well.  This should happen rarely.
//

public void reSync()
{

    reSynced = false;

    //track the number of time this function is called, even if a resync is not
    //successful - this will track the number of sync errors
    reSyncCount++;

    try{
        while (byteIn.available() > 0) {
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] == (byte)0xaa) {reSynced = true; break;}
            }
        }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 169");
    }

}//end of Simulator::reSync
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::getRunPacket
//
// Returns a run-time packet of simulated data.
//

public void getRunPacket(byte[] pPacket)
{

}// end of Simulator::getRunPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::addByteToPacket
//
// Inserts low byte of integer pByte in pPacket, at location pIndex.
//

void addByteToPacket(byte[] pPacket, int pIndex, int pByte)
{

    pPacket[pIndex++] = (byte)(pByte & 0xff);

}//end of Simulator::addByteToPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::addUnsignedShortToPacket
//
// Inserts the two least significant bytes of pInteger to pPacket, MSB first
// starting at location pIndex.
//
// The two bytes will represent an unsigned short.
//

void addUnsignedShortToPacket(byte[] pPacket, int pIndex, int pInteger)
{

    pPacket[pIndex++] = (byte)((pInteger >> 8) & 0xff);
    pPacket[pIndex++] = (byte)(pInteger & 0xff);

}//end of Simulator::addUnsignedShortToPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::addIntToPacket
//
// Inserts the four bytes of pInteger to pPacket, MSB first starting at
// location pIndex.
//

void addIntToPacket(byte[] pPacket, int pIndex, int pInteger)
{

    pPacket[pIndex++] = (byte)((pInteger >> 24) & 0xff);
    pPacket[pIndex++] = (byte)(pInteger >> 16 & 0xff);
    pPacket[pIndex++] = (byte)((pInteger >> 8) & 0xff);
    pPacket[pIndex++] = (byte)(pInteger & 0xff);

}//end of Simulator::addIntToPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::simulatePositiveSignal
//
// Simulates a positive-going trace with baseline noise and occasional higher
// kicks.
//

int simulatePositiveSignal()
{

    int value = AD_ZERO_OFFSET;

    value += (int)(SIM_NOISE * Math.random());

    if ((int)(SPIKE_ODDS_RANGE*Math.random()) < spikeOdds){
        value += (int)(100 * Math.random());
    }

    if (value > AD_MAX_VALUE) { value = AD_MAX_VALUE; }

    return(value);

}//end of Simulator::simulatePositiveSignal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::simulateNegativeSignal
//
// Simulates a negative-going trace with baseline noise and occasional higher
// kicks.
//

int simulateNegativeSignal()
{

    int value = AD_ZERO_OFFSET;

    value -= (int)(SIM_NOISE * Math.random());

    if ((int)(SPIKE_ODDS_RANGE*Math.random()) < spikeOdds){
        value -= (int)(100 * Math.random());
    }

    if (value < AD_MIN_VALUE) { value = AD_MIN_VALUE; }

    return(value);

}//end of Simulator::simulateNegativeSignal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::waitSleep
//
// Sleeps for pTime milliseconds.
//

public void waitSleep(int pTime)
{

    try {Thread.sleep(pTime);} catch (InterruptedException e) { }

}//end of Simulator::waitSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

final void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of Simulator::logSevere
//-----------------------------------------------------------------------------

}//end of class Simulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

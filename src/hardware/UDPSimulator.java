/******************************************************************************
* Title: UDPSimulator.java
* Author: Mike Schoonover
* Date: 3/25/15
*
* Purpose:
*
* This class simulates a UDP connection between the host and the remote
* devices (Multi-IO Boards, Control Boards, UT Boards, etc.)
*
* This is a subclass of MulticastSocket and can be substituted for an instance
* of that class when simulated data is needed.
* 
* This class uses a crude method to simulate the socket...it overrides the
* MulticastSocket.receive method and simply returns a data packet when the
* main program calls that method to receive data.
* 
* The Ethernet socket simulator used by the Simulator class is a more robust
* simulation and actually reads and writes data to the underlying sockets
* to communicate with the main program.
*
* On creation, a string list of device types is passed in. The types should
* mimic the response string which would be returned from an actual device. This
* simulator will respond with each of the strings in the list to mimic multiple
* devices responding. The same string may be present multiple times as a system
* may have more than one device of the same type.
* 
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package hardware;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class UDPSimulator
//
// This class simulates data from a UDP connection between the host computer
// and remote devices such as Control Boards, UT Boards, etc.
//

public class UDPSimulator extends MulticastSocket{

    int port;

    int responseCount = 0;
    
    int numDevices;

    ArrayList<String> deviceTypes;

//-----------------------------------------------------------------------------
// UDPSimulator::UDPSimulator (constructor)
//

public UDPSimulator(int pPort, ArrayList<String> pDeviceTypes)
                                            throws SocketException, IOException
{

    super(pPort);

    port = pPort; deviceTypes = pDeviceTypes;
    
    numDevices = deviceTypes.size();

}//end of UDPSimulator::UDPSimulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// UDPSimulator::send
//
//

@Override
public void send(DatagramPacket p)
{

}//end of UDPSimulator::send
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// UDPSimulator::receive
//
// This method gets triggered when the program waits on a UDP packet.
//
// See notes in header regarding the methods used in this class compared to
// the socket simulator in the Simulator class.
//

@Override
public void receive(DatagramPacket p) throws SocketTimeoutException
{

    //no response after all devices have responded, socket times out
    if (responseCount == numDevices){
        throw new SocketTimeoutException();
    }
    
    //return each of the device type strings one time
    p.setData(deviceTypes.get(responseCount).getBytes());

    //each simulated device sends a response packet which will have its IP
    //address - for each instance of this class created, use the next sequential
    //IP address

    String ip = "169.254.1." + responseCount;

    responseCount++;
    
    try{p.setAddress(InetAddress.getByName(ip));}
    catch(UnknownHostException e){
        logSevere(e.getMessage() + " - Error: 120");
    }

}//end of UDPSimulator::receive
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// UDPSimulator::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of UDPSimulator::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// UDPSimulator::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of UDPSimulator::logStackTrace
//-----------------------------------------------------------------------------

}//end of class UDPSimulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

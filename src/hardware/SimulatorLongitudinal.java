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

import java.net.InetAddress;
import java.net.SocketException;

//-----------------------------------------------------------------------------
// class SimulatorLongitudinal
//

public class SimulatorLongitudinal extends Simulator
{


//-----------------------------------------------------------------------------
// SimulatorLongitudinal::SimulatorLongitudinal (constructor)
//

public SimulatorLongitudinal(InetAddress pIPAddress, int pPort,
     String pTitle, String pSimulationDataSourceFilePath) throws SocketException
{

    super(pIPAddress, pPort, pTitle, pSimulationDataSourceFilePath);

}//end of SimulatorLongitudinal::SimulatorLongitudinal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorLongitudinal::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init(int pBoardNumber)
{

    super.init(pBoardNumber);

    numClockPositions = 48;

    spikeOdds = 5;

}// end of SimulatorLongitudinal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// SimulatorLongitudinal::handlePacket
//
// Performs the processing and returns data appropriate for the packet
// identifier/command byte passed in via pCommand.
//

@Override
public void handlePacket(byte pCommand)
{

    super.handlePacket(pCommand);

}//end of SimulatorLongitudinal::handlePacket
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

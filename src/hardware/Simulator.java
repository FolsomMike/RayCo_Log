/******************************************************************************
* Title: Simulator.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class is the parent class for subclasses which provide simulated data.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// class Simulator
//

public class Simulator
{

    private final int simulatorNum;
    
    static final int AD_MAX_VALUE = 1023;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 511;
    static final int AD_ZERO_OFFSET = 511;
    static final int SIM_NOISE = 10;
    static final double SPIKE_ODDS = .10;
        
//-----------------------------------------------------------------------------
// Simulator::Simulator (constructor)
//

public Simulator(int pSimulatorNum)
{

    simulatorNum = pSimulatorNum;
    
}//end of Simulator::Simulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{


}// end of Simulator::init
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
    
    if (Math.random() > SPIKE_ODDS){
        value += (int)(AD_MAX_SWING * Math.random());
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
    
    if (Math.random() > SPIKE_ODDS){
        value -= (int)(AD_MAX_SWING * Math.random());
    }
    
    if (value < AD_MIN_VALUE) { value = AD_MIN_VALUE; }
    
    return(value);

}//end of Simulator::simulateNegativeSignal
//-----------------------------------------------------------------------------


}//end of class Simulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

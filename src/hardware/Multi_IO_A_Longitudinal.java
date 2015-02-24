/******************************************************************************
* Title: Multi_IO_A_Longitudinal.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class handles communication with a Multi-IO board configuration A
* used for Longitudinal data acquisition.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.IniFile;
import toolkit.MKSInteger;

//-----------------------------------------------------------------------------
// class Multi_IO_A_Longitudinal
//

public class Multi_IO_A_Longitudinal extends Device
{

    byte[] packet;

    MKSInteger data = new MKSInteger(0);    
    
    Simulator simulator = null;

    static final int PACKET_SIZE = 9;

    static final int AD_MAX_VALUE = 1023;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 511;
    static final int AD_ZERO_OFFSET = 511;
        
//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::Multi_IO_A_Longitudinal (constructor)
//

public Multi_IO_A_Longitudinal(
                            int pIndex, IniFile pConfigFile, boolean pSimMode)
{

    super(pIndex, pConfigFile, pSimMode);

    packet = new byte[PACKET_SIZE];

    if(simMode){ simulator = new SimulatorLongitudinal(0); }
    
}//end of Multi_IO_A_Longitudinal::Multi_IO_A_Longitudinal (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{
    
    super.init();

    loadConfigSettings();
    
    setUpChannels();    

}// end of Multi_IO_A_Longitudinal::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::collectData
//
// Collects data from source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//

@Override
public void collectData()
{
    
    if (!simMode){ 
        getRunPacketFromDevice(packet);
    }else{        
        simulator.getRunPacket(packet);
    }
    
    //first channel's buffer location specifies start of channel data section
    int index = channels[0].getBufferLoc();
    
    for(Channel channel : channels){
     
        data.x = getUnsignedShortFromPacket(packet, index);
        data.x = Math.abs(data.x -= AD_ZERO_OFFSET);
        channel.catchPeak(data);
        index += 2;
        
    }

}// end of Multi_IO_A_Longitudinal::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Multi_IO_A_Longitudinal::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{
    
    super.loadConfigSettings();
    
    String section = "Device " + deviceNum + " Settings";

}// end of Multi_IO_A_Longitudinal::loadConfigSettings
//-----------------------------------------------------------------------------


}//end of class Multi_IO_A_Longitudinal
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

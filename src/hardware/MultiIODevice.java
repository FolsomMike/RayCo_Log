/******************************************************************************
* Title: MultiIODevice.java
* Author: Mike Schoonover
* Date: 02/24/15
*
* Purpose:
*
* This class is the parent class for subclasses which handle communication with
* Multi-IO boards.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.IniFile;
import view.LogPanel;


//-----------------------------------------------------------------------------
// class MultiIODevice
//

public class MultiIODevice extends Device
{
    
    int data;
    
    int[] mapData;
    
    int PACKET_SIZE;
    
    byte[] packet;    
  
    static final int AD_MAX_VALUE = 1023;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 511;
    static final int AD_ZERO_OFFSET = 511;
    
    
//-----------------------------------------------------------------------------
// MultiIODevice::MultiIODevice (constructor)
//

public MultiIODevice(int pDeviceNum, LogPanel pLogPanel, IniFile pConfigFile,
                                                             boolean pSimMode)
{

    super(pDeviceNum, pLogPanel, pConfigFile, pSimMode);

}//end of MultiIODevice::MultiIODevice (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Do not call loadConfigSettings here...the subclasses should do it.
//

@Override
public void init()
{

    super.init();

    packet = new byte[PACKET_SIZE];
    
}// end of MultiIODevice::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::initAfterLoadingConfig
//
// Further initializes the object using data loaded from the config file.
// Must be called by subclasses after they call loadConfigSettings(), which
// they must call themselves as they specify the section to be read from.
//

@Override
public void initAfterLoadingConfig()
{

    super.initAfterLoadingConfig();
    
    if(numClockPositions != 0){ mapData = new int[numClockPositions]; }
    
}// end of MultiIODevice::initAfterLoadingConfig
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::collectData
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

    super.collectData();
    
    getRunPacketFromDevice(packet);
    
    //first channel's buffer location specifies start of channel data section
    int index = channels[0].getBufferLoc();
    
    for(Channel channel : channels){
     
        data = getUnsignedShortFromPacket(packet, index);
        data = Math.abs(data -= AD_ZERO_OFFSET);
        channel.catchPeak(data);
        index += 2;
        
    }
    
    if(numClockPositions > 0){ extractMapDataAndCatchPeak(packet, index); }

}// end of MultiIODevice::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::extractMapDataAndCatchPeak
//
// Extracts map data from pPacket beginning at position pIndex and catches
// peak values.
//
// Returns the updated value of pIndex which will then point at the next
// bayte after the map data which was extracted.
//

public int extractMapDataAndCatchPeak(byte[] pPacket, int pIndex)
{
    
    for(int i=0; i<numClockPositions; i++){
        mapData[i] = getUnsignedShortFromPacket(pPacket, pIndex);
        mapData[i] = Math.abs(mapData[i] -= AD_ZERO_OFFSET);
        pIndex += 2;                
    }
    
    peakMapBuffer.catchPeak(mapData);    

    return(pIndex);
    
}// end of MultiIODevice::extractMapDataAndCatchPeak
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::loadConfigSettings
//
// Loads settings for the object from configFile.
//

@Override
void loadConfigSettings()
{

    super.loadConfigSettings();
    
}// end of MultiIODevice::loadConfigSettings
//-----------------------------------------------------------------------------

}//end of class MultiIODevice
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

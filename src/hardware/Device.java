/******************************************************************************
* Title: Device.java
* Author: Mike Schoonover
* Date: 01/16/15
*
* Purpose:
*
* This class is the parent class for subclasses which handle communication with
* various remote devices which perform data acquisition, outputs, etc.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------

import model.IniFile;


//-----------------------------------------------------------------------------
// class Device
//

public class Device
{

    final IniFile configFile;
    final int deviceNum;
    String title = "", shortTitle = "";
    int numChannels = 0;
    public int getNumChannels(){ return(numChannels); }
    Channel []channels = null;

    boolean simMode;
    
//-----------------------------------------------------------------------------
// Device::Device (constructor)
//

public Device(int pDeviceNum, IniFile pConfigFile, boolean pSimMode)
{

    deviceNum = pDeviceNum; configFile = pConfigFile; simMode = pSimMode;
    
}//end of Device::Device (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Do not call loadConfigSettings here...the subclasses should do it.
//

public void init()
{
    
}// end of Device::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::setUpChannels
//
// Creates and sets up the channels.
//

void setUpChannels()
{
    
    if (numChannels <= 0){ return; }

    channels = new Channel[numChannels];
    
    for(int i=0; i<numChannels; i++){
     
        channels[i] = new Channel(deviceNum, i, configFile);
        channels[i].init();
        
    }
    
}// end of Device::setUpChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::collectData
//
// Collects data from source(s) -- remote hardware devices, databases,
// simulations, etc.
//
// Should be called periodically to allow collection of data buffered in the
// source.
//
// Should be overridden by child classes to provide custom handling.
//

public void collectData()
{
    
    
    
}// end of Device::collectData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{
    
    String section = "Device " + deviceNum + " Settings";

    title = configFile.readString(section, "title", "Device " + deviceNum);
    
    shortTitle = configFile.readString(section, "short title", 
                                                        "Device " + deviceNum);

    numChannels = configFile.readInt(section, "number of channels", 0);

}// end of Device::loadConfigSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakForChannelAndReset
//
// Retrieves the current value of the peak for channel pChannel and resets
// the peak to the reset value.
//
// This class returns an object as the peak may be of various data types.
//

public void getPeakForChannelAndReset(int pChannel, Object pPeakValue)
{
    
    channels[pChannel].getPeakAndReset(pPeakValue);
    
}// end of Device::getPeakForChannelAndReset
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getPeakData
//
// Retrieves the current value of the peak for channel pChannel along with
// all relevant info for the channel such as the chart & trace to which it is
// attached.
//
// Resets the peak to the reset value.
//

public void getPeakData(int pChannel, PeakData pPeakData)
{
    
    channels[pChannel].getPeakData(pPeakData);
    
}// end of Device::getPeakData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getRunPacketFromDevice
//
// Retrieves a run-time data packet from the remote device.
//

void getRunPacketFromDevice(byte[] pPacket)
{
    
}// end of Device::getRunPacketFromDevice
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getIntFromPacket
//
// Extracts and combines four bytes from pPacket, MSB first starting at
// location pIndex, and returns the value as an int.
//

int getIntFromPacket(byte[] pPacket, int pIndex)
{

    return(
         ((pPacket[pIndex++]<<24) & 0xff000000)
         + ((pPacket[pIndex++]<<16) & 0xff0000)
          + ((pPacket[pIndex++]<<8) & 0xff00)
            + (pPacket[pIndex++] & 0xff)
    );
    
}//end of Device::getIntFromPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::getUnsignedShortFromPacket
//
// Extracts and combines two bytes representing an unsigned short from pPacket,
// MSB first starting at location pIndex, and returns the value as an int.
//

int getUnsignedShortFromPacket(byte[] pPacket, int pIndex)
{

    return (
            (int)((pPacket[pIndex++]<<8) & 0xff00)
            + (int)(pPacket[pIndex++] & 0xff)    
    );
    
}//end of Device::getUnsignedShortFromPacket
//-----------------------------------------------------------------------------

}//end of class Device
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

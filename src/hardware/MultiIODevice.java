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
import toolkit.MKSInteger;


//-----------------------------------------------------------------------------
// class MultiIODevice
//

public class MultiIODevice extends Device
{
    
    MKSInteger data = new MKSInteger(0);    
    
    Simulator simulator = null;
    
    int PACKET_SIZE;
    
    byte[] packet;    
    
    static final int AD_MAX_VALUE = 1023;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 511;
    static final int AD_ZERO_OFFSET = 511;
    
    
//-----------------------------------------------------------------------------
// MultiIODevice::MultiIODevice (constructor)
//

public MultiIODevice(int pDeviceNum, IniFile pConfigFile, boolean pSimMode)
{

    super(pDeviceNum, pConfigFile, pSimMode);

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

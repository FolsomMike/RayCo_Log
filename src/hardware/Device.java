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
    final int index;
    String title = "", shortTitle = "";
    int numChannels = 0;
    public int getNumChannels(){ return(numChannels); }
    Channel []channels = null;
    
//-----------------------------------------------------------------------------
// Device::Device (constructor)
//

public Device(int pIndex, IniFile pConfigFile)
{

    index = pIndex; configFile = pConfigFile;
    
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
     
        channels[i] = new Channel(index, i, configFile);
        channels[i].init();
        
    }
    
}// end of Device::setUpChannels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Device::loadConfigSettings
//
// Loads settings for the object from configFile.
//

void loadConfigSettings()
{
    
    String section = "Device " + index + " Settings";

    title = configFile.readString(section, "title", "Device " + index);
    
    shortTitle = configFile.readString(section, "short title", 
                                                            "Device " + index);

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


}//end of class Device
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

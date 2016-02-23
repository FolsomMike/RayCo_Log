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
        
    byte[] packet;    
  
    static final int AD_MAX_VALUE = 255;
    static final int AD_MIN_VALUE = 0;
    static final int AD_MAX_SWING = 127;
    static final int AD_ZERO_OFFSET = 127;
    
    
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

    packet = new byte[RUN_DATA_BUFFER_SIZE];
    
}// end of MultiIODevice::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::initAfterConnect
//
// Performs initialization of the remote device after it has been connected.
//
// Should be overridden by child classes to provide custom handling.
//

@Override
void initAfterConnect(){

    super.initAfterConnect();
    
    //debug mks
    
    requestAllStatusPacket();
    
    waitSleep(300);
    
    requestAllLastADValues();
    
    //waitSleep(5000); //without the sleep, calling twice locks up the PICs -- why?
    //requestAllStatusPacket();

    //debug mks end
         
}//end of MultiIODevice::initAfterConnect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetPotPacket
//
// Sets gain or offset digital pot for hardware channel pHdwChannel to pValue.
//
// Each digital pot chip contains four pots. Two pots are used for the gain
// and offset of a channel while the other two pots are used for a second
// channel. Thus, each chip is shared by a channel pair.
//
// Each pot chip is enabled by an I/O pin on a PIC: 
// CH1/CH2 pot by PIC1 (I2C address 0)
// CH3/CH4 pot by PIC3 (I2C address 2)
// ...and so on...
//
// To set a pot value, the chip containing that pot is first enabled by the
// Master PIC by sending a command to the appropriate Slave PIC which controls
// the enable line of that chip. Afterwards, it disables the pot chip using a
// second command.
//
// pChannel: 0-7
// pGainOrOffset: GAIN_POT or OFFSET_POT
// pVAlue: 0-255
//
// Note: on the schematic/board PIC and Channel numbering is 1 based, i.e.
//      Channel 0~7 -> channel 1~8  PIC 0~7 -> PIC 1~8
//

void sendSetPotPacket(int pHdwChannel, int pGainOrOffset, int pValue)
{

    int slavePICAddr, potNum;
    
    //even number channels enabled by PIC with address same as channel num
    //  Offset = Pot 0 inside chip
    //  Gain   = Pot 1 inside chip
    
    //odd number channels enabled by PIC with address one less than channel num
    //  Offset = Pot 3 inside chip
    //  Gain   = Pot 2 inside chip
    
    if((pHdwChannel % 2) == 0){
        slavePICAddr = pHdwChannel;
        potNum = (pGainOrOffset == OFFSET_POT) ? 0 : 1;
    }else{    
        slavePICAddr = pHdwChannel - 1;
        potNum = (pGainOrOffset == OFFSET_POT) ? 3 : 2;
    }
 
    sendPacket(SET_POT_CMD, (byte)slavePICAddr, (byte)potNum, (byte)pValue);
    
    numACKsExpected++;
    
}//end of MultiIODevice::sendSetPotPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetGainPacket
//
// Sets gain digital pot for hardware channel pHdwChannel to pValue.
//
// See sendSetPotPacket for more info.
//
// pChannel: 0-7
// pValue: 0-255
//

void sendSetGainPacket(int pHdwChannel, int pValue)
{

    sendSetPotPacket(pHdwChannel, GAIN_POT, pValue);
        
}//end of MultiIODevice::sendSetGainPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetOffsetPacket
//
// Sets offset digital pot for hardware channel pHdwChannel to pValue.
//
// See sendSetPotPacket for more info.
//
// pChannel: 0-7
// pValue: 0-255
//

void sendSetOffsetPacket(int pHdwChannel, int pValue)
{

    sendSetPotPacket(pHdwChannel, OFFSET_POT, pValue);
        
}//end of MultiIODevice::sendSetOffsetPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MultiIODevice::sendSetOnOffPacket
//
// Sends a command to the remote device to set the On/Off state for pHdwChannel
// to pValue.
//
// Sends value of 1 if state is on; value of 0 if off.
//
// The remote should return an ACK packet.
//

void sendSetOnOffPacket(int pHdwChannel, boolean pValue)
{

    sendPacket(SET_ONOFF_CMD, (byte)pHdwChannel, (byte)(pValue ? 1 : 0));
    
    numACKsExpected++;
    
}//end of MultiIODevice::sendSetOnOffPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainHandler::processChannelParameterChanges
//
// Processes any channel parameters which have been modified since the last
// time this method ran.
//
// All dirty flags are cleared as the changes are processes.
//
// NOTE: This method and processChannelParameterChanges() should only be called
// by synchronized methods so that values cannot be updated by one thread while
// another is processing all the changes. The device object's dirty flag is
// cleared after all changes handled, so no changes can be allowed during that
// process.
//

@Override
synchronized public void processChannelParameterChanges()
{

    super.processChannelParameterChanges();
    
    if(!getHdwParamsDirty()){ return; } //do nothing if no values changed

    // invoke all devices with changed values to process those changes
    
    for(Channel channel : channels){
        if (channel.getHdwParamsDirty()){ 
            if(channel.gain.isDirty()){
                sendSetGainPacket(
                           channel.getBoardChannel(), channel.gain.getValue());
            }
            if(channel.offset.isDirty()){
                sendSetOffsetPacket(
                         channel.getBoardChannel(), channel.offset.getValue());
            }
            if(channel.onOff.isDirty()){
                sendSetOnOffPacket(
                          channel.getBoardChannel(), channel.onOff.getValue());
            }                                   
        }                
    }
    
    //updates have been applied, so clear dirty flag...since this method and
    //the method which handels the updates are synchronized, no updates will
    //have occurred while all this method has processed all the updates
    
    setHdwParamsDirty(false);
    
}//end of MultiIODevice::processChannelParameterChanges
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
        
    boolean processPacket = getRunPacketFromDevice(packet);
    
    if (processPacket){
        
        //first channel's buffer location specifies start of channel data section
        int index = channels[0].getBufferLoc();

        for(Channel channel : channels){

            data = getUnsignedShortFromPacket(packet, index);
            data = Math.abs(data -= AD_ZERO_OFFSET);
            channel.catchPeak(data);
            index += 2;

        }

        if(numClockPositions > 0){ extractMapDataAndCatchPeak(packet, index); }

    }
    
    //send a request to the device for the next packet
    requestRunDataPacket();
        
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
        mapData[i] = getUnsignedByteFromPacket(pPacket, pIndex);
        mapData[i] = Math.abs(mapData[i] -= AD_ZERO_OFFSET);
        pIndex++;                
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

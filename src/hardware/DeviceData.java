/******************************************************************************
* Title: DeviceData.java
* Author: Hunter Schoonover
* Date: 09/12/17
*
* Purpose:
*
* This class is a synchronized wrapper class to handle the storage and retrieval
* of device data.
*
* The putData() and getData() functions are synchronized to ensure that the data
* in the peak, snapshot, and clock map buffers are always in sync. Data cannot
* be put one buffer while data is changing in the other.
*
*/

//-----------------------------------------------------------------------------

package hardware;

//-----------------------------------------------------------------------------
// class DeviceData
//

public class DeviceData
{

    private final Device device;

//-----------------------------------------------------------------------------
// DeviceData::DeviceData (constructor)
//

public DeviceData(Device pDevice)
{

    device = pDevice;

}//end of DeviceData::DeviceData (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DeviceData::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Do not call loadConfigSettings here...the subclasses should do it.
//

public void init()
{

}// end of DeviceData::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DeviceData::putData
//
// Puts the data retrieved from the device (channel, snapshot, map) into
// appropriate buffers.
//
// This function is synchronized so that data cannot be retrieved from one of
// these buffers while data is being put in another.
//

public synchronized void putData(int[] pChannelPeaks, int pSnapPeak,
                                    int[] pSnapData, int[] pMapData)
{

    for(Channel ch : device.getChannels()){
        ch.catchPeak(pChannelPeaks[ch.meta.channelNum]);
    }

    device.catchSnapshotPeak(pSnapPeak, pSnapData);

    device.catchMapPeak(pMapData);

}// end of DeviceData::putData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DeviceData::getData
//
// Gets the data retrieved from the device (channel, snapshot, map) from the
// appropriate buffers and returns them in the parameters.
//
// This function is synchronized so that data cannot be retrieved from one of
// these buffers while data is being put in another.
//

public synchronized boolean getData(PeakData pPeakData,
                                    PeakSnapshotData pSnapshotData,
                                    PeakMapData pMapData)
{

    boolean results = true;

    //get all channel peaks
    for (Channel ch:device.getChannels()){
        if (!ch.getPeakDataAndReset(pPeakData)) { results = false; }
    }

    if (!device.getPeakSnapshotDataAndReset(pSnapshotData)) { results = false; }

    if (!device.getPeakMapDataAndReset(pMapData)) { results = false; }

    return results;

}// end of DeviceData::getData
//-----------------------------------------------------------------------------

}//end of class DeviceData
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: GuiUpdater.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class provides updating of GUI components in a thread safe manner.
*
* A thread wishing to update a GUI component calls the synchronized addUpdate
* method to add a set of update information.
*
* The main Java thread can then call applyUpdates to apply all the updates
* stored.
*
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import java.awt.*;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class GuiUpdater
//

class GuiUpdater {

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class UpdateInfo
//

class UpdateInfo {

    JLabel label;
    Font font;
    String text;

//-----------------------------------------------------------------------------
// UpdateInfo::UpdateInfo (constructor)
//

public UpdateInfo(JLabel pLabel, Font pFont, String pText)
{

    label = pLabel; font = pFont; text = pText;

}// end of UpdateInfo::UpdateInfo (constructor)
//-----------------------------------------------------------------------------

}//end of class UpdateInfo
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

    static int UPDATE_BUFFER_SIZE = 100;

    UpdateInfo[] updateInfos;

    JFrame mainFrame;

    int nextSlotAvailable = 0;
    int nextSlotToDisplay = 0;

//-----------------------------------------------------------------------------
// GuiUpdater::GuiUpdater (constructor)
//

public GuiUpdater(JFrame pMainFrame)
{

    mainFrame = pMainFrame;

}// end of GuiUpdater::GuiUpdater (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// GuiUpdater::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    //create an array to hold a collection of update information sets
    //info sets are stored in a circular buffer fashion
    updateInfos = new UpdateInfo[UPDATE_BUFFER_SIZE];

    //null out the array
    for(int i=0; i < UPDATE_BUFFER_SIZE; i++) {updateInfos[i] = null;}

}// end of GuiUpdater::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// GuiUpdater::addUpdate
//
// This method adds a new set of update info which will later be applied to
// the GUI by the main Java thread.
//

public synchronized void addUpdate(JLabel pLabel, Font pFont, String pText)
{

    //add the info set to the array as a new object
    updateInfos[nextSlotAvailable++] = new UpdateInfo(pLabel, pFont, pText);

    //wrap around from end of array to beginning
    if (nextSlotAvailable == UPDATE_BUFFER_SIZE) {nextSlotAvailable = 0;}

    //schedule a call by the main Java thread to the GUI update method
    //all updates will be applied the next time the main thread calls
    //applyUpdates, so there may be more call requests than necessary but the
    //extra calls are harmless as applyUpdates will ignore them

    javax.swing.SwingUtilities.invokeLater(
        new Runnable() {
            @Override
            public void run() {

                applyUpdates();

            }});

}// end of GuiUpdater::addUpdate
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// GuiUpdater::applyUpdates
//
// Applies any GUI updates waiting in the buffer.  The function is passed to
// invokeLater so it will be called by the main Java thread in order to update
// GUI components in a thread safe manner.
//

public synchronized void applyUpdates()
{

    //apply all new updates in the buffer

    while (nextSlotToDisplay != nextSlotAvailable){

        Font lFont;

        //if the label pointer is not null, then that label needs to be updated
        if (updateInfos[nextSlotToDisplay].label != null){

            //only update the font if it was not passed in as null
            if ((lFont = updateInfos[nextSlotToDisplay].font) != null) {
                updateInfos[nextSlotToDisplay].label.setFont(lFont);
            }

            //update the label text
            updateInfos[nextSlotToDisplay].label.setText(
                                          updateInfos[nextSlotToDisplay].text);

        }

        //force a display refresh
        mainFrame.repaint();

        //null out the current info set to release it and move to the next
        updateInfos[nextSlotToDisplay++] = null;

        //wrap around from end of array to beginning
        if (nextSlotToDisplay == UPDATE_BUFFER_SIZE) {nextSlotToDisplay = 0;}

    }// while (nextSlotToDisplay != nextSlotAvailable)

}// end of GuiUpdater::applyUpdates
//-----------------------------------------------------------------------------


}//end of class GuiUpdater
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

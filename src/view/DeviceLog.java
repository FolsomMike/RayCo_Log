/******************************************************************************
* Title: DeviceLog.java
* Author: Mike Schoonover
* Date: 03/22/15
*
* Purpose:
*
* This class subclasses a JDialog window to provide multiple text panels to
* display logging and status info from multiple devices. Each device can be
* linked to a different text panel so the messages from each will not be
* inter-mixed.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.util.ArrayList;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class DeviceLog
//

public class DeviceLog extends JDialog{

    
    JFrame mainFrame;
    
    JPanel mainPanel, panel1, panel2;

    JPanel masterPanel = null;
    boolean masterPanelAdded = false;
    
    int numTextPanelsInPanel1 = 0, numTextPanelsInPanel2 = 0;
    
    private final ArrayList<LogPanel> logPanels = new ArrayList<>();
    public ArrayList<LogPanel> getLogPanels(){ return(logPanels); }
    
    private static final int LOG_PANEL_WIDTH = 200;
    private static final int LOG_PANEL_HEIGHT = 300;
    
    private static final int NUM_TEXT_PANELS_PER_ROW = 3;
    
//-----------------------------------------------------------------------------
// DeviceLog::DeviceLog (constructor)
//
//

public DeviceLog(JFrame pMainFrame)
{

    super(pMainFrame, "Device Logs");    
    
    mainFrame = pMainFrame;

}//end of DeviceLog::DeviceLog (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DeviceLog::init
//
// Initializes the object.  Must be called immediately after instantiation.
//
// Text panels are added for each Device, distributed amongst multiple subpanels
// on multiple rows.
//

public void init()
{

    setModal(false);

    //add a JPanel to the frame to provide a familiar container
    mainPanel = new JPanel();
    setContentPane(mainPanel);

    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
    
    panel1 = new JPanel(); mainPanel.add(panel1);
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
    panel2 = new JPanel(); mainPanel.add(panel2);
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
        
}// end of DeviceLog::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DeviceLog::addPanel
//
// Adds a text panel to the window with title pTitle.
//
// If pSetMasterPanel is true, the panel added is designated as the master
// panel. Adding a master panel is optional. If a master panel is set, the top
// row is allowed to contain one more panel -- the master panel. This avoids
// using another row or shifting a Device Log panel to the next row.
//
// Returns a reference to the new LogPanel object.
//
// NOTE: the pack() method should be called on this object after all panels
// have been added.
//

public LogPanel addPanel(String pTitle, boolean pSetMasterPanel)
{

    if ( pSetMasterPanel ) { masterPanelAdded = true; }

    int numTextPanelsPerRow1 = NUM_TEXT_PANELS_PER_ROW;
    
    if(masterPanelAdded){ numTextPanelsPerRow1++; }
    
    JPanel targetPanel;
    
    //add new panels to panel 1 until it is full, then panel 2, etc.
    
    if (numTextPanelsInPanel1 < numTextPanelsPerRow1){
        targetPanel = panel1; numTextPanelsInPanel1++;
    }else
    if (numTextPanelsInPanel2 < NUM_TEXT_PANELS_PER_ROW){
        targetPanel = panel2; numTextPanelsInPanel2++;       
    }else{
        return(null); //do nothing if all panels filled
    }
    
    LogPanel logPanel;    
    logPanel = new LogPanel(pTitle, LOG_PANEL_WIDTH, LOG_PANEL_WIDTH);
    logPanel.init(); targetPanel.add(logPanel);

    //place any empty space to the right
    targetPanel.add(Box.createHorizontalGlue());
    
    logPanels.add(logPanel);
    
    if ( pSetMasterPanel ) { masterPanel = logPanel; }
    
    return(logPanel);
    
}// end of DeviceLog::addPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// DeviceLog::removeMasterPanel
//
// If a master panel has been added, it is removed from its container and the
// masterPanelAdded flags is set false.
//

public void removeMasterPanel()
{

    if(!masterPanelAdded){ return; }
    
    masterPanelAdded = false;
    
    if (masterPanel == null){ return; }
    
    panel1.remove(masterPanel);

    masterPanel = null;
    
}// end of DeviceLog::removeMasterPanel
//-----------------------------------------------------------------------------


}//end of class DeviceLog
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

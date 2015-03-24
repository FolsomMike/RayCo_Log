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
// Returns a reference to the new LogPanel object.
//

public LogPanel addPanel(String pTitle)
{
    
    JPanel targetPanel;
    
    //add new panels to panel 1 until it is full, then panel 2, etc.
    
    if (numTextPanelsInPanel1 < NUM_TEXT_PANELS_PER_ROW){
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
    
    pack();
    
    return(logPanel);
    
}// end of DeviceLog::addPanel
//-----------------------------------------------------------------------------

}//end of class DeviceLog
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

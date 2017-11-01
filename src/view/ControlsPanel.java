/******************************************************************************
* Title: ControlsPanel.java
* Author: Hunter Schoonover
* Date: 10/31/17
*
* Purpose:
*
* This class subclasses a JPanel and is used to display various controls to
* the user. It may have static buttons that never go away, but also makes use
* of the ControlsPanel class and its children to display different control
* panels.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

//-----------------------------------------------------------------------------

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import model.SharedSettings;
import toolkit.Tools;

//-----------------------------------------------------------------------------
// class ControlsPanel
//

class ControlsPanel extends JPanel implements ActionListener, ChangeListener,
                                                MouseListener
{
    
    private final ActionListener parentActionListener;
    private final SharedSettings sharedSettings;
    
    private JPanel controlsGroupPanel;
    
    private ControlsGroup currentControlPanel;
    public int getChartGroupNum() { return currentControlPanel.getChartGroupNum(); }
    public int getChartNum() { return currentControlPanel.getChartNum(); }
    
    private ControlPanelControls controlPanelControls;
    
    private JButton scanBtn, inspectBtn, stopBtn;
    
//-----------------------------------------------------------------------------
// ControlsPanel::ControlsPanel (constructor)
//
//

public ControlsPanel(ActionListener pParentActionListener,
                        SharedSettings pSharedSettings)
{

    parentActionListener = pParentActionListener;
    
    sharedSettings = pSharedSettings;
        
}//end of ControlsPanel::ControlsPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    setupGUI();
    
    //force display of control panel controls by mimicking action performed
    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                                "Display Controls Panel"));
    
}// end of ControlsPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::setupGUI
//
// Creates and sets up GUI controls and adds them to the panel.
//

public void setupGUI()
{
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setAlignmentX(Component.LEFT_ALIGNMENT);
    setBorder(BorderFactory.createTitledBorder("Controls"));

    Tools.addVerticalSpacer(this, 10);

    add(controlsGroupPanel = new JPanel());
    controlsGroupPanel.setLayout(new BoxLayout(controlsGroupPanel, 
                                                    BoxLayout.PAGE_AXIS));
    controlsGroupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    add(Box.createVerticalGlue());
    
    add(createDisplayControlsButtonPanel());
    
    Tools.addVerticalSpacer(this, 5);

    add(createModeButtonPanel());
    
}// end of ControlsPanel::setupGUI
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::createDisplayControlsButtonPanel
//
// Returns a JPanel containing the display controls panel button.
//

private JPanel createDisplayControlsButtonPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    GUITools.setSizes(panel, 202, 30);

    JButton button;

    //add button
    button = new JButton("Controls");
    button.setActionCommand("Display Controls Panel");
    button.addActionListener(this);
    button.setToolTipText("Display Controls Panel.");
    panel.add(button);
    return(panel);

}// end of ControlsPanel::createDisplayControlsButtonPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::createModeButtonPanel
//
// Returns a JPanel containing the mode buttons, such as Scan, Inspect, Stop.
//

private JPanel createModeButtonPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    GUITools.setSizes(panel, 202, 30);

    //add button
    inspectBtn = new JButton("Inspect");
    inspectBtn.setActionCommand("Start Inspect Mode");
    inspectBtn.addActionListener(this);
    inspectBtn.setToolTipText("Start Inspect mode.");
    panel.add(inspectBtn);

    Tools.addHorizontalSpacer(panel, 3);

    //add button
    scanBtn = new JButton("Scan");
    scanBtn.setActionCommand("Start Scan Mode");
    scanBtn.addActionListener(this);
    scanBtn.setToolTipText("Start Scan mode.");
    panel.add(scanBtn);

    Tools.addHorizontalSpacer(panel, 3);

    //add button
    stopBtn = new JButton("Stop");
    stopBtn.setActionCommand("Start Stop Mode");
    stopBtn.addActionListener(this);
    stopBtn.setToolTipText("Start Stop mode.");
    panel.add(stopBtn);

    return(panel);

}// end of ControlsPanel::createModeButtonPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::refreshModeButtonPanel
//
// Refreshes all labels, spinners, buttons, etc. in the info panel based
// on settings found in SharedSettings.
//
// Spinner values are cast to a double or the float spinner will switch its 
// internal value to an integer which will cause problems later when using 
// getIntValue()
//

private void refreshModeButtonPanel()
{

    switch (sharedSettings.opMode) {
        
        case SharedSettings.INSPECT_MODE:
            //same as pause mode
            
        case SharedSettings.SCAN_MODE:
            //same as pause mode

        case SharedSettings.PAUSE_MODE:
            inspectBtn.setEnabled(false);
            scanBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            break;
            
        case SharedSettings.STOP_MODE:
            //same as default
            
        default:
            inspectBtn.setEnabled(true);
            scanBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            break;
            
    }

}// end of ControlsPanel::refreshModeButtonPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::display3DMapManipulatorControlPanel
//
// Displays a control panel for manipulating 3D maps.
//
// The chart group and chart number which sent the command to open the
// panel will be appended to pActionCommand from whence it will be extracted
// to link the panel to the appropriate chart.
//

public void display3DMapManipulatorControlPanel(int pInvokingChartGroupNum,
                                                int pInvokingChartNum,
                                                ArrayList<Object> pGraphParameters)
{

    //remove any panels already opened
    controlsGroupPanel.removeAll();

    Map3DManipulator map3DManip = new Map3DManipulator(
                                pInvokingChartGroupNum, pInvokingChartNum, this);
    map3DManip.init();
    controlsGroupPanel.add(map3DManip);
    map3DManip.setAlignmentX(Component.LEFT_ALIGNMENT);
    currentControlPanel = map3DManip;

    currentControlPanel.setAllValues(pGraphParameters);

    ((TitledBorder)(getBorder())).setTitle(map3DManip.getPanelTitle());

    invalidate(); repaint();

}//end of ControlsPanel::display3DMapManipulatorControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::displayCalibrationPanel
//
// Displays a calibration panel appropriate for pChartNum of pChartGroupNum with
// name of pPanelName with all channels in pChannelList displayed on the panel.
//
// The ChannelInfo objects in list pChannelList provide the necessary
// information to link the GUI controls to the channels and traces.
//

public void displayCalibrationPanel(int pChartGroupNum, int pChartNum,
                        String pPanelTitle, ArrayList<ChannelInfo> pChannelList,
                        LinkedHashSet<String> pGroupTitles, 
                        ArrayList<Threshold[]> pThresholds,
                        ArrayList<Object> pGraphParameters)
{

    //remove any panels already opened
    controlsGroupPanel.removeAll();

    int mapGraphNumber = 0; //graph of 3D map always expected to be first

    ControlPanelBasic transCalPanel = new ControlPanelBasic(pChartGroupNum,
                                            pChartNum, pPanelTitle, pGroupTitles,
                                            pChannelList, pThresholds, this);
    transCalPanel.init();
    
    controlsGroupPanel.add(transCalPanel);
    transCalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    currentControlPanel = transCalPanel;

    currentControlPanel.setAllValues(pGraphParameters);

    ((TitledBorder)(getBorder())).setTitle(transCalPanel.getPanelTitle());

    invalidate(); repaint();

}//end of ControlsPanel::displayCalibrationPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::displayControlsPanel
//
// Displays the controls panel, which contains job #, scan speed, etc.
//

public void displayControlsPanel()
{
    
    //this one is never thrown away because it contains the most important
    //-1 because we don't care about group or chart nums
    if (controlPanelControls == null) {
        controlPanelControls = new ControlPanelControls(-1, -1, this,
                                                            sharedSettings);
        controlPanelControls.init();
        controlPanelControls.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    //remove any panels already opened
    controlsGroupPanel.removeAll();

    controlsGroupPanel.add(controlPanelControls);
    currentControlPanel = controlPanelControls;

    ((TitledBorder)(getBorder())).setTitle(controlPanelControls.getPanelTitle());

    invalidate(); repaint();

}//end of ControlsPanel::displayControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::getAllValuesFromCurrentControlPanel
//

public ArrayList<Object> getAllValuesFromCurrentControlPanel()
{

    return currentControlPanel.getAllValues();

}// end of ControlsPanel::getAllValuesFromCurrentControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::setAllValuesInCurrentControlPanel
//

public void setAllValuesInCurrentControlPanel(
                                        ArrayList<Object> pGraphParameters)
{

    currentControlPanel.setAllValues(pGraphParameters);

}// end of ControlsPanel::setAllValuesInCurrentControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::refresh
//

public void refresh()
{

    currentControlPanel.refresh();
    
    //always force refresh of control panel controls
    controlPanelControls.refresh();
    
    //make sure proper buttons are enabled and disabled
    refreshModeButtonPanel();

}// end of ControlsPanel::refresh
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::actionPerformed
//
// Responds to events and passes them on to the parent actionListener object.
//

@Override
public void actionPerformed(ActionEvent e)
{

    actionPerformedLocal(e); //local processing

    parentActionListener.actionPerformed(e); //parent handler processing

}//end of ControlsPanel::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanel::actionPerformedLocal
//
// Responds to events which require action by this object.
//

private void actionPerformedLocal(ActionEvent e)
{

}//end of ControlsPanel::actionPerformedLocal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlsPanelBasic::(various abstract functions)
//
// These functions are implemented per requirements of implemented interfaces.
//

@Override
public void stateChanged(ChangeEvent e) {}

@Override
public void mouseClicked(MouseEvent e) {}

@Override
public void mousePressed(MouseEvent e) {}

@Override
public void mouseReleased(MouseEvent e) {}

@Override
public void mouseEntered(MouseEvent e) {}

@Override
public void mouseExited(MouseEvent e) {}

//end of ControlsPanelBasic::(various abstract functions)
//-----------------------------------------------------------------------------

}//end of class ControlsPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
    
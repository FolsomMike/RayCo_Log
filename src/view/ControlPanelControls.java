/******************************************************************************
* Title: ControlPanelControls.java
* Author: Hunter Schoonover
* Date: 10/28/17
*
* Purpose:
*
* This class subclasses ControlPanel to display controls for adjusting piece
* number, calibration mode, scan speed, etc.
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import mksystems.mswing.MFloatSpinner;
import model.SharedSettings;
import static view.ControlPanelControls.addHorizontalSpacer;
import static view.ControlPanelControls.addVerticalSpacer;

//-----------------------------------------------------------------------------
// class ControlPanelControls
//

class ControlPanelControls extends ControlPanel 
                                    implements ActionListener, ChangeListener
{
    
    private final SharedSettings sharedSettings;
    
    //manual control panel
    private JPanel manualControlPanel;
    private JLabel calModeWarning;
    private JButton nextPieceButton;
    private JButton pauseResumeButton;
    public JButton getNextPieceButton() { return nextPieceButton; }
    public JButton getPauseResumeButton() { return pauseResumeButton; }
    
    //scan speed panel
    private MFloatSpinner scanSpeedEditor;
    
    //status panel
    private MFloatSpinner pieceNumberEditor;
    private JCheckBox calModeCheckBox;
    public void setCalModeCBEnabled(boolean pE) { calModeCheckBox.setEnabled(pE); }
    
//-----------------------------------------------------------------------------
// ControlPanelControls::ControlPanelControls (constructor)
//
//

public ControlPanelControls(int pChartGroupNum, int pChartNum, 
                                ActionListener pParentActionListener,
                                SharedSettings pSettings)
{

    super(pChartGroupNum, pChartNum, pParentActionListener);
    
    panelTitle = "Controls";
    
    sharedSettings = pSettings;
    
}//end of ControlPanelControls::ControlPanelControls (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{
    
    super.init();

    setupGUI();
    
    refresh(); //make sure all values are correct

}// end of ControlPanelControls::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::setupGUI
//
// Creates and sets up GUI controls and adds them to the panel.
//

public void setupGUI()
{
    
    removeAll();//just to be safe

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setAlignmentX(Component.LEFT_ALIGNMENT);
    
    add(createInfoPanel());
    
    addVerticalSpacer(this, 10);
    
    add(createStatusPanel());
    
    addVerticalSpacer(this, 10);
    
    add(createScanSpeedPanel());
    
    createManualControlPanel();

}// end of ControlPanelControls::setupGUI
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::createInfoPanel
//
// Returns a JPanel displaying misc information, such as job #.
//

private JPanel createInfoPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Info"));
    GUITools.setSizes(panel, 202, 50);
    
    addHorizontalSpacer(this, 10);

    //job #/name
    JLabel jobLabel = new JLabel(" Job #: ");
    panel.add(jobLabel);
    JLabel jobValue = new JLabel(sharedSettings.currentJobName);
    panel.add(jobValue);

    return(panel);

}// end of ControlPanelControls::createInfoPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::refreshInfoPanel
//
// Refreshes all labels, spinners, buttons, etc. in the info panel based
// on settings found in SharedSettings.
//
// Spinner values are cast to a double or the float spinner will switch its 
// internal value to an integer which will cause problems later when using 
// getIntValue()
//

public void refreshInfoPanel()
{

    //nothing should need to be refreshed. job name should always be the same

}// end of ControlPanelControls::refreshInfoPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::createManualControlPanel
//
// Adds a JPanel with controls and displays for manual control of the 
// inspection process.  This is mainly used with handheld crabs and other 
// systems without encoders or photoeyes.
//
// Only adds if in timer driven tracking mode.
//

private void createManualControlPanel()
{
    
    //only create manual control panel if necessary
    if (manualControlPanel != null || (!sharedSettings.timerDrivenTracking 
           && !sharedSettings.timerDrivenTrackingInCalMode)) 
    {
        return;
    }
     
    addVerticalSpacer(this, 10);

    manualControlPanel = new JPanel();
    manualControlPanel.setLayout(new BoxLayout(manualControlPanel, BoxLayout.Y_AXIS));
    manualControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    manualControlPanel.setBorder(BorderFactory.createTitledBorder("Manual Inspection"));
    GUITools.setSizes(manualControlPanel, 202, 80);
    
    addVerticalSpacer(manualControlPanel, 5);
    
    //warning goes above buttons
    manualControlPanel.add(calModeWarning = new JLabel("Cal Mode", warningSymbol, 
                                            JLabel.LEADING));
    calModeWarning.setVisible(false); //starts out invisible
    
    addVerticalSpacer(manualControlPanel, 5);
    
    //panel so buttons can be displayed horizontally
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
    buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    manualControlPanel.add(buttonsPanel);
    
    //add button
    nextPieceButton = new JButton("Next Run");
    nextPieceButton.setActionCommand("Next Run");
    nextPieceButton.addActionListener(this);
    nextPieceButton.setToolTipText("Begins inspection of next "
                                    + sharedSettings.pieceDescriptionLC + ".");
    nextPieceButton.setEnabled(false); //starts out invisible
    buttonsPanel.add(nextPieceButton);

    addHorizontalSpacer(buttonsPanel, 5);

    //add button
    pauseResumeButton = new JButton("Pause");
    pauseResumeButton.setActionCommand(pauseResumeButton.getText());
    pauseResumeButton.addActionListener(this);
    pauseResumeButton.setToolTipText("Pauses the inspection without moving to"
                                    + " next " 
                                    + sharedSettings.pieceDescriptionLC + ".");
    pauseResumeButton.setEnabled(false); //starts out invisible
    buttonsPanel.add(pauseResumeButton);

    add(manualControlPanel);
    
    //default to disabled
    setManualControlPanelEnabled(false);

}// end of ControlPanelControls::createManualControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::refreshManualControlPanel
//
// Refreshes all labels, spinners, buttons, etc. in the manual control panel 
// based on settings found in SharedSettings.
//
// If the manual control panel is not yet created/visible, this function will
// create it if it should be.
//
// Spinner values are cast to a double or the float spinner will switch its 
// internal value to an integer which will cause problems later when using 
// getIntValue()
//

public void refreshManualControlPanel()
{

    createManualControlPanel(); //create control panel if necessary
    
    if (manualControlPanel==null) { return; } //do nothing else if not created
    
    //set buttons enabled and pause/resume text appropriately
    switch (sharedSettings.opMode) {
        
        case SharedSettings.INSPECT_MODE:
            setManualControlPanelEnabled(false);
            break;
            
        case SharedSettings.INSPECT_WITH_TIMER_TRACKING_MODE:
            setManualControlPanelEnabled(true);
            pauseResumeButton.setText("Pause");
            break;
            
        case SharedSettings.SCAN_MODE:
            setManualControlPanelEnabled(false);
            break;
            
        case SharedSettings.PAUSE_MODE:
            setManualControlPanelEnabled(true);
            pauseResumeButton.setText("Resume");
            break;
            
        case SharedSettings.STOP_MODE:
            setManualControlPanelEnabled(false);
            break;
        
        default:
            setManualControlPanelEnabled(true);
            break;
            
    }
    
    //set action command to text of button
    pauseResumeButton.setActionCommand(pauseResumeButton.getText());
    
    //display cal mode warning if necessary
    calModeWarning.setVisible(sharedSettings.calMode);
    
}// end of ControlPanelControls::refreshManualControlPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::setManualControlPanelEnabled
//
// Set all controls enabled or enabled.
//

public void setManualControlPanelEnabled(boolean pState)
{

    //hide everything
    manualControlPanel.setVisible(pState);
    
    //disable buttons for good measure
    pauseResumeButton.setEnabled(pState);
    nextPieceButton.setEnabled(pState);
    
}//end of ControlPanelControls::setManualControlPanelEnabled
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::createScanSpeedPanel
//
// Returns a JPanel allowing adjustment of the chart scan speed.
//

private JPanel createScanSpeedPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Scan Speed"));
    GUITools.setSizes(panel, 202, 50);
    
    addHorizontalSpacer(panel, 10);
    
    scanSpeedEditor = new MFloatSpinner(sharedSettings.scanSpeed, 1, 10, 1,
                                            "##0", 60, -1);
    scanSpeedEditor.setName("Scan Speed Spinner");
    scanSpeedEditor.addChangeListener(this);
    setSpinnerNameAndMouseListener(scanSpeedEditor, scanSpeedEditor.getName(), this);
    scanSpeedEditor.setToolTipText("Scanning & Inspecting Speed");
    panel.add(scanSpeedEditor);

    return(panel);

}// end of ControlPanelControls::createScanSpeedPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::refreshScanSpeedPanel
//
// Refreshes all labels, spinners, buttons, etc. in the scan speed panel based
// on settings found in SharedSettings.
//
// Spinner values are cast to a double or the float spinner will switch its 
// internal value to an integer which will cause problems later when using 
// getIntValue()
//

public void refreshScanSpeedPanel()
{

    scanSpeedEditor.setValue((double)sharedSettings.scanSpeed);

}// end of ControlPanelControls::refreshScanSpeedPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::createStatusPanel
//
// Returns a JPanel containing the status info, such as piece number and 
// cal mode checkbox.
//

private JPanel createStatusPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Status"));
    GUITools.setSizes(panel, 202, 50);
    
    addHorizontalSpacer(panel, 10);

    //spinner to change piece number
    pieceNumberEditor = new MFloatSpinner(1, 1, 100000, 1, "##0", 60, -1);
    pieceNumberEditor.setName("Piece Number Spinner");
    pieceNumberEditor.addChangeListener(this);
    setSpinnerNameAndMouseListener(pieceNumberEditor, 
                                        pieceNumberEditor.getName(), this);
    pieceNumberEditor.setToolTipText("The next " 
                                        + sharedSettings.pieceDescription 
                                        + " number.");
    panel.add(pieceNumberEditor);
    
    addHorizontalSpacer(panel, 10);

    //checkbox to indicate whether or not in cal mode
    calModeCheckBox = new JCheckBox("Cal Mode");
    calModeCheckBox.setSelected(false);
    calModeCheckBox.setActionCommand("Calibration Mode Checkbox");
    calModeCheckBox.addActionListener(this);
    calModeCheckBox.addMouseListener(this);
    calModeCheckBox.setToolTipText(
                "Check this box to run and save calibration " 
                        + sharedSettings.pieceDescriptionPluralLC + ".");
    panel.add(calModeCheckBox);

    return(panel);

}// end of ControlPanelControls::createStatusPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::refreshStatusPanel
//
// Refreshes all labels, spinners, buttons, etc. in the status panel based on
// settings found in Shared Settings.
//
// Spinner values are cast to a double or the float spinner will switch its 
// internal value to an integer which will cause problems later when using 
// getIntValue()
//

public void refreshStatusPanel()
{

    //display either the cal piece number or normal piece number
    if (sharedSettings.calMode) {
        pieceNumberEditor.setValue((double)sharedSettings.nextCalPieceNumber);
    } 
    else { 
        pieceNumberEditor.setValue((double)sharedSettings.nextPieceNumber);
    }

}// end of ControlPanelControls::refreshStatusPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::refresh
//
// Checks SharedSettings for any value changes and adjusts the GUI accordingly.
//
// Spinner values are cast to a double or the float spinner will switch its 
// internal value to an integer which will cause problems later when using 
// getIntValue()
//

@Override
public void refresh()
{
    
    refreshInfoPanel();
    refreshManualControlPanel();
    refreshScanSpeedPanel();
    refreshStatusPanel();

}//end of ControlPanelControls::setCalModeEnabled
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::actionPerformedLocal
//
// Responds to events which require action by this object.
//
// Returns true if the action is not to be handled by other listeners.
//

@Override
protected boolean actionPerformedLocal(ActionEvent e)
{

    if (e.getActionCommand().equals("Calibration Mode Checkbox")){

        if (!(e.getSource() instanceof JCheckBox)) { return(false); }
        JCheckBox box = (JCheckBox)e.getSource();
        
        //tell parent listeners that calibration mode has changed
        parentActionListener.actionPerformed(new ActionEvent(this, 1,
                                            "Calibration Mode,"
                                            + box.isSelected()));
        
        return true;
    }

    return(false);

}//end of ControlPanelControls::actionPerformedLocal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::handleSpinnerChange
//
// Processes changes to Spinner values.
//
// Should be overridden by child classes for specific handling.
//

@Override
public void handleSpinnerChange(MFloatSpinner pSpinner)
{
    
    if (pSpinner == null) { return; }

    String name = pSpinner.getName();

    if(name == null){ return; }

    //update SharedSettings with value from spinner
    if ("Scan Speed Spinner".equals(name)){
        
        sharedSettings.scanSpeed = scanSpeedEditor.getIntValue();
        
    }
    
    //update SharedSettings with value from spinner
    else if ("Piece Number Spinner".equals(name)){
        
        int val = pieceNumberEditor.getIntValue();
        if (sharedSettings.calMode) { sharedSettings.nextCalPieceNumber = val; }
        else { sharedSettings.nextPieceNumber = val; }
        
    }

}//end of ControlPanelControls::handleSpinnerChange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::addHorizontalSpacer
//
// Adds a horizontal spacer of pNumPixels width to JPanel pTarget.
//

public static void addHorizontalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(pNumPixels,0)));

}// end of ControlPanelControls::addHorizontalSpacer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::addVerticalSpacer
//
// Adds a vertical spacer of pNumPixels height to JPanel pTarget.
//

public static void addVerticalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(0,pNumPixels)));

}// end of ControlPanelControls::addVerticalSpacer
//-----------------------------------------------------------------------------

}//end of class ControlPanelControls
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
    
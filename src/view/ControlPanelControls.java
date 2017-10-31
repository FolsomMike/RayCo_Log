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
    private boolean manualControlPanelVisible;
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
    
    manualControlPanelVisible = false;
    
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
    
    //only display manual control panel if necessary
    if (sharedSettings.timerDrivenTracking 
            || sharedSettings.timerDrivenTrackingInCalMode) {
     
        addVerticalSpacer(this, 10);
    
        add(createManualControlPanel());
        
        //if using timer driven for cal mode only, start with panel disabled
        if(sharedSettings.timerDrivenTrackingInCalMode){
            setManualControlPanelEnabled(false);
        }
        
    }

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
// ControlPanelControls::createManualControlPanel
//
// Returns a JPanel with controls and displays for manual control of the 
// inspection process.  This is mainly used with handheld crabs and other 
// systems without encoders or photoeyes.
//

private JPanel createManualControlPanel()
{
    
    manualControlPanelVisible = true;

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Manual Inspection"));
    GUITools.setSizes(panel, 202, 80);
    
    addVerticalSpacer(panel, 5);
    
    //warning goes above buttons
    panel.add(calModeWarning = new JLabel("Cal Mode", warningSymbol, 
                                            JLabel.LEADING));
    calModeWarning.setVisible(false); //starts out invisible
    
    addVerticalSpacer(panel, 5);
    
    //panel so buttons can be displayed horizontally
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
    buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(buttonsPanel);
    
    //add button
    nextPieceButton = new JButton("Next Run");
    nextPieceButton.setActionCommand("Next Run");
    nextPieceButton.addActionListener(this);
    nextPieceButton.setToolTipText("Begins inspection of next piece.");
    nextPieceButton.setEnabled(false); //starts out invisible
    buttonsPanel.add(nextPieceButton);

    addHorizontalSpacer(buttonsPanel, 5);

    //add button
    pauseResumeButton = new JButton("Pause");
    pauseResumeButton.setActionCommand("Pause or Resume");
    pauseResumeButton.addActionListener(this);
    pauseResumeButton.setToolTipText("Pauses the inspection without moving to next piece.");
    pauseResumeButton.setEnabled(false); //starts out invisible
    buttonsPanel.add(pauseResumeButton);

    return(panel);

}// end of ControlPanelControls::createManualControlPanel
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
    pieceNumberEditor.addChangeListener(this);
    pieceNumberEditor.setToolTipText("The next piece number."); //DEBUG HSS// change piece type to be read from inifile
    panel.add(pieceNumberEditor);
    
    addHorizontalSpacer(panel, 10);

    //checkbox to indicate whether or not in cal mode
    calModeCheckBox = new JCheckBox("Cal Mode");
    calModeCheckBox.setSelected(false);
    calModeCheckBox.setActionCommand("Calibration Mode Checkbox");
    calModeCheckBox.addActionListener(this);
    calModeCheckBox.addMouseListener(this);
    calModeCheckBox.setToolTipText(
                "Check this box to run and save calibration pieces"); //DEBUG HSS// change piece type to be read from inifile
    panel.add(calModeCheckBox);

    return(panel);

}// end of ControlPanelControls::createStatusPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::refresh
//
// Checks SharedSettings for any value changes and adjusts the GUI accordingly.
//

@Override
public void refresh()
{

    //easiest way to do this is to reset the gui
    setupGUI();

}//end of ControlPanelControls::setCalModeEnabled
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::setCalModeEnabled
//
// Sets the cal mode in SharedSettings to pEnabled. If enabled, cal mode
// warning label is displayed.
//

protected void setCalModeEnabled(boolean pEnabled)
{

    sharedSettings.calMode = pEnabled;
    
    if (manualControlPanelVisible){ calModeWarning.setVisible(pEnabled); }

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
        
        //set cal mode to state of checkbox
        setCalModeEnabled(box.isSelected());
        
        return(true);
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

    //put spinner value in scan speed spinner
    if (name.equals("Scan Speed Spinner")){
        
        sharedSettings.scanSpeed = scanSpeedEditor.getIntValue();
        
    }

}//end of ControlPanelControls::handleSpinnerChange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelControls::setManualControlPanelEnabled
//
// Set all controls enabled or enabled.
//

public void setManualControlPanelEnabled(boolean pFalse)
{

    pauseResumeButton.setEnabled(pFalse);
    nextPieceButton.setEnabled(pFalse);
    
}//end of ControlPanelControls::setManualControlPanelEnabled
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
    
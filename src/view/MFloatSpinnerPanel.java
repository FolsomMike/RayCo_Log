/******************************************************************************
* Title: MFloatSpinnerPanel.java
* Author: Mike Schoonover
* Date: 03/10/15
*
* Purpose:
*
* This class subclasses a JPanel to display an MFloatSpinner along with a
* label.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import static java.awt.event.ActionEvent.ACTION_PERFORMED;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mksystems.mswing.MFloatSpinner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MFloatSpinnerPanel
//

class MFloatSpinnerPanel extends JPanel implements ChangeListener
{

    
    MFloatSpinner spinner;
    String name;
    String toolTip;
    ActionListener actionListener;
    double value, min, max, increment;
    String formatPattern;
    int spinnerWidth, spinnerHeight;
    String label1Text, label2Text, actionCommand;
    int panelWidth, panelHeight;

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::MFloatSpinnerPanel (constructor)
//
//

public MFloatSpinnerPanel(String pName, String pToolTip,
      ActionListener pActionListener, double pValue, double pMin, double pMax,
             double pIncrement, String pFormatPattern, 
             int pSpinnerWidth, int pSpinnerHeight, String pLabel1Text,
             String pLabel2Text, String pActionCommand, int pPanelWidth,
             int pPanelHeight)
{

    name = pName;
    toolTip = pToolTip;
    actionListener = pActionListener;
    value = pValue; min = pMin; max = pMax; increment = pIncrement;
    formatPattern = pFormatPattern;
    spinnerWidth = pSpinnerWidth; spinnerHeight = pSpinnerHeight;
    label1Text = pLabel1Text; label2Text = pLabel2Text;
    actionCommand = pActionCommand;
    panelWidth = pPanelWidth; panelHeight = pPanelHeight;

}//end of MFloatSpinnerPanel::MFloatSpinnerPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    setSizes(this, panelWidth, panelHeight);

    add(new JLabel(label1Text));

    addHorizontalSpacer(this, 3);
    
    spinner = new MFloatSpinner(
       value, min, max, increment, formatPattern, spinnerWidth, spinnerHeight);
        
    spinner.setName(name);
    spinner.addChangeListener(this);
    spinner.setToolTipText(toolTip);
    add(spinner);
   
    addHorizontalSpacer(this, 3);
    
    add(new JLabel(label2Text));    
    
}// end of MFloatSpinnerPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::stateChanged
//
// Catch all control changes such as those from MFloatSpinners.
//
// It is not necessary to know which object changed; this class simply
// sends an action command upstream and the upstream objects handle that
// action by re-processing the data from all controls.
//

@Override
public void stateChanged(ChangeEvent ce) {

    actionListener.actionPerformed(new ActionEvent(this, ACTION_PERFORMED,
                                                               actionCommand));
    
}//end of MFloatSpinnerPanel::stateChanged
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::getIntValue
//
// Returns the current value in the spinner as an integer.
//

public int getIntValue()
{

   return(spinner.getIntValue());
    
}// end of MFloatSpinnerPanel::getIntValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::getDoubleValue
//
// Returns the current value in the spinner as an double.
//

public double getDoubleValue()
{

   return(spinner.getDoubleValue());
    
}// end of MFloatSpinnerPanel::getDoubleValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of MFloatSpinnerPanel::setSizes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MFloatSpinnerPanel::addHorizontalSpacer
//
// Adds a horizontal spacer of pNumPixels width to JPanel pTarget.
//

private void addHorizontalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(pNumPixels,0)));
    
}// end of MFloatSpinnerPanel::addHorizontalSpacer
//-----------------------------------------------------------------------------


}//end of class MFloatSpinnerPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

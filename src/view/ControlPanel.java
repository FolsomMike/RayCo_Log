/******************************************************************************
* Title: ControlPanel.java
* Author: Mike Schoonover
* Date: 03/15/15
*
* Purpose:
*
* This class subclasses a JPanel and is the parent class for classes which
* display and handle controls.
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
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mksystems.mswing.MFloatSpinner;

//-----------------------------------------------------------------------------
// class ControlPanel
//

class ControlPanel extends JPanel implements ControlsGroup, ActionListener, 
                                        ChangeListener, MouseListener
{
    
    String panelTitle = "";
    String getPanelTitle(){ return(panelTitle); }
    
    //specifies which chart the panel is connected to
    protected final int chartGroupNum;
    @Override
    public int getChartGroupNum(){ return(chartGroupNum); }
    protected final int chartNum;
    @Override
    public int getChartNum(){ return(chartNum); }

    ActionListener parentActionListener;    
    
    protected ImageIcon warningSymbol;
    
//-----------------------------------------------------------------------------
// ControlPanel::ControlPanel (constructor)
//
//

public ControlPanel(int pChartGroupNum, int pChartNum, 
                                          ActionListener pParentActionListener)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    parentActionListener = pParentActionListener;
        
}//end of ControlPanel::ControlPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    //NOTE: You must use forward slashes in the path names for the resource
    //loader to find the image files in the JAR package.
    warningSymbol = createImageIcon("images/windows-warning.gif");
    
}// end of ControlPanel::init
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// MainView::getAllValues
//
// 

@Override
public ArrayList<Object> getAllValues()
{

    return(null);
    
}// end of MainView::getAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setAllValues
//
// 

@Override
public void setAllValues(ArrayList<Object> pValues)
{
    
}// end of MainView::setAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::actionPerformed
//
// Responds to events and passes them on to the parent actionListener object.
//

@Override
public void actionPerformed(ActionEvent e)
{

    if (actionPerformedLocal(e) == true) { return; } //local processing

    parentActionListener.actionPerformed(e); //parent handler processing

}//end of ControlPanel::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::actionPerformedLocal
//
// Responds to events which require action by this object.
//
// Generally overridden by children classes to provide special handling.
//

protected boolean actionPerformedLocal(ActionEvent e)
{

    return false;

}//end of ControlPanel::actionPerformedLocal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::stateChanged
//
// Responds to value changes in spinners, etc.
//
// You can tell which item was changed by using similar to:
//
// Object source = e.getSource();
//

@Override
public void stateChanged(ChangeEvent e)
{

    //if for some reason the object which changed state is not a subclass of
    //of Component, do nothing as this code only handles Components

    if (!(e.getSource() instanceof Component)) { return; }

    MFloatSpinner sp;

    //try casting the source component to a spinner - if valid, then get the
    //name if the component is a threshold control, then update chart
    //settings only

    try{
        sp = (MFloatSpinner)e.getSource();
        handleSpinnerChange(sp);
        return;
    }
    catch (ClassCastException ce){
        //this is an expected exception -- do not print warning to err file
    }

    //all other components which fire stateChanged events call to copy their
    //values to the appropriate variables
    //do something here with all uncaught changes

    return;

}//end of ControlPanel::stateChanged
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::handleSpinnerChange
//
// Processes changes to Spinner values.
//
// Should be overridden by child classes for specific handling.
//

public void handleSpinnerChange(MFloatSpinner pSpinner)
{

}//end of ControlPanel::handleSpinnerChange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::setSpinnerNameAndMouseListener
//
// A mouse listener cannot be added directly to a JSpinner or its sub-classes.
// The listener must be added to the text field inside the spinner to work
// properly.  This function also sets the name of the text field so that the
// mouse listener response method can use it.
//

void setSpinnerNameAndMouseListener(JSpinner pSpinner, String pName,
                                                   MouseListener pMouseListener)
{

    for (Component child : pSpinner.getComponents()) {

        if (child instanceof JSpinner.NumberEditor) {
            for (Component child2 :
                  ((javax.swing.JSpinner.NumberEditor) child).getComponents()){
                ((javax.swing.JFormattedTextField) child2).
                                               addMouseListener(pMouseListener);
                                ((javax.swing.JFormattedTextField) child2).
                                                                setName(pName);
            }
        }
    }

}//end of ControlPanel::setSpinnerNameAndMouseListener
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::createImageIcon
//
// Returns an ImageIcon, or null if the path was invalid.
//
// ***************************************************************************
// NOTE: You must use forward slashes in the path names for the resource
// loader to find the image files in the JAR package.
// ***************************************************************************
//

public static ImageIcon createImageIcon(String path)
{
    
    // have to use the MainView class because it is located in the same 
    // package as the file; specifying the class specifies the first 
    // portion of the path to the image, this concatenated with the pPath
    java.net.URL imgURL = MainView.class.getResource(path);

    if (imgURL != null) {
        return new ImageIcon(imgURL);
    }
    else {return null;}

}//end of ControlPanel::createImageIcon
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::(various abstract functions)
//
// These functions are implemented per requirements of implemented interfaces.
//

@Override
public void refresh() {}

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

//end of ControlPanelBasic::(various abstract functions)
//-----------------------------------------------------------------------------

}//end of class ControlPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
    
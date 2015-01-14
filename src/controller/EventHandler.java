/******************************************************************************
* Title: EventHandler.java
* Author: Mike Schoonover
* Date: 12/30/13
*
* Purpose:
*
* This Interface provides methods used to transfer event handling information
* between objects.
*
* It uses methods based on Java ActionListener, WindowListener, and other
* similar classes for the sake of clarity.
*
*/

//-----------------------------------------------------------------------------

package controller;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import javax.swing.event.ChangeEvent;

public interface EventHandler {

    public void actionPerformed(ActionEvent e);
    
    public void stateChanged(ChangeEvent ce);

    public void windowClosing(WindowEvent e);
    public void windowActivated(WindowEvent e);
    public void windowDeactivated(WindowEvent e);
    public void windowOpened(WindowEvent e);
    public void windowClosed(WindowEvent e);
    public void windowIconified(WindowEvent e);
    public void windowDeiconified(WindowEvent e);

}

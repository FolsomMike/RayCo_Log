/******************************************************************************
* Title: Tools.java
* Author: Mike Schoonover
* Date: 9/30/13
*
* Purpose:
*
* This class contains useful tools.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package toolkit;

//-----------------------------------------------------------------------------

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//

public class Tools extends Object{


//-----------------------------------------------------------------------------
// Tools::Toolls (constructor)
//

public Tools()
{


}//end of Tools::Tools (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Tools::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//

public void init()
{


}//end of Tools::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Tools::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

static public void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of Tools::setSizes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Tools::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

static public void displayErrorMessage(String pMessage, JFrame pMainFrame)
{

    JOptionPane.showMessageDialog(pMainFrame, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of Tools::displayErrorMessage
//-----------------------------------------------------------------------------


}//end of class Tools
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

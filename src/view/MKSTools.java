/******************************************************************************
* Title: MKSTools.java
* Author: Mike Schoonover
* Date: 01/14/15
*
* Purpose:
*
* This class contains useful tools used by various classes.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

//-----------------------------------------------------------------------------

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//-----------------------------------------------------------------------------
// class MKSTools
//

public class MKSTools{

    
//-----------------------------------------------------------------------------
// MKSTools::MKSTools (constructor)
//
//

public MKSTools()
{
    
}//end of MKSTools::MKSTools (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MKSTools::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init(JFrame pMainFrame)
{

    
}// end of MKSTools::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MKSTools::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

public static void logSevere(String pClassAndMethod, String pMessage)
{

    Logger.getLogger(pClassAndMethod).log(Level.SEVERE, pMessage);

}//end of MKSTools::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MKSTools::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of MKSTools::logStackTrace
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MKSTools::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

private void displayErrorMessage(JFrame pFrame, String pMessage)
{

    JOptionPane.showMessageDialog(pFrame, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of MKSTools::displayErrorMessage
//-----------------------------------------------------------------------------

}//end of class MKSTools
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

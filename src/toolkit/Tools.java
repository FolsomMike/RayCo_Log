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
import view.Xfer;

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
// Tools::matchAndParseInt
//
// Determines if the key in pString matches pKey and parses the value to an
// integer.  If the value is invalid, pDefault will be returned.
//
// The function returns true if the value matches pKey.
//
// The value and result flags are returned via pMatchVars.
//

static public boolean matchAndParseInt(String pString, String pKey,
                                            int pDefault, Xfer pMatchVars)
{

    //remove whitespace & force upper case
    String ucString = pString.trim().toUpperCase();

    //if the string does not start with the key, return default value
    if (!ucString.startsWith(pKey.toUpperCase())) {
        pMatchVars.rInt1 = pDefault;
        return(false); //does not match
        }

    int indexOfEqual;

    //look for '=' symbol, if not found then return default
    if ( (indexOfEqual = pString.indexOf("=")) == -1) {
        pMatchVars.rInt1 = pDefault;
        return(true); //key matched but parse was invalid
        }

    //return the part of the line after the '=' sign - on error return default
    try{
        pMatchVars.rString1 = pString.substring(indexOfEqual + 1);
        pMatchVars.rInt1 = Integer.parseInt(pMatchVars.rString1);
        return(true); //key matched, parse valid
        }
    catch(NumberFormatException e){
        pMatchVars.rInt1 = pDefault;
        return(true); //key matched but parse was invalid
        }

}//end of Tools::matchAndParseInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Tools::matchAndParseString
//
// Determines if the key in pString matches pKey and parses the value to a
// string.  If the value is invalid, pDefault will be returned.  If the
// value is blank, pDefault will be returned.
//
// Note: this function can also be used to determine if a string contains
//  pKey regardless of whether a value exists, thus it can be used to search
//  for a section tag such as "[section]".
//
// The function returns true if the value matches pKey.
//
// The value and result flags are returned via pMatchVars.
//

static public boolean matchAndParseString(String pString, String pKey,
                                          String pDefault, Xfer pMatchVars)
{

    //remove whitespace & force upper case
    String ucString = pString.trim().toUpperCase();

    //if the string does not start with the key, return default value
    if (!ucString.startsWith(pKey.toUpperCase())) {
        pMatchVars.rString1 = pDefault;
        return(false); //does not match
        }

    int indexOfEqual;

    //look for '=' symbol, if not found then return default
    if ( (indexOfEqual = pString.indexOf("=")) == -1) {
        pMatchVars.rString1 = pDefault;
        return(true); //key matched but parse was invalid
        }

    //return the part of the line after the '=' sign - on error return default
    try{
        pMatchVars.rString1 = pString.substring(indexOfEqual + 1);
        if (pMatchVars.rString1.equals("")) {pMatchVars.rString1 = pDefault;}
        return(true); //key matched, parse valid
        }
    catch(StringIndexOutOfBoundsException e){
        pMatchVars.rString1 = pDefault;
        return(true); //key matched but parse was invalid
        }

}//end of Tools::matchAndParseString
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Tools::matchAndParseString
//
// Determines if the key in pString matches pKey and parses the value to a
// boolean.  If the value is invalid, pDefault will be returned.  If the
// value is blank, pDefault will be returned.
//
// Note: this function can also be used to determine if a string contains
//  pKey regardless of whether a value exists, thus it can be used to search
//  for a section tag such as "[section]".
//
// The function returns true if the value matches pKey.
//
// The value and result flags are returned via pMatchVars.
//

static public boolean matchAndParseBoolean(String pString, String pKey,
                                             Boolean pDefault, Xfer pMatchVars)
{

    //remove whitespace & force upper case
    String ucString = pString.trim().toUpperCase();

    //if the string does not start with the key, return default value
    if (!ucString.startsWith(pKey.toUpperCase())) {
        pMatchVars.rBoolean1 = pDefault;
        return(false); //does not match
        }

    int indexOfEqual;

    //look for '=' symbol, if not found then return default
    if ( (indexOfEqual = pString.indexOf("=")) == -1) {
        pMatchVars.rBoolean1 = pDefault;
        return(true); //key matched but parse was invalid
        }

    //return the part of the line after the '=' sign - on error return default
    try{
        pMatchVars.rString1 = pString.substring(indexOfEqual + 1);

        //return boolean value for the value - default for any invalid value
        if (pMatchVars.rString1.equalsIgnoreCase("true")) {
            pMatchVars.rBoolean1 = true;
        }
        else if (pMatchVars.rString1.equalsIgnoreCase("false")) {
            pMatchVars.rBoolean1 = false;
        }
        else {
            pMatchVars.rBoolean1 = pDefault;
        }

        return(true); //key matched, parse valid
        }
    catch(StringIndexOutOfBoundsException e){
        pMatchVars.rBoolean1 = pDefault;
        return(true); //key matched but parse was invalid
        }

}//end of Tools::matchAndParseBoolean
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

//-----------------------------------------------------------------------------
// Tools::escapeIllegalFilenameChars
//
// Checks pString for characters which are not allowed for file or folder names
// and converts them to ASCII.
//
// Returns the validated file name.
//

static public String escapeIllegalFilenameChars(String pString)
{

    //the matches function for the String class could not be used since it
    //compares the entire string - Internet search suggest using a Whitelist
    //rather than a Blacklist
    String[] whiteList = new String[] { "%", //this must be FIRST
                                        "<", ">", "/", "?",
                                        ":", "\"", "\\", "|", "*"
                                        };

    String newName = pString;
    for (String ch : whiteList) {
        newName = newName.replace(ch, "%"+(byte)ch.charAt(0));
    }

    return newName;

}//end of Tools::escapeIllegalFilenameChars
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Tools::convertFilenameAsciiToReadable
//
// Checks pString for ASCII characters of the characters found in the white
// list following the % sign, such as "%01" and converts them back to their
// readable values/normal characters.
//
// Returns the validated file name.
//

static public String convertFilenameAsciiToReadable(String pString)
{

    //the matches function for the String class could not be used since it
    //compares the entire string - Internet search suggest using a Whitelist
    //rather than a Blacklist
    String[] whiteList = new String[] { "%", //this must be FIRST
                                        "<", ">", "/", "?",
                                        ":", "\"", "\\", "|", "*"
                                        };

    String newName = pString;
    for (String ch : whiteList) {
        newName = newName.replace("%"+(byte)ch.charAt(0), ch);
    }

    return newName;

}//end of Tools::convertFilenameAsciiToReadable
//-----------------------------------------------------------------------------


}//end of class Tools
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

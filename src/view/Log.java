/******************************************************************************
* Title: Log.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class displays a window for displaying information.
*
* It also has a method for appending messages to an error log file.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Log
//
// This class displays a text area in a window.
//

class Log extends JDialog{

    JTextArea textArea;

    static public final String newline = "\n";

//-----------------------------------------------------------------------------
// Log::Log (constructor)
//

public Log(JFrame frame)
{

    super(frame, "Log");

    int panelWidth = 400;
    int panelHeight = 500;

    setMinimumSize(new Dimension(panelWidth, panelHeight));
    setPreferredSize(new Dimension(panelWidth, panelHeight));
    setMaximumSize(new Dimension(panelWidth, panelHeight));

    textArea = new JTextArea();

    JScrollPane areaScrollPane = new JScrollPane(textArea);

    add(areaScrollPane);

    //setVisible(true);

}//end of Log::Log (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::appendString
//
// Appends a text string to the text window.
//

public void appendString(String pText)
{

    textArea.append(pText);

}//end of Log::appendString
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::appendLine
//
// Appends a text string to the text window and appends a new line.
//

public void appendLine(String pText)
{

    textArea.append(pText + newline);

}//end of Log::appendLine
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::appendToErrorLogFile
//
// Appends pMessage to the error log file "Error Log.txt".
//

public void appendToErrorLogFile(String pMessage)
{

    try (PrintWriter outputStream = 
            new PrintWriter(new FileWriter("Error Log.txt", true))) {

        outputStream.println(pMessage);

    }
    catch(IOException e){

        //ignore the error -- can't write it to the log file

    }

}//end of Log::appendToErrorLogFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

private void displayErrorMessage(String pMessage)
{

    JOptionPane.showMessageDialog(null, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of Log::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::toHex4String
//
// Converts an integer to a 4 character hex string.
//

static String toHex4String(int pValue)
{

    String s = Integer.toString(pValue, 16);

    //force length to be four characters

    if (s.length() == 0) {return "0000" + s;}
    else
    if (s.length() == 1) {return "000" + s;}
    else
    if (s.length() == 2) {return "00" + s;}
    else
    if (s.length() == 3) {return "0" + s;}
    else{
        return s;
    }

}//end of Log::toHex4String
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::toHex8String
//
// Converts an integer to an 8 character hex string.
//

static String toHex8String(int pValue)
{

    String s = Integer.toString(pValue, 16);

    //force length to be eight characters

    if (s.length() == 0) {return "00000000" + s;}
    else
    if (s.length() == 1) {return "0000000" + s;}
    else
    if (s.length() == 2) {return "000000" + s;}
    else
    if (s.length() == 3) {return "00000" + s;}
    else
    if (s.length() == 4) {return "0000" + s;}
    else
    if (s.length() == 5) {return "000" + s;}
    else
    if (s.length() == 6) {return "00" + s;}
    else
    if (s.length() == 7) {return "0" + s;}
    else{
        return s;
    }

}//end of Log::toHex8String
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::toUnsignedHex8String
//
// Converts an unsigned integer to an 8 character hex string.
//
// Since Java does not implement unsigned variables, the unsigned integer is
// transferred as a long value which is large enough to contain the full
// positive value of an unsigned integer
//

static String toUnsignedHex8String(long pValue)
{

    String s = Long.toString(pValue, 16);

    //force length to be eight characters

    if (s.length() == 0) {return "00000000" + s;}
    else
    if (s.length() == 1) {return "0000000" + s;}
    else
    if (s.length() == 2) {return "000000" + s;}
    else
    if (s.length() == 3) {return "00000" + s;}
    else
    if (s.length() == 4) {return "0000" + s;}
    else
    if (s.length() == 5) {return "000" + s;}
    else
    if (s.length() == 6) {return "00" + s;}
    else
    if (s.length() == 7) {return "0" + s;}
    else{
        return s;
    }

}//end of Log::toUnsignedHex8String
//-----------------------------------------------------------------------------

}//end of class Log
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

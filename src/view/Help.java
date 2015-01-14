/******************************************************************************
* Title: Help.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class displays a window for displaying help information.
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
import java.io.*;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Help
//
// This class displays a text area in a window.
//

class Help extends JDialog{

    public JTextArea textArea;

    private static final String HELP_FILENAME = "DSP Tools Help.txt";
    
//-----------------------------------------------------------------------------
// Help::Help (constructor)
//

public Help(JFrame frame)
{

    super(frame, "Help");

    //release the window's resources when it is closed
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    int panelWidth = 400;
    int panelHeight = 500;

    setMinimumSize(new Dimension(panelWidth, panelHeight));
    setPreferredSize(new Dimension(panelWidth, panelHeight));
    setMaximumSize(new Dimension(panelWidth, panelHeight));

    textArea = new JTextArea();

    JScrollPane areaScrollPane = new JScrollPane(textArea);

    add(areaScrollPane);

    loadAndDisplayInfo();

    setVisible(true);

}//end of Help::Help (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Help::loadAndDisplayInfo
//
// Loads the help info from a text file and displays it in the
// window.
//

private void loadAndDisplayInfo()
{

    String filename =
        "Help Files" + File.separator + HELP_FILENAME;

    String line;

    FileInputStream fileInputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader in = null;

    textArea.append("\n"); //blank line at the top

    try{

        fileInputStream = new FileInputStream(filename);
        inputStreamReader = new InputStreamReader(fileInputStream);
        in = new BufferedReader(inputStreamReader);

            //read each line and display it
            while ((line = in.readLine()) != null){
                textArea.append(line);
                textArea.append("\n");
            }

    }
    catch(IOException e){
            displayErrorMessage("Could not open Help file: " + filename);
    }
    finally{
        try{if (in != null) {in.close();}}
        catch(IOException e){}
        try{if (inputStreamReader != null) {inputStreamReader.close();}}
        catch(IOException e){}
        try{if (fileInputStream != null) {fileInputStream.close();}}
        catch(IOException e){}
    }

}//end of Help::loadAndDisplayInfo
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Help::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

private void displayErrorMessage(String pMessage)
{

    JOptionPane.showMessageDialog(null, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of Help::displayErrorMessage
//-----------------------------------------------------------------------------

}//end of class Help
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

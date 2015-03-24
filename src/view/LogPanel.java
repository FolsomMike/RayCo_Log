/******************************************************************************
* Title: LogPanel.java
* Author: Mike Schoonover
* Date: 03/23/15
*
* Purpose:
*
* This class encapsulates a panel with a text area.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.*;
import javax.swing.border.TitledBorder;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class LogPanel
//

public class LogPanel extends JPanel{
     
    String title;
    int width, height;

    private JTextArea textArea;

    ArrayList<String> textBuffer = new ArrayList<>();
    
//-----------------------------------------------------------------------------
// LogPanel::LogPanel (constructor)
//

public LogPanel(String pTitle, int pWidth, int pHeight)
{

    super();    
    
    title = pTitle; width = pWidth; height = pHeight;

}//end of LogPanel::LogPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LogPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    GUITools.setSizes(this, width, height);        
    
    createTextPanel(title, width-10, height-30);
    
}// end of LogPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LogPanel::createTextPanel
//
// Creates a panel containing a text area with title pTitle, width pWidth,
// length pLength.
//

private void createTextPanel(String pTitle, int pWidth, int pHeight)
{

    setBorder(BorderFactory.createTitledBorder(pTitle));

    textArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(textArea);
    GUITools.setSizes(scrollPane, pWidth, pHeight);    
    add(scrollPane);
          
}// end of LogPanel::createTextPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LogPanel::append
//
// Appends string pString to the text area.
//

public void append(String pString)
{

    textArea.append(pString);

}// end of LogPanel::append
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LogPanel::appendThreadSafe
//
// Appends string pString to the text area in a thread safe manner so that
// a thread other than Java's Event Dispatch Thread (which runs the GUI) can
// safely append text.
//
// The string is actually added to a buffer and the invokeLater command is
// used to trigger the Event Dispatch Thread to later append the buffered text
// to the text area object.
//

public synchronized void appendThreadSafe(String pString)
{
    
    textBuffer.add(pString);

    javax.swing.SwingUtilities.invokeLater(this::processTextBuffer);
    
}// end of LogPanel::appendThreadSave
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LogPanel::processTextBuffer
//
// Appends any text in the text buffer to the text area GUI object.
//

public synchronized void processTextBuffer()
{
    
    ListIterator lines = textBuffer.listIterator();
    
    while(lines.hasNext()){        
        textArea.append((String)lines.next());
        lines.remove(); //delete each string after it is handled
    }

}// end of LogPanel::processTextBuffer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LogPanel::setTitle
//
// Sets the panel's title to pTitle.
//

public void setTitle(String pTitle)
{

    ((TitledBorder)getBorder()).setTitle(pTitle);

}// end of LogPanel::setTitle
//-----------------------------------------------------------------------------


}//end of class LogPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

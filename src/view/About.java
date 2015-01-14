/******************************************************************************
* Title: About.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class displays a window for displaying information about the program.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import model.Options;
import java.awt.*;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class About
//
// This class displays a text area in a window.
//

class About extends JDialog{

    public JTextArea textArea;

//-----------------------------------------------------------------------------
// About::About (constructor)
//
//

public About(JFrame frame)
{

    super(frame, "About");

    int panelWidth = 300;
    int panelHeight = 500;

    setMinimumSize(new Dimension(panelWidth, panelHeight));
    setPreferredSize(new Dimension(panelWidth, panelHeight));
    setMaximumSize(new Dimension(panelWidth, panelHeight));

    textArea = new JTextArea();

    add(textArea);

    textArea.append("Software Version: " + Options.SOFTWARE_VERSION + "\n");

    textArea.append("\n");
    textArea.append("Author: Mike Schoonover" + "\n");

    setVisible(true);

}//end of About::About (constructor)
//-----------------------------------------------------------------------------

}//end of class About
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

/******************************************************************************
* Title: MainMenu.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class creates the main menu and sub-menus for the main form.
*
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import java.awt.event.*;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainMenu
//
// This class creates the main menu and sub menus for the main form.
//

public class MainMenu extends JMenuBar{

    ActionListener actionListener;

    JMenu fileMenu;
    JMenuItem newFile;
    JMenuItem openFile;
    JMenuItem saveFile;
    JMenuItem saveFileAs;

    JMenu helpMenu;
    JMenuItem logMenuItem, aboutMenuItem, helpMenuItem, exitMenuItem;

//-----------------------------------------------------------------------------
// MainMenu::MainMenu (constructor)
//

String language;

public MainMenu(ActionListener pActionListener)
{

    actionListener = pActionListener;

    //File menu
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.setToolTipText("File");
    add(fileMenu);

    //File/New
    newFile = new JMenuItem("New File");
    newFile.setMnemonic(KeyEvent.VK_N);
    newFile.setToolTipText("Create a new blank file.");
    newFile.setActionCommand("New File");
    newFile.addActionListener(actionListener);
    fileMenu.add(newFile);

    //File/Open
    openFile = new JMenuItem("Open File");
    openFile.setMnemonic(KeyEvent.VK_O);
    openFile.setToolTipText("Open an existing file");
    openFile.setActionCommand("Open File");
    openFile.addActionListener(actionListener);
    fileMenu.add(openFile);

    //File/Exit menu item
    exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.setMnemonic(KeyEvent.VK_X);
    exitMenuItem.setToolTipText("Exit");
    exitMenuItem.setActionCommand("Exit");
    exitMenuItem.addActionListener(actionListener);
    fileMenu.add(exitMenuItem);

    //Help menu
    helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    helpMenu.setToolTipText("Help");
    add(helpMenu);

    //Help menu items and submenus

    //View menu items and submenus
    logMenuItem = new JMenuItem("Log");
    logMenuItem.setMnemonic(KeyEvent.VK_L);
    logMenuItem.setToolTipText("Log");
    logMenuItem.setActionCommand("Display Log");
    logMenuItem.addActionListener(actionListener);
    helpMenu.add(logMenuItem);

    //option to display the "About" window
    aboutMenuItem = new JMenuItem("About");
    aboutMenuItem.setMnemonic(KeyEvent.VK_A);
    aboutMenuItem.setToolTipText("Display the About window.");
    aboutMenuItem.setActionCommand("Display About");
    aboutMenuItem.addActionListener(actionListener);
    helpMenu.add(aboutMenuItem);

    //option to display the "About" window
    helpMenuItem = new JMenuItem("Help");
    helpMenuItem.setMnemonic(KeyEvent.VK_H);
    helpMenuItem.setToolTipText("Display the Help window.");
    helpMenuItem.setActionCommand("Display Help");
    helpMenuItem.addActionListener(actionListener);
    helpMenu.add(helpMenuItem);

}//end of MainMenu::MainMenu (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainMenu::refreshMenuSettings
//
// Sets menu items such as checkboxes and radio buttons to match their
// associated option values.  This function can be called after the variables
// have been loaded to force the menu items to match.
//

public void refreshMenuSettings()
{


}//end of MainMenu::refreshMenuSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainMenu::isSelected
//
// Returns true is any of the top level menu items are selected.
//
// NOTE: this is a workaround for JMenuBar.isSelected which once true never
// seems to go back false when the menu is no longer selected.
//

@Override
public boolean isSelected()
{

    //return true if any top level menu item is selected

    if (fileMenu.isSelected() || helpMenu.isSelected()) {
        return(true);
    }

    return false;

}//end of MainMenu::isSelected
//-----------------------------------------------------------------------------

}//end of class MainMenu
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
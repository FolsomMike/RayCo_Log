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
import model.SharedSettings;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainMenu
//
// This class creates the main menu and sub menus for the main form.
//

public class MainMenu extends JMenuBar{

    ActionListener actionListener;
    SharedSettings sharedSettings;

    JMenu fileMenu;
    JMenuItem jobInfoMenuItem;
    JMenuItem changeJob;
    JMenuItem newJob;
    JMenuItem newFile;
    JMenuItem openFile;
    JMenuItem saveFile;
    JMenuItem saveFileAs;
    JMenu managePresetsMenuItem;
    JMenuItem loadFromAnotherJobMenuItem;
    JMenuItem savePresetMenuItem;
    JMenuItem loadPresetMenuItem;
    JMenuItem renamePresetMenuItem;
    JMenuItem deletePresetMenuItem;

    JMenu viewMenu;
    JMenuItem viewCompleted;
    JMenuItem viewIDInfoMenuItem;

    JMenu helpMenu;
    JMenuItem logMenuItem, aboutMenuItem, helpMenuItem, monitor, exitMenuItem;

//-----------------------------------------------------------------------------
// MainMenu::MainMenu (constructor)
//

String language;

public MainMenu(ActionListener pActionListener, SharedSettings pSharedSettings)
{

    actionListener = pActionListener;
    sharedSettings = pSharedSettings;

}//end of MainMenu::MainMenu (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainMenu::init
//

public void init()
{
    
    //File menu
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.setToolTipText("File");
    add(fileMenu);

    //File/Job Info menu item
    jobInfoMenuItem = new JMenuItem("Job Info");
    jobInfoMenuItem.setMnemonic(KeyEvent.VK_J);
    jobInfoMenuItem.setToolTipText("Display and edit job info.");
    jobInfoMenuItem.setActionCommand("Display Job Info");
    jobInfoMenuItem.addActionListener(actionListener);
    fileMenu.add(jobInfoMenuItem);

    //File/Change Job
    changeJob = new JMenuItem("Change Job");
    changeJob.setMnemonic(KeyEvent.VK_C);
    changeJob.setToolTipText("Change to a different job.");
    changeJob.setActionCommand("Display Change Job");
    changeJob.addActionListener(actionListener);
    fileMenu.add(changeJob);

    //File/New Job
    newJob = new JMenuItem("New Job");
    newJob.setMnemonic(KeyEvent.VK_N);
    newJob.setToolTipText("New Job");
    newJob.setActionCommand("Display New Job");
    newJob.addActionListener(actionListener);
    fileMenu.add(newJob);
    
    //File/Manage Presets menu item
    managePresetsMenuItem = new JMenu("Manage Presets");
    managePresetsMenuItem.setMnemonic(KeyEvent.VK_M);
    managePresetsMenuItem.setToolTipText("Manage Presets");
    fileMenu.add(managePresetsMenuItem);

    //File/Manage Presets/Load From Another Job menu item
    loadFromAnotherJobMenuItem = new JMenuItem("Copy From Another Job");
    loadFromAnotherJobMenuItem.setMnemonic(KeyEvent.VK_C);
    loadFromAnotherJobMenuItem.setToolTipText(
                                        "Copy settings from a different job.");
    loadFromAnotherJobMenuItem.setActionCommand("Display Copy Preset");
    loadFromAnotherJobMenuItem.addActionListener(actionListener);
    managePresetsMenuItem.add(loadFromAnotherJobMenuItem);

    //File/Manage Presets/Save Preset menu item
    savePresetMenuItem = new JMenuItem("Save Preset");
    savePresetMenuItem.setMnemonic(KeyEvent.VK_S);
    savePresetMenuItem.setToolTipText("Save current settings as a preset.");
    savePresetMenuItem.setActionCommand("Display Save Preset");
    savePresetMenuItem.addActionListener(actionListener);
    managePresetsMenuItem.add(savePresetMenuItem);

    //File/Manage Presets/Load Preset menu item
    loadPresetMenuItem = new JMenuItem("Load Preset");
    loadPresetMenuItem.setMnemonic(KeyEvent.VK_L);
    loadPresetMenuItem.setToolTipText("Load new settings from a preset.");
    loadPresetMenuItem.setActionCommand("Load Preset");
    loadPresetMenuItem.addActionListener(actionListener);
    managePresetsMenuItem.add(loadPresetMenuItem);

    //File/Manage Presets/Rename Preset menu item
    renamePresetMenuItem = new JMenuItem("Rename Preset");
    renamePresetMenuItem.setMnemonic(KeyEvent.VK_R);
    renamePresetMenuItem.setToolTipText("Rename the selected preset.");
    renamePresetMenuItem.setActionCommand("Rename Preset");
    renamePresetMenuItem.addActionListener(actionListener);
    managePresetsMenuItem.add(renamePresetMenuItem);

    //File/Manage Presets/Delete Preset menu item
    deletePresetMenuItem = new JMenuItem("Delete Preset");
    deletePresetMenuItem.setMnemonic(KeyEvent.VK_D);
    deletePresetMenuItem.setToolTipText("Delete a preset.");
    deletePresetMenuItem.setActionCommand("Delete Preset");
    deletePresetMenuItem.addActionListener(actionListener);
    managePresetsMenuItem.add(deletePresetMenuItem);

    //File/Exit menu item
    exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.setMnemonic(KeyEvent.VK_X);
    exitMenuItem.setToolTipText("Exit");
    exitMenuItem.setActionCommand("Exit");
    exitMenuItem.addActionListener(actionListener);
    fileMenu.add(exitMenuItem);

    //View menu
    viewMenu = new JMenu("View");
    viewMenu.setMnemonic(KeyEvent.VK_V);
    viewMenu.setToolTipText("View");
    add(viewMenu);

    //View/View Completed
    //WIP HSS// settings needs to have piece description so this can be changed
    viewCompleted = new JMenuItem("View Chart of a Completed " 
                                        + sharedSettings.pieceDescription);
    viewCompleted.setMnemonic(KeyEvent.VK_X);
    viewCompleted.setToolTipText("View chart of a completed " 
                                        + sharedSettings.pieceDescriptionLC + ".");
    viewCompleted.setActionCommand("View Completed");
    viewCompleted.addActionListener(actionListener);
    viewMenu.add(viewCompleted);
    
    //View/Edit Identifier Info
    viewIDInfoMenuItem = new JMenuItem("View / Edit Identifier Info");
    viewIDInfoMenuItem.setMnemonic(KeyEvent.VK_I);
    viewIDInfoMenuItem.setToolTipText(
                    "View and edit the identifier info for each "
                                    + sharedSettings.pieceDescriptionLC + ".");
    viewIDInfoMenuItem.addActionListener(actionListener);
    viewMenu.add(viewIDInfoMenuItem);


    //Help menu
    helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    helpMenu.setToolTipText("Help");
    add(helpMenu);

    //Help menu items and submenus

    //Log menu items and submenus
    logMenuItem = new JMenuItem("Log");
    logMenuItem.setMnemonic(KeyEvent.VK_L);
    logMenuItem.setToolTipText("Log");
    logMenuItem.setActionCommand("Display Log");
    logMenuItem.addActionListener(actionListener);
    helpMenu.add(logMenuItem);

    //Help/Monitor
    monitor = new JMenuItem("Monitor");
    monitor.setMnemonic(KeyEvent.VK_M);
    monitor.setToolTipText("Monitor");
    monitor.setActionCommand("Start Monitor");
    monitor.addActionListener(actionListener);
    helpMenu.add(monitor);

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

}//end of MainMenu::init
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
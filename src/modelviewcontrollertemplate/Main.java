/******************************************************************************
* Title: Main.java - Main Source File for a program based on the
*                                              ModelViewController Template
* Author: Mike Schoonover
* Date: 11/15/13
*
* Purpose:
*
* This application is based on the ModelViewController Template. For detailed
* info on the purpose of the program, view the comments at the top of the
* controller.MainController class.
*
* This class does nothing more than create an instance of
* controller.MainController and pass control to it.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package modelviewcontrollertemplate;

//-----------------------------------------------------------------------------


import controller.MainController;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Main
//

public class Main{

static MainController controller;

//-----------------------------------------------------------------------------
// Main::createController
//
// This method creates an instance of the MainController class which will then
// take over.
//
// Since it will generally cause the creation of the GUI and show it, for
// thread safety, this method should be invoked from the event-dispatching
// thread.  This is usually done by using invokeLater to schedule this funtion
// to be called from inside the event- dispatching thread.  This is necessary
// because the main function is not operating in the event-dispatching thread.
// See the main function for more info.
//

private static void createController()
{

    //create the program's controller which will create all other objects
    controller = new MainController();
    controller.init();

}//end of Main::createController
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Main::main
//

public static void main(String[] args)
{

    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.

    javax.swing.SwingUtilities.invokeLater(() -> {
        createController();
    });

}//end of Main::main
//-----------------------------------------------------------------------------

}//end of class Main
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// Useful debugging code

//displays message on bottom panel of IDE
//System.out.println("File not found");

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

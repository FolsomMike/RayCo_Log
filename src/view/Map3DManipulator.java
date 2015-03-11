/******************************************************************************
* Title: Map3DManipulator.java
* Author: Mike Schoonover
* Date: 03/10/15
*
* Purpose:
*
* This class subclasses a JPanel to display controls for manipulating a 3D
* map display.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3DManipulator
//

class Map3DManipulator extends JPanel implements ControlsGroup{

    ActionListener actionListener;
    
    private MFloatSpinnerPanel xPos, yPos;
    private MFloatSpinnerPanel rotation;

    ArrayList<Object> values = new ArrayList<>();
    
    private static final String ACTION_COMMAND = "Handle 3D Map Control Change";

//-----------------------------------------------------------------------------
// Map3DManipulator::Map3DManipulator (constructor)
//
//

public Map3DManipulator(ActionListener pActionListener)
{

    actionListener = pActionListener;
        
}//end of Map3DManipulator::Map3DManipulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    setupGUI();
    
}// end of Map3DManipulator::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::setupGUI
//
// Creates and sets up GUI controls and adds them to the panel.
//

public void setupGUI()
{
    
    //panel.setBorder(BorderFactory.createTitledBorder("3D Map Manipulator"));
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    setAlignmentX(Component.LEFT_ALIGNMENT);
    
    addVerticalSpacer(this, 10);
    
    createXYPositionPanel();

    addVerticalSpacer(this, 5);
        
    rotation = new MFloatSpinnerPanel("Rotation",
            "Adjusts the rotation of the display. (rotation variable)",
            actionListener, 200, 0, 359, 1, "##0", 60, 20,
             "Rotation", "degrees", ACTION_COMMAND, 153, 25);
    rotation.init();
    rotation.setAlignmentX(Component.LEFT_ALIGNMENT);    
    add(rotation);
        
}// end of Map3DManipulator::setupGUI
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::createXYPositionPanel
//
// Creates a panel with an x and a y position adjustment control.
//

public void createXYPositionPanel()
{

    JPanel panel = new JPanel();
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    xPos = new MFloatSpinnerPanel("X Position",
            "Adjusts the x position of the display. (xPos variable)",
            actionListener, -19, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "X,Y Position", "", ACTION_COMMAND, 125, 25);
    xPos.init();
    panel.add(xPos);

    yPos = new MFloatSpinnerPanel("Y Position",
            "Adjusts the y position of the display. (yPos variable)",
            actionListener, 110, Integer.MIN_VALUE, Integer.MAX_VALUE, 1,
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    yPos.init();
    panel.add(yPos);
    
    add(panel);
    
}// end of Map3DManipulator::createXYPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::resetAll
//
// Resets all values and child values to default.
//

public void resetAll()
{
    
}// end of Map3DManipulator::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::getAllValues
//
// Collects all values from the different controls and returns them
// via an ArrayList.
//
// The first object will always be a string describing the control group so
// that any receiving object can discern the content and ordering in the list.
//

@Override
public ArrayList<Object> getAllValues()
{

    values.clear();
    
    values.add("Map3DManipulator");
    
    values.add(xPos.getIntValue());    
    
    values.add(yPos.getIntValue());    
    
    values.add(rotation.getIntValue());
    
    return(values);
    
}// end of Map3DManipulator::getAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::setAllValues
//
// Sets all values in the different controls from objects in an ArrayList.
//
// The first object will always be a string describing the control group.
//

@Override
public void setAllValues(ArrayList<Object> pValues)
{

    
    
}// end of Map3DManipulator::setAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::addVerticalSpacer
//
// Adds a vertical spacer of pNumPixels height to JPanel pTarget.
//

public void addVerticalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(0,pNumPixels)));
    
}// end of Map3DManipulator::addVerticalSpacer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::addHorizontalSpacer
//
// Adds a horizontal spacer of pNumPixels width to JPanel pTarget.
//

public void addHorizontalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(pNumPixels,0)));
    
}// end of Map3DManipulator::addHorizontalSpacer
//-----------------------------------------------------------------------------


}//end of class Map3DManipulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

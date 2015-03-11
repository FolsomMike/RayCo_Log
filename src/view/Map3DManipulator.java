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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import static view.MainView.addVerticalSpacer;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3DManipulator
//

class Map3DManipulator extends JPanel implements ControlsGroup{

    ActionListener actionListener;
    
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
    
    rotation = new MFloatSpinnerPanel("Rotation", "Adjusts the rotation.",
            actionListener, 200, 0, 359, 1, "##0", 60, 20,
             "Rotation", "degrees", ACTION_COMMAND, 150, 25);
    rotation.init();
    add(rotation);
    
}// end of Map3DManipulator::setupGUI
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


}//end of class Map3DManipulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

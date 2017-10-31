/******************************************************************************
* Title: Map3DManipulator.java
* Author: Mike Schoonover
* Date: 03/10/15
*
* Purpose:
*
* This class subclasses ControlPanel to display controls for manipulating a 3D
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3DManipulator
//

class Map3DManipulator extends ControlPanel implements ActionListener{

    private MFloatSpinnerPanel xPos, yPos;
    private MFloatSpinnerPanel rotation;
    private MFloatSpinnerPanel viewAngle;
    private MFloatSpinnerPanel xFrom, yFrom, zFrom;
    private MFloatSpinnerPanel xAt, yAt, zAt;

    JButton expandBtn, animateBtn;
    
    ArrayList<Object> values = new ArrayList<>();
    
    private static final String ACTION_COMMAND = "Handle 3D Map Control Change";

//-----------------------------------------------------------------------------
// Map3DManipulator::Map3DManipulator (constructor)
//
//

public Map3DManipulator(int pChartGroupNum, int pChartNum, 
                                          ActionListener pParentActionListener)
{

    super(pChartGroupNum, pChartNum, pParentActionListener);
  
    panelTitle = "3D Map Manipulator";
    
}//end of Map3DManipulator::Map3DManipulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();
    
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
    
    createViewFromPositionPanel();

    addVerticalSpacer(this, 5);
    
    createViewAtPositionPanel();
    
    addVerticalSpacer(this, 5);    

    createRotationPanel();
    
    addVerticalSpacer(this, 5);
        
    createViewAnglePanel();
    
    addVerticalSpacer(this, 5);    
    
    createChartControlsPanel();
    
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
    panel.setBorder(BorderFactory.createTitledBorder("Position on Screen XY"));    
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    xPos = new MFloatSpinnerPanel("X Position",
            "Adjusts the x position of the display. (xPos variable)",
            this, -19, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    xPos.init();
    panel.add(xPos);

    yPos = new MFloatSpinnerPanel("Y Position",
            "Adjusts the y position of the display. (yPos variable)",
            this, 110, Integer.MIN_VALUE, Integer.MAX_VALUE, 1,
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    yPos.init();
    panel.add(yPos);
    
    setSizes(panel, 190, 47);
    
    add(panel);
    
}// end of Map3DManipulator::createXYPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::createViewFromPositionPanel
//
// Creates a panel with x,y,z position adjustment controls to specify the
// point in world coordinates where the user's eye is located.
//

public void createViewFromPositionPanel()
{

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("View From Position XYZ"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    xFrom = new MFloatSpinnerPanel("X From Position",
            "Adjusts 'view from' position. (xFrom variable)",
            this, 15, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    xFrom.init();
    panel.add(xFrom);

    yFrom = new MFloatSpinnerPanel("Y From Position",
            "Adjusts 'view from' position. (yFrom variable)",
            this, 9, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    yFrom.init();
    panel.add(yFrom);

    zFrom = new MFloatSpinnerPanel("Z From Position",
            "Adjusts 'view from' position. (zFrom variable)",
            this, 26, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    zFrom.init();
    panel.add(zFrom);
        
    setSizes(panel, 190, 47);    
    
    add(panel);
    
}// end of Map3DManipulator::createViewFromPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::createViewAtPositionPanel
//
// Creates a panel with x,y,z position adjustment controls to specify the
// point in world coordinates where the view target is located.
//

public void createViewAtPositionPanel()
{

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("View At Position XYZ"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    xAt = new MFloatSpinnerPanel("X At Position",
            "Adjusts 'view at' position. (xAt variable)",
            this, 11, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    xAt.init();
    panel.add(xAt);

    yAt = new MFloatSpinnerPanel("Y At Position",
            "Adjusts 'view at' position. (yAt variable)",
            this, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    yAt.init();
    panel.add(yAt);

    zAt = new MFloatSpinnerPanel("Z At Position",
            "Adjusts 'view at' position. (zAt variable)",
            this, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 
            "##0", 55, 20, "", "", ACTION_COMMAND, 60, 25);
    zAt.init();
    panel.add(zAt);
        
    setSizes(panel, 190, 47);    
    
    add(panel);
    
}// end of Map3DManipulator::createViewAtPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::createRotationPanel
//
// Creates a panel with rotation adjustment controls to specify the
// angle of rotation of the target.
//

public void createRotationPanel()
{

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Rotation Angle"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    rotation = new MFloatSpinnerPanel("Rotation",
            "Adjusts the rotation of the target. (rotation variable)",
            this, 200, 0, 359, 1, "##0", 60, 20,
             "", "degrees", ACTION_COMMAND, 160, 25);
    rotation.init();
    rotation.setAlignmentX(Component.LEFT_ALIGNMENT);    
    add(rotation);

    panel.add(rotation);
    
    setSizes(panel, 190, 47);    
    
    add(panel);
    
}// end of Map3DManipulator::createRotationPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::createViewnAnglePanel
//
// Creates a panel with view angle adjustment controls to specify the
// angle of rotation of the target.
//
// The view angle specifies the amount of the target in the view...effectively
// acting as a zoom in / zoom out.
//

public void createViewAnglePanel()
{

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder(
                                                  "View Angle (zoom in/out)"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    viewAngle = new MFloatSpinnerPanel("View Angle",
            "Adjusts the angle of view -- how much is in the view. "
            +" Acts as zoom in/out. (viewAngle variable)",
            this, 7, 1, 179, 1, "##0", 60, 20,
             "", "degrees", ACTION_COMMAND, 175, 25);
    viewAngle.init();
    viewAngle.setAlignmentX(Component.LEFT_ALIGNMENT);    
    add(viewAngle);

    panel.add(viewAngle);
    
    setSizes(panel, 190, 47);    
    
    add(panel);
    
}// end of Map3DManipulator::createViewAnglePanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::createChartControlsPanel
//
// Creates a panel with controls for adjusting chart parameters.
//

public void createChartControlsPanel()
{

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Chart"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    addVerticalSpacer(panel, 5);    
    
    expandBtn = new JButton("Expanded View");
    expandBtn.setActionCommand("Expand Chart Height");
    expandBtn.addActionListener(this);
    expandBtn.setToolTipText("Expand chart height.");            
    panel.add(expandBtn);
    
    addVerticalSpacer(panel, 5);

    animateBtn = new JButton("Animate");
    animateBtn.setActionCommand("Animate Graph");
    animateBtn.addActionListener(this);
    animateBtn.setToolTipText("Start or stop animation of a graph.");
    panel.add(animateBtn);
    
    addVerticalSpacer(panel, 5);
    
    
    setSizes(panel, 190, 90);
    
    add(panel);
    
}// end of Map3DManipulator::createChartControlsPanel
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
    
    values.add(xFrom.getIntValue());
    
    values.add(yFrom.getIntValue());
    
    values.add(zFrom.getIntValue());
    
    values.add(xAt.getIntValue());
    
    values.add(yAt.getIntValue());
    
    values.add(zAt.getIntValue());

    values.add(rotation.getIntValue());
    
    values.add(viewAngle.getIntValue());    
    
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

    int i = 1;
    
    xPos.setValueAsInt((Integer)pValues.get(i++));
    
    yPos.setValueAsInt((Integer)pValues.get(i++));

    xFrom.setValueAsInt((Integer)pValues.get(i++));
    
    yFrom.setValueAsInt((Integer)pValues.get(i++));
    
    zFrom.setValueAsInt((Integer)pValues.get(i++));
    
    xAt.setValueAsInt((Integer)pValues.get(i++));
    
    yAt.setValueAsInt((Integer)pValues.get(i++));
    
    zAt.setValueAsInt((Integer)pValues.get(i++));
        
    rotation.setValueAsInt((Integer)pValues.get(i++));
    
    viewAngle.setValueAsInt((Integer)pValues.get(i++));
        
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

//-----------------------------------------------------------------------------
// Map3DManipulator::actionPerformedLocal
//
// Responds to events which require action by this object.
//

@Override
protected boolean actionPerformedLocal(ActionEvent e)
{

    if ("Expand Chart Height".equals(e.getActionCommand())
        || "Set Normal Chart Height".equals(e.getActionCommand())) {
        handleExpandButtonClick();
        return false; //return false so parent handling still occurs
    }
    
    return false;
    
}//end of Map3DManipulator::actionPerformedLocal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::handleExpandButtonClick
//
// Responds clicking of the "Expand" button.
//
// Flips the text and meaning of the button from "Expanded View" to
// "Normal View".
//

public void handleExpandButtonClick()
{
    
    
    if(expandBtn.getText().equals("Expanded View")){

        expandBtn.setText("Normal View");    
        expandBtn.setActionCommand("Set Normal Chart Height");
        expandBtn.setToolTipText("Set chart height to normal view.");            
          
    }else{
    
        expandBtn.setText("Expanded View");
        expandBtn.setActionCommand("Expand Chart Height");
        expandBtn.setToolTipText("Expand chart height.");  
        
    }

}//end of Map3DManipulator::handleExpandButtonClick
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3DManipulator::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of Map3DManipulator::setSizes
//-----------------------------------------------------------------------------


}//end of class Map3DManipulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

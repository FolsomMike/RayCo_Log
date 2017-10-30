/******************************************************************************
* Title: ControlPanel.java
* Author: Mike Schoonover
* Date: 03/15/15
*
* Purpose:
*
* This class subclasses a JPanel and is the parent class for classes which
* display and handle controls.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

//-----------------------------------------------------------------------------

import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

//-----------------------------------------------------------------------------
// class ControlPanel
//

class ControlPanel extends JPanel implements ControlsGroup{
    
    String panelTitle = "";
    String getPanelTitle(){ return(panelTitle); }
    
    //specifies which chart the panel is connected to
    private final int chartGroupNum;
    @Override
    public int getChartGroupNum(){ return(chartGroupNum); }
    private final int chartNum;
    @Override
    public int getChartNum(){ return(chartNum); }

    ActionListener parentActionListener;    
    
    protected ImageIcon warningSymbol;
    
//-----------------------------------------------------------------------------
// ControlPanel::ControlPanel (constructor)
//
//

public ControlPanel(int pChartGroupNum, int pChartNum, 
                                          ActionListener pParentActionListener)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    parentActionListener = pParentActionListener;
        
}//end of ControlPanel::ControlPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    //NOTE: You must use forward slashes in the path names for the resource
    //loader to find the image files in the JAR package.
    warningSymbol = createImageIcon("images/windows-warning.gif");
    
}// end of ControlPanel::init
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// MainView::getAllValues
//
// 

@Override
public ArrayList<Object> getAllValues()
{

    return(null);
    
}// end of MainView::getAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// MainView::setAllValues
//
// 

@Override
public void setAllValues(ArrayList<Object> pValues)
{
    
}// end of MainView::setAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanel::createImageIcon
//
// Returns an ImageIcon, or null if the path was invalid.
//
// ***************************************************************************
// NOTE: You must use forward slashes in the path names for the resource
// loader to find the image files in the JAR package.
// ***************************************************************************
//

public static ImageIcon createImageIcon(String path)
{
    
    // have to use the MainView class because it is located in the same 
    // package as the file; specifying the class specifies the first 
    // portion of the path to the image, this concatenated with the pPath
    java.net.URL imgURL = MainView.class.getResource(path);

    if (imgURL != null) {
        return new ImageIcon(imgURL);
    }
    else {return null;}

}//end of ControlPanel::createImageIcon
//-----------------------------------------------------------------------------

}//end of class ControlPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
    
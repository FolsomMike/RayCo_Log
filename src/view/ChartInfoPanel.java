/******************************************************************************
* Title: ChartInfoPanel.java
* Author: Mike Schoonover
* Date: 02/27/15
*
* Purpose:
*
* This class subclasses a JPanel to display information about a chart, such
* as a color key, monitoring info, etc.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.*;
import model.IniFile;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ChartInfoPanel
//

class ChartInfoPanel extends JPanel{
    
    IniFile configFile;
    
    private String title;    
    private String shortTitle;
    private final int chartGroupNum, chartNum, infoPanelNum;
    private int width, height;
    public void setWidth(int pWidth) { width = pWidth; }
    public void setHeight(int pHeight) { height = pHeight; }
    ArrayList<ColorKeyInfo> colorKeys = new ArrayList<>();

    String button1Text, button1Action, button1ToolTipText;
    
    int numColorKeys;

    ActionListener parentActionListener;
    
//-----------------------------------------------------------------------------
// ChartInfoPanel::ChartInfoPanel (constructor)
//
//

public ChartInfoPanel(int pChartGroupNum, int pChartNum, int pInfoPanelNum,
               int pWidth, int pHeight, ActionListener pParentActionListener,
                                                           IniFile pConfigFile)
{

    chartGroupNum = pChartGroupNum; chartNum = pChartNum;
    infoPanelNum = pInfoPanelNum;
    width = pWidth; height = pHeight; configFile = pConfigFile;
    parentActionListener = pParentActionListener;
    
}//end of ChartInfoPanel::ChartInfoPanel (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    loadConfigSettings();
    
    setSizes(this, width, height);
 
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    //add the chart group and chart number to the end of the action string
    //so the chart can be linked to the control panel when opened
    button1Action += "," + chartGroupNum + "," + chartNum;
    
    JButton button1;
    
    button1 = new JButton(button1Text);
    button1.setActionCommand(button1Action);    
    button1.setMargin(new Insets(0, 0, 0, 0));
    button1.addActionListener(parentActionListener);
    button1.setToolTipText(button1ToolTipText);    
    setSizes(button1, 65, 15);
    add(button1);
    
}// end of ChartInfoPanel::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::addColorKey
//
// Adds a color key item to the list so it will be displayed. Color keys
// display a colored square with a note to explain the color's meaning and
// usage.
//

public void addColorKey(Color pKeyColor, String pKeyDescription,
                                                          int pXPos, int pYPos)
{

    colorKeys.add(new ColorKeyInfo(pKeyColor, pKeyDescription, pXPos, pYPos));
    
}// end of ChartInfoPanel::addColorKey
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::createColorKeys
//
// Loads information from the config file for color keys and creates them and
// adds them to the list. Other objects can add color keys by calling
// addColorKey, such as a chart adding color keys for its traces.
//
// This method allows color keys to be added via config file entries for graphs
// such as 3D maps which don't have traces and thus won't have color keys added
// by the containing chart.
//
// A note with no color swatch can be created by specifing the color "none" in
// the config file.
//

public void createColorKeys(String pSection)
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                             + " Info Panel " + infoPanelNum;

    for (int i=0; i<numColorKeys; i++){
    
        String keyPrefix = "color key " + i + " ";
        
        colorKeys.add(new ColorKeyInfo(
            configFile.readColor(section, keyPrefix + "color", null),
            configFile.readString(section, keyPrefix + "description", ""),
            configFile.readInt(section, keyPrefix + "x position", 0),
            configFile.readInt(section, keyPrefix + "y position", 0)
        ));

    }
    
}// end of ChartInfoPanel::createColorKeys
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::updateDimensions
//
// Adjusts all width and height variables for the panel along with all such
// values in relevant child objects.
//
// Should be called any time the panel is resized.
//

public void updateDimensions(int pNewWidth, int pNewHeight)
{

    width = pNewWidth; height = pNewHeight;

    setSizes(this, width, height);

}// end of ChartInfoPanel::updateDimensions
//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------
// ChartInfoPanel::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of ChartInfoPanel::setSizes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

    super.paintComponent(g);
 
    ListIterator iter = colorKeys.listIterator();
    
    while(iter.hasNext()){
        ((ColorKeyInfo)iter.next()).paint((Graphics2D)g);
    }
        
}// end of ChartInfoPanel::paintComponent
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ChartInfoPanel::loadConfigSettings
//
// Loads settings for the object from configFile.
//

private void loadConfigSettings()
{

    String section = "Chart Group " + chartGroupNum + " Chart " + chartNum
                                             + " Info Panel " + infoPanelNum;

    button1Text = configFile.readString(section, "button 1 text", "Calibrate");
        
    button1ToolTipText = configFile.readString(
                                        section, "button 1 tool tip text", "");
    button1Action = 
                 configFile.readString(section, "button 1 action command", "");
   
    numColorKeys = configFile.readInt(section, "number of color keys", 0);

    createColorKeys(section);
    
}// end of ChartInfoPanel::loadConfigSettings
//-----------------------------------------------------------------------------

}//end of class ChartInfoPanel
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

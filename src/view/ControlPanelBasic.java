/******************************************************************************
* Title: ChannelControlPanel.java
* Author: Mike Schoonover
* Date: 03/10/15
*
* Purpose:
*
* This class subclasses ControlPanel to display controls for adjusting settings
* for hardware channels such as gain, hardware offset (referred to as
* centering), positive trace offset, and negative trace offset.
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
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;
import javax.swing.border.TitledBorder;
import mksystems.mswing.MFloatSpinner;
import toolkit.Tools;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ControlPanelBasic
//

class ControlPanelBasic extends ControlPanel
{

    private Chart chart;
    
    JTabbedPane tabPane;

    int numChannelsPerGroup;
    LinkedHashSet<String> groupTitles;
    ArrayList<ChannelInfo> channelList;

    ArrayList<Threshold[]> thresholds;
    

    private MFloatSpinnerPanel xPos, yPos;
    private MFloatSpinnerPanel rotation;
    private MFloatSpinnerPanel viewAngle;
    private MFloatSpinnerPanel xFrom, yFrom, zFrom;
    private MFloatSpinnerPanel xAt, yAt, zAt;
    
    private JCheckBox hideChartCheckBox;

    JButton expandBtn, animateBtn;

    ArrayList<Object> values = new ArrayList<>();

    private static final String ACTION_COMMAND =
                                        "Handle Channel Control Panel Change";

//-----------------------------------------------------------------------------
// ControlPanelBasic::ControlPanelBasic (constructor)
//
// The channels can be all in one group or separated into multiple groups. The
// number of groups is deduced from the number of titles in pGroupTitles. For
// single grouping, the channels are numbered in ascending order throughout
// the group. For multiple channels, a group title is inserted between each
// group and numbering restarts from 1 for each group.
//
// For a single group, the single title "Channel" may be used as it will make
// sense for a heading above the channel numbers.
//
// For multiple groups, titles such as "Shoe 1" and "Shoe 2" make sense; the
// fact that the numbers below those titles are channel numbers can be deduced
// by the user.
//

public ControlPanelBasic(int pChartGroupNum, int pChartNum, Chart pChart,
                            String pPanelTitle,                            
                            LinkedHashSet<String> pGroupTitles,
                            ArrayList<ChannelInfo> pChannelList,
                            ArrayList<Threshold[]> pThresholds,
                            ActionListener pParentActionListener)
{

    super(pChartGroupNum, pChartNum, pParentActionListener);
    
    chart = pChart;
    
    groupTitles = pGroupTitles; channelList = pChannelList;

    thresholds = pThresholds;

    panelTitle = pPanelTitle;

    //assume that the channels are distributed evenly amongst the groups
    numChannelsPerGroup = channelList.size() / groupTitles.size();

}//end of ControlPanelBasic::ControlPanelBasic (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override
public void init()
{

    super.init();

    setupGUI();

}// end of ControlPanelBasic::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::setupGUI
//
// Creates and sets up GUI controls and adds them to the panel.
//

public void setupGUI()
{

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    tabPane = new JTabbedPane();

    add(tabPane);

    JPanel gainTab = new JPanel();
    tabPane.addTab("Gain", null, gainTab, "Gain & Centering");
    setupGainTab(gainTab);

    JPanel chartTab = new JPanel();    
    tabPane.addTab("Chart", null, chartTab, "Chart settings & thresholds.");
    setupChartTab(chartTab);

    JPanel processingTab = new JPanel();
    tabPane.addTab("Processing", null, processingTab, "Processing");

    JPanel optionsTab = new JPanel();
    tabPane.addTab("Options", null, optionsTab, "Options");



    /*

    //panel.setBorder(BorderFactory.createTitledBorder(""));
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

            */

}// end of ControlPanelBasic::setupGUI
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::setupGainTab
//
// Adds Gain and Hardware Offset (centering) controls to pPanel.
//

public void setupGainTab(JPanel pPanel)
{

    addGainOffsetPanel(pPanel);

}// end of ControlPanelBasic::setupGainTab
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::addGainOffsetPanel
//
// Adds Gain and Hardware Offset (centering) panel to pPanel.
//

public void addGainOffsetPanel(JPanel pPanel)
{

    JPanel panel = new JPanel();

    int numChGr = numChannelsPerGroup;
    int numGroups = groupTitles.size();

    //calculate number of rows needed to hold the titles/labels/controls

    int numRows = 1; //start with 1 row for top labels and first group title

    numRows += numChGr * numGroups; //add one row for each channel

    //add title row for each group
    numRows += numGroups;

    panel.setLayout(new GridLayout(numRows, 4, 3, 3));

    panel.add(new JLabel("Channel"));
    panel.add(new JLabel("On/Off"));
    panel.add(new JLabel("Gain"));
    panel.add(new JLabel("Centering"));

    //title for first group is included on the top header row
    //titles for groups thereafter placed on a row inserted between groups

    Iterator<String> itr = groupTitles.iterator();

    int infoIndex = 0;

    while(itr.hasNext()){

        String groupName = itr.next();

        //fill in group title row for each group after the first one
        panel.add(new JLabel(groupName));
        panel.add(createAllOnOrOffButton("On", groupName));
        panel.add(createAllOnOrOffButton("Off", groupName));
        panel.add(new JLabel("")); //blank space(s)

        for(int j=0; j<numChGr; j++){

            panel.add(new JLabel("" + (j+1)));

            ChannelInfo chInfo = channelList.get(infoIndex);

            chInfo.onOffBox = new JCheckBox();
            chInfo.onOffBox.setName("On-Off Checkbox," + groupName + "," +
                  infoIndex + "," + chInfo.deviceNum + ","+ chInfo.channelNum);
            chInfo.onOffBox.addActionListener(this);
            chInfo.onOffBox.setActionCommand("On-Off Checkbox");
            chInfo.onOffBox.addMouseListener(this);
            chInfo.onOffBox.setSelected(chInfo.onOff);
            panel.add(chInfo.onOffBox);

            chInfo.gainSpin = new MFloatSpinner(chInfo.gain,0,255.0,1,
                                                                "##0",60,-1);
            chInfo.gainSpin.addChangeListener(this);
            chInfo.gainSpin.setName("Gain Spinner," + groupName + "," +
                  infoIndex + "," + chInfo.deviceNum + ","+ chInfo.channelNum);
            //watch for right mouse clicks
            setSpinnerNameAndMouseListener(chInfo.gainSpin,
                                            chInfo.gainSpin.getName(), this);
            panel.add(chInfo.gainSpin);

            chInfo.offsetSpin = new MFloatSpinner(chInfo.offset,0,255.0,1,
                                                                "##0",60,-1);
            chInfo.offsetSpin.addChangeListener(this);
            chInfo.offsetSpin.setName("Offset Spinner," + groupName + "," +
                  infoIndex + "," + chInfo.deviceNum + ","+ chInfo.channelNum);
            //watch for right mouse clicks
            setSpinnerNameAndMouseListener(chInfo.offsetSpin,
                                            chInfo.offsetSpin.getName(), this);
            panel.add(chInfo.offsetSpin);

            infoIndex++;

        }

    }

    pPanel.add(panel);

}// end of ControlPanelBasic::addGainOffsetPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::setupChartTab
//
// Adds threshold levels to chart tab.
//

public void setupChartTab(JPanel pPanel)
{
    
    JPanel panel = pPanel;
    panel.setLayout(new GridBagLayout());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.PAGE_START;
    c.weightx = 0.5;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 5, 5);  //padding
    
    panel.add(createThresholdsPanel(), c);
    c.gridy++;
    panel.add(createDisplayPanel(), c);
    c.gridy++;
    panel.add(createTransformPanel(), c);
    
    //fill the remaining space in the tab so that the rest
    //of the content shrinks to fit their children components
    c.gridy++; c.weighty = 1;
    panel.add(Box.createVerticalGlue(), c);

}// end of ControlPanelBasic::setupChartTab
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createThresholdsPanel
//
// Creates and returns the Thresholds panel.
//

public JPanel createThresholdsPanel()
{
    
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Thresolds"));
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.PAGE_START;
    c.weightx = 0.5;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 5, 5);  //padding
    
    panel.add(new JLabel("Graph"), c); c.gridx++;
    panel.add(new JLabel("Threshold"), c); c.gridx++;
    panel.add(new JLabel("Color", CENTER), c); c.gridx++;
    panel.add(new JLabel("Level"), c); 
    
    for (Threshold[] thresholdsArray : thresholds) {
        for (Threshold threshold : thresholdsArray) {

            c.gridy++; //go to next row
            c.gridx=0; //restart back at 0 for each new row
            
            //graph title
            panel.add(new JLabel(threshold.getGraphInfo().title, LEFT), c); 
            c.gridx++;

            //threshold title
            panel.add(new JLabel(threshold.getThresholdInfo().getTitle(),LEFT), c); 
            c.gridx++;

            //add a color swatch to the color column
            Color thresColor = threshold.getThresholdInfo().getThresholdColor();
            panel.add(new JLabel(createColorSwatch(thresColor),CENTER), c); 
            c.gridx++;

            //threshold level spinner
            MFloatSpinner lvl = new MFloatSpinner(threshold.getLevel(),0,
                    255.0,1,"##0",60,-1);
            lvl.addChangeListener(this);
            lvl.setName("Threshold Spinner,"
                        + threshold.getThresholdInfo().getChartGroupNum() + ","
                        + threshold.getThresholdInfo().getChartNum() + ","
                        + threshold.getThresholdInfo().getGraphNum() + ","
                        + threshold.getThresholdInfo().getThresholdNum());
            setSpinnerNameAndMouseListener(lvl, lvl.getName(), this);
            panel.add(lvl, c);
            c.gridx++;
            
        }
    }
    
    return panel;

}// end of ControlPanelBasic::createThresholdsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createDisplayPanel
//
// Creates and returns the Display panel.
//

public JPanel createDisplayPanel()
{

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Display"));

    panel.add(hideChartCheckBox = new JCheckBox("Hide Chart"));
    hideChartCheckBox.setName("Hide Chart");
    hideChartCheckBox.setActionCommand("Hide Chart," + chartGroupNum 
                                            + "," + chartNum);
    hideChartCheckBox.addActionListener(this);
    hideChartCheckBox.setSelected(!chart.isChartVisible());
    
    panel.add(Box.createHorizontalGlue()); //extend box to fit panel

    return panel;

}// end of ControlPanelBasic::createDisplayPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createTransformPanel
//
// Creates and returns the Transform panel.
//

public JPanel createTransformPanel()
{
    
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createTitledBorder("Transform"));
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.PAGE_START;
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 5, 5);  //padding
    
    panel.add(new JLabel("Graph"), c); c.gridx++;
    panel.add(new JLabel("Offset"), c); 
    
    for (Graph graph : chart.getGraphs()) {
        
        //only TraceGraphs have editable y-offsets
        if (!(graph instanceof TraceGraph)) { continue; }
        
        c.gridy++; //go to next row
        c.gridx=0; //restart back at 0 for each new row

        //graph title
        panel.add(new JLabel(graph.title, LEFT), c); 
        c.gridx++;

        //y offset spinner
        MFloatSpinner lvl = new MFloatSpinner(graph.getGraphInfo().yOffset,0,
                                                255.0,1,"##0",60,-1);
        lvl.addChangeListener(this);
        lvl.setName("Graph Y-Offset Spinner,"
                    + graph.getChartGroupNum() + ","
                    + graph.getChartNum() + ","
                    + graph.getGraphNum());
        setSpinnerNameAndMouseListener(lvl, lvl.getName(), this);
        panel.add(lvl, c);
        c.gridx++;
        
    }
    
    return panel;

}// end of ControlPanelBasic::createTransformPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::setStateOfAllCheckboxesInAGroup
//
// Sets mode of all checkboxex in the group specified at the end of
// pActionCommand to pState.
//

private void setStateOfAllCheckboxesInAGroup(String pActionCommand,
                                                                boolean pState)
{

    String groupName = pActionCommand.split(",")[1];

    Iterator<ChannelInfo> itr = channelList.iterator();

    while(itr.hasNext()){

        ChannelInfo chInfo = itr.next();

        if(chInfo.calPanelGroup.equals(groupName)){

            parentActionListener.actionPerformed(new ActionEvent(this, 1,
                                            "Update Channel,"
                                            + chInfo.onOffBox.getName() + ","
                                            + pState));

            chInfo.onOffBox.setSelected(pState);

        }
    }

}// end of ControlPanelBasic::setStateOfAllCheckboxesInAGroup
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createAllOnOrOffButton
//
// Creates and returns a JButton with text "All On" or "All Off". Parameter
// pOnOff should either be "On" or "Off" to select the function.
//
// Parameter pGroupName should be the name of the group controlled by the
// button. This will be passed in the action command when the button is pressed
// so the action listener will know which group is affected.
//

public JButton createAllOnOrOffButton(String pOnOff, String pGroupName)
{

    JButton button = new JButton("All " + pOnOff);
    button.setMargin(new Insets(0, 0, 0, 0));
    setSizes(button, 55, 15); //ignored when placed in grid layout

    button.setActionCommand("All " + pOnOff + "," + pGroupName);
    button.addActionListener(this);
    button.setToolTipText("Turns all channels in this group "
                                                + pOnOff.toLowerCase() + ".");

    return(button);

}// end of ControlPanelBasic::createAllOnOrOffButton
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createXYPositionPanel
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

}// end of ControlPanelBasic::createXYPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createViewFromPositionPanel
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

}// end of ControlPanelBasic::createViewFromPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createViewAtPositionPanel
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

}// end of ControlPanelBasic::createViewAtPositionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createRotationPanel
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

}// end of ControlPanelBasic::createRotationPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createViewnAnglePanel
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

}// end of ControlPanelBasic::createViewAnglePanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::createChartControlsPanel
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

}// end of ControlPanelBasic::createChartControlsPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// UTControls::createColorSwatch
//
// Creates a color swatch for use in displaying colors.
//

public ImageIcon createColorSwatch(Color pColor)
{

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gs = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gs.getDefaultConfiguration();

    //Java tutorial suggests checking the following cast with "instanceof", to
    //avoid runtime errors but this seems pointless as the cast MUST work for
    //the program to work so it will crash regardless if the cast is bad

    //define the points for a polygon shaped like a paint splotch
    int[] xPoints = {0,  7, 8, 11, 14, 16, 19, 19, 20, 18, 17, 14, 13, 11,
                                                   7,  7,  4,  4,  0,  2, 3, 0};
    int[] yPoints = {5,  4, 0,  3,  0,  4,  2,  8,  9, 12, 16, 14, 17, 19,
                                                  17, 15, 16, 14, 12, 10, 9, 5};


    // have to create a new image buffer for each icon because the icon continues
    // to use the image
    //create an image to store the plot on so it can be copied to the screen
    //during repaint

    BufferedImage imageBuffer;

    imageBuffer = (gc.createCompatibleImage(20, 20, Transparency.OPAQUE));
    Graphics2D g2 = (Graphics2D)imageBuffer.getGraphics();
    //fill the image with same color as menu background
    g2.setColor(new Color(238, 238, 238));
    g2.fillRect(0, 0, imageBuffer.getWidth(), imageBuffer.getHeight());
    g2.setColor(pColor);
    g2.fillPolygon(xPoints, yPoints, 22);

    return new ImageIcon(imageBuffer);

}//end of UTControls::createColorSwatch
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::resetAll
//
// Resets all values and child values to default.
//

public void resetAll()
{

}// end of ControlPanelBasic::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::getAllValues
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

    values.add("Channel Control Panel");

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

}// end of ControlPanelBasic::getAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::setAllValues
//
// Sets all values in the different controls from objects in an ArrayList.
//
// The first object will always be a string describing the control group.
//

@Override
public void setAllValues(ArrayList<Object> pValues)
{

    if(pValues == null) { return; }

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

}// end of ControlPanelBasic::setAllValues
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::addVerticalSpacer
//
// Adds a vertical spacer of pNumPixels height to JPanel pTarget.
//

public void addVerticalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(0,pNumPixels)));

}// end of ControlPanelBasic::addVerticalSpacer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::addHorizontalSpacer
//
// Adds a horizontal spacer of pNumPixels width to JPanel pTarget.
//

public void addHorizontalSpacer(JPanel pTarget, int pNumPixels)
{

    pTarget.add(Box.createRigidArea(new Dimension(pNumPixels,0)));

}// end of ControlPanelBasic::addHorizontalSpacer
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::actionPerformedLocal
//
// Responds to events which require action by this object.
//
// Returns true if the action is not to be handled by other listeners.
//

@Override
protected boolean actionPerformedLocal(ActionEvent e)
{

    if ("Expand Chart Height".equals(e.getActionCommand())
        || "Set Normal Chart Height".equals(e.getActionCommand())) {
        handleExpandButtonClick();
        return(true);
    }

    if (e.getActionCommand().startsWith("All On")){
        setStateOfAllCheckboxesInAGroup(e.getActionCommand(), true);
        return(true);
    }

    if (e.getActionCommand().startsWith("All Off")){
        setStateOfAllCheckboxesInAGroup(e.getActionCommand(), false);
        return(true);
    }
    
    if (e.getActionCommand().startsWith("Hide Chart")){

        if (!(e.getSource() instanceof JCheckBox)) { return(false); }
        JCheckBox box = (JCheckBox)e.getSource();

        parentActionListener.actionPerformed(new ActionEvent(this, 1,
                                e.getActionCommand() + "," + box.isSelected()));
        
        return(true);
    }

    if (e.getActionCommand().startsWith("On-Off Checkbox")){

        if (!(e.getSource() instanceof JCheckBox)) { return(false); }
        JCheckBox box = (JCheckBox)e.getSource();

        parentActionListener.actionPerformed(new ActionEvent(this, 1,
                "Update Channel," + box.getName() + "," + box.isSelected()));
        return(true);
    }

    return(false);

}//end of ControlPanelBasic::actionPerformedLocal
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::handleExpandButtonClick
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

}//end of ControlPanelBasic::handleExpandButtonClick
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::setSizes
//
// Sets the min, max, and preferred sizes of pComponent to pWidth and pHeight.
//

private void setSizes(Component pComponent, int pWidth, int pHeight)
{

    pComponent.setMinimumSize(new Dimension(pWidth, pHeight));
    pComponent.setPreferredSize(new Dimension(pWidth, pHeight));
    pComponent.setMaximumSize(new Dimension(pWidth, pHeight));

}//end of ControlPanelBasic::setSizes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::handleSpinnerChange
//
// Processes changes to Spinner values. The spinner's name should contain
// the name of the component along with the Group number, the index number of
// the channel in the cal channel list, Device number and the Channel number.
//

@Override
public void handleSpinnerChange(MFloatSpinner pSpinner)
{

    if (pSpinner == null) { return; }

    String name = pSpinner.getName();

    if(name == null){ return; }

    if (name.startsWith("Gain Spinner")){
        //pass the name along with the value back to the parent listener
        parentActionListener.actionPerformed(new ActionEvent(
                this, 1, "Update Channel," + name + "," + pSpinner.getText()));
        return;
    }

    if (name.startsWith("Offset Spinner")){
        //pass the name along with the value back to the parent listener
        parentActionListener.actionPerformed(new ActionEvent(
                this, 1, "Update Channel," + name + "," + pSpinner.getText()));
        return;
    }

    if (name.startsWith("Threshold Spinner")){
        //pass the name along with the value back to the parent listener
        parentActionListener.actionPerformed(new ActionEvent(
                this, 1, "Update Threshold," + name + "," + pSpinner.getText()));
        return;
    }
    
    if (name.startsWith("Graph Y-Offset Spinner")){
        //pass the name along with the value back to the parent listener
        parentActionListener.actionPerformed(new ActionEvent(
                this, 1, "Update Graph Y-Offset," + name + "," + pSpinner.getText()));
        return;
    }

}//end of ControlPanelBasic::handleSpinnerChange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::handleOnOffCheckBoxRightClick
//
// Handles right-click on On/Off checkboxes by turning on the clicked box and
// turning off all other boxes in the group.
//
// The parameter pName should contain the name of the component along with the
// Group number, the index number of the channel in the cal channel list,
// Device number and the Channel number.
//

private void handleOnOffCheckBoxRightClick(String pName)
{

    String[] split = pName.split(",");

    String groupName = split[1];
    int index = Integer.parseInt(split[2]);

    setStateOfAllCheckboxesInAGroup("All Off" + "," + groupName, false);

    channelList.get(index).onOffBox.setSelected(true);

}//end of ControlPanelBasic::handleOnOffCheckBoxRightClick
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::mouseClicked
//
// Responds when the mouse button is released while over a component which is
// listening to the mouse.
//
// NOTE: The controls which trigger this event by right click are expected to
// be valid controls for copying between channels.  If other controls trigger
// this event, they should be trapped and processed at the beginning of the
// function to prevent their being added to the copy item list.
//

@Override
public void mouseClicked(MouseEvent e)
{

    int button = e.getButton();

    String name = e.getComponent().getName();

    //catch left clicks
    if (button == MouseEvent.BUTTON1){
        return;
    }

    //catch right clicks
    if (button == MouseEvent.BUTTON3){

        if(name.startsWith("On-Off Checkbox")){
            handleOnOffCheckBoxRightClick(name);
            return;
        }

        return;
    }

}//end of ControlPanelBasic::mouseClicked
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ControlPanelBasic::(various mouse listener functions)
//
// These functions are implemented per requirements of interface MouseListener
// but do nothing at the present time.  As code is added to each function, it
// should be moved from this section and formatted properly.
//

@Override
public void mousePressed(MouseEvent e){}

@Override
public void mouseReleased(MouseEvent e){}

@Override
public void mouseEntered(MouseEvent e){}

@Override
public void mouseExited(MouseEvent e){}

//end of ControlPanelBasic::(various mouse listener functions)
//-----------------------------------------------------------------------------

}//end of class ControlPanelBasic
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

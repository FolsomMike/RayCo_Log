/******************************************************************************
* Title: GUIDataSet.java
* Author: Mike Schoonover
* Date: 01/20/15  
* 
* Purpose:
*
* This class encapsulates various data variables used for transferring values
* between the View GUI and the Controller.
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class GUIDataSet
//

public class GUIDataSet
{
    
    public int chartGroupNum;
    public int chartNum;
    public int graphNum;
    public int traceNum;

    public static final int RESET = -1;
    
//-----------------------------------------------------------------------------
// GUIDataSet::GUIDataSet (constructor)
//

public GUIDataSet()
{
    
}//end of GUIDataSet::GUIDataSet (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// GUIDataSet::resetGUIChildNums
//
// Resets the variables used to refer to index number of various types of
// GUI objects.
//

public void resetGUIChildNums()
{
    
    chartGroupNum = RESET;
    chartNum = RESET;
    graphNum = RESET;
    traceNum = RESET;
    
}//end of GUIDataSet::resetGUIChildNums
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// GUIDataSet::setGUIChildNums
//
// Sets to pValue the variables used to refer to index number of various types
// of GUI objects.
//

public void setGUIChildNums(int pValue)
{
    
    chartGroupNum = pValue;
    chartNum = pValue;
    graphNum = pValue;
    traceNum = pValue;
    
}//end of GUIDataSet::setGUIChildNums
//-----------------------------------------------------------------------------

}//end of class GUIDataSet
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

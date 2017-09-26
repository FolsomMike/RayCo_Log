/******************************************************************************
* Title: DataFlags.java
* Author: Hunter Schoonover
* Date: 09/26/17
*
* Purpose:
*
* Flags used for data transfer.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package model;

//-----------------------------------------------------------------------------
// class DataFlags
//

public class DataFlags{

// flags and constants to store meta data for each datapoint
public static final int FLAG_RESET_VALUE =  0x0000000000000000;
public final static int DATA_VALID =        0x0000000000000001;
public final static int DATA_READY =        0x0000000000000002;
public final static int DATA_ERASED =       0x0000000000000004;
public static final int VERTICAL_BAR =      0x0000000000000008;
public static final int CIRCLE =            0x0000000000000010;
public static final int CLEAR_ALL_FLAGS = 0;
public static final int MIN_MAX_FLAGGED =         0x10000;
public static final int SEGMENT_START_SEPARATOR = 0x20000;
public static final int SEGMENT_END_SEPARATOR =   0x40000;
public static final int END_MASK_MARK =           0x80000;
public static final int IN_PROCESS =             0x400000;
public static final int MARKER_SQUARE =          0x800000;

public static final int CLEAR_CLOCK_MASK = 0xfffffe00;
public static final int THRESHOLD_MASK = 0x0000fe00;
public static final int TRIM_CLOCK_MASK = 0x1ff;
public static final int CLEAR_THRESHOLD_MASK = 0xffff01ff;
public static final int TRIM_THRESHOLD_MASK = 0x7f;
public static final int CLEAR_DATA_ERASED = ~DATA_ERASED;

}//end of class DataFlags
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------


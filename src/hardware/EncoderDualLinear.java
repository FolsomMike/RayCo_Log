/******************************************************************************
* Title: EncoderDualLinear.java
* Author: Mike Schoonover
* Date: 11/16/16
*
* Purpose:
*
* This class handles encoder inputs and distance conversions. It inherits from
* EncoderHandler. This child class handles helical or non-helical pass-through
* units with two linear encoders...one before the inspection heads and one
* after.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package hardware;

//-----------------------------------------------------------------------------

import java.text.DecimalFormat;
import javax.swing.JLabel;
import model.SharedSettings;



//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class EncoderDualLinear
//
//

public class EncoderDualLinear extends EncoderHandler{
    

    int encoderInUse = ENCODER1;

    int encoder1CountAtSwitchToEncoder2 = 0;    
    double encoder1InchesAtSwitchToEncoder2 = 0;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    public final static int ENCODER1 = 0, ENCODER2 = 1;

//-----------------------------------------------------------------------------
// EncoderDualLinear::EncoderDualLinear (constructor)
//

public EncoderDualLinear(EncoderValues pEncVals, SharedSettings pSettings)
{

    super(pEncVals, pSettings);

}//end of EncoderDualLinear::EncoderDualLinear (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//

@Override
public void init()
{


}//end of EncoderDualLinear::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::resetAll
//
// Resets all values to default.
//
// NOTE: child classes should call this function.
//

@Override
public void resetAll()
{

    super.resetAll();
    
    encoderInUse = ENCODER1;

    encoder1CountAtSwitchToEncoder2 = 0;    
    encoder1InchesAtSwitchToEncoder2 = 0.0;

}//end of EncoderDualLinear::resetAll
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::setCurrentLinearDirectionAsFoward
//
// Sets the direction designated as Forward for whichever encoder(s) are
// used for tracking linear position
//

@Override
public void setCurrentLinearDirectionAsFoward()
{
    
    // encoder 1 direction is used to set the direction for both encoders
    // as encoder 1 will be in use when inspection starts - the time when the
    // direction is determined
    
    encoder1FwdDir = encoder1Dir; encoder2FwdDir = encoder1Dir;
    
}//end of EncoderDualLinear::setCurrentLinearDirectionAsFoward
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::calculateTally
//
// Calculates the length of the tube.
//

@Override
public double calculateTally()
{

    super.calculateTally();

    double totalEncoderInches = encoder1InchesAtSwitchToEncoder2 +
                encVals.convertEncoder2CountsToInches(encoder2 - encoder2Start);
 
    //subtract the distance between the perpendicular eyes -- tracking
    //starts when lead eye hits pipe but ends when trailing eye clears,
    //so the extra distance between the eyes must be accounted for...
    //also subtract the length of the end stop which will be non-zero
    //for systems where the away laser triggers on that instead of the
    //end of the tube

    totalEncoderInches -= encVals.photoEyeToPhotoEyeDistance;
    
    totalEncoderInches += linearPositionCorrection;
    
    measuredLengthFt = totalEncoderInches/ 12.0; //convert to decimal feet

    return(measuredLengthFt);
    
}//end of EncoderDualLinear::calculateTally
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::calculateTruncatedTally
//
// Calculates the length of the tube which has passed by the first sensor
// thus far.
//
// Uses encoder 2 which tracks the linear position of the trolley.
//
// This is NOT the total tally of the tube.
//
// The distance between the vertical eyes is not accounted for when calculating
// the distance traveled thus far -- the trailing photo-eye may not even have
// reached the end of the test piece.
//
// The correction factor is added into the returned value.
//

@Override
public double calculateTruncatedTally()
{

    double totalEncoderInches = encoder1InchesAtSwitchToEncoder2 +
                encVals.convertEncoder2CountsToInches(encoder2 - encoder2Start);
 
    totalEncoderInches += linearPositionCorrection;
    
    measuredLengthFt = totalEncoderInches / 12.0; //convert to decimal feet
  
    return(measuredLengthFt);

}//end of EncoderDualLinear::calculateTruncatedTally
//-----------------------------------------------------------------------------
    
//-----------------------------------------------------------------------------
// EncoderDualLinear::getDirectionSetForLinearForward
//
// Returns the direction of linear travel which is currently defined as 
// "forward" or "reverse" for whichever encoder(s) is handling linear tracking.
//
// Trolley units can inspect in both directions, so the concept of "forward"
// and "reverse" are flipped depending on the end from which the trolley
// started.
//
// Pass through units generally have the same direction for both encoders.
//

@Override
public int getDirectionSetForLinearFoward()
{
    
    return(encoderInUse == ENCODER1 ? encoder1FwdDir : encoder2FwdDir);
        
}//end of EncoderDualLinear::getDirectionSetForLinearFoward
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::recordLinearStartCount
//
// Records the current count of the encoders tracking the linear position.
//

@Override
public void recordLinearStartCount()
{

    encoder1Start = encoder1; encoder2Start = encoder2;
    
}//end of EncoderDualLinear::recordLinearStartCount
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::getAbsValueLinearDistanceMovedInches
//
// Returns the absolute value of the total distance moved in inches since the
// linear start count was recorded. Returns absolute value so head moving in
// reverse works the same as forward. 
//
// If the pipe has not yet reached the second encoder, only the first encoder
// is used in the calculation. If the pipe has reached and switched over to
// using the second encoder, then the counts accumulated by the first encoder
// until the switch point are added with the counts of the second encoder
// since the switch point to get the cumulative total.
//
// The dual encoder systems may not need the absolute value, but it is returned
// to maintain compatability.
//
// A correction factor is added to the position. That value is computed when
// the end of the tube passes a fixed sensor position which reports the
// actual position of the end of the tube. This allows inaccuracies in tracking
// to be corrected as the tube passes each sensor.
//

@Override
public double getAbsValueLinearDistanceMovedInches()
{
    
    double position;
    
    if(encoderInUse == ENCODER1){        
        position = 
                encVals.convertEncoder1CountsToInches(encoder1 - encoder1Start);
    }else{
        position = encoder1InchesAtSwitchToEncoder2 +
                encVals.convertEncoder2CountsToInches(encoder2 - encoder2Start);
        
    }
  
    linearDistanceMovedInches = Math.abs(position);

    linearDistanceMovedInchesCorrected = linearDistanceMovedInches
                                                    + linearPositionCorrection;

//debug mks    displayMsg("" + linearDistanceMovedInchesCorrected);
    
    return(linearDistanceMovedInchesCorrected);

}//end of EncoderDualLinear::getAbsValueLinearDistanceMovedInches
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::handleEncoderSwitchOver
//
// For systems with multiple linear tracking encoders, this function handles
// switching from the first encoder to the second encoder once the tube has
// passed the second encoder. It also handles switching back to the first
// encoder in case the tube is reversed.
//
// It is assumed that the switch over points are not exact, so this function can
// be called a bit early or a bit late after the switch over points have been
// passed with no issues.
//

@Override
public void handleEncoderSwitchOver()
{

    if (encoderInUse == ENCODER1 && 
       linearDistanceMovedInches > encVals.distanceToSwitchToEncoder2){
        
        sharedSettings.displayMsg("Switching to Encoder 2...");

        //switch to encoder 2        
        encoderInUse = ENCODER2;
  
        //encoder 2 starts tracking at current position
        encoder2Start = encoder2;
        
        //compute the distance tracked by encoder 1 in counts and inches
        
        encoder1CountAtSwitchToEncoder2 = encoder1 - encoder1Start;
    
        encoder1InchesAtSwitchToEncoder2 =
         encVals.convertEncoder1CountsToInches(encoder1CountAtSwitchToEncoder2);
        
    }else if (encoderInUse == ENCODER2 && 
        linearDistanceMovedInches < encVals.distanceToSwitchToEncoder1){
        
        sharedSettings.displayMsg("Switching to Encoder 1...");
        
        //switch to encoder 1
        encoderInUse = ENCODER1;        

        //set encoder 1 counts and start counts appropriately
        encoder1 = encoder1CountAtSwitchToEncoder2; encoder1Start = 0;
        
    }

}//end of EncoderDualLinear::handleEncoderSwitchOver
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::handleLinearPositionOverride
//
// Handles adjusting the linearPositionCorrection variable so that the
// linear position equals pOverride. This allows the linear position to be
// corrected to match a distance measure by a sensor.
//
// The override is only applied if it is greater than the current linear
// position to prevent erasing data. Later, code can be added to undo
// the override as the tube reverses back over each sensor.
//
// If pOverride equals Integer.MAX_VALUE, then nothing is done.
//

@Override
public void handleLinearPositionOverride(double pOverride)
{

    if(pOverride == Integer.MAX_VALUE){ return; }

/* debug mks -- this section prevents shortening the tube
    if(pOverride <= linearDistanceMovedInches) {
    displayMsg("not corrected " + createAdjustmentMsgSuffix(pOverride));

        return;
    }
*/
    linearPositionCorrection = pOverride - linearDistanceMovedInches;

    displayMsg("corrected "  + createAdjustmentMsgSuffix(pOverride));

}//end of EncoderDualLinear::handleLinearPositionOverride
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderDualLinear::createAdjustmentMsgSuffix
//
// Creates a suffix with the current corrected position, the new suggested
// adjustment position pOverride, and the difference between the two.

private String createAdjustmentMsgSuffix(double pOverride)
{

    return(decimalFormat.format(linearDistanceMovedInchesCorrected)
            + " to "
            + decimalFormat.format(pOverride)
            + " ("
            + decimalFormat.format(
                                pOverride - linearDistanceMovedInchesCorrected)
            + ")"
            );

}//end of EncoderDualLinear::createAdjustmentMsgSuffix
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// EncoderHandler::allowTraceUpdate
//
// Determines if the trace should be updated based on the number and sign
// of the pixels moved value.
//
// Currently, this class disallows trace reversal because when the distance
// correction attempts to shorten the tube, it uses negative pixels moved
// values to do this. In that case, a correction would reverse the trace.
// After a better method is found for shortening corrections, this code
// should be changed to allow all moves (or this method removed from the
// EncoderHandler classes and subclasses).
//

@Override
public boolean allowTraceUpdate(int pPixelsMoved)
{

    if (pPixelsMoved <= 0) { return(false); } else{ return(true); }

}//end of EncoderHandler::handleLinearPositionOverride
//-----------------------------------------------------------------------------

}//end of class EncoderDualLinear
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

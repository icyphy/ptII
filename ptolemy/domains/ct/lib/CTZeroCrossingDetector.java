/* One line description of file.

 Copyright (c) 1998-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CTZeroCrossingDetector
/**
This is a event detector that monitors the signal coming from "trigger"
input. If the trigger is zero, then output the token from "input."
This actor controls the integration step size to accurately resolve 
the time that the zero crossing happens.
It has a parameter "ErrorTolerance," which controls how accurate the 
zero 
@author Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTZeroCrossingDetector extends CTActor
        implements CTEventGenerator, CTStepSizeControlActor {

    public static boolean DEBUG = false;

    /** Construct a CTActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor has two input, "trigger" and "input", and one output,
     *  "output." Both of them are single ports.
     *
     *  @param CompositeActor The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */
    public CTZeroCrossingDetector(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setDeclaredType(DoubleToken.class);
        trigger = new TypedIOPort(this, "trigger");
        trigger.setMultiport(false);
        trigger.setInput(true);
        trigger.setOutput(false);
        trigger.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setMultiport(false);
        output.setInput(false);
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);
        _errorTolerance = (double)1e-4;
        _paramErrorTolerance = new CTParameter(this, "ErrorTolerance", 
                new DoubleToken(_errorTolerance));

    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initilaize, chech if the director is a CTMixedSignalDirector.
     *  
     *  If the director is not a CTMixedSignalDirector throw an exception.
     */
    public void initialize() throws IllegalActionException {
        if(!(getDirector() instanceof CTMixedSignalDirector)) {
            throw new IllegalActionException(this,
                " Must be executed after a CTMixedSignalDirector.");
        }
        updateParameters();
        _first = true;
        if(DEBUG) {
            System.out.println("ZeroCrossingDetector initialize");
        }
    }

    /** Fire: if the current time is the event time, request the end
     *  of this fire.
     */
    public void fire() throws IllegalActionException {
        _thisTrg = ((DoubleToken) trigger.get(0)).doubleValue();
        _inputToken = input.get(0);
    }

    /** Postfire: if this is the sampling point, output a token with the
     *  input signal as the value. Otherwise output no token.
     *  register the next sampling time as the next break point.
     */
    public boolean postfire() throws IllegalActionException {
        _lastTrg = _thisTrg;
        return true;
    }

    /** Return true if this step did not cross zero.
     */
    public boolean isThisStepSuccessful() {
        if (_first) {
            _first = false;
            return true;
        }
        if (Math.abs(_thisTrg) < _errorTolerance) {
            if (_enabled) {
                //double tnow = dir.getCurrentTime(); 
                //dir.setFireEndTime(tnow);
                _eventNow = true;
                if(DEBUG) {
                    System.out.println("Event Detected:" + 
                            getDirector().getCurrentTime());
                }
                _enabled = false;
            }
            _eventMissed = false;
            return true;
        } else {
            if(!_enabled) {  // if last step is a zero, always successful.
                _enabled = true;
            } else {
                if ((_lastTrg * _thisTrg) < 0.0) {
                    CTDirector dir = (CTDirector)getDirector();
                    _eventMissed = true;
                    _refineStep = (-_lastTrg*dir.getCurrentStepSize())/
                        (_thisTrg-_lastTrg);
                    return false;
                }
            }
            _eventMissed = false;
            return true;
        }
    }
    
    /** Return the maximum Double, since this actor does not predict 
     *  step size.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     */
    public double refinedStepSize() {
        if(_eventMissed) {
            return _refineStep;
        } 
        return ((CTDirector)getDirector()).getCurrentStepSize();
    }
                             
    /** Return true if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        return _eventNow;
    }

    /** Emit the event. There's no current event after emitting it.
     */
    public void emitCurrentEvents() throws IllegalActionException{
        if(_eventNow) {
            output.broadcast(_inputToken);
            _eventNow = false;
        }
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     */
    public void updateParameters() throws IllegalActionException{
        double p = ((DoubleToken)_paramErrorTolerance.getToken()
                    ).doubleValue();
        if(p <= 0) {
            throw new IllegalActionException(this,
                    " Sample period must be greater than 0.");
        }
        _errorTolerance = p;
    }



    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////
    public TypedIOPort input;
    public TypedIOPort output;
    public TypedIOPort trigger;

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////



    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Parameter, the sample period.
    private CTParameter _paramErrorTolerance;
    private double _errorTolerance;

    private boolean _eventMissed = false;
    private double _refineStep;
    private double _lastTrg;
    private double _thisTrg;
    private boolean _enabled;
    private boolean _eventNow = false;
    private boolean _first = true;
    private  Token _inputToken;
}

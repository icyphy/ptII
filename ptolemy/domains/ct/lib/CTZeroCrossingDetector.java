/* One line description of file.

 Copyright (c) 1998 The Regents of the University of California.
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
This actor periodically sample the input signal and generate an event
which has the value of the input signal.
@author Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTZeroCrossingDetector extends CTActor
        implements CTEventGenerateActor {

    public static final boolean DEBUG = true;

    /** Construct a CTActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  A CTActor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
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
       
        if(DEBUG) {
            System.out.println("ZeroCrossingDetector initialize");
        }
    }

    /** Fire: if the current time is the event time, request the end
     *  of this fire.
     */
    public void fire() throws IllegalActionException {
        _thisTrg = ((DoubleToken) trigger.get(0)).doubleValue();
    }

    /** Postfire: if this is the sampling point, output a token with the
     *  input signal as the value. Otherwise output no token.
     *  register the next sampling time as the next break point.
     */
    public boolean postfire() throws IllegalActionException {
        if (_eventNow) {
            if(input.hasToken(0)) {
                DoubleToken value = (DoubleToken) input.get(0);
                if(DEBUG) {
                    CTDirector dir = (CTDirector) getDirector();
                    double tnow = dir.getCurrentTime(); 
                    System.out.println(" Emit an event at" + tnow
                    +  " with the value: " +value.doubleValue());
                }
                output.broadcast(value);
            }
            _eventNow = false;
        }
        _lastTrg = _thisTrg;
        return true;
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

    /** Return true if there is defintly an event missed in the
     *  last step.
     */
    public boolean hasMissedEvent() {

	if (_first_ask) {
            _first_ask = false;
            return false;
        }

        CTMixedSignalDirector dir = (CTMixedSignalDirector) getDirector();
        if (Math.abs(_thisTrg) < _errorTolerance) {
            if (_enabled) {
                double tnow = dir.getCurrentTime(); 
                dir.setFireEndTime(tnow);
                _eventNow = true;
                if(DEBUG) {
                    System.out.println("set FireEndTime:" + tnow);
                }
                _enabled = false;
            }
            _eventMissed = false;
            return false;
        } else {
            _enabled = true;
            if (_lastTrg < 0.0) {
                if (_thisTrg > 0.0) {
                    _eventMissed = true;
                    _refineStep = (-_lastTrg*dir.getCurrentStepSize())/
                        (_thisTrg-_lastTrg);
                    return true;
                }
                _eventMissed = false;
            } else {
                if (_thisTrg < 0.0) {
                     _eventMissed = true;
                    _refineStep = (-_lastTrg*dir.getCurrentStepSize())/
                        (_thisTrg-_lastTrg);
                    return true;
                }
                _eventMissed = false;
            }
        }
        return false;
    }
       
    /** If there is a missed event return the expected sample time
     *  - current time; else return the current step size.
     */
    public double refineStepSize() {
        if(_eventMissed) {
            _eventMissed = false;
            return _refineStep;
        }
        CTDirector dir = (CTDirector)getDirector();
        return dir.getCurrentStepSize();
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
    private boolean _first_ask = true;
}

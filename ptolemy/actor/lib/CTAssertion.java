/* An actor that does assertion specific for CT domain.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CTAssertion
/**

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 2.0
*/

public class CTAssertion extends Assertion implements CTStepSizeControlActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CTAssertion(CompositeEntity container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	// threshold is used for adjust integrator step size only
	threshold = new Parameter(this, "threshold",
				  new DoubleToken(0.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameter that is to adjust the step size of integrator.
     *  Typically, this parameter contains a DoubleToken with the value as
     *  the limit of the assertion.
     */
    public Parameter threshold;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>threshold<i> then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    public void attributeTypeChanged(Attribute attribute)
	throws IllegalActionException {
	super.attributeTypeChanged(attribute);
        if (attribute == threshold) {
	    double p = ((DoubleToken)threshold.getToken()).doubleValue();
	    _threshold = p;
	}
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */

    public Object clone(Workspace workspace)
	throws CloneNotSupportedException {
        CTAssertion newObject = (CTAssertion) super.clone(workspace);
        return newObject;
    }

    /** Consume one input token.
     *  @exception IllegalActionException If the get() method of IOport
     *  or getToken() method of Variable throws it.
     */
    public void fire() throws IllegalActionException {

	super.fire();
	// consume the input.
	// The assertion actor always has only one input port.
	// FIXME: handle multiports.

	TypedIOPort input = (TypedIOPort) inputPortList().get(0);
	_thisInput = ((DoubleToken) input.get(0)).doubleValue();
        _threshold = ((DoubleToken) threshold.getToken()).doubleValue();
		
	if (_debugging)
	    _debug(getFullName() + " consuming input Token" + _thisInput);
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
    }
    
    /** Return true if this step does not cross the threshold.
     *  The current trigger
     *  token will be compared to the previous trigger token. If they
     *  cross the level threshold, this step is not accurate.
     *  A special case is taken care so that if the previous trigger
     *  and the current trigger both equal to the level value,
     *  then no new event is
     *  detected. If this step crosses the level threshold,
     *  then the refined integration
     *  step size is computed by linear interpolation.
     *  If this is the first iteration after initialize() is called,
     *  then always return false, since there is no history to compare with.
     *  @return True if the trigger input in this integration step
     *          does not cross the level threshold.
     */
    public boolean isThisStepAccurate() {
        // FIXME: Handle multiports
        // It needs a list of cached of last inputs.

	if (_first) {
            _first = false;
            return true;
        }
        if (_debugging) {
            _debug(this.getFullName() + " This input " + _thisInput);
            _debug(this.getFullName() + " The last input " + _lastInput);
        }
        if (Math.abs(_thisInput - _threshold) < _errorTolerance) {
            if (_enabled) {
                _enabled = false;
            }
            _eventMissed = false;
            return true;
        } else {
            if (!_enabled) {  // if last step is a level, always accurate.
                _enabled = true;
            } else {
                if ((_lastInput - _threshold) * (_thisInput - _threshold)
		    < 0.0) {

                    CTDirector dir = (CTDirector)getDirector();
                    _eventMissed = true;
                    // The refined step size is a linear interpolation.
                    _refineStep = (Math.abs(_lastInput - _threshold)
				   *dir.getCurrentStepSize())
                        /Math.abs(_thisInput-_lastInput);
                    if (_debugging) _debug(getFullName() +
					   " Refined step at" +  _refineStep);
                    return false;
                }
            }
            _eventMissed = false;
            return true;
        }
    }

    /** Evaluate the assertion and increment the iteration count.
     *  @exception IllegalActionException If the super class throws it.
     */
    public boolean postfire() throws IllegalActionException {

	_lastInput = _thisInput;

	return super.postfire();
    }

    /** Return the maximum Double, since this actor does not predict
     *  step size.
     *  @return java.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        if (_eventMissed) {
            return _refineStep;
        }
        return ((CTDirector)getDirector()).getCurrentStepSize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _thisInput;
    private double _lastInput;

    private double _threshold;

    // flag indicating if the event detection is enable for this step
    private boolean _enabled;

    // flag indicating if this is the first iteration in the execution,
    private boolean _first = true;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // refined step size.
    private double _refineStep;

}

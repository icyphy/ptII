/* Monitor integration steps so that the threshold is not crossed in one step.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;

//////////////////////////////////////////////////////////////////////////
//// ThresholdMonitor
/**
Monitor integration steps so that the threshold is not crossed in one step.
This actor has one input port, but no output port. If functionality is
solely devoted to controlling the integration step size. It has two
parameters "thresholdWidth" and "thresholdCenter", which have default
value 1e-2 and 0, respectively.
@author  Jie Liu
@version $Id$
*/
//FIXME: need to use the new parameter mechanism.

public class ThresholdMonitor extends TypedAtomicActor
    implements CTStepSizeControlActor, TimedActor{
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ThresholdMonitor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setTypeEquals(BaseType.DOUBLE);

        _thWidth = (double)1e-2;
        thresholdWidth = new Parameter(this, "thresholdWidth",
                new DoubleToken(_thWidth));

        _thCenter = (double)0.0;
        thresholdCenter = new Parameter(this, "thresholdCenter",
                new DoubleToken(_thCenter));

        _lowerBound = -5e-3;
        _upperBound = 5e-3;

    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////
    /** The input port, single port with type double.
     */
    public TypedIOPort input;

    /** The parameter for the width of the threshold.
     */
    public Parameter thresholdWidth;

    /** The parameter for the center of the threshold.
     */
    public  Parameter thresholdCenter;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

   /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException If there is no token in the
     *  parameter.
     */
    public void attributeChanged(Attribute att) throws IllegalActionException {
        _thCenter = ((DoubleToken)thresholdCenter.getToken()).doubleValue();
        _thWidth = Math.abs(
                ((DoubleToken)thresholdWidth.getToken()).doubleValue());

        _lowerBound = _thCenter - _thWidth/(double)2.0;
        _upperBound = _thCenter + _thWidth/(double)2.0;
    }

    /** Consume the current input.
     *  @exception IllegalActionException If there is no input token.
     */
    public void fire() throws IllegalActionException {
        _debug("Monitor" + getFullName() + " fired.");
        _thisInput = ((DoubleToken) input.get(0)).doubleValue();
    }

    /** Setup the internal variables so that there is no history.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _first = true;
    }

    /** Return true if this step did not cross the threshold.
     *  @return True if this step did not cross the threshold.
     */
    public boolean isThisStepSuccessful() {
        if (!_first) {
            if (((_lastInput >= _upperBound) && (_thisInput <= _lowerBound)) ||
                    ((_lastInput <= _lowerBound) &&
                            (_thisInput >= _upperBound))) {
                _debug(getFullName() + "one step crosses the threshold" +
                        "cutting the step size in half.");
                _success = false;
                return false;
            }
        }
        _success = true;
        return true;
    }

    /** Return true always. Make this input the history.
     *  @return True always.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        _lastInput = _thisInput;
        _first = false;
        return true;
    }

    /** Return java.lang.Double.MAX_VALUE, since this actor does not predict
     *  step sizes.
     *  @return java.lang.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return half the current step size if the step is not successful.
     *  Otherwise, return the current step size.
     *  @return Half the current step size if the step is not successful.
     */
    public double refinedStepSize() {
        CTDirector dir = (CTDirector)getDirector();
        if(!_success) {
            return 0.5*dir.getCurrentStepSize();
        }
        return dir.getCurrentStepSize();
    }

 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // local copy of the threshold width
    private double _thWidth;

    // local copy of the threshold center.
    private double _thCenter;

    // flag indicting if this is the first iteration in an execution.
    private boolean _first;

    // flag indicating if the current step is successful
    private boolean _success;

    // upper bound of the input value, = thCenter + thWidth/2
    private double _upperBound;

    // lower bound of the input value, = thCenter - thWidth/2
    private double _lowerBound;

    // last input token value
    private double _lastInput;

    // this input token value.
    private double _thisInput;
}

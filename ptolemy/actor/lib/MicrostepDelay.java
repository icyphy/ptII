/* An actor that delays the input by the specified amount.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MicrostepDelay

/**
 This actor delays the input by one microstep. The output is always
 absent in the first firing. This actor is designed
 to be used in domains where the director implements
 SuperdenseTimeDirector, like Continuous and DE.
 Inputs are read only during the postfire() method.
 If an input is present, then this actor schedules itself to fire again
 to produce the just received token on the corresponding output channel in
 the next microstep at the same time stamp.
 <p>
 When this actor is used in the Continuous domain, it requires that its
 input be purely  discrete (specifically that it be absent at microstep 0).
 The reason for rejecting continuous inputs is that a continuous input
 would cause a stuttering Zeno condition, where time cannot advance.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class MicrostepDelay extends Transformer {
    /** Construct an actor with the specified container and name.
     *  Constrain that the output type to be the same as the input type.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MicrostepDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. Set a type
     *  constraint that the output type is the same as the that of input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MicrostepDelay newObject = (MicrostepDelay) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(input, output, 0.0);
    }

    /** Read one token from the input. Send out a token that is scheduled
     *  to be produced at the current time.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_pendingOutput != null) {
            output.send(0, _pendingOutput);
            if (_debugging) {
                _debug("Sending output. Value = " + _pendingOutput);
            }
        } else {
            // Nothing to send. Assert the output to be absent.
            output.send(0, null);
            if (_debugging) {
                _debug("Nothing to send. Asserting absent output at time "
                        + getDirector().getModelTime());
            }
        }
    }

    /** Initialize the states of this actor.
     *  @exception IllegalActionException If the director does
     *   not implement SuperdenseTimeDirector.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _pendingOutput = null;

        if (!(getDirector() instanceof SuperdenseTimeDirector)) {
            throw new IllegalActionException(
                    this,
                    "MicrostepDelay can only be used with a director that implements "
                            + "SuperdenseTimeDirector, such as ContinuousDirector or DEDirector.");
        }
    }

    /** Return false indicating that this actor can be fired even if
     *  the inputs are unknown.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Read the input, if there is one, and request refiring.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed, or if there is input and the current microstep is
     *  zero, or if the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // No point in using the isTime() method here, since we need
        // all the intermediate values.
        Director director = getDirector();
        if (input.hasToken(0)) {
            /* Do not enforce this. It is useful sometimes.
            int microstep = ((SuperdenseTimeDirector) director).getIndex();
            if (microstep == 0) {
                throw new IllegalActionException(this,
                        "Input is not purely discrete.");
            }
             */
            _pendingOutput = input.get(0);
            // Do not use fireAtCurrentTime() because if synchronizeToRealTime is set,
            // that will not necessarily match the current model time.
            director.fireAt(this, director.getModelTime());
            if (_debugging) {
                _debug("Read input with value = " + _pendingOutput);
            }
        } else {
            _pendingOutput = null;
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A token to send in the next microstep. */
    protected Token _pendingOutput;
}

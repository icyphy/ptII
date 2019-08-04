/* An actor that delays the input for a certain amount of real time.

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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Sleep

/**
 <p>On each firing, read at most one token from each input channel, sleep
 by the specified amount of real time, and then produce the same input
 tokens on the respective output channels. This actor calls Thread.sleep()
 in the fire() method, so the thread that calls fire() will be suspended.
 If fire() is called multiple times in one iteration, sleep is only called
 the first time.
 If the width of the output port is less than that of the input port,
 the tokens in the extra channels are lost.
 </p><p>
 The effect of this actor is different in different domains.
 In domains where all actors are iterated from within a single director
 thread (like SDF and DE), then multiple instances of this actor will
 result in cumulative time delays. That is, the time taken by an iteration
 of the model will be greater than the sum of the sleep times of all the
 instances. In domains where actors execute in their own thread (like PN
 and CSP), only the execution of the individual actor is slowed.
 Note that another way to slow down the execution of a model while running
 inside vergil is to turn on animation.</p>

 @author Jie Liu, Christopher Hylands, Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0

 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh) Review _wasSleepCalledInFireYet, esp locking
 */
public class Sleep extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sleep(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        sleepTime = new PortParameter(this, "sleepTime");
        sleepTime.setExpression("0L");
        sleepTime.setTypeEquals(BaseType.LONG);

        Port sleepPort = sleepTime.getPort();
        StringAttribute sleepCardinal = new StringAttribute(sleepPort,
                "_cardinal");
        sleepCardinal.setExpression("SOUTH");

        // Data type polymorphic, multiports.
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        output.setWidthEquals(input, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The sleep time in milliseconds. This has type long and default
     *  "0L".
     */
    public PortParameter sleepTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new instance of Sleep.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Sleep newObject = (Sleep) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setWidthEquals(newObject.input, true);
        return newObject;
    }

    /** Read input tokens, call Thread.sleep(), and then
     *  transfer tokens from inputs to outputs, at most one token from each
     *  channel.  If fire() is called twice in a row without an
     *  intervening call to either postfire() or prefire(), then no
     *  sleep is performed, an inputs are copied to the output immediately.
     *  <p>
     *  If the width of the output port is less than
     *  that of the input port, the tokens in the extra channels
     *  are lost.
     *  @exception IllegalActionException Not thrown in this base class
     */
    @Override
    public void fire() throws IllegalActionException {
        if (!_wasSleepCalledInFireYet) {
            _wasSleepCalledInFireYet = true;
            super.fire();
            sleepTime.update();

            int inputWidth = input.getWidth();
            Token[] inputs = new Token[inputWidth];

            for (int i = 0; i < inputWidth; i++) {
                if (input.hasToken(i)) {
                    inputs[i] = input.get(i);
                }
            }

            try {
                long sleepTimeValue = ((LongToken) sleepTime.getToken())
                        .longValue();

                if (_debugging) {
                    _debug(getName() + ": Wait for " + sleepTimeValue
                            + " milliseconds.");
                }

                Thread.sleep(sleepTimeValue);
            } catch (InterruptedException e) {
                // Ignore...
            }

            int outputWidth = output.getWidth();

            for (int i = 0; i < inputWidth; i++) {
                if (inputs[i] != null) {
                    if (i < outputWidth) {
                        output.send(i, inputs[i]);
                    }
                }
            }
        }
    }

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _wasSleepCalledInFireYet = false;
    }

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the parent class throws it.
     *  @return Whatever the superclass returns (probably true).
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _wasSleepCalledInFireYet = false;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if sleep was called in fire().  Thread.sleep() should only
     *   be called once in fire().
     */
    private boolean _wasSleepCalledInFireYet = false;
}

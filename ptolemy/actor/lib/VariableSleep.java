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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Sleep

/**
 An actor that calls Thread.sleep() on the current thread the first
 time fire() is called.  The sleep delays the inputs for a certain
 amount of real time, specified by the <i>sleepTime</i> input.

 <p>Note that one way to slow down the execution of a model while running
 inside vergil is to turn on animation.

 <p>If the width of the output port is less than that of the input port,
 the tokens in the extra channels are lost.

 @author Yang Zhao, based on Sleep by Jie Liu, Christopher Hylands
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class VariableSleep extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableSleep(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        defaultSleepTime = new Parameter(this, "defaultSleepTime",
                new LongToken(0));
        defaultSleepTime.setTypeEquals(BaseType.LONG);

        // Data type polymorphic, multiports.
        input.setMultiport(true);
        output.setMultiport(true);
        sleepTime = new TypedIOPort(this, "sleepTime", true, false);
        sleepTime.setTypeEquals(BaseType.LONG);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The sleepTime amount, in milliseconds
     *  This parameter must contain a LongToken.
     *  The default value of this parameter is 0, meaning
     *  that this actor will not sleep the current thread at all.
     */
    public Parameter defaultSleepTime;

    /** An input port receives the value of sleep time.
     */
    public TypedIOPort sleepTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call Thread.sleep() the first time fire is called and then
     *  transfer tokens from inputs to outputs, one token from each
     *  channel.  If fire() is called twice in a row without an
     *  intervening call to either postfire() or prefire(), then no
     *  output is produced.
     *  <p>If the width of the output port is less than
     *  that of the input port, the tokens in the extra channels
     *  are lost.
     *  @exception IllegalActionException Not thrown in this base class */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (!_wasSleepCalledInFireYet) {
            try {
                if (sleepTime.hasToken(0)) {
                    _sleepTime = ((LongToken) sleepTime.get(0)).longValue();
                } else {
                    _sleepTime = ((LongToken) defaultSleepTime.getToken())
                            .longValue();
                }

                if (_debugging) {
                    _debug(getName() + ": Wait for " + _sleepTime
                            + " milliseconds.");
                }

                Thread.sleep(_sleepTime);
            } catch (InterruptedException e) {
                // Ignore...
            }

            // Pull these out of the loop so we do not call them
            // more than once.
            int inputWidth = input.getWidth();
            int outputWidth = output.getWidth();

            for (int i = 0; i < inputWidth; i++) {
                if (input.hasToken(i)) {
                    Token inToken = input.get(i);

                    if (i < outputWidth) {
                        output.send(i, inToken);
                    }
                }
            }
        }
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

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the parent class throws it.
     *  @return Whatever the superclass returns (probably true).
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        _wasSleepCalledInFireYet = false;
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // True if sleep was called in fire().  Sleep should only
    // be called once in fire().
    private boolean _wasSleepCalledInFireYet = false;

    private long _sleepTime;
}

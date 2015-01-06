/* A triggered clock source.

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
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TriggeredClock

/**
 This actor is an extension of Clock with a <i>start</i> and <i>stop</i>
 input. A token at the <i>start</i> input will start the clock. A token
 at the <i>stop</i> input will stop the clock, if it is still running.
 If both <i>start</i> and <i>stop</i> are received simultaneously, then
 the clock will be stopped.
 <p>
 So that this <i>start</i> and <i>stop</i> ports can
 be used meaningfully in the CT domain, they are declared DISCRETE, and they
 should be connected to an event generator.  Other domains ignore this
 declaration.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class TriggeredClock extends Clock {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TriggeredClock(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // start port.
        start = new TypedIOPort(this, "start");
        start.setInput(true);
        new SingletonParameter(start, "_showName").setToken(BooleanToken.TRUE);

        // type is undeclared.
        // Annotate DISCRETE, for the benefit of CT.
        new Parameter(start, "signalType", new StringToken("DISCRETE"));

        // stop port.
        stop = new TypedIOPort(this, "stop");
        stop.setInput(true);
        new SingletonParameter(stop, "_showName").setToken(BooleanToken.TRUE);

        // type is undeclared.
        // Annotate DISCRETE, for the benefit of CT.
        new Parameter(stop, "signalType", new StringToken("DISCRETE"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A port that, if connected, is used to specify when the clock
     *  starts. This port has undeclared type.
     */
    public TypedIOPort start;

    /** A port that, if connected, is used to specify when the clock
     *  stops. This port has undeclared type.
     */
    public TypedIOPort stop;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to start not being enabled.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _enabled = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Copy values committed in initialize() or in the last postfire()
     *  into the corresponding tentative variables. In effect, this loads
     *  the last known good value for these variables, which is particularly
     *  important if time has gone backwards. This overrides the base class
     *  to check whether the <i>start</i> or <i>stop</i> inputs have values.
     *  @exception IllegalActionException If thrown accessing start or stop
     *   input data.
     */
    @Override
    protected void _updateTentativeValues() throws IllegalActionException {
        // Check the start input, to see whether everything needs to
        // be reset.
        if (start.isOutsideConnected()) {
            if (start.hasToken(0)) {
                if (_debugging) {
                    _debug("Received a start input.");
                }
                start.get(0);
                // Restart everything.
                initialize();
                _enabled = true;
            }
        }
        // Check stop
        if (stop.isOutsideConnected()) {
            if (stop.hasToken(0)) {
                if (_debugging) {
                    _debug("Received a stop input.");
                }
                stop.get(0);
                _enabled = false;
            }
        }
        super._updateTentativeValues();
    }
}

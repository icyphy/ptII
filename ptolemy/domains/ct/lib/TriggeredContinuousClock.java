/* A triggered continuous clock source.

Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// TriggeredContinuousClock

/**
   This actor is an extension of ContinuousClock with a <i>start</i> and
   <i>stop</i> input. It is only used in CT domain. A token at the <i>start</i>
   input will start the clock. A token at the <i>stop</i> input will stop
   the clock, if it is still running. If both <i>start</i> and <i>stop</i>
   are received simultaneously, then the clock will be stopped.
   <p>
   This <i>start</i> and <i>stop</i> ports can are declared DISCRETE, and they
   only accept discrete events as inputs.
   <p>
   @see ptolemy.domains.ct.lib.ContinuousClock

   @author Edward A. Lee, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 2.2
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class TriggeredContinuousClock extends ContinuousClock {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TriggeredContinuousClock(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // start port.
        start = new TypedIOPort(this, "start");
        start.setInput(true);

        // type is undeclared.
        // Annotate DISCRETE, for the benefit of CT.
        new Parameter(start, "signalType", new StringToken("DISCRETE"));

        // stop port.
        stop = new TypedIOPort(this, "stop");
        stop.setInput(true);

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
    ////                         protected methods                 ////

    /** Initialize the cycle count and done flag.  This overrides the
     *  base class to indicate that the clock is not running yet.
     */
    protected void _initializeCycleCount() {
        _done = true;
        _cycleCount = 0;
    }

    /** Call the _updateTentativeValues method of super class.
     *  Override the super class
     *  to check whether the <i>start</i> or <i>stop</i> inputs have values
     *  and update the affected variables.
     *  @exception IllegalActionException If thrown accessing start or stop
     *   input data.
     */
    protected void _updateTentativeValues() throws IllegalActionException {
        super._updateTentativeValues();

        // Check the start input, to see whether everything needs to
        // be reset.
        if (start.getWidth() > 0) {
            if (start.hasToken(0)) {
                if (_debugging) {
                    _debug("Received a start input.");
                }

                start.get(0);

                // Indicate to postfire() that it can call fireAt().
                _tentativeDone = false;

                Time currentTime = getDirector().getModelTime();
                _tentativeCycleStartTime = currentTime;
                _tentativeStartTime = currentTime;
                _tentativePhase = 0;
                _tentativeCycleCount = 1;
            }
        }

        // Check stop
        if (stop.getWidth() > 0) {
            if (stop.hasToken(0)) {
                if (_debugging) {
                    _debug("Received a stop input.");
                }

                stop.get(0);
                _tentativeDone = true;
                _tentativeCycleCount = 0;
                _tentativeCurrentValue = defaultValue.getToken();
            }
        }
    }
}

/* An actor that generates messages according to Poisson process.

 Copyright (c) 1998-2010 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDETAL, OR CONSEQUENTIAL DAMAGES
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
package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.RandomSource;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DEMessageSource

/**
 Generate messages according to Poisson process.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (cxh)
 */
public class DEMessageSource extends RandomSource {
    /** Constructor.
     *  @param container The composite actor that this actor belongs to.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEMessageSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(BaseType.INT);
        request = new TypedIOPort(this, "request", false, true);
        request.setTypeEquals(BaseType.GENERAL);
        next = new TypedIOPort(this, "next", true, false);
        next.setTypeEquals(BaseType.GENERAL);
        maxDelay = new Parameter(this, "maxDelay", new DoubleToken(0.5));

        //        next.delayTo(request);
        //        next.delayTo(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to declare that the <i>output</i>
     *  and <i>request</i> ports do not depend on the <i>next</i>
     *  port in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(next, output, 0.0);
        _declareDelayDependency(next, request, 0.0);
    }
    
    /** Schedule the first fire after a random delay between zero and MaxDelay.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _firstFire = true;
        _msgNum = 0;
        _nextMsgTime = new Time(getDirector(), -1.0);

        if (_random == null
                || ((BooleanToken) resetOnEachRun.getToken()).booleanValue()) {
            _createGenerator();
        }
        //System.out.println("DEChannel " + getFullName() +
        //        " initializing at time " + getCurrentTime());
        DEDirector dir = (DEDirector) getDirector();
        Time now = dir.getModelTime();
        _generateRandomNumber();
        dir.fireAt(this, now.add(((DoubleToken) maxDelay.getToken())
                .doubleValue()
                * _current));
    }

    /** If this is the first fire, output the request
     *  token. Otherwise, if current time agrees with the scheduled
     *  message output time, output the message. If there is a token
     *  in port next, then schedule the next message output time.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_firstFire) {
            request.broadcast(new Token());
            _firstFire = false;
            return;
        }

        DEDirector dir = (DEDirector) getDirector();
        Time now = dir.getModelTime();
        double maxDelayValue = ((DoubleToken) maxDelay.getToken())
                .doubleValue();

        if (next.hasToken(0)) {
            next.get(0);

            if (now.compareTo(_nextMsgTime) < 0) {
                // ignore this
            } else {
                // compute a random delay between zero and MaxDelay.
                // super.fire calls _generateRandomNumber for us,
                // which sets _current.
                double delay = maxDelayValue * _current;
                _nextMsgTime = now.add(delay);
                dir.fireAt(this, _nextMsgTime);
            }

            //System.out.println("DEMessageSource " + this.getFullName() +
            //        " next message " + "scheduled at " + _nextMsgTime);
        }

        if (now.compareTo(_nextMsgTime) == 0) {
            ++_msgNum;
            output.broadcast(new IntToken(_msgNum));
        } else {
            // this refire should be discarded
            return;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** @serial The next port. */
    public TypedIOPort next;

    /** @serial The request port. */
    public TypedIOPort request;

    /** @serial the mean inter-arrival time and value */
    public Parameter maxDelay;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate a new random number.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes may throw it if there are problems getting parameter
     *  values.
     */
    protected void _generateRandomNumber() throws IllegalActionException {
        _current = _random.nextDouble();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial True if this is the first firing. */
    private boolean _firstFire = true;

    /** @serial The message number*/
    private int _msgNum = 0;

    /** @serial The next time to generate a message. */
    private Time _nextMsgTime;

    /** The random number for the current iteration. */
    private double _current;
}

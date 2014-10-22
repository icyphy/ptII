/* Discrete Time (DT) domain receiver.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.dt.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// DTReceiver

/**

 A first-in, first-out (FIFO) queue receiver with variable
 capacity. Tokens are put into the receiver with the put() method, and
 removed from the receiver with the get() method. The token removed is
 the oldest one in the receiver.  Time is incremented by a fixed amount
 <i>delta time</i> every time the get() method is called. Each receiver
 has its own value of delta time.  We calculate delta time as "period /
 (rate * repetitions)" where:

 <UL>
 <LI> period is the execution time of the director per iteration
 <LI> rate   is the rate of the port that holds this receiver
 <LI> repetitions is the firing count per iteration of the actor
 that holds this receiver
 </UL>
 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (chf)
 @Pt.AcceptedRating Yellow (vogel)
 */
public class DTReceiver extends SDFReceiver {
    /** Construct an empty receiver with no container.
     */
    public DTReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     *
     *  @param container The container of the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public DTReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _init();

        Time localTime = new Time(
                ((Actor) container.getContainer()).getDirector());
        initializeLocalTime(localTime);
    }

    /** Construct an empty receiver with the specified container and size.
     *
     *  @param container The container of the receiver.
     *  @param size  The size of the buffer for the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public DTReceiver(IOPort container, int size) throws IllegalActionException {
        super(container, size);
        _init();

        Time localTime = new Time(
                ((Actor) container.getContainer()).getDirector());
        initializeLocalTime(localTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calculate the constant time increment for this receiver. This method
     *  should only be invoked by the DT Director.
     *
     *  @exception IllegalActionException If there is an error in
     *  getting attribute information from the ports.
     */
    public void calculateDeltaTime() throws IllegalActionException {
        // This method should only be called after the preinitialize() stage.
        // Prior to that, certain information about the SDF graph topology is
        // not yet accessible
        int repeats;
        double periodValue;
        boolean isCompositeContainer = !((ComponentEntity) _to).isAtomic();

        if (_from == null) {
            throw new InternalErrorException(
                    "internal DT error: Receiver with null source");
        }

        Parameter param = (Parameter) _fromPort
                .getAttribute("tokenProductionRate");

        if (param == null) {
            _outrate = 1;
        } else {
            _outrate = ((IntToken) param.getToken()).intValue();
        }

        if (isCompositeContainer && _toPort.isOutput()) {
            _inRate = 1;
        } else {
            param = (Parameter) _toPort.getAttribute("tokenConsumptionRate");

            if (param == null) {
                _inRate = 1;
            } else {
                _inRate = ((IntToken) param.getToken()).intValue();
            }
        }

        IOPort containerPort = this.getContainer();
        Actor containerActor = (Actor) _toPort.getContainer();
        DTDirector localDirector;

        if (containerActor instanceof TypedCompositeActor
                && !containerPort.isOutput()) {
            localDirector = (DTDirector) containerActor.getExecutiveDirector();
        } else {
            localDirector = (DTDirector) containerActor.getDirector();
        }

        // FIXME: check tunneling topology
        periodValue = localDirector.getPeriod();

        if (_toPort.isOutput()) {
            repeats = localDirector._getRepetitions(_from);
            _tokenFlowRate = repeats * _outrate;
            _deltaTime = periodValue / _tokenFlowRate;
        } else {
            repeats = localDirector._getRepetitions(_to);
            _tokenFlowRate = repeats * _inRate;
            _deltaTime = periodValue / _tokenFlowRate;
        }
    }

    /** Determine the source and destination ports that use this
     *  receiver in their communications.  In DT, the source and
     *  destination ports are distinct for each receiver because
     *  "non-deterministic merge" type relations are not allowed.
     * @exception IllegalActionException
     */
    public void determineEnds() throws IllegalActionException {
        _toPort = this.getContainer();
        _to = (Actor) _toPort.getContainer();
        _fromPort = null;

        IOPort connectedPort = null;
        List listOfConnectedPorts = null;
        boolean isCompositeContainer = !((ComponentEntity) _to).isAtomic();

        if (isCompositeContainer && _toPort.isOutput()) {
            listOfConnectedPorts = _toPort.insidePortList();
        } else {
            listOfConnectedPorts = _toPort.connectedPortList();
        }

        Iterator portListIterator = listOfConnectedPorts.iterator();

        foundReceiver: while (portListIterator.hasNext()) {
            connectedPort = (IOPort) portListIterator.next();

            if (connectedPort.isOutput() == true) {
                Receiver[][] remoteReceivers = connectedPort
                        .getRemoteReceivers();

                for (int i = 0; i < connectedPort.getWidth(); i++) {
                    for (int j = 0; j < remoteReceivers[i].length; j++) {
                        if (remoteReceivers[i][j] == this) {
                            _from = (Actor) connectedPort.getContainer();
                            _fromPort = connectedPort;
                            break foundReceiver;
                        }
                    }
                }
            } else if (connectedPort.getContainer() instanceof TypedCompositeActor) {
                // FIXME: should use at isAtomic() instead of instanceof?
                _from = (Actor) connectedPort.getContainer();
                _fromPort = connectedPort;

                break foundReceiver;
            } else if (connectedPort.isInput() == true) {
                // This case occurs when the destination port and
                // the queried connected port are both inputs.
                // This case should be ignored.
            }
        }

        if (_fromPort == null) {
            throw new InternalErrorException(
                    "internal DT error: Receiver with null source");
        }
    }

    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.  Increment the local time by deltaTime.
     *
     *  @return The oldest token in the receiver.
     */
    @Override
    public Token get() {
        // -get-
        Actor actor = (Actor) super.getContainer().getContainer();
        Director director = actor.getDirector();

        // FIXME: need to consider different cases for
        // TypedCompositeActor ports.
        if (director instanceof DTDirector) {
            DTDirector dtDirector = (DTDirector) director;
            dtDirector.setActorLocalTime(_localTime, actor);
        }

        // FIXME: timing has bugs for DT inside DT
        _localTime = _localTime.add(_deltaTime);
        return super.get();
    }

    /**  Return the local time associated with this receiver.
     *
     *   @return The local time associated with this receiver.
     *   @deprecated As of Ptolemy II 4.1, replaced by
     *   {@link #getModelTime()}
     */
    @Deprecated
    @Override
    public double getCurrentTime() {
        return getModelTime().getDoubleValue();
    }

    /** Return the time interval between tokens for this receiver.
     *  Delta time is defined as "period / (token flow rate)"; where
     *  period is the director's <i>period</i> parameter and token
     *  flow rate is the number of tokens flowing through this
     *  receiver per iteration
     *
     *  @return The time interval between tokens
     */
    public double getDeltaTime() {
        return _deltaTime;
    }

    /**  Return the local time associated with this receiver.
     *
     *   @return The local time associated with this receiver.
     */
    @Override
    public Time getModelTime() {
        return _localTime;
    }

    /** Return the port that feeds this Receiver
     *  The port returned by this method is precalculated
     *  during the determineEnds() method call.
     *
     *  @return The port that feeds this receiver.
     */
    public TypedIOPort getSourcePort() {
        return (TypedIOPort) _fromPort;
    }

    /** Return the token flow rate for this receiver
     *  In DT, the token flow rate has the value
     *  "rate * repetitions"; where 'rate' is the
     *  port rate and 'repetitions' is the firing
     *  count of the actor that contains this
     *  receiver.
     *
     *  @return The token flow rate of this receiver
     */
    public int getTokenFlowRate() {
        return _tokenFlowRate;
    }

    /** Return true if get() will succeed in returning a token.
     *
     *  @return A boolean indicating whether there is a token in this
     *  receiver.
     */
    @Override
    public boolean hasToken() {
        if (overrideHasToken == true) {
            return false;
        }

        return super.hasToken();
    }

    /** Put a token to the receiver. If the port feeding this
     *  receiver is null, report an internal error.
     *
     *  @param token The token to be put to the receiver, or null to put no token.
     *  @exception InternalErrorException If the source port is null.
     */
    @Override
    public void put(Token token) {
        if (token == null) {
            return;
        }
        if (_fromPort == null) {
            throw new InternalErrorException(
                    "internal DT error: Receiver with null source");
        }

        super.put(token);
    }

    /** Reset this receiver to its initial state, which includes
     *  calling clear() and resetting the current time to 0.0.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void reset() throws IllegalActionException {
        super.reset();
        IOPort containerPort = getContainer();
        if (containerPort != null) {
            Actor containerActor = (Actor) containerPort.getContainer();
            if (containerActor != null) {
                Director director = containerActor.getDirector();
                if (director != null) {
                    initializeLocalTime(new Time(director));
                    return;
                }
            }
        }
        throw new IllegalActionException(containerPort,
                "Receiver has no director!");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package-access methods                   ////

    /**  For debugging purposes. Display pertinent information about
     *   this receiver.
     */
    void _debugViewReceiverInfo() {
        //        String fromString;
        //        String toString;
        //
        //        if (_from == null) {
        //            fromString = "0";
        //        } else {
        //            fromString = ((Nameable) _from).getName();
        //        }
        //
        //        fromString += (" (" + ((TypedIOPort) _fromPort).getType() + ")");
        //
        //        if (_to == null) {
        //            toString = "0";
        //        } else {
        //            toString = ((Nameable) _to).getName();
        //        }
        //
        //        toString += (" (" + ((TypedIOPort) _toPort).getType() + ")");

        //    _debug(fromString + " " + toString + " " + _deltaTime);
    }

    /** Initialize the local time to the specified time.
     *  FIXME: The specified time is assumed to have value 0.0.
     *  Instead, the director should have a startTime parameter.
     *  This method is designed for the newReceiver method of DTDirector only.
     *
     *  @param time The desired local time.
     */
    void initializeLocalTime(Time time) {
        _localTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the DTReceiver.  Set the cached information regarding
     *  source and destination actors to null.  Set the local time to
     *  zero.  Set deltaTime to zero.
     */
    private void _init() {
        _from = null;
        _to = null;
        _tokenFlowRate = 0;
        _deltaTime = 0.0;
        overrideHasToken = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package-access variables                 ////
    // override the value of hasToken() given by SDFReceiver
    // This variable is used in mixed-hierarchical DT
    boolean overrideHasToken;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The amount of time increment for every get() method call
    private double _deltaTime;

    // The actor feeding this receiver
    private Actor _from;

    // The port feeding this receiver
    private IOPort _fromPort;

    // The cached value of the destination token consumption rate
    private int _inRate;

    // The local cached time
    private Time _localTime;

    // The cached value of the source token production rate
    private int _outrate;

    // The dividing factor to the discrete time period
    private int _tokenFlowRate;

    // The actor containing this receiver
    private Actor _to;

    // The port containing this receiver
    private IOPort _toPort;
}

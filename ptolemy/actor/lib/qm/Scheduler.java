/* This actor implements a Scheduler.

@Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.qm;

import java.util.HashMap;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * Scheduler actor that reads execution time information of actors and
 * schedules execution. This basic scheduler only illustrates the concept
 * and schedules on a first come first serve basis.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class Scheduler extends MonitoredQuantityManager {

    /** Construct a Scheduler with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Scheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _tokens = new FIFOQueue();
        _receiversAndTokensToSendTo = new HashMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     */
    public IntermediateReceiver getReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        if (_debugging) {
            _debug("Create new intermediate receiver for " + receiver);
        }
        return intermediateReceiver;
    }

    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the quantity manager.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the quantity manager.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        return getReceiver(receiver);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Scheduler newObject = (Scheduler) super.clone(workspace);
        newObject._nextReceiver = null;
        newObject._nextTimeFree = null;
        newObject._receiversAndTokensToSendTo = new HashMap();
        newObject._tokens = new FIFOQueue();
        return newObject;
    }

    /** Return the value stored in a parameter associated with
     *  the input port.
     *  Used for deviceDelay, deviceDelayBound, networkDelayBound,
     *  platformDelay and sourcePlatformDelay.
     *  FIXME: specialized ports do contain the parameters, don't
     *  have to get the attribute with the string! For now leave it
     *  that way to support older models that do not use PtidesPorts.
     *  @param namedObj The Parameter
     *  @param parameterName The name of the parameter.
     *  @return the value of the deviceDelay parameter if the parameter is not
     *  null. Otherwise return null.
     *  @exception IllegalActionException If the token of the deviceDelay
     *  parameter cannot be evaluated.
     */
    protected static Double _getDoubleParameterValue(NamedObj namedObj, String parameterName)
            throws IllegalActionException {
        Parameter parameter = (Parameter) namedObj
                .getAttribute(parameterName);
        if (parameter != null) {
            return Double.valueOf(((DoubleToken) parameter.getToken())
                    .doubleValue());
        }
        return null;
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _receiversAndTokensToSendTo.clear();
        _tokens.clear();
        _nextTimeFree = null;
    }

    /** Send tokens in the queue to the target receiver.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Fire at " + currentTime);
        }

        if (_nextTimeFree != null && _tokens.size() > 0
                && currentTime.compareTo(_nextTimeFree) == 0) {
            Object[] output = (Object[]) _tokens.get(0);
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];
            _sendToReceiver(receiver, token);

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send to "
                        + receiver.getContainer().getFullName() + ": " + token);
            }
        }
    }

    /** If there are still tokens in the queue and a token has been produced in the fire,
     *  schedule a refiring.
     */
    public boolean postfire() throws IllegalActionException {
        // This method contains two places where refirings can be
        // scheduled. We only want to schedule a refiring once.
        Time currentTime = getDirector().getModelTime();

        if (_debugging) {
            _debug("Postfire at " + currentTime);
        }
        // If a token was actually sent to a delegated receiver
        // by the fire() method, then remove that token from
        // the queue and, if there are still tokens in the queue,
        // request another firing at the time those tokens should
        // be delivered to the delegated receiver.
       if (_nextTimeFree != null && _tokens.size() > 0
                && currentTime.compareTo(_nextTimeFree) == 0) {
            // Discard the token that was sent to the output in fire().
            _tokens.take();
        }

        if (_tokens.size() > 0
                && (_nextTimeFree == null || currentTime
                        .compareTo(_nextTimeFree) >= 0)) {
            _scheduleRefire();
        }

        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("sendToken at " + currentTime);
        }

        // If previously in the current iteration we have
        // sent a token, then we require the token to have the
        // same value. Thus, this Bus can be used only in domains
        // that either call fire() at most once per iteration,
        // or domains that have a fixed-point semantics.
        Token tokenToSend = _receiversAndTokensToSendTo.get(receiver);
        if (tokenToSend != null) {
            if (!tokenToSend.equals(token)) {
                throw new IllegalActionException(this, receiver.getContainer(),
                        "Previously initiated a transmission with value "
                                + tokenToSend
                                + ", but now trying to send value " + token
                                + " in the same iteration.");
            }
        } else {
            Double executionTime = _getDoubleParameterValue(receiver.getContainer().getContainer(), "executionTime");
            _tokens.put(new Object[] { receiver, token, executionTime });
            _tokenCount++;
            sendQMTokenEvent((Actor) source.getContainer().getContainer(),
                    0, _tokenCount, EventType.RECEIVED);
            if (_tokens.size() == 1) { // no refiring has been scheduled
                _scheduleRefire();
            }
        }

        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        if (token == null) {
            return;
        }
        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /**
     * Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        //_tokens.clear();
    }

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    ///////////////////////////////////////////////////////////////////
    //                          protected methods                    //

    /** Schedule a refiring of the actor.
     *  @exception IllegalActionException Thrown if the actor cannot be rescheduled
     */
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextReceiver = (Receiver) ((Object[]) _tokens.get(0))[0];
        _nextTimeFree = currentTime.add((Double) ((Object[]) _tokens.get(0))[2]);
        System.out.println(currentTime +" fire at " + _nextTimeFree);
        _fireAt(_nextTimeFree);

    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /** Next receiver to which the next token to be sent is destined. */
    private Receiver _nextReceiver;

    /** Next time a token is sent and the next token can be processed. */
    private Time _nextTimeFree;

    /** Map of receivers and tokens to which the token provided via sendToken() should
     *  be sent to.
     */
    private HashMap<Receiver, Token> _receiversAndTokensToSendTo;

    /** Tokens stored for processing. */
    private FIFOQueue _tokens;

}

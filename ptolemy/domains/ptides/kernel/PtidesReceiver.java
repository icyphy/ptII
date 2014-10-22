/* PTIDES receiver that extends DEReceiver and works with PtidesBasicDirector and its subclasses.
@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptides.kernel;

import java.util.Comparator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
 * Receivers in the Ptides domain use a timed queue to sort events in
 * the receivers.
 * Tokens are stored in the receiver as a pair of token and tag, to help
 * methods such as hasToken() to indicate whether there is a token of the
 * requested tag. This receiver is needed because events can be processed
 * out of timestamp order, thus tokens are transmitted between actors out
 * of timestamp order. Some of this code is copied from PtidesReceiver written
 * by Patricia Derler, since we want to have a sorted list of events at each
 * receiver. However we still want to extend DEReceiver so that the structure
 * is uniform in that PtidesBasicDirector extends DEDirector, and PtidesBasicReceiver
 * extends DEReceiver.
 *
 * @author Jia Zou, Slobodan Matic
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */
public class PtidesReceiver extends DEReceiver {

    /**
     * Construct an empty queue with no container.
     */
    public PtidesReceiver() {
        super();
    }

    /**
     * Construct an empty queue with the specified IOPort container.
     *
     * @param container
     *            The IOPort that contains this receiver.
     * @exception IllegalActionException
     *                If this receiver cannot be contained by the proposed
     *                container.
     */
    public PtidesReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    /**
     * Wrap the token in an DETokenEvent and post the trigger event to the director.
     * The director will be responsible to dequeue the trigger event at the
     * correct timestamp and microstep and invoke the corresponding actor whose
     * input port contains this receiver. This receiver may contain more than
     * one events.
     *
     * @param token
     *            The token to be put, or null to not put a token.
     */
    @Override
    public void put(Token token) {
        if (token == null) {
            return;
        }
        try {
            getDirector()._enqueueTriggerEvent(getContainer(), token, this);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex, null);
        }
    }

    /** Actually put the token into this receiver. This method should be called
     *  by the director when to token is ready to be processed by the actor
     *  where this receiver resides.
     *  @param token The token to be put to this receiver, or null to put no token.
     */
    public void putToReceiver(Token token) {
        if (token != null) {
            _tokens.add(token);
        }
    }

    /** Remove the token from the list of stored tokens. This is only used
     *  to remove tokens that were stored temporarily in the receiver to
     *  check whether the actor's prefire returns true.
     * @param token The token to be removed.
     */
    public void remove(Token token) {
        _tokens.remove(token);
    }

    /**
     * Return the director that created this receiver. If this receiver is an
     * inside receiver of an output port of an opaque composite actor, then the
     * director will be the local director of the container of its port.
     * Otherwise, it's the executive director of the container of its port.Note
     * that the director returned is guaranteed to be non-null. This method is
     * read synchronized on the workspace.
     *
     * @return An instance of DEDirector.
     * @exception IllegalActionException
     *                If there is no container port, or if the port has no
     *                container actor, or if the actor has no director, or if
     *                the director is not an instance of DEDirector.
     */
    public PtidesDirector getDirector() throws IllegalActionException {
        IOPort port = getContainer();

        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }

            // Cache is invalid. Reconstruct it.
            try {
                port.workspace().getReadAccess();

                Actor actor = (Actor) port.getContainer();

                if (actor != null) {
                    Director dir;

                    if (!port.isInput() && actor instanceof CompositeActor
                            && ((CompositeActor) actor).isOpaque()) {
                        dir = actor.getDirector();
                    } else {
                        dir = actor.getExecutiveDirector();
                    }

                    if (dir != null) {
                        _director = (PtidesDirector) dir;
                        _directorVersion = port.workspace().getVersion();
                        return _director;
                    }
                }
            } finally {
                port.workspace().doneReading();
            }
        }

        throw new IllegalActionException(getContainer(),
                "Does not have a IOPort as the container of the receiver.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director where this DEReceiver should register for ptides events.
    private PtidesDirector _director;

    /**
     * An Event is an aggregation consisting of a Token, a time stamp and
     * destination Receiver. Both the token and destination receiver are allowed
     * to have null values. This is particularly useful in situations where the
     * specification of the destination receiver may be considered redundant.
     */
    public static class Event {

        /**
         * Construct an Event with a token and time stamp.
         *
         * @param token
         *            Token for the event.
         * @param tag
         *            Tag tag of the event.
         */
        public Event(Token token, Tag tag) {
            _token = token;
            _tag = tag;
        }

        // /////////////////////////////////////////////////////////
        // // public inner methods ////

        /**
         * Return the tag of this event.
         *
         * @return The tag of the event.
         */
        public Tag getTag() {
            return _tag;
        }

        /**
         * Return the token of this event.
         *
         * @return The token of the event.
         */
        public Token getToken() {
            return _token;
        }

        // /////////////////////////////////////////////////////////
        // // private inner variables ////
        /** Time stamp of this event. */
        Tag _tag;

        /** Token of this event. */
        Token _token = null;
    }

    /**
     * Compare two events according to - time stamp - value did not find a way
     * to compare Tokens, therefore am comparing DoubleTokens and IntTokens
     * here. If other kinds of Tokens are used, this Comparer needs to be
     * extended.
     *
     * @author Jia Zou, Slobodan Matic, Patricia Derler
     *
     */
    public static class TagComparator implements Comparator {

        /**
         * Compare two events according to time stamps and values.
         *
         * FIXME Because there is no general compare method for tokens, I
         * implemented the comparison for int and double tokens. A more general
         * compare is required.
         *
         * @param arg0
         *            First event.
         * @param arg1
         *            Second event.
         * @return -1 if event arg0 should be processed before event arg1, 0 if
         *         they should be processed at the same time, 1 if arg1 should
         *         be processed before arg0.
         */
        @Override
        public int compare(Object arg0, Object arg1) {
            Event event1 = (Event) arg0;
            Event event2 = (Event) arg1;
            Tag time1 = event1._tag;
            Tag time2 = event2._tag;

            return time1.compareTo(time2);

        }

    }
}

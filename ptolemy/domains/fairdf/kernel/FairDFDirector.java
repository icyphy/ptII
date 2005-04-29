/* Director implementing dataflow with a notion of fairness.

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.domains.fairdf.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// FairDFDirector

/**

This director implements a variant of dataflow that guarantees a
degree of fairness in selecting actors to fire.

Actors are fired in an unspecified order, subject to the following constraints.

<ol>
<li> In one iteration of the model, each actor is fired at most once.
<li>The iteration ends when all remaining actors (i.e. those that did
not fire during the current iteration) cannot fire (i.e. they return
false on <tt>prefire()</tt>).
</ol>
<p>
This implementation allows proper rollback, i.e. it may be repeatedly fired without intervening <tt>postfire()</tt>
and it restores the queues to their original state.

@author J&#246;rn W. Janneck
@version $Id$
@ProposedRating Red (janneck)
@AcceptedRating Red (reviewmoderator)
*/
public class FairDFDirector extends Director {
    public FairDFDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        init();
    }

    public FairDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        init();
    }

    public FairDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        init();
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        // get my contained actors
        CompositeActor container = (CompositeActor) this.getContainer();
        List entities = container.entityList();
        actors = new ArrayList();

        for (Iterator i = entities.iterator(); i.hasNext();) {
            Object e = i.next();

            if (e instanceof Actor) {
                actors.add(e);
            }
        }

        iterationCount = 0;
    }

    /**
     * Always return what <tt>super.prefire()</tt> returns, because an
     * DDF model can potentially always perform an iteration,
     * regardless of the presence of input tokens.
     *
     * @return Always returns what <tt>super.prefire()</tt> returns.
     * @exception IllegalActionException If thrown by <tt>super.prefire()</tt>
     */
    public boolean prefire() throws IllegalActionException {
        reFire = false;
        return super.prefire();
    }

    public void fire() throws IllegalActionException {
        if (reFire) {
            rollbackReceivers();
        }

        reFire = true;

        List unfiredActors = new ArrayList(actors);
        firedActors = new HashSet();

        boolean hasFired = true;

        while (hasFired) {
            hasFired = false;

            for (ListIterator i = unfiredActors.listIterator(); i.hasNext();) {
                Actor a = (Actor) i.next();

                if (a.prefire()) {
                    a.fire();
                    i.remove();
                    firedActors.add(a);
                    hasFired = true;
                }
            }
        }
    }

    public boolean postfire() throws IllegalActionException {
        commitReceivers();

        for (Iterator i = firedActors.iterator(); i.hasNext();) {
            Actor a = (Actor) i.next();
            a.postfire();
        }

        iterationCount += 1;

        int iterationLimit = ((IntToken) (iterations.getToken())).intValue();

        if ((iterationLimit > 0) && (iterationCount >= iterationLimit)) {
            iterationCount = 0;
            return false;
        }

        return super.postfire();
    }

    public Receiver newReceiver() {
        return new FairDFReceiver();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Inform the director that the specified receiver has changed its state
     * (i.e. that a token has been added to or removed from it).
     *
     * @param r The receiver.
     */
    private void notifyReceiverChange(FairDFReceiver r) {
        modifiedReceivers.add(r);
    }

    /**
     * Undo all changes made to all receivers since the last time they
     * were committed.
     */
    private void rollbackReceivers() {
        for (Iterator i = modifiedReceivers.iterator(); i.hasNext();) {
            FairDFReceiver r = (FairDFReceiver) i.next();
            r.rollback();
        }

        modifiedReceivers.clear();
    }

    /**
     * Commit the changes made to all receivers instantiated by this director.
     */
    private void commitReceivers() {
        for (Iterator i = modifiedReceivers.iterator(); i.hasNext();) {
            FairDFReceiver r = (FairDFReceiver) i.next();
            r.commit();
        }

        modifiedReceivers.clear();
    }

    private void init() throws IllegalActionException, NameDuplicationException {
        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     * <p>
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    private Set modifiedReceivers = new HashSet();
    private List actors = null;
    private Collection firedActors = null;
    private boolean reFire = false;
    private int iterationCount;

    ///////////////////////////////////////////////////////////////////
    ////                         nested & inner classes            ////

    /**
     * This receiver implements a queue that realizes a
     * commit/rollback protocol. It notifies the enclosing director if
     * it changes its state. The director can commit the state or roll
     * it back to the last state that has been committed.
     */
    class FairDFReceiver extends AbstractReceiver {
        ///////////////////////////////////////////////////////////////////
        //// implement: AbstractReceiver                               ////
        ///////////////////////////////////////////////////////////////////
        public Token get() throws NoTokenException {
            if (next >= queue.size()) {
                throw new NoTokenException(
                        "Attempt to read from an empty queue.");
            }

            Token v = (Token) queue.get(next++);
            notifyReceiverChange(this);
            return v;
        }

        public boolean hasRoom() {
            return true;
        }

        public boolean hasRoom(int i) {
            return true;
        }

        public boolean hasToken() {
            return next < queue.size();
        }

        public boolean hasToken(int i) {
            return ((next + i) - 1) < queue.size();
        }

        /** Clear this receiver of any contained tokens.
         */
        public void clear() {
            queue.clear();
        }

        public void put(Token token) throws NoRoomException {
            queue.add(token);
            added += 1;
            notifyReceiverChange(this);
        }

        ///////////////////////////////////////////////////////////////////
        ////                      FairDFReceiver                          ////
        ///////////////////////////////////////////////////////////////////
        FairDFReceiver() {
            super();
        }

        FairDFReceiver(IOPort container) throws IllegalActionException {
            super(container);
        }

        void rollback() {
            for (int i = 0; i < added; i++) {
                queue.remove(queue.size() - 1);
            }

            next = 0;
            added = 0;
        }

        void commit() {
            for (int i = 0; i < next; i++) {
                queue.remove(0);
            }

            next = 0;
            added = 0;
        }

        private List queue = new ArrayList();
        private int next = 0;
        private int added = 0;
    }

    /**
     * This is a simplified version of the DDF receiver, which does
     * not support rollback, and which is more efficient as a
     * result. Because it does not need access to the director that
     * instantiated it, it can be <tt>static</tt>.
     * <p>
     * Perhaps we should let the user choose whether rollback is needed?
     * <p>
     * <bf>NOTE:</bf> This class is currently not used, and hence redundant.
     */
    static class SimpleFairDFReceiver extends AbstractReceiver {
        ///////////////////////////////////////////////////////////////////
        //// implement: AbstractReceiver                               ////
        ///////////////////////////////////////////////////////////////////
        public Token get() throws NoTokenException {
            Token v = (Token) queue.get(0);
            queue.remove(0);
            return v;
        }

        public boolean hasRoom() {
            return true;
        }

        public boolean hasRoom(int i) {
            return true;
        }

        public boolean hasToken() {
            return 1 <= queue.size();
        }

        public boolean hasToken(int i) {
            return i <= queue.size();
        }

        /** Clear this receiver of any contained tokens.
         */
        public void clear() {
            queue.clear();
        }

        public void put(Token token) throws NoRoomException {
            queue.add(token);
        }

        ///////////////////////////////////////////////////////////////////
        //// SimpleFairDFReceiver                                         ////
        ///////////////////////////////////////////////////////////////////
        SimpleFairDFReceiver() {
            super();
        }

        SimpleFairDFReceiver(IOPort container) throws IllegalActionException {
            super(container);
        }

        private List queue = new ArrayList();
    }
}

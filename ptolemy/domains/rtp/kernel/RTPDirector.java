/* Director for the real-time processes  model of computation.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.rtp.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.data.LongToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// RTPDirector
/**

FIXME: document this.

FIXME: How to stop a model???

@see ptolemy.domains.rtp.kernel.RTPReceiver

@author  Jie Liu
@version $Id$
*/
public class RTPDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public RTPDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public RTPDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the base class has an
     *   "iterations" parameter (which it should not).
     */
    public RTPDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The amount of time that the model is executed, in terms of
     *  seconds. After that the postfire will return false.
     *  The value must be an integer. Otherwise an exception will be
     *  thrown.
     *  If the value is less than or equal to zero,
     *  then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter executionDuration;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the time count of the computer in the number of milliseconds,
     *  starting from 1/1/1970 (UTC).
     *  @return The current computer time.
     */
    public double getCurrentTime() {
        return (double)(System.currentTimeMillis()-_realStartTime);
    }

    /** Calculate the current schedule, if necessary, and iterate
     *  the contained actors in the order given by the schedule.
     *
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
        // FIXME: resolve deadlock? Do nothing for now...
    }

    /** Sleep for the amount of time specified.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        double timeNow = getCurrentTime();
        if (time > timeNow) {
            long delay = (long) (time - timeNow);
            try {
                if (_debugging) _debug("Sleep " + delay);
                Thread.sleep(delay+ 5);
                if (_debugging) _debug("Wake up at " + getCurrentTime());
            } catch (InterruptedException ex) {
                if (_debugging) _debug("interrupted.");
                // ignore...
            }
        }
    }

    /** Start all actors and set the stop condition.
     *  @exception IllegalActionException If any of the actors throws it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) _debug("Start threads.");
        _realStartTime = System.currentTimeMillis();
        super.initialize();
    }

    /** Return a new receiver consistent with the RTP domain.
     *  @return A new RTPReceiver.
     */
    public Receiver newReceiver() {
        if (_debugging) _debug("creates a new rtp receiver.");
        return new RTPReceiver();
    }

    /** Return false, since the execution must be ended by now.
     *  @return false.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {
        long duration = ((LongToken)executionDuration.getToken()).longValue();
        if (duration>0) {
            try {
                if (_debugging) _debug(getName() + " sleep... " + duration);
                Thread.sleep(duration);
                stopFire();
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore...
            }
        }

        return false;
    }

    public void wrapup() throws IllegalActionException {
        stop();
        synchronized (this) {
            notifyAll();
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the director by creating a scheduler and iterations
     *  parameter.
     */
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        executionDuration = new Parameter
            (this, "executionDuration", new LongToken(10000));
        executionDuration.setTypeEquals(BaseType.LONG);

        //addDebugListener(new StreamListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The real start time
    private long _realStartTime;

    // The anded result of the values returned by actors' postfire().
    // private boolean _postFireReturns = true;

    // List of all receivers this director has created.
    // private LinkedList _receivers = new LinkedList();
}

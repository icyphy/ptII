/* Director for the real-time processes  model of computation.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// RTPDirector
/**

FIXME: document this.

FIXME: How to stop a model???

@see ptolemy.domains.sdf.kernel.RTPReceiver

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
    public RTPDirector(TypedCompositeActor container, String name)
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

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the interations member.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        RTPDirector newobj = (RTPDirector)(super.clone(ws));
        newobj.executionDuration = (Parameter)newobj.getAttribute
            ("executionDuration");
        return newobj;
    }

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
        double tnow = getCurrentTime();
        if (time > tnow) {
            long delay = (long) (time - tnow);
            try {
                System.out.println("Sleep " + delay);
                Thread.sleep(delay+ 5);
                System.out.println("Wake up at " + getCurrentTime());
            } catch (InterruptedException ex) {
                System.out.println("interrupted.");
                // ignore...
            }
        }
    }

    /** Start all actors and set the stop condition.
     *  @throws IllegalActionException If any of the actors throws it.
     */
    public void initialize() throws IllegalActionException {
        System.out.println("Start threads.");
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
     *  @throws IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {
        long duration = ((LongToken)executionDuration.getToken()).longValue();
        if (duration>0) {
            try {
                System.out.println(getName() + " sleep... " + duration);
                Thread.sleep(duration);
                stopFire();
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore...
            }
        }

        return false;
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
        // FIXME: Remove this after debugging, or when GUI supports it.
        addDebugListener(new StreamListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The real start time
    private long _realStartTime;

    // The anded result of the values returned by actors' postfire().
    // private boolean _postfirereturns = true;

    // List of all receivers this director has created.
    // private LinkedList _receivers = new LinkedList();
}

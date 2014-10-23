/* Thread that manages the execution of active actors in the CI domain.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.domains.ci.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.PtolemyThread;

///////////////////////////////////////////////////////////////////
//// ActiveActorManager

/**
 An active actor manager iterates an active actor until its postfire()
 returns false, or the director is requested to stop. If the active
 actor has pull input and its prefire() returns false, the actor manager
 will notify the CI director to process the pull request by the actor.

 When the actor has a <i>period</i> parameter, the actor manager will
 sleep between successive iterations of the actor for the duration
 given by the parameter, in milliseconds. This is used to control the
 execution rate of active actors that are always ready to produce the
 next output or request the next input.

 @author Xiaojun Liu, Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public class ActiveActorManager extends PtolemyThread {
    /** Construct an actor manager to iterate the actor.
     *  @param actor The actor that is managed.
     *  @param director The director of the actor.
     * @exception IllegalActionException
     */
    public ActiveActorManager(Actor actor, CIDirector director)
            throws IllegalActionException {
        super();
        _actor = actor;
        _director = director;

        CompositeActor container = (CompositeActor) ((NamedObj) actor)
                .getContainer();
        _manager = container.getManager();
        _init();
        director._addActorManager(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Iterate the actor until its postfire() returns false, or the
     *  director is requested to stop.
     */
    @Override
    public void run() {
        boolean iterate = true;

        try {
            while (iterate && !_director._isStopRequested()) {
                synchronized (_director) {
                    while (_director._pauseRequested) {
                        _director.wait();
                    }
                }

                // container is checked for null to detect the
                // deletion of the actor from the topology
                if (((Entity) _actor).getContainer() != null) {
                    if (_actor.prefire()) {
                        _actor.fire();
                        iterate = _actor.postfire();

                        if (_period > 0) {
                            sleep(_period);
                        }
                    } else {
                        if (_isPushSource) {
                            // this should be the case when the actor is an
                            // async data source, e.g. a datagram receiver
                            yield();
                        } else {
                            synchronized (_actor) {
                                if (!_actor.prefire()) {
                                    _director._requestAsyncPull(_actor);
                                    _actor.wait();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            _manager.notifyListenersOfException(ex);
        } catch (InterruptedException ex) {
            // ignore
            // either the director interrupts this actor manager to stop
            // the model, or the user interrupts the execution of the model.
        } finally {
            _director._removeActorManager(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // check actor connection, and set the _isPushSource flag and _period
    // value, which are used in the run() method.
    private void _init() throws IllegalActionException {
        boolean hasInput = false;
        boolean outputIsPush = false;
        Iterator inputPorts = _actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            if (port.isOutsideConnected()) {
                hasInput = true;
            }
        }

        Iterator outputPorts = _actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            if (port.isOutsideConnected()) {
                outputIsPush |= CIDirector._isPushPort(port);
            }
        }

        _isPushSource = !hasInput && outputIsPush;

        Parameter p = (Parameter) ((Entity) _actor).getAttribute("period");

        if (p != null) {
            _period = 0;

            try {
                _period = ((IntToken) p.getToken()).intValue();
            } catch (Exception ex) {
                System.err
                        .println("ActiveActorManager, failed to parse the period parameter: "
                                + ex);
            }
        } else {
            _period = (int) _director._interval;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The active actor being managed.
    private Actor _actor;

    // The CI director that executes inactive actors.
    private CIDirector _director;

    // The manager of the Ptolemy model.
    private Manager _manager;

    // Flag that indicates whether the managed actor is a push source.
    private boolean _isPushSource = false;

    // The period of one iteration of the managed actor.
    private int _period = 0;
}

/* Thread that manages the execution of active actors in the CI domain.

 Copyright (c) 2002 The Regents of the University of California.
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

@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.ci.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.IOPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ActiveActorManager
/**
An active actor manager iterates the active actor through the execution
cycle until stop is requested. If the active actor pulls data from its
input ports and its prefire() returns false, the actor manager will notify
the CI director to process the pull request by the actor.

@author Xiaojun Liu, Yang Zhao
@version $Id$
@since Ptolemy II 2.1
*/
public class ActiveActorManager extends PtolemyThread {

    /** Construct a thread to be used for the execution of the
     *  iteration methods of the actor. This increases the count of active
     *  actors in the director.
     *  @param actor The actor that is managed.
     *  @param director The director responsible for the execution of this
     *   actor.
     */
    public ActiveActorManager(Actor actor, CIDirector director) {
        super();
        _actor = actor;
        _director = director;
        CompositeActor container =
            (CompositeActor)((NamedObj)actor).getContainer();
        _manager = container.getManager();
        _init();
        director._addActorManager(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Iterate the actor through the execution cycle
     *  until a stop has been requested. At the end of the termination,
     *  calls wrapup on the actor.
     */
    public void run() {
        boolean iterate = true;
        try {
            while (iterate && !_director._stopRequested) {
                // container is checked for null to detect the
                // deletion of the actor from the topology
                if (((Entity)_actor).getContainer() != null) {
                    if (_actor.prefire()) {
                        _actor.fire();
                        iterate = _actor.postfire();
                        if (_period > 0) {
                            try {
                                sleep(_period);
                            } catch (InterruptedException ex) {
                                //FIXME: better way to handle?
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        if (_isPushSource) {
                            // this should be the case when the actor is an
                            // async data source, e.g. a datagram receiver
                            yield();
                        } else {
                            synchronized (_actor) {
                                try {
                                    if (!_actor.prefire()) {
                                        _director._requestAsyncPull(_actor);
                                        _actor.wait();
                                    }
                                } catch (InterruptedException ex) {
                                    //FIXME: better way to handle?
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalActionException e) {
            _manager.notifyListenersOfException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // check actor connection, and set the _isPushSource flag and _period
    // value, which are used in the run() method.
    private void _init() {
        boolean hasInput = false;
        boolean outputIsPush = false;
        Iterator inputPorts = _actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)inputPorts.next();
            if (port.getWidth() > 0) {
                hasInput = true;
            }
        }
        Iterator outputPorts = _actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort)outputPorts.next();
            if (port.getWidth() > 0) {
                outputIsPush |= CIDirector._isPushPort(port);
            }
        }
        _isPushSource = !hasInput && outputIsPush;
        Parameter p = (Parameter)((Entity)_actor).getAttribute("period");
        if (p != null) {
            _period = 0;
            try {
                _period = ((IntToken)p.getToken()).intValue();
            } catch (Exception ex) {
                // ignore, so period will have default value 0
            }
        } else {
            _period = (int)_director._interval;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The active actor being managed.
    private Actor _actor;

    // The CI director that executes non-active actors.
    private CIDirector _director;

    // The manager of the Ptolemy model.
    private Manager _manager;

    // Flag that indicates whether the managed actor is a push source.
    private boolean _isPushSource = false;

    // The period of one iteration of the managed actor.
    private int _period = 0;

}


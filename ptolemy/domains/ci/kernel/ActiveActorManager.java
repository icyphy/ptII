/* Thread that manages the execution of active actors in the CI domain.

 Copyright (c) 2002-2003 The Regents of the University of California.
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
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ActiveActorManager
/**
Construct a thread to iterate the active actor through the execution cycle
until a stop has been requested. It increases the count of active
actors in the director. At the end of the termination,
calls wrapup on the actor.

@author Xiaojun Liu, Yang Zhao
@version $Id$
*/
public class ActiveActorManager extends PtolemyThread {

    /** Construct a thread to be used for the execution of the
     *  iteration methods of the actor. This increases the count of active
     *  actors in the director.
     *  @param actor The actor that needs to be executed.
     *  @param director The director responsible for the execution of this
     *  actor.
     */
    public ActiveActorManager(Actor actor, CIDirector director) {
        super();
        _actor = actor;
        _director = director;
        _manager = ((CompositeActor)
                ((NamedObj)actor).getContainer()).getManager();
        _name = ((Nameable)_actor).getName();
        _setFlags();
        director._activeCount++;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Request that execution of the actor controlled by this
     *  thread continue.
     */
    public void cancelStopThread() {
        _threadStopRequested = false;
    }

    /** Return the actor being executed by this thread
     *  @return The actor being executed by this thread.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Iterate the actor through the execution cycle
     *  until a stop has been requested. At the end of the termination,
     *  calls wrapup on the actor.
     */
    public void run() {

        System.out.println("Manager of " + ((NamedObj)_actor).getName()
                + " running...");

        boolean iterate = true;
        System.out.println("Manager of " + ((NamedObj)_actor).getName()
                + " point 1");
        try {
            System.out.println("Manager of " + ((NamedObj)_actor).getName()
                    + " point 2");
            System.out.println("  my state " + iterate + " "
                    + _director._stopRequested);
            while (iterate && !_director._stopRequested) {
                System.out.println("Manager of " + ((NamedObj)_actor).getName()
                        + " point 3");
                /*// If a stop has been requested, then
                if(_threadStopRequested) {
                    // Tell the director we're stopped
                    _director._actorHasStopped();
                    // And wait until the flag has been cleared.
                    synchronized(_director) {
                        while(_threadStopRequested) {
                            workspace.wait(_director);
                        }
                    }
                }*/

                System.out.println("Manager of "
                        + ((NamedObj)_actor).getName() + " iterating...");

                // container is checked for null to detect the
                // deletion of the actor from the topology.
                if (((Entity)_actor).getContainer() != null) {
                    if (_actor.prefire()) {

                        System.out.println("Manager firing actor "
                                + ((NamedObj)_actor).getName());

                        _actor.fire();
                        iterate = _actor.postfire();
                        if (_period > 0) {
                            try {
                                sleep(_period);
                            } catch (InterruptedException ex) {}
                        }
                    } else {
                        if (_isPushSource) {
                            // this should be the case when the actor is an
                            // async data source, e.g. datagram receiver
                            yield();
                        } else {
                            /*_director._requestAsyncPull(_actor);
                            if (_receiver == null) {
                                IOPort input = (IOPort)_actor.inputPortList().
                                        iterator().next();
                                _receiver = (input.getReceivers())[0][0];
                            }*/

                            System.out.println("Wait for input to "
                                    + "pulled actor "
                                    + ((Nameable)_actor).getName());

                            /*synchronized(_receiver) {
                                try {
                                    _director._requestAsyncPull(_actor);
                                    _receiver.wait();
                                } catch (InterruptedException ex) {}
                            }*/

                            synchronized(_actor) {
                                try {
                                    if (!_actor.prefire()) {
                                        _director._requestAsyncPull(_actor);
                                        _actor.wait();
                                    }
                                } catch (InterruptedException ex) {}
                            }

                            System.out.println("Wake up from waiting - "
                                    + ((Nameable)_actor).getName());

                        }
                    }
                }
            }
        } catch (IllegalActionException e) {

            System.out.println("Manager of " + ((NamedObj)_actor).getName()
                    + " caught exception : " + e.getMessage());

            _manager.notifyListenersOfException(e);
        }
        _director._activeCount--;
        synchronized (_director) {
            _director.notifyAll();
        }
        System.out.println("Manager stopped - " +
                ((Nameable)_actor).getName());
    }

    /** Request that execution of the actor controlled by this
     *  thread stop.
     */
    public void stopThread() {
        _threadStopRequested = true;
    }

    /** End the execution of the actor under the control of this
     *  thread. Subclasses are encouraged to override this method
     *  as necessary for domain specific functionality.
     * @exception IllegalActionException If an error occurs while
     *  ending execution of the actor under the control of this
     *  thread.
     */
    public void wrapup() throws IllegalActionException {
        _actor.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                ////

    // check actor connection, and set the _isPushSource flag and
    // _period value, which are used in the run() method.

    private void _setFlags() {
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
                if (port.getAttribute("push") != null) outputIsPush = true;
            }
        }
        _isPushSource = !hasInput && outputIsPush;
        Parameter p = (Parameter)((Entity)_actor).getAttribute("period");
        if (p != null) {
            _period = 0;
            try {
                _period = ((IntToken)p.getToken()).intValue();
            } catch (IllegalActionException ex) {
                //ignore for now
            }
        } else {
            _period = (int)_director._interval;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the active actor executed.
    private Actor _actor;

    //the CI director.
    private CIDirector _director;

    //
    private Manager _manager;

    //name of the active actor executed.
    private String _name;

    //flag that indicates that this thread has been requested to stop.
    private boolean _threadStopRequested = false;

    //
    private boolean _preparingToWrapup = false;

    //???
    private Receiver _receiver;

    //flag indicates that whether an actor is a push source.
    private boolean _isPushSource = false;

    //how often to interate the actor.
    private int _period = 0;

}

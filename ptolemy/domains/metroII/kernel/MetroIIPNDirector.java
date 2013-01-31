/* Director for simplified MetroII semantic.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.data.IntToken;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIIPNDirector

/**
 * <p> MetroIIPNDirector extends PNDirector and implements the 
 * MetroIIEventHandler interface. In addition to being blocked on 
 * read or write, the actors governed by MetroIIPNDirector may 
 * be blocked by MetroII events: 'Get.End' or 'Put.Begin'. 'Get.End' 
 * is PROPOSED after a token is successfully obtained from the 
 * receiver. And the actor is blocked until 'Get.End' is NOTIFIED.
 * 'Put.Begin' is PROPOSED before trying to put a token into the 
 * receiver. Similarly, the actor is blocked until 'Put.Begin' is 
 * NOTIFIED. </p>
 * 
 * 
 * 
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */

public class MetroIIPNDirector extends PNDirector implements
        MetroIIEventHandler {

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  Initialize an eventLock vector.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  Thrown in derived classes.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MetroIIPNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        eventLock = Collections.synchronizedList(new ArrayList<Object>());
    }

    /** Clone the director into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIIPNDirector newObject = (MetroIIPNDirector) super
                .clone(workspace);
        newObject.eventLock = Collections
                .synchronizedList(new ArrayList<Object>());
        newObject.eventNameID = new Hashtable<String, Integer>();
        newObject.proposedMetroIIEventList = Collections
                .synchronizedList(new ArrayList<Event.Builder>());
        newObject._metroIIEventBlockedThreads = Collections
                .synchronizedSet(new HashSet());
        return newObject;
    }

    /**
     * The same as super class except replacing the PNQueueReceiver 
     * by MetroIIPNQueueReceiver. 
     */
    public Receiver newReceiver() {
        MetroIIPNQueueReceiver receiver = new MetroIIPNQueueReceiver();
        _receivers.add(new WeakReference(receiver));

        // Set the capacity to the default. Note that it will also
        // be set in preinitialize().
        try {
            int capacity = ((IntToken) initialQueueCapacity.getToken())
                    .intValue();
            receiver.setCapacity(capacity);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return receiver;
    }

    /**
     * Add a proposed MetroII event into the director's event list. 
     * @param e
     */
    public synchronized void addProposedMetroIIEvent(Event.Builder e) {
        proposedMetroIIEventList.add(e);
    }

    /**
     * Implement YieldAdapter interface. 
     */
    @Override
    public YieldAdapterIterable<Iterable<Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    @Override
    public void getfire(ResultHandler<Iterable<Builder>> resultHandler)
            throws CollectionAbortedException {
        // Don't call "Director.super.fire();" here, do the work instead.
        if (_debugging) {
            _debug("Called fire().");
        }

        Workspace workspace = workspace();

        // In case we have an enclosing process director,
        // we identify it so that we can notify it when we are blocked.
        CompositeActor container = (CompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        if (!(outsideDirector instanceof ProcessDirector)) {
            outsideDirector = null;
        }

        int depth = 0;
        try {
            synchronized (this) {
                while (!_areThreadsDeadlocked() && !_areAllThreadsStopped()
                        && !_stopRequested) {
                    // Added to get thread to stop reliably on pushing stop button.
                    // EAL 8/05
                    if (_stopRequested) {
                        return;
                    }

                    if (_debugging) {
                        _debug("Waiting for actors to stop.");
                    }

                    try {
                        if (outsideDirector != null) {
                            ((ProcessDirector) outsideDirector).threadBlocked(
                                    Thread.currentThread(), null);
                        }
                        // NOTE: We cannot use workspace.wait(Object) here without
                        // introducing a race condition, because we have to release
                        // the lock on the _director before calling workspace.wait(_director).
                        if (depth == 0) {
                            depth = workspace.releaseReadPermission();
                        }

                        System.out.println(_getActiveThreadsCount());
                        System.out
                                .println(_getMetroIIEventBlockedThreadsCount());
                        System.out.println(_getStoppedThreadsCount());
                        System.out.println(_getBlockedThreadsCount());

                        while (!_areThreadsDeadlocked()
                                && !_areAllThreadsStopped()
                                && _getActiveThreadsCount() != _getMetroIIEventBlockedThreadsCount()
                                        + _getBlockedThreadsCount()) {
                            wait(1);
                        }

                        System.out.println("events: " + proposedMetroIIEventList.size());
                        ArrayList<Event.Builder> tmp_events = new ArrayList<Event.Builder>(
                                proposedMetroIIEventList);
                        System.out.println("tmp_events: " + tmp_events.size());
                        proposedMetroIIEventList.clear();
                        resultHandler.handleResult(tmp_events);
                        for (Builder etb : tmp_events) {
                            if (etb.getStatus() == Event.Status.NOTIFIED) {
                                String event_name = etb.getName();
                                Object lock = eventLock
                                        .get(eventName2Id(event_name));
                                synchronized (lock) {
                                    lock.notifyAll();
                                    System.out.println("notify: " + event_name);
                                }

                            } else {
                                proposedMetroIIEventList.add(etb);
                            }
                        }

                    } catch (InterruptedException e) {
                        // stop all threads
                        stop();
                        return;
                    } finally {
                        if (outsideDirector != null) {
                            ((ProcessDirector) outsideDirector)
                                    .threadUnblocked(Thread.currentThread(),
                                            null);
                        }
                    }
                }

                if (_debugging) {
                    _debug("Actors have stopped.");
                }

                // Don't resolve deadlock if we are just pausing
                // or if a stop has been requested.
                // NOTE: Added !_stopRequested.  EAL 3/12/03.
                if (_areThreadsDeadlocked() && !_stopRequested) {
                    if (_debugging) {
                        _debug("Deadlock detected.");
                    }

                    try {
                        _notDone = _resolveDeadlock();
                    } catch (IllegalActionException e) {
                        // stop all threads.
                        stop();
                        try {
                            throw e;
                        } catch (IllegalActionException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            if (depth > 0) {
                workspace.reacquireReadPermission(depth);
            }
        }
    }

    public synchronized int eventName2Id(String event_name) {
        if (!eventNameID.containsKey(event_name)) {
            eventNameID.put(event_name, eventLock.size());
            eventLock.add(new Object());
        }
        return eventNameID.get(event_name);
    }

    public void proposeMetroIIEvent(String suffix) throws InterruptedException {
        // Actor actor = (Actor) getContainer().getContainer();
        Thread current_thread = Thread.currentThread();
        String event_name = current_thread.getName() + suffix;
        addProposedMetroIIEvent(makeEventBuilder(event_name, Event.Type.BEGIN));
        
        // System.out.println("propose: " + event_name);
        
        Object lock = eventLock.get(eventName2Id(event_name));
        synchronized (lock) {
            _metroIIEventBlockedThreads.add(current_thread);
            lock.wait();
        }
        _metroIIEventBlockedThreads.remove(current_thread);
    }


    ///////////////////////////////////////////////////////////////////
    ////                  protected methods                        ////

    /** Return the number of threads that are currently blocked on 
     *  a MetroII event. 
     *  @return Return the number of threads that are currently blocked 
     *  on a MetroII event.
     */
    protected final synchronized int _getMetroIIEventBlockedThreadsCount() {
        return _metroIIEventBlockedThreads.size();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  protected fields                         ////

    protected Set _metroIIEventBlockedThreads = Collections
            .synchronizedSet(new HashSet());

    ///////////////////////////////////////////////////////////////////
    ////                  private methods                          ////
    
    private List eventLock;

    private Event.Builder makeEventBuilder(String name, Event.Type t) {
        Event.Builder meb = Event.newBuilder();
        meb.setName(name);
        meb.setOwner(name);
        meb.setStatus(Event.Status.PROPOSED);
        meb.setType(t);
        return meb;
    }


    ///////////////////////////////////////////////////////////////////
    ////                   private fields                          ////

    private Hashtable<String, Integer> eventNameID = new Hashtable<String, Integer>();

    private List proposedMetroIIEventList = Collections
            .synchronizedList(new ArrayList<Event.Builder>());

}

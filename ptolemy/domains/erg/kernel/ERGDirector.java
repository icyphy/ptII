/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.domains.erg.kernel;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.domains.erg.lib.SynchronizeToRealtime;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ERGDirector extends Director {

    /**
     * @throws NameDuplicationException
     * @throws IllegalActionException
     *
     */
    public ERGDirector() throws IllegalActionException, NameDuplicationException {
        _init();
    }

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ERGDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     */
    public ERGDirector(Workspace workspace) {
        super(workspace);
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>controllerName</i> attribute, then make note that this
     *  has changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *  attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == controllerName) {
            _controllerVersion = -1;
        }
    }

    public Time cancel(Event event) {
        Iterator<TimedEvent> iterator = _queue.iterator();
        while (iterator.hasNext()) {
            TimedEvent timedEvent = iterator.next();
            if (timedEvent.contents == event) {
                iterator.remove();
                return timedEvent.timeStamp;
            }
        }
        return null;
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new director.
     *  @param workspace The workspace for the new director.
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ERGDirector newObject = (ERGDirector) super.clone(workspace);
        newObject._controllerVersion = -1;
        return newObject;
    }

    public void fire() throws IllegalActionException {
        List<?> synchronizeAttributes =
            getController().attributeList(SynchronizeToRealtime.class);
        boolean synchronize = false;
        if (synchronizeAttributes.size() > 0) {
            SynchronizeToRealtime attribute =
                (SynchronizeToRealtime) synchronizeAttributes.get(0);
            synchronize = ((BooleanToken) attribute.getToken()).booleanValue();
        }

        Time nextEventTime;
        Time modelTime = getModelTime();
        while (!_queue.isEmpty()) {
            TimedEvent timedEvent = (TimedEvent) _queue.peek();
            nextEventTime = timedEvent.timeStamp;
            if (modelTime.equals(nextEventTime)) {
                _queue.poll();
                if (synchronize) {
                    long elapsedTime = System.currentTimeMillis()
                            - _realStartTime;
                    double elapsedTimeInSeconds = elapsedTime / 1000.0;
                    long timeToWait = (long) (nextEventTime.subtract(
                            elapsedTimeInSeconds).getDoubleValue() * 1000.0);
                    if (timeToWait > 0) {
                        try {
                            _workspace.wait(_queue, timeToWait);
                            if (_stopRequested) {
                                return;
                            }
                        } catch (InterruptedException ex) {
                        }
                    }
                    synchronize = false;
                }

                Event event = (Event) timedEvent.contents;
                event.fire(timedEvent.arguments);

                if (((BooleanToken) event.isFinalState.getToken())
                        .booleanValue()) {
                    _queue.clear();
                }
            } else {
                break;
            }
        }
    }

    public void fireAt(Event event, Time time) throws IllegalActionException {
        fireAt(event, time, null);
    }

    public void fireAt(Event event, Time time, ArrayToken arguments)
    throws IllegalActionException {
        if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this,
                    "Attempt to schedule an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }
        _queue.add(new TimedEvent(time, event, arguments));
    }

    public ERGController getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }

        try {
            workspace().getReadAccess();

            String name = controllerName.getExpression();

            if (name == null) {
                throw new IllegalActionException(this, "No name for mode "
                        + "controller is set.");
            }

            Nameable container = getContainer();

            if (!(container instanceof CompositeActor)) {
                throw new IllegalActionException(this, "No controller found.");
            }

            CompositeActor cont = (CompositeActor) container;
            Entity entity = cont.getEntity(name);

            if (entity == null) {
                throw new IllegalActionException(this, "No controller found "
                        + "with name " + name);
            }

            if (!(entity instanceof FSMActor)) {
                throw new IllegalActionException(this, entity,
                        "mode controller must be an instance of FSMActor.");
            }

            _controller = (ERGController) entity;
            _controllerVersion = workspace().getVersion();
            return _controller;
        } finally {
            workspace().doneReading();
        }
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        _queue.clear();

        ERGController controller = getController();
        Iterator<?> entities = controller.deepEntityList().iterator();
        while (entities.hasNext()) {
            Event event = (Event) entities.next();
            boolean isInitial =
                ((BooleanToken) event.isInitialState.getToken()).booleanValue();
            if (isInitial) {
                _queue.add(new TimedEvent(_startTime, event, null));
            }
        }

        _realStartTime = System.currentTimeMillis();
    }

    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        if (result && !_queue.isEmpty()) {
            if (_isTopLevel()) {
                TimedEvent event = (TimedEvent) _queue.peek();
                setModelTime(event.timeStamp);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();

        if (_isTopLevel()) {
            return result;
        }

        Time modelTime = getModelTime();
        Time nextEventTime = Time.POSITIVE_INFINITY;

        if (!_queue.isEmpty()) {
            TimedEvent event = (TimedEvent) _queue.peek();
            nextEventTime = event.timeStamp;
        }

        while (modelTime.compareTo(nextEventTime) > 0) {
            _queue.poll();

            if (!_queue.isEmpty()) {
                TimedEvent event = (TimedEvent) _queue.peek();
                nextEventTime = event.timeStamp;
            } else {
                nextEventTime = Time.POSITIVE_INFINITY;
            }
        }

        if (!nextEventTime.equals(modelTime)) {
            CompositeActor container = (CompositeActor) getContainer();
            Iterator<?> inputPorts = container.inputPortList().iterator();
            boolean hasInput = false;

            while (inputPorts.hasNext() && !hasInput) {
                IOPort port = (IOPort) inputPorts.next();

                for (int i = 0; i < port.getWidth(); i++) {
                    if (port.hasToken(i)) {
                        hasInput = true;
                        break;
                    }
                }
            }

            if (!hasInput) {
                result = false;
            }
        }

        if (_debugging) {
            _debug("Prefire returns: " + result);
        }
        return result;
    }

    public void stop() {
        if (_queue != null) {
            synchronized (_queue) {
                _stopRequested = true;
                _queue.notifyAll();
            }
        }

        super.stop();
    }

    /** Attribute specifying the name of the mode controller in the
     *  container of this director. This director must have a mode
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public StringAttribute controllerName = null;

    public static class TimedEvent extends ptolemy.actor.util.TimedEvent {

        public TimedEvent(Time time, Event event, ArrayToken arguments) {
            super(time, event);
            this.arguments = arguments;
        }

        /** Display timeStamp and contents. */
        public String toString() {
            if (arguments == null) {
                return super.toString();
            }
            return "timeStamp: " + timeStamp + ", contents: " + contents + "(" +
                    arguments + ")";
        }

        protected ArrayToken arguments;
    }

    /** Create the controllerName attribute.
     * @throws NameDuplicationException
     * @throws IllegalActionException */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        controllerName = new StringAttribute(this, "controllerName");
        _startTime = new Time(this, 0.0);
    }

    // Cached reference to mode controller.
    private ERGController _controller = null;

    // Version of cached reference to mode controller.
    private long _controllerVersion = -1;

    @SuppressWarnings("unchecked")
    private PriorityQueue<TimedEvent> _queue =
        new PriorityQueue<TimedEvent>(10, new TimedEvent.TimeComparator());

    private long _realStartTime;

    private Time _startTime;
}

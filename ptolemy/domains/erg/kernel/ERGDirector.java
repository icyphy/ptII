/*

@Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.domains.erg.kernel;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see DEDirector
 */
public class ERGDirector extends Director implements TimedDirector {

    /**
     * @throws NameDuplicationException
     * @throws IllegalActionException
     *
     */
    public ERGDirector() throws IllegalActionException,
    NameDuplicationException {
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

    public Time cancel(Event event) throws IllegalActionException {
        Iterator<TimedEvent> iterator = _eventQueue.iterator();
        while (iterator.hasNext()) {
            TimedEvent timedEvent = iterator.next();
            boolean found = timedEvent.contents == event;
            if (!found) {
                Actor[] refinements = event.getRefinement();
                if (refinements != null) {
                    for (Actor refinement : refinements) {
                        if (timedEvent.contents == refinement) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (found) {
                iterator.remove();
                _inputQueue.remove(timedEvent);
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
        if (!_isInController()) {
            ERGModalModel modalModel = (ERGModalModel) getContainer();
            List<?> ergControllers = modalModel.entityList(ERGController.class);
            for (Object controllerObject : ergControllers) {
                ((ERGController) controllerObject).readInputs();
            }
        }

        ERGController controller = getController();
        boolean hasInput = controller.hasInput();
        boolean synchronize = controller.synchronizeToRealtime();
        Time modelTime = getModelTime();
        boolean fired = false;

        if (hasInput && !_inputQueue.isEmpty()) {
            Iterator<TimedEvent> iterator = _inputQueue.iterator();
            while (!fired && iterator.hasNext()) {
                TimedEvent timedEvent = iterator.next();
                if (timedEvent.contents instanceof Event) {
                    iterator.remove();
                    _eventQueue.remove(timedEvent);
                    _fire(timedEvent);
                    fired = true;
                }
            }
        }

        if (hasInput && !fired && !_inputQueue.isEmpty()) {
            TimedEvent timedEvent = _inputQueue.poll();
            _eventQueue.remove(timedEvent);
            _fire(timedEvent);
            fired = true;
        }

        if (!fired && !_eventQueue.isEmpty()) {
            TimedEvent timedEvent = _eventQueue.peek();
            Time nextEventTime = timedEvent.timeStamp;
            if (nextEventTime.compareTo(modelTime) <= 0) {
                _eventQueue.poll();
                Object contents = timedEvent.contents;
                if (contents instanceof Actor) {
                    _inputQueue.remove(timedEvent);
                } else if (timedEvent.contents instanceof Event &&
                        ((Event) contents).fireOnInput()) {
                    _inputQueue.remove(timedEvent);
                }
                if (synchronize) {
                    if (!_synchronizeToRealtime(nextEventTime)) {
                        return;
                    }
                    synchronize = false;
                }

                _fire(timedEvent);
                fired = true;
            }
        }
    }

    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        _fireAt(actor, time, null);
    }

    public void fireAt(Event event, Time time, ArrayToken arguments)
    throws IllegalActionException {
        _fireAt(event, time, arguments);
    }

    public ERGController getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }

        try {
            workspace().getReadAccess();

            Nameable container = getContainer();
            if (_isInController()) {
                _controller = (ERGController) container;
            } else {
                String name = controllerName.getExpression();

                if (name == null) {
                    throw new IllegalActionException(this,
                            "No name for mode controller is set.");
                }

                if (!(container instanceof CompositeActor)) {
                    throw new IllegalActionException(this,
                            "No controller found.");
                }

                CompositeActor cont = (CompositeActor) container;
                Entity entity = cont.getEntity(name);

                if (entity == null) {
                    throw new IllegalActionException(this,
                            "No controller found with name " + name);
                }

                if (!(entity instanceof FSMActor)) {
                    throw new IllegalActionException(this, entity,
                            "mode controller must be an instance of FSMActor.");
                }

                _controller = (ERGController) entity;
            }
            _controllerVersion = workspace().getVersion();
            return _controller;
        } finally {
            workspace().doneReading();
        }
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        _initializeSchedule();
    }

    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        if (result) {
            if (!_eventQueue.isEmpty()) {
                if (_isTopLevel()) {
                    TimedEvent event = (TimedEvent) _eventQueue.peek();
                    setModelTime(event.timeStamp);
                }
            } else {
                result = false;
            }
        }
        if (_isEmbedded()) {
            _requestFiring();
        }
        return result;
    }

    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();

        if (_hasInput() && !_inputQueue.isEmpty()) {
            return result;
        }

        if (!_eventQueue.isEmpty()) {
            Time modelTime = getModelTime();
            Time nextEventTime = ((TimedEvent) _eventQueue.peek()).timeStamp;
            while (modelTime.compareTo(nextEventTime) > 0) {
                _eventQueue.poll();

                if (!_eventQueue.isEmpty()) {
                    TimedEvent event = (TimedEvent) _eventQueue.peek();
                    nextEventTime = event.timeStamp;
                } else {
                    nextEventTime = Time.POSITIVE_INFINITY;
                }
            }

            if (!nextEventTime.equals(modelTime)) {
                result = false;
            }

        }

        if (_debugging) {
            _debug("Prefire returns: " + result);
        }

        return result;
    }

    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (!_isInController()) {
            getController().director.preinitialize();
        }
        _realStartTime = System.currentTimeMillis();
    }

    public void stop() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _stopRequested = true;
            }
        }

        super.stop();
    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _eventQueue.clear();
    }

    /** Attribute specifying the name of the mode controller in the
     *  container of this director. This director must have a mode
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public StringAttribute controllerName = null;

    protected void _initializeSchedule() throws IllegalActionException {
        _eventQueue.clear();
        _inputQueue.clear();

        ERGController controller = getController();
        if (_isInController()) {
            ERGModalModel modalModel =
                (ERGModalModel) getContainer().getContainer();
            _currentTime = modalModel.getDirector().getModelTime();

            Iterator<?> entities = controller.deepEntityList().iterator();
            while (entities.hasNext()) {
                Event event = (Event) entities.next();
                boolean isInitial = ((BooleanToken) event.isInitialState
                        .getToken()).booleanValue();
                if (isInitial) {
                    _eventQueue.add(new TimedEvent(_currentTime, event, null));
                }
            }
        } else {
            _eventQueue.add(new TimedEvent(_currentTime, controller, null));
            if (_isEmbedded()) {
                _requestFiring();
            }
        }
    }

    protected boolean _isTopLevel() {
        return super._isTopLevel() && !_isInController();
    }

    private void _fire(TimedEvent timedEvent) throws IllegalActionException {
        ERGController controller = getController();
        Object contents = timedEvent.contents;
        if (contents instanceof Actor) {
            Actor actor = (Actor) contents;
            boolean prefire = actor.prefire();
            if (prefire) {
                actor.fire();
                actor.postfire();
            }
        } else if (contents instanceof Event) {
            Event event = (Event) timedEvent.contents;
            controller._setCurrentEvent(event);
            event.fire(timedEvent._arguments);

            Actor[] refinements = event.getRefinement();
            if (refinements != null) {
                for (Actor refinement : refinements) {
                    if (_stopRequested) {
                        break;
                    }
                    if (refinement instanceof ERGController) {
                        ((ERGController) refinement).director
                                ._initializeSchedule();
                        _fireAt(refinement, _currentTime, null);
                    } else {
                        if (refinement.prefire()) {
                            refinement.fire();
                            refinement.postfire();
                        }
                    }
                }
            }

            if (((BooleanToken) event.isFinalState.getToken()).booleanValue()) {
                _eventQueue.clear();
            }
        } else {
            throw new InternalErrorException(this, null, "The contents of a "
                    + "TimedEvent can only be Actor or Event.");
        }
    }

    private void _fireAt(Object object, Time time, ArrayToken arguments)
    throws IllegalActionException {
        if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this,
                    "Attempt to schedule an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        TimedEvent timedEvent = new TimedEvent(time, object, arguments);
        _eventQueue.add(timedEvent);
        if (object instanceof Actor) {
            _inputQueue.add(timedEvent);
        } else if (object instanceof Event) {
            Event event = (Event) object;
            if (event.fireOnInput()) {
                _inputQueue.add(timedEvent);
            }
        }
    }

    private boolean _hasInput() throws IllegalActionException {
        if (_isInController()) {
            return ((ERGController) getContainer()).hasInput();
        } else {
            NamedObj container = getContainer();
            List<?> inputPorts;
            if (container instanceof ERGController) {
                inputPorts = ((ERGController) container).inputPortList();
            } else {
                inputPorts = ((CompositeActor) getContainer()).inputPortList();
            }
            for (Object portObject : inputPorts) {
                IOPort port = (IOPort) portObject;
                for (int i = 0; i < port.getWidth(); i++) {
                    if (port.hasToken(i)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /** Create the controllerName attribute.
     * @throws NameDuplicationException
     * @throws IllegalActionException */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        controllerName = new StringAttribute(this, "controllerName");
    }

    private boolean _isInController() {
        return getContainer() instanceof ERGController;
    }

    private void _requestFiring() throws IllegalActionException {
        NamedObj container = getContainer();
        if (!_eventQueue.isEmpty()) {
            Time time = _eventQueue.peek().timeStamp;
            if (_isInController()) {
                ERGController controller = (ERGController) container;
                controller.getExecutiveDirector().fireAt(controller, time);
            } else {
                CompositeActor composite = (CompositeActor) container;
                composite.getExecutiveDirector().fireAt(composite, time);
            }
        } else if (!_inputQueue.isEmpty()) {
            ERGController controller = (ERGController) container;
            controller.getExecutiveDirector().fireAt(controller,
                    Time.POSITIVE_INFINITY);
        }
    }

    private boolean _synchronizeToRealtime(Time nextEventTime) {
        long elapsedTime = System.currentTimeMillis() - _realStartTime;
        double elapsedTimeInSeconds = elapsedTime / 1000.0;
        long timeToWait = (long) (nextEventTime.subtract(elapsedTimeInSeconds)
                .getDoubleValue() * 1000.0);
        if (timeToWait > 0) {
            try {
                _workspace.wait(_eventQueue, timeToWait);
                if (_stopRequested) {
                    return false;
                }
            } catch (InterruptedException ex) {
                return false;
            }
        }
        return true;
    }

    // Cached reference to mode controller.
    private ERGController _controller = null;

    // Version of cached reference to mode controller.
    private long _controllerVersion = -1;

    @SuppressWarnings("unchecked")
    private Comparator<TimedEvent> _eventComparator =
        new TimedEvent.TimeComparator();

    private PriorityQueue<TimedEvent> _eventQueue =
        new PriorityQueue<TimedEvent>(10, _eventComparator);

    private PriorityQueue<TimedEvent> _inputQueue =
        new PriorityQueue<TimedEvent>(5, _eventComparator);

    private long _realStartTime;

    private static class TimedEvent extends ptolemy.actor.util.TimedEvent {

        public TimedEvent(Time time, Object object, ArrayToken arguments) {
            super(time, object);
            _arguments = arguments;
        }

        /** Display timeStamp and contents. */
        public String toString() {
            String result = "timeStamp: " + timeStamp + ", contents: " +
                    contents;
            if (_arguments != null) {
                result += "(" + _arguments + ")";
            }
            return result;
        }

        protected ArrayToken _arguments;
    }
}

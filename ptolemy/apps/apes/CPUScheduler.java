package ptolemy.apps.apes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;
import ptolemy.apps.apes.TaskExecutionListener.ScheduleEventType;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ResourceToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * The CPU resource implements a fixed priority preemptive scheduling of tasks.
 * Actors that can send events to the input of the CPU resource are triggers and
 * tasks. Input tokens are ResourceTokens. Output events are sent to tasks,
 * output tokens are empty tokens, they are just used to trigger task actors.
 * The CPU resource manages a stack of all tasks and their remaining times
 * according to the task priorities. When the CPU resource is fired, it
 * decreases the remaining time of the currently executing task, puts the tasks
 * that are scheduled to be
 * 
 * @author Patricia Derler
 */
public class CPUScheduler extends ResourceActor {

    /**
     * Construct an actor in the default workspace with an empty string as its
     * name. The object is added to the workspace directory. Increment the
     * version number of the workspace.
     * 
     * @throws IllegalActionException
     */
    public CPUScheduler() {
        super();
        _initialize();
    }

    /**
     * Construct an actor in the specified workspace with an empty string as a
     * name. You can then change the name with setName(). If the workspace
     * argument is null, then use the default workspace. The object is added to
     * the workspace directory. Increment the version number of the workspace.
     * 
     * @param workspace
     *            The workspace that will list the entity.
     */
    public CPUScheduler(Workspace workspace) {
        super(workspace);
        _initialize();
    }

    /**
     * Create a new actor in the specified container with the specified name.
     * The name must be unique within the container or an exception is thrown.
     * The container argument must not be null, or a NullPointerException will
     * be thrown.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of this actor within the container.
     * @exception IllegalActionException
     *                If this actor cannot be contained by the proposed
     *                container (see the setContainer() method).
     * @exception NameDuplicationException
     *                If the name coincides with an entity already in the
     *                container.
     */
    public CPUScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    public enum TaskState {
        running, ready, suspended, terminated
    }

    // TODO initialize private variables - maps and lists
    public Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    /**
     * Schedule actors.
     */
    public void fire() throws IllegalActionException {
        Time passedTime = getDirector().getModelTime().subtract(
                _previousModelTime);
        Actor taskInExecution = null;

        // decrease remaining time of task currently in execution
        if (_tasksInExecution.size() > 0) {
            taskInExecution = _tasksInExecution.peek();
            if (passedTime.getDoubleValue() > 0.0) {
                Time remainingTimeForTaskInExecution = _taskExecutionTimes
                        .get(taskInExecution);
                if (remainingTimeForTaskInExecution
                        .equals(Time.POSITIVE_INFINITY)) {
                    _usedExecutionTimes.put(taskInExecution,
                            _usedExecutionTimes.get(taskInExecution).add(
                                    passedTime));
                } else {
                    remainingTimeForTaskInExecution = remainingTimeForTaskInExecution
                            .subtract(passedTime);
                }
                if (remainingTimeForTaskInExecution.equals(new Time(
                        getDirector(), 0.0))) {
                    _tasksInExecution.pop();
                    _sendTaskExecutionEvent(taskInExecution, getDirector()
                            .getModelTime().getDoubleValue(),
                            ScheduleEventType.STOP);
                    if (_tasksInExecution.size() > 0) {
                        Actor secondTask = _tasksInExecution.peek();
                        _sendTaskExecutionEvent(secondTask, getDirector()
                                .getModelTime().getDoubleValue(),
                                ScheduleEventType.START);
                    }
                    output.send(
                            ((Integer) _connectedTasks.get(taskInExecution))
                                    .intValue(), new BooleanToken(true));
                }
                _taskExecutionTimes.put(taskInExecution,
                        remainingTimeForTaskInExecution);
            }
        }

        // schedule tasks that requested refiring
        for (int channelId = 0; channelId < input.getWidth(); channelId++) {
            if (input.hasToken(channelId)) {
                ResourceToken token = (ResourceToken) input.get(channelId);
                Actor actorToSchedule = token.actorToSchedule;
                Time executionTime = (Time) token.requestedValue;

                // if task is scheduled as a result of a callback
                if (executionTime.compareTo(new Time(getDirector(), 0.0)) >= 0) {
                    _scheduleTask(actorToSchedule, executionTime);
                } // else task is scheduled as a result of a trigger
                // the task is not in execution yet, only after 
            }
        }

        // next task in list can be started?
        if (_tasksInExecution.size() > 0) {
            taskInExecution = _tasksInExecution.peek();
            Time remainingTime = _taskExecutionTimes.get(taskInExecution);
            if (remainingTime.equals(new Time(getDirector(), 0.0))) {
                _tasksInExecution.pop();
                _sendTaskExecutionEvent(taskInExecution, getDirector()
                        .getModelTime().getDoubleValue(),
                        ScheduleEventType.START);
                if (_tasksInExecution.size() > 0) {
                    Actor secondTask = _tasksInExecution.peek();
                    _sendTaskExecutionEvent(secondTask, getDirector()
                            .getModelTime().getDoubleValue(),
                            ScheduleEventType.STOP);
                }
                output.send(_connectedTasks.get(taskInExecution).intValue(),
                        new BooleanToken(true));
                _tasksInExecution.push(taskInExecution);
                _taskExecutionTimes
                        .put(taskInExecution, Time.POSITIVE_INFINITY);
                _usedExecutionTimes.put(taskInExecution, new Time(
                        getDirector(), 0.0));
            } else {
                getDirector().fireAt(this,
                        remainingTime.add(getDirector().getModelTime()));
            }
        }
        _previousModelTime = getDirector().getModelTime();
    }

    /**
     * Returns the priority of the actor. The priority is an int value. The
     * default return value is 0.
     * 
     * @param actor
     *            Given actor.
     * @return Priority of the given actor.
     */
    public int getPriority(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("priority");

            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();

                return token.intValue();
            } else {
                return 0;
            }
        } catch (ClassCastException ex) {
            return 0;
        } catch (IllegalActionException ex) {
            return 0;
        }
    }

    /**
     * Set private variables.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            _previousModelTime = new Time(getDirector(), 0.0);
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }

    }

    /**
     * Register task execution listener.
     * @param taskExecutionListener
     */
    protected void _registerExecutionListener(
            TaskExecutionListener taskExecutionListener) {
        if (_executionListeners == null)
            _executionListeners = new ArrayList<TaskExecutionListener>();
        _executionListeners.add(taskExecutionListener);
    }

    /**
     * Send event to the task execution listeners.
     * @param actor
     * @param time
     * @param eventType
     */
    protected final void _sendTaskExecutionEvent(Actor actor, double time,
            ScheduleEventType eventType) {
        if (_executionListeners != null) {
            Iterator listeners = _executionListeners.iterator();

            while (listeners.hasNext()) {
                ((TaskExecutionListener) listeners.next()).event(actor, time,
                        eventType);
            }
        }
    }

    /**
     * Initialize private variables.
     */
    private void _initialize() {
        _taskExecutionTimes = new HashMap<Actor, Time>();
        _usedExecutionTimes = new HashMap<Actor, Time>();
        _tasksInExecution = new Stack<Actor>();
        _taskStates = new HashMap<Actor, TaskState>();
    }

    /**
     * If the actor to schedule is not in the list, add it to the list at the 
     * correct position (according to the priority) and update the remaining 
     * execution time of the actor. If the actor is already in the 
     * list (then it has the execution time Time.POSITIVE_INFINITY) update the 
     * execution time of the actor.
     * @param actorToSchedule
     * @param executionTime
     * @throws IllegalActionException
     */
    private void _scheduleTask(Actor actorToSchedule, Time executionTime)
            throws IllegalActionException {
        Actor taskInExecution = null;
        if (_tasksInExecution.size() > 0)
            taskInExecution = _tasksInExecution.peek();

        if (taskInExecution == null
                || getPriority(actorToSchedule) > getPriority(taskInExecution)) {
            _tasksInExecution.push(actorToSchedule);
        } else if (_tasksInExecution.contains(actorToSchedule)) {
            // FIXME don't schedule if already scheduled
            executionTime = executionTime.subtract(_usedExecutionTimes
                    .get(actorToSchedule));
            _usedExecutionTimes.put(actorToSchedule, new Time(getDirector(),
                    0.0));
        } else {
            for (int i = 1; i < _tasksInExecution.size(); i++) { //TODO: more efficient implementation
                Actor actor = _tasksInExecution.get(i);
                if (getPriority(actor) < getPriority(actorToSchedule)) {
                    _tasksInExecution.insertElementAt(actorToSchedule, i);
                    break;
                }
            }
        }
        _taskExecutionTimes.put(actorToSchedule, executionTime);
    }

    /** Tasks in execution and their remaining execution time. */
    private Map<Actor, Time> _taskExecutionTimes;
    private Map<Actor, Time> _usedExecutionTimes;

    /** Tasks in execution. */
    private Stack<Actor> _tasksInExecution;

    /** Model time at the previous firing. */
    private Time _previousModelTime;

    /** List of all tasks and their current state */
    private Map<Actor, TaskState> _taskStates;

    private Collection<TaskExecutionListener> _executionListeners;

}

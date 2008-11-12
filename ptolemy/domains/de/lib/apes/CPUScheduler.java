package ptolemy.domains.de.lib.apes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ptolemy.actor.Actor; 
import ptolemy.actor.util.Time;
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
 * 
 * Actors that can send events to the input of the CPU resource are triggers and tasks. 
 * Input tokens are ResourceTokens.
 * 
 * Output events are sent to tasks, output tokens are empty tokens, they are just used to 
 * trigger task actors. 
 * 
 * The CPU resource manages a stack of all tasks and their remaining times according to the
 * task priorities. When the CPU resource is fired, it decreases the remaining time of the
 * currently executing task, puts the tasks that are scheduled to be 
 * 
 * @author Patricia Derler
 *
 */
public class CPUScheduler extends ResourceActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     * @throws IllegalActionException 
     */
    public CPUScheduler() {
        super(); 
        _initialize();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public CPUScheduler(Workspace workspace) {
        super(workspace); 
        _initialize();
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public CPUScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _initialize();
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
     * Schedule actors. 
     */
    public void fire() throws IllegalActionException {
        Actor taskInExecution = null;
        // decrease remaining time of task currently in execution
        if (_tasksInExecution.size() > 0) {
            taskInExecution = _tasksInExecution.peek();
            if (getDirector().getModelTime().compareTo(_previousModelTime) > 0) {
                Time remainingTime = _taskExecutionTimes.get(taskInExecution);
                remainingTime = remainingTime.subtract(getDirector().getModelTime()
                        .subtract(_previousModelTime));
                if (remainingTime.equals(new Time(getDirector(), 0.0))) {
                    _tasksInExecution.pop();
                    output.send(((Integer)_connectedTasks.get(taskInExecution)).intValue(), null);
                    if (_tasksInExecution.size() > 0)
                        taskInExecution = _tasksInExecution.pop();
                } else {
                    _taskExecutionTimes.put(taskInExecution, remainingTime);
                }
            }
        }
        
        // schedule new tasks
        for (int channelId = 0; channelId < input.getWidth(); channelId++) {
            ResourceToken token = (ResourceToken) input.get(channelId);
            Actor actorToSchedule = token.actorToSchedule;
            Time executionTime = (Time)token.requestedValue;
            if (taskInExecution == null ||
                    getPriority(actorToSchedule) > getPriority(taskInExecution)) {
                _tasksInExecution.push(actorToSchedule);
                _taskExecutionTimes.put(actorToSchedule, executionTime);
            } else {
                for (int i = 1; i < _tasksInExecution.size(); i++) {
                    Actor actor = _tasksInExecution.get(i);
                    if (getPriority(actor) < getPriority(actorToSchedule)) {
                        _tasksInExecution.insertElementAt(actorToSchedule, i);
                        break;
                    }
                }                    
            }
        }
    }
    
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            _previousModelTime = new Time(getDirector(), 0.0);
        } catch (IllegalActionException e) { 
            e.printStackTrace();
        }
        
        for (Actor task : _connectedTasks.keySet()) {
            //output.send(_connectedTasks.get(task), 0.0)
        }
    }
    
    /**
     * Initialize private variables.
     */
    private void _initialize() {
        _taskExecutionTimes = new HashMap();
        _tasksInExecution = new Stack();
        
    }
    
    /** Task trigger periods */
    private Map<Actor, Time> _taskTriggerPeriods;
    
    /** Tasks in execution and their remaining execution time. */
    private Map<Actor, Time> _taskExecutionTimes;
    
    /** Tasks in execution. */
    private Stack<Actor> _tasksInExecution;
    
    /** Model time at the previous firing. */
    private Time _previousModelTime;
    
}

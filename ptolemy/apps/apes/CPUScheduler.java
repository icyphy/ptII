
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


   public enum TaskState {
       running, ready, suspended, terminated
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


   public Time getTaskTriggerPeriod(Actor actor) throws IllegalActionException {
       try {
           Parameter parameter = (Parameter) ((NamedObj) actor)
                   .getAttribute("triggerPeriod");

           if (parameter != null) {
               DoubleToken token = (DoubleToken) parameter.getToken();

               return new Time(getDirector(), token.doubleValue());
           } else {
               return new Time(getDirector(), 1.0);
           }
       } catch (ClassCastException ex) {
           return new Time(getDirector(), 1.0);
       } catch (IllegalActionException ex) {
           return new Time(getDirector(), 1.0);
       }
   }

   public Time getTaskTriggerOffset(Actor actor) throws IllegalActionException {
       try {
           Parameter parameter = (Parameter) ((NamedObj) actor)
                   .getAttribute("triggerOffset");

           if (parameter != null) {
               DoubleToken token = (DoubleToken) parameter.getToken();

               return new Time(getDirector(), token.doubleValue());
           } else {
               return new Time(getDirector(), 0.0);
           }
       } catch (ClassCastException ex) {
           return new Time(getDirector(), 0.0);
       } catch (IllegalActionException ex) {
           return new Time(getDirector(), 0.0);
       }
   }

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
                   _sendTaskExecutionEvent(taskInExecution, getDirector().getModelTime().getDoubleValue(), ScheduleEventType.STOP);
                   output.send(((Integer)_connectedTasks.get(taskInExecution)).intValue(), new BooleanToken(true)); 
               } else {
                   _taskExecutionTimes.put(taskInExecution, remainingTime);
               }
           }
       }

       // schedule tasks that request refiring
       for (int channelId = 0; channelId < input.getWidth(); channelId++) {
           if (input.hasToken(channelId)) {
               ResourceToken token = (ResourceToken) input.get(channelId);
               Actor actorToSchedule = token.actorToSchedule;
               Time executionTime = (Time)token.requestedValue;
               scheduleTask(actorToSchedule, executionTime);
           }
       }

       // next task in list can be started?
       if (_tasksInExecution.size() > 0) {
           Actor task = _tasksInExecution.peek(); 
           Time remainingTime = _taskExecutionTimes.get(task);
           if (remainingTime.equals(new Time(getDirector(), 0.0))) {
               _tasksInExecution.pop();
               _sendTaskExecutionEvent(task, getDirector().getModelTime().getDoubleValue(), ScheduleEventType.START);
               output.send(_connectedTasks.get(task).intValue(), new BooleanToken(true)); 
           } 
       }
   }

   private void scheduleTask( Actor actorToSchedule,
           Time executionTime) {
       Actor taskInExecution = null; 
       if (_tasksInExecution.size() > 0) 
           taskInExecution = _tasksInExecution.peek();
           
       if (taskInExecution == null ||
               getPriority(actorToSchedule) > getPriority(taskInExecution)) {
           _tasksInExecution.push(actorToSchedule);
       } else {
           for (int i = 1; i < _tasksInExecution.size(); i++) {  //TODO: more efficient implementation
               Actor actor = _tasksInExecution.get(i);
               if (getPriority(actor) < getPriority(actorToSchedule)) {
                   _tasksInExecution.insertElementAt(actorToSchedule, i);
                   break;
               }
           }                    
       }
       _taskExecutionTimes.put(actorToSchedule, executionTime);
   }

   @Override
   public void initialize() throws IllegalActionException {
       super.initialize();
       try {
           _previousModelTime = new Time(getDirector(), 0.0);
       } catch (IllegalActionException e) { 
           e.printStackTrace();
       }

       
   }

   // TODO initialize private variables - maps and lists
   public Object clone() throws CloneNotSupportedException {
       // TODO Auto-generated method stub
       return super.clone();
   }

   /**
    * Initialize private variables.
    */
   private void _initialize() {
       _taskExecutionTimes = new HashMap<Actor, Time>();
       _tasksInExecution = new Stack<Actor>();
       _taskStates = new HashMap<Actor, TaskState>(); 
   }


   /** Tasks in execution and their remaining execution time. */
   private Map<Actor, Time> _taskExecutionTimes;

   /** Tasks in execution. */
   private Stack<Actor> _tasksInExecution;

   /** Model time at the previous firing. */
   private Time _previousModelTime;

   /** List of all tasks and their current state */
   private Map<Actor, TaskState> _taskStates;

   private Collection<TaskExecutionListener> _executionListeners;

   public void registerExecutionListener(
           TaskExecutionListener taskExecutionListener) {
       if (_executionListeners == null)
           _executionListeners = new ArrayList();
       _executionListeners.add(taskExecutionListener);
   }

}
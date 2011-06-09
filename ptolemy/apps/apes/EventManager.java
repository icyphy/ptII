package ptolemy.apps.apes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.util.Time;
import ptolemy.apps.apes.CPUScheduler.TaskState; 
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class EventManager extends ApeActor {
   
   
   /** Construct an actor in the default workspace with an empty string
    *  as its name.  The object is added to the workspace directory.
    *  Increment the version number of the workspace.
    * @throws IllegalActionException 
    */
   public EventManager() {
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
   public EventManager(Workspace workspace) {
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
   public EventManager(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
       super(container, name);
       _initialize();
   }
   
   
   public enum StatusType {
       E_OK, E_OS_ACCESS, E_OS_CALLEVEL, E_OS_ID, E_OS_LIMIT, E_OS_NOFUNC, E_OS_RESOURCE, E_OS_STATE, E_OS_VALUE}

   /**
    * Returns the id of the actor. The id is an int value. The
    * default return value is 0.
    * 
    * @param actor
    *            Given actor.
    * @return Priority of the given actor.
    */
   public int getTaskId(Actor actor) {
       try {
           Parameter parameter = (Parameter) ((NamedObj) actor)
                   .getAttribute("ID");

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
   
   //////////////////////
   /// Event Control

   /** <taskId, List of Events registered for the task> */
   public HashMap<Actor, Integer> _actorsWaitingForEvents; 
   public HashMap<Actor, Integer> _actorsWithEvents; 
   
   public int setEvent(int taskId, int newEvents) throws NoRoomException, IllegalActionException {

       Actor task = _tasks.get(taskId);
       Actor currentTask = _taskNames.get(Thread.currentThread().getName());
       if(_actorsWithEvents.get(task) == null){
           _actorsWithEvents.put(task, newEvents);
       }
       else{
       _actorsWithEvents.put(task, _actorsWithEvents.get(task).intValue() | newEvents);
       }
       if ((_actorsWaitingForEvents.get(task) != null) && 
               ((_actorsWaitingForEvents.get(task).intValue() & newEvents) != 0)) {
           ((CTask)currentTask).bufferOutput(new ResourceToken(task, null, TaskState.ready_running));
       }
       return 0;
   }
   
   public int clearEvent(int eventMask) {
       int clearMask = ~eventMask;
       Actor currentTask = _taskNames.get(Thread.currentThread().getName());
       _actorsWithEvents.put(currentTask,_actorsWithEvents.get(currentTask).intValue() & clearMask);
       return 0;
   }
   
   public int waitEvent(int eventMask) throws NoRoomException, IllegalActionException {
       Actor currentTask = _taskNames.get(Thread.currentThread().getName()); 
       if ((_actorsWithEvents.get(currentTask) != null) && ((_actorsWithEvents.get(currentTask).intValue() & eventMask) != 0)){
           return 0;
       }
       _actorsWaitingForEvents.put(currentTask, eventMask);
       ((CTask)currentTask).bufferOutput(new ResourceToken(currentTask, null, TaskState.waiting));
       return 0;
   }
   
   @Override
   public void initialize() throws IllegalActionException { 
       super.initialize(); 
       CompositeActor compositeActor = (CompositeActor) getContainer();
       List entities = compositeActor.entityList();
       for (Iterator it = entities.iterator(); it.hasNext();) {
           Object entity = it.next();
           if (entity instanceof Actor) {
               Actor actor = (Actor) entity;
               if (actor instanceof CTask) {
                   _tasks.put(getTaskId(actor), actor); 
                   _taskNames.put(actor.getName(), actor);
               }
           }
       }
   }
   
   public native void InitializeC();
   
   private void _initialize() {
       
       _tasks = new HashMap<Integer, Actor>();
       _taskNames = new HashMap<String, Actor>();
       _actorsWaitingForEvents = new HashMap<Actor, Integer>();
       _actorsWithEvents = new HashMap<Actor, Integer>();
       
       Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
       sourceActorList.setExpression("*");

       Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
       destinationActorList.setExpression("CPUScheduler");
        
       
    }

   
   
   /** Map of taskIds to tasks. */
   private Map<Integer, Actor> _tasks;
   
   /** Map of taskNames and tasks. */
   private Map<String, Actor> _taskNames;
   
}
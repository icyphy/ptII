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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class EventManager extends ApeActor {
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
    public HashMap<Actor, List<Integer>> _actorsWaitingForEvents; 
    
    public StatusType SetEvent(int taskId, List<Integer> newEvents) throws NoRoomException, IllegalActionException {
        boolean taskResumes = false;
        Actor task = _tasks.get(taskId);
        for (Integer eventId : newEvents) {
            if (_actorsWaitingForEvents.get(task).contains(eventId)) {
                taskResumes = true;
                break;
            }
        }
        if (taskResumes) {
            output.send(new ResourceToken(task, Time.POSITIVE_INFINITY, TaskState.ready_running));
        }
        return StatusType.E_OK;
    }
    
    public StatusType ClearEvent() {
        Actor currentTask = _taskNames.get(Thread.currentThread().getName());
        _actorsWaitingForEvents.remove(currentTask);
        return StatusType.E_OK;
    }
    
    public StatusType WaitEvent(List<Integer> events) throws NoRoomException, IllegalActionException {
        Actor currentTask = _taskNames.get(Thread.currentThread().getName()); 
        _actorsWaitingForEvents.put(currentTask, events);
        output.send(new ResourceToken(currentTask, Time.POSITIVE_INFINITY, TaskState.waiting));
        return StatusType.E_OK;
    }
   
    
    private void _initialize() {
        _tasks = new HashMap();
         
        Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
        sourceActorList.setExpression("*");

        Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
        destinationActorList.setExpression("CPUScheduler");
        
        
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

    
    
    /** Map of taskIds to tasks. */
    private Map<Integer, Actor> _tasks;
    
    /** Map of taskNames and tasks. */
    private Map<String, Actor> _taskNames;
    
}

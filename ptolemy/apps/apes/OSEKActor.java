package ptolemy.apps.apes;

import ptolemy.actor.Actor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class OSEKActor extends ApeActor {
    

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public OSEKActor() {
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
    public OSEKActor(Workspace workspace) {
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
    public OSEKActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    public enum ReturnCode {
        E_OK, E_OS_ID, E_OS_LIMIT
    }
    
    //////////////////////
    /// Task management
    
    // DeclareTask ??

    public ReturnCode ActivateTask(int taskId) throws NoRoomException, IllegalActionException {
        Actor task = _getTask(taskId);
        if (task == null)
            return ReturnCode.E_OS_ID;
        //else if (max tasks running) return E_OS_LIMIT;
        output.send(new ResourceToken(task, Time.POSITIVE_INFINITY, CPUScheduler.TaskState.ready_running));
        return ReturnCode.E_OK;
    }
    
    public void TerminateTask() throws NoRoomException, IllegalActionException {
        Actor task = _getTask(Thread.currentThread().getName());
        // if (task still occupies resources) return E_OS_RESOURCE;
        // else if (call at interrupt level) return E_OS_CALLEVEL;
        output.send(new ResourceToken(task, Time.POSITIVE_INFINITY, CPUScheduler.TaskState.suspended));

    }
    
    public void ChainTask(int taskId) throws NoRoomException, IllegalActionException {
        Actor task = _getTask(taskId);
        Actor currentTask = _getTask(Thread.currentThread().getName());
        output.send(new ResourceToken(currentTask, Time.POSITIVE_INFINITY, CPUScheduler.TaskState.suspended));
        output.send(new ResourceToken(task, Time.POSITIVE_INFINITY, CPUScheduler.TaskState.ready_running));

    }
    
    /**
     * release resources if higher priority task is ready
     * @return
     */
    public void Schedule() {
        
    }
    
    public ReturnCode GetTaskId(/*TaskRefType*/int taskId) {
        return ReturnCode.E_OK;
    }
    
    public ReturnCode GetTaskState(int taskId, /*taskRefType*/int taskState) {
        return ReturnCode.E_OK;
    }
    
    
    //////////////////////
    /// Resource management
    
    // DeclareResource
    
//    public ReturnCode GetResource(int resourceId) {
//        if (getResource(resourceId)) {
//            resources.put(getResource(resourceId), _getTask(Thread.currentThread().getName()));
//        } else {
//            // schedule refiring at 
//        }
//        return ReturnCode.E_OK;
//    }
//    
//    public ReturnCode ReleaseResource(int resourceId) {
//        resources.put(getResource(resourceId), null);
//        return ReturnCode.E_OK;
//    }
    
    //////////////////////
    /// Event Control
    
    public void DeclareEvent(int eventId) {
        
    }
    
    //////////////////////
    /// private methods
    

    private Actor _getTask(String name) { 
        return null;
    }

    private Actor _getTask(int taskId) { 
        return null;
    }
    
    private void _initialize() {
        Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
        sourceActorList.setExpression("*");

        Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
        destinationActorList.setExpression("CPUScheduler");
    }
    
}

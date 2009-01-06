package ptolemy.apps.apes;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ResourceToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class InterruptServiceRoutine extends ApeActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public InterruptServiceRoutine() {
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
    public InterruptServiceRoutine(Workspace workspace) {
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
    public InterruptServiceRoutine(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    @Override
    public void fire() throws IllegalActionException {
        System.out.println("IRS.fire() - Time: " + getDirector().getModelTime().toString() + " this: " + this.getName());
        output.send(new ResourceToken(_task, Time.POSITIVE_INFINITY, CPUScheduler.TaskState.ready_running));
    }
    
    @Override
    public void initialize() throws IllegalActionException { 
        super.initialize();
     
        CompositeActor container = (CompositeActor) this.getContainer();
        List entities = container.entityList();
        for (Iterator it = entities.iterator(); it.hasNext(); ) {
            Object entity = it.next();
            if (entity instanceof Actor) {
                Actor actor = (Actor) entity;
                if (triggerTarget.getExpression().equals(actor.getName())) {
                    _task = actor;
                }
            }
        }
    }
    
    public StringParameter triggerTarget;
    
    
    private void _initialize() {
        Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
        sourceActorList.setExpression("*");

        Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
        destinationActorList.setExpression("CPUScheduler");
        
        try {
            triggerTarget = new StringParameter(this, "triggerTarget");
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameDuplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
    }
    
    private Actor _task;
    
}

package ptolemy.apps.apes;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ResourceToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class InterruptServiceRoutine extends TypedAtomicActor {

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

    public IOPort input;
    public IOPort output;
    public IOPort taskConnector;
    
    @Override
    public void fire() throws IllegalActionException {
        input.get(0);
        System.out.println("Time: " + getDirector().getModelTime().toString() + "; Task: " + this.getName());
        output.send(0, new ResourceToken(_task, new Time(getDirector(), 0.0)));
    }
    
    @Override
    public void initialize() throws IllegalActionException { 
        super.initialize();
        
        _task = (Actor) ((IOPort)taskConnector.sourcePortList().get(0)).getContainer();
        
    }
    
    private void _initialize() {
        try {
            input = new TypedIOPort(this, "input", true, false);
            output = new TypedIOPort(this, "output", false, true);
            taskConnector = new TypedIOPort(this, "taskConnector", true, false);
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

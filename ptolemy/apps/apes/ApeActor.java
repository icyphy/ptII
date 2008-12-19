package ptolemy.apps.apes;

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * This abstract class implements a platform resource. Examples are the CPU or memory.
 * A resource actor receives requests from tasks on the multiport input. Those requests 
 * contain a ResourceToken. A ResourceToken is a pair of a task and a value that describes what is
 * requested from the resource. In the fire method of derived classes, the task scheduling 
 * should be implemented. When a task is scheduled to do something, this resource actor sends 
 * an event to the task through the multiport output on the correct channel. 
 * 
 * TODO: implement ResourceToken
 * @author Patricia Derler
 *
 */
public abstract class ApeActor extends TypedAtomicActor {
    
    
    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     * @throws IllegalActionException 
     */
    public ApeActor() {
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
    public ApeActor(Workspace workspace) {
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
    public ApeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


     /** multicast input port that receives requests from tasks */
    public MulticastIOPort input;

    /** multicast output port used to send answers to requests */
    public MulticastIOPort output;
      
    
    public void initialize() throws IllegalActionException { 
        super.initialize();
        
    }
    
    /**
     * Initialize ports.
     */
    private void _initialize() {
        try {
            input = new MulticastIOPort(this, "input", true, false);
            input.setMultiport(true); 
            input.setTypeEquals(BaseType.GENERAL);
            output = new MulticastIOPort(this, "output", false, true);
            output.setMultiport(true);
            output.setTypeEquals(BaseType.GENERAL);
       
        } catch (Exception ex) {
            ex.printStackTrace();
         }
    }
  
}

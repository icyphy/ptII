package ptolemy.apps.apes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.Time;
import ptolemy.apps.apes.CPUScheduler.TaskState;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class InterruptServiceController extends TypedAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public InterruptServiceController() throws IllegalActionException, NameDuplicationException {
        super();
        _initialize();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public InterruptServiceController(Workspace workspace) throws IllegalActionException, NameDuplicationException {
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
    public InterruptServiceController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    
    public MulticastIOPort suspendOSSystemCalls;
    public MulticastIOPort resumeOSSystemCalls;
    
    public MulticastIOPort disableAllSystemCalls;
    public MulticastIOPort enableAllSystemCalls;
    public MulticastIOPort suspendAllSystemCalls;
    public MulticastIOPort resumeAllSystemCalls;
    
    public MulticastIOPort output;
    
    public Actor getIRS(IOPort actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("IRS");

            if (parameter != null) {
                ObjectToken token = (ObjectToken) parameter.getToken();

                return (Actor) token.getValue();
            } else {
                return null;
            }
        } catch (ClassCastException ex) {
            return null;
        } catch (IllegalActionException ex) {
            return null;
        }
    }
    
    @Override
    public void fire() throws IllegalActionException {
        System.out.println(this.getName() + ".fire() ");
        for (IOPort port : (Collection<IOPort>)inputPortList()) {
            while (port.hasToken(0)){
                Token token = port.get(0);
                if (port == suspendOSSystemCalls) {
                    save = true;
                    send = false;
                } else if (port == resumeOSSystemCalls) {
                    save = false;
                    send = true;
                } else if (port == disableAllSystemCalls) {
                    save = false;
                    send = false;
                } else if (port == suspendAllSystemCalls) {
                    save = true;
                    send = false;
                } else if (port == resumeAllSystemCalls) {
                    save = false;
                    send = true;
                } else {        
                    Actor actor = getIRS(port); 
                    ResourceToken resourceToken = new ResourceToken(actor, Time.POSITIVE_INFINITY, TaskState.ready_running);
                    System.out.println("  send: " + actor.getName());
                    if (send) {
                        output.send(resourceToken);
                    } else if (save) {
                        _savedInterrupts.add(resourceToken);
                    }
                }
            }
        }
    }
    
    private boolean send = true;
    private boolean save = false;
    private List<ResourceToken> _savedInterrupts = new ArrayList();
    
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
                    _taskNames.put(actor.getName(), actor);
                }
            }
        }
    }
    
    /**
     * Initialize private variables.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    private void _initialize() throws IllegalActionException, NameDuplicationException {
        _taskNames = new HashMap();
        
        suspendOSSystemCalls = new MulticastIOPort(this, "suspendOSSystemCalls", true, false); 
        resumeOSSystemCalls = new MulticastIOPort(this, "resumeOSSystemCalls", true, false);
        disableAllSystemCalls = new MulticastIOPort(this, "disableAllSystemCalls", true, false);
        enableAllSystemCalls = new MulticastIOPort(this, "enableAllSystemCalls", true, false);
        suspendAllSystemCalls = new MulticastIOPort(this, "suspendAllSystemCalls", true, false);
        resumeAllSystemCalls = new MulticastIOPort(this, "resumeAllSystemCalls", true, false);
        output = new MulticastIOPort(this, "output", false, true);
        
        suspendOSSystemCalls.setMultiport(true);
        resumeOSSystemCalls.setMultiport(true);
        disableAllSystemCalls.setMultiport(true);
        enableAllSystemCalls.setMultiport(true);
        suspendAllSystemCalls.setMultiport(true);
        resumeAllSystemCalls.setMultiport(true);
        output.setMultiport(true);
        
        ((Parameter) suspendOSSystemCalls.getAttribute("sourceActors")).setExpression("*");
        ((Parameter) resumeOSSystemCalls.getAttribute("sourceActors")).setExpression("*");
        ((Parameter) disableAllSystemCalls.getAttribute("sourceActors")).setExpression("*");
        ((Parameter) enableAllSystemCalls.getAttribute("sourceActors")).setExpression("*");
        ((Parameter) suspendAllSystemCalls.getAttribute("sourceActors")).setExpression("*");
        ((Parameter) resumeAllSystemCalls.getAttribute("sourceActors")).setExpression("*");
          
        ((Parameter) output.getAttribute("destinationActors")).setExpression("CPUScheduler");
        
        
     }
    
    private HashMap<String, Actor> _taskNames;
    
}


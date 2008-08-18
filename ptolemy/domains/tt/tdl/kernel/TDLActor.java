package ptolemy.domains.tt.tdl.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.RealDependency;
import ptolemy.actor.util.Time;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.modal.ModalPort;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.graph.Node;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * TDL Actor used in the TDL domain.
 * 
 * @author Patricia Derler
 * 
 */
public class TDLActor extends FSMActor {

	/**
	 * Construct an FSMActor in the default workspace with an empty string as
	 * its name. Add the actor to the workspace directory. Increment the version
	 * number of the workspace.
	 */
	public TDLActor() {
		super();
	}

	/**
	 * Construct an FSMActor in the specified workspace with an empty string as
	 * its name. You can then change the name with setName(). If the workspace
	 * argument is null, then use the default workspace. Add the actor to the
	 * workspace directory. Increment the version number of the workspace.
	 * 
	 * @param workspace
	 *            The workspace that will list the actor.
	 */
	public TDLActor(Workspace workspace) {
		super(workspace);
	}

	/**
	 * Create an FSMActor in the specified container with the specified name.
	 * The name must be unique within the container or an exception is thrown.
	 * The container argument must not be null, or a NullPointerException will
	 * be thrown.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor within the container.
	 * @exception IllegalActionException
	 *                If the entity cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the name coincides with an entity already in the
	 *                container.
	 */
	public TDLActor(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	/**
	 * Create a new Relation. This relation should not be a Transition but a TDL
	 * transition.
	 */
	public ComponentRelation newRelation(String name)
			throws IllegalActionException, NameDuplicationException {
		try {
			workspace().getWriteAccess();

			// Director director = getDirector();
			TDLTransition tr = new TDLTransition(this, name);
			return tr;
		} finally {
			workspace().doneWriting();
		}
	}

    

    public boolean readInput(Node node, IOPort p, long modePeriod)
            throws IllegalActionException { 
        // TODO for all channels  
        List<IOPort> ports = p.deepInsidePortList();
        for (IOPort port : ports) {
            if (port instanceof RefinementPort) {
                if (inputIsSafeToProcess(port)) {
                    super._readInputs(port, 0);
                    TDLModuleDirector director = ((TDLModuleDirector)getDirector());
                    
                    director.scheduleEventsAfterAction(node);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean inputIsSafeToProcess(IOPort port) throws IllegalActionException {
        Time modelTime = getDirector().getModelTime();
        Time physicalTime = getExecutiveDirector().getModelTime();
        CausalityInterfaceForComposites causalityInterface = (CausalityInterfaceForComposites) ((TDLModule)getContainer()).getCausalityInterface();
        Dependency minimumDelay = causalityInterface
                .getMinimumDelay(port);  
        if (minimumDelay instanceof RealDependency) {
        return ((RealDependency) minimumDelay).value() == Double.MAX_VALUE
                || modelTime.subtract(((RealDependency)minimumDelay).value()).compareTo(
                        physicalTime) <= 0;     
        } else 
            return true;
    }

}

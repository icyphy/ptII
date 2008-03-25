package ptolemy.domains.tt.tdl.kernel;

import ptolemy.domains.fsm.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * Controller for the TDL module
 * 
 * @author Patricia Derler
 * 
 */
public class TDLController extends TDLActor {
	/**
	 * Construct a modal controller in the specified workspace with no container
	 * and an empty string as a name. You can then change the name with
	 * setName(). If the workspace argument is null, then use the default
	 * workspace.
	 * 
	 * @param workspace
	 *            The workspace that will list the actor.
	 */
	public TDLController(Workspace workspace) {
		super(workspace);
	}

	/**
	 * Construct a modal controller with a name and a container. The container
	 * argument must not be null, or a NullPointerException will be thrown.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the container is incompatible with this actor.
	 * @exception NameDuplicationException
	 *                If the name coincides with an actor already in the
	 *                container.
	 */
	public TDLController(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Create a new port with the specified name in the container of this
	 * controller, which in turn creates a port in this controller and all the
	 * refinements. This method is write-synchronized on the workspace.
	 * 
	 * @param name
	 *            The name to assign to the newly created port.
	 * @return The new port.
	 * @exception NameDuplicationException
	 *                If the entity already has a port with the specified name.
	 */
	public Port newPort(String name) throws NameDuplicationException {
		try {
			_workspace.getWriteAccess();

			if (_mirrorDisable || (getContainer() == null)) {
				// Have already called the super class.
				// This time, process the request.
				TDLRefinementPort port = new TDLRefinementPort(this, name);

				// NOTE: Changed RefinementPort so mirroring
				// is enabled by default. This means mirroring
				// will occur during MoML parsing, but this
				// should be harmless. EAL 12/04.
				// port._mirrorDisable = false;
				// Create the appropriate links.
				ModalModel container = (ModalModel) getContainer();

				if (container != null) {
					String relationName = name + "Relation";
					Relation relation = container.getRelation(relationName);

					if (relation == null) {
						relation = container.newRelation(relationName);

						Port containerPort = container.getPort(name);
						containerPort.link(relation);
					}

					port.link(relation);
				}

				return port;
			} else {
				_mirrorDisable = true;
				((TDLModule) getContainer()).newPort(name);
				return getPort(name);
			}
		} catch (IllegalActionException ex) {
			// This exception should not occur, so we throw a runtime
			// exception.
			throw new InternalErrorException(
					"ModalController.newPort: Internal error: "
							+ ex.getMessage());
		} finally {
			_mirrorDisable = false;
			_workspace.doneWriting();
		}
	}

	/**
	 * Control whether adding a port should be mirrored in the modal model and
	 * refinements. This is added to allow control by the UI.
	 * 
	 * @param disable
	 *            True if mirroring should not occur.
	 */
	public void setMirrorDisable(boolean disable) {
		_mirrorDisable = disable;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ////

	/**
	 * Override the base class to ensure that the proposed container is a
	 * ModalModel or null.
	 * 
	 * @param container
	 *            The proposed container.
	 * @exception IllegalActionException
	 *                If the proposed container is not a TypedActor, or if the
	 *                base class throws it.
	 */
	protected void _checkContainer(Entity container)
			throws IllegalActionException {
		if (!(container instanceof ModalModel) && (container != null)) {
			throw new IllegalActionException(container, this,
					"ModalController can only be contained by "
							+ "ModalModel objects.");
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////
	// These are protected to be accessible to ModalModel.

	/** Indicator that we are processing a newPort request. */
	protected boolean _mirrorDisable = false;
}

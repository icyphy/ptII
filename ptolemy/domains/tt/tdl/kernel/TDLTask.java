package ptolemy.domains.tt.tdl.kernel;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * A TDL task is an SDF actor with some TDL specific parameters.
 * 
 * @author Patricia Derler
 * 
 */
public class TDLTask extends TypedCompositeActor {

	/**
	 * Construct a TDL task. You can then change the name with
	 * setName(). If the workspace argument is null, then use the default
	 * workspace. You should set a director before attempting to execute it. You
	 * should set the container before sending data to it. Increment the version
	 * number of the workspace.
	 * 
	 * @throws NameDuplicationException Thrown if parameters cannot be set.
	 * @throws IllegalActionException Thrown if parameters cannot be set.
	 */
	public TDLTask() throws IllegalActionException, NameDuplicationException {
		super();
		_init();
	}

	/**
	 * Construct a TDL Task in the specified workspace with no container
	 * and an empty string as a name. You can then change the name with
	 * setName(). If the workspace argument is null, then use the default
	 * workspace. You should set a director before attempting to execute it. You
	 * should set the container before sending data to it. Increment the version
	 * number of the workspace.
	 * 
	 * @param workspace
	 *            The workspace that will list the actor.
	 * @throws NameDuplicationException
	 * @throws IllegalActionException
	 */
	public TDLTask(Workspace workspace) throws IllegalActionException,
			NameDuplicationException {
		super(workspace);
		_init();
	}

	/**
	 * Create a TDL task with a name and a container. The container argument must
	 * not be null, or a NullPointerException will be thrown. This actor will
	 * use the workspace of the container for synchronization and version
	 * counts. If the name argument is null, then the name is set to the empty
	 * string. Increment the version of the workspace. This actor will have no
	 * local director initially, and its executive director will be simply the
	 * director of the container. You should set a director before attempting to
	 * execute it.
	 * 
	 * @param container
	 *            The container actor.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the container is incompatible with this actor.
	 * @exception NameDuplicationException
	 *                If the name coincides with an actor already in the
	 *                container.
	 */
	public TDLTask(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_init();
	}

	/**
	 * Frequency of the task invocation in the mode period.
	 */
	public Parameter frequency;

	/**
	 * Describes if task is a fast task.
	 */
	public Parameter fast;

	/**
	 * Slot selection string for the task.
	 */
	public Parameter slots;

	/**
	 * Create a new TDL port.
	 * 
	 * @param name Name of the TDL port.
	 * @return a new TDL Task output port.
	 * @throws NameDuplicationException If the name for the port already exists.
	 */
	public Port newPort(String name) throws NameDuplicationException {
		try {
			workspace().getWriteAccess();

			TDLTaskOutputPort port = new TDLTaskOutputPort(this, name);
			return port;
		} catch (IllegalActionException ex) {
			// This exception should not occur, so we throw a runtime
			// exception.
			throw new InternalErrorException(this, ex, null);
		} finally {
			workspace().doneWriting();
		}
	}

	/**
	 * Initialize the TDL task.
	 * 
	 * @throws NameDuplicationException Thrown if parameters cannot be set.
	 * @throws IllegalActionException Thrown if parameters cannot be set.
	 */
	private void _init() throws IllegalActionException,
			NameDuplicationException {
		SDFDirector director = new SDFDirector(this, "SDF director");
		director.iterations.setExpression("0");
		frequency = new Parameter(this, "frequency");
		frequency.setExpression("1");
		fast = new Parameter(this, "fast");
		fast.setExpression("false");
		slots = new Parameter(this, "slots");
		slots.setExpression("'1*'");
	}
}

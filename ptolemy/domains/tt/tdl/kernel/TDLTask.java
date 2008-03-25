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

	public TDLTask() throws IllegalActionException, NameDuplicationException {
		super();
		_init();
	}

	/**
	 * Construct a CompositeActor in the specified workspace with no container
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
	 * Create an actor with a name and a container. The container argument must
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

	public Parameter frequency;

	public Parameter fast;

	public Parameter slots;

	@Override
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

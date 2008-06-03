package ptolemy.domains.tt.tdl.kernel;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * A TDL task output port has some TDL specific parameters. - initialValue: a
 * TDL output port has an initial value.
 * 
 * @author Patricia Derler
 * 
 */
public class TDLTaskOutputPort extends TypedIOPort {

	/**
	 * Construct a TypedIOPort with no container and no name that is neither an
	 * input nor an output.
	 * 
	 * @throws NameDuplicationException
	 * @throws IllegalActionException
	 */
	public TDLTaskOutputPort() throws IllegalActionException,
			NameDuplicationException {
		super();
		_init();
	}

	/**
	 * Construct a port in the specified workspace with an empty string as a
	 * name. You can then change the name with setName(). If the workspace
	 * argument is null, then use the default workspace. The object is added to
	 * the workspace directory. Increment the version number of the workspace.
	 * 
	 * @param workspace
	 *            The workspace that will list the port.
	 * @throws NameDuplicationException Thrown if the initial value parameter cannot be created.
	 * @throws IllegalActionException Thrown if the initial value parameter cannot be created.
	 */
	public TDLTaskOutputPort(Workspace workspace)
			throws IllegalActionException, NameDuplicationException {
		super(workspace);
		_init();
	}

	/**
	 * Construct a TypedIOPort with a containing actor and a name that is
	 * neither an input nor an output. The specified container must implement
	 * the TypedActor interface, or an exception will be thrown.
	 * 
	 * @param container
	 *            The container actor.
	 * @param name
	 *            The name of the port.
	 * @exception IllegalActionException
	 *                If the port is not of an acceptable class for the
	 *                container, or if the container does not implement the
	 *                TypedActor interface.
	 * @exception NameDuplicationException
	 *                If the name coincides with a port already in the
	 *                container.
	 */
	public TDLTaskOutputPort(ComponentEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_init();
	}

	/**
	 * Construct a TypedIOPort with a container and a name that is either an
	 * input, an output, or both, depending on the third and fourth arguments.
	 * The specified container must implement the TypedActor interface or an
	 * exception will be thrown.
	 * 
	 * @param container
	 *            The container actor.
	 * @param name
	 *            The name of the port.
	 * @param isInput
	 *            True if this is to be an input port.
	 * @param isOutput
	 *            True if this is to be an output port.
	 * @exception IllegalActionException
	 *                If the port is not of an acceptable class for the
	 *                container, or if the container does not implement the
	 *                TypedActor interface.
	 * @exception NameDuplicationException
	 *                If the name coincides with a port already in the
	 *                container.
	 */
	public TDLTaskOutputPort(ComponentEntity container, String name,
			boolean isInput, boolean isOutput) throws IllegalActionException,
			NameDuplicationException {
		super(container, name, isInput, isOutput);
		_init();
	}

	/**
	 * The initial value of the task.
	 */
	public Parameter initialValue;

	/**
	 * Sets the port of a task to an input port.
	 * 
	 * @param isInput true if port is an input port.
	 * @throws IllegalActionException Thrown by parent class.
	 */
	public void setInput(boolean isInput) throws IllegalActionException {
		super.setInput(isInput);
		if (!isOutput()) {
			initialValue.setVisibility(Settable.NONE);
		}
	}

	/**
	 * Sets the port of a task to an output port.
	 * 
	 * @param isOutput true if port is an output port.
	 * @throws IllegalActionException Thrown by parent class.
	 */
	public void setOutput(boolean isOutput) throws IllegalActionException {
		super.setOutput(isOutput);
		initialValue.setVisibility(Settable.FULL);
	}

	/**
	 * Initialize the task, set an initial value parameter.
	 * @throws IllegalActionException Thrown if the initial value parameter cannot be created.
	 * @throws NameDuplicationException Thrown if the initial value parameter cannot be created.
	 */
	private void _init() throws IllegalActionException,
			NameDuplicationException {
		initialValue = new Parameter(this, "initialValue");
		initialValue.setExpression("0");
		initialValue.setVisibility(Settable.FULL);
	}

}

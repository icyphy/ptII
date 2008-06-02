package ptolemy.domains.tt.tdl.kernel;

import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * The TDL refinement port represents an acutator and has some TDL specific
 * parameters: - frequceny: update frequency - initialValue: at time 0, this
 * initial value is used - fast: if the actuator is connected to a fast task,
 * this parameter is true - slots: string that supports TDL slot selection.
 * 
 * @author Patricia Derler
 * 
 */
public class TDLRefinementPort extends RefinementPort {
	/**
	 * Construct a port in the given workspace.
	 * 
	 * @param workspace
	 *            The workspace.
	 * @exception IllegalActionException
	 *                If the port is not of an acceptable class for the
	 *                container, or if the container does not implement the
	 *                TypedActor interface.
	 * @throws NameDuplicationException
	 */
	public TDLRefinementPort(Workspace workspace)
			throws IllegalActionException, NameDuplicationException {
		super(workspace);
		_init();
	}

	/**
	 * Construct a port with a containing actor and a name that is neither an
	 * input nor an output. The specified container must implement the
	 * TypedActor interface, or an exception will be thrown.
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
	public TDLRefinementPort(ComponentEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_init();
	}

	/**
	 * frequency for the port update.
	 */
	public Parameter frequency;

	/**
	 * Initial value of the port.
	 */
	public Parameter initialValue;

	/**
	 * Describes a fast actuator.
	 */
	public Parameter fast;

	/**
	 * Slot selection string
	 */
	public Parameter slots;

	/**
	 * Make port to an output port, for TDL this means that it is an actuator.
	 * 
	 * @param isOutput True if port is an output port.
	 */
	public void setOutput(boolean isOutput) throws IllegalActionException {
		super.setOutput(isOutput);
		setMirrorDisable(false);
		frequency.setVisibility(Settable.FULL);
		initialValue.setVisibility(Settable.FULL);
		fast.setVisibility(Settable.FULL);
		slots.setVisibility(Settable.FULL);
	}

	/**
	 * Make port to an input port, for TDL this means that it is a sensor.
	 * 
	 * @param isInput True if port is an input port.
	 */
	public void setInput(boolean isInput) throws IllegalActionException {
		super.setInput(isInput);
		setMirrorDisable(false);
		if (!isOutput()) {
			frequency.setVisibility(Settable.NONE);
			initialValue.setVisibility(Settable.NONE);
			fast.setVisibility(Settable.NONE);
			slots.setVisibility(Settable.NONE);
		}
	}

	/**
	 * Initialize the port.
	 * @throws IllegalActionException Thrown if parameters cannot be set.
	 * @throws NameDuplicationException Thrown if parameters cannot be set.
	 */
	private void _init() throws IllegalActionException,
			NameDuplicationException {
		setMirrorDisable(false);
		frequency = new Parameter(this, "frequency");
		initialValue = new Parameter(this, "initialValue");
		fast = new Parameter(this, "fast");
		slots = new Parameter(this, "slots");
		frequency.setExpression("1");
		initialValue.setExpression("0");
		slots.setExpression("'1*'");
		fast.setExpression("false");
		frequency.setVisibility(Settable.FULL);
		initialValue.setVisibility(Settable.FULL);
		fast.setVisibility(Settable.FULL);
		slots.setVisibility(Settable.FULL);
	}

}

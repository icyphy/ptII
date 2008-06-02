package ptolemy.domains.tt.tdl.kernel;

import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * A TDL transition has some specific TDL parameters. - frequency: together with
 * the mode period, this value defines when this transition is tested.
 * 
 * @author Patricia Derler
 * 
 */
public class TDLTransition extends Transition {

	public TDLTransition(Workspace workspace) throws IllegalActionException,
			NameDuplicationException {
		super(workspace);
		_init();
	}

	/**
	 * Construct a transition with the given name contained by the specified
	 * entity. The container argument must not be null, or a
	 * NullPointerException will be thrown. This transition will use the
	 * workspace of the container for synchronization and version counts. If the
	 * name argument is null, then the name is set to the empty string.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of the transition.
	 * @exception IllegalActionException
	 *                If the container is incompatible with this transition.
	 * @exception NameDuplicationException
	 *                If the name coincides with any relation already in the
	 *                container.
	 */
	public TDLTransition(TDLActor container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_init();
	}

	/**
	 * The frequency of the transition.
	 */
	public Parameter frequency;

	/**
	 * Initialize the parameters of a transition.
	 * @throws IllegalActionException Thrown if frequency parameter cannot be created.
	 * @throws NameDuplicationException Thrown if The frequency parameter cannot be created.
	 */
	private void _init() throws
			NameDuplicationException, IllegalActionException {
		outputActions.setVisibility(Settable.NONE);
		setActions.setVisibility(Settable.NONE);
		reset.setVisibility(Settable.NONE);
		preemptive.setVisibility(Settable.NONE);
		defaultTransition.setVisibility(Settable.NONE);
		nondeterministic.setVisibility(Settable.NONE);
		refinementName.setVisibility(Settable.NONE);
		frequency = new Parameter(this, "frequency");
		frequency.setExpression("1");
	}

}

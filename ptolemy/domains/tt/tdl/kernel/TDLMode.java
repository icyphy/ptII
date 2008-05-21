package ptolemy.domains.tt.tdl.kernel;

import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A TDL mode is a collection of TDL tasks.
 * 
 * @author Patricia Derler
 * 
 */
public class TDLMode extends State {

	/**
	 * Construct a new TDL mode.
	 * 
	 * @param container
	 *            The container for the TDL mode.
	 * @param name
	 *            The name of the TDL mode.
	 * @throws NameDuplicationException
	 *             Thrown if the same name already exists.
	 * @throws IllegalActionException
	 *             Thrown if the mode cannot be created.
	 */
	public TDLMode(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

}

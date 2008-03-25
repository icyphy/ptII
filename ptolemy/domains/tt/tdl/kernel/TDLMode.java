package ptolemy.domains.tt.tdl.kernel;

import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A TDL mode is a collection of TDL tasks
 * 
 * @author Patricia Derler
 * 
 */
public class TDLMode extends State {

	public TDLMode(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// TDLRefinement refinement = new TDLRefinement(container, name);
		// StringAttribute attr = new StringAttribute();
		// attr.setExpression(name);
		// refinementName = attr;
	}

}

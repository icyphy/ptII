
// This class is for figure 10.4 of the graph.fm
import ptolemy.graph.*;
import ptolemy.kernel.util.*;


// A variable InequalityTerm with a String value.
class Variable implements InequalityTerm {
	// Construct a variable InequalityTerm with a null initial value.
		public Variable() {
	}

	// Return the String value of this term.
	public Object getValue() {
		return _value;
	}

	// Return an array containing this variable term.
	public InequalityTerm[] getVariables() {
		InequalityTerm[] variable = new InequalityTerm[1];
		variable[0] = this;
		return variable;
	}

	// Variable terms are settable.
	public boolean isSettable() {
		return true;
	}

	// Set the value of this variable to the specified String.
	// Not checking the type of the specified Object before casting for simplicity.
	public void setValue(Object e) throws IllegalActionException {
		_value = (String)e;
	}

	private String _value = null;
}

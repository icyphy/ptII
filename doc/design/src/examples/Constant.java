// This class is for figure 10.4 of the graph.fm
import ptolemy.graph.*;
import ptolemy.kernel.util.*;

// A constant InequalityTerm with a String Value.
class Constant implements InequalityTerm {

	// construct a constant term with the specified String value.
	public Constant(String value) {
		_value = value;
	}

	// Return the constant String value of this term.
	public Object getValue() {
		return _value;
	}

	// Constant terms do not contain any variable, so return an array of size zero.
	public InequalityTerm[] getVariables() {
		return new InequalityTerm[0];
	}

	// Constant terms are not settable.
	public boolean isSettable() {
		return false;
	}

	// Throw an Exception on an attempt to change this constant.
	public void setValue(Object e) throws IllegalActionException {
		throw new IllegalActionException("Constant.setValue: This term is a constant.");
	}

	// the String value of this term.
	private String _value = null;
}


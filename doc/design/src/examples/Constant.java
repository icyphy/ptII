// This class is for figure 10.4 of the graph.fm
import ptolemy.graph.*;
import ptolemy.kernel.util.*;

// A constant InequalityTerm with a String Value.
class Constant implements InequalityTerm {

	// Construct a constant term with the specified String value.
	public Constant(String value) {
		_value = value;
	}

    // Return the String associated with this term.
	public Object getAssociatedObject() {
		return _value;
	}

	// Return the constant String value of this term.
	public Object getValue() {
		return _value;
	}

	// Constant terms do not contain variables, so return an array of size zero.
	public InequalityTerm[] getVariables() {
		return new InequalityTerm[0];
	}

    // Initialize the value of this term to the specified CPO element.
    public void initialize(Object object) throws IllegalActionException {
        setValue(object);
    }

	// Constant terms are not settable.
	public boolean isSettable() {
		return false;
	}

    // Check whether the current value of this term is acceptable.
    public boolean isValueAcceptable() {
        return true;  // Any string value is acceptable.
    }

	// Throw an Exception on an attempt to change this constant.
	public void setValue(Object e) throws IllegalActionException {
		throw new IllegalActionException("This term is a constant.");
	}

	// the String value of this term.
	private String _value = null;
}


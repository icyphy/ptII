package ptolemy.caltrop.util;

import caltrop.interpreter.Function;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

import java.util.List;

/**
 * This class is an adapter for {@link caltrop.interpreter.Function Function} objects that provides the Ptolemy II
 * <tt>FunctionToken.Function</tt>
 * interface. It allows them to be seamlessly used with Ptolemy II-generated function objects.
 *
 * @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 * @see caltrop.interpreter.Context
 * @see caltrop.interpreter.Function
 */

public class PtCalFunction implements FunctionToken.Function {

    public Token apply(List list) throws IllegalActionException {
        // TODO: should we allow non-token returns and "tokefy" them?
        return (Token) f.apply(list.toArray());
    }

    public int getNumberOfArguments() {
        return f.arity();
    }

    public boolean isCongruent(FunctionToken.Function function) {
        return false;
    }

    public PtCalFunction(Function f) {
        this.f = f;
    }

    private Function f;
}

package ptolemy.matlab.impl.engine.adaption;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author david
 *
 */
public interface ToMatlabObjectAdapter <T extends Token> {
	public MatlabObject adapt(final T token, final String tokenName) throws IllegalActionException;
}

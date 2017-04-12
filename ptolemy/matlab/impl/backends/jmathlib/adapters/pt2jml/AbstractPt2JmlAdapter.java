package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import ptolemy.data.Token;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public abstract class AbstractPt2JmlAdapter<T extends Token>
implements ToMatlabObjectAdapter<T> {

	protected final ToMatlabObjectAdapter<Token> delegate;

	public AbstractPt2JmlAdapter(final ToMatlabObjectAdapter<Token> delegate) {
		this.delegate = delegate;
	}
	
}

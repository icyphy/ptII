package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.CharToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlStringAdapter extends AbstractPt2JmlAdapter<StringToken> {
	/**
	 * @param source
	 * @param target
	 * @param delegate
	 */
	public Pt2JmlStringAdapter(final ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter#adapt(ptolemy.data.Token)
	 */
	@Override
	public MatlabObject adapt(StringToken ptToken,final String ptTokenName) {
		final CharToken jmlString = new CharToken(ptToken.stringValue());
		return new JMLObject(jmlString,ptTokenName);
	}

}

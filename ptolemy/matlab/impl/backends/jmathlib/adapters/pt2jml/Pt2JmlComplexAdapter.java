package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlComplexAdapter extends AbstractPt2JmlAdapter<ComplexToken> {

	public Pt2JmlComplexAdapter(ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter#adapt(ptolemy.data.Token)
	 */
	@Override
	public MatlabObject adapt(ComplexToken ptToken,final String ptTokenName) {
		final DoubleNumberToken jmlToken = new DoubleNumberToken(ptToken.complexValue().real,ptToken.complexValue().imag);
		return new JMLObject(jmlToken,ptTokenName);
	}

}

package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlDoubleMatrixAdapter extends AbstractPt2JmlAdapter<DoubleMatrixToken> {

	public Pt2JmlDoubleMatrixAdapter(ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	@Override
	public MatlabObject adapt(final DoubleMatrixToken ptToken,final String ptTokenName) {
		final DoubleNumberToken jmlToken = new DoubleNumberToken(ptToken.doubleMatrix());
		return new JMLObject(jmlToken,ptTokenName);
	}

}

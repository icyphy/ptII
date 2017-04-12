package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlScalarAdapter extends AbstractPt2JmlAdapter<ScalarToken> {

	public Pt2JmlScalarAdapter(ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	/**
	 * @throws IllegalActionException 
	 * @see ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter#adapt(ptolemy.data.Token)
	 */
	@Override
	public MatlabObject adapt(ScalarToken ptToken,final String ptTokenName) throws IllegalActionException {

		double ptTokenValue = 0.0;
		
		if (ptToken instanceof BooleanToken) {
			ptTokenValue = ((BooleanToken) ptToken).booleanValue() ? 1.0 : 0.0;
		} else if (ptToken instanceof DoubleToken) {
			ptTokenValue = ptToken.doubleValue();
		} else if (ptToken instanceof IntToken) {
			ptTokenValue = ptToken.intValue();
		} else {
			throw new IllegalActionException(" Token "
							+ ptToken
							+ " is of type "
							+ ptToken.getType()
							+ ", but it should be one of: "
							+ "ArrayToken, RecordToken, StringToken, ComplexMatrixToken, "
							+ "MatrixToken, ComplexToken, BooleanToken, DoubleToken or IntToken.");
		}
		
		return new JMLObject(new DoubleNumberToken(ptTokenValue),ptTokenName);

	}

}

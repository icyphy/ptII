package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.CharToken;
import jmathlib.core.tokens.MatrixToken;
import jmathlib.core.tokens.OperandToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlStringArrayAdapter extends AbstractPt2JmlAdapter<ArrayToken> {

	public Pt2JmlStringArrayAdapter(ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	@Override
	public MatlabObject adapt(ArrayToken ptToken,final String ptTokenName) {

		final Token[] ptTokenElements = ptToken.arrayValue();
		final OperandToken[][] matlabTokenArray = new OperandToken[1][ptTokenElements.length];
		for (int j = 0; j < ptTokenElements.length; j++) {
			final StringToken ptStringToken = (StringToken) ptTokenElements[j];
			matlabTokenArray[0][j] = new CharToken(ptStringToken.stringValue());
		}

		return new JMLObject(new MatrixToken(matlabTokenArray),ptTokenName);

	}

}

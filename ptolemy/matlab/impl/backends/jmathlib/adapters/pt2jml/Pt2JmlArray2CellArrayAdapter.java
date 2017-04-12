package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.CellArrayToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;
import ptolemy.matlab.impl.engine.impl.ObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlArray2CellArrayAdapter extends AbstractPt2JmlAdapter<ArrayToken> {

	public Pt2JmlArray2CellArrayAdapter(final ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	@Override
	public MatlabObject adapt(ArrayToken ptToken, String ptTokenName) throws IllegalActionException {
		
		final CellArrayToken jmlToken = new CellArrayToken();		
		jmlToken.setSize(ptToken.length(),1);

		final Token[] ptTokenElements = ptToken.arrayValue();
		for (int n = 0; n < ptTokenElements.length; n++) {
			final JMLObject cellJmlObject = (JMLObject) delegate.adapt(ptTokenElements[n],ObjectAdapter.ToolBox.buildIndexString(n));
			jmlToken.setElement(n, cellJmlObject.getJmlToken());
		}

		return new JMLObject(jmlToken,ptTokenName);

	}

}

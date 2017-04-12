package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.MathLibObject;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlStructAdapter extends AbstractPt2JmlAdapter<RecordToken> {

	public Pt2JmlStructAdapter(final ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	/**
	 * @throws IllegalActionException 
	 * @see ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter#adapt(ptolemy.data.Token)
	 */
	@Override
	public MatlabObject adapt(RecordToken ptToken,final String ptTokenName) throws IllegalActionException {
		
		final MathLibObject jmlToken = new MathLibObject();

		final Object[] ptTokenFieldNames = ptToken.labelSet().toArray();
		for (final Object ptFieldName : ptTokenFieldNames) {
			final Token ptFieldToken = ptToken.get((String) ptFieldName);
			final JMLObject  fieldJmlObject = (JMLObject) delegate.adapt(ptFieldToken, (String)ptFieldName);
			jmlToken.setField((String)ptFieldName, fieldJmlObject.getJmlToken());
		}

		return new JMLObject(jmlToken,ptTokenName);
	}

}

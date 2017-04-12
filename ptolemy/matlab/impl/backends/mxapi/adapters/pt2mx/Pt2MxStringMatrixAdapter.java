package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2MxStringMatrixAdapter extends AbstractPt2MxObjectAdapter<ArrayToken> {

	public Pt2MxStringMatrixAdapter(final ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(ArrayToken t, String name) throws IllegalActionException {
		final Token[] ta = t.arrayValue();
		String s = ((StringToken) ta[0]).stringValue();
		long ma = ptMatlab.ptmatlabCreateString(name, s, ta.length, s.length());
		for (int n = 1; n < ta.length; n++) {
			s = ((StringToken) ta[n]).stringValue();
			ptMatlab.ptmatlabSetString(name, ma, n, s, s.length());
		}
		return new MxObject(ptMatlab,ma,name);
	}

}

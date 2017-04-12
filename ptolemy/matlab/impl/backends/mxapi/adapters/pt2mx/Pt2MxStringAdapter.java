package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

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
public class Pt2MxStringAdapter extends AbstractPt2MxObjectAdapter<StringToken> {

	public Pt2MxStringAdapter(ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(StringToken token, String tokenName) throws IllegalActionException {
		final String s = token.stringValue();
		final long mxArray = ptMatlab.ptmatlabCreateString(tokenName, s, 1, s.length());
		return new MxObject(ptMatlab,mxArray,tokenName);
	}

}

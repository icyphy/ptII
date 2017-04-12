package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.ComplexToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2MxComplexAdapter extends AbstractPt2MxObjectAdapter<ComplexToken> {

	public Pt2MxComplexAdapter(ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(ComplexToken token, String tokenName) throws IllegalActionException {
		final Complex[] a = { token.complexValue() };
		final long mxArray = ptMatlab.ptmatlabCreateComplexMatrixOneDim(tokenName, a, a.length);
		return new MxObject(ptMatlab,mxArray,tokenName);
	}

}

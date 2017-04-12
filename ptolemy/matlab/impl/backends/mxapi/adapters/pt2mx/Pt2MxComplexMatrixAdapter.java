package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.ComplexMatrixToken;
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
public class Pt2MxComplexMatrixAdapter extends AbstractPt2MxObjectAdapter<ComplexMatrixToken> {

	public Pt2MxComplexMatrixAdapter(ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(ComplexMatrixToken token, String tokenName) throws IllegalActionException {
		final Complex[][] a = token.complexMatrix();
		final long mxArray = ptMatlab.ptmatlabCreateComplexMatrix(tokenName, a, a.length, a[0].length);
		return new MxObject(ptMatlab,mxArray,tokenName);
	}

}

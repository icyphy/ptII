package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.DoubleMatrixToken;
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
public class Pt2MxDoubleMatrixAdapter extends AbstractPt2MxObjectAdapter<DoubleMatrixToken> {

	public Pt2MxDoubleMatrixAdapter(ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(DoubleMatrixToken token, String tokenName) throws IllegalActionException {
		final double[][] a =  token.doubleMatrix();
		final long mxArray = ptMatlab.ptmatlabCreateDoubleMatrix(tokenName, a, a.length, a[0].length);
		return new MxObject(ptMatlab,mxArray,tokenName);
	}

}

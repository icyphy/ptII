package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
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
public class Pt2MxDoubleAdapter extends AbstractPt2MxObjectAdapter<ScalarToken> {

	public Pt2MxDoubleAdapter(final ToMatlabObjectAdapter<Token> delegate,
			final PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(ScalarToken token, String tokenName) throws IllegalActionException {

		double[] a = new double[1];
		if (token instanceof BooleanToken) {
			a[0] = ((BooleanToken) token).booleanValue() ? 1.0 : 0.0;
		} else if (token instanceof DoubleToken) {
			a[0] = token.doubleValue();
		} else if (token instanceof IntToken) {
			a[0] = token.intValue();
		} else {
			throw new IllegalActionException(
					"Token "
							+ token
							+ " is of type "
							+ token.getType()
							+ ", it should be one of "
							+ "ArrayToken, RecordToken, StringToken, ComplexMatrixToken, "
							+ "MatrixToken, ComplexToken, BooleanToken, DoubleToken or IntToken.");
		}

		final long mxArray = ptMatlab.ptmatlabCreateDoubleMatrixOneDim(tokenName, a, 1);
		return new MxObject(ptMatlab,mxArray,tokenName);

	}

}

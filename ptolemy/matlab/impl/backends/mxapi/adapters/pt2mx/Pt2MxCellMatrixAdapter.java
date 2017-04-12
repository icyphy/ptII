package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.ArrayToken;
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
public class Pt2MxCellMatrixAdapter extends AbstractPt2MxObjectAdapter<ArrayToken> {

	public Pt2MxCellMatrixAdapter(final ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(ArrayToken t, String name) throws IllegalActionException {
		final Token[] ta = t.arrayValue();
		long ma = ptMatlab.ptmatlabCreateCellMatrix(name, 1, ta.length);
		if (ma == 0) {
			throw new IllegalActionException("couldn't create cell "
					+ "array " + name);
		}
		for (int n = 0; n < ta.length; n++) {
			final MxObject mxFma = (MxObject) delegate.adapt(ta[n],"(" + n + ")");
			if (mxFma.getMxArray() == 0) {
				throw new IllegalActionException("couldn't create array for index " + n
						+ " in cell array " + name);
			}
			ptMatlab.ptmatlabSetCell(name, ma, 0, n, mxFma.getMxArray());
		}
		return new MxObject(ptMatlab,ma,name);

	}

}

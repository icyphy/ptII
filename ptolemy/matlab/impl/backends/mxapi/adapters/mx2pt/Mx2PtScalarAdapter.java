package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Mx2PtScalarAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<ScalarToken> {

	public Mx2PtScalarAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public ScalarToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		//TODO: these if's should be at the backend-agnostic level. They are part of the most general contract.
		if (matlabType.isConvertToInt()) {
			return adaptToInt(matlabObject);
		}
		return adaptToDouble(matlabObject);
	}
	
	public IntToken adaptToInt(final MatlabObject matlabObject) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		double[][] a = ptMatlab.ptmatlabGetDoubleMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException("can't get double matrix from matlab engine.");
		}
		return new IntToken((int) a[0][0]);
	}
	
	public DoubleToken adaptToDouble(final MatlabObject matlabObject) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		double[][] a = ptMatlab.ptmatlabGetDoubleMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get double matrix from matlab engine.");
		}
		return new DoubleToken(a[0][0]);
	}

}

package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;
import ptolemy.matlab.impl.utils.IntToolbox;
import ptolemy.matlab.impl.utils.SimpleIntToolbox;

/**
 * @author david
 *
 */
public class Mx2PtScalarMatrixAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<MatrixToken> {

	private IntToolbox dataUtils = new SimpleIntToolbox();
	
	public Mx2PtScalarMatrixAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public MatrixToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		throw new RuntimeException("not implemented");
	}
	
	public IntMatrixToken adaptToIntMatrix(final MatlabObject matlabObject) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		double[][] a = ptMatlab.ptmatlabGetDoubleMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get double matrix from matlab engine.");
		}
		int[][] tmp = dataUtils.convertDoubleToInt(a);
		return new IntMatrixToken(tmp);
	}
	
	public DoubleMatrixToken adaptToDoubleMatrix(final MatlabObject matlabObject) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		double[][] a = ptMatlab.ptmatlabGetDoubleMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get double matrix from matlab engine.");
		}
		return new DoubleMatrixToken(a);
	}

}

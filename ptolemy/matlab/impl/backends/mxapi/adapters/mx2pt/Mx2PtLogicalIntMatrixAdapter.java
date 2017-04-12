package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.IntMatrixToken;
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
public class Mx2PtLogicalIntMatrixAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<IntMatrixToken> {

	public Mx2PtLogicalIntMatrixAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public IntMatrixToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		int[][] a = ptMatlab.ptmatlabGetLogicalMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get logical matrix from matlab engine.");
		}
		return new IntMatrixToken(a);	}
}

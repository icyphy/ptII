package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.IntToken;
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
public class Mx2PtLogicalIntAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<IntToken> {

	public Mx2PtLogicalIntAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public IntToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		int[][] a = ptMatlab.ptmatlabGetLogicalMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get logical matrix from matlab engine.");
		}
		return new IntToken(a[0][0]);
	}

}

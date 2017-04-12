package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.ComplexMatrixToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Mx2PtComplexMatrixAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<ComplexMatrixToken> {

	public Mx2PtComplexMatrixAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public ComplexMatrixToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		Complex[][] a = ptMatlab.ptmatlabGetComplexMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get complex matrix from matlab engine.");
		}
		return new ComplexMatrixToken(a);
	}

}

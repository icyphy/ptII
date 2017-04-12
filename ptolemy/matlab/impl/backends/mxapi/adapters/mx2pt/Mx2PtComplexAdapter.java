package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.ComplexToken;
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
public class Mx2PtComplexAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<ComplexToken> {

	public Mx2PtComplexAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public ComplexToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		Complex[][] a = ptMatlab.ptmatlabGetComplexMatrix(mxArray, nRows, nCols);
		if (a == null) {
			throw new IllegalActionException(
					"can't get complex matrix from matlab engine.");
		}

		return new ComplexToken(a[0][0]);	}

}

package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
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
public class Mx2PtStringArrayAdapter extends AbstractMx2PtAdapter 
	implements ToPtObjectAdapter<ArrayToken> {

	public Mx2PtStringArrayAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public ArrayToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		
		final MxObject mxObject = (MxObject) matlabObject;
		final int nRows = mxObject.getnRows();
		final long mxArray = mxObject.getMxArray();

		final Token[] stringTokensArray = new Token[nRows];
		for (int n = 0; n < nRows; n++) {
			stringTokensArray[n] = new StringToken(ptMatlab.ptmatlabGetString(mxArray, n));
		}

		return new ArrayToken(stringTokensArray);
		
	}

}

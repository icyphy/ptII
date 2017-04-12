package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.StringToken;
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
public class Mx2PtStringAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<StringToken> {

	public Mx2PtStringAdapter(PtMatlab ptMatlab) {
		super(ptMatlab);
	}

	@Override
	public StringToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		final MxObject mxObject = (MxObject) matlabObject;
		final long mxArray = mxObject.getMxArray();
		return new StringToken(ptMatlab.ptmatlabGetString(mxArray, 0));
	}

}

package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.RecordToken;
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
public class Pt2MxStructAdapter extends AbstractPt2MxObjectAdapter<RecordToken> {

	public Pt2MxStructAdapter(ToMatlabObjectAdapter<Token> delegate, PtMatlab ptMatlab) {
		super(delegate, ptMatlab);
	}

	@Override
	public MatlabObject adapt(RecordToken token, String name) throws IllegalActionException {
		final Object[] fieldNames = token.labelSet().toArray();
		long mxStruct = ptMatlab.ptmatlabCreateStructMatrix(name, fieldNames, 1, 1);
		if (mxStruct == 0) {
			throw new IllegalActionException("couldn't create struct array " + name);
		}
		for (Object fieldName : fieldNames) {
			final Token f = token.get((String) fieldName);
			final MxObject mxFma = (MxObject) delegate.adapt(f,(String) fieldName);
			if (mxFma.getMxArray() == 0) {
				throw new IllegalActionException("couldn't create array for field " + fieldName + " in struct " + name);
			}
			ptMatlab.ptmatlabSetStructField(name, mxStruct, (String) fieldName, 0, 0, mxFma.getMxArray());
		}
		return new MxObject(ptMatlab,mxStruct,name);

	}

}

package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Jml2PtScalarAdapter implements ToPtObjectAdapter<ScalarToken> {

	@Override
	public ScalarToken adapt(MatlabObject matlabObject, final MatlabObjectInfo matlabType)
			throws IllegalActionException {
		if (matlabType.isConvertToInt()) {
			return adaptToInt(matlabObject);
		}
		return adaptToDouble(matlabObject);
	}
	
	private static IntToken adaptToInt(MatlabObject matlabObject) {
		final JMLObject jmlObject = (JMLObject) matlabObject;
		final DoubleNumberToken jmlToken = (DoubleNumberToken) jmlObject.getJmlToken();
		return new IntToken((int) jmlToken.getValueRe());
	}

	private static DoubleToken adaptToDouble(MatlabObject matlabObject) {
		final JMLObject jmlObject = (JMLObject) matlabObject;
		final DoubleNumberToken jmlToken = (DoubleNumberToken) jmlObject.getJmlToken();
		return new DoubleToken(jmlToken.getValueRe());
	}

}

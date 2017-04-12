package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.ComplexToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Jml2PtComplexAdapter implements ToPtObjectAdapter<ComplexToken> {

	@Override
	public ComplexToken adapt(MatlabObject matlabObject,final MatlabObjectInfo matlabType)
			throws IllegalActionException {
		final JMLObject jmlObject = (JMLObject) matlabObject;
		final DoubleNumberToken jmlComplexToken = (DoubleNumberToken) jmlObject.getJmlToken();
		final Complex jmlComplex = new Complex(jmlComplexToken.getValueRe(),jmlComplexToken.getValueIm());
		return new ComplexToken(jmlComplex);
	}

}

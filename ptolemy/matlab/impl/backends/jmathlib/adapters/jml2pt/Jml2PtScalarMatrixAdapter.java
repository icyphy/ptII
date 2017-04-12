package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;
import ptolemy.matlab.impl.utils.IntToolbox;
import ptolemy.matlab.impl.utils.SimpleIntToolbox;

/**
 * @author david
 *
 */
public class Jml2PtScalarMatrixAdapter implements ToPtObjectAdapter<MatrixToken> {

	private IntToolbox dataUtils = new SimpleIntToolbox();

	@Override
	public MatrixToken adapt(MatlabObject matlabObject,final MatlabObjectInfo matlabType)
			throws IllegalActionException {

		if (matlabType.isConvertToInt()) {
			return adaptToIntMatrix(matlabObject);
		}
		return adaptToDoubleMatrix(matlabObject);
		
	}
	
	private IntMatrixToken adaptToIntMatrix(MatlabObject matlabObject) throws IllegalActionException {
		final JMLObject jmlObject = (JMLObject) matlabObject;
		final DoubleNumberToken jmlToken = (DoubleNumberToken) jmlObject.getJmlToken();
		final int[][] intValues = dataUtils.convertDoubleToInt(jmlToken.getReValues());
		return new IntMatrixToken(intValues);
	}

	private static DoubleMatrixToken adaptToDoubleMatrix(MatlabObject matlabObject) throws IllegalActionException {
		final JMLObject jmlObject = (JMLObject) matlabObject;
		final DoubleNumberToken jmlToken = (DoubleNumberToken) jmlObject.getJmlToken();
		return new DoubleMatrixToken(jmlToken.getReValues());
	}
}

package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.ComplexMatrixToken;
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
public class Jml2PtComplexMatrixAdapter implements ToPtObjectAdapter<ComplexMatrixToken> {

	@Override
	public ComplexMatrixToken adapt(MatlabObject matlabObject,final MatlabObjectInfo matlabType)
			throws IllegalActionException {

		final JMLObject jmlObject = (JMLObject) matlabObject;

		final DoubleNumberToken jmlToken = (DoubleNumberToken) jmlObject.getJmlToken();
		final int nCols = jmlObject.getnCols();
		final int nRows = jmlObject.getnRows();
		final Complex[][] complexValues = new Complex[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				final DoubleNumberToken jmlComplex = (DoubleNumberToken) jmlToken.getElement(i, j);
				complexValues[i][j] = new Complex(jmlComplex.getValueRe(),jmlComplex.getValueIm());
			}
		}

		return new ComplexMatrixToken(complexValues);
		
	}

}

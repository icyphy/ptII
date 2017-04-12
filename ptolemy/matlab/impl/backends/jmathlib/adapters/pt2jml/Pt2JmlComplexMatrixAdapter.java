package ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml;

import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.Token;
import ptolemy.math.Complex;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public class Pt2JmlComplexMatrixAdapter extends AbstractPt2JmlAdapter<ComplexMatrixToken> {

	public Pt2JmlComplexMatrixAdapter(final ToMatlabObjectAdapter<Token> delegate) {
		super(delegate);
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter#adapt(ptolemy.data.Token)
	 */
	@Override
	public MatlabObject adapt(ComplexMatrixToken ptToken,final String ptTokenName) {
		
		int ptTokenRows = ptToken.getRowCount();
		int ptTokenCols = ptToken.getColumnCount();

		double[][] jmlTokenReals = new double[ptTokenRows][ptTokenCols];
		double[][] jmlTokenIms = new double[ptTokenRows][ptTokenCols];
		for (int i = 0; i < ptTokenRows; i++) {
			for (int j = 0; j < ptTokenCols; j++) {
				final Complex ptComplex = ptToken.getElementAt(i, j);
				jmlTokenReals[i][j] = ptComplex.real;
				jmlTokenIms[i][j] = ptComplex.imag;
			}
		}

		final DoubleNumberToken jmlComplexToken = new DoubleNumberToken(jmlTokenReals, jmlTokenIms);
		return new JMLObject(jmlComplexToken,ptTokenName);

	}

}

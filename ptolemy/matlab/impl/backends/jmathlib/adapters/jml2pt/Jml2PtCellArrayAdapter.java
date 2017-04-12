package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.CellArrayToken;
import jmathlib.core.tokens.DataToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;
import ptolemy.matlab.impl.engine.impl.ObjectAdapter;

/**
 * @author David Guardado barcia
 *
 */
public class Jml2PtCellArrayAdapter extends AbstractJmlToPtAdapter implements ToPtObjectAdapter<Token> {

	public Jml2PtCellArrayAdapter(final ToPtObjectAdapter<Token> fallbackDelegate) {
		super(fallbackDelegate);
	}

	@Override
	public Token adapt(MatlabObject matlabObject,final MatlabObjectInfo matlabType)
			throws IllegalActionException {

		final JMLObject jmlObject = (JMLObject) matlabObject;

		final int nCols = jmlObject.getnCols();
		final int nRows = jmlObject.getnRows();
		final CellArrayToken jmlCellArray = (CellArrayToken) jmlObject.getJmlToken();
		
		return adaptJmlCellArray(nCols, nRows, jmlCellArray);

	}

	private Token adaptJmlCellArray(final int nCols, final int nRows, final CellArrayToken jmlCellArray)
			throws IllegalActionException {
		
		final Token[] ptColumnTokens = new Token[nCols];
		final Token[] ptRowTokens = new Token[nRows];

		for (int n = 0; n < nRows; n++) {
			boolean anyIntegers = false;
			boolean anyDoubles = false;
			for (int m = 0; m < nCols; m++) {
				final DataToken jmlCellToken = (DataToken) jmlCellArray.getElement(n, m);
				final JMLObject jmlCellObject = new JMLObject(jmlCellToken,ObjectAdapter.ToolBox.buildIndexString(n,m));
				ptColumnTokens[m] = fallbackDelegate.adapt(jmlCellObject,null);
				if (ptColumnTokens[m] instanceof IntToken) {
					anyIntegers = true;
				} else if (ptColumnTokens[m] instanceof DoubleToken) {
					anyDoubles = true;
				}
			}
			if (anyIntegers && anyDoubles) {
				for (int m = 0; m < ptColumnTokens.length; m++) {
					if (ptColumnTokens[m] instanceof IntToken) {
						ptColumnTokens[m] = DoubleToken.convert(ptColumnTokens[m]);
					}
				}
			}
			if (ptColumnTokens.length > 1) {
				ptRowTokens[n] = new ArrayToken(ptColumnTokens);
			} else {
				ptRowTokens[n] = ptColumnTokens[0];
			}
		}

		if (nRows == 1) {
			return ptRowTokens[0];
		}
		
		return new ArrayToken(ptRowTokens);
	}

}

package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;
import ptolemy.matlab.impl.engine.impl.ObjectAdapter;

/**
 * @author david
 *
 */
public class Mx2PtCellArrayAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<Token> {

	private final ToPtObjectAdapter<Token> fallbackDelegate;

	public Mx2PtCellArrayAdapter(PtMatlab ptMatlab,final ToPtObjectAdapter<Token> delegate) {
		super(ptMatlab);
		this.fallbackDelegate = delegate;
	}

	@Override
	public Token adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		
		return adaptMxCellArray(nCols, nRows, mxArray);
		
	}

	private Token adaptMxCellArray(final int nCols, final int nRows, long mxArray) throws IllegalActionException {
		
		Token[] columnTokens = new Token[nCols];
		Token[] rowTokens = new Token[nRows];

		for (int n = 0; n < nRows; n++) {
			boolean anyIntegers = false;
			boolean anyDoubles = false;
			for (int m = 0; m < nCols; m++) {
				final long cell = ptMatlab.ptmatlabGetCell(mxArray, n, m);
				if (cell != 0) {
					columnTokens[m] = adaptCell(n, m, cell);
					if (columnTokens[m] instanceof IntToken) {
						anyIntegers = true;
					} else if (columnTokens[m] instanceof DoubleToken) {
						anyDoubles = true;
					}
				}
			}
			if (anyIntegers && anyDoubles) {
				convertToDouble(columnTokens);
			}
			rowTokens[n] = new ArrayToken(columnTokens);
		}

		if (nRows == 1) {
			return  rowTokens[0];
		}
		
		return new ArrayToken(rowTokens);
		
	}

	private Token adaptCell(int n, int m, final long cell) throws IllegalActionException {
		final String cellName = ObjectAdapter.ToolBox.buildIndexString(n, m);
		final MatlabObject cellObject = new MxObject(ptMatlab,cell,cellName);
		return fallbackDelegate.adapt(cellObject,null);
	}

	private static void convertToDouble(Token[] columnTokens) throws IllegalActionException {
		for (int m = 0; m < columnTokens.length; m++) {
			if (columnTokens[m] instanceof IntToken) {
				columnTokens[m] = DoubleToken.convert(columnTokens[m]);
			}
		}
	}

}

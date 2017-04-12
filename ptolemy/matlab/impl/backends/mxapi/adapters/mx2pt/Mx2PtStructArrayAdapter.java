package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * 
 * @author david
 *
 */
public class Mx2PtStructArrayAdapter extends AbstractMx2PtAdapter implements ToPtObjectAdapter<Token> {

	private final ToPtObjectAdapter<Token> delegate;

	public Mx2PtStructArrayAdapter(PtMatlab ptMatlab,final ToPtObjectAdapter<Token> delegate) {
		super(ptMatlab);
		this.delegate = delegate;
	}

	/**
	 * Adapts a 'struct' Matlab object into a PtolemyII {@link RecordToken} or {@link ArrayToken} object.
	 * 
	 * @param matlabObject an {@link MxObject} object of type 'struct'
	 * @param matlabType a Matlab object infoset associated to the object
	 * @return a {@link RecordToken} instance, if mxArray dimension 1x1; an {@link ArrayToken} 
	 * instance of ArrayTokens of RecordTokens {{RecordToken,...}, {...}} ("two-dimensional" ArrayToken) otherwise 
	 */
	@Override
	public Token  adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType) throws IllegalActionException {
		
		final MxObject mxObject = (MxObject) matlabObject;
		final int nCols = mxObject.getnCols();
		final int nRows = mxObject.getnRows();
		long mxArray = mxObject.getMxArray();
		int nfields = ptMatlab.ptmatlabGetNumberOfFields(mxArray);
		Token[] columnTokens = new Token[nCols];
		Token[] rowTokens = new Token[nRows];
		String[] fieldNames = new String[nfields];

		for (int k = 0; k < nfields; k++) {
			fieldNames[k] = ptMatlab.ptmatlabGetFieldNameByNumber(mxArray, k);
		}

		Token[] fieldValueTokens = new Token[nfields];

		for (int n = 0; n < nRows; n++) {
			for (int m = 0; m < nCols; m++) {
				for (int k = 0; k < nfields; k++) {
					long mxKField = ptMatlab.ptmatlabGetFieldByNumber(mxArray, k, n, m);
					if (mxKField != 0) {
						final MatlabObject kFieldMatlabObject = new MxObject(ptMatlab,mxKField,fieldNames[k]);
						fieldValueTokens[k] = delegate.adapt(kFieldMatlabObject, null);
					} else {
						throw new IllegalActionException("can't get field "
								+ fieldNames[k] + " from matlab "
								+ " struct " + nRows + "x" + nCols);
					}
				}
				columnTokens[m] = new RecordToken(fieldNames, fieldValueTokens);
			}
			rowTokens[n] = new ArrayToken(columnTokens);
		}
		boolean scalarStructs = nCols == 1 && nRows == 1;
		if (scalarStructs) {
			return ((ArrayToken) rowTokens[0]).getElement(0);
		}
		return  new ArrayToken(rowTokens);

	}

}

package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jmathlib.core.interpreter.Variable;
import jmathlib.core.tokens.DataToken;
import jmathlib.core.tokens.MathLibObject;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Jml2PtMathLibObjectAdapter extends AbstractJmlToPtAdapter implements ToPtObjectAdapter<Token> {

	public Jml2PtMathLibObjectAdapter(ToPtObjectAdapter<Token> fallbackDelegate) {
		super(fallbackDelegate);
	}

	/**
	 * Adapts a 'struct' Matlab object into a PtolemyII {@link RecordToken} or {@link ArrayToken} object.
	 * 
	 * @param matlabObject an {@link JMLObject} object of type 'struct', i.e one returning an instance of {@link MathLibObject} when 
	 *  {@link JMLObject#getJmlToken()} is called
	 * @param matlabType a Matlab object infoset associated to the object
	 * @return a {@link RecordToken} instance, if mxArray dimension 1x1; an {@link ArrayToken} 
	 * instance of ArrayTokens of RecordTokens {{RecordToken,...}, {...}} ("two-dimensional" ArrayToken) otherwise 
	 */
	@Override
	public Token adapt(final MatlabObject matlabObject,final MatlabObjectInfo matlabType)
			throws IllegalActionException {
		
		final JMLObject jmlObject = (JMLObject) matlabObject;
		int nCols = jmlObject.getnCols() == 0? 1 : jmlObject.getnRows();
		int nRows = jmlObject.getnRows() == 0? 1 : jmlObject.getnRows();
		final MathLibObject jmlRecord = (MathLibObject) jmlObject.getJmlToken();
		final int nfields = jmlRecord.getFieldsHash().size();
		
		return adaptRecord(nCols, nRows, jmlRecord, nfields);
	}

	private Token adaptRecord(final int nCols, final int nRows, final MathLibObject jmlRecordToken, final int nfields)
			throws IllegalActionException {
		
		final Token[] columnTokens = new Token[nCols];
		final Token[] rowTokens = new Token[nRows];
		final String[] ptStructFieldNames = new String[nfields];
		final Token[] ptStructFields = new Token[nfields];

		@SuppressWarnings("rawtypes")
		Iterator jmlRecordIterator = jmlRecordToken.getFields();
		for (int n = 0; n < nRows; n++) {
			for (int m = 0; m < nCols; m++) {
				for (int k = 0; k < nfields; k++) {
					@SuppressWarnings("unchecked")
					final Map.Entry<String,Variable> kField = (Entry<String, Variable>) jmlRecordIterator.next();
					ptStructFields[k] = adaptRecordField(kField);
					ptStructFieldNames[k] = kField.getKey();
				}
				columnTokens[m] = new RecordToken(ptStructFieldNames, ptStructFields);
			}
			rowTokens[n] = new ArrayToken(columnTokens);
		}
		boolean scalarStructs = nCols == 1 && nRows == 1;
		if (scalarStructs) {
			return ((ArrayToken) rowTokens[0]).getElement(0);
		}
		return  new ArrayToken(rowTokens);
	}

	private Token adaptRecordField(final Map.Entry<String, Variable> kField)
			throws IllegalActionException {
		final DataToken kFieldValue = (DataToken) kField.getValue().getData();
		final String kFieldName = kField.getKey();
		final MatlabObject ptStructField = new JMLObject(kFieldValue,kFieldName);
		 return this.fallbackDelegate.adapt(ptStructField, null);
	}

}

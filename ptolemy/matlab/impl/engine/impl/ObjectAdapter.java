package ptolemy.matlab.impl.engine.impl;

import ptolemy.data.ArrayToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.engine.MatlabEngine;
import ptolemy.matlab.impl.engine.MatlabEngine.ConversionParameters;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * Converts between PtolemyII types/object (from now on referred to as "Token") and Matlab types/objects.
 * <p>Conversions are done according to the following tables.</p>
 <table border="1">
 <caption><em>Conversion from matlab to PtolemyII types</em></caption>
 <tr><th>Matlab Type<th>PtolemyII Token
 <tr>
 <td>'double'
 <td>
 <ul>
 <li>Double, if Matlab object's dimension is 1x1 and
 {@link MatlabEngine.ConversionParameters#getScalarMatrices} is true;
 DoubleMatrix otherwise.</li>
 <li>Complex, if the Matlab object has imaginary parts, has dimension 1x1, and
 {@link MatlabEngine.ConversionParameters#getScalarMatrices} is true;
 ComplexMatrix otherwise.</li>
 </ul>
 <br>
 <em>Note:</em>
 If {@link MatlabEngine.ConversionParameters#getIntMatrices} is true and
 all matrix double values can be cast to integers without loss of
 precision then an {@link IntToken} or {@link IntTokenMatrix} is returned.
 <tr>
 <td>'struct'
 <td>RecordToken, if Matlab object's dimension 1x1, ArrayToken of ArrayTokens
 of RecordTokens {{RecordToken,...}; {...}} ("two-dimensional" ArrayToken)
 otherwise.
 <tr>
 <td>'cell'
 <td>ArrayToken of whatever Tokens the cell elements resolve to through
 recursive checking of these rules. In the special case of a cell
 array of doubles, an {int} is always returned if all cell double
 values can be losslessly converted to integers.
 Note that PtolemyII is more
 restrictive here in that it requires all array elements to be of the
 same type (not all matlab cell variables may be converted to PtolemyII
 ArrayTokens).
   <br>
 <em>Note:</em>
 When returning ArrayTokens of ArrayTokens, the innermost ArrayTokens will be converted into 
 scalars when their dimension is 1x1.
 <tr>
 <td>'char'
 <td>StringToken, if the Matlab object's dimension is 1xn; ArrayToken of StringTokens
 otherwise.
<tr>
 <td>'logical'
 <td>IntToken, if the Matlab object's dimension is 1x1; ArrayToken of IntTokens
 otherwise.
 </table>
 <p>

 <table border="1">
 <caption><em>Conversion from PtolemyII obects/types into Matlab object/types</em></caption>
 <tr><th>PtolemyII Token<th>Matlab type
 <tr>
 <td>ArrayToken
 <td>'cell', 1xn, elements are determined by recursing this method
 on ArrayToken elements.
 <tr>
 <td>RecordToken
 <td>'struct', 1x1, fields are determined by recursing this method on
 RecordToken fields
 <tr>
 <td>StringToken
 <td>'char', 1xn
 <tr>
 <td>ComplexMatrixToken
 <td>'double', with complex part; nxm
 <tr>
 <td>MatrixToken
 <td>'double', only real part; nxm
 <tr>
 <td>ComplexToken
 <td>'double', with complex part; 1x1
 <tr>
 <td>ScalarToken
 <td>'double', only real part; 1x1
 </table>
 <p>
 */
public class ObjectAdapter implements ToMatlabObjectAdapter<Token>, ToPtObjectAdapter <Token> {

	public static class ToolBox {
		public static String buildIndexString(final int n) {
			return "(" + n + ")";
		}
		public static String buildIndexString(final int n, final int m) {
			return "(" + n + "," + m + ")";
		}
	}
	
	final ConversionParameters conversionParameters;
	private ObjectAdapterFactory adapterFactory;
	
	public ObjectAdapter(final ConversionParameters conversionParameters) {
		super();
		this.conversionParameters = conversionParameters == null? new ConversionParameters() : conversionParameters;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MatlabObject adapt(Token ptToken, String ptTokenName) throws IllegalActionException {
		final MatlabObjectInfo proposedType = assignMatlabType(ptToken);
		final ToMatlabObjectAdapter adapter = geAdapterToMatlab(proposedType);
		return adapter.adapt(ptToken,ptTokenName);
	}

	@SuppressWarnings({ "rawtypes"})
	@Override
	public Token adapt(MatlabObject matlabObject, MatlabObjectInfo matlabObjectType) throws IllegalActionException {
		if (matlabObjectType == null) {
			matlabObjectType = inferMatlabType(matlabObject,
					conversionParameters.getIntMatrices,
					conversionParameters.getScalarMatrices);			
		}
		final ToPtObjectAdapter typeAdapter = getAdapterToPt(matlabObjectType);
		return typeAdapter.adapt(matlabObject, matlabObjectType);
	}

	public void setAdapterFactory(final ObjectAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
		adapterFactory.setRootAdapter2Pt(this);
		adapterFactory.setRootAdapter2Matlab(this);
	}

	private final static MatlabObjectInfo assignMatlabType(final Token token) {

		MatlabObjectInfo returned = MatlabObjectInfo.INVALID;

		if (token instanceof ArrayToken) {
			final ArrayToken arrayToken = (ArrayToken) token;
			if (containsNonStrings(arrayToken)) {
				returned = MatlabObjectInfo.CELL_MxN_MATRIX;
			} else if (arrayToken.length() == 1) {
				returned =  MatlabObjectInfo.CHAR;				
			} else {
				returned = MatlabObjectInfo.CHAR_MxN;
			}
			returned.setOriginClass(ArrayToken.class);
		} else if (token instanceof RecordToken) {
			returned =  MatlabObjectInfo.STRUCT;
			returned.setOriginClass(RecordToken.class);
		} else if (token instanceof StringToken) {
			returned =  MatlabObjectInfo.CHAR;
			returned.setOriginClass(StringToken.class);
		} else if (token instanceof ComplexMatrixToken) {
			returned =  MatlabObjectInfo.COMPLEX_MATRIX;
			returned.setOriginClass(ComplexMatrixToken.class);
		} else if (token instanceof DoubleMatrixToken) {
			returned =  MatlabObjectInfo.DOUBLE_MATRIX;
			returned.setOriginClass(DoubleMatrixToken.class);
		} else if (token instanceof ComplexToken) {
			returned =  MatlabObjectInfo.COMPLEX;
			returned.setOriginClass(ComplexToken.class);
		} else if (token instanceof ScalarToken) {
			returned =  MatlabObjectInfo.DOUBLE;
			returned.setOriginClass(ScalarToken.class);
		}

		return returned;
	}

	private static boolean containsNonStrings(final ArrayToken arrayToken) {
		return arrayToken.length() > 0 && !(arrayToken.getElement(0) instanceof StringToken); // It looks as if PT did not allow mixed-type arrays.
	}

	private final static MatlabObjectInfo inferMatlabType(final MatlabObject matlabObject, final boolean preferAsInteger, final boolean preferAsScalar) {

		MatlabObjectInfo foundType = MatlabObjectInfo.INVALID;

		final boolean treatAsScalar = preferAsScalar && matlabObject.isZeroDimensional();
		if (matlabObject.getClassName().equals(MatlabObjectInfo.DOUBLE.getClassName())) {
			if (matlabObject.isComplex()) {
				if (treatAsScalar) {
					foundType = MatlabObjectInfo.COMPLEX;
				} else {
					foundType = MatlabObjectInfo.COMPLEX_MATRIX;
				}
				foundType.setTreatAsScalar(treatAsScalar);
				foundType.setConvertToInt(false);
			} else {
				if (treatAsScalar) {
					foundType = MatlabObjectInfo.DOUBLE;
				} else {
					foundType = MatlabObjectInfo.DOUBLE_MATRIX;
				}
				foundType.setTreatAsScalar(treatAsScalar);
				foundType.setConvertToInt(preferAsInteger && matlabObject.hasIntegerValue());
			}
		} else if (matlabObject.getClassName().equals(MatlabObjectInfo.LOGICAL.getClassName())) {
			if (treatAsScalar) {
				foundType = MatlabObjectInfo.LOGICAL;
			} else {
				foundType = MatlabObjectInfo.LOGICAL_MATRIX;
			}
			foundType.setTreatAsScalar(treatAsScalar);
			foundType.setConvertToInt(true);
		} else if (matlabObject.getClassName().equals(MatlabObjectInfo.STRUCT.getClassName())) {
			foundType = MatlabObjectInfo.STRUCT;
			foundType.setConvertToInt(false);
		} else if (matlabObject.getClassName().equals(MatlabObjectInfo.CELL_MxN_MATRIX.getClassName())) {
			foundType = MatlabObjectInfo.CELL_MxN_MATRIX;
			foundType.setConvertToInt(false);
			foundType.setTreatAsScalar(treatAsScalar);
		} else if (matlabObject.getClassName().equals(MatlabObjectInfo.CHAR.getClassName())) {
			if (matlabObject.isZeroDimensional()) {
				foundType = MatlabObjectInfo.CHAR;
			} else {
				foundType = MatlabObjectInfo.CHAR_MxN;
			}
			foundType.setTreatAsScalar(false);
			foundType.setConvertToInt(false);
		}

		return foundType;

	}

	private ToMatlabObjectAdapter<?> geAdapterToMatlab(final MatlabObjectInfo targetType) throws IllegalActionException {

		if (targetType == MatlabObjectInfo.CELL_MxN_MATRIX && ArrayToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlCellMatrixAdapter();
		} 
		if (targetType == MatlabObjectInfo.CHAR_MxN && ArrayToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlNxNMatrixAdapter();
		} 
		if (targetType == MatlabObjectInfo.CHAR && StringToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlStringAdapter();
		} 
		if (targetType == MatlabObjectInfo.STRUCT && RecordToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlStructAdapter();
		} 
		if (targetType == MatlabObjectInfo.COMPLEX_MATRIX && ComplexMatrixToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlComplexMatrixAdapter();
		} 
		if (targetType == MatlabObjectInfo.COMPLEX && ComplexToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlComplexAdapter();
		} 
		if (targetType == MatlabObjectInfo.DOUBLE_MATRIX && DoubleMatrixToken.class.equals(targetType.getOriginClass())) {
			return adapterFactory.getPt2MlDoubleMatrixAdapter();
		} 
		if (targetType == MatlabObjectInfo.DOUBLE && ScalarToken.class.equals(targetType.getOriginClass())){
			return adapterFactory.getPt2MlDoubleAdapter();
		}

		throw new IllegalActionException("unsupported matlab type: " + targetType );

	}

	private ToPtObjectAdapter<?> getAdapterToPt(MatlabObjectInfo matlabType) throws IllegalActionException {

		if (matlabType == MatlabObjectInfo.CELL_MxN_MATRIX) {
			return adapterFactory.getCellArray2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.CHAR_MxN) {
			return adapterFactory.getStringMatrix2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.CHAR) {
			return adapterFactory.getString2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.STRUCT) {
			return adapterFactory.getStruct2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.COMPLEX_MATRIX) {
			return adapterFactory.getComplexMatrix2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.COMPLEX) {
			return adapterFactory.getComplex2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.DOUBLE_MATRIX) {
			return adapterFactory.getScalarMatrix2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.DOUBLE) {
			return adapterFactory.getScalar2PtAdapter();
		}
		if  (matlabType == MatlabObjectInfo.LOGICAL_MATRIX) {
			return adapterFactory.getScalarMatrix2PtAdapter();
		}
		if (matlabType == MatlabObjectInfo.LOGICAL) {
			return adapterFactory.getScalar2PtAdapter();
		}

		throw new IllegalActionException("unsupported matlab type: " + matlabType );

	}

}
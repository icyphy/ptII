package ptolemy.matlab.impl.backends.jmathlib.adapters;

import jmathlib.core.tokens.CellArrayToken;
import jmathlib.core.tokens.CharToken;
import jmathlib.core.tokens.DataToken;
import jmathlib.core.tokens.LogicalToken;
import jmathlib.core.tokens.MathLibObject;
import jmathlib.core.tokens.MatrixToken;
import jmathlib.core.tokens.NumberToken;
import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.utils.IntToolbox;
import ptolemy.matlab.impl.utils.SimpleIntToolbox;

/**
 * @author david
 *
 */
public class JMLObject implements MatlabObject {
	
	final IntToolbox dataUtils = new SimpleIntToolbox();
	private final DataToken  jmlToken;
	private final String name;

	public JMLObject(final DataToken  jmlToken,final String name) {
		this.jmlToken = jmlToken;
		this.name = name;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#hasIntegerValue()
	 */
	@Override
	public boolean hasIntegerValue() {
		if (!(jmlToken instanceof NumberToken) && 
				!(jmlToken instanceof LogicalToken)) {
			return false;
		}
		if (!(jmlToken instanceof DoubleNumberToken)) {
			return true;
		}
		final DoubleNumberToken doubleValue = (DoubleNumberToken) jmlToken;
		return !doubleValue.isComplex() && dataUtils.isInteger(doubleValue.getValueRe());
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#isComplex()
	 */
	@Override
	public boolean isComplex() {
		if (!(jmlToken instanceof DoubleNumberToken)) {
			return false;
		}
		return  ((DoubleNumberToken) jmlToken).isComplex();
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#isZeroDimensional()
	 */
	@Override
	public boolean isZeroDimensional() {
		if (jmlToken instanceof NumberToken) {
			return ((NumberToken) jmlToken).isScalar();
		} else if (jmlToken instanceof CharToken) {
			return jmlToken.getSizeY() == 1;
		}
		return jmlToken.getSizeX() <=1 && jmlToken.getSizeY() <=1;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#getClassName()
	 */
	@Override
	public String getClassName() {
		if (jmlToken instanceof CellArrayToken) {
			return MatlabObjectInfo.CELL_MxN_MATRIX.getClassName();
		}
		if (jmlToken instanceof CharToken) {
			return MatlabObjectInfo.CHAR.getClassName();
		}
		if (jmlToken instanceof LogicalToken) {
			return MatlabObjectInfo.LOGICAL.getClassName();
		}
		if (jmlToken instanceof MathLibObject) {
			return MatlabObjectInfo.STRUCT.getClassName();
		}
		if (jmlToken instanceof MatrixToken) {
			return MatlabObjectInfo.CHAR_MxN.getClassName();
		}
		if (jmlToken instanceof NumberToken) {
			return MatlabObjectInfo.DOUBLE.getClassName();
		}
		throw new IllegalStateException("cannot find class name for JML token " + jmlToken.getClass().getSimpleName());
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#getnRows()
	 */
	@Override
	public int getnRows() {
//		if (jmlToken instanceof CharToken) {
//			return jmlToken.get
//		}
		return jmlToken.getSizeY();
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#getnCols()
	 */
	@Override
	public int getnCols() {
		return jmlToken.getSizeX();
	}

	/**
	 * @return the jmlToken
	 */
	public DataToken getJmlToken() {
		return jmlToken;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

}

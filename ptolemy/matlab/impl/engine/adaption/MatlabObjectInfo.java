package ptolemy.matlab.impl.engine.adaption;

import ptolemy.data.Token;

/**
 * @author david
 *
 */
public enum MatlabObjectInfo {
	
	CELL_MxN_MATRIX(MatlabClassName.CELL.toString()),
	/** A single, N-long string */
	CHAR(MatlabClassName.CHAR.toString()),
	/** An M-long array of N-long strings*/
	CHAR_MxN(MatlabClassName.CHAR.toString()),
	STRUCT(MatlabClassName.STRUCT.toString()),
	COMPLEX(MatlabClassName.DOUBLE.toString()),
	COMPLEX_MATRIX(MatlabClassName.DOUBLE.toString()),
	DOUBLE(MatlabClassName.DOUBLE.toString()),
	DOUBLE_MATRIX(MatlabClassName.DOUBLE.toString()),
	LOGICAL(MatlabClassName.LOGICAL.toString()),
	LOGICAL_MATRIX(MatlabClassName.LOGICAL.toString()),
	INVALID("invalid");
	
	private boolean convertToInt;
	private boolean treatAsScalar;
	private Class<? extends Token> originClass;
	private final String className;
	
	private MatlabObjectInfo(final String className) {
		this.className = className;
	}
	
	/**
	 * @return the convertToInt
	 */
	public boolean isConvertToInt() {
		return convertToInt;
	}
	/**
	 * @param convertToInt the convertToInt to set
	 */
	public void setConvertToInt(boolean convertToInt) {
		this.convertToInt = convertToInt;
	}
	/**
	 * @return the treatAsScalar
	 */
	public boolean isTreatAsScalar() {
		return treatAsScalar;
	}
	/**
	 * @param treatAsScalar the treatAsScalar to set
	 */
	public void setTreatAsScalar(boolean treatAsScalar) {
		this.treatAsScalar = treatAsScalar;
	}
	public void setOriginClass(Class<? extends Token> aClass) {
		this.originClass = aClass;
	}
	public Class<? extends Token> getOriginClass() {
		return originClass;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	
}

enum MatlabClassName {

	CHAR("char"),
	CELL("cell"),
	STRUCT("struct"),
	LOGICAL("logical"),
	DOUBLE("double");

	private final String className;

	MatlabClassName(final String className) {
		this.className = className;
	}
	
	@Override
	public String toString() {
		return className;
	}
	
	public String getClassName() {
		return className;
	}
	
}

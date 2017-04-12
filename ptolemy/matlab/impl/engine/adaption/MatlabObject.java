package ptolemy.matlab.impl.engine.adaption;

/**
 * @author david
 *
 */
public interface MatlabObject {
	
	public String getName();
	boolean hasIntegerValue();
	boolean isComplex();
	boolean isZeroDimensional();
	String getClassName();
	int getnRows();
	int getnCols();

}

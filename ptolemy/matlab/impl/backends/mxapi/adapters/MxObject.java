package ptolemy.matlab.impl.backends.mxapi.adapters;

import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.utils.IntToolbox;
import ptolemy.matlab.impl.utils.SimpleIntToolbox;

/**
 * A Java object containing context info to uniquely identify a MEX array.
 * 
 * @author David Guardado Barcia
 *
 */
public class MxObject implements MatlabObject {

	private PtMatlab ptMatlab;
	private long mxArray;
	private int nRows;
	private int nCols;
	private String className;
	private IntToolbox dataUtils = new SimpleIntToolbox();
	private final String name;
	
	public MxObject(PtMatlab ptMatlab, long mxArray, final String name) {
		super();
		this.ptMatlab = ptMatlab;
		this.mxArray = mxArray;
		final int[] dims = ptMatlab.ptmatlabGetDimensions(mxArray);
		nRows = dims[0];
		nCols = dims[1];
		className = ptMatlab.ptmatlabGetClassName(mxArray);
		this.name = name;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#hasIntegerValue()
	 */
	@Override
	public boolean hasIntegerValue() {
		if (this.getClassName().equals("double")) {
			double[][] a = ptMatlab.ptmatlabGetDoubleMatrix(mxArray, nRows, nCols);	
			return isZeroDimensional() && dataUtils.isInteger(a[0][0]) || !isZeroDimensional() && dataUtils.hasOnlyIntegerValues(a);
		} else if (this.getClassName().equals("logical")) {
			return true;
		}
		return false;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return className;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#isComplex()
	 */
	@Override
	public boolean isComplex() {
		return ptMatlab.ptmatlabIsComplex(mxArray);
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#isZeroDimensional()
	 */
	@Override
	public boolean isZeroDimensional() {
		return nRows == 1 && nCols == 1;
	}
	
	public boolean hasOnlyOneRow() {
		return nRows == 1;
	}

	/**
	 * @return the mxArray
	 */
	public long getMxArray() {
		return mxArray;
	}

	/**
	 * @return the nRows
	 */
	@Override
	public int getnRows() {
		return nRows;
	}

	/**
	 * @return the nCols
	 */
	@Override
	public int getnCols() {
		return nCols;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.adaption.MatlabObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

}
package ptolemy.matlab.impl.backends.mxapi.jni;

import ptolemy.data.expr.UtilityFunctions;
import ptolemy.math.Complex;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;

/**
 * A java wrapper of Matlab's MEX API methods themselves wrapped into the non-Java 
 * library <code>ptmatlab.cc</code>, using JNI.
 * 
 * <p>It provides a java API to the matlab environment. It uses an intermediary
 *  C++ language layer (<code>ptmatlab</code>) that converts between the java environment
 * using the Java Native Interface and the matlab environment using the
 * matlab engine API and associated MEX API functions.</p>

 * <p>The intermediary layer is built as a DLL on Windows systems
 * (<code>ptmatlab.dll</code>).  This shared library is placed into the <code>$PTII/bin</code>
 * directory (that should be in the user's path) when this package is
 * built. <code>ptmatlab</code> depends on Matlab's engine MEX API shared libraries (<code>libeng</code>
 * and <code>libmx</code>) that should also be in the user's path (usually the
 * case when Matlab is installed and Matlab's <code>bin</code> directory is added to the
 * path).</p>
 */
public class JniPtMatlab implements PtMatlab {
	
	/** Load the "ptmatlab" native interface. Use a classpath-relative
	 * pathname without the shared library suffix (which is selected
	 * and appended by {@link UtilityFunctions#loadLibrary}) for
	 * portability. */
	static {
		UtilityFunctions.loadLibrary("ptolemy/matlab/ptmatlab");
	}
		
    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabEngOpen(java.lang.String)
	 */
    @Override
	public native long ptmatlabEngOpen(String startCmd);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabEngClose(long, long)
	 */
    @Override
	public native int ptmatlabEngClose(long e, long outputBuffer);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabEngEvalString(long, java.lang.String)
	 */
    @Override
	public native int ptmatlabEngEvalString(long e, String s);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabEngGetArray(long, java.lang.String)
	 */
    @Override
	public native long ptmatlabEngGetArray(long e, String name);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabEngPutArray(long, java.lang.String, long)
	 */
    @Override
	public native int ptmatlabEngPutArray(long e, String name, long mxArray);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabEngOutputBuffer(long, int)
	 */
    @Override
	public native long ptmatlabEngOutputBuffer(long e, int n);

    // C-Mx style functions
    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateCellMatrix(java.lang.String, int, int)
	 */
    @Override
	public native long ptmatlabCreateCellMatrix(String name, int n, int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateString(java.lang.String, java.lang.String, int, int)
	 */
    @Override
	public native long ptmatlabCreateString(String name, String s, int n, int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateDoubleMatrixOneDim(java.lang.String, double[], int)
	 */
    @Override
	public native long ptmatlabCreateDoubleMatrixOneDim(String name,
            double[] a, int length);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateDoubleMatrix(java.lang.String, double[][], int, int)
	 */
    @Override
	public native long ptmatlabCreateDoubleMatrix(String name, double[][] a,
            int n, int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateComplexMatrixOneDim(java.lang.String, ptolemy.math.Complex[], int)
	 */
    @Override
	public native long ptmatlabCreateComplexMatrixOneDim(String name,
            Complex[] a, int length);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateComplexMatrix(java.lang.String, ptolemy.math.Complex[][], int, int)
	 */
    @Override
	public native long ptmatlabCreateComplexMatrix(String name, Complex[][] a,
            int n, int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabCreateStructMatrix(java.lang.String, java.lang.Object[], int, int)
	 */
    @Override
	public native long ptmatlabCreateStructMatrix(String name,
            Object[] fieldNames, int n, int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabDestroy(long, java.lang.String)
	 */
    @Override
	public native void ptmatlabDestroy(long mxArray, String name);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetCell(long, int, int)
	 */
    @Override
	public native long ptmatlabGetCell(long mxArray, int n, int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetClassName(long)
	 */
    @Override
	public native String ptmatlabGetClassName(long mxArray);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetDimensions(long)
	 */
    @Override
	public native int[] ptmatlabGetDimensions(long mxArray);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetComplexMatrix(long, int, int)
	 */
    @Override
	public native Complex[][] ptmatlabGetComplexMatrix(long mxArray, int n,
            int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetLogicalMatrix(long, int, int)
	 */
    @Override
	public native int[][] ptmatlabGetLogicalMatrix(long mxArray, int nRows,
            int nCols);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetFieldNameByNumber(long, int)
	 */
    @Override
	public native String ptmatlabGetFieldNameByNumber(long mxArray, int k);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetFieldByNumber(long, int, int, int)
	 */
    @Override
	public native long ptmatlabGetFieldByNumber(long mxArray, int k, int n,
            int m);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetNumberOfFields(long)
	 */
    @Override
	public native int ptmatlabGetNumberOfFields(long mxArray);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetString(long, int)
	 */
    @Override
	public native String ptmatlabGetString(long mxArray, int n);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetOutput(long, int)
	 */
    @Override
	public native String ptmatlabGetOutput(long outputBuffer, int n);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabIsComplex(long)
	 */
    @Override
	public native boolean ptmatlabIsComplex(long mxArray);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabSetCell(java.lang.String, long, int, int, long)
	 */
    @Override
	public native void ptmatlabSetCell(String name, long mxArray, int n,
            int m, long valueMxArray);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabSetString(java.lang.String, long, int, java.lang.String, int)
	 */
    @Override
	public native void ptmatlabSetString(String name, long mxArray, int n,
            String s, int slen);

    /**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabSetStructField(java.lang.String, long, java.lang.String, int, int, long)
	 */
    @Override
	public native void ptmatlabSetStructField(String name, long mxArray,
            String fieldName, int n, int m, long valueMxArray);

	/**
	 * @see ptolemy.matlab.impl.backends.mxapi.PtMatlab#ptmatlabGetDoubleMatrix(long, int, int)
	 */
	@Override
	public native double[][] ptmatlabGetDoubleMatrix(long mxArray, int n, int m);


}

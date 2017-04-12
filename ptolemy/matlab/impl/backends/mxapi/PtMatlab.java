package ptolemy.matlab.impl.backends.mxapi;

import ptolemy.math.Complex;

/**
 * A generic facade encapsulating pointer-based Matlab APIs (such as Matlab's MEX API)..
 */
public interface PtMatlab {

	static final int SUCCESS = 0;
	static final int INVALID_POINTER = 0;
	
	/**
	 * 
	 * @param startCmd path to Matlab's "maci.exe" executable file
	 * @return a result other than INVALID_POINTER if the operation was successful
	 */
	long ptmatlabEngOpen(String startCmd);

	int ptmatlabEngClose(long e, long outputBuffer);

	int ptmatlabEngEvalString(long e, String s);

	long ptmatlabEngGetArray(long e, String name);

	int ptmatlabEngPutArray(long e, String name, long mxArray);

	long ptmatlabEngOutputBuffer(long e, int n);

	// C-Mx style functions
	long ptmatlabCreateCellMatrix(String name, int n, int m);

	long ptmatlabCreateString(String name, String s, int n, int m);

	long ptmatlabCreateDoubleMatrixOneDim(String name, double[] a, int length);

	long ptmatlabCreateDoubleMatrix(String name, double[][] a, int n, int m);

	long ptmatlabCreateComplexMatrixOneDim(String name, Complex[] a, int length);

	long ptmatlabCreateComplexMatrix(String name, Complex[][] a, int n, int m);

	long ptmatlabCreateStructMatrix(String name, Object[] fieldNames, int n, int m);

	void ptmatlabDestroy(long mxArray, String name);

	long ptmatlabGetCell(long mxArray, int n, int m);

	String ptmatlabGetClassName(long mxArray);

	int[] ptmatlabGetDimensions(long mxArray);

	Complex[][] ptmatlabGetComplexMatrix(long mxArray, int n, int m);

	int[][] ptmatlabGetLogicalMatrix(long mxArray, int nRows, int nCols);

	String ptmatlabGetFieldNameByNumber(long mxArray, int k);

	long ptmatlabGetFieldByNumber(long mxArray, int k, int n, int m);

	int ptmatlabGetNumberOfFields(long mxArray);

	String ptmatlabGetString(long mxArray, int n);

	String ptmatlabGetOutput(long outputBuffer, int n);

	boolean ptmatlabIsComplex(long mxArray);

	void ptmatlabSetCell(String name, long mxArray, int n, int m, long valueMxArray);

	void ptmatlabSetString(String name, long mxArray, int n, String s, int slen);

	void ptmatlabSetStructField(String name, long mxArray, String fieldName, int n, int m, long valueMxArray);

    double[][] ptmatlabGetDoubleMatrix(long mxArray, int n, int m);

}
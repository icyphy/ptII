package org.ptolemy.machineLearning;

public class Algorithms {

    private Algorithms() {
        // TODO Auto-generated constructor stub
    }
    /**
     * Do a binary interval search for the key in array A. The bin index in which
     * key is found is returned.
     * @param A The search array
     * @param key Key to be searched
     * @param 
     */
    public static int _binaryIntervalSearch(double[] A, double key) {
        return _binaryIntervalSearch(A, key, 0, A.length-1);
    }
    public static int _binaryIntervalSearch(double[] A, double key, int imin, int imax) {
        if (imax < imin) {
            return -1;
        } else {
            int imid = imin + ((imax - imin) / 2);
            if (imid >= A.length - 1) {
                return -1;
            } else if (A[imid] <= key && A[imid + 1] > key) {
                return imid;
            } else if (A[imid] > key) {
                return _binaryIntervalSearch(A, key, imin, imid - 1);
            } else if (A[imid] < key) {
                return _binaryIntervalSearch(A, key, imid + 1, imax);
            } else {
                return imid;
            }
        }
    }

}

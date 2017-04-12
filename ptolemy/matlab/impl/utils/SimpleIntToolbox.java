package ptolemy.matlab.impl.utils;

/**
 * A simple, obvious implementation of {@link IntToolbox}.
 * 
 * @author David Guardado Barcia
 *
 */
public class SimpleIntToolbox implements IntToolbox {

	/**
	 * @see ptolemy.matlab.impl.utils.IntToolbox#hasOnlyIntegerValues(double[][])
	 */
	@Override
	public boolean hasOnlyIntegerValues(double[][] a) {
		boolean hasOnlyIntegerValues = true;
		for (int i = 0; hasOnlyIntegerValues && i < a.length; i++) {
		    for (int j = 0; hasOnlyIntegerValues && j < a[0].length; j++) {
		        hasOnlyIntegerValues &= isInteger(a[i][j]);
		    }
		}
		return hasOnlyIntegerValues;
	}
	
	/**
	 * @see ptolemy.matlab.impl.utils.IntToolbox#convertDoubleToInt(double[][])
	 */
	@Override
	public int[][] convertDoubleToInt(double[][] a) {
		int[][] tmp = new int[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
		    for (int j = 0; j < a[0].length; j++) {
		        tmp[i][j] = (int) a[i][j];
		    }
		}
		return tmp;
	}
	
    /**
	 * @see ptolemy.matlab.impl.utils.IntToolbox#isInteger(double)
	 */
    @Override
	public boolean isInteger(double d) {
        // FindBugs reports "Test for floating point equality", which
        // may be ignored here because we really want to know if
        // the double is equal to the floor of the double.
        return d == Math.floor(d) && d <= Integer.MAX_VALUE
                && d >= Integer.MIN_VALUE;
    }
	
}

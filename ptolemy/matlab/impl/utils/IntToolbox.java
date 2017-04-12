package ptolemy.matlab.impl.utils;

/**
 * A toolb0x-like interface for common operations on ints and doubles.
 * 
 * @author David Guardado Barcia
 */
public interface IntToolbox {

	/**
	 * Returns <code>true</code> if, and only if there is no number inside the passed array with non-integer parts.
	 * @param doublesArray a 2-dimensional array of doubles
	 */
	boolean hasOnlyIntegerValues(double[][] doublesArray);

	/**
	 * Rounds an array of doubles into an array of ints.
	 * @param aDouble a 2-dimensional array of doubles
	 */
	int[][] convertDoubleToInt(double[][] aDouble);

	/**
	 * Returns <code>true</code> if, and only if the passed number contains no non-integer part.
	 * @param aDouble a double
	 */
	boolean isInteger(double aDouble);

}
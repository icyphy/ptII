package ptolemy.domains.tt.tdl.kernel;

/**
 * some calculations required in the TT/TDL domain
 * 
 * @author Patricia Derler
 */
public class MathUtilities {

	/**
	 * greatest common divisor
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int gcd(int a, int b) {
		if (b == 0)
			return a;
		else
			return MathUtilities.gcd(b, a % b);
	}

	/**
	 * greatest common divisor
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static long gcd(long a, long b) {
		if (b == 0)
			return a;
		else
			return MathUtilities.gcd(b, a % b);
	}

}

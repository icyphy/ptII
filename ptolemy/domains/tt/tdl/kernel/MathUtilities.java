package ptolemy.domains.tt.tdl.kernel;

/**
 * some calculations required in the TT/TDL domain
 * 
 * @author Patricia Derler
 */
public class MathUtilities {

	/**
	 * Return the greatest common divisor.
	 * 
	 * @param a
	 * @param b
	 * @return The geatest common divisor.
	 */
	public static int gcd(int a, int b) {
            if (b == 0) {
                return a;
            } else {
                return MathUtilities.gcd(b, a % b);
            }
	}

	/**
	 * Return the greatest common divisor.
	 * 
	 * @param a
	 * @param b
	 * @return The greatest common divisor.
	 */
	public static long gcd(long a, long b) {
            if (b == 0) {
                return a;
            } else {
                return MathUtilities.gcd(b, a % b);
            }
	}

}

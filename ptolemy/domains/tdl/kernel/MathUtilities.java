/*
@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                        PT_COPYRIGHT_VERSION_2
                        COPYRIGHTENDKEY


 */
package ptolemy.domains.tdl.kernel;

/**
 * some calculations required in the TT/TDL domain.
 * 
 * @author Patricia Derler
 */
public class MathUtilities {

	/**
	 * Return the greatest common divisor of two values.
	 * 
	 * @param a First value.
	 * @param b Second value.
	 * @return The greatest common divisor.
	 */
	public static int gcd(int a, int b) {
            if (b == 0) {
                return a;
            } else {
                return MathUtilities.gcd(b, a % b);
            }
	}

	/**
	 * Return the greatest common divisor of two values.
	 * 
	 * @param a First value.
	 * @param b Second value.
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

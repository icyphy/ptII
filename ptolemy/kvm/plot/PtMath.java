/* A signal plotter.

@Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/
package ptolemy.kvm.plot;

/** Ptolemy java.lang.Math compatibility package
*/
public class PtMath {
    /**
     * Returns the smallest (closest to negative infinity) 
     * <code>double</code> value that is not less than the argument and is 
     * equal to a mathematical integer. 
     *
     * @param   a   a <code>double</code> value.
     * <!--@return  the value &lceil;&nbsp;<code>a</code>&nbsp;&rceil;.-->
     * @return  the smallest (closest to negative infinity) 
     *          <code>double</code> value that is not less than the argument
     *          and is equal to a mathematical integer. 
     */
    public static double ceil(double a) {
        //KVM_FIXME: need ceiling
        return a;
    }

    /**
     * Returns the trigonometric cosine of an angle.
     *
     * @param   a   an angle, in radians.
     * @return  the cosine of the argument.
     */
    public static double cos(double a) {
        //KVM_FIXME: need cos()
        return a;
    }

    /**
     * Returns the largest (closest to positive infinity) 
     * <code>double</code> value that is not greater than the argument and 
     * is equal to a mathematical integer. 
     *
     * @param   a   a <code>double</code> value.
     * @param   a   an assigned value.
     * <!--@return  the value &lfloor;&nbsp;<code>a</code>&nbsp;&rfloor;.-->
     * @return  the largest (closest to positive infinity) 
     *          <code>double</code> value that is not greater than the argument
     *          and is equal to a mathematical integer. 
     */
    public static double floor(double a) {
        //KVM_FIXME: need floor()
        return a;
    }

    /**
     * Returns the natural logarithm (base <i>e</i>) of a <code>double</code>
     * value.
     *
     * @param   a   a number greater than <code>0.0</code>.
     * @return  the value ln&nbsp;<code>a</code>, the natural logarithm of
     *          <code>a</code>.
     */
    public static double log(double a) {
        //KVM_FIXME: need log()
        return a;

    }

    /*
     * Returns of value of the first argument raised to the power of the
     * second argument.
     * <p>
     * If (<code>a&nbsp;==&nbsp;0.0</code>), then <code>b</code> must be
     * greater than <code>0.0</code>; otherwise an exception is thrown. 
     * An exception also will occur if (<code>a&nbsp;&lt;=&nbsp;0.0</code>)
     * and <code>b</code> is not equal to a whole number.
     *
     * @param   a   a <code>double</code> value.
     * @param   b   a <code>double</code> value.
     * @return  the value <code>a<sup>b</sup></code>.
     * @exception ArithmeticException  if (<code>a&nbsp;==&nbsp;0.0</code>) and
     *              (<code>b&nbsp;&lt;=&nbsp;0.0</code>), or
     *              if (<code>a&nbsp;&lt;=&nbsp;0.0</code>) and <code>b</code>
     *              is not equal to a whole number.
     */
    public static double pow(double a, double b) {
        //KVM_FIXME: need pow()
        return a;
    }

    /**
     * returns the closest integer to the argument. 
     *
     * @param   a   a <code>double</code> value.
     * @return  the closest <code>double</code> value to <code>a</code> that is
     *          equal to a mathematical integer. If two <code>double</code>
     *          values that are mathematical integers are equally close to the
     *          value of the argument, the result is the integer value that
     *          is even.
     */
    public static double rint(double a) {
        //KAWT_FIXME: need rint()
        return a;
    }

    /* kvm java.lang.Double does not exist */
    public static final double MAX_VALUE = 1.79769313486231570e+308;
    public static final double MIN_VALUE = 4.94065645841246544e-324;
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    
    /* kvm java.lang.Math does not have PI */
    public static final double PI = 3.14159265358979323846;

}

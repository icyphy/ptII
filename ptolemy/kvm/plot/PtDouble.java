/* Trivial implementation of Double
@Author: Edward A. Lee and Christopher Hylands

@Contributors:  William Wu

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
/** FIXME: PtDouble is a copy of part of the Sun source code.
 */
public class PtDouble {
    /**
     * Constructs a newly allocated <code>Double</code> object that
     * represents the primitive <code>double</code> argument.
     *
     * @param   value   the value to be represented by the <code>Double</code>.
     */
    public PtDouble(double value) {
	this.value = value;
    }

    /**
     * Constructs a newly allocated <code>Double</code> object that
     * represents the floating- point value of type <code>double</code>
     * represented by the string. The string is converted to a
     * <code>double</code> value as if by the <code>valueOf</code> method.
     *
     * @param      s   a string to be converted to a <code>Double</code>.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable number.
     * @see        java.lang.Double#valueOf(java.lang.String)
     */
    public PtDouble(String s) throws NumberFormatException {
	// REMIND: this is inefficient
	this(valueOf(s).doubleValue());
    }

    /**
     * Returns the double value of this Double.
     *
     * @return  the <code>double</code> value represented by this object.
     */
    public double doubleValue() {
	return (double)value;
    }

    /**
     * Returns a new <code>Double</code> object initialized to the value 
     * represented by the specified string. The string <code>s</code> is 
     * interpreted as the representation of a floating-point value and a 
     * <code>Double</code> object representing that value is created and 
     * returned. 
     * <p>
     * If <code>s</code> is <code>null</code>, then a 
     * <code>NullPointerException</code> is thrown.
     * <p>
     * Leading and trailing whitespace characters in s are ignored. The rest 
     * of <code>s</code> should constitute a <i>FloatValue</i> as described 
     * by the lexical rule:
     * <blockquote><pre><i>
     * FloatValue:
     * 
     *        Sign<sub>opt</sub> FloatingPointLiteral
     * </i></pre></blockquote>
     * where <i>Sign</i> and <i>FloatingPointLiteral</i> are as defined in 
     * Åß3.10.2 of the <a href="http://java.sun.com/docs/books/jls/html/">Java 
     * Language Specification</a>. If it does not have the form of a 
     * <i>FloatValue</i>, then a <code>NumberFormatException</code> is 
     * thrown. Otherwise, it is regarded as representing an exact decimal 
     * value in the usual "computerized scientific notation"; this exact 
     * decimal value is then conceptually converted to an "infinitely 
     * precise" binary value that is then rounded to type <code>double</code> 
     * by the usual round-to-nearest rule of IEEE 754 floating-point 
     * arithmetic. Finally, a new object of class <code>Double</code> is 
     * created to represent the <code>double</code> value. 
     *
     * @param      s   the string to be parsed.
     * @return     a newly constructed <code>Double</code> initialized to the
     *             value represented by the string argument.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable number.
     */
    public static PtDouble valueOf(String s) throws NumberFormatException {
	return new PtDouble(PtFloatingDecimal.readJavaFormatString(s).doubleValue());
    }

    /**
     * The value of the Double.
     *
     * @serial
     */
    private double value;
}

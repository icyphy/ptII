/*
An interface specifying how to represent matrices and vectors as
Strings.

Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.math;

/**
@author Jeff Tsay
@version $Id$
*/
public interface ArrayStringFormat {

    /** Return a String representation of a boolean. */
    public String booleanString(boolean b);

    /** Return a String representation of a complex number. */
    public String complexString(Complex c);

    /** Return a String representation of a double. */
    public String doubleString(double d);

    /** Return a String separating elements in each row vector. */
    public String elementDelimiterString();

    /** Return a String representation of a float. */
    public String floatString(float f);

    /** Return a String representation of an integer. */
    public String intString(int i);

    /** Return a String representation of a long. */
    public String longString(long l);

    /** Return a String marking the beginning of a matrix. */
    public String matrixBeginString();

    /** Return a String marking the end of a matrix. */
    public String matrixEndString();

    /** Return a String representation of a short. */
    public String shortString(short s);

    /** Return a String marking the beginning of a row vector. */
    public String vectorBeginString();

    /** Return a String separating the row vectors. */
    public String vectorDelimiterString();

    /** Return a String marking the end of a row vector. */
    public String vectorEndString();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     */
    public static class ArrayStringFormatBase implements ArrayStringFormat {
        public ArrayStringFormatBase(String elemDelim, String matrixBegin,
         String matrixEnd, String vectorBegin, String vectorDelim,
         String vectorEnd) {
            _elemDelim = elemDelim;
            _matrixBegin = matrixBegin;
            _matrixEnd = matrixEnd;
            _vectorBegin = vectorBegin;
            _vectorDelim = vectorDelim;
            _vectorEnd = vectorEnd;
        }

        public String booleanString(boolean b) {
            return "" + b; // this is a hack
        }

        public String complexString(Complex c) {
            return c.toString();
        }

        public String doubleString(double d) {
            return Double.toString(d);
        }

        public String elementDelimiterString() {
            return _elemDelim;
        }

        public String floatString(float f) {
            return Float.toString(f);
        }

        public String intString(int i) {
            return Integer.toString(i);
        }

        public String longString(long l) {
            return Long.toString(l);
        }

        public String matrixBeginString() {
            return _matrixBegin;
        }

        public String matrixEndString() {
            return _matrixEnd;
        }

        public String shortString(short s) {
            return Short.toString(s);
        }

        public String vectorBeginString() {
            return _vectorBegin;
        }

        public String vectorDelimiterString() {
            return _vectorDelim;
        }

        public String vectorEndString() {
            return _vectorEnd;
        }

        protected final String _elemDelim;
        protected final String _matrixBegin;
        protected final String _matrixEnd;
        protected final String _vectorBegin;
        protected final String _vectorDelim;
        protected final String _vectorEnd;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Implements ArrayStringFormat to produce strings in the format used
     *  to initialize arrays in Java. More specifically, the format
     *  "{x[0], x[1], x[2], ... , x[n-1]}",
     *  where x[i] is the ith element of the array.
     */
    public static final ArrayStringFormat javaASFormat =
        new ArrayStringFormatBase(", ", "{", "}", "{", ", ", "}");

    /** Implements ArrayStringFormat to produce strings in the format used
     *  in the Ptolemy II expression language and Matlab. More specifically,
     *  the format
     *  "[x[0] x[1] x[2] ...  x[n-1]]",
     */
    public static final ArrayStringFormat exprASFormat =
        new ArrayStringFormatBase(" ", "[", "]", "[", "; ", "]");
}

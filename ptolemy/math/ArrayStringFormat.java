/* An interface specifying how to represent matrices and vectors as
String objects.

Copyright (c) 1998-1999 The Regents of the University of California.
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

public interface ArrayStringFormat {

  /**
   *  Return a String representation of a complex number.
   */
  public String complexString(Complex c);

  /**
   *  Return a String representation of a double.
   */
  public String doubleString(double d);

  /**
   *  Return a String separating elements in the matrix or vector.
   */
  public String elementDeliminatorString();

  /**
   *  Return a String marking the beginning of a matrix.
   */
  public String matrixBeginString();

  /**
   *  Return a String marking the end of a matrix.
   */
  public String matrixEndString();

  /**
   *  Return a String marking the beginning of a vector.
   */
  public String vectorBeginString();

  /**
   *  Return a String separating the row vectors.
   */
  public String vectorDeliminatorString();

  /**
   *  Return a String marking the beginning of a vector.
   */
  public String vectorEndString();

  /////////////////////////////////////////////////////////////////////////
  ////                         Public classes                          ////

  /** Implements ArrayStringFormat to produce strings in the format used
   *  to initialize arrays in Java. More specifically, the format
   *  "{x[0], x[1], x[2], ... , x[n-1]}",
   *  where x[i] is the ith element of the array.
   */
  public static class JavaArrayStringFormat implements
   ArrayStringFormat {

    public JavaArrayStringFormat() {}

    public String complexString(Complex c) {
      return c.toString();
    }

    public String doubleString(double d) {
      return Double.toString(d);
    }

    public String elementDeliminatorString() {
      return ", ";
    }

    public String matrixBeginString() {
      return "{";
    }

    public String matrixEndString() {
      return "}";
    }

    public String vectorBeginString() {
      return "{";
    }

    public String vectorDeliminatorString() {
      return ", ";
    }

    public String vectorEndString() {
      return "}";
    }
  }

  /** Implements ArrayStringFormat to produce strings in the format used
   *  in the Ptolemy II expression language and Matlab. More specifically,
   *  the format
   *  "[x[0] x[1] x[2] ...  x[n-1]]",
   */
  public static class ExprArrayStringFormat implements
   ArrayStringFormat {
    public ExprArrayStringFormat() {}

    public String complexString(Complex c) {
      return c.toString();
    }

    public String elementDeliminatorString() {
      return " ";
    }

    public String doubleString(double d) {
      return Double.toString(d);
    }

    public String matrixBeginString() {
      return "[";
    }

    public String matrixEndString() {
      return "]";
    }

    public String vectorBeginString() {
      return "[";
    }

    public String vectorDeliminatorString() {
      return " | ";
    }

    public String vectorEndString() {
      return "]";
    }
  }

  /////////////////////////////////////////////////////////////////////////
  ////                         Public fields                           ////

  /** A static instance of JavaArrayStringFormat.
   */
  public static final ArrayStringFormat javaASFormat =
   new JavaArrayStringFormat();

  /** A static instance of ExprArrayStringFormat.
   */
  public static final ArrayStringFormat exprASFormat =
   new ExprArrayStringFormat();
}

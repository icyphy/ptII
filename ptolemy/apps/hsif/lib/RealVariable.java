/* A realvariable is used to indicate the state of the DNHA.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.apps.hsif.lib;

public class RealVariable extends Variable{

  public RealVariable(String name) {
    this(name, "unknown", 0.0, 0.0, 0.0, 0.0, 0.0);
 }

  public RealVariable(String name, double value) {
    this(name, "unknown", value, 0.0, 0.0, 0.0, 0.0);
 }

  public RealVariable(String name, double value, double min, double max) {
    this(name, "unknown", value, min, max, 0.0, 0.0);
 }

  public RealVariable(String name, String IOType, double value, double min, double max, double initialMin, double initialMax) {
    super(name, "double", IOType);
    _value = value;
    _minValue = min;
    _maxValue = max;
    _initialMinValue = initialMin;
    _initialMaxValue = initialMax;
 }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

  /** set the value of the integer variable
   *
   */
  public void setValue(double value) {
    _value = value;
  }

  public double getValue() {
    return _value;
  }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

  private double _value = 0.0;
  private double _minValue = 0.0;
  private double _maxValue = 0.0;
  private double _initialMinValue = 0.0;
  private double _initialMaxValue = 0.0;
}
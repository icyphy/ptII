/* A parameter with type double with a limited range.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

 */
package ptolemy.actor.parameters;

import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DoubleRangeParameter

/**
 This is a parameter with type double with a limited range and
 limited precision.
 Its value is an double token that is constrained to lie
 within the boundaries specified by its two parameters,
 <i>min</i> and <i>max</i>, inclusive.  Moreover, the values
 are quantized so that there are exactly <i>precision</i>
 values, uniformly spaced in the range.
 A user interface will typically use this
 information to represent the parameter value using a slider
 which can be decorated by labels indicating the minimum and
 maximum values. The actual text displayed by the labels can
 be set using the <i>minLabel</i> and <i>maxLabel</i> parameters
 which default to showing the actual minimum and maximum double value.
 The default values for <i>min</i> and <i>max</i> are 0.0 and 1.0,
 with a default value of 0.5.  The default <i>precision</i> is 100.
 <p>
 @author Edward A. Lee, Christoph Daniel Schulze
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DoubleRangeParameter extends Parameter {
    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DoubleRangeParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        min = new Parameter(this, "min");
        min.setExpression("0.0");
        min.setTypeEquals(BaseType.DOUBLE);

        max = new Parameter(this, "max");
        max.setExpression("1.0");
        max.setTypeEquals(BaseType.DOUBLE);

        precision = new Parameter(this, "precision");
        precision.setExpression("100");
        precision.setTypeEquals(BaseType.INT);

        minLabel = new StringParameter(this, "minLabel");
        minLabel.setExpression("$min");

        maxLabel = new StringParameter(this, "maxLabel");
        maxLabel.setExpression("$max");

        // We can't set a default value because then
        // restore defaults will restore to this value,
        // but this may be out of range for a particular
        // application.
        // setExpression("0.5");
        setTypeEquals(BaseType.DOUBLE);

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-30\" y=\"-2\" "
                + "width=\"60\" height=\"4\" " + "style=\"fill:white\"/>\n"
                + "<rect x=\"15\" y=\"-10\" " + "width=\"4\" height=\"20\" "
                + "style=\"fill:grey\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The maximum value. This is has a double value, and defaults to 1.0. */
    public Parameter max;

    /** The minimum value. This is has a double value, and defaults to 0.0. */
    public Parameter min;

    /** The precision, which is the number of possible values.
     *  This is has an integer value, and defaults to 100.
     */
    public Parameter precision;

    /** The label text displayed for the maximum end of the slider. This is a String,
     *  and defaults to {@code $max}, which is expanded to the value of the {@code max}
     *  parameter in the user interface.
     */
    public StringParameter maxLabel;

    /** The label text displayed for the minimum end of the slider. This is a String,
     *  and defaults to {@code $min}, which is expanded to the value of the {@code min}
     *  parameter in the user interface.
     */
    public StringParameter minLabel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute by ensuring that the current
     *  value remains within the range given by <i>min</i> and <i>max</i>.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == max && !_inCheck) {
            try {
                _inCheck = true;

                double maxValue = ((DoubleToken) max.getToken()).doubleValue();
                double value = ((DoubleToken) getToken()).doubleValue();

                if (value > maxValue) {
                    setToken(max.getToken());
                }
            } finally {
                _inCheck = false;
            }
        } else if (attribute == min && !_inCheck) {
            try {
                _inCheck = true;

                double minValue = ((DoubleToken) min.getToken()).doubleValue();
                double value = ((DoubleToken) getToken()).doubleValue();

                if (value < minValue) {
                    setToken(min.getToken());
                }
            } finally {
                _inCheck = false;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /*  Set the token value and type of the variable, and notify the
     *  container that the value (and type, if appropriate) has changed.
     *  Also notify value dependents that they need to be re-evaluated,
     *  and notify any listeners that have been registered with
     *  addValueListener().
     *  If setTypeEquals() has been called, then attempt to convert
     *  the specified token into one of the appropriate type, if needed,
     *  rather than changing the type.
     *  @param newToken The new value of the variable.
     *  @exception IllegalActionException If the token is not an IntToken
     *   or its value is out of range.
     */
    @Override
    protected void _setTokenAndNotify(Token newToken)
            throws IllegalActionException {
        if (_inCheck) {
            super._setTokenAndNotify(newToken);
            return;
        }

        if (newToken instanceof DoubleToken) {
            try {
                _inCheck = true;

                double minValue = ((DoubleToken) min.getToken()).doubleValue();
                double maxValue = ((DoubleToken) max.getToken()).doubleValue();
                double currentValue = ((DoubleToken) newToken).doubleValue();

                if (minValue <= currentValue && currentValue <= maxValue) {
                    // All is OK.
                    super._setTokenAndNotify(newToken);
                    return;
                }

                throw new IllegalActionException(this,
                        "Value is required to lie between " + min + " and "
                                + max + ".");
            } finally {
                _inCheck = false;
            }
        }

        throw new IllegalActionException(this,
                "Value is required to be a double token.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that we are in the middle of a check, so skip
     *  circular dependency.
     */
    private boolean _inCheck = false;
}

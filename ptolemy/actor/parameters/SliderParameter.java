/* A parameter with type integer with a limited range.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.parameters;

import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// SliderParameter
/**
This is a parameter with type integer with a limited range.
Its value is a record token with three fields, <i>min</i>, <i>max</i>,
and <i>current</i>.  These specify the minimum and maximum values
as well as the current value.  A user interface will typically use this
information to represent the parameter value using a slider.
<p>
@author Edward A. Lee
@version $Id$
*/
public class SliderParameter extends Parameter {

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
    public SliderParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setExpression("{min = 0, max = 100, current = 100}");
        setTypeEquals(getToken().getType());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current value of the slider.
     *  Contrast this to getToken(), which returns the record containing
     *  the min, max, and current value.
     *  @return The current slider value.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public int getCurrentValue() throws IllegalActionException {
        return ((IntToken)((RecordToken)getToken()).get("current")).intValue();
    }

    /** Return the maximum value of the slider.
     *  Contrast this to getToken(), which returns the record containing
     *  the min, max, and current value.
     *  @return The maximum slider value.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public int getMaxValue() throws IllegalActionException {
        return ((IntToken)((RecordToken)getToken()).get("max")).intValue();
    }

    /** Return the minimum value of the slider.
     *  Contrast this to getToken(), which returns the record containing
     *  the min, max, and current value.
     *  @return The maximum slider value.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public int getMinValue() throws IllegalActionException {
        return ((IntToken)((RecordToken)getToken()).get("min")).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /*  Set the token value and type of the variable, and notify the
     *  container that the value (and type, if appropriate) has changed.
     *  Also notify value dependents that they need to be re-evaluated,
     *  and notify any listeners that have been registered with
     *  addValueListener().
     *  If setTypeEquals() has been called, then attempt to convert
     *  the specified token into one of the appropriate type, if needed,
     *  rather than changing the type.
     *  @param newToken The new value of the variable.
     *  @exception IllegalActionException If the token type is not
     *   a properly structured record, or if the current value is outside
     *   of the specified range, or if you are attempting
     *   to set to null a variable that has value dependents.
     */
    protected void _setTokenAndNotify(Token newToken)
            throws IllegalActionException {
        if (newToken instanceof RecordToken) {
            Token min = ((RecordToken)newToken).get("min");
            Token max = ((RecordToken)newToken).get("max");
            Token current = ((RecordToken)newToken).get("current");
            if (min instanceof IntToken
                    && max instanceof IntToken
                    && current instanceof IntToken) {
                int currentValue = ((IntToken)current).intValue();
                if (((IntToken)min).intValue() <= currentValue
                       && currentValue <= ((IntToken)max).intValue()) {
                    // All is OK.
                    super._setTokenAndNotify(newToken);
                    return;
                }
            }
        }
        throw new IllegalActionException(this,
                "Value is required to be a record of form "
                + "{min = m, max = M, current = c}, where min <= c <= max.");
    }
}

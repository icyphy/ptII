/* The base unit of a unit category.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@Pt.ProposedRating Red (liuxj@eecs.berkeley.edu)
@Pt.AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.data.unit;

import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// BaseUnit
/**
The base unit of a unit category.  The category of a base unit is specified
by a unit category property.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 2.0
*/

public class BaseUnit extends Parameter {

    /** Construct a base unit with the given name contained by
     *  the specified entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public BaseUnit(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the token contained by this base unit.  The token contains
     *  the unit information specified by the unit category property.
     *  Calling this method
     *  will trigger evaluation of the expression, if the value has been
     *  given by setExpression(). Notice the evaluation of the expression
     *  can trigger an exception if the expression is not valid, or if the
     *  result of the expression violates type constraints specified by
     *  setTypeEquals() or setTypeAtMost(), or if the result of the expression
     *  is null and there are other variables that depend on this one.
     *  The returned value will be null if neither an expression nor a
     *  token has been set, or either has been set to null.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public Token getToken() throws IllegalActionException {
        Token token = super.getToken();
        if (token != _token) {
            Iterator attributes =
                attributeList(UnitCategory.class).iterator();
            if (attributes.hasNext()) {
                UnitCategory category = (UnitCategory)attributes.next();
//                 token = ptolemy.data.expr.UtilityFunctions.baseUnit(
//                         token, getName());
                String name = getName();
                UnitUtilities.registerUnitCategory(name);
                int index =
                    UnitUtilities.getUnitCategoryIndex(name);
                if (index >= 0) {
                    ((ScalarToken)token).setUnitCategory(index);
                }
            }
            _token = token;
        }
        return _token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The token that has unit exponents set correctly.
    private Token _token = null;

}

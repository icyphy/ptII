/* Monotonic function that extracts the type of a field from an associative type.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.AssociativeType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////ExtractFieldType

/**
 A partial monotonic function of the given port that returns a type
 associated with the given field name, to be found in the type definition
 of the port.
 <p>
 The <code>getValue()</code> method is used to get the current value of the
 function. If the port type is an <code>AssociateType</code> with a field for
 the specified field name, then the function returns the type of that field.
 If the port type is <code>BaseType.GENERAL</code>, then return <code>
 BaseType.GENERAL</code>, or if the type is <code>BaseType.UNKNOWN</code>,
 then return <code>BaseType.UNKNOWN</code>.
 If the port type is <code>AssociateType</code> but it has no
 corresponding field, then return <code>BaseType.GENERAL</code>.
 Otherwise, the getValue() method throws an exception, which makes the
 function partial.
 </p>
 @author Edward A. Lee, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (marten)
 @Pt.AcceptedRating Red
 */
public class ExtractFieldType extends MonotonicFunction {

    /** Construct a new monotonic function.
     *  @param port The port of which the type is extracted the field from
     *  @param name The name of the field of interest
     */
    public ExtractFieldType(TypedIOPort port, String name) {
        _name = name;
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   public inner methods                ////

    /** Return the current value of this monotonic function.
     *  Specifically, this is a function of one variable, the type variable
     *  of the given port. If the port type is <code>BaseType.GENERAL</code>,
     *  then return <code>BaseType.GENERAL</code>, or if the type is
     *  <code>BaseType.UNKNOWN</code>, then return
     *  <code>BaseType.UNKNOWN</code>. If the port type is
     *  <code>AssociateType</code> but it has no corresponding field,
     *  then return <code>BaseType.GENERAL</code>.
     *  Otherwise, throw an exception.
     *  @return A Type.
     *  @exception IllegalActionException If the port type is
     *   not <code>BaseType.UNKNOWN</code> or <code>BaseType.GENERAL</code>
     *   and is not an instance of <code>AssociateType</code>.
     */
    @Override
    public Object getValue() throws IllegalActionException {
        Type portType;

        if (_port.getTypeTerm().isSettable()) {
            portType = (Type) _port.getTypeTerm().getValue();
        } else {
            portType = _port.getType();
        }

        // If the port type was declared as or did resolve to GENERAL,
        // return GENERAL
        if (portType.equals(BaseType.GENERAL)) {
            return BaseType.GENERAL;
        }

        // If the port type was declared as or did resolve to UNKNOWN,
        // return UNKNOWN
        if (portType.equals(BaseType.UNKNOWN)) {
            return BaseType.UNKNOWN;
        }

        // Extract the field type if the port type is a associative type,
        // or else throw an exception
        if (portType instanceof AssociativeType) {
            Type fieldType = ((AssociativeType) portType).get(_name);

            // The field is missing from the associative type
            if (fieldType == null) {
                return BaseType.GENERAL; // NOTE: to ensure monotonicity
            } else {
                return fieldType;
            }
        } else {
            throw new IllegalActionException(_port,
                    "Invalid type for given port: " + portType);
        }
    }

    /** Return an additional string describing the current value
     *  of this function.
     */
    @Override
    public String getVerboseString() {
        if (_port.getType() instanceof AssociativeType) {
            AssociativeType type = (AssociativeType) _port.getType();
            Type fieldType = type.get(_name);

            if (fieldType == null) {
                return "AssociativeType doesn't have field named " + _name;
            }
        }

        return null;
    }

    /** Return the type variables in this inequality term. If the
     *  type of the input port is not declared, return a one
     *  element array containing the inequality term representing
     *  the type of the port; otherwise, return an empty array.
     *  @return An array of InequalityTerm.
     */
    @Override
    public InequalityTerm[] getVariables() {
        InequalityTerm portTerm = _port.getTypeTerm();

        if (portTerm.isSettable()) {
            InequalityTerm[] variable = new InequalityTerm[1];
            variable[0] = portTerm;
            return variable;
        }

        return new InequalityTerm[0];
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner variable               ////

    /** The name of the field of interest. */
    private String _name;

    /** The port of which the type is extracted the field type from. */
    private TypedIOPort _port;

}

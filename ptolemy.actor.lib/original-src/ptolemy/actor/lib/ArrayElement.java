/* Extract an element from an array.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Typeable;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayElement

/**
 Extract an element from an array.  This actor reads an array from the
 <i>input</i> port and sends one of its elements to the <i>output</i>
 port.  The element that is extracted is determined by the
 <i>index</i> parameter (or port).  It is required that
 0 &lt;= <i>index</i> &lt; <i>N</i>, where <i>N</i> is the
 length of the input array, or
 an exception will be thrown by the fire() method.

 @see LookupTable
 @see RecordDisassembler
 @author Edward A. Lee, Elaine Cheong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (cxh)
 */
public class ArrayElement extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayElement(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set type constraints.
        output.setTypeAtLeast(ArrayType.elementType(input));

        // Set parameters.
        index = new PortParameter(this, "index");
        index.setTypeEquals(BaseType.INT);
        index.setExpression("0");
        new Parameter(index.getPort(), "_showName", BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The index into the input array.  This is an integer that
     *  defaults to 0, and is required to be less than or equal to the
     *  length of the input array. If the port is left unconnected,
     *  then the parameter value will be used.
     */
    public PortParameter index;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayElement.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayElement newObject = (ArrayElement) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.input));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Force the type of the input to be an array with fields of a type
     *  greater than or equal to the type of the output port.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()) {
            result.add(new Inequality(new ArrayTypeFunction(output), input
                    .getTypeTerm()));
        }
        return result;
    }

    /** Consume at most one array from the input port and produce
     *  one of its elements on the output port.  If there is no token
     *  on the input, then no output is produced.
     *  @exception IllegalActionException If the <i>index</i> parameter
     *   (or port value) is out of range.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: This has be outside the if because we need to ensure
        // that if an index token is provided that it is consumed even
        // if there is no input token.
        index.update();

        int indexValue = ((IntToken) index.getToken()).intValue();

        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);

            if (indexValue < 0 || indexValue >= token.length()) {
                throw new IllegalActionException(this, "index " + indexValue
                        + " is out of range for the input "
                        + "array, which has length " + token.length());
            }

            output.send(0, token.getElement(indexValue));
        }
    }

    /** A monotonic function that returns an array type of which the
     *  fields have the same type as the provided argument.
     *  @author Marten Lohstroh
     */
    private static class ArrayTypeFunction extends MonotonicFunction {

        /** Construct a ArrayElementTypeFunction whose argument is the
         *  type of the specified object.
         *  @param typeable A Typeable object.
         */
        public ArrayTypeFunction(Typeable typeable) {
            _typeable = typeable;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return the current value of this monotonic function.
         *  @return A Type.
         *  @exception IllegalActionException If the type of the argument
         *   cannot be determined.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            return new ArrayType(_typeable.getType());
        }

        /** Return the type variables for this function, which is
         *  the type term of the specified typeable, unless it has a constant type,
         *  in which case return an empty array.
         *  @return An array of InequalityTerms.
         */
        @Override
        public InequalityTerm[] getVariables() {
            InequalityTerm term = _typeable.getTypeTerm();
            if (term.isSettable()) {
                InequalityTerm[] result = new InequalityTerm[1];
                result[0] = term;
                return result;
            } else {
                return new InequalityTerm[0];
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** The argument. */
        private Typeable _typeable;

    }

}

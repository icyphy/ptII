/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.actor.util;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ArrayElementTypeFunction

/** This class implements a monotonic function that returns the element
 *  type of its argument, if the argument is an array type. If the
 *  argument is general, then it returns general.  Otherwise, it return unknown.
 * @author Edward A. Lee
 * @version $Id: ArrayElementTypeFunction.java$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ArrayElementTypeFunction extends MonotonicFunction {

    /** Construct a ArrayElementTypeFunction whose argument is the
     *  type of the specified object.
     *  @param typeable A Typeable object.
     */
    public ArrayElementTypeFunction(Typeable typeable) {
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
        Type type = _typeable.getType();
        if (type instanceof ArrayType) {
            return ((ArrayType) type).getElementType();
        }
        if (type.equals(BaseType.GENERAL)) {
            return BaseType.GENERAL;
        }
        return BaseType.UNKNOWN;
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
            return EMPTY_TERM_ARRAY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The argument. */
    private Typeable _typeable;

    private static InequalityTerm[] EMPTY_TERM_ARRAY = new InequalityTerm[0];
}

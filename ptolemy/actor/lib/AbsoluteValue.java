/* An actor that outputs the absolute value of the input.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.graph.InequalityTerm;
import ptolemy.data.*;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// AbsoluteValue
/**
Produce an output token on each firing with a value that is
equal to the absolute value of the input. The input can have any
scalar type. If the input type is not Complex, the output has the
same type as the input. If the input type is Complex, the output
type is Double, in which case, the output value is the magnitude
of the input complex.

@author Edward A. Lee
@version $Id$
*/

public class AbsoluteValue extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AbsoluteValue(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	output.setTypeAtLeast(new FunctionTerm(input));
	output.setTypeAtMost(BaseType.SCALAR);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        AbsoluteValue newobj = (AbsoluteValue)super.clone(ws);
	newobj.output.setTypeAtLeast(new FunctionTerm(newobj.input));
	newobj.output.setTypeAtMost(BaseType.SCALAR);

        return newobj;
    }

    /** Return the following type constraints: If the input type is Complex,
     *  the output type is no less than Double, otherwise, the output type
     *  is no less than the input; The output type is no greater than Scalar.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
	// type constraints are stored in the output port.
	return output.typeConstraintList();
    }

    /** Compute the absolute value of the input.  If there is no input, then
     *  produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ScalarToken in = (ScalarToken)input.get(0);
            output.send(0, in.absolute());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class FunctionTerm implements InequalityTerm {

	// The constructor takes a port argument so that the clone()
	// method can construct an instance of this class for the
	// input port on the clone.
	private FunctionTerm(TypedIOPort port) {
	    _port = port;
	}

	///////////////////////////////////////////////////////////////
	////                       public inner methods            ////

	/** Do nothing.
	 */
	public void fixValue() {
	}

	/** Return null.
	 *  @return null.
	 */
	public Object getAssociatedObject() {
	    return null;
	}

	/** Return the function result.
	 *  @return A Type.
	 */
	public Object getValue() {
	    Type inputType = _port.getType();
	    return inputType == BaseType.COMPLEX ? BaseType.DOUBLE : inputType;
        }

        /** Return a one element array containing the InequalityTerm
	 *  representing the type of the input port.
	 *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
	    InequalityTerm[] variable = new InequalityTerm[1];
	    variable[0] = _port.getTypeTerm();
	    return variable;
        }

        /** Throw an Exception.
         *  @exception IllegalActionException If we call initialize on
         *  a function term.  Always thrown in this class.
         */
        public void initialize(Object e)
		throws IllegalActionException {
	    throw new IllegalActionException("AbsoluteValue$FunctionTerm." +
                    "initialize: Cannot initialize a function term.");
        }

        /** Return false.
         *  @return false.
         */
        public boolean isSettable() {
	    return false;
        }

        /** Return true.
         *  @return True.
         */
        public boolean isValueAcceptable() {
            return true;
        }

        /** Throw an Exception.
         *  @exception IllegalActionException If the type is not settable.
         *  Always thrown in this class.
         */
        public void setValue(Object e)
		throws IllegalActionException {
	    throw new IllegalActionException(
                    "AbsolutionValue$FunctionTerm.setValue: The type is not " +
                    "settable.");
        }

        /** Override the base class to give a description of this term.
         *  @return A description of this term.
         */
        public String toString() {
            return "(AbsoluteValue$FunctionTerm, " + getValue() + ")";
        }

	/** Do nothing.
	 */
	public void unfixValue() {
	}

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

	private TypedIOPort _port;
    }
}

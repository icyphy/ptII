/* A pulse source.

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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.graph.*;

//////////////////////////////////////////////////////////////////////////
//// Pulse
/**
Produce a pulse with a shape specified by the parameters.
The <i>values</i> parameter contains an ArrayToken, which specifies
the sequence of values to produce at the output.  The <i>indexes</i>
parameter contains an IntMatrixToken, which specifies when those values
should be produced.  The IntMatrixToken in the <i>indexes</i> parameter
must be a row vector with the same length as ArrayToken in the
<i>values</i> parameter or an exception will be thrown by the fire() method.
<p>
The <i>indexes</i> vector must be increasing and non-negative,
or an exception will be thrown when it is set.
<p>
Eventually, this actor will support various kinds of interpolation.
For now, it outputs a zero (of the same type as the values) whenever
the iteration count does not match an index in <i>indexes</i>.
<p>
The default for the <i>values</i> parameter is
an integer vector of form [1, 0].
The default indexes matrix is [0, 1].
Thus, the default output sequence will be 1, 0, 0, ...
<p>
However, the Pulse actor has a <I>repeat</i> parameter. When set to
true, the defined sequence is repeated indefinitely. Otherwise, the
default sequence of zero values result.
<p>
The type of the output can be any token type. This type is inferred
from the element type of the <i>values</i> parameter.
<p>
NOTE: A reset input for this actor would be useful.  This would reset
the iterations count, to cause the pulse to emerge again.
@author Edward A. Lee
@version $Id$
*/

public class Pulse extends SequenceSource {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Pulse(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        indexes = new Parameter(this, "indexes", _defaultIndexToken);
        indexes.setTypeEquals(BaseType.INT_MATRIX);
        // Call this so that we don't have to copy its code here...
        attributeChanged(indexes);

	// set values parameter
	IntToken[] defaultValues = new IntToken[2];
	defaultValues[0] = new IntToken(1);
	defaultValues[1] = new IntToken(0);
	ArrayToken defaultValueToken = new ArrayToken(defaultValues);
        values = new Parameter(this, "values", defaultValueToken);
	values.setTypeEquals(new ArrayType(BaseType.NAT));

        // Set the Repeat Flag.
        repeat = new Parameter(this, "repeat", new BooleanToken(false));
	repeat.setTypeEquals(BaseType.BOOLEAN);
        attributeChanged(repeat);

	// set type constraint
	ArrayType valuesArrayType = (ArrayType)values.getType();
	InequalityTerm elemTerm = valuesArrayType.getElementTypeTerm();
	output.setTypeAtLeast(elemTerm);

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);
        _zero = new IntToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The indexes at which the specified values will be produced.
     *  This parameter must contain an IntMatrixToken with one row.
     */
    public Parameter indexes;

    /** The values that will be produced at the specified indexes.
     *  This parameter must contain an ArrayToken.
     */
    public Parameter values;

    /** The flag that indicates whether the pulse sequence needs to be
     *  repeated.
     */
    public Parameter repeat;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>indexes</i>, then check
     *  that it is increasing and nonnegative, and that it has exactly
     *  one row.
     *  @exception IllegalActionException If the indexes vector is not
     *   increasing and nonnegative, or the indexes is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == indexes) {
            int[][] idx =
                ((IntMatrixToken)indexes.getToken()).intMatrix();
            if (idx.length != 1) {
                throw new IllegalActionException(this,
                        "Cannot set indexes parameter to a non-row vector.");
            }
            int previous = -1;
            for (int j = 0; j < idx[0].length; j++) {
                if (idx[0][j] <= previous) {
                    throw new IllegalActionException(this,
                            "Value of indexes must be an array of nonnegative "
                            + "integers increasing in value.");
                }
                previous = idx[0][j];
            }
        } else {
            if (attribute == repeat) {
                _repeatFlag = ((BooleanToken)repeat.getToken()).booleanValue();
            } else {
                super.attributeChanged(attribute);
            }
        }
    }

    /** Notify the director that a type change has occurred that may
     *  affect the type of the output.
     *  This will cause type resolution to be redone at the next opportunity.
     *  It is assumed that type changes in the parameters are implemented
     *  by the director's change request mechanism, so they are implemented
     *  when it is safe to redo type resolution.
     *  If there is no director, then do nothing.
     *  @exception IllegalActionException If the new values array has no
     *   elements in it.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == values) {
            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateResolvedTypes();
            }
            try {
                ArrayToken valuesArray = (ArrayToken)values.getToken();
                Token prototype = valuesArray.getElement(0);
                _zero = prototype.zero();
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalActionException(this,
                        "Cannot set values to an empty array.");
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                        "Cannot set values to something that is not an array: "
                        + values.getToken());
            }
        } else {
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        Pulse newobj = (Pulse)super.clone(ws);
        newobj.indexes = (Parameter)newobj.getAttribute("indexes");
        try {
            newobj.indexes = (Parameter)newobj.getAttribute("indexes");
            newobj.attributeChanged(newobj.indexes);
            newobj.values = (Parameter)newobj.getAttribute("values");
            newobj.attributeChanged(newobj.values);
            // set the type constraints
	    ArrayType valuesArrayType = (ArrayType)newobj.values.getType();
	    InequalityTerm elemTerm = valuesArrayType.getElementTypeTerm();
	    newobj.output.setTypeAtLeast(elemTerm);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return newobj;
    }

    /** Output a value if the count of iterations matches one of the entries
     *  in the indexes array.
     *  Otherwise output a zero token with the same type as the values in
     *  the value array.
     *  @exception IllegalActionException If the values and indexes parameters
     *   do not have the same dimension, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int currentIndex = 0;
        ArrayToken val = (ArrayToken)values.getToken();
        int[][] idx = ((IntMatrixToken)indexes.getToken()).intMatrix();
        if (_indexColCount < idx[0].length) {
            if (val.length() != idx[0].length) {
                throw new IllegalActionException(this,
                        "Parameters values and indexes have " +
                        "different lengths.");
            }
            currentIndex = idx[0][_indexColCount];
            if (_iterationCount == currentIndex) {
                // Got a match with an index.
                output.send(0, val.getElement(_indexColCount));
                _match = true;
                return;
            }
        } else {
            if ( _repeatFlag ) {

                // Repeat the pulse sequence again.
                _iterationCount = 0;
                _indexColCount = 0;

                currentIndex = idx[0][_indexColCount];
                if (_iterationCount == currentIndex) {
                    output.send(0, val.getElement(_indexColCount));
                    _match = true;
                }
                return;
            }
        }
        output.send(0, _zero);
        _match = false;

    }

    /** Set the iteration count to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
        _indexColCount = 0;
    }

    /** Update the iteration counters until they exceed the values
     *  in the indexes vector.
     *  @exception IllegalActionException If the expression of indexes
     *   is not valid.
     */
    public boolean postfire() throws IllegalActionException {
        int[][] idx = ((IntMatrixToken)indexes.getToken()).intMatrix();
        // We stop incrementing after reaching the top of the indexes
        // vector to avoid possibility of overflow.
        if (_iterationCount <= idx[0][idx[0].length-1]) {
            ++_iterationCount;
        }
        if (_match) {
            ++_indexColCount;
        }
        return super.postfire();
    }

    /** Start an iteration.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        _match = false;
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of the iterations.  This stops incrementing when
    // we exceed the top of the indexes vector.
    private int _iterationCount = 0;

    // Index of the next output in the values array.
    private int _indexColCount = 0;

    // Zero token of the same type as in the values array.
    private Token _zero;

    // Indicator of whether the iterations count matches one of the indexes.
    private boolean _match = false;

    // Default value of the indexes.
    private int _defaultIndexes[][] = {
        {0, 1}
    };

    // Default value of the indexes parameter.
    private IntMatrixToken _defaultIndexToken =
    new IntMatrixToken(_defaultIndexes);

    // Flag to indicate whether or not to repeat the sequence.
    private boolean _repeatFlag;

}

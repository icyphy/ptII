/* A pulse source.

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

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Pulse

/**
 Produce a pulse with a shape specified by the parameters.
 The <i>values</i> parameter contains an ArrayToken, which specifies
 the sequence of values to produce at the output.  The <i>indexes</i>
 parameter contains an array of integers, which specifies when those values
 should be produced.  The array in the <i>indexes</i> parameter
 must have the same length as that in the
 <i>values</i> parameter or an exception will be thrown by the fire() method.
 Also, the <i>indexes</i> array must be increasing and non-negative,
 or an exception will be thrown when it is set.
 <p>
 Eventually, this actor will support various kinds of interpolation.
 For now, it outputs a zero (of the same type as the values) whenever
 the iteration count does not match an index in <i>indexes</i>.
 <p>
 The default for the <i>values</i> parameter is
 an integer vector of form {1, 0}.
 The default indexes array is {0, 1}.
 Thus, the default output sequence will be 1, 0, 0, ...
 <p>
 However, the Pulse actor has a <I>repeat</i> parameter. When set to
 true, the defined sequence is repeated indefinitely. Otherwise, the
 default sequence of zero values result.
 <p>
 The type of the output can be any token type. This type is inferred
 from the element type of the <i>values</i> parameter.
 <p>The Ptolemy Expression language has several constructs that are
 useful for creating arrays for use as values or indexes:
 <dl>
 <dt><code>[0:1:100].toArray()</code>
 <dd>Matlab style array construction that creates an array of 100 elements,
 0 through 99.
 <dt><code>repeat(100, {1}(0))</code>
 <dd>Creat a sequence of one hundred 1's.
 </dl>
 <p>
 NOTE: A reset input for this actor would be useful.  This would reset
 the iterations count, to cause the pulse to emerge again.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
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
    public Pulse(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        indexes = new Parameter(this, "indexes");
        indexes.setExpression("{0, 1}");
        indexes.setTypeEquals(new ArrayType(BaseType.INT));

        // Call this so that we don't have to copy its code here...
        attributeChanged(indexes);

        // set values parameter
        values = new Parameter(this, "values");
        values.setExpression("{1, 0}");

        // Set the Repeat Flag.
        repeat = new Parameter(this, "repeat", new BooleanToken(false));
        repeat.setTypeEquals(BaseType.BOOLEAN);
        attributeChanged(repeat);

        // set type constraint
        output.setTypeAtLeast(ArrayType.elementType(values));

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);

        // Show the firingCountLimit parameter last.
        firingCountLimit.moveToLast();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The indexes at which the specified values will be produced.
     *  This parameter is an array of integers, with default value {0, 1}.
     */
    public Parameter indexes;

    /** The flag that indicates whether the pulse sequence needs to be
     *  repeated. This is a boolean, and defaults to false.
     */
    public Parameter repeat;

    /** The values that will be produced at the specified indexes.
     *  This parameter is an array, with default value {1, 0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>indexes</i>, then check
     *  that it is increasing and nonnegative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the indexes vector is not
     *   increasing and nonnegative, or the indexes is not a row vector.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == indexes) {
            ArrayToken indexesValue = (ArrayToken) indexes.getToken();
            _indexes = new int[indexesValue.length()];

            int previous = 0;

            for (int i = 0; i < indexesValue.length(); i++) {
                _indexes[i] = ((IntToken) indexesValue.getElement(i))
                        .intValue();

                // Check nondecreasing property.
                if (_indexes[i] < previous) {
                    throw new IllegalActionException(this,
                            "Value of indexes is not nondecreasing "
                                    + "and nonnegative.");
                }

                previous = _indexes[i];
            }
        } else if (attribute == values) {
            try {
                ArrayToken valuesArray = (ArrayToken) values.getToken();
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
        } else if (attribute == repeat) {
            _repeatFlag = ((BooleanToken) repeat.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This overrides the
     *  base class to handle type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Pulse newObject = (Pulse) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.values));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        newObject._indexes = new int[_indexes.length];
        System.arraycopy(_indexes, 0, newObject._indexes, 0, _indexes.length);

        try {
            ArrayToken valuesArray = (ArrayToken) newObject.values.getToken();
            Token prototype = valuesArray.getElement(0);
            newObject._zero = prototype.zero();
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
        return newObject;
    }

    /** Output a value if the count of iterations matches one of the entries
     *  in the indexes array.
     *  Otherwise output a zero token with the same type as the values in
     *  the value array.
     *  @exception IllegalActionException If the values and indexes parameters
     *   do not have the same length, or if there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int currentIndex = 0;
        ArrayToken val = (ArrayToken) values.getToken();

        if (_indexColCount < _indexes.length) {
            if (val.length() != _indexes.length) {
                throw new IllegalActionException(this,
                        "Parameters values and indexes have "
                                + "different lengths.  Length of values = "
                                + val.length() + ". Length of indexes = "
                                + _indexes.length + ".");
            }

            currentIndex = _indexes[_indexColCount];

            if (_iterationCount == currentIndex) {
                // Got a match with an index.
                output.send(0, val.getElement(_indexColCount));
                _match = true;
                return;
            }
        } else {
            if (_repeatFlag) {
                // Repeat the pulse sequence again.
                _iterationCount = 0;
                _indexColCount = 0;

                currentIndex = _indexes[_indexColCount];

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
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
        _indexColCount = 0;
    }

    /** Update the iteration counters until they exceed the values
     *  in the indexes array.
     *  @exception IllegalActionException If the expression of indexes
     *   is not valid.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // We stop incrementing after reaching the top of the indexes
        // vector to avoid possibility of overflow.
        if (_iterationCount <= _indexes[_indexes.length - 1]) {
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
    @Override
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

    // Cache of indexes array value.
    private transient int[] _indexes;

    // Zero token of the same type as in the values array.
    private Token _zero;

    // Indicator of whether the iterations count matches one of the indexes.
    private boolean _match = false;

    // Flag to indicate whether or not to repeat the sequence.
    private boolean _repeatFlag;
}

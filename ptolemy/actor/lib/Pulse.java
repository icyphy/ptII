/* A pulse source.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;

//////////////////////////////////////////////////////////////////////////
//// Pulse
/**
Produce a pulse with a shape specified by the parameters.
The <i>values</i> parameter specifies the sequence of values
to produce at the output.  The <i>indexes</i> parameter specifies
when those values should be produced.  These two parameters
must both contain matrices with the same dimensions or an
exception will be thrown by the fire() method.
<p>
The <i>values</i> and <i>indexes</i> parameters are two-dimensional
matrices.  They are read row first; i.e., the first row is read,
then the second, etc.  This actor counts iterations, and when the
iterations match a value in the <i>indexes</i> matrix, then
the corresponding value (at the same position in the matrix)
from the <i>values</i> array is produced at the output.
<p>
The <i>indexes</i> array must be increasing and non-negative,
or an exception will be thrown when it is set.
<p>
Eventually, this actor will support various kinds of interpolation.
For now, it outputs a zero (of the same type as the values) whenever
the iteration count does not match an index in <i>indexes</i>.
<p>
The <i>values</i> parameter must be set to
a MatrixToken value, or an exception will be thrown.
If it is not set, then by default it has a value that is
an IntMatrix of form {{1,0}} (one row, two columns, with
values 1 and 0).  The default indexes matrix is {{0,1}}.
Thus, the default output sequence will be 1, 0, 0, ...
<p>
The type of the output can be any token type that has a corresponding
matrix token type.  The type is inferred from the type of the
<i>values</i> parameter.

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

        indexes = new Parameter(this, "indexes", defaultIndexToken);
        indexes.setTypeEquals(IntMatrixToken.class);
        // Call this so that we don't have to copy its code here...
        attributeChanged(indexes);
        values = new Parameter(this, "values", defaultValueToken);
        // Call this so that we don't have to copy its code here...
        attributeChanged(values);
        _zero = new IntToken(0);
        _dummy = new Variable(this, "_dummy", _zero);
	output.setTypeAtLeast(_dummy);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The indexes at which the specified values will be produced.
     *  This parameter must contain an IntMatrixToken.
     */
    public Parameter indexes;

    /** The values that will be produced at the specified indexes.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>indexes</i>, then check
     *  that it is increasing and nonnegative.
     *  @exception IllegalActionException If the indexes array is not
     *   increasing and nonnegative.
     */
// FIXME: Change this so it only accepts row vectors...
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == indexes) {
            int[][] idx =
                   ((IntMatrixToken)indexes.getToken()).intMatrix();
            int previous = -1;
            for (int i=0; i<idx.length; i++) {
                for (int j=0; j<idx[i].length; j++) {
                    if (idx[i][j] <= previous) {
                        throw new IllegalActionException(this,
                        "Value of indexes must be an array of nonnegative "
                        + "integers increasing in value.");
                    }
                    previous = idx[i][j];
                }
            }
        } else {
            super.attributeChanged(attribute);
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
     *   elements in it, or if it is not a MatrixToken.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == values) {
            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateResolvedTypes();
            }
            try {
                MatrixToken valuesArray = (MatrixToken)values.getToken();
                Token prototype = valuesArray.getElementAsToken(0,0);
                _dummy.setToken(prototype);
                _zero = prototype.zero();
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalActionException(this,
                "Cannot set values to an empty array.");
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                "Cannot set values to something that is not an array: "
                + values.getToken());
            }
        } else if (attribute != _dummy) {
            // Notice that type changes to _dummy are allowed...
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Pulse newobj = (Pulse)super.clone(ws);
        newobj.indexes = (Parameter)newobj.getAttribute("indexes");
        try {
            newobj.indexes = (Parameter)newobj.getAttribute("indexes");
            newobj.attributeChanged(newobj.indexes);
            newobj.values = (Parameter)newobj.getAttribute("values");
            newobj.attributeChanged(newobj.values);
            // set the type constraints.
            MatrixToken val = (MatrixToken)(newobj.values.getToken());
            if (val != null && val.getRowCount() > 0 &&
                    val.getColumnCount() > 0) {
                MatrixToken valuesArray = (MatrixToken)newobj.values.getToken();
                Token tok = valuesArray.getElementAsToken(0,0);
                newobj._zero = tok.zero();
            } else {
                newobj._zero = new IntToken(0);
            }
            newobj._dummy = (Variable)(newobj.getAttribute("_dummy"));
            newobj._dummy.setToken(_zero);
            newobj.output.setTypeAtLeast(newobj._dummy);
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
        MatrixToken val = (MatrixToken)values.getToken();
        int[][] idx =
               ((IntMatrixToken)indexes.getToken()).intMatrix();
        if (val.getRowCount() != idx.length) {
            throw new IllegalActionException(this,
            "Parameters values and indexes must be arrays "
            + "of the same dimension.");
        }
        if (_indexRowCount < idx.length &&
                _indexColCount < idx[_indexRowCount].length) {
            if (val.getColumnCount() != idx[_indexRowCount].length) {
                throw new IllegalActionException(this,
                "Parameters values and indexes must be arrays "
                + "of the same dimension.");
            }
            int currentIndex = idx[_indexRowCount][_indexColCount];
            if (_iterationCount == currentIndex) {
                // Got a match with an index.
                output.broadcast(val.getElementAsToken(
                    _indexRowCount,_indexColCount));
                _match = true;
                return;
            }
        }
        output.broadcast(_zero);
        _match = false;
    }

    /** Set the iteration count to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
        _indexRowCount = 0;
        _indexColCount = 0;
    }

    /** Update the iteration counters.
     */
    public boolean postfire() {
        MatrixToken val = (MatrixToken)values.getToken();
        ++_iterationCount;
        if (_match) {
            ++_indexColCount;
            if (_indexColCount >= val.getColumnCount()) {
                _indexColCount = 0;
                ++_indexRowCount;
            }
        }
        return super.postfire();
    }

    /** Start an interation.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        _match = false;
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: What happens when these overflow?
    private int _iterationCount = 0;
    private int _indexRowCount = 0;
    private int _indexColCount = 0;

    private Token _zero;
    private boolean _match = false;

    private int defaultIndexes[][] = {
        {0, 1}
    };

    private IntMatrixToken defaultIndexToken =
            new IntMatrixToken(defaultIndexes);

    private int defaultValues[][] = {
        {1,0}
    };

    private IntMatrixToken defaultValueToken =
            new IntMatrixToken(defaultValues);

    // Dummy variable which reflects the type of the elements of the
    // values parameter, so that the output type can be related to it.
    private Variable _dummy;
}


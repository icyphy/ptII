/* Calculates the discrete cosine transform of a 2x2 integer block.

Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.video;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DCT2x2dc

/**
   Calculate the discrete cosine transform(DCT) of dc coefficients of U and V components.
   It uses 2x2 integer transform using in H.264 standard.
   Input block should have two dimensions by 2x2 integer block.
   The output port will have a 2x2 integer block of the result.

   @author Hwayong Oh
   @version $Id: DCT2x2dc.java
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red
 */
public class DCT2x2dc extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DCT2x2dc(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT_MATRIX);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT_MATRIX);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Input for tokens. This is a 2x2 integer block, and its
     * type is integer matrix.
     */
    public TypedIOPort input;

    /** Output port. This is a result for calculating the DCT
     * of the 2x2 integer block.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints on the ports.
     *  @param workspace The workspace into which to clone.
     *  @return A new instance of AddSubtract.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DCT2x2dc newObject = (DCT2x2dc) super.clone(workspace);
        newObject.input.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.output);

        return newObject;
    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _output = new IntMatrixToken[1];
    }

    /** .
     *
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int[][] temp = new int[2][2];
        int[][] sum = new int[2][2];
        IntMatrixToken _input;

        if (input.hasToken(0)) {
            _input = (IntMatrixToken) input.get(0);
        } else {
            return;
        }

        temp[0][0] = _input.getElementAt(0, 0) + _input.getElementAt(0, 1);
        temp[1][0] = _input.getElementAt(0, 0) - _input.getElementAt(0, 1);
        temp[0][1] = _input.getElementAt(1, 0) + _input.getElementAt(1, 1);
        temp[1][1] = _input.getElementAt(1, 0) - _input.getElementAt(1, 1);

        sum[0][0] = temp[0][0] + temp[0][1];
        sum[1][0] = temp[1][0] + temp[1][1];
        sum[0][1] = temp[0][0] - temp[0][1];
        sum[1][1] = temp[1][0] - temp[1][1];

        _output[0] = new IntMatrixToken(sum);
        output.send(0, _output, _output.length);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IntMatrixToken[] _output;

}

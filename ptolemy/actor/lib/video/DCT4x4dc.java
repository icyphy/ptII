/* Calculates the discrete cosine transform of a 4x4 integer block.

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
//// DCT4x4dc

/**
   Calculate the discrete cosine transform(DCT) of dc coefficients of Y components.
   It uses 2x2 integer transform using in H.264 standard.
   Input block should have two dimensions by 4x4 integer block.
   The output port will have a 4x4 integer block of the result.

   @author Hwayong Oh
   @version $Id: DCT4x4dc.java
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red
 */
public class DCT4x4dc extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DCT4x4dc(CompositeEntity container, String name)
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
        DCT4x4dc newObject = (DCT4x4dc) super.clone(workspace);
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

        int[][] temp = new int[4][4];
        int i;
        int s01, s23;
        int d01, d23;
        int[][] sum = new int[4][4];
        IntMatrixToken _input;

        if (input.hasToken(0)) {
            _input = (IntMatrixToken) input.get(0);
        } else {
            return;
        }

        for (i = 0; i < 4; i++) {
            s01 = _input.getElementAt(i, 0) + _input.getElementAt(i, 1);
            d01 = _input.getElementAt(i, 0) - _input.getElementAt(i, 1);
            s23 = _input.getElementAt(i, 2) + _input.getElementAt(i, 3);
            d23 = _input.getElementAt(i, 2) - _input.getElementAt(i, 3);

            temp[0][i] = s01 + s23;
            temp[1][i] = s01 - s23;
            temp[2][i] = d01 - d23;
            temp[3][i] = d01 + d23;
        }

        for (i = 0; i < 4; i++) {
            s01 = temp[i][0] + temp[i][1];
            d01 = temp[i][0] - temp[i][1];
            s23 = temp[i][2] + temp[i][3];
            d23 = temp[i][2] - temp[i][3];

            sum[i][0] = s01 + s23 + 1 >> 1;
        sum[i][1] = s01 - s23 + 1 >> 1;
        sum[i][2] = d01 - d23 + 1 >> 1;
        sum[i][3] = d01 + d23 + 1 >> 1;
        }

        _output[0] = new IntMatrixToken(sum);
        output.send(0, _output, _output.length);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IntMatrixToken[] _output;

}

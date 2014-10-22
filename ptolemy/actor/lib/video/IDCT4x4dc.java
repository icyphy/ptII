/* A Sum of Absolute Difference between two image blocks.

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

// Imports go here, in alphabetical order, with no wildcards.
///////////////////////////////////////////////////////////////////
//// DCT4x4dc

/**
   Calculate the sum of absolute difference(SAD) between two blocks.
   Each input block should have two dimensions by rectangle block(16 by 16, 8 by 8, etc.).
   The output port will have the result of SAD.

   @author Hwayong Oh
   @version $Id: DCT4x4dc.java,v 0.2
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (oh)
   @Pt.AcceptedRating Red (oh)
 */
public class IDCT4x4dc extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IDCT4x4dc(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT_MATRIX);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT_MATRIX);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Input for tokens to be a part of previous frame. This is a single port, and its
     * type is 2D array.
     */
    public TypedIOPort input;

    /** Output port. This is a result for calculating sum of absolute differences between
     * ImgBlockA and ImgBlockB.
     * The type is inferred form the connections.
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
        IDCT4x4dc newObject = (IDCT4x4dc) super.clone(workspace);
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

            sum[i][0] = s01 + s23;
            sum[i][1] = s01 - s23;
            sum[i][2] = d01 - d23;
            sum[i][3] = d01 + d23;
        }

        _output[0] = new IntMatrixToken(sum);
        output.send(0, _output, _output.length);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IntMatrixToken[] _output;

}

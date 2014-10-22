/* 8x8 Discrete Cosine Transform.

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
//// DCT8x8

/**
   Calculate the DCT of a 8x8 block which can be one of Y, U or V pixel data.


   @author Hwayong Oh
   @version $Id: DCT8x8.java
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red
 */
public class DCT8x8 extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DCT8x8(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT_MATRIX);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT_MATRIX);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Input for tokens to be a part of image blocks. This is a single port, and its
     * type is integer matrix.
     */
    public TypedIOPort input;

    /** Output for tokens.
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
        DCT8x8 newObject = (DCT8x8) super.clone(workspace);
        newObject.input.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.output);

        return newObject;
    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _output = new IntMatrixToken[1];
        cos_values = new float[8];
        cos_values[0] = (float) 0.7071068;
        cos_values[1] = (float) 0.4903926;
        cos_values[2] = (float) 0.4619398;
        cos_values[3] = (float) 0.4157348;
        cos_values[4] = (float) 0.3535534;
        cos_values[5] = (float) 0.2777851;
        cos_values[6] = (float) 0.1913417;
        cos_values[7] = (float) 0.0975452;
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

        int i, j, k;
        float[] _block = new float[8];
        float[] _temp = new float[8];
        float[][] _ftemp = new float[8][8];
        int[][] sum = new int[8][8];
        IntMatrixToken _input;

        if (input.hasToken(0)) {
            _input = (IntMatrixToken) input.get(0);
        } else {
            return;
        }

        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                _block[j] = _input.getElementAt(i, j);
            }

            for (j = 0; j < 4; j++) {
                k = 7 - j;
                _temp[j] = _block[j] + _block[k];
                _temp[k] = _block[j] - _block[k];
            }

            _block[0] = _temp[0] + _temp[3];
            _block[1] = _temp[1] + _temp[2];
            _block[2] = _temp[1] - _temp[2];
            _block[3] = _temp[0] - _temp[3];

            _block[4] = _temp[4];

            _block[5] = (_temp[6] - _temp[5]) * cos_values[0];
            _block[6] = (_temp[6] + _temp[5]) * cos_values[0];

            _block[7] = _temp[7];

            _ftemp[i][0] = (_block[0] + _block[1]) * cos_values[4];
            _ftemp[i][4] = (_block[0] - _block[1]) * cos_values[4];
            _ftemp[i][2] = _block[2] * cos_values[6] + _block[3]
                    * cos_values[2];
            _ftemp[i][6] = _block[3] * cos_values[6] - _block[2]
                    * cos_values[2];

            _temp[4] = _block[4] + _block[5];
            _temp[7] = _block[7] + _block[6];
            _temp[5] = _block[4] - _block[5];
            _temp[6] = _block[7] - _block[6];

            _ftemp[i][1] = _temp[4] * cos_values[7] + _temp[7] * cos_values[1];
            _ftemp[i][5] = _temp[5] * cos_values[3] + _temp[6] * cos_values[5];
            _ftemp[i][7] = _temp[7] * cos_values[7] - _temp[4] * cos_values[1];
            _ftemp[i][3] = _temp[6] * cos_values[3] - _temp[5] * cos_values[5];
        }

        for (i = 0; i < 8; i++) {
            for (j = 0; j < 4; j++) {
                k = 7 - j;
                _temp[j] = _ftemp[j][i] + _ftemp[k][i];
                _temp[k] = _ftemp[j][i] - _ftemp[k][i];
            }
            _block[0] = _temp[0] + _temp[3];
            _block[1] = _temp[1] + _temp[2];
            _block[2] = _temp[1] - _temp[2];
            _block[3] = _temp[0] - _temp[3];

            _block[4] = _temp[4];

            _block[5] = (_temp[6] - _temp[5]) * cos_values[0];
            _block[6] = (_temp[6] + _temp[5]) * cos_values[0];

            _block[7] = _temp[7];

            _ftemp[0][i] = (_block[0] + _block[1]) * cos_values[4];
            _ftemp[4][i] = (_block[0] - _block[1]) * cos_values[4];
            _ftemp[2][i] = _block[2] * cos_values[6] + _block[3]
                    * cos_values[2];
            _ftemp[6][i] = _block[3] * cos_values[6] - _block[2]
                    * cos_values[2];

            _temp[4] = _block[4] + _block[5];
            _temp[7] = _block[7] + _block[6];
            _temp[5] = _block[4] - _block[5];
            _temp[6] = _block[7] - _block[6];

            _ftemp[1][i] = _temp[4] * cos_values[7] + _temp[7] * cos_values[1];
            _ftemp[5][i] = _temp[5] * cos_values[3] + _temp[6] * cos_values[5];
            _ftemp[7][i] = _temp[7] * cos_values[7] - _temp[4] * cos_values[1];
            _ftemp[3][i] = _temp[6] * cos_values[3] - _temp[5] * cos_values[5];

        }

        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                sum[i][j] = (int) _ftemp[i][j];
            }
        }
        _output[0] = new IntMatrixToken(sum);
        output.send(0, _output, _output.length);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IntMatrixToken[] _output;
    private float[] cos_values;

}

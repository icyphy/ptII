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
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

// Imports go here, in alphabetical order, with no wildcards.
///////////////////////////////////////////////////////////////////
//// SumofAsoluteDifferences

/**
   Calculate the sum of absolute difference(SAD) between two blocks.
   Each input block should have two dimensions by rectangle block(16 by 16, 8 by 8, etc.).
   The output port will have the result of SAD.

   @author Hwayong Oh
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (oh)
   @Pt.AcceptedRating Red (oh)
 */
public class SumofAbsoluteDifferences extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public SumofAbsoluteDifferences(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        previousImageBlock = new TypedIOPort(this, "previousImageBlock", true,
                false);
        previousImageBlock.setTypeEquals(BaseType.INT_MATRIX);

        currentImageBlock = new TypedIOPort(this, "currentImageBlock", true,
                false);
        currentImageBlock.setTypeEquals(BaseType.INT_MATRIX);

        sumAbsoluteDifference = new TypedIOPort(this, "sumAbsoluteDifference",
                false, true);
        sumAbsoluteDifference.setTypeEquals(BaseType.INT);

        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("16");
        blockSize.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Input for tokens to be a part of previous frame. This is a single port, and its
     * type is 2D array.
     */
    public TypedIOPort previousImageBlock;

    /** Input for tokens to be a part of current frame. This is a single port, and its
     * type is 2D array.
     */
    public TypedIOPort currentImageBlock;

    /** Output port. This is a result for calculating sum of absolute differences between
     * ImgBlockA and ImgBlockB.
     * The type is inferred form the connections.
     */
    public TypedIOPort sumAbsoluteDifference;

    /** BlockSize parameter. This is given by a number of elements in a block.
     *  Macro Block in
     */
    public Parameter blockSize;

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
        SumofAbsoluteDifferences newObject = (SumofAbsoluteDifferences) super
                .clone(workspace);
        newObject.previousImageBlock
        .setTypeAtLeast(newObject.previousImageBlock);
        newObject.currentImageBlock.setTypeAtLeast(newObject.currentImageBlock);
        newObject.sumAbsoluteDifference
        .setTypeAtLeast(newObject.sumAbsoluteDifference);
        return newObject;
    }

    /** If there is at least one token on the input ports, add
     *  tokens from the <i>plus</i> port, subtract tokens from the
     *  <i>minus</i> port, and send the result to the
     *  <i>output</i> port. At most one token is read
     *  from each channel, so if more than one token is pending, the
     *  rest are left for future firings.  If none of the input
     *  channels has a token, do nothing.  If none of the plus channels
     *  have tokens, then the tokens on the minus channels are subtracted
     *  from a zero token of the same type as the first token encountered
     *  on the minus channels.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int i, j, sum = 0;
        IntMatrixToken previousImage;
        IntMatrixToken currentImage;
        if (previousImageBlock.hasToken(0)) {
            previousImage = (IntMatrixToken) previousImageBlock.get(0);
        } else {
            return;
        }
        if (currentImageBlock.hasToken(0)) {
            currentImage = (IntMatrixToken) currentImageBlock.get(0);
        } else {
            return;
        }

        for (i = 0; i < _blockSize; i++) {
            for (j = 0; j < _blockSize; j++) {
                sum += Math.abs(previousImage.getElementAt(i, j)
                        - currentImage.getElementAt(i, j));
            }

        }
        sumAbsoluteDifference.send(0, new IntToken(sum));

    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _blockSize = ((IntToken) blockSize.getToken()).intValue();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Description of the variable. */

    protected int _blockSize;

}

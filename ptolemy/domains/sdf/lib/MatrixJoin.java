/* Join matrices provided either in sequence or on multiple input channels.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.domains.sdf.lib;

import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// MatrixJoin

/**
 This actor joins matrices into a single matrix by tiling.
 It reads N input matrices from each channel, where N is
 the value of the <i>sequentialTiles</i> parameter.
 
 Assuming M input channels and <i>sequentialInputs<i> = N,
 FIXME
 <p>

 @author Edward Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class MatrixJoin extends SDFTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixJoin(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set parameters.
        columns = new Parameter(this, "columns");
        columns.setExpression("1");

        input_tokenConsumptionRate.setExpression("columns");
        input.setMultiport(true);

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of columns in the output.  This is an integer that defaults
     *  to 1.
     */
    public Parameter columns;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ensure that the rows and columns parameters are both positive.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == columns) {
            int columnsValue = ((IntToken) columns.getToken()).intValue();
            if (columnsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of columns: " + columnsValue);
            }
        }
        super.attributeChanged(attribute);
    }

    /** Consume the inputs and produce the output matrix.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int numberOfColumns = ((IntToken) columns.getToken()).intValue();
        int numberOfRows = input.getWidth();
        MatrixToken[][] result = new MatrixToken[numberOfRows][numberOfColumns];
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                result[i][j] = (MatrixToken)input.get(i);
            }
        };
        output.send(0, MatrixToken.matrixJoin(result));
    }

    /** Return true if each input channel has enough tokens for this actor to
     *  fire. The number of tokens required is the value of the <i>columns</i> parameter.
     *  @return boolean True if there are enough tokens at the input port
     *   for this actor to fire.
     *  @exception IllegalActionException If the hasToken() query to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    public boolean prefire() throws IllegalActionException {
        int columnsValue = ((IntToken) columns.getToken()).intValue();
        if (!input.hasToken(0, columnsValue)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }
}

/* An actor that disassembles a DoubleMatrixToken to a multiport output.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (celaine@eecs.berkeley.edu)
@AcceptedRating Yellow (celaine@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// VectorDisassembler
/**
An actor that disassembles a DoubleMatrixToken to a multiport output.
<p>On each firing, read one column vector (i.e. a DoubleMatrixToken with
one column) from the <i>input</i> port and send out individual
DoubleTokens to each channel of the <i>output</i> port.  If the width
of the <i>output</i> port (say, <i>n</i>) is less than the number of
rows (say, <i>m</i>) in the input token, then the first <i>n<i>
elements in the vector will be sent, and the remaining tokens are
discarded.  If <i>n</i> is greater than <i>m<i>, then the last
<i>n-m</i> channels of the output port will never send tokens out.
This class throws an exception if the input is not a column vector.

<p>For sequential domains like SDF, the combination of
domains.sdf.lib.MatrixToDouble and a Distributor is equivalent to this
actor.  However, that combination will not work in CT, so we need this
actor.

@author Jie Liu, Elaine Cheong
@version $Id$
@since Ptolemy II 2.0
@see VectorAssembler
*/
public class VectorDisassembler extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VectorDisassembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        input.setMultiport(false);
        output.setTypeEquals(BaseType.DOUBLE);
        output.setMultiport(true);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" width=\"6\" " +
                "height=\"40\" style=\"fill:blue\"/>\n" +
                "</svg>\n");
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token at the input, read one column vector
     *  (i.e. a DoubleMatrixToken with one column) from the input port,
     *  and for each channel i of the output port, send send the ith
     *  element of this column vector to this channel.  Otherwise, do
     *  nothing.
     *
     *  Note: The output tokens are copies of the corresponding
     *  elements in the input token.
     *
     *  @exception IllegalActionException If there is no director, or
     *  the input token has more than one column.
     */
    public void fire() throws IllegalActionException {

        if (input.hasToken(0)) {
            DoubleMatrixToken vector = (DoubleMatrixToken)input.get(0);

            if (vector.getColumnCount() != 1) {
                throw new IllegalActionException(this, "The input must "
                        + "be a DoubleMatrixToken with one column.");
            }

            int min = Math.min(vector.getRowCount(), output.getWidth());

            for (int i = 0; i < min; i++) {
                output.send(i, vector.getElementAsToken(i, 0));
            }
        }
    }
}


/* An actor that disassembles a DoubleMatrixToken to a multi-output.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.DoubleToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Transformer;


//////////////////////////////////////////////////////////////////////////
//// VectorDisassembler
/**
On each firing, read one column vector (i.e. a DoubleMatrixToken with
one column) from the input port and send out individual DoubleTokens
to the output port.
If the width of the output port (say, <i>n</i>) is less than the number
of rows (say <i>m</i>) in the input token, then the first <i>n<i>
elements in the vector will be sent, and the rest is discarded.
If <i>n</i> is greater than <i>m<i>, then the last <i>n-m</i> channels
in the output port will never send tokens out.

<p>For sequential domains like SDF, the combination of the
sdf.actor.lib.DoubleMatrixToDouble and a Distributor is equivalent
to this actor.  However, that combination will not work in CT,
so we need this actor.

@author Jie Liu
@version $Id$
@see VectorAssembler
*/
public class VectorDisassembler extends Transformer {

    /** Construct a VectorDisassembler with the given container and name.
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

        _addIcon();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token at the input, read one column vector
     *  (i.e. a DoubleMatrixToken with one column)
     *  from the input port and send its first <i>n</i> elements
     *  to the output port, one in each channel, where <i>n</i> is the
     *  width of the output port. Otherwise, do nothing.
     *  Note: The output tokens are copies of the corresponding elements
     *  in the input token.
     *  @exception IllegalActionException If there is no director, or
     *  the input token has more than one column.
     */
    public void fire() throws IllegalActionException {

	if (input.hasToken(0)) {
	    DoubleMatrixToken vector = (DoubleMatrixToken)input.get(0);

            if(vector.getColumnCount() != 1) {
                throw new IllegalActionException(this, "The input must "
                        + "be a DoubleMatrixToken with one column.");
            }

            for (int i = 0; i < Math.min(vector.getRowCount(),
                    output.getWidth()); i++) {
                output.send(i, vector.getElementAsToken(i, 0));
            }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addIcon() {
	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-5\" y=\"-15\" width=\"10\" " +
                "height=\"30\" style=\"fill:blue\"/>\n" +
                "</svg>\n");
    }
}


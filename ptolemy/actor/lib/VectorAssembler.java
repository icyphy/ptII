/* An actor that reads a token from each input channel to assemble a
 DoubleMatrixToken.

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

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// VectorAssembler

/**
 <p>On each firing, read exactly one token from each channel of the
 <i>input</i> port and assemble the tokens into a DoubleMatrixToken
 with one column.  The DoubleMatrixToken is sent to the <i>output</i>
 port.  If there is no input token at any channel of the <i>input</i> port,
 then prefire() will return false.  Note that the elements in the
 vector are not copied.

 </p><p>For sequential domains like SDF, the combination of a Commutator
 and domains.sdf.lib.DoubleToMatrix is equivalent to this actor.
 However, that combination will not work in CT, so we need this actor.</p>

 @author Jie Liu, Elaine Cheong
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 @see VectorDisassembler
 */
public class VectorAssembler extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VectorAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setMultiport(false);
        isColumn = new Parameter(this, "isColumn", BooleanToken.TRUE);
        isColumn.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** True if the output vector is a column matrix. Otherwise,
     *  the output is a row matrix. The default value is true.
     */
    public Parameter isColumn;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each channel of the <i>input</i> port,
     *  assemble those tokens into a DoubleMatrixToken, and send the
     *  result to the output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int size = input.getWidth();

        double[][] data;

        boolean isColumnValue = ((BooleanToken) isColumn.getToken())
                .booleanValue();

        if (isColumnValue) {
            data = new double[size][1];

            for (int i = 0; i < size; i++) {
                Token token = input.get(i);
                try {
                    data[i][0] = ((ScalarToken) token).doubleValue();
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this, ex, "Cannot cast \""
                            + token + "\" to a ScalarToken");
                }
            }
        } else {
            data = new double[1][size];

            for (int i = 0; i < size; i++) {
                Token token = input.get(i);
                try {
                    data[0][i] = ((ScalarToken) token).doubleValue();
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this, ex, "Cannot cast \""
                            + token + "\" to a ScalarToken");
                }
            }
        }

        DoubleMatrixToken result = new DoubleMatrixToken(data);

        output.send(0, result);
    }

    /** Return true if all channels of the <i>input</i> port have
     *  tokens, false if any channel does not have a token.
     *  @return True if all channels of the <i>input</i> port have tokens.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        for (int i = 0; i < input.getWidth(); i++) {
            if (!input.hasToken(i)) {
                return false;
            }
        }

        return super.prefire();
    }
}

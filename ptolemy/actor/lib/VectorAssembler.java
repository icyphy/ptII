/* An actor that assembles multiple inputs to a DoubleMatrixToken.

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
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Transformer;

//////////////////////////////////////////////////////////////////////////
//// VectorAssembler
/**
On each firing, read exactly one token from each input port and assemble
them into a DoubleMatrixToken with one column. If there is no input token
at any channel of the input port, then the prefire() will return false.
Note that the elements in the vector are not copied.

<p>For sequential domains like SDF, the combination of the
a Commutator and sdf.actor.lib.DoubleToDoubleMatrix is equivalent
to this actor.  However, that combination will not work in CT,
so we need this actor.

@author Jie Liu
@version $Id$
@see VectorDisassembler
*/

public class VectorAssembler extends Transformer {

    /** Construct a VectorAssembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VectorAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setMultiport(false);

        _addIcon();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input port, assemble them into a
     *  DoubleMatrixToken, and send the token to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	int size = input.getWidth();

	double[][] data = new double[size][1];

	for (int i = 0; i < size; i++) {
            data[i][0] = ((DoubleToken)input.get(i)).doubleValue();
	}

	DoubleMatrixToken result = new DoubleMatrixToken(data);

        output.send(0, result);
    }

    /** Return true if all input ports have tokens, false if some input
     *  ports do not have a token.
     *  @return True if all input ports have tokens.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean prefire() throws IllegalActionException {
	for(int i = 0; i < input.getWidth(); i++) {
	    if ( !input.hasToken(i)) {
	        return false;
	    }
        }
	return true;
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


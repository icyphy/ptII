/** Actor that reads a stream of double tokens and places them into a
Upper Triangular Matrix.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

package ptolemy.domains.pn.demo.QR;

import ptolemy.actor.lib.Transformer;

import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// StreamToMatrix
/**

Convert a stream of Double Tokens into a Matrix. The Matrix is
considered to be an Upper triangular Matrix.
@author Bart Kienhuis
@version $Id: StreamToMatrix.java,v 1.2 1999/11/30 03:55:57 kienhuis
Exp $
*/

public class StreamToMatrix extends Transformer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StreamToMatrix(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException
        {
            super(container, name);

            input.setTypeEquals(BaseType.DOUBLE);
            output.setTypeEquals(BaseType.DOUBLE_MATRIX);
            dimension = new Parameter(this, "dimension", new IntToken( 6 ));

            // Initialize the dimension
            attributeChanged( dimension );

        }

    /** If the argument is the meanTime parameter, check that it is
     *  positive.
     * @exception IllegalActionException If the
     *  meanTime value is * not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dimension) {
            _rows = ((IntToken)dimension.getToken()).intValue();
            _columns = ((IntToken)dimension.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        StreamToMatrix newobj = (StreamToMatrix)super.clone(ws);
        newobj.dimension = (Parameter)newobj.getAttribute("dimension");
        return newobj;
    }

    /** Reads a stream of DoubleTokens and places these tokens in a
     *  Matrix. The Matrix produced is an Upper Triangular Matrix.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int runL = 0;
        double[][] image = new double[_rows][_columns];
        for (int i = 0; i < _rows; i++) {
            for (int j = 0; j < _columns; j++) {
                if ( j >= runL ) {
                    image[i][j] = ((DoubleToken)input.get(0)).doubleValue();
                } else {
                    image[i][j] = 0.0;
                }
            }
            runL++;
        }
        output.broadcast(new DoubleMatrixToken(image));
    }


    /** Initialize the row and column number.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();

	// Get the correct value from the parameters
        _rows = ((IntToken)dimension.getToken()).intValue();
        _columns = ((IntToken)dimension.getToken()).intValue();

    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The dimension of the matrix.
     */
    public Parameter dimension;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _rows    = 6;
    private int _columns = 6;

}

/* Reads an audio file and divides the audio data into blocks.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNImageSink
/**
Stores an image file (int the ASCII PBM format) and creates a matrix token

@author Mudit Goel
@version $Id$
*/

public class StreamToMatrix extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StreamToMatrix(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // input  = new IOPort(this, "input", true, false);
        // output = new IOPort(this, "output", false, true);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        dimension = new Parameter(this, "Dimension", new IntToken( 6 ));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The interpolation ratio of the filter. This must contain an
     *  IntToken, and by default it has value one.
     */
    public Parameter dimension;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the meanTime parameter, check that it is
     *  positive.
     *  @exception IllegalActionException If the meanTime value is
     *   not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dimension) {
            System.out.println(" ---- Attribute Changed For Dimension ");
            _rows = ((IntToken)dimension.getToken()).intValue();
            _columns = ((IntToken)dimension.getToken()).intValue();
            System.out.println(" ---- Row:    " + _rows );
            System.out.println(" ---- Column: " + _columns );
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {

        int runL = 0;
        double[][] image = new double[_rows][_columns];
        for (int i = 0; i < _rows; i++) {
            for (int j = 0; j < _columns; j++) {

                if ( j >= runL ) {
                    image[i][j] = ((DoubleToken)input.get(0)).doubleValue();
                    System.out.println(" created[" +i+"]["+j+"] " + image[i][j]);
                } else {
                  image[i][j] = 0.0;
                  System.out.println(" ZERO created[" +i+"]["+j+"] " + image[i][j]);
                }
            }
            runL++;
        }
        System.out.println(" **** MATRIX SEND **** ");
        output.broadcast(new DoubleMatrixToken(image));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _rows    = 6;
    private int _columns = 6;


    //Get Matrix dimensions
    // int rows = ((IntToken)_dimen.get(0)).intValue();
    // int columns = ((IntToken)_dimen.get(0)).intValue();

}

/* A PN process actor object.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.demo.QR;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;


//////////////////////////////////////////////////////////////////////////
//// ND_2

/**

This class defines a PN actor object. This actor is automatically
generated, as part of the <A
HREF="http://www.gigascale.org/compaan">Compaan</A> project. Although
most of the actor is generated automatically, some parts have been
manually tuned for this demo especially when reading and writing of
matrices is involved; they may change in future releases.

@author Bart Kienhuis
@version $Id$
*/

public class ND_2 extends TypedAtomicActor {

    /** Construct an actor that is an SBF object with the given container
     *  and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ND_2(TypedCompositeActor aContainer, String aName)
            throws IllegalActionException, NameDuplicationException
        {

            super(aContainer, aName);

            WP_4 = new TypedIOPort(this, "WP_4", false, true);
            WP_8 = new TypedIOPort(this, "WP_8", false, true);

            WP_4.setTypeEquals(BaseType.DOUBLE);
            WP_8.setTypeEquals(BaseType.DOUBLE);

            // The Type of these Parameters is set by the First
            // Token placed in the parameters when created.
            parameter_N = new Parameter(this, "N" , new IntToken(6));
            parameter_K = new Parameter(this, "K" , new IntToken(6));

            x_1.ReadMatrix( "U_1000x16" );

        }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    // -- Part of the Actor

    public TypedIOPort WP_4;
    public TypedIOPort WP_8;

    // -- Public interface of the Actor
    public Parameter parameter_N;
    public Parameter parameter_K;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Initialize the PN actor.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _returnValue = true;

        // Get the correct value from the parameters
        N = ((IntToken) parameter_N.getToken()).intValue();
        K = ((IntToken) parameter_K.getToken()).intValue();
    }

    /** Fire the actor.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        for ( int k = 1 ; k <= 1*K ; k += 1 ) {
            for ( int j = 1 ; j <= 1*N ; j += 1 ) {
                out_0 = x_1.retrieve( x_1.atKey(k, j) );
                _debug(" Broadcast from ND_2: " + out_0 );
                if ( j - 1 == 0 ) {
                    WP_4.broadcast( new DoubleToken( out_0 ) );
                }
                if ( j - 2 >= 0 ) {
                    WP_8.broadcast( new DoubleToken( out_0 ) );
                }
            }
        }
    }

    /** Post fire the actor. Return false to indicated that the
     *  process has finished. If it returns true, the process will
     *  continue indefinitely.
     */
    public boolean postfire() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // -- Get private copies of the parameters
    private int N;
    private int K;

    private double out_0;

    private ArrayIndex x_1 = new ArrayIndex();


    private boolean _returnValue = true;

}

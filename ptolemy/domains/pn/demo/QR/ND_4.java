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
//// ND_4

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

public class ND_4 extends TypedAtomicActor {

    /** Construct an actor that is an SBF object with the given container
     *  and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ND_4(TypedCompositeActor aContainer, String aName)
            throws IllegalActionException, NameDuplicationException   {

                super(aContainer, aName);

                RP_5 = new TypedIOPort(this, "RP_5", true, false);
                RP_6 = new TypedIOPort(this, "RP_6", true, false);
                RP_7 = new TypedIOPort(this, "RP_7", true, false);
                RP_8 = new TypedIOPort(this, "RP_8", true, false);
                RP_9 = new TypedIOPort(this, "RP_9", true, false);
                RP_10 = new TypedIOPort(this, "RP_10", true, false);

                WP_5 = new TypedIOPort(this, "WP_5", false, true);
                WP_12 = new TypedIOPort(this, "WP_12", false, true);
                WP_3 = new TypedIOPort(this, "WP_3", false, true);
                WP_7 = new TypedIOPort(this, "WP_7", false, true);
                WP_9 = new TypedIOPort(this, "WP_9", false, true);

                RP_5.setTypeEquals(BaseType.DOUBLE);
                RP_6.setTypeEquals(BaseType.DOUBLE);
                RP_7.setTypeEquals(BaseType.DOUBLE);
                RP_8.setTypeEquals(BaseType.DOUBLE);
                RP_9.setTypeEquals(BaseType.DOUBLE);
                RP_10.setTypeEquals(BaseType.DOUBLE);

                WP_5.setTypeEquals(BaseType.DOUBLE);
                WP_12.setTypeEquals(BaseType.DOUBLE);
                WP_3.setTypeEquals(BaseType.DOUBLE);
                WP_7.setTypeEquals(BaseType.DOUBLE);
                WP_9.setTypeEquals(BaseType.DOUBLE);

                // The Type of these Parameters is set by the First
                // Token placed in the parameters when created.
                parameter_N = new Parameter(this, "N" , new IntToken(6));
                parameter_K = new Parameter(this, "K" , new IntToken(6));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    // -- Part of the Actor
    public TypedIOPort RP_5;
    public TypedIOPort RP_6;
    public TypedIOPort RP_7;
    public TypedIOPort RP_8;
    public TypedIOPort RP_9;
    public TypedIOPort RP_10;

    public TypedIOPort WP_5;
    public TypedIOPort WP_12;
    public TypedIOPort WP_3;
    public TypedIOPort WP_7;
    public TypedIOPort WP_9;

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
            for ( int j = 1 ; j <= 1*N + -1 ; j += 1 ) {
                for ( int i = 1*j + 1 ; i <= 1*N ; i += 1 ) {
                    if ( k - 2 >= 0 ) {
                        r_3.add( new Double(((DoubleToken) RP_5.get(0)).
                                doubleValue() ) );
                        in_0 = ((Double)r_3.elementAt(w_r_3++)).doubleValue();
                    }
                    if ( k - 1 == 0 ) {
                        r_1.add( new Double(((DoubleToken) RP_6.get(0)).
                                doubleValue() ) );
                        in_0 = ((Double)r_1.elementAt(w_r_1++)).doubleValue();
                    }
                    if ( j - 2 >= 0 ) {
                        x_3.add( new Double(((DoubleToken) RP_7.get(0)).
                                doubleValue() ) );
                        in_1 = ((Double)x_3.elementAt(w_x_3++)).doubleValue();
                    }
                    if ( j - 1 == 0 ) {
                        x_1.add( new Double(((DoubleToken) RP_8.get(0)).
                                doubleValue() ) );
                        in_1 = ((Double)x_1.elementAt(w_x_1++)).doubleValue();
                    }
                    if ( i - j - 2 >= 0 ) {
                        t_2.add( new Double(((DoubleToken) RP_9.get(0)).
                                doubleValue() ) );
                        in_2 = ((Double)t_2.elementAt(w_t_2++)).doubleValue();
                    }
                    if ( -i + j + 1 == 0 ) {
                        t_1.add( new Double(((DoubleToken) RP_10.get(0)).
                                doubleValue() ) );
                        in_2 = ((Double)t_1.elementAt(w_t_1++)).doubleValue();
                    }

                    _Rotate(in_0, in_1, in_2);
                    out_0 = _argOut0;
                    out_1 = _argOut1;
                    out_2 = _argOut2;


                    if ( K - k - 1 >= 0 ) {
                        WP_5.broadcast( new DoubleToken( out_0 ) );
                    }
                    if ( -K + k == 0 ) {
                        WP_12.broadcast( new DoubleToken( out_0 ) );
                    }
                    if ( -i + j + 1 == 0 ) {
                        WP_3.broadcast( new DoubleToken( out_1 ) );
                    }
                    if ( i - j - 2 >= 0 ) {
                        WP_7.broadcast( new DoubleToken( out_1 ) );
                    }
                    if ( N - i - 1 >= 0 ) {
                        WP_9.broadcast( new DoubleToken( out_2 ) );
                    }
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
    ////                         private methods                   ////

    private void _Rotate(double arg0, double arg1, double arg2) {
        _argOut0 = Math.cos(arg2) * arg0   -   Math.sin(arg2) * arg1;
	_argOut1 = Math.sin(arg2) * arg0   +   Math.cos(arg2) * arg1;
	_argOut2 = arg2;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // -- Get private copies of the parameters
    private int N;
    private int K;

    private double in_0;
    private double in_1;
    private double in_2;
    private double out_0;
    private double out_1;
    private double out_2;

    // Needed to communicate results from the function
    private double _argOut0;
    private double _argOut1;
    private double _argOut2;

    private Vector r_3 = new Vector();
    private Vector r_1 = new Vector();
    private Vector x_3 = new Vector();
    private Vector x_1 = new Vector();
    private Vector t_2 = new Vector();
    private Vector t_1 = new Vector();

    private int w_r_3 = 0;
    private int w_r_1 = 0;
    private int w_x_3 = 0;
    private int w_x_1 = 0;
    private int w_t_2 = 0;
    private int w_t_1 = 0;

    private boolean _returnValue = true;

}

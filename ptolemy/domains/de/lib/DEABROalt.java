/* ABRO

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEABROalt
/**
This actor implements the classical ABRO specification.
<br>
Emit an output O as soon as two inputs A and B have occured. Reset this
behavior each time the input R occurs.

@author Lukito Muliadi
@version $Id$
*/
public class DEABROalt extends DEThreadActor {

    private static boolean DEBUG = true;

    /** Construct a DEANDGate actor with the specified delay.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *  @param tlh The amount of propagation delay for the output to goes
     *      from low to high.
     *  @param thl The amount of propagation delay for the output to goes
     *      from high to low.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container, or if the delay is less than zero.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEABROalt(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the ports
        A = new DEIOPort(this, "A", true, false);
        B = new DEIOPort(this, "B", true, false);
        R = new DEIOPort(this, "R", true, false);
        O = new DEIOPort(this, "O", false, true);
        A.setDeclaredType(Token.class);
        B.setDeclaredType(Token.class);
        R.setDeclaredType(Token.class);
        O.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void run() {
        try {


            while (true) {
                if (DEBUG) {
                    System.out.println("In initial state..");
                }
                waitForNewInputs();
                if (R.hasToken(0)) {
                    if (DEBUG) {
                        System.out.println("Resetting..");
                    }
                    continue;
                }
                if (A.hasToken(0)) {
                    if (DEBUG) {
                        System.out.println("Seen A..");
                    }

                    IOPort[] ports = {B,R};
                    waitForNewInputs(ports);
                    if (!R.hasToken(0)) {
                        if (DEBUG) {
                            System.out.println("Seen A then B..");
                        }
                        O.broadcast(new DoubleToken(1.0));
                        IOPort[] ports2 = {R};
                        waitForNewInputs(ports2);
                    } else {
                        if (DEBUG) {
                            System.out.println("Resetting");
                        }
                        continue;
                    }
                } else if (B.hasToken(0)) {
                    if (DEBUG) {
                        System.out.println("Seen B..");
                    }

                    IOPort[] ports = {A,R};
                    waitForNewInputs(ports);
                    if (!R.hasToken(0)) {
                        if (DEBUG) {
                            System.out.println("Seen B then A..");
                        }
                        O.broadcast(new DoubleToken(1.0));
                        IOPort[] ports2 = {R};
                        waitForNewInputs(ports2);
                    } else {
                        if (DEBUG) {
                            System.out.println("Resetting");
                        }
                        continue;
                    }
                }
            } // while (true)


        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // the ports.
    public DEIOPort A;
    public DEIOPort B;
    public DEIOPort R;
    public DEIOPort O;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}





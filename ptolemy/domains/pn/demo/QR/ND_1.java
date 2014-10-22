/* A PN process actor object.

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ND_1

/**

 This class defines a PN actor object. This actor is automatically
 generated, as part of the <A
 HREF="http://www.gigascale.org/compaan">Compaan</A> project. Although
 most of the actor is generated automatically, some parts have been
 manually tuned for this demo especially when reading and writing of
 matrices is involved; they may change in future releases.

 @author Bart Kienhuis
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (kienhuis)
 @Pt.AcceptedRating Red (kienhuis)
 */
public class ND_1 extends TypedAtomicActor {
    /** Construct an actor that is an SBF object with the given container
     *  and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ND_1(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        WP_2 = new TypedIOPort(this, "WP_2", false, true);
        WP_6 = new TypedIOPort(this, "WP_6", false, true);

        WP_2.setTypeEquals(BaseType.DOUBLE);
        WP_6.setTypeEquals(BaseType.DOUBLE);

        // The Type of these Parameters is set by the First
        // Token placed in the parameters when created.
        parameter_N = new Parameter(this, "N", new IntToken(6));
        parameter_K = new Parameter(this, "K", new IntToken(6));

        x_1.readMatrix("Zeros_64x64");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // -- Part of the Actor
    public TypedIOPort WP_2;

    public TypedIOPort WP_6;

    // -- Public interface of the Actor
    public Parameter parameter_N;

    public Parameter parameter_K;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the PN actor.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Get the correct value from the parameters
        N = ((IntToken) parameter_N.getToken()).intValue();
        K = ((IntToken) parameter_K.getToken()).intValue();
    }

    /** Fire the actor.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (int j = 1; j <= 1 * N; j += 1) {
            for (int i = 1 * j; i <= 1 * N; i += 1) {
                out_0 = x_1.retrieve(x_1.atKey(j, i));
                _debug(" Broadcast from ND_1: " + out_0);

                if (-i + j == 0 && K - 1 >= 0) {
                    WP_2.broadcast(new DoubleToken(out_0));
                }

                if (i - j - 1 >= 0 && K - 1 >= 0) {
                    WP_6.broadcast(new DoubleToken(out_0));
                }
            }
        }
    }

    /** Post fire the actor. Return false to indicated that the
     *  process has finished. If it returns true, the process will
     *  continue indefinitely.
     *  @exception IllegalActionException If thrown by the parent
     *  class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // We intentially ignore the return value of super.postfire()
        // here.
        super.postfire();
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // -- Get private copies of the parameters
    private int N;

    private int K;

    private double out_0;

    private ArrayIndex x_1 = new ArrayIndex();
}

/* A Synchronous default operator.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Default

/**
 A Synchronous default operator.

 <p>When the <i>preferred</i> input has a token, then the output is equal
 to that token. If the <i>preferred</i> input is absent, then the output is
 equal to the <i>alternate</i> input (whether it is absent or not).
 This actor is non-strict, in that can produce an output even if
 <i>alternate</i> input is unknown. Thus, it can be used to break
 causality loops.
 <p>
 This actor is inspired by the "default" operator in the synchronous
 language SIGNAL, and is similar to the "followed by" operator in Lustre.
 But in the context of the Ptolemy II SR domain, its
 behavior is deterministic. This is because the Ptolemy II SR domain
 has a Lustre or Esterel style master clock, and this actor is given
 the opportunity to fire on each tick of that master clock. In SIGNAL,
 by contrast, this actor can be used to "upsample" to a higher rate
 clock; however, without considerable care, its use in SIGNAL results
 in nondeterminism.

 <p>This actor is typically used in the SR domain, but
 may also be used inside the Continuous domain.</p>

 <p>P. Caspi, D. Pilaud, N. Halbwachs, and J. A. Plaice, "LUSTRE: A
 Declarative Language for Programming Synchronous Systems,"
 Conference Record of the 14th Annual ACM Symp. on Principles of
 Programming Languages, Munich, Germany, January, 1987.</p>

 <p>A. Benveniste and P. Le Guernic, "Hybrid Dynamical Systems Theory
 and the SIGNAL Language," IEEE Tr. on Automatic Control, Vol. 35,
 No. 5, pp. 525-546, May 1990.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Default extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Default(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        preferred = new TypedIOPort(this, "preferred", true, false);
        alternate = new TypedIOPort(this, "alternate", true, false);

        StringAttribute controlCardinal = new StringAttribute(alternate,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");

        output = new TypedIOPort(this, "output", false, true);

        // Default type constraints are the right ones, so we need not
        // explicitly declare them here.
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The preferred input port.  If there is a token here, then that
     *  token is produced on the output. Any data type is accepted.
     */
    public TypedIOPort preferred;

    /** The alternate input port. If there is no token on the preferred
     *  input port, then the output will equal whatever is here (including
     *  absent). Any data type is accepted.
     */
    public TypedIOPort alternate;

    /** The output port. The type is greater than or equal to the
     *  types of the two input ports.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the <i>preferred</i> input is known and present, then its token is
     *  sent to the output port. Otherwise, the output is obtained from
     *  the <i>alternate</i> input port.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (preferred.isKnown(0)) {
            if (preferred.hasToken(0)) {
                output.send(0, preferred.get(0));
            } else {
                // NOTE: It is essential that preferred be known
                // to be absent before we produce the alternate,
                // or else this will not implement a monotonic function.
                if (alternate.isKnown(0)) {
                    if (alternate.hasToken(0)) {
                        output.send(0, alternate.get(0));
                    } else {
                        output.send(0, null);
                    }
                }
            }
        }
    }

    /** Return false. This actor is non-strict in that it can produce
     *  an output even if alternate input is unknown.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>alternate</i> in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(alternate, output);
    }
}

/* An actor that merges two monotonically increasing streams into one.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ScalarToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// OrderedMerge
/**
This actor merges two monotonically nondecreasing streams of tokens into
one monotonically nondecreasing stream. On each firing, it reads data from
one of the inputs.  On the first firing, it simply records that token.
On the second firing, it reads data from the other input and outputs
the smaller of the recorded token and the one it just read.  If they
are equal, then it outputs the recorded token. It then
records the larger token.  On each subsequent firing, it reads a token
from the input port that did not provide the recorded token, and produces
at the output the smaller of the recorded token and the one just read.
<p>
If both input sequences are nondecreasing, then the output sequence
will be nondecreasing.
Note that if the inputs are not nondecreasing, then the output is
rather complex.  The key is that in each firing, it produces the smaller
of the recorded token and the token it is currently reading.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0.1
*/

public class OrderedMerge extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OrderedMerge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        inputA = new TypedIOPort(this, "inputA", true, false);
        inputB = new TypedIOPort(this, "inputB", true, false);
        inputB.setTypeSameAs(inputA);
        inputA.setTypeAtMost(BaseType.SCALAR);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeSameAs(inputA);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The first input port, which accepts any scalar token. */
    public TypedIOPort inputA;

    /** The second input port, which accepts any scalar token with
     *  the same type as the first input port.
     */
    public TypedIOPort inputB;

    /** The output port, which has the same type as the input ports. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        OrderedMerge newObject = (OrderedMerge)super.clone(workspace);
        newObject.inputA.setTypeAtMost(BaseType.SCALAR);
        newObject.inputB.setTypeSameAs(newObject.inputA);
        newObject.output.setTypeSameAs(newObject.inputA);
        return newObject;
    }

    /** Read one token from the port that did not provide the recorded
     *  token (or <i>inputA</i>, on the first firing), and output the
     *  smaller of the recorded token or the newly read token.
     *  If there is no token on the port to be read, then do nothing
     *  and return.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (nextPort.hasToken(0)) {
            ScalarToken readToken = (ScalarToken)nextPort.get(0);
            if (recordedToken == null) {
                // First firing.  Just record the token.
                tentativeRecordedToken = readToken;
                tentativeNextPort = inputB;
            } else {
                if ((readToken.isLessThan(recordedToken)).booleanValue()) {
                    // Produce the smaller output.
                    output.send(0, readToken);
                } else {
                    // Produce the smaller output.
                    output.send(0, recordedToken);
                    tentativeRecordedToken = readToken;

                    // Swap ports.
                    if (nextPort == inputA) {
                        tentativeNextPort = inputB;
                    } else {
                        tentativeNextPort = inputA;
                    }
                }
            }
        }
    }

    /** Initialize this actor to indicate that no token is recorded.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        nextPort = inputA;
        recordedToken = null;
    }

    /** Commit the recorded token.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        recordedToken = tentativeRecordedToken;
        nextPort = tentativeNextPort;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The recorded token. */
    private ScalarToken recordedToken = null;

    /** The port from which to read next. */
    private TypedIOPort nextPort = null;

    /** The tentative recorded token. */
    private ScalarToken tentativeRecordedToken = null;

    /** The tentative port from which to read next. */
    private TypedIOPort tentativeNextPort = null;
}

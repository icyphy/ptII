/* An actor that accepts a token from any channel connected to
 its input.

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
package ptolemy.domains.csp.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPMultiSink

/**
 A CSPMultiSink actor accepts a token from any channel connected to
 its input. It uses a conditional do (CDO) construct to always be
 ready to accept a new token. The channels it can accept from are set
 at the start of each firing.

 @author Neil Smyth
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Red (nsmyth)
 @see ptolemy.domains.csp.kernel.CSPActor
 */
public class CSPMultiSink extends CSPActor {
    /** Construct a CSPMultiSink in the default workspace with an
     *  empty string as its name. The actor is created with a single
     *  input port named "input".
     */
    public CSPMultiSink() {
        super();
    }

    /** Construct a CSPMultiSink with the specified container and
     *  name. The actor is created with a single input port named
     *  "input". The name of the actor must be unique within the
     *  container or a NameDuplicationException is thrown. The
     *  container argument must not be null, or a NullPointerException
     *  will be thrown.
     *  @param container The container of this actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the port cannot be
     *   contained by this actor.
     *  @exception NameDuplicationException If the port name coincides
     *   with a port already in this actor.
     */
    public CSPMultiSink(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This actor's input port.
     */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by consuming tokens through the input
     *  port. Use a conditional do (CDO) construct for token
     *  consumption.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            int count = 0;
            int size = input.getWidth();
            _branchCount = new int[size];

            int i = 0;
            boolean[] guards = new boolean[size];

            for (i = 0; i < size; i++) {
                _branchCount[i] = 0;
                guards[i] = true;
            }

            boolean continueCDO = true;

            while (continueCDO || count < 25) {
                ConditionalBranch[] branches = new ConditionalBranch[size];

                for (i = 0; i < size; i++) {
                    branches[i] = new ConditionalReceive(guards[i], input, i, i);
                }

                int successfulBranch = chooseBranch(branches);

                _branchCount[successfulBranch]++;

                boolean flag = false;

                for (i = 0; i < size; i++) {
                    if (successfulBranch == i) {
                        Token t = branches[successfulBranch].getToken();
                        System.out.println(getName() + ": received Token: "
                                + t.toString() + " from receiver " + i);
                        flag = true;
                    }
                }

                if (successfulBranch == -1) {
                    // all guards false so exit CDO
                    continueCDO = false;
                } else if (!flag) {
                    throw new TerminateProcessException(getName() + ": "
                            + "branch id returned during execution of CDO.");
                }

                count++;
            }
        } catch (IllegalActionException ex) {
            throw new TerminateProcessException(getName() + ": Error: "
                    + "could not create ConditionalReceive branch");
        }

        return;
    }

    /** Return false indicating that iteration of this actor should
     *  not continue.
     * @return false Indicating that iteration of this actor should
     *  should not continue.
     */
    @Override
    public boolean postfire() {
        return false;
    }

    /** Discontinue the execution of this actor.
     */
    @Override
    public void wrapup() {
        System.out.println(Thread.currentThread().getName()
                + ":Invoking wrapup of CSPMultiSink...\n");

        try {
            for (int i = 0; i < input.getWidth(); i++) {
                System.out.println("MultiSink: Branch " + i
                        + " successfully  rendezvoused " + _branchCount[i]
                        + " times.");
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "At this time IllegalActionExceptions are not allowed to happen.\n"
                            + "Width inference should already have been done.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Array storing the number of times each branch rendezvoused. */
    private int[] _branchCount;
}

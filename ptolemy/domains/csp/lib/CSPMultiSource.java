/* An actor that produces tokens through an output channel via a
continuous do (CDO) construct.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (nsmyth@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// CSPMultiSource
/**
A CSPMultiSource actor produces tokens through an output channel
via a continuous do (CDO) construct. The tokenLimit parameter
specifies how many tokens are produced by this actor. If the value
of tokenLimit is a nonnegative integer, then the actor produces
that many tokens. If the value is negative, then the actor produces
tokens indefinitely. The default value of tokenLimit is -1.

@author Neil Smyth
@version $Id$
@see ptolemy.domains.csp.kernel.CSPActor
@see ptolemy.domains.csp.kernel.ConditionalBranch
*/
public class CSPMultiSource extends CSPActor {

    /** Construct a CSPMultiSource in the default workspace with an
     *  empty string as its name. The actor is created with a single
     *  input port named "input". The number of tokens produced by this
     *  actor is specified by the tokenLimit parameter. The actor will
     *  produce N=tokenLimit tokens unless tokenLimit < 0 in which case
     *  this actor will produce tokens indefinitely.
     *  @exception IllegalActionException If the tokenLimit parameter
     *   cannot be contained by this actor.
     *  @exception NameDuplicationException If the tokenLimit parameter
     *   name coincides with a port already in this actor.
     */
    public CSPMultiSource() throws
    	    IllegalActionException, NameDuplicationException {
        super();
        tokenLimit = new Parameter( this, "tokenLimit",
        	(new IntToken(-1)) );
    }

    /** Construct a CSPMultiSource with the specified container and
     *  name. The actor is created with a single input port named
     *  "input". The name of the actor must be unique within the
     *  container or a NameDuplicationException is thrown. The
     *  container argument must not be null, or a NullPointerException
     *  will be thrown. The number of tokens produced by this actor is
     *  specified by the tokenLimit parameter. The actor will produce
     *  N=tokenLimit tokens unless tokenLimit < 0 in which case this
     *  actor will produce tokens indefinitely.
     *  @param cont The container of this actor.
     *  @param name The name of this actor.
     *  @param limit The number of tokens produced by this actor.
     *  @exception IllegalActionException If the port or tokenLimit
     *   parameter cannot be contained by this actor.
     *  @exception NameDuplicationException If the port or tokenLimit
     *   parameter name coincides with a port or parameter already
     *   in this actor.
     */
    public CSPMultiSource(TypedCompositeActor cont, String name,
    	    int limit) throws IllegalActionException,
            NameDuplicationException {
        super(cont, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
	output.setTypeEquals(BaseType.INT);
        tokenLimit = new Parameter( this, "tokenLimit",
        	(new IntToken(limit)) );
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This actor's output port.
     */
    public TypedIOPort output;

    /** The number of tokens produced by this actor. If this limit
     *  is set to -1, then produce output tokens indefinitely. The
     *  default value of this parameter is -1.
     */
    public Parameter tokenLimit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by producing IntTokens on the output port.
     *  If the tokenCount was not set to a nonnegative, integer value,
     *  then produce output tokens indefinitely. Otherwise, produce
     *  N output tokens for N = tokenCount.
     */
    public void fire() {
        try {
            int limit =
            	    ((IntToken)tokenLimit.getToken()).intValue();
            int count = 0;
            int size = output.getWidth();
            _branchCount = new int[size];
            int i = 0;
            boolean[] guards = new boolean[size];
            for (i = 0; i < size; i++) {
                _branchCount[i] = 0;
                guards[i] = true;
            }

            boolean continueCDO = true;
            while ( continueCDO ) {
                if( count > limit && limit >= 0 ) {
                    return;
                }
                Token t = new IntToken(count);
                ConditionalBranch[] branches = new ConditionalBranch[size];
                for (i = 0; i < size; i++) {
                    branches[i] = new ConditionalSend(guards[i],
                            output, i, i, t);
                }

                int successfulBranch = chooseBranch(branches);

                _branchCount[successfulBranch]++;
                boolean flag = false;
                for (i = 0; i < size; i++) {
                    if (successfulBranch == i) {
                        System.out.println(getName() + ": sent Token: " +
                                t.toString() + " to receiver " + i);
                        flag = true;
                    }
                }
                if (successfulBranch == -1) {
                    // all guards false so exit CDO
                    continueCDO = false;
                } else if (!flag) {
                    throw new TerminateProcessException(getName() + ": " +
                            "invalid branch id returned during execution " +
                            "of CDO.");
                }
                count++;
            }
        } catch (IllegalActionException ex) {
            throw new TerminateProcessException(getName() + ": could not " +
		    "create all branches for CDO.");
        }
	return;
    }

    /** Return false indicating that iteration of this actor should
     *  not continue.
     * @return false Indicating that iteration of this actor should
     *  should not continue.
     */
    public boolean postfire() {
        return false;
    }

    /** Discontinue the execution of this actor.
     */
    public void wrapup() {
        System.out.println("Invoking wrapup of CSPMultiSource...\n");
        for (int i = 0; i < output.getWidth(); i++) {
            System.out.println("MultiSource: Branch " + i +
                    " successfully  rendezvoused " +
                    _branchCount[i] + " times.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Array storing the number of times each branch rendezvous.
    private int[] _branchCount;

}





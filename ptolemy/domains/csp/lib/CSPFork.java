/* Fork in Sieve of Eratosthenes demo.

 Copyright (c) 1998 The Regents of the University of California.
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

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Fork
/**
Fork in Sieve of Eratosthenes demo. Each fork can only be used by 
one philosopher at a time. When it is not being used it can be 
claimed by either of the two philophers next to it. Once it has 
been claimed, it is not available until it is released by the 
philospopher holding it.
<p>
@author Neil Smyth
@version 

*/

public class CSPFork extends CSPActor {
    public CSPFork() throws IllegalActionException, NameDuplicationException{
        super();
        leftOut = new IOPort(this, "leftOut", false, true);
        leftIn = new IOPort(this, "leftIn", true, false);
        rightOut = new IOPort(this, "rightOut", false, true);
        rightIn = new IOPort(this, "rightIn", true, false);
    }

    public CSPFork(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         this(cont, name, 1);
    }

    public CSPFork(CompositeActor cont, String name, int depth)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         leftOut = new IOPort(this, "leftOut", false, true);
         leftIn = new IOPort(this, "leftIn", true, false);
         rightOut = new IOPort(this, "rightOut", false, true);
         rightIn = new IOPort(this, "rightIn", true, false);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        try {
            boolean guard = true;
            boolean continueCDO = true;
            ConditionalBranch[] branches = new ConditionalBranch[2];
            Token t = new IntToken(0);
            while (continueCDO) {
                // step 1
                branches[0] = new ConditionalSend(guard, leftOut, 0, 0, t);
                branches[1] = new ConditionalSend(guard, rightOut, 0, 1, t);

                // step 2
                int successfulBranch = chooseBranch(branches);

                // step 3
                if (successfulBranch == 0) {
                    // Print some info here
                    leftIn.get(0);
                } else if (successfulBranch == 1) {
                    // Print some info here
                    rightIn.get(0);
                } else if (successfulBranch == -1) {
                    // all guards false so exit CDO
                    continueCDO = false;
                } else {
                    throw new TerminateProcessException(getName() + ": " +
                            "branch id returned during execution of CDO.");
                }
            }
        } catch (IllegalActionException ex) {
            System.out.println(getName() + ": IllegalActionException, " +
                    "exiting" + ex.getMessage());
        } catch (NoTokenException ex) {
            System.out.println(getName() + ": cannot get  token.");
        }
    }

    public IOPort leftIn;
    public IOPort leftOut;
    public IOPort rightIn;
    public IOPort rightOut;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////
}

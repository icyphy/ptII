/* Accepts a Token from any channel connected to its port.

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
@AcceptedRating Red (nsmyth@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// CSPMultiSink
/**
Accepts a Token from any channel connected to its port. It uses a 
CDO construct to always be ready to accept a new Token.
The channels it can accept from is set at the start of each firing. 
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.domains.csp.kernel
*/
public class CSPMultiSink extends CSPActor {
    public CSPMultiSink() {
        super();
    }

    public CSPMultiSink  (CompositeActor cont, String name)
       throws IllegalActionException, NameDuplicationException {
	 super(cont, name);
	 input = new IOPort(this, "input", true, false);
	 input.setMultiport(true);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        try {
            int count = 0;
            int size = input.getWidth();
            _branchCount = new int[size];
            int i = 0;
            boolean[] guards = new boolean[size];
            for (i=0; i<size; i++) {
                _branchCount[i] = 0;
                guards[i] = true;
            }

            boolean continueCDO = true;
            while (continueCDO || (count < 25) ) {
                ConditionalBranch[] branches = new ConditionalBranch[size];
                for (i = 0; i < size; i++) {
                    branches[i] = new ConditionalReceive(guards[i],
                            input, i, i);
                }

                int successfulBranch = chooseBranch(branches);

                _branchCount[successfulBranch]++;
                boolean flag = false;
                for (i=0; i<size; i++) {
                    if (successfulBranch == i) {
                        Token t = branches[successfulBranch].getToken();
                        System.out.println(getName() + ": received Token: " +
                                t.toString() + " from receiver " + i);
                        flag = true;
                    }
                }
                if (successfulBranch == -1) {
                    // all guards false so exit CDO
                    continueCDO = false;
                } else if (!flag) {
                    throw new TerminateProcessException(getName() + ": " +
                            "branch id returned during execution of CDO.");
                }
                count++;
            }
        } catch (IllegalActionException ex) {
            throw new TerminateProcessException(getName() + ": Error: " + 
			"could not create ConditionalReceive branch");
        }
        return;
    }

    public boolean postfire() {
        return false;
    }

    public void wrapup() {
        System.out.println(Thread.currentThread().getName() + ":Invoking wrapup of CSPMultiSink...\n");
        for (int i=0; i<input.getWidth(); i++) {
            String str = "MultiSink: Branch " + i +  " successfully  rendez";
            System.out.println(str + "voused " + _branchCount[i] + " times.");
        }
    }

    public IOPort input;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Array storing the number of times each brach rendezvoused.
    private int[] _branchCount;
}

/* CSPCondGet

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
package ptolemy.domains.csp.kernel.test;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPCondGet

/**

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class CSPCondGet extends CSPGet {
    /**
     */
    public CSPCondGet(TypedCompositeActor cont, String name, int receiverCount)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _receiverCount = receiverCount;
        _truth = new boolean[receiverCount];
        _winningBranch = new boolean[receiverCount];

        for (int i = 0; i < receiverCount; i++) {
            _winningBranch[i] = false;
            _truth[i] = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    @Override
    public void fire() throws IllegalActionException {
        int numReceivers = 0;
        Receiver[][] rcvrs = inputPort.getReceivers();

        for (Receiver[] rcvr : rcvrs) {
            for (int j = 0; j < rcvr.length; j++) {
                numReceivers++;
            }
        }

        if (_receiverCount != numReceivers) {
            throw new IllegalActionException(getName()
                    + ": Error in the number"
                    + " of receivers. Be sure constructor is properly"
                    + " instantiated.");
        }

        ConditionalBranch[] brchs = new ConditionalBranch[numReceivers];

        for (int i = 0; i < numReceivers; i++) {
            brchs[i] = new ConditionalReceive(_truth[i], inputPort, i, i);
        }

        int winner = chooseBranch(brchs);

        _winningBranch[winner] = true;
    }

    /**
     */
    public boolean isWinner(int index) {
        return _winningBranch[index];
    }

    /**
     */
    public void setTruth(int index, boolean val) {
        _truth[index] = val;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _receiverCount;

    private boolean[] _truth;

    private boolean[] _winningBranch;
}

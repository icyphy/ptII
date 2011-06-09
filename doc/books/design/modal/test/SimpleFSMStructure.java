/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2010 The Regents of the University of California.
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
*/
package doc.books.design.modal.test;

import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.KernelException;

/**
 * SimpleFSMStructure class.
 *
 * @author eal
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class SimpleFSMStructure {

    public SimpleFSMStructure() {
        try {
            FSMActor actor = new FSMActor();
            State state1 = new State(actor, "State1");
            State state2 = new State(actor, "State2");
            Transition relation = new Transition(actor, "relation");
            Transition relation2 = new Transition(actor, "relation2");
            state1.incomingPort.link(relation2);
            state1.outgoingPort.link(relation);
            state2.incomingPort.link(relation);
            state2.outgoingPort.link(relation2);

        } catch (KernelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

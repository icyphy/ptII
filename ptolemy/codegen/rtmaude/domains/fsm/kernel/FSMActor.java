/* RTMaude Code generator helper class for the FSMActor class.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.rtmaude.domains.fsm.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////Director

/**
 * Generate RTMaude code for a FSMActor in DE domain.
 *
 * @see ptolemy.domains.fsm.kernel.FSMActor
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class FSMActor extends Entity {
    /**
     * Constructor method for the FSMActor adapter.
     * @param component the associated FSMActor
     */
    public FSMActor(ptolemy.domains.fsm.kernel.FSMActor component) {
        super(component);
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.Entity#getInfo(java.lang.String, java.util.List)
     */
    protected String getInfo(String name, List<String> parameters)
            throws IllegalActionException {
        ptolemy.domains.fsm.kernel.FSMActor fa = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        if (name.equals("initState")) {
            return fa.getInitialState().getName();
        }
        if (name.equals("transitions")) {
            ArrayList transitions = new ArrayList();
            for (State s : (List<State>) fa.entityList(State.class)) {
                transitions.addAll(s.outgoingPort.linkedRelationList());
            }
            return new ListTerm<Transition>("emptyTransitionSet", " ;" + _eol,
                    transitions) {
                public String item(Transition t) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(t)).generateTermCode();
                }
            }.generateCode();
        }
        return super.getInfo(name, parameters);
    }
}

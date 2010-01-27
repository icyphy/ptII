/* RTMaude Code generator helper class for the ModalModel class.

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
package ptolemy.codegen.rtmaude.domains.fsm.modal;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.codegen.rtmaude.actor.TypedCompositeActor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ModalModel

/**
 * Generate RTMaude code for a ModalModel in DE domain.
 *
 * @see ptolemy.domains.fsm.modal.ModalModel
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class ModalModel extends TypedCompositeActor {
    /**
     * Constructor method for the ModalModel adapter.
     * @param component the associated actor
     */
    public ModalModel(ptolemy.domains.fsm.modal.ModalModel component) {
        super(component);
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.actor.TypedCompositeActor#getInfo(java.lang.String, java.util.List)
     */
    protected String getInfo(String name, List<String> parameters)
            throws IllegalActionException {
        ptolemy.domains.fsm.modal.ModalModel mm = (ptolemy.domains.fsm.modal.ModalModel) getComponent();
        if (name.equals("controller")) {
            return mm.getController().getName();
        }
        if (name.equals("refinement")) {
            return new ListTerm<State>("empty", " ", mm.getController()
                    .entityList(State.class)) {
                public String item(State s) throws IllegalActionException {
                    Actor[] rfs = s.getRefinement();
                    if (rfs != null) {
                        StringBuffer code = new StringBuffer();
                        for (Actor a : rfs) {
                            code.append(_generateBlockCode("refineStateBlock",
                                    s.getName(), a.getName()));
                        }
                        return code.toString();
                    } else {
                        return null;
                    }
                }
            }.generateCode();
        }
        return super.getInfo(name, parameters);
    }
}

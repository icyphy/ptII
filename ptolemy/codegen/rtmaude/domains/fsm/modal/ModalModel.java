/* RTMaude Code generator helper class for the ModalModel class.

 Copyright (c) 2009 The Regents of the University of California.
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

import java.util.Map;

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
* @version $Id: ModalModel.java 53821 2009-04-12 19:12:45Z cxh $
* @Pt.ProposedRating Red (kquine)
*
*/
public class ModalModel extends TypedCompositeActor {

    public ModalModel(ptolemy.domains.fsm.modal.ModalModel component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        ptolemy.domains.fsm.modal.ModalModel mm = 
            (ptolemy.domains.fsm.modal.ModalModel) getComponent();
        
        atts.put("controller", "'" + mm.getController().getName());
        
        atts.put("refinement",
                new ListTerm<State>("empty"," ", 
                        mm.getController().entityList(State.class)) {
                    public String item(State s) throws IllegalActionException {
                        Actor[] rfs = s.getRefinement();
                        if (rfs != null) {
                            StringBuffer code = new StringBuffer();
                            for (Actor a : rfs) 
                                code.append(
                                        _generateBlockCode("refineStateBlock", 
                                                s.getName(), a.getName()));
                            return code.toString();
                        }
                        else
                            return null;
                    }
                }.generateCode()
            );
        return atts;
    }

}

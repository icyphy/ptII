package ptolemy.codegen.rtmaude.domains.fsm.modal;

import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.codegen.rtmaude.actor.TypedCompositeActor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;

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

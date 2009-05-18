package ptolemy.codegen.rtmaude.domains.fsm.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;

public class FSMActor extends Entity {
    
    public FSMActor(ptolemy.domains.fsm.kernel.FSMActor component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        ptolemy.domains.fsm.kernel.FSMActor fa = 
            (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        
        String initstate = fa.getInitialState().getName();
        
        atts.put("currState", "'" + initstate);
        atts.put("initState", "'" + initstate);
        
        ArrayList transitions = new ArrayList();
        for(State s : (List<State>)fa.entityList(State.class))
            transitions.addAll(s.outgoingPort.linkedRelationList());

        atts.put("transitions",
            new ListTerm<Transition>("emptyTransitionSet", " ;" + _eol, transitions) {
                public String item(Transition t) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(t)).generateTermCode();
                }
            }.generateCode()
        );

        return atts;
    }
    
}

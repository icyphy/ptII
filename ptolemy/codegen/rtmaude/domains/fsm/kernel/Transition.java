package ptolemy.codegen.rtmaude.domains.fsm.kernel;

import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;

public class Transition extends RTMaudeAdaptor {

    public Transition(ptolemy.domains.fsm.kernel.Transition component) {
        super(component);
    }

    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.domains.fsm.kernel.Transition t = 
            (ptolemy.domains.fsm.kernel.Transition) getComponent();
        ParseTreeCodeGenerator pcg = getParseTreeCodeGenerator();
        
        ASTPtRootNode pt = (new PtParser()).generateParseTree(t.getGuardExpression());
        pcg.evaluateParseTree(pt, null);
        String guard = pcg.generateFireCode();
        
        String set = ((RTMaudeAdaptor) _getHelper(t.setActions)).generateTermCode();
        String out = ((RTMaudeAdaptor) _getHelper(t.outputActions)).generateTermCode();
        
        return _generateBlockCode(defaultTermBlock,
                t.sourceState().getName(),
                t.destinationState().getName(),
                guard, out, set);
    }

}

package ptolemy.codegen.rtmaude.domains.fsm.kernel;

import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;

public class AbstractActionsAttribute extends RTMaudeAdaptor {

    public AbstractActionsAttribute(ptolemy.domains.fsm.kernel.AbstractActionsAttribute component) {
        super(component);
    }
    
    @Override
    public String generateTermCode() throws IllegalActionException {
        final ptolemy.domains.fsm.kernel.AbstractActionsAttribute aa = 
            (ptolemy.domains.fsm.kernel.AbstractActionsAttribute) getComponent();
        final ParseTreeCodeGenerator pcg = getParseTreeCodeGenerator();
        
        return new ListTerm<String>("emptyMap", " ;"+_eol, aa.getDestinationNameList()) {
            public String item(String aan) throws IllegalActionException {
                pcg.evaluateParseTree(aa.getParseTree(aan),null);
                return _generateBlockCode("mapBlock", aan, pcg.generateFireCode());
            }
        }.generateCode();
    }

}

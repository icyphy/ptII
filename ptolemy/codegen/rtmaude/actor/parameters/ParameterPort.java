package ptolemy.codegen.rtmaude.actor.parameters;

import ptolemy.codegen.rtmaude.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;

public class ParameterPort extends IOPort {
    
    public ParameterPort(ptolemy.actor.parameters.ParameterPort component) {
        super(component);
    }
    
    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.actor.parameters.ParameterPort p = 
            (ptolemy.actor.parameters.ParameterPort) getComponent();
        if (p.isOutput())
            throw new IllegalActionException("Not Input Port Parametor");
        
        return _generateBlockCode(defaultTermBlock,
                p.getName(), 
                p.getParameter().getName()
        );
    }
}

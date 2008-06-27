package ptolemy.data.properties.configuredSolvers.token;

import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class FirstValuePTS extends PropertyTokenSolver {

    public FirstValuePTS(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        useCase.setExpression("firstValueToken");
        useCase.setVisibility(Settable.NOT_EDITABLE);
        listeningMethod.setExpression("Input & Output Ports");        
        listeningMethod.setVisibility(Settable.NOT_EDITABLE);
    }        

}

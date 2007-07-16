package ptolemy.data.properties.token.firstValueToken;

import ptolemy.actor.IOPort;
import ptolemy.actor.TokenSentEvent;
import ptolemy.actor.TokenSentListener;
import ptolemy.data.Token;
import ptolemy.data.properties.token.PortValueHelper;
import ptolemy.data.properties.token.PortValueSolver;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.kernel.util.IllegalActionException;

public class FirstTokenSentListener implements TokenSentListener {

    private PortValueSolver _solver;

    public FirstTokenSentListener(PortValueSolver solver) {
        _solver = solver;
    }
    
    public void tokenSentEvent(TokenSentEvent event) {
        
        IOPort port = event.getPort();
        Token token = event.getToken();
        if (token == null) {
            token = event.getTokenArray()[0];
        } 

        try {
            ((PortValueHelper)_solver.getHelper(port.getContainer())).setEquals(port, new PropertyToken(token));
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

package ptolemy.data.properties.token.firstValueToken;

import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.IOPortEventListener;
import ptolemy.actor.TokenSentEvent;
import ptolemy.actor.TokenSentListener;
import ptolemy.data.Token;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.properties.token.PropertyTokenHelper;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;

public class FirstTokenSentListener implements TokenSentListener, IOPortEventListener {

    private PropertyTokenSolver _solver;

    public FirstTokenSentListener(PropertyTokenSolver solver) {
        _solver = solver;
    }
    
    public void tokenSentEvent(TokenSentEvent event) {
        
        IOPort port = event.getPort();
        Token token = event.getToken();
        if (token == null) {
            token = event.getTokenArray()[0];
        } 

        try {
            ((PropertyTokenHelper)_solver.getHelper(port.getContainer())).setEquals(port, new PropertyToken(token));
        } catch (IllegalActionException e) {
            assert false;
        }
    }

    public void portEvent(IOPortEvent event) {
        if (event.getEventType() != IOPortEvent.SEND) {
            return;
        }
            
        IOPort port = event.getPort();
        Token token = event.getToken();
        if (token == null) {
            token = event.getTokenArray()[0];
        } 

        try {
            ((PropertyTokenHelper)_solver.getHelper(port.getContainer())).setEquals(port, new PropertyToken(token));
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

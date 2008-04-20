package ptolemy.data.properties.token.firstValueToken;

import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.IOPortEventListener;
import ptolemy.actor.TokenGotEvent;
import ptolemy.actor.TokenGotListener;
import ptolemy.data.Token;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.properties.token.PropertyTokenHelper;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;

public class FirstTokenGotListener implements TokenGotListener, IOPortEventListener {

    public FirstTokenGotListener(PropertyTokenSolver solver) {
        _solver = solver;
    }
    
    public void tokenGotEvent(TokenGotEvent event) {
        
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
        if (event.getEventType() != IOPortEvent.GET_END) {
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
            assert false;
        }
    }

    private PropertyTokenSolver _solver;

}

package ptolemy.data.properties.token.firstValueToken;

import ptolemy.actor.IOPort;
import ptolemy.actor.TokenGotEvent;
import ptolemy.actor.TokenGotListener;
import ptolemy.data.Token;
import ptolemy.data.properties.token.PortValueHelper;
import ptolemy.data.properties.token.PortValueSolver;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.kernel.util.IllegalActionException;

public class FirstTokenGotListener implements TokenGotListener {

    public FirstTokenGotListener(PortValueSolver solver) {
        _solver = solver;
    }

    public void tokenGotEvent(TokenGotEvent event) {

        IOPort port = event.getPort();
        Token token = event.getToken();

        try {
            ((PortValueHelper) _solver.getHelper(port.getContainer()))
                    .setEquals(port, new PropertyToken(token));
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private PortValueSolver _solver;
}

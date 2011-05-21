package ptserver.actor;

import java.util.concurrent.ArrayBlockingQueue;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptserver.data.CommunicationToken;

public class RemoteSource extends RemoteActor {

    private ArrayBlockingQueue<CommunicationToken> tokenQueue;

    public RemoteSource(CompositeEntity container, ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, entity);
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        return !getTokenQueue().isEmpty();
    }

    @Override
    public void fire() throws IllegalActionException {
        CommunicationToken token = getTokenQueue().poll();
        for (Object p : this.portList()) {
            if (p instanceof IOPort) {
                IOPort port = (IOPort) p;
                int width = port.getWidth();
                for (int i = 0; i < width; i++) {
                    Token[] tokens = token.getTokens(port.getName(), i);
                    port.send(i, tokens, tokens.length);
                }
            }
        }
    }

    public void setTokenQueue(ArrayBlockingQueue<CommunicationToken> tokenQueue) {
        this.tokenQueue = tokenQueue;
    }

    public ArrayBlockingQueue<CommunicationToken> getTokenQueue() {
        return tokenQueue;
    }

}

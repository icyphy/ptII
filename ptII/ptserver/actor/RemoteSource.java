package ptserver.actor;

import java.util.concurrent.ArrayBlockingQueue;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class RemoteSource extends RemoteActor {

    private ArrayBlockingQueue<Token> tokenQueue;

    public RemoteSource(CompositeEntity container, ComponentEntity entity,
            ArrayBlockingQueue<Token> tokenQueue)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, entity);

        this.tokenQueue = tokenQueue;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            Token token = tokenQueue.take();
            for (Object p : this.portList()) {
                if (p instanceof IOPort) {
                    IOPort port = (IOPort) p;
                    port.send(0, token);
                }
            }
        } catch (InterruptedException e) {
        }
    }

}

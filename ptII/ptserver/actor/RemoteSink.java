package ptserver.actor;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptserver.communication.TokenPublisher;

public class RemoteSink extends RemoteActor {

    private TokenPublisher publisher;

    public RemoteSink(CompositeEntity container, ComponentEntity entity,
            TokenPublisher publisher) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        super(container, entity);

        this.publisher = publisher;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (Object p : this.portList()) {
            if (p instanceof IOPort) {
                IOPort port = (IOPort) p;

                int width = port.getWidth();

                for (int i = 0; i < width; i++) {
                    if (port.hasToken(i)) {
                        Token token = port.get(i);
                        publisher.sendToken(token);
                    }
                }
            }
        }
    }

}

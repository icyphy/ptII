package ptserver.actor;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptserver.communication.TokenPublisher;
import ptserver.data.CommunicationToken;

public class RemoteSink extends RemoteActor {

    private TokenPublisher publisher;

    public RemoteSink(CompositeEntity container, ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, entity);
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        for (Object p : this.portList()) {
            if (p instanceof IOPort) {
                IOPort port = (IOPort) p;
                int consumptionRate = 1;
                Attribute attribute = port.getAttribute("tokenConsumptionRate");
                if (attribute instanceof Settable) {
                    Settable settableAttribute = (Settable) attribute;
                    String value = settableAttribute.getExpression();
                    if (value != null) {
                        try {
                            consumptionRate = Integer
                                    .parseInt(settableAttribute.getExpression());
                        } catch (NumberFormatException ex) {
                        }
                    }

                }
                int width = port.getWidth();
                for (int i = 0; i < width; i++) {
                    if (!port.hasToken(i, consumptionRate)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void fire() throws IllegalActionException {
        CommunicationToken token = new CommunicationToken(
                getOriginalActorName());
        for (Object p : this.portList()) {
            if (p instanceof IOPort) {
                IOPort port = (IOPort) p;
                int consumptionRate = 1;
                Attribute attribute = port.getAttribute("tokenConsumptionRate");
                if (attribute instanceof Settable) {
                    Settable settableAttribute = (Settable) attribute;
                    String value = settableAttribute.getExpression();
                    if (value != null) {
                        try {
                            consumptionRate = Integer
                                    .parseInt(settableAttribute.getExpression());
                        } catch (NumberFormatException ex) {
                        }
                    }

                }
                int width = port.getWidth();
                token.addPort(port.getName(), width);
                for (int i = 0; i < width; i++) {
                    token.putTokens(port.getName(), i,
                            port.get(i, consumptionRate));
                }
            }
        }
        getPublisher().sendToken(token);
    }

    public void setPublisher(TokenPublisher publisher) {
        this.publisher = publisher;
    }

    public TokenPublisher getPublisher() {
        return publisher;
    }

}

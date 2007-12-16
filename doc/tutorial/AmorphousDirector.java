package doc.tutorial;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class AmorphousDirector extends Director {

    public AmorphousDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public Receiver newReceiver() {
        return new DelegatingReceiver();
    }

    public static class DelegatingReceiver extends AbstractReceiver {

        private Receiver _receiver;

        public DelegatingReceiver() {
            super();
            _receiver = new SDFReceiver();
        }

        public DelegatingReceiver(IOPort container)
                throws IllegalActionException {
            super(container);
            _receiver = new SDFReceiver(container);
        }

        public void clear() throws IllegalActionException {
            IOPort container = getContainer();
            if (container != null) {
                StringParameter receiverClass = (StringParameter) container
                        .getAttribute("receiverClass", StringParameter.class);
                if (receiverClass != null) {
                    String className = ((StringToken) receiverClass.getToken())
                            .stringValue();
                    try {
                        Class desiredClass = Class.forName(className);
                        _receiver = (Receiver) desiredClass.newInstance();
                    } catch (Exception e) {
                        throw new IllegalActionException(container, e,
                                "Invalid class for receiver: " + className);
                    }
                }
            }
            _receiver.clear();
        }

        public Token get() throws NoTokenException {
            return _receiver.get();
        }

        public boolean hasRoom() {
            return _receiver.hasRoom();
        }

        public boolean hasRoom(int numberOfTokens) {
            return _receiver.hasRoom(numberOfTokens);
        }

        public boolean hasToken() {
            return _receiver.hasToken();
        }

        public boolean hasToken(int numberOfTokens) {
            return _receiver.hasToken(numberOfTokens);
        }

        public void put(Token token) throws NoRoomException,
                IllegalActionException {
            _receiver.put(token);
        }
    }
}

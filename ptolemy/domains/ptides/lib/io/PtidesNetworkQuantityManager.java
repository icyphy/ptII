package ptolemy.domains.ptides.lib.io;

import java.util.ArrayList;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.qm.CompositeQuantityManager;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PtidesNetworkQuantityManager extends CompositeQuantityManager {

    public PtidesNetworkQuantityManager() throws IllegalActionException, NameDuplicationException {
        super(); 
    }

    public PtidesNetworkQuantityManager(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace); 
    }
    
    public PtidesNetworkQuantityManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    public void sendToken(Receiver source, Receiver receiver, Token token, IOPort port)
        throws IllegalActionException {
            prefire();
            
            PtidesBasicDirector director = (PtidesBasicDirector) 
                    ((CompositeActor)
                      ((Actor)source.getContainer().getContainer()).getContainer()).getDirector();
            
            String[] labels = new String[] { "timestamp", "microstep", "payload", "receiver" };
            Token[] values = new Token[] {
                    new DoubleToken(director.getModelTime()
                            .getDoubleValue()),
                    new IntToken(director.getMicrostep()), token, new ObjectToken(receiver) };
            RecordToken record = new RecordToken(labels, values);  
            
            for (int i = 0; i < port.insidePortList().size(); i++) { 
                ((IOPort)port.insidePortList().get(i)).getReceivers()[0][0].put(record);
            } 
            ((CompositeActor)getContainer()).getDirector().fireAtCurrentTime(this);
    }
    
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire()");
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            // Need to read from port parameters
            // first because in some domains (e.g. SDF)
            // the behavior of the schedule might depend on rate variables
            // set from ParameterPorts.
            for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                    .hasNext() && !_stopRequested;) {
                IOPort p = (IOPort) inputPorts.next();

                if (p instanceof ParameterPort) {
                    ((ParameterPort) p).getParameter().update();
                }
            }

            // Use the local director to transfer inputs from
            // everything that is not a port parameter.
            // The director will also update the schedule in
            // the process, if necessary.
            for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                    .hasNext() && !_stopRequested;) {
                IOPort p = (IOPort) inputPorts.next();

                if (!(p instanceof ParameterPort)) {
                    getDirector().transferInputs(p);
                }
            }

            if (_stopRequested) {
                return;
            }

            getDirector().fire();

            if (_stopRequested) {
                return;
            }

            Iterator<?> outports = outputPortList().iterator();
            while (outports.hasNext() && !_stopRequested) {
                IOPort p = (IOPort) outports.next();
                if (p.getInsideReceivers()[0][0].hasToken()) {
                    RecordToken token = (RecordToken) p.getInsideReceivers()[0][0].get();
                    ObjectToken receiverToken = (ObjectToken) token.get("receiver");
                    Receiver receiver = (Receiver) receiverToken.getValue();
                    Token payload = (Token) token.get("payload");
                    receiver.put(payload);
                }
            } 
        } finally {
            _workspace.doneReading();
        }
 
    }

}

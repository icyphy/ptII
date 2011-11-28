package ptolemy.domains.ptides.lib.io;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class NetworkTransmitterPort extends TypedIOPort {

    public Parameter deviceDelay;
    public Parameter deviceDelayBound; 
    
    public NetworkTransmitterPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.setOutput(true);
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE); 
    }
 
    @Override
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException { 
        PtidesBasicDirector director = (PtidesBasicDirector) ((CompositeActor)getContainer()).getDirector();
        
        String[] labels = new String[] { timestamp, microstep, payload };
        Token[] values = new Token[] {
                new DoubleToken(director.getModelTime()
                        .getDoubleValue()),
                new IntToken(director.getMicrostep()), token };
        RecordToken record = new RecordToken(labels, values); 
        
        super.send(channelIndex, record);
    }
    
    public Token convert(Token token) throws IllegalActionException { 
        return token;
    }
    
    
    @Override
    protected void _checkType(Token token) throws IllegalActionException {
        // do nothing
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** label of the microstep that is transmitted within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** label of the payload that is transmitted within the RecordToken.
     */
    private static final String payload = "payload";
    
}

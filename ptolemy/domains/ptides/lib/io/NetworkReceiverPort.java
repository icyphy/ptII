package ptolemy.domains.ptides.lib.io;

import java.util.HashSet;
import java.util.Set;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexType;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class NetworkReceiverPort extends TypedIOPort {

    public Parameter deviceDelay;
    public Parameter deviceDelayBound;
    public Parameter networkDelayBound;
    
    public NetworkReceiverPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.setInput(true);  
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        networkDelayBound = new Parameter(this, "networkDelayBound");
        networkDelayBound.setExpression("0.0");
        networkDelayBound.setTypeEquals(BaseType.DOUBLE); 
    }
     
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        PtidesBasicDirector director = (PtidesBasicDirector) ((CompositeActor)getContainer()).getDirector();

        RecordToken record = (RecordToken) token;

        if (record.labelSet().size() != 3) {
            throw new IllegalActionException(
                    "the input record token has a size not equal to 3: "
                            + "Here we assume the Record is of types: timestamp"
                            + " + microstep + token");
        }

        Time recordTimeStamp = new Time(director,
                ((DoubleToken) (record.get(timestamp))).doubleValue());

        int recordMicrostep = ((IntToken) (record.get(microstep)))
                .intValue(); 
        
        Time lastModelTime = director.getModelTime();
        int lastMicrostep = director.getMicrostep();
        director.setTag(recordTimeStamp, recordMicrostep);
        
        director.setTag(lastModelTime, lastMicrostep);
        super.sendInside(channelIndex, record.get(payload));
    }

    
    @Override
    protected void _checkType(Token token) throws IllegalActionException {
        // do nothing
    }
    
    @Override
    public Token convert(Token token) throws IllegalActionException { 
        return token;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////



    /** Label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** Label of the microstep that is transmitted within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** Label of the payload that's transmitted within the RecordToken.
     */
    private static final String payload = "payload";
    
}

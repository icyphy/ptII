/* Network receiver port.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY

*/



package ptolemy.domains.ptides.lib.io;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesDirector;
import ptolemy.domains.ptides.kernel.PtidesEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This port provides a specialized TypedIOPort for network receivers
 *  used in Ptides. This port just specializes parameters.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class NetworkReceiverPort extends PtidesPort {
    
    /** Create a new NetworkReceiverPort with a given container and a name.
     * @param container The container of the port. 
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public NetworkReceiverPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.setInput(true);  
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        sourcePlatformDelayBound = new Parameter(this, "sourcePlatformDelayBound");
        sourcePlatformDelayBound.setExpression("0.0");
        sourcePlatformDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        networkDelayBound = new Parameter(this, "networkDelayBound");
        networkDelayBound.setExpression("0.0");
        networkDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
    }
    
    /** Return the custom shape for this port.
     *  @return List of coordinates representing the shape.
     */
    public List<Integer[]> getCoordinatesForShape() {
        List<Integer[]> coordinates = new ArrayList<Integer[]>();
        coordinates.add(new Integer[]{-8, 8});
        coordinates.add(new Integer[]{8, 8});
        coordinates.add(new Integer[]{8, 4});
        coordinates.add(new Integer[]{12, 4});
        coordinates.add(new Integer[]{12, -4});
        coordinates.add(new Integer[]{8, -4});
        coordinates.add(new Integer[]{8, -8}); 
        coordinates.add(new Integer[]{-8, -8});
        return coordinates;
    }
    
    /** Device delay parameter that defaults to the double value 0.0. */
    public Parameter deviceDelay;
    
    /** Device delay bound parameter that defaults to the double value 0.0. */
    public Parameter deviceDelayBound;
    
    /** Network delay bound parameter that defaults to the double value 0.0. */
    public Parameter networkDelayBound; 
    
    /** Source platform delay bound parameter that defaults to the double value 0.0. */
    public Parameter sourcePlatformDelayBound; 
    
     
    /** Send Token inside. Tokens received on this port are recordTokens. Only the
     *  payload of the RecordToken should be sent inside. 
     *  @param channelIndex Channel token is sent to.
     *  @param token Token to be sent.
     *  @throws IllegalActionException If received token is not a record token 
     *  with the fields timestamp, microstep and payload.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        PtidesDirector director = (PtidesDirector) ((CompositeActor)getContainer()).getDirector();

        if (!(token instanceof RecordToken) || ((RecordToken)token).labelSet().size() != 3) {
            throw new IllegalActionException(this, 
                    "The input token is not a RecordToken or " +
                    "does not have a size not equal to 3: "
                            + "Here we assume the Record is of types: timestamp"
                            + " + microstep + token");
        }
        
        RecordToken record = (RecordToken) token;

        Time recordTimeStamp = new Time(director,
                ((DoubleToken) (record.get(timestamp))).doubleValue());

        int recordMicrostep = ((IntToken) (record.get(microstep)))
                .intValue(); 
        
        

        Receiver[][] farReceivers = deepGetReceivers(); 
        for (int i = 0; i < farReceivers[channelIndex].length; i++) { 
            director.addInputEvent(new PtidesEvent(this, channelIndex, recordTimeStamp, 
                    recordMicrostep, -1, (Token) record.get(payload), farReceivers[channelIndex][i]), 
                    ((DoubleToken)deviceDelay.getToken()).doubleValue());
                    
        } 
    }
    
    /** Override conversion such that only payload of recordtoken is
     *  converted. 
     *  FIXME: Is this enough?.
     *  @param token Token to be converted.
     *  @throws IllegalActionException If payload token cannot be converted.
     */
    public Token convert(Token token) throws IllegalActionException { 
        Type type = getType();
        if (type.equals((((RecordToken)token).get(payload)).getType())) {
            return token;
        } else {
            Token newToken = type.convert(((RecordToken)token).get(payload));
            String[] labels = new String[] { timestamp, microstep, payload };
            Token[] values = new Token[] {
                    (((RecordToken)token).get(timestamp)),
                    (((RecordToken)token).get(microstep)), newToken };
            RecordToken record = new RecordToken(labels, values); 
            return record;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

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

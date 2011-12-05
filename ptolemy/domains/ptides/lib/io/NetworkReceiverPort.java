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
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
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
     
    /** Send Token inside. Tokens received on this port are recordTokens. Only the
     *  payload of the RecordToken should be sent inside. 
     *  @param channelIndex Channel token is sent to.
     *  @param token Token to be sent.
     *  @throws IllegalActionException If received token is not a record token 
     *  with the fields timestamp, microstep and payload.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        PtidesBasicDirector director = (PtidesBasicDirector) ((CompositeActor)getContainer()).getDirector();

        if (!(token instanceof RecordToken) || ((RecordToken)token).labelSet().size() != 3) {
            throw new IllegalActionException(
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
        
        Time lastModelTime = director.getModelTime();
        int lastMicrostep = director.getMicrostep();
        director.setTag(recordTimeStamp, recordMicrostep);
        
        director.setTag(lastModelTime, lastMicrostep);
        director.setModelTime(recordTimeStamp);
        super.sendInside(channelIndex, record.get(payload));
    }

    
    /** Override Type checking with empty method. Otherwise the conversion
     *  to a RecordToken which is performed in the send method causes errors
     *  in the simulaiton.
     *  FIXME: Find better solution for type checking.
     *  @param token Token to be type-checked.
     */
    protected void _checkType(Token token) throws IllegalActionException {
        // do nothing
    }
    
    /** Override Type checking with empty method. Otherwise the conversion
     *  to a RecordToken which is performed in the send method causes errors
     *  in the simulaiton.
     *  FIXME: Find better solution for type checking.
     *  @param token Token to be type-checked.
     */
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

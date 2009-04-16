package ptolemy.domains.ptides.kernel;

import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/** This class is used for networking messages transmitted between platforms. When the
 *  messages are transmitted, the timestamp of these messages (tokenValue) must be preserved,
 *  which is saved in the timestamp field.
 * 
 *  @author jiazou
 *
 */
public class TimedToken extends Token {

    /** Construct a DoubleToken with value 0.0.
     */
    public TimedToken(Time timestamp, Token tokenValue) {
        this.setTimestamp(timestamp);
        this.setTokenValue(tokenValue);
    }
    
    /** Convert the specified token into a token with type equal
     *  to the type returned by getType(). If the token is already
     *  of this type, then simply return the specified token.
     *  @param token The token to convert.
     *  @return The converted token.
     *  @exception IllegalActionException If the conversion is
     *   invalid.
     */
    public Token convert(Token token) throws IllegalActionException {
        Type type = getType();

        if (type.equals(token.getType())) {
            return token;
        } else {
            Token newToken = type.convert(token);
            return newToken;
        }
    }
    
    /** get timestamp
     *  @return timestamp
     */
    public Time getTimestamp() {
        return timestamp;
    }

    /** get tokenValue
     *  @return tokenValue
     */
    public Token getTokenValue() {
        return tokenValue;
    }
    
    /** the Type of the timed token inherents the type of the tokenValue type 
     *  The reason is that when data is sent out at composite actors, they
     *  need to have the correct type.
     */
    public Type getType() {
        return tokenValue.getType();
    }

    /** set timestamp
     *  @param timestamp
     */
    public void setTimestamp(Time timestamp) {
        this.timestamp = timestamp;
    }

    /** set tokenValue
     *  @param tokenValue
     */
    public void setTokenValue(Token tokenValue) {
        this.tokenValue = tokenValue;
    }

    /** Timestamp is the timestamp of the associated token
     */
    private Time timestamp;
    
    /** TokenValue is the original Token
     */
    private Token tokenValue;
}

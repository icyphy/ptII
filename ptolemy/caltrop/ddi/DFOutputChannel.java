package ptolemy.caltrop.ddi;

import caltrop.interpreter.OutputChannel;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.CalIOException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 */
class DFOutputChannel implements OutputChannel {

    public void put(Object a) {
        try {
            port.send(channel, (Token)a);
        } catch (IllegalActionException e) {
            throw new CalIOException("Could not send token.", e);
        } catch (NoRoomException e) {
            throw new CalIOException("No room for sending token.", e);
        } catch (ClassCastException e) {
            throw new CalIOException("Token not of valid token type.", e);
        }
    }

    //

    public DFOutputChannel(TypedIOPort port, int channel) {
        this.port = port;
        this.channel = channel;
    }

    public String toString() {
        return "(DFOutputChannel " + channel + " at " + port.toString() + ")";
    }

    private TypedIOPort port;
    private int channel;
}

package ptolemy.caltrop.ddi;

import caltrop.interpreter.InputChannel;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.CalIOException;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 */
class DFInputChannel implements InputChannel {

    //
    //  InputChannel
    //

    public Object get(int n) {
        int m = n - buffer.size() + 1;
        if (m <= 0)
            return buffer.get(n);
        try {
            if (!port.hasToken(channel, m)) {
                throw new CalIOException("Insufficient number of tokens.");
            }
            for (int i = 0; i < m; i++) {
                buffer.add(port.get(channel));
            }
            return buffer.get(n);
        } catch (IllegalActionException e) {
            throw new CalIOException("Could not read tokens.", e);
        }
    }

    public void reset() {
        buffer.clear();
    }

    public boolean hasAvailable(int n) {
        int m = n - buffer.size();
        if (m <= 0)
            return true;
        try {
            if (channel < port.getWidth())
                return port.hasToken(channel, m);
            else
                return n == 0;
        } catch (IllegalActionException e) {
            throw new CalIOException("Could not test for presence of tokens. (" + e.getMessage() + ")", e);
        }
    }


    //

    public DFInputChannel(TypedIOPort port, int channel) {
        this.port = port;
        this.channel = channel;
        this.buffer = new ArrayList();
    }

    public String toString() {
        return "(DFInputChannel " + channel + " at " + port.toString() + ")";
    }

    private TypedIOPort port;
    private int channel;
    private List buffer;
}

package ptolemy.codegen.rtmaude.actor;

import ptolemy.actor.Director;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;

public class IOPort extends RTMaudeAdaptor implements PortCodeGenerator {
    
    /** Construct the code generator helper associated
     *  with the given IOPort.
     *  @param component The associated component.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.actor.IOPort p = (ptolemy.actor.IOPort) getComponent();
        if ( p.getWidth() > 1 )
            return _generateBlockCode("multiBlock", p.getName());
        else
            return _generateBlockCode(defaultTermBlock,
                    p.getName(), 
                    (p.isInput() && p.isOutput() ? 
                            "InOut" : (p.isInput() ? "In" : "Out")
                    ) + "Port"
        );
    }

    public String generateCodeForGet(String channel)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateCodeForSend(String channel, String dataToken)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateOffset(String offset, int channel, boolean isWrite,
            Director directorHelper) throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public int getBufferSize(int channelNumber) throws IllegalActionException {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object getReadOffset(int channelNumber)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getWriteOffset(int channelNumber)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String initializeOffsets() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setBufferSize(int channelNumber, int bufferSize) {
        // TODO Auto-generated method stub
        
    }

    public void setReadOffset(int channelNumber, Object readOffset) {
        // TODO Auto-generated method stub
        
    }

    public void setWriteOffset(int channelNumber, Object writeOffset) {
        // TODO Auto-generated method stub
        
    }

    public String updateConnectedPortsOffset(int rate, Director director)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public String updateOffset(int rate, Director directorHelper)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }
    
}

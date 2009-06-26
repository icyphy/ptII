package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel;

import ptolemy.actor.IOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.Receiver;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/** 
 * 
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
@version $Id$
@since Ptolemy II 7.1
 *
 */
public class SDFReceiver extends Receiver {

    public SDFReceiver(ptolemy.domains.sdf.kernel.SDFReceiver receiver) throws IllegalActionException {
        super(receiver);
    }

    public String generateGetCode() throws IllegalActionException {
        IOPort port = getReceiver().getContainer();
        int channel = port.getChannelForReceiver(getReceiver());

        ProgramCodeGeneratorAdapter helper = 
            (ProgramCodeGeneratorAdapter) getAdapter(port.getContainer());

        return helper.processCode("$ref(" + port.getName() 
                + "#" + channel + ")");
    }

    public String generateHasTokenCode() throws IllegalActionException {
        return "true";  // Assume "true" is a defined constant.
    }

    public String generatePutCode(String token) throws IllegalActionException{
        IOPort port = getReceiver().getContainer();
        int channel = port.getChannelForReceiver(getReceiver());

        ProgramCodeGeneratorAdapter helper = 
            (ProgramCodeGeneratorAdapter) getAdapter(port.getContainer());
        
        return helper.processCode("$ref(" + port.getName() 
                + "#" + channel + ")") + " = " + token + ";" + _eol;
    }

    
    //$send(port#channel) ==> port_channel[writeOffset]
    //$get(port#channel) ==> port_channel[readOffset]
}

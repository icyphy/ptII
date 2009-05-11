package ptolemy.codegen.c.domains.de.kernel;

import ptolemy.actor.IOPort;
import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;

/** 
 * 
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *
 */
public class DEReceiver extends ptolemy.codegen.c.actor.Receiver {

    public DEReceiver(ptolemy.domains.de.kernel.DEReceiver receiver) {
        super(receiver);
        // TODO Auto-generated constructor stub
    }
    
    public String generateCodeForGet() throws IllegalActionException {
        return "";
    }
    
    public String generateCodeForHasToken(int channel) throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        IOPort port = ((ptolemy.domains.de.kernel.DEReceiver)getObject()).getContainer();
        code.append("Event_Head_" + port.getContainer().getName() + "_" + port.getName() 
                            + "[" + channel + "] != NULL");
        return code.toString();
    }
    
    public String generateCodeForPut(PartialResult token) throws IllegalActionException{
        return "";
    }
    
}

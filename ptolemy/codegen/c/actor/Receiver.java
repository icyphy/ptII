package ptolemy.codegen.c.actor;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** 
 * 
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *
 */
public class Receiver extends CodeGeneratorHelper {

    public Receiver() {
        // TODO Auto-generated constructor stub
    }

    public Receiver(ptolemy.actor.Receiver receiver) {
        super(receiver);
        // TODO Auto-generated constructor stub
    }

    public Receiver(NamedObj component, String name) {
        super(component, name);
        // TODO Auto-generated constructor stub
    }

    public Receiver(Object object) {
        super(object);
        // TODO Auto-generated constructor stub
    }
    
    public String generateCodeForGet(int channel) throws IllegalActionException {
        return "";
    }
    
    public String generateCodeForHasToken(int channel) throws IllegalActionException{
        return "1";
    }
    
    public String generateCodeForPut(ptolemy.codegen.c.actor.IOPort sourcePort, String token) throws IllegalActionException{
        return "";
    }
    
    protected ptolemy.actor.Receiver _receiver;
}

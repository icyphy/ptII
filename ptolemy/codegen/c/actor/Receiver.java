package ptolemy.codegen.c.actor;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 *
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *
 */
public class Receiver extends CCodeGeneratorHelper {

    public Receiver(ptolemy.actor.Receiver receiver) {
        super(receiver);
    }

    public String generateCodeForGet(int channel) throws IllegalActionException {
        return "";
    }

    public String generateCodeForHasToken(int channel) throws IllegalActionException{
        return "1";
    }

    public String generateCodeForPut(String token) throws IllegalActionException{
        return "";
    }

    public ptolemy.actor.Receiver getReceiver() {
        return (ptolemy.actor.Receiver) getObject();
    }

}

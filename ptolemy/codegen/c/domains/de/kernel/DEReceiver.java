package ptolemy.codegen.c.domains.de.kernel;

import ptolemy.actor.IOPort;
import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;

/**
 *
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
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
        IOPort port = getReceiver().getContainer();
        return "Event_Head_" + generateName(port) + "[" + channel + "] != NULL";
    }

    public String generateCodeForPut(PartialResult token) throws IllegalActionException{
        return "";
    }

}

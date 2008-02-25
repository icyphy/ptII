package ptolemy.domains.ptides.kernel;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.domains.dde.kernel.NullToken;
import ptolemy.domains.dde.kernel.ReceiverComparator;
import ptolemy.kernel.util.IllegalActionException;
/**
 * @author Patricia Derler
 */
public class TimeKeeper4Ptides extends TimeKeeper {

	public TimeKeeper4Ptides(Actor actor) throws IllegalActionException {
        super(actor);
    }
	
    
    
    public synchronized void _setReceiverPriorities() throws IllegalActionException {
        LinkedList listOfPorts = new LinkedList();
        Iterator inputPorts = _actor.inputPortList().iterator();

        if (!inputPorts.hasNext()) {
            return;
        }

        while (inputPorts.hasNext()) {
            listOfPorts.addLast(inputPorts.next());
        }

        int cnt = 0;
        int currentPriority = 0;

        while (cnt < listOfPorts.size()) {
            IOPort port = (IOPort) listOfPorts.get(cnt);
            Receiver[][] receivers = port.getReceivers();

            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    ((DDEReceiver4Ptides) receivers[i][j])._priority = currentPriority;

                    //
                    // Is the following necessary??
                    //
                    updateReceiverList((DDEReceiver4Ptides) receivers[i][j]);

                    currentPriority++;
                }
            }

            cnt++;
        }
    }
	
}

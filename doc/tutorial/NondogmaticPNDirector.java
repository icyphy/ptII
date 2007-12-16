package doc.tutorial;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class NondogmaticPNDirector extends PNDirector {

    public NondogmaticPNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public Receiver newReceiver() {
        return new FlexibleReceiver();
    }

    public static class FlexibleReceiver extends PNQueueReceiver {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public boolean hasToken() {
            IOPort port = getContainer();
            Attribute attribute = port.getAttribute("tellTheTruth");
            if (attribute == null) {
                return super.hasToken();
            }
            // Tell the truth...
            return _queue.size() > 0;
        }
    }
}

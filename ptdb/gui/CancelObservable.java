package ptdb.gui;

import java.util.Observable;

///////////////////////////////////////////////////////////////////
//// CancelObservable

/**
 * An extension of Observable to be used for canceling of model searches.
 * Currently, no added capability.  However, there may be a need
 * to require additional action on the part of observers upon cancellation.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */
public class CancelObservable extends Observable {

    /**
     * Send notification to registered observers.
     * Currently, only calls super method.
     */
    public void notifyObservers() {

        super.notifyObservers();

    }
}

// A state that is passed through in a firing of the FSM.
package ptolemy.domains.modal.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// TransientState

/**
 A state that is passed through in a firing of the FSM.
 FIXME: This is not yet implemented! Don't use it!!!!

 @author Edward A. Lee, Christian Motika, Miro Spoenemann
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see State
 @see FSMActor
 @see FSMDirector
*/
public class TransientState extends State {

    /** Construct a transient state.
     * @param container The container.
     * @param name The name.
     * @exception IllegalActionException If the superclass throws it.
     * @exception NameDuplicationException If the superclass throws it.
     */
    public TransientState(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: Make sure this attribute is never set to true.
        // Just making it invisible in the GUI is not enough.
        isFinalState.setVisibility(Settable.NONE);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"0,0 10,10 20,0 10,-10\" style=\"fill:#000000\"/>\n"
                + "</svg>\n");
        new SingletonAttribute(this, "_hideName");
    }
}

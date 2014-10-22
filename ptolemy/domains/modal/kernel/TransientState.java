/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
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
 @since Ptolemy II 10.0
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

        _attachText(
                "_iconDescription",
                "<svg>\n"
                        + "<polygon points=\"0,0 10,10 20,0 10,-10\" style=\"fill:#000000\"/>\n"
                        + "</svg>\n");
        new SingletonAttribute(this, "_hideName");
    }
}

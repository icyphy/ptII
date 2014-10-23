/* Record messages from the PetriNetDirector.

 Copyright (c) 2010-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.petrinet.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.domains.petrinet.kernel.PetriNetDisplayer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PetriNetRecorder

/**
 Accept messages from a PetriNetDirector.

 <p>This class is a non-graphical version of
 {@link ptolemy.domains.petrinet.lib.gui.PetriNetDisplay}.</p>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PetriNetRecorder extends TypedAtomicActor implements
PetriNetDisplayer {
    /** Construct an actor that accepts descriptions from the
     *  PetriNetDirector.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PetriNetRecorder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        messages = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the messages recorded since initialize() was called.
     *  @return The messages recorded since initialize() was called.
     */
    public String getMessages() {
        return messages.toString();
    }

    /** Initialize the StringBuffer used to messages.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        messages = new StringBuffer();
    }

    /** In this class, do nothing.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void openDisplay() throws IllegalActionException {
    }

    /** Set the text for the display.  This method is called by the
     * PetriNetDirector.
     *
     * @param text
     *          The text to be shown in the display.
     */
    @Override
    public void setText(String text) {
        messages.append(text);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** This string represents the evolution of the Petri Net and should
     * be set by the PetriNetDirector.
     */
    StringBuffer messages;
}

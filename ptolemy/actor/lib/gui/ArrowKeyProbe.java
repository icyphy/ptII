/* An actor which pops up a keystroke-sensing JFrame.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.gui;

import ptolemy.data.IntToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ArrowKeySensor

/**
 Detect when the user presses or releases an arrow key and produce an
 integer on the corresponding output.

 <p>When this actor is preinitialized, it pops up a new JFrame window on
 the desktop, usually in the upper left hand corner of the screen.
 When this JFrame has the focus (such as when it has been clicked on)
 it is capable of sensing keystrokes.

 <p>This actor senses only the four non-numeric-pad arrow-key
 keystrokes.  This actor responds to key releases as well as key
 presses.  Upon each key press, the integer 1 is broadcast from the
 corresponding output.  Upon each key release, the integer 0 is output.

 <p>This actor contains a private inner class which generated the JFrame.
 The frame sets up call-backs which react to the keystrokes.  When called,
 these call the director's fireAtCurrentTime() method.  This causes
 the director to call fire() on the actor.   The actor then broadcasts
 tokens from one or both outputs depending on which keystroke(s) have
 occurred since the actor was last fired.

 <p>NOTE: This actor only works in the DE domain due to its reliance on
 this director's fireAtCurrentTime() method.

 @author Winthrop Williams
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class ArrowKeyProbe extends ArrowKeySensor {

    /**
     * Create an actor that detects user presses on the arrow key.
     *
     * @param container The container for this actor.
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public ArrowKeyProbe(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast the integer value 1 for each key pressed and 0 for
     *  each released.
     */
    @Override
    public synchronized void fire() throws IllegalActionException {
        //super.fire();
        // Broadcast key presses.
        if (_upKeyPressed) {
            //_upKeyPressed = false;
            upArrow.broadcast(new IntToken(1));
        } else {
            upArrow.broadcast(new IntToken(0));
        }

        if (_leftKeyPressed) {
            leftArrow.broadcast(new IntToken(1));
        } else {
            leftArrow.broadcast(new IntToken(0));
        }

        if (_rightKeyPressed) {
            rightArrow.broadcast(new IntToken(1));
        } else {
            rightArrow.broadcast(new IntToken(0));
        }

        if (_downKeyPressed) {
            downArrow.broadcast(new IntToken(1));
        } else {
            downArrow.broadcast(new IntToken(0));
        }
    }
}

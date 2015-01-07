/* TimeDisplay test actor

 Copyright (c) 2000-2013 The Regents of the University of California.
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
 @ProposedRating Red (cxh)
 @AcceptedRating Red (cxh)
 */
package ptolemy.actor.lib.gui;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import ptolemy.actor.Director;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.data.IntToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TimedDisplay

/**
 * Display the model time and the input.
 * @author  Christopher Brooks, based on dt/kernel/test/TimedDisplay.java by Chamberlain Fong
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)n
 */
public class TimedDisplay extends Display implements SequenceActor {

    /** Construct an actor with an input multiport of type GENERAL that
     *  displays the model time and the value of the input.   
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return a string describing the model time of the containing director 
     *  a colon and the input on channel i.
     *  @param i The channel
     *  @return A string representation of the input, or null if there
     *  is nothing to display. If there is no director, then only the
     *  value is returned.
     *  @exception IllegalActionException If reading the input fails.
     */
    protected String _getInputString(int i) throws IllegalActionException {
        String value = super._getInputString(i);

        Director director = getDirector();
        if (director != null) {
            return director.getModelTime() + ": " + value;
        }
        return value;
    }
}

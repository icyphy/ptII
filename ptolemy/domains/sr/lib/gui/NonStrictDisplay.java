/* Display the values of the tokens arriving on the input channels along
 with the associated time in a text area on the screen.

 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.sr.lib.gui;

import ptolemy.actor.lib.gui.Display;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 Display the inputs in a text area.
 This overrides the base class to tolerate unknown inputs.

 @author  Paul Whitaker, Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (pwhitake)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class NonStrictDisplay extends Display {
    /** Construct an actor with an input multiport of type GENERAL.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonStrictDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false. This actor displays "undefined" when the input
     *  receiver has status unknown.
     *
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a string describing the input on channel i.
     *  This is a protected method to allow subclasses to override
     *  how inputs are observed.
     *  @param i The channel
     *  @return A string representation of the input, or the string
     *   "absent" or "unknown" if the input is absent or unknown.
     *  @exception IllegalActionException If reading the input fails.
     */
    @Override
    protected String _getInputString(int i) throws IllegalActionException {
        if (input.isKnown(i)) {
            if (input.hasToken(i)) {
                return super._getInputString(i);
            } else {
                return _ABSENT_STRING;
            }
        } else {
            return _UNDEFINED_STRING;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static final String _ABSENT_STRING = "absent";

    private static final String _UNDEFINED_STRING = "unknown";
}

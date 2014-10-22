/* An actor that displays all tokens present on the inputs on screen.

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
package ptolemy.domains.taskpt.lib.gui;

import ptolemy.actor.lib.gui.Display;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DisplayAll

/** An actor that displays the values of all tokens present on the input channels on screen
 * (in contrast to one token per channel in the base class).
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 * @see ptolemy.actor.lib.gui.Display
 */
public class DisplayAll extends Display {

    /** Construct an actor with an input multiport of type GENERAL.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException Thrown if the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Thrown if the container already has an
     *   actor with this name.
     */
    public DisplayAll(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read all tokens from all input channels and display its
     *  string value on the screen. Each value is terminated
     *  with a newline character. Order display of tokens by position in channel
     *  first, then by channel.
     *
     *  @exception IllegalActionException Thrown if there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        //  TODO: Order by channel first and then by position in channel will be nicer,
        //  but this requires to override the Display.postfire() method completely and
        //  will result in some code copying.

        boolean result = true;
        boolean hasTokensSomewhere = true;
        int width = input.getWidth();
        while (hasTokensSomewhere) {
            result = super.postfire();
            hasTokensSomewhere = false;
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    hasTokensSomewhere = true;
                    break;
                }
            }

        }

        return result;
    }

}

/* An actor that displays the status and value of input tokens in a text area.

 @Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import javax.swing.text.BadLocationException;

import ptolemy.actor.lib.gui.Display;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 Display the values of the tokens arriving on the input channels along
 with the associated time in a text area on the screen.  If the value is
 undefined or known to be absent, that information is indicated instead.
 Each input token is written on a separate line.  The input type can be
 of any type.  If the input happens to be a StringToken,
 then the surrounding quotation marks are stripped before printing
 the value of the token.  Thus, string-valued tokens can be used to
 generate arbitrary textual output, at one token per line.
 Tokens are read from the input only in
 the postfire() method, to allow them to settle in domains where they
 converge to a fixed point.

 @author  Paul Whitaker, Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
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
        new Attribute(this, "_nonStrictMarker");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input channel and display its
     *  string value along with the current time on the screen.  Each
     *  value is terminated with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();

        for (int i = 0; i < width; i++) {
            String value;

            if (input.isKnown(i)) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);

                    // If the window has been deleted, read the
                    // rest of the inputs.
                    if (textArea == null) {
                        continue;
                    }

                    value = token.toString();

                    // If it is a pure string, strip the quotation marks.
                    if ((value.length() > 1) && value.startsWith("\"")
                            && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                } else {
                    value = ABSENT_STRING;
                }
            } else {
                value = UNDEFINED_STRING;
            }

            textArea.append(value);

            // Append a newline character.
            if (width > (i + 1)) {
                textArea.append("\n");
            }

            // Regrettably, the default in swing is that the top
            // of the textArea is visible, not the most recent text.
            // So we have to manually move the scrollbar.
            // The (undocumented) way to do this is to set the
            // caret position (despite the fact that the caret
            // is already where want it).
            try {
                int lineOffset = textArea.getLineStartOffset(textArea
                        .getLineCount() - 1);
                textArea.setCaretPosition(lineOffset);
            } catch (BadLocationException ex) {
                // Ignore ... worst case is that the scrollbar
                // doesn't move.
            }
        }

        if (textArea != null) {
            textArea.append("\n");
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static final String ABSENT_STRING = "absent";

    private static final String UNDEFINED_STRING = "undefined";
}

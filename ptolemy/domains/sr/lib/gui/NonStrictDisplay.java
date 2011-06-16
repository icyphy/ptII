/* Display the values of the tokens arriving on the input channels along
 with the associated time in a text area on the screen.

 @Copyright (c) 1998-2009 The Regents of the University of California.
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

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import ptolemy.actor.lib.gui.Display;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 Display the status and value of input tokens in a text area.
 Display the values of the tokens arriving on the input channels along
 with the associated time in a text area on the screen. If the value is
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

    /** Read at most one token from each input channel and display its
     *  string value along with the current time on the screen.  Each
     *  value is terminated with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // We don't invoke super.postfire() here, but we should
        // do what Display.super.postfire() does, which is eventually
        // call AtomicActor.postfire(), which prints debugging.
        if (_debugging) {
            _debug("Called postfire()");
        }

        JTextArea textArea = (JTextArea) _getImplementation().getTextArea();

        int width = input.getWidth();

        boolean currentInputIsBlankLine = true;

        for (int i = 0; i < width; i++) {
            String value;

            if (!initialized) {
                initialized = true;
                _openWindow();
            }

            if (input.isKnown(i)) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);

                    // The toString() method yields a string that can be parsed back
                    // in the expression language to get the original token.
                    // However, if the token is a StringToken, that probably is
                    // not what we want. So we treat StringToken separately.
                    value = token.toString();
                    if (token instanceof StringToken) {
                        value = ((StringToken) token).stringValue();
                    }
                } else {
                    value = ABSENT_STRING;
                }
            } else {
                value = UNDEFINED_STRING;
            }

            // If the window has been deleted, read the
            // rest of the inputs.

            if (textArea == null) {
                continue;
            }

            // FIXME: There is a race condition here.
            // textArea can be set to null during execution of this method
            // if another thread closes the display window.

            // If the value is not an empty string, set the
            // currentInputIsBlankLine to false.
            // Note that if there are multiple input ports, and if any of
            // the input ports has data, the current input is considered
            // to be non-empty.
            if (value.length() > 0) {
                currentInputIsBlankLine = false;
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

        // If the current input is not a blank line, or the supressBlankLines
        // parameter is configured to false, append a newline character.
        if ((textArea != null)
                && !(isSuppressBlankLines && currentInputIsBlankLine)) {
            textArea.append("\n");
        }

        // We don't invoke super.postfire() here, but we should
        // do what Display.super.postfire() does, which is eventually
        // call AtomicActor.postfire(), which returns the value of !_stopRequested.
        return !_stopRequested;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static final String ABSENT_STRING = "absent";

    private static final String UNDEFINED_STRING = "undefined";
}

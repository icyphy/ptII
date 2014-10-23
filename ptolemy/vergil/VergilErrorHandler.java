/* Handle a MoML Parsing Error.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.vergil;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ptolemy.gui.UndeferredGraphicalMessageHandler;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.ErrorHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// VergilErrorHandler

/**
 This error handler attempts to replace any failed MoML elements with
 generic versions so that the parsing of the MoML can continue. The
 generic versions, where appropriate, have icons that indicate failure.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class VergilErrorHandler implements ErrorHandler {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enable or disable skipping of errors.
     *  If this method is called with a true argument, then
     *  do not report subsequent errors when handleError() is called,
     *  and instead return CONTINUE. If it is called with a false
     *  argument, then report all subsequent errors.
     *  <p>
     *  This method is intended to be used when an operation may trigger
     *  a large number of errors, and the user interface wishes to offer
     *  the user the option of ignoring them.  This method should be
     *  called with a true argument before the operation begins, and
     *  then called with a false argument after the operation ends.
     *  @param enable True to enable skipping, false to disable.
     */
    @Override
    public void enableErrorSkipping(boolean enable) {
        _skippingEnabled = enable;

        if (!enable) {
            _skipping = false;
        }
    }

    /** Handle an error.
     *  @param element The XML element that triggered the error.
     *  @param context The container object for the element.
     *  @param exception The exception that was thrown.
     *  @return CONTINUE to skip this element, CANCEL to abort processing
     *   of the XML, IGNORE to continue to process the XML as if nothing
     *   had happened, or RETHROW to request that the exception be rethrown.
     */
    @Override
    public int handleError(String element, NamedObj context, Throwable exception) {
        if (_skipping) {
            return CONTINUE;
        }

        // Get the context w.r.t. which the dialog should be iconified.
        // FIXME: This pattern window actually is never set.
        Component parentWindow = UndeferredGraphicalMessageHandler.getContext();

        // If the element is longer than 80 characters, we split it
        // into shorter new line separated strings.
        String message = "Error encountered in:\n"
                + StringUtilities.split(element) + "\n"
                + exception.getMessage();

        Object[] messageArray = new Object[1];
        messageArray[0] = StringUtilities.ellipsis(message,
                StringUtilities.ELLIPSIS_LENGTH_LONG);

        if (context == null) {
            // Top-level object, so continuing is not an option.
            messageArray[0] = messageArray[0]
                    + "\nThis is a top-level element, so cannot continue.";

            Object[] options = { "Display stack trace", "Cancel" };

            // Show the MODAL dialog
            int selected = JOptionPane.showOptionDialog(parentWindow,
                    messageArray, "Error", JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, options, options[0]);

            if (selected == 0) {
                return _showStackTrace(parentWindow, false, false, exception,
                        message);
            }

            return CANCEL;
        } else {
            if (_skippingEnabled) {
                Object[] options = { "Skip element", "Skip remaining errors",
                        "Display stack trace", "Cancel" };

                // Show a MODAL dialog
                int selected = JOptionPane.showOptionDialog(parentWindow,
                        messageArray, "Error", JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, options, options[0]);

                if (selected == 3) {
                    return CANCEL;
                } else if (selected == 2) {
                    return _showStackTrace(parentWindow, true,
                            _skippingEnabled, exception, message);
                } else if (selected == 1) {
                    _skipping = true;
                }

                return CONTINUE;
            } else {
                // Skipping is not enabled.
                Object[] options = { "Skip element", "Display stack trace",
                "Cancel" };

                // Show the MODAL dialog
                int selected = JOptionPane.showOptionDialog(parentWindow,
                        messageArray, "Error", JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, options, options[0]);

                if (selected == 1) {
                    return _showStackTrace(parentWindow, false,
                            _skippingEnabled, exception, message);
                } else if (selected == 2) {
                    return CANCEL;
                }

                return CONTINUE;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Display a stack trace dialog. The "info" argument is a
     *  string printed at the top of the dialog instead of the Exception
     *  message.
     *  @param context The context.
     *  @param skipElement True if one of the buttons should be
     *  'Skip element'.    If skippingEnabled is true,
     *  then the value of skipElement is ignored.
     *  @param skippingEnabled True if one of the buttons should be
     *  'Skip remaining messages'.
     *  @param exception The exception.
     *  @param info A message.
     *  @return CONTINUE to skip this element, CANCEL to abort processing
     *   of the XML.
     */
    private int _showStackTrace(Component context, boolean skipElement,
            boolean skippingEnabled, Throwable exception, String info) {
        // FIXME: Eventually, the dialog should
        // be able to email us a bug report.
        // FIXME: The user should be able to click on the links and
        // jump to the line in the offending text.
        // Show the stack trace in a scrollable text area.
        JTextArea text = new JTextArea(
                KernelException.stackTraceToString(exception), 60, 80);
        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        text.setCaretPosition(0);
        text.setEditable(false);

        // We want to stack the text area with another message
        Object[] message = new Object[2];
        String string;

        if (info != null) {
            string = info + "\n" + exception.getMessage();
        } else {
            string = exception.getMessage();
        }

        message[0] = StringUtilities.ellipsis(string,
                StringUtilities.ELLIPSIS_LENGTH_SHORT);

        message[1] = scrollPane;

        Object[] options = null;

        if (skippingEnabled) {
            options = new Object[] { "Skip element", "Skip remaining errors",
            "Cancel" };
        } else {
            if (skipElement) {
                options = new Object[] { "Skip element", "Cancel" };
            } else {
                options = new Object[] { "Cancel" };
            }
        }

        // Show the MODAL dialog
        int selected = JOptionPane.showOptionDialog(context, message,
                "Stack trace", JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[0]);

        if (selected == options.length - 1) {
            // The last button is the Cancel button.
            return CANCEL;
        }

        if (skippingEnabled && selected == 1) {
            _skipping = true;
        }

        return CONTINUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Enable skipping.
    private boolean _skippingEnabled = false;

    // Activate skipping.
    private boolean _skipping = false;
}

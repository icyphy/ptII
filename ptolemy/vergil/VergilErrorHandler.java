/* Handle a MoML Parsing Error.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.vergil;

import ptolemy.gui.CancelException;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.ErrorHandler;

import java.util.Map;
import java.awt.Component;
import javax.swing.JOptionPane;

//////////////////////////////////////////////////////////////////////////
//// VergilErrorHandler
/**
This error handler attempts to replace any failed MoML elements with
generic versions so that the parsing of the MoML can continue. The
generic versions, where appropriate, have icons that indicate failure.

@author Edward A. Lee
@version $Id$
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
    public int handleError(
            String element,
            NamedObj context,
            Exception exception) {

       if (_skipping) {
           return CONTINUE;
       }
       // Get the context w.r.t. which the dialog should be iconified.
       // FIXME: This pattern window actually is never set.
       Component parentWindow = GraphicalMessageHandler.getContext();

       String message = "Error encountered in:\n"
               + element
               + "\n"
               + exception.getMessage();

       Object[] messageArray = new Object[1];
       messageArray[0] = message;

       if (context == null) {
           // Top-level object, so continuing is not an option.
           messageArray[0] = message
                   + "\nThis is a top-level element, so cannot continue.";
           Object[] options = {"Display stack trace",
			       "Cancel"};

           // Show the MODAL dialog
           int selected = JOptionPane.showOptionDialog(
                    parentWindow,
                    messageArray,
                    "Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);

	   if (selected == 0) {
	       GraphicalMessageHandler.showStackTrace(parentWindow,
						      exception,
						      message);
	   }
           return CANCEL;
       } else {
           if (_skippingEnabled) {
               Object[] options = {
                       "Skip element",
                       "Skip remaining errors",
		       "Display stack trace and skip element",
                       "Cancel"};

               // Show a MODAL dialog
               int selected = JOptionPane.showOptionDialog(
                        parentWindow,
                        messageArray,
                        "Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[0]);

	       if (selected == 3) {
                   return CANCEL;
               } else if (selected == 2) {
		   GraphicalMessageHandler.showStackTrace(parentWindow,
							  exception,
							  message);
               } else if (selected == 1) {
                   _skipping = true;
               }
               return CONTINUE;
           } else {
               // Skipping is not enabled.
               Object[] options = {"Skip element",
				   "Display stack trace and skip element",
				   "Cancel"};

               // Show the MODAL dialog
               int selected = JOptionPane.showOptionDialog(
                        parentWindow,
                        messageArray,
                        "Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[0]);

               if (selected == 2) {
		   GraphicalMessageHandler.showStackTrace(parentWindow,
							  exception,
							  message);
	       } else if (selected == 1) {
                   return CANCEL;
               }
               return CONTINUE;
           }
       }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Enable skipping.
    private boolean _skippingEnabled = false;

    // Activate skipping.
    private boolean _skipping = false;
}

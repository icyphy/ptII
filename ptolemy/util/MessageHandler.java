/* Base class for displaying exceptions, warnings, and messages.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//////////////////////////////////////////////////////////////////////////
//// MessageHandler
/**
This is a class that is used to report errors.  It provides a
set of static methods that are called to report errors.  However, the
actual reporting of the errors is deferred to an instance of this class
that is set using the setMessageHandler() method.  Normally there
is only one instance, set up by the application, so the class is
a singleton.  But this is not enforced.
<p>
This base class simply writes the errors to System.err.
When an applet or application starts up, it may wish to set a subclass
of this class as the message handler, to allow a nicer way of
reporting errors.  For example, a swing application will probably
want to report errors in a dialog box, using for example
the derived class GraphicalMessageHandler.
@see GraphicalMessageHandler

@author  Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
public class MessageHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Defer to the set message handler to show the specified
     *  error message.
     *  @param info The message.
     */
    public static void error(String info) {
        _handler._error(info);
    }

    /** Defer to the set message handler to
     *  show the specified message and throwable information.
     *  If the throwable is an instance of CancelException, then it
     *  is not shown.  By default, only the message of the throwable
     *  is thrown.  The stack trace information is only shown if the
     *  user clicks on the "Display Stack Trace" button.
     *
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see CancelException
     */
    public static void error(String info, Throwable throwable) {
        // Sometimes you find that errors are reported multiple times.
        // To find out who is calling this method, uncomment the following.
        // System.out.println("------ reporting error:");
        // (new Exception()).printStackTrace();
        _handler._error(info, throwable);
    }

    /** Return a short description of the throwable.
     *  @param throwable The throwable
     *  @return If the throwable is an Exception, return "Exception",
     *  if it is an Error, return "Error", if it is a Throwable, return
     *  "Throwable".
     */
    public static String shortDescription(Throwable throwable) {
        String throwableType = null;
        if (throwable instanceof Exception) {
            throwableType = "Exception";
        } else if (throwable instanceof Error) {
            throwableType = "Error";
        } else {
            throwableType = "Throwable";
        }
        return throwableType;
    }

    /** Return the message handler instance that is used by the static
     *  methods in this class.
     */
    public static MessageHandler getMessageHandler() {
        return _handler;
    }

    /** Defer to the set message handler to show the specified
     *  message.
     *  @param info The message.
     */
    public static void message(String info) {
        _handler._message(info);
    }

    /** Set the message handler instance that is used by the static
     *  methods in this class.  If the given handler is null, then
     *  do nothing.
     *  @param handler The message handler.
     */
    public static void setMessageHandler(MessageHandler handler) {
        if (handler != null) {
            _handler = handler;
        }
    }

    /** Defer to the set message handler to
     *  show the specified message in a modal dialog.  If the user
     *  clicks on the "Cancel" button, then throw an exception.
     *  This gives the user the option of not continuing the
     *  execution, something that is particularly useful if continuing
     *  execution will result in repeated warnings.
     *  @param info The message.
     *  @exception CancelException If the user clicks on the "Cancel" button.
     */
    public static void warning(String info) throws CancelException {
        _handler._warning(info);
    }

    /** Show the specified message and throwable information
     *  in a modal dialog.  If the user
     *  clicks on the "Cancel" button, then throw an exception.
     *  This gives the user the option of not continuing the
     *  execution, something that is particularly useful if continuing
     *  execution will result in repeated warnings.
     *  By default, only the message of the throwable
     *  is shown.  The stack trace information is only shown if the
     *  user clicks on the "Display Stack Trace" button.
     *  @param info The message.
     *  @exception CancelException If the user clicks on the "Cancel" button.
     */
    public static void warning(String info, Throwable throwable)
            throws CancelException {
        _handler._warning(info + ": " + throwable.getMessage(), throwable);
    }

    /** Ask the user a yes/no question, and return true if the answer
     *  is yes.
     *  @return True if the answer is yes.
     */
    public static boolean yesNoQuestion(String question) {
        return _handler._yesNoQuestion(question);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Show the specified error message.
     *  @param info The message.
     */
    protected void _error(String info) {
        System.err.println(info);
    }

    /** Show the specified message and throwable information.
     *  If the throwable is an instance of CancelException, then it
     *  is not shown.  By default, only the message of the exception
     *  is thrown.  The stack trace information is only shown if the
     *  user clicks on the "Display Stack Trace" button.
     *
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see CancelException
     */
    protected void _error(String info, Throwable throwable) {
        if (throwable instanceof CancelException) {
            return;
        }
        System.err.println(info);
        throwable.printStackTrace();
    }

    /** Show the specified message.
     *  @param info The message.
     */
    protected void _message(String info) {
        System.err.println(info);
    }

    /** Show the specified message in a modal dialog.  If the user
     *  clicks on the "Cancel" button, then throw an exception.
     *  This gives the user the option of not continuing the
     *  execution, something that is particularly useful if continuing
     *  execution will result in repeated warnings.
     *  @param info The message.
     *  @exception CancelException If the user clicks on the "Cancel" button.
     */
    protected void _warning(String info) throws CancelException {
        _error(info);
    }

    /** Show the specified message and throwable information
     *  in a modal dialog.  If the user
     *  clicks on the "Cancel" button, then throw an exception.
     *  This gives the user the option of not continuing the
     *  execution, something that is particularly useful if continuing
     *  execution will result in repeated warnings.
     *  By default, only the message of the throwable
     *  is thrown.  The stack trace information is only shown if the
     *  user clicks on the "Display Stack Trace" button.
     *  @param info The message.
     *  @exception CancelException If the user clicks on the "Cancel" button.
     */
    protected void _warning(String info, Throwable throwable)
            throws CancelException {
        _error(info, throwable);
    }

    /** Ask the user a yes/no question, and return true if the answer
     *  is yes.  In this base class, this prints the question on standard
     *  output and looks for the reply on standard input.
     *  @return True if the answer is yes.
     */
    protected boolean _yesNoQuestion(String question) {
        System.out.print(question);
        System.out.print(" (yes or no) ");
        BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));
        try {
            String reply = stdIn.readLine();
            if (reply.trim().toLowerCase().equals("yes")) {
                return true;
            }
        } catch (IOException ex) {}
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The message handler.
    private static MessageHandler _handler = new MessageHandler();
}

/* A simple message handler that throws exceptions.

 Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.util;


///////////////////////////////////////////////////////////////////
//// SimpleMessageHandler

/**
 This is a message handler that reports errors in a graphical dialog box.

 @see ptolemy.gui.GraphicalMessageHandler

 @author  Christopher Brooks
 @version $Id: MessageHandler.java 57040 2010-01-27 20:52:32Z cxh $
 @since Ptolemy II 9.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SimpleMessageHandler extends MessageHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw a RunetimException.
     *  @param info The message.
     */
    protected void _error(String info) {
        throw new RuntimeException(info);
    }

    /** Show the specified message and throwable information.
     *  If the throwable is an instance of CancelException, then nothing
     *  is shown.
     *
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see CancelException
     */
    protected void _error(String info, Throwable throwable) {
        if (throwable instanceof CancelException) {
            return;
        }
        //throwable.printStackTrace();
        throw new RuntimeException(info, throwable);
    }

    /** Display the warning message.  In this base class, the
     *  the default handler merely prints the warning to stderr.
     *  @param info The message.
     */
    protected void _message(String info) {
        System.err.println(info);
    }

    /** Show the specified message.  In this base class, the message
     *  is printed to standard error.
     *  <p>Derived classes might show the specified message in a modal
     *  dialog.  If the user clicks on the "Cancel" button, then throw
     *  an exception.  This gives the user the option of not
     *  continuing the execution, something that is particularly
     *  useful if continuing execution will result in repeated
     *  warnings.
     *  @param info The message.
     *  @exception CancelException If the user clicks on the "Cancel" button.
     */
    protected void _warning(String info) throws CancelException {
        _error(info);
    }

    /** Display the warning message and throwable information.  In
     *  this base class, the the default handler merely prints the
     *  warning to stderr.
     *  @param info The message.
     *  @param throwable The Throwable.
     *  @exception CancelException If the user clicks on the "Cancel" button.
     */
    protected void _warning(String info, Throwable throwable)
            throws CancelException {
        _error(info, throwable);
    }

    /** Ask the user a yes/no question, and return true if the answer
     *  is yes.  In this base class, this prints the question on standard
     *  output and looks for the reply on standard input.
     *  @param question The yes/no question to be asked.
     *  @return True if the answer is yes.
     */
    protected boolean _yesNoQuestion(String question) {
        System.out.print(question);
        System.out.print(" (yes or no) ");
        return false;
    }

    /** Ask the user a yes/no/cancel question, and return true if the
     *  answer is yes.  If the user chooses "cancel", then throw an
     *  exception.  In this base class, this prints the question on
     *  the standard output and looks for the reply on the standard
     *  input.
     *  @param question The yes/no/cancel question to be asked.
     *  @return True if the answer is yes.
     *  @exception ptolemy.util.CancelException If the user chooses
     *  "cancel".
     */
    protected boolean _yesNoCancelQuestion(String question)
            throws ptolemy.util.CancelException {
        System.out.print(question);
        System.out.print(" (yes or no or cancel) ");
        return false;
    }
}

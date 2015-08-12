/* A simple message handler that throws exceptions.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SimpleMessageHandler extends MessageHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw a RunetimException.
     *  @param info The message.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    protected boolean _yesNoQuestion(String question) {
        System.out.print(question);
        System.out.print(" (yes or no) ");
        return false;
    }

    /** Ask the user a question with three possible answers;
     *  return true if the answer is the first one and false if
     *  the answer is the second one; throw an exception if the
     *  user selects the third one.
     *  @param question The question.
     *  @param trueOption The option for which to return true.
     *  @param falseOption The option for which to return false.
     *  @param exceptionOption The option for which to throw an exception.
     *  @return Always return false.
     *  @exception ptolemy.util.CancelException If the user selects the third option.
     */
    @Override
    protected boolean _yesNoCancelQuestion(String question, String trueOption,
            String falseOption, String exceptionOption)
                    throws ptolemy.util.CancelException {
        System.out.print(question + " (" + trueOption + " or " + falseOption
                + " or " + exceptionOption + ") ");
        return false;
    }
}

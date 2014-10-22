/* An ExecutionListener that copies events to a given stream.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor;

import java.io.OutputStream;
import java.io.PrintStream;

///////////////////////////////////////////////////////////////////
//// StreamExecutionListener

/**
 A default implementation of the ExecutionListener interface.
 This implementation prints information about each event to a stream.

 @author Steve Neuendorffer, Lukito Muliadi, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bart)
 */
public class StreamExecutionListener implements ExecutionListener {
    /** Create a listener that sends messages to the standard output.
     */
    public StreamExecutionListener() {
        _output = System.out;
    }

    /** Create a listener that sends messages to the given output stream.
     *  @param out The output stream to send the messages to.
     */
    public StreamExecutionListener(OutputStream out) {
        _output = new PrintStream(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Report an execution failure by printing a message to output
     *  stream specified to the constructor.
     */
    @Override
    public void executionError(Manager manager, Throwable throwable) {
        _output.println("Execution error.");
        throwable.printStackTrace(_output);
    }

    /** Report that the current execution finished  by printing a
     *  message to output stream specified to the constructor.
     *  @param manager The manager controlling the execution.
     */
    @Override
    public void executionFinished(Manager manager) {
        _output.println("Completed execution with "
                + manager.getIterationCount() + " iterations");
    }

    /** Report that the manager has changed state by printing a
     *  message to output stream specified to the constructor.
     *  @param manager The manager controlling the execution.
     */
    @Override
    public void managerStateChanged(Manager manager) {
        Manager.State state = manager.getState();
        String message;

        if (state == Manager.ITERATING) {
            message = state.getDescription() + " number "
                    + manager.getIterationCount();
        } else {
            message = state.getDescription();
        }

        _output.println(message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private PrintStream _output;
}

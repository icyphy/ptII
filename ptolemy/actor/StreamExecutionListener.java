/* An ExecutionListener that copies events to a given stream.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor;

import java.util.Enumeration;
import java.io.OutputStream;
import java.io.PrintStream;

//////////////////////////////////////////////////////////////////////////
//// StreamExecutionListener
/**
A default implementation of the ExecutionListener interface.
This implementation prints information about each event to a stream.

@author Steve Neuendorffer, Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class StreamExecutionListener implements ExecutionListener {

    /** Create an execution listener that sends
     *  messages to the standard output.
     */
    public StreamExecutionListener() {
        _output = System.out;
    }

    /** Create an execution listener that sends
     *  messages to the given output stream.
     */
    public StreamExecutionListener(OutputStream out) {
        _output = new PrintStream(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Report an execution failure.
     */
    public void executionError(Manager manager, Exception ex) {
        _output.println("Execution error.");
	_output.println(ex.getMessage());
        ex.printStackTrace(_output);
    }

    /** Report that the current execution finished.
     *
     *  @param manager The manager controlling the execution.
     */
    public void executionFinished(Manager manager) {
        _output.println("Completed execution with "
                + manager.getIterationCount() + " iterations");
    }

    /** Called to report that the manager has changed state.
     *
     *  @param manager The manager controlling the execution.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State state = manager.getState();
        String msg;
        if (state == manager.ITERATING) {
            msg = state.getDescription() + " number "
                + manager.getIterationCount();
        } else {
            msg = state.getDescription();
        }
        _output.println(msg);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private PrintStream _output;
}

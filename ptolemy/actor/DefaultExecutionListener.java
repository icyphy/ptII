/* A default implementation of the ExecutionListener interface

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor;

import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// DefaultExecutionListener
/**
A default implementation of the ExecutionListener interface.
This implementation prints information about each event to the standard
output.

@author Steve Neuendorffer, Lukito Muliadi
@version $Id$
*/
public class DefaultExecutionListener implements ExecutionListener {

    /** Constructor.
     */
    public DefaultExecutionListener() {
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Called to report an execution failure.
     */
    public void executionError(ExecutionEvent event) {
        System.out.println("DefaultExecutionListener.executionError()");
        Exception e = event.getException();
        if (e instanceof TypeConflictException) {
            TypeConflictException tce = (TypeConflictException) e;
            Enumeration ports = tce.getPorts();
            System.out.println("Type conflict exception with the offending "+
                    "ports:");
            while (ports.hasMoreElements() ) {
                IOPort port = (IOPort) ports.nextElement();
                System.out.println(port.getFullName());
            }
        }
        e.printStackTrace();
    }

    /** Called to report that the current iteration finished and
     *  the wrapup sequence completed normally.
     */
    public void executionFinished(ExecutionEvent event) {
        System.out.println("DefaultExecutionListener.executionFinished()");
    }

    /** Called to report that a toplevel iteration has begun.
     */
    public void executionIterationStarted(ExecutionEvent event) {
        // This is printed every iteration.. way too much for now..
    }

    /** Called to report a successful pause of execution.
     */
    public void executionPaused(ExecutionEvent event) {
        System.out.println("DefaultExecutionListener.executionPaused()");
    }

    /** Called to report a successfull resumption of execution.
     */
    public void executionResumed(ExecutionEvent event) {
        System.out.println("DefaultExecutionListener.executionResumed()");
    }

    /** Called to report a successful start of execution.
     */
    public void executionStarted(ExecutionEvent event) {
        System.out.println("DefaultExecutionListener.executionStarted()");
    }

    /** Called to report a successful termination of execution.
     */
    public void executionTerminated(ExecutionEvent event) {
        System.out.println("DefaultExecutionListener.executionTerminated()");
    }
}

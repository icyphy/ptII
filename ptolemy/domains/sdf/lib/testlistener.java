/*
@Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// testlistener
/**
@author Stephen Neuendorffer
@version $Id$
*/
public class testlistener implements ExecutionListener {

    /** Called to report an execution failure
     */
    public void executionError(ExecutionEvent event) {
        Debug.println("testlistener: executionError");
        Exception e = event.getException();
        e.printStackTrace();
    }

    /** Called to report a successful pause of execution
     */
    public void executionPaused(ExecutionEvent event) {
        Debug.println("testlistener: executionPaused");
    }
    
    /** Called to report a successfull resumption of execution
     */
    public void executionResumed(ExecutionEvent event) {
        Debug.println("testlistener: executionResumed");
    }

    /** Called to report a successful start of execution
     */
    public void executionStarted(ExecutionEvent event) {
        Debug.println("testlistener: executionStarted");
    }

    /** Called to report a successful termination of execution.
     */    
    public void executionTerminated(ExecutionEvent event) {
        Debug.println("testlistener: executionTerminated");
    } 
    
    /** Called to report that the current iteration finished and 
     *  the wrapup sequence completed normally.
     */
    public void executionFinished(ExecutionEvent event) {
        Debug.println("testlistener: executionWrappedup");
    }

    /** Called to report that a toplevel iteration has begun
     */
    public void executionIterationStarted(ExecutionEvent event) {
        Debug.println("testlistener: executionIterationStarted");
    }

}

/* A change listener that listens for change requests that result in 
exceptions

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package util.testsuite;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.StreamChangeListener;

import java.io.OutputStream;
import java.io.PrintStream;


//////////////////////////////////////////////////////////////////////////
//// FailedChangeListener
/**
A simple change listener that reacts to change requests that result
in an exception by throwning a RuntimeException when changeFailed()
is called

<p>This change listener differs from StreamChangeListener in that
this change listener will throw an exception when changeFailed() is called.
and that changeExecuted() does nothing.

<p>The primary purpose of this class is to be used in the auto tests
by ptII/util/testsuite/auto.tcl

@author Christopher Hylands
@version $Id$
*/
public class FailedChangeListener extends StreamChangeListener {

    /** Create a change listener that sends messages to the standard output.
     */
    public FailedChangeListener() {
	super();
    }

    /** Create a change listener that sends messages to the specified stream.
     *  @param out The stream to send messages to.
     */
    public FailedChangeListener(OutputStream out) {
	super(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ignore successful change requests.  The changeExecuted()
     *  method in the base class is <b>not</b> called,
     *  we are only interested in changeErrors();
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
    }

    /** Print the description of the failure and the stack trace
     *	to the stream output, then throw a RuntimeException
     *  
     *  @param change The change that has been executed.
     *  @param exception The exception that occurred.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
	super.changeFailed(change, exception);
	exception.printStackTrace(_output);
        throw new RuntimeException(exception);
    }
}

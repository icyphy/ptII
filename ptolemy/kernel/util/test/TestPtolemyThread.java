/* Class used to test PtolemyThread.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.kernel.util.test;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.PtolemyThread;

//////////////////////////////////////////////////////////////////////////
//// TestPtolemyThread

/** This class is used to test the protected _debug() method in
 kernel.util.PtolemyThread.
 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.3
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxheecs.berkeley.edu)
 */
public class TestPtolemyThread extends PtolemyThread {
    /** Construct a new TestPtolemyThread object. This constructor has
     *  the same effect as TestPtolemyThread(null, null,
     *  <i>generatedName</i>), where <i>generatedName</i> is a newly
     *  generated name. Automatically generated names are of the form
     *  "Thread-"+n, where n is an integer.
     */
    public TestPtolemyThread() {
        super();
    }

    /** Construct a new TestPtolemyThread object. This constructor has the same
     *  effect as TestPtolemyThread(null, target, <i>generatedName</i>), where
     *  <i>generatedName</i> is a newly generated name. Automatically generated
     *  names are of the form "Thread-"+n, where n is an integer.
     *  @param target The object whose run method is called.
     */
    public TestPtolemyThread(Runnable target) {
        super(target);
    }

    /** Construct a new TestPtolemyThread object. This constructor has the
     *  same effect as TestPtolemyThread(null, target, name)
     *  @param target The object whose run method is called.
     *  @param name The name of the new thread.
     *
     */
    public TestPtolemyThread(Runnable target, String name) {
        super(target, name);
    }

    /** Construct a new TestPtolemyThread object. This constructor has the
     *  same effect as TestPtolemyThread(null, null, name)
     *  @param name The name of the new thread.
     */
    public TestPtolemyThread(String name) {
        super(name);
    }

    /** Construct a new TestPtolemyThread object. This constructor has the
     *  same effect as TestPtolemyThread(group, target, generatedName),
     *  where generatedName is a newly generated name. Automatically
     *  generated names are of the form "Thread-"+n, where n is an
     *  integer.
     *  @param group The thread group
     *  @param target The object whose run method is called.
     */
    public TestPtolemyThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    /** Construct a new TestPtolemyThread object so that it has target as
     *  its run object, has the specified name as its name, and belongs
     *  to the thread group referred to by group.
     *  @param group The thread group.
     *  @param target The object whose run method is called.
     *  @param name The name of the new thread.
     *  @exception SecurityException If the superclass constructor throws it.
     *
     */
    public TestPtolemyThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    /** Construct a new TestPtolemyThread object. This constructor has the same
     *  effect as TestPtolemyThread(group, null, name).
     *  @param group The thread group.
     *  @param name The name of the new thread.
     */
    public TestPtolemyThread(ThreadGroup group, String name) {
        super(group, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a debug event to all debug listeners that have registered.
     *  TestPtolemyThread exists solely because in the parent class
     *  _debug() is protected
     *  @param event The event.
     */
    public void debug(DebugEvent event) {
        _debug(event);
    }

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  Note that using this method could be fairly expensive if the
     *  message is constructed from parts, and that this expense will
     *  be incurred regardless of whether there are actually any debug
     *  listeners.  Thus, you should avoid, if possible, constructing
     *  the message from parts.
     *  @param message The message.
     *  @since Ptolemy II 2.3
     */
    public void debug(String message) {
        _debug(message);
    }
}

/* A subclass of Thread to be used for optimizing reader-writer mechanism in
   Ptolemy II.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Green (liuj@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
////
/** PtolemyThread
PtolemyThread extends Thread by adding rudimentary debugging capability.

@author Lukito Muliadi, contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/
public class PtolemyThread extends Thread implements Debuggable {

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(null, null, <i>generatedName</i>), where
     *  <i>generatedName</i> is a newly generated name. Automatically generated
     *  names are of the form "Thread-"+n, where n is an integer.
     */
    public PtolemyThread() {
        super();
    }

    /** Construct a new PtolemyThread object. This constructor has the same
     *  effect as PtolemyThread(null, target, <i>generatedName</i>), where
     *  <i>generatedName</i> is a newly generated name. Automatically generated
     *  names are of the form "Thread-"+n, where n is an integer.
     *  @param target The object whose run method is called.
     */
    public PtolemyThread(Runnable target) {
        super(target);
    }

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(null, target, name)
     *  @param target The object whose run method is called.
     *  @param name The name of the new thread.
     *
     */
    public PtolemyThread(Runnable target, String name) {
        super(target, name);
    }

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(null, null, name)
     *  @param name The name of the new thread.
     */
    public PtolemyThread(String name) {
        super(name);
    }

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(group, target, generatedName),
     *  where generatedName is a newly generated name. Automatically
     *  generated names are of the form "Thread-"+n, where n is an
     *  integer.
     *  @param group The thread group
     *  @param target The object whose run method is called.
     */
    public PtolemyThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    /** Construct a new PtolemyThread object so that it has target as
     *  its run object, has the specified name as its name, and belongs
     *  to the thread group referred to by group.
     *  @param group The thread group.
     *  @param target The object whose run method is called.
     *  @param name The name of the new thread.
     *  @exception SecurityException If the superclass constructor throws it.
     *
     */
    public PtolemyThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    /** Construct a new PtolemyThread object. This constructor has the same
     *  effect as PtolemyThread(group, null, name).
     *  @param group The thread group.
     *  @param name The name of the new thread.
     */
    public PtolemyThread(ThreadGroup group, String name) {
        super(group, name);
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     *  @see #removeDebugListeners(DebugListener)
     *  @since Ptolemy II 2.3
     */
    public synchronized void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        } else {
            if (_debugListeners.contains(listener)) {
                return;
            }
        }
        _debugListeners.add(listener);
        _debugging = true;
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListeners(DebugListener)
     *  @since Ptolemy II 2.3
     */
    public synchronized void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }
        _debugListeners.remove(listener);
        if (_debugListeners.size() == 0) {
            _debugging = false;
        }
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Send a debug event to all debug listeners that have registered.
     *  @param event The event.
     *  @since Ptolemy II 2.3
     */
    protected final void _debug(DebugEvent event) {
        if (_debugging) {
            // We copy this list to that responding to the event may block.
            // while the execution thread is blocked, we want to be able to
            // add more debug listeners...
            // Yes, this is slow, but hey, it's debug code.
            List list;
            synchronized(this) {
                list = new ArrayList(_debugListeners);
            }
            Iterator listeners = list.iterator();
            while (listeners.hasNext()) {
                ((DebugListener)listeners.next()).event(event);
            }
        }
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
    protected final void _debug(String message) {
        if (_debugging) {
            // We copy this list to that responding to the event may block.
            // while the execution thread is blocked, we want to be able to
            // add more debug listeners...
            // Yes, this is slow, but hey, it's debug code.
            List list;
            synchronized(this) {
                list = new ArrayList(_debugListeners);
            }
            Iterator listeners = list.iterator();
            while (listeners.hasNext()) {
                ((DebugListener)listeners.next()).message(message);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Flag that is true if there are debug listeners.
     *  @since Ptolemy II 2.3
     */
    protected boolean _debugging = false;

    /** The list of DebugListeners registered with this object.
     *  @since Ptolemy II 2.3
     */
    protected LinkedList _debugListeners = null;
}

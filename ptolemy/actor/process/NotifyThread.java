/* Helper thread for calling notifyAll on a LinkedList of locks.


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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow


*/

package ptolemy.actor.process;

import ptolemy.actor.*;

import java.util.Iterator;
import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// NotifyThread
/**
Helper thread for calling notifyAll on a single lock or a LinkedList
of locks. Since this is a new thread without any locks, calling notifyAll
 from this thread reduces the possibility of deadlocks.
<p>
To use this to wake up any threads waiting on a lock, create a new instance
of this class with a LinkedList of lock objects (or single lock) to call
notifyAll on.
<p>
@author Neil Smyth, Mudit Goel
@version $Id$

*/

public class NotifyThread extends Thread {

    /** Construct a thread to be used call notifyAll on a set of locks.
     *  @param locks The set of locks to call notifyAll on.
     */
    public NotifyThread(LinkedList locks) {
        _locks = new LinkedList();
        Iterator e = locks.iterator();
        while (e.hasNext() ) {
            _locks.addLast(e.next());
        }
    }

    /** Construct a thread to be used call notifyAll on a set of locks.
     *  @param locks The set of locks to call notifyAll on.
     */
    public NotifyThread(Object lock) {
	_lock = lock;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call NotifyAll() on the lock object (or objects) passed to this
     *  class in its constructor.
     */
    public void run() {
	if (_locks != null) {
	    Iterator objs = _locks.iterator();
	    while (objs.hasNext()) {
		Object nextObj = objs.next();
		synchronized(nextObj) {
		    nextObj.notifyAll();
		}
	    }
	} else {
	    synchronized(_lock) {
		_lock.notifyAll();
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The locks to call notifyAll on.
    private LinkedList _locks = null;

    // The lock to call notifyAll on.
    private Object _lock = null;
}

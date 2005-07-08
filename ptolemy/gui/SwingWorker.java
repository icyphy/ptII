/* Abstract class to perform GUI-related work in a dedicated thread.

 Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.gui;

import javax.swing.SwingUtilities;

/**
 This is the 3rd version of SwingWorker (also known as
 SwingWorker 3), an abstract class that you subclass to
 perform GUI-related work in a dedicated thread.  For
 instructions on using this class, see:

 <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html</a>

 Note that the API changed slightly in the 3rd version:
 You must now invoke start() on the SwingWorker after
 creating it.
 @author Sun Microsystems
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class SwingWorker {
    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                try {
                    setValue(construct());
                } finally {
                    _threadVar.clear();
                }

                SwingUtilities.invokeLater(doFinished);
            }
        };

        Thread thread = new Thread(doConstruct);
        _threadVar = new ThreadVar(thread);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Compute the value to be returned by the <code>get</code> method.
     * @return The object that is created by the construct() method
     * in a subclass.
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * Return the value created by the <code>construct</code> method.
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     *
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {
            Thread thread = _threadVar.get();

            if (thread == null) {
                return getValue();
            }

            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread thread = _threadVar.get();

        if (thread != null) {
            thread.interrupt();
        }

        _threadVar.clear();
    }

    /** Start the worker thread. */
    public void start() {
        Thread thread = _threadVar.get();

        if (thread != null) {
            thread.start();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the value produced by the worker thread, or null if it
     * hasn't been constructed yet.
     *  @return the value. 
     */
    protected synchronized Object getValue() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Set the value produced by worker thread
     */
    private synchronized void setValue(Object object) {
        _value = object;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        ThreadVar(Thread thread) {
            _thread = thread;
        }

        synchronized Thread get() {
            return _thread;
        }

        synchronized void clear() {
            _thread = null;
        }

        private Thread _thread;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ThreadVar _threadVar;

    private Object _value; // see getValue(), setValue()
}

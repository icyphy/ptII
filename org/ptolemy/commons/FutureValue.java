/* A Future implementation that just waits until its result value/exception is available.

Copyright (c) 2014-2016 The Regents of the University of California; iSencia Belgium NV.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package org.ptolemy.commons;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Future implementation that just waits until its result value/exception is available.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
@SuppressWarnings("serial")
public class FutureValue<V> implements Future<V>, Serializable {

    /**
     * Creates a future that will be waiting for its result.
     */
    public FutureValue() {
    }

    /**
     * Creates a future that is immediately set with its result value.
     * @param value the future's result
     */
    public FutureValue(V value) {
        set(value);
    }

    @Override
    public synchronized boolean isCancelled() {
        return _state == CANCELLED;
    }

    @Override
    public synchronized boolean isDone() {
        return _completedOrCancelled();
    }

    /**
     * @param mayInterruptIfRunning ignored in this simple value-based implementation
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (_completedOrCancelled()) {
                return false;
            }
            _state = CANCELLED;
            notifyAll();
        }
        _done();
        return true;
    }

    @Override
    public synchronized V get()
            throws InterruptedException, ExecutionException {
        _waitFor();
        return _getResult();
    }

    @Override
    public synchronized V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        _waitFor(unit.toNanos(timeout));
        return _getResult();
    }

    /**
     * Sets the result of this Future to the given value unless this future has already been set or has been cancelled.
     *
     * @param v
     *          the value
     */
    public void set(V v) {
        _setCompleted(v);
    }

    /**
     * Causes this future to report an <tt>ExecutionException</tt> with the given throwable as its cause,
     * unless this Future has already been set or has been cancelled.
     *
     * @param t
     *          the cause of failure
     */
    public void setException(Throwable t) {
        _setFailed(t);
    }

    @Override
    public String toString() {
        if (!isDone()) {
            return "waiting";
        } else {
            // FindBugs reports "toString method may return null".  So we return the empty string instead.
            return _result != null ? _result.toString() : "" /*null*/;
        }
    }

    // protected things

    /**
    * Protected method invoked when this task transitions to state <tt>isDone</tt> (whether normally or via cancellation).
    *
    * The default implementation does nothing. Subclasses may override this method to invoke completion callbacks or perform bookkeeping.
    */
    protected void _done() {
    }

    // private things

    /** State value representing that future is still waiting for the result */
    private static final int WAITING = 1;
    /** State value representing that future has obtained its result */
    private static final int COMPLETED = 2;
    /** State value representing that future was cancelled */
    private static final int CANCELLED = 4;

    /** The result to return from get() */
    private V _result;
    /** The exception to throw from get() */
    private Throwable _exception;
    /** The current state of the future */
    private int _state = WAITING;

    /**
     * Assumes that a lock is owned on this future instance!
     * @return true if the state is COMPLETED or CANCELLED
     */
    private boolean _completedOrCancelled() {
        return (_state & (COMPLETED | CANCELLED)) != 0;
    }

    /**
     * Marks the future's underlying task as completed with the given result value.
     *
     * @param result
     *          the result of a task.
     */
    private void _setCompleted(V result) {
        synchronized (this) {
            if (_completedOrCancelled()) {
                return;
            }
            this._state = COMPLETED;
            this._result = result;
            notifyAll();
        }

        // invoking callbacks *after* setting future as completed and
        // outside the synchronization block makes it safe to call
        // interrupt() from within callback code (in which case it will be
        // ignored rather than cause deadlock / illegal state exception)
        _done();
    }

    /**
     * Marks the future's underlying task as failed, with the given exception as failure cause.
     *
     * @param exception
     *          the cause of the task failure.
     */
    private void _setFailed(Throwable exception) {
        synchronized (this) {
            if (_completedOrCancelled()) {
                return;
            }
            this._state = COMPLETED;
            this._exception = exception;
            notifyAll();
        }

        // invoking callbacks *after* setting future as completed and
        // outside the synchronization block makes it safe to call
        // interrupt() from within callback code (in which case it will be
        // ignored rather than cause deadlock / illegal state exception)
        _done();
    }

    /**
     * Waits for the task to complete.
     * Assumes that a lock is owned on this future instance!
     */
    private void _waitFor() throws InterruptedException {
        while (!isDone()) {
            wait();
        }
    }

    /**
     * Waits for the task to complete for timeout nanoseconds or throw TimeoutException if still not completed after that
     * Assumes that a lock is owned on this future instance!
     */
    private void _waitFor(long nanos)
            throws InterruptedException, TimeoutException {
        if (nanos < 0) {
            throw new IllegalArgumentException();
        }
        if (isDone()) {
            return;
        }
        long deadline = System.nanoTime() + nanos;
        while (nanos > 0) {
            TimeUnit.NANOSECONDS.timedWait(this, nanos);
            if (isDone()) {
                return;
            }
            nanos = deadline - System.nanoTime();
        }
        throw new TimeoutException();
    }

    /**
     * Gets the result of the future's underlying task.
     *
     * Assumes that a lock is owned on this future instance!
     */
    private V _getResult() throws ExecutionException {
        if (_state == CANCELLED) {
            throw new CancellationException();
        }
        if (_exception != null) {
            throw new ExecutionException(_exception);
        }
        return _result;
    }
}

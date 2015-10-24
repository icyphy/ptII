/* Embedding of a Vert.x core.

   Copyright (c) 2014-2015 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

///////////////////////////////////////////////////////////////////
//// VertxHelperBase

/**
   A base class for helper classes that use an embedded Vert.x core.
   This creates only one static Vert.x core that is accessible to its subclasses.
   It also keeps track of helpers that have been created for a particular
   actor, and provides a static method to obtain those helpers. Generally
   we will want no more than one such helper per actor. The references
   to the helpers are weak so that if the actor is garbage collected, the
   reference can be too.
   <p>
   This class also provides utilities for submitting jobs to be executed
   in an associated verticle.  This is useful for jobs that trigger future
   callbacks, because the verticle will ensure that the callbacks are called
   in the same thread, in a Vert.x event loop, that job itself is executed
   in.
   <p>
   Moreover, this class provides utilities for ensuring that a requested job
   has been completed, including all expected callbacks, before the next job
   is initiated. This is useful for ensuring that responses to requests
   occur in the same order as the requests themselves.

   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class VertxHelperBase extends HelperBase {
	
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

	/** Return an instance of this helper for the specified actor, if one
	 *  has been created and not garbage collected. Return null otherwise.
	 *  @param actor Either a JavaScript actor or a RestrictedJavaScriptInterface.
	 */
	public static VertxHelperBase getHelper(Object actor) {
		if (actor instanceof RestrictedJavaScriptInterface) {
			actor = ((RestrictedJavaScriptInterface)actor)._getActor();
		}
		if (!(actor instanceof JavaScript)) {
			throw new IllegalArgumentException("getOrCreateHelper: Must be "
					+ "passed an instance of RestrictedJavaScriptInterface or "
					+ "JavaScript.");
		}
		WeakReference<VertxHelperBase> helperReference = _vertxHelpers.get(actor);
		if (helperReference != null) {
			VertxHelperBase helper = helperReference.get();
			if (helper != null) {
				return helper;
			}
		}
		return null;
	}
	
	/** Submit a job to be executed by the associated verticle.
	 *  @param job The job to execute.
	 */
	public void submit(Runnable job) {
		_pendingJobs.add(job);
		
		// Notify the verticle to process the next job.
		EventBus eventBus = _vertx.eventBus();
		eventBus.publish(_address, "submit");
	}
	
    ///////////////////////////////////////////////////////////////////
    ////                     protected constructor                 ////

    /** Construct a helper for the specified JavaScript actor.
     *  This is protected to help prevent applications from creating
     *  more than one instance per actor.
     *  @param actor The JavaScript actor that this is helping, or
     *   a RestrictedJavaScriptInterface proxy for that actor.
     */
    protected VertxHelperBase(Object actor) {
        super(actor);
        
        _vertxHelpers.put(_actor, new WeakReference<VertxHelperBase>(this));
        
    	_verticle = new AccessorVerticle();
    	_vertx.deployVerticle(_verticle, result -> {
    		_deploymentID = result.result();
    		
    		// Now that the verticle is deployed, process any pending jobs.
    		_processPendingJob();
    	});
    	
    	_address = _actor.getFullName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

	/** If the verticle is not busy, process the next pending job.
	 *  This method should be called only by the verticle.
	 *  @see #_setBusy(boolean)
	 */
    protected void _processPendingJob() {
    	boolean busy = false;
    	synchronized(this) {
    		busy = _busy;
    	}
    	if (!busy) {
    		Runnable job = _pendingJobs.poll();
    		if (job != null) {
    			job.run();
    			// Process another job, unless the job calls setBusy(true).
    			_processPendingJob();
    		}
    	}
    }

	/** Specify whether this helper is currently processing a previous
	 *  request. After this is called with argument true, any subsequent
	 *  calls to {@link #submit(Runnable)} will defer execution of the
	 *  job until this is later called with argument false. If you call
	 *  this with argument, please be sure you later call it with argument
	 *  false. The purpose of this method is to ensure that if a job
	 *  involves some callbacks, that those callbacks are processed
	 *  before the next job is executed. Normally, you would call this
	 *  with argument true when requesting a service, and call it with
	 *  argument false when the service has been completely provided.
	 *  If you do not call this at all, then jobs and callbacks may
	 *  be arbitrarily interleaved (though they will all execute in the
	 *  same thread).
	 *  @param busy True to defer jobs, false to stop deferring.
	 */
	protected synchronized void _setBusy(boolean busy) {
		_busy = busy;
		if (!busy) {
			// Notify the verticle to process the next job.
			EventBus eventBus = _vertx.eventBus();
			eventBus.publish(_address, "submit");
		}
	}

    ///////////////////////////////////////////////////////////////////
    ////                     protected fields                      ////

    /** Event bus address for notifications. */
    private String _address;
    
    /** Flag indicating whether job requests shoudl be deferred. */
    private boolean _busy;
    
    /** The deplotmentID, if deploying the verticle is successful. */
    private String _deploymentID;
    
    /** Queue of pending jobs. */
    private ConcurrentLinkedQueue<Runnable> _pendingJobs
    		= new ConcurrentLinkedQueue<Runnable>();

    /** Verticle supporting this helper. */
    protected AccessorVerticle _verticle;

    /** Global instance of Vert.x core. */
    protected static Vertx _vertx = Vertx.vertx();

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Index of Vertx helpers by actor. */
    private static WeakHashMap<JavaScript,WeakReference<VertxHelperBase>> _vertxHelpers
    		= new WeakHashMap<JavaScript,WeakReference<VertxHelperBase>>();
    
    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** Verticle to handle requests.
     */
    private class AccessorVerticle extends AbstractVerticle {
    	
    	/** Register a handler to the event bus to process pending jobs. */
    	@Override
    	public void start() {
    		EventBus eventBus = _vertx.eventBus();
    		eventBus.consumer(_address, message -> {
    			_processPendingJob();
    		});
    	}
    	
    	/** Clear all pending jobs. */
    	@Override
    	public void stop() {
    		_pendingJobs.clear();
    	}
    }
}

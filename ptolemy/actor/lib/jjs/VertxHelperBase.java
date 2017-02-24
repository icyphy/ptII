/* Embedding of a Vert.x core.

   Copyright (c) 2014-2016 The Regents of the University of California.
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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.util.StringUtilities;

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
   callbacks, because the verticle will ensure that all callbacks are called
   in the same thread.
   <p>
   Moreover, this class provides utilities for ensuring that a requested job
   has been completed, including all expected callbacks, before the next job
   is initiated. This is useful for ensuring that responses to requests
   occur in the same order as the requests themselves. To use this, when you
   make a request (e.g. using {@link #submit(Runnable)}, the subclass can
   call the protected method {@link #_setBusy(boolean)} with argument true,
   and subsequent requests will simply be queued until the subclass calls
   {@link #_setBusy(boolean)} with argument false.
   <p>
   This class also provides utilities for a subclass to issue asynchronous
   requests in order, and when callbacks occur, to execute those callbacks
   in the same order as the submitted requests. To use this, the subclass
   should assign consecutive increasing integers, starting with zero, to
   each request, and then wrap responses to that request in a Runnable
   and pass that Runnable to the protected method
   {@link #_issueOrDeferResponse(long, boolean, Runnable)}.
   That method will execute the Runnable only when all previous
   requests (ones with earlier sequence numbers) have had a response
   executed that indicated (with the second argument) that the request
   has been fully handled.
   This facility must be used with care: the subclass must ensure that
   every request results in at least one call to
   {@link #_issueOrDeferResponse(long, boolean, Runnable)}
   with the second argument being true; failing to do so will result in
   all future responses being queued and never executing.
   Using a timeout, for example, can ensure this.
   Note also that the specified Runnable will execute in the director
   thread (via a timeout mechanism with timeout equal to zero), not
   in the Verticle. Thus, subclasses that use should not directly invoke
   Vert.x functions that need to be run in the verticle in the Runnable
   that they pass. They should instead use {@link #submit(Runnable)}.

   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class VertxHelperBase extends HelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Stop the global (unclustered instance of Vert.x.
     *  This method is typically called before exiting the JVM.
     */
    public static void closeVertx() {
        // This method should probably be called close(),
        // but WebSocketHelper already has close().

        // ptolemy/actor/gui/HTMLAbout.java calls this
        // method to prevent HTMLAbout from hanging while
        // running.  To replicate:
        // (cd ptolemy/configs/doc; rm models.txt; make models.txt)
        // What happens is that the Hue demos have
        // LiveLinks so the Hue demos are getting parsed,
        // which is instantiating this class and instantiating
        // _vertx.
        // See http://vertx.io/docs/vertx-core/java/#_causing_vert_x_to_exit
        if (_vertx != null) {
            _vertx.close();
        }
        _vertx = null;
    }

    /** Return an instance of this helper for the specified actor, if one
     *  has been created and not garbage collected. Return null otherwise.
     *  If this returns null, the client should create an instance of the appropriate
     *  subclass. That instance will deploy a verticle that will execute jobs
     *  submitted through the {@link #submit(Runnable)} method.
     *  @param actor Either a JavaScript actor or a RestrictedJavaScriptInterface.
     *  @return An instance of this helper for the specified actor.
     */
    public static VertxHelperBase getHelper(Object actor) {
        if (actor instanceof RestrictedJavaScriptInterface) {
            actor = ((RestrictedJavaScriptInterface) actor)._getActor();
        }
        if (!(actor instanceof JavaScript)) {
            throw new IllegalArgumentException("getOrCreateHelper: Must be "
                    + "passed an instance of RestrictedJavaScriptInterface or "
                    + "JavaScript.");
        }
        WeakReference<VertxHelperBase> helperReference = _vertxHelpers
                .get(actor);
        if (helperReference != null) {
            VertxHelperBase helper = helperReference.get();
            if (helper != null) {
                return helper;
            }
        }
        return null;
    }

    /** Reset this helper. This method discards any pending submitted jobs
     *  and marks the helper not busy.
     */
    public void reset() {
        // Execute this in the vert.x event thread.
        submit(new Runnable() {
            @Override
            public void run() {
                // Coverity Scan warned that we were accessing _busy without holding the lock.
                synchronized (VertxHelperBase.this) {
                    _busy = false;
                    // Ensure that any future callbacks are ignored.
                    _nextResponse = 0L;
                    // If there are pending responses, discard them.
                    _deferredHandlers.clear();
                }
            }
        });
    }

    /** Submit a job to be executed by the associated verticle.
     *  The job can invoke Vert.x functionality, specifying callbacks,
     *  and those callbacks will be ensured of running in the same thread
     *  as the job itself. This can be called from any thread.
     *  @param job The job to execute.
     */
    public void submit(Runnable job) {
        // NOTE: Perhaps could use _vertx.currentContext().isEventLoopContext()
        // to determine whether this job can be run immediately. But for now,
        // we always defer the job using the event bus.
        _pendingJobs.add(job);

        // Notify the verticle to process the next job.
        EventBus eventBus = _vertx.eventBus();
        eventBus.publish(_address, "submit");
    }

    /** Return a set of informal image type names that can be sent.
     *  @return A set of image type names.
     */
    public static Set<String> getImageTypes() {
        // Coverity Scan: "Unguarded read
        // (GUARDED_BY_VIOLATION)1. missing_lock: Accessing
        // ptolemy.actor.lib.jjs.VertxHelperBase._sendImageTypes
        // without holding lock"
        synchronized (_receiveTypesMutex) {
            return _sendImageTypes;
        }
    };

    /** Return an array of the types supported by the current host for
     *  receiveType arguments, which are the types that can be extracted from buffers.
     *  @return An array of types.
     */
    public static String[] supportedReceiveTypes() {
        // Formerly, we checked to see if _receiveTypes was null outside of the synchronized block
        // However, Coverity scan warned:
        // "CID 1349635 (#1 of 1): Check of thread-shared field evades
        // lock acquisition
        // (LOCK_EVASION)5. thread2_checks_field_early: Thread2 checks
        // _receiveTypes, reading it after Thread1 assigns to
        // _receiveTypes but before some of the correlated field
        // assignments can occur. It sees the condition
        // ptolemy.actor.lib.jjs.modules.socket.SocketHelper._receiveTypes
        // == null as being false. It continues on before the critical
        // section has completed, and can read data changed by that
        // critical section while it is in an inconsistent state."

        // Avoid FindBugs LI: Unsynchronized Lazy Initialization (FB.LI_LAZY_INIT_UPDATE_STATIC)
        synchronized (_receiveTypesMutex) {
            if (_receiveTypes == null) {
                int length = DATA_TYPE.values().length;
                _receiveTypes = new String[length];
                int i = 0;
                for (DATA_TYPE type : DATA_TYPE.values()) {
                    _receiveTypes[i++] = type.toString().toLowerCase();
                }
            }
            return _receiveTypes;
        }
    }

    /** Return an array of the types supported by the current host for
     *  sendType arguments, which are the types that can be written to buffers.
     *  @return An array of types.
     */
    public static String[] supportedSendTypes() {
        // See supportedReceiveTypes() for why we grab the lock.
        synchronized (_sendTypesMutex) {
            if (_sendTypes == null) {
                int length = DATA_TYPE.values().length;
                _sendImageTypes = _removeDuplicates(ImageIO.getWriterFormatNames());
                _sendTypes = new String[length + _sendImageTypes.size()];
                int i = 0;
                for (DATA_TYPE type : DATA_TYPE.values()) {
                    _sendTypes[i++] = type.toString().toLowerCase();
                }
                for (String imageType : _sendImageTypes) {
                    _sendTypes[i++] = imageType;
                }
            }
            return _sendTypes;
        }
    }

    /** Undeploy the associated verticle.
     *  Note that jobs that are submitted after this is called will
     *  never be executed.
     */
    public void undeploy() {
        if (_deploymentID != null) {
            _vertx.undeploy(_deploymentID);
            _deploymentID = null;
            reset();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected constructors                ////

    /** Construct a helper for the specified JavaScript actor and
     *  create a verticle that can execute submitted jobs atomically.
     *  This is protected to help prevent applications from creating
     *  more than one instance per actor.
     *  @param actor The JavaScript actor that this is helping, or
     *   a RestrictedJavaScriptInterface proxy for that actor.
     *  @param helping The JavaScript object that this is helping.
     */
    protected VertxHelperBase(Object actor, ScriptObjectMirror helping) {
        this(actor, helping, null);
    }

    /** Construct a helper for the specified JavaScript actor and
     *  create a verticle that can execute submitted jobs atomically.
     *  This is protected to help prevent applications from creating
     *  more than one instance per actor.
     *  @param actor The JavaScript actor that this is helping, or
     *   a RestrictedJavaScriptInterface proxy for that actor.
     *  @param helping The JavaScript object that this is helping.
     *  @param helper The helper providing the verticle and event
     *   handler, or null to create a new verticle and event handler.
     */
    protected VertxHelperBase(Object actor, ScriptObjectMirror helping, VertxHelperBase helper) {
        super(actor, helping);
        
        // See whether there is already a verticle associate with this actor
        // and use it if there is.
        if (helper == null) {
            helper = getHelper(actor);
        }

        // If still no verticle is specified, then create one. Also register
        // this helper as the helper for the actor. If a verticle is specified,
        // then we assume that there is already a helper registered for the actor.
        if (helper == null) {
            _vertxHelpers.put(_actor, new WeakReference<VertxHelperBase>(this));

            _verticle = new AccessorVerticle();
            _vertx.deployVerticle(_verticle, result -> {
                _deploymentID = result.result();
            });
        } else {
            _verticle = helper._verticle;
            // Also use the _pendingJobs list of the helper so that any submitted jobs
            // are handled by that helper.
            _pendingJobs = helper._pendingJobs;
        }
        _address = _actor.getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the specified response in the same order as the request
     *  that triggered the response. The execution will be done in the
     *  director thread, not in the verticle,
     *  so that it is executed atomically with respect to the swarmlet
     *  execution. This ensures, for example, that if the response
     *  produces multiple output events or errors, that all those
     *  output events and errors are simultaneous. It also prevents
     *  threading issues from having the response execute concurrently
     *  with the swarmlet execution.
     *
     *  Specifically,
     *  if the specified request number matches the next expected response,
     *  then execute the specified response. Otherwise, if there is an
     *  earlier expected response, then defer this one, and if a response
     *  has already been issued for this request, then discard this response.
     *  This must be called in a vert.x event loop; i.e., it should be called
     *  only within handlers for vert.x objects.
     *
     *  Specifically:
     *  If the requestNumber matches the _nextResponse number, then
     *  execute the specified response.
     *  If the requestNumber is less than _nextResponse, then queue the
     *  response for later execution.
     *  If the requestNumber is greater than _nextResponse, then discard
     *  the response.
     *  If the done argument is true,
     *  then after this response is executed, increment the _nextResponse
     *  number and check for any deferred requests that match that new
     *  number, and execute that one.
     *  @param requestNumber The number of the request.
     *  @param done True to indicate that this request is complete.
     *  @param response The response to execute.
     */
    protected void _issueOrDeferResponse(long requestNumber, boolean done,
            Runnable response) {
        // System.err.println("===========" + Thread.currentThread().getName());
        if (requestNumber > _nextResponse) {
            // Defer the request.
            // System.err.println("****** Deferring response to " + requestNumber);
            HandlerInvocation handler = new HandlerInvocation();
            handler.response = response;
            handler.done = done;
            LinkedList<HandlerInvocation> deferred = _deferredHandlers
                    .get(requestNumber);
            if (deferred == null) {
                deferred = new LinkedList<HandlerInvocation>();
                _deferredHandlers.put(requestNumber, deferred);
            }
            deferred.add(handler);
        } else if (requestNumber == _nextResponse) {
            // System.err.println("****** Issuing response to " + requestNumber);

            // Execute the response in the director thread using a timeout.
            _issueResponse(response);

            if (done) {
                // Look for deferred responses that are next in the sequence.
                // System.err.println("****** Done with request request " + requestNumber);
                _nextResponse++;
                LinkedList<HandlerInvocation> nextResponses = _deferredHandlers
                        .get(_nextResponse);
                boolean deferredResponseDone = nextResponses != null;
                while (nextResponses != null && deferredResponseDone) {
                    // There are matching deferred responses.
                    for (HandlerInvocation nextResponse : nextResponses) {
                        // System.err.println("****** Issuing response to " + _nextResponse);
                        _issueResponse(nextResponse.response);
                        if (nextResponse.done) {
                            // The response is done.
                            // System.err.println("****** Done with request request " + _nextResponse);
                            _deferredHandlers.remove(_nextResponse);
                            _nextResponse++;
                            nextResponses = _deferredHandlers
                                    .get(_nextResponse);
                            deferredResponseDone = true;
                            // Skip any remaining parts of this response.
                            break;
                        } else {
                            // The next response is not done yet.
                            // Continue with any responses in the list, but unless one of those
                            // marks this response done, do not proceed to the next response.
                            deferredResponseDone = false;
                        }
                    }
                }
            }
        }
    }

    /** If the verticle is not busy, process the next pending job.
     *  This method should be called only by the verticle.
     *  @see #_setBusy(boolean)
     */
    protected void _processPendingJob() {
        boolean busy = false;
        synchronized (this) {
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
     *  this with argument true, please be sure you later call it with argument
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

    /** Verticle supporting this helper. */
    protected AccessorVerticle _verticle;

    /** Global (unclustered) instance of Vert.x core. */
    protected static Vertx _vertx = null;

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Event bus address for notifications. */
    private String _address;

    /** Flag indicating whether job requests should be deferred. */
    private boolean _busy;

    /** The deplotmentID, if deploying the verticle is successful. */
    private String _deploymentID;

    /** Queue of pending jobs. */
    private ConcurrentLinkedQueue<Runnable> _pendingJobs = new ConcurrentLinkedQueue<Runnable>();

    /** The array of receive type names. */
    private static String[] _receiveTypes;

    /** A mutex used when creating _receiveTypes. */
    private static Object _receiveTypesMutex = new Object();

    /** The set of informal image type names that can be sent. */
    private static TreeSet<String> _sendImageTypes;

    /** The array of send type names. */
    private static String[] _sendTypes;

    /** A mutex used when creating _sendTypes. */
    private static Object _sendTypesMutex = new Object();

    static {
        try {
            // If the vertx.cacheDirBase property is not set, then set it to $HOME/.vertxPt
            // See https://www.terraswarm.org/testbeds/wiki/Vert/VertxDirectories
            String cacheDirBase = StringUtilities.getProperty("vertx.cacheDirBase");
            if (cacheDirBase.isEmpty()) {
                File directory = null;
                try {
                    directory = new File(StringUtilities.getProperty("user.home"),  ".vertxPt");
                    System.setProperty("vertx.cacheDirBase", directory.getCanonicalPath());
                } catch (Throwable throwable) {
                    System.err.println("Could not set the vertx.cacheDirBase property to " + directory
                            + ".  This means that a .vertx directory will be created in the current directory.");
                }
            }
            _vertx = Vertx.vertx();
        } catch (Throwable throwable) {
            System.err.println("Static initialization of VertxHelperBase failed.");
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Sequence number of the next expected response. */
    private long _nextResponse = 0L;

    /** Queue of deferred responses. */
    private HashMap<Long, LinkedList<HandlerInvocation>> _deferredHandlers = new HashMap<Long, LinkedList<HandlerInvocation>>();

    /** Index of Vertx helpers by actor. */
    private static WeakHashMap<JavaScript, WeakReference<VertxHelperBase>> _vertxHelpers = new WeakHashMap<JavaScript, WeakReference<VertxHelperBase>>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Verticle to handle requests.
     */
    public class AccessorVerticle extends AbstractVerticle {

        /** Register a handler to the event bus to process pending jobs. */
        @Override
        public void start() {
            // Listen on the event bus for notifications to process jobs.
            EventBus eventBus = _vertx.eventBus();
            eventBus.consumer(_address, message -> {
                _processPendingJob();
            });
            // Process any jobs that have been submitted.
            _processPendingJob();
        }

        /** Clear all pending jobs. */
        @Override
        public void stop() {
            _pendingJobs.clear();
        }
    }

    /** A structure for storing a deferred handler invocation. */
    private static class HandlerInvocation {
        // Coverity Scan suggested making this inner class static.
        public Runnable response;
        public boolean done;
    }
}

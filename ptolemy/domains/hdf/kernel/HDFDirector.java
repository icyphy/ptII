/* Director for the heterochronous dataflow model of computation.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.hdf.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.CachedSDFScheduler;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HDFDirector

/**
 The Heterochronous Dataflow (HDF) domain is an extension of the
 Synchronous Dataflow (SDF) domain and implements the HDF model of
 computation [1]. In SDF, the set of port rates (called rate signatures)
 of an actor are constant. In HDF, however, rate signatures are allowed
 to change between iterations of the HDF schedule.
 <p>
 This director is often used with HDFFSMDirector. The HDFFSMDirector
 governs the execution of a modal model. The change of rate signatures can
 be modeled by state transitions of the modal model, in which each state
 refinement infers a set of rate signatures. Within each state, the HDF
 model behaves like an SDF model.
 <p>
 This director recomputes the schedules dynamically. To improve efficiency,
 this director uses a CachedSDFScheduler. A CachedSDFScheduler caches
 schedules labeled by their corresponding rate signatures, with the most
 recently used at the beginning of the queue. Therefore, when a state in HDF
 is revisited, the schedule identified by its rate signatures in the cache
 is used. We do not need to recompute the schedule.
 <p>
 The size of the cache in the CachedSDFScheduler is set by the
 <i>scheduleCacheSize</i> parameter of HDFDirector. The default value of
 this parameter is 100. If the cache is full, the least recently used
 schedule (at the end of the cache) is discarded.
 <p>
 <b>References</b>
 <p>
 <OL>
 <LI>
 A. Girault, B. Lee, and E. A. Lee,
 ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">
 Hierarchical Finite State Machines with Multiple Concurrency Models</A>,
 '' April 13, 1998.</LI>
 </ol>

 @see HDFFSMDirector
 @see CachedSDFScheduler

 @author Ye Zhou. Contributor: Brian K. Vogel
 @version $Id$
 @since Ptolemy II 5.0
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class HDFDirector extends SDFDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public HDFDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public HDFDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The HDFDirector will have a default scheduler of type
     *  SDFScheduler.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public HDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                           ////

    /** A parameter representing the size of the schedule cache to use.
     *  If the value is less than or equal to zero, then schedules
     *  will never be discarded from the cache. The default value is 100.
     *  <p>
     *  Note that the number of schedules in an HDF model can be
     *  exponential in the number of actors. Setting the cache size to a
     *  very large value is therefore not recommended if the
     *  model contains a large number of HDF actors.
     */
    public Parameter scheduleCacheSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute changed is the <i>scheduleCacheSize</i> parameter,
     *  construct the cache in the associated CachedSDFScheduler with the
     *  given cache size.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the super class throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == scheduleCacheSize) {
            int cacheSize = ((IntToken) scheduleCacheSize.getToken())
                    .intValue();
            ((CachedSDFScheduler) getScheduler()).constructCaches(cacheSize);
        }

        super.attributeChanged(attribute);
    }

    /** Send a request to the manager to get the HDF schedule if the schedule
     *  is not valid or this director is not at the top level.
     *  @exception IllegalActionException If no schedule can be found,
     *  or if the super class method throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // If this director is not at the top level, the HDFFSMDirector
        // of the modal model that it contains may change rates after
        // making a change request, which invalidates this HDF's schedule.
        // So we need to get the schedule of this HDFDirector also in a
        // change request.
        if (!isScheduleValid() || getContainer() != toplevel()) {
            CompositeActor container = (CompositeActor) getContainer();
            ChangeRequest request = new ChangeRequest(this, "reschedule") {
                @Override
                protected void _execute() throws KernelException {
                    getScheduler().getSchedule();
                }
            };

            request.setPersistent(false);
            container.requestChange(request);
        }

        return super.postfire();
    }

    /** Preinitialize the actors associated with this director.
     *  The super class method will compute the schedule.
     *  @exception IllegalActionException If the super class
     *  preinitialize throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        ((CachedSDFScheduler) getScheduler()).clearCaches();
        super.preinitialize();
    }

    /** Override the base class to ensure that the scheduler is an
     *  SDFScheduler and that its <i>constrainBufferSizes</i>
     *  parameter is set to false.
     *  @param scheduler The scheduler that this director will use.
     *  @exception IllegalActionException If the scheduler is not
     *   an instance of SDFScheduler.
     *  @exception NameDuplicationException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     */
    @Override
    public void setScheduler(Scheduler scheduler)
            throws IllegalActionException, NameDuplicationException {
        if (!(scheduler instanceof SDFScheduler)) {
            throw new IllegalActionException(this,
                    "Scheduler is required to be an instance of SDFScheduler");
        }

        // FIXME: Instead, should fix the buffer sizes calculation.
        ((SDFScheduler) scheduler).constrainBufferSizes.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object. In this case, we give the HDFDirector a
     *  default scheduler of the class CachedSDFScheduler and a
     *  cacheSize parameter with default value 100.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        // During construction, create the scheduleCacheSize parameter
        // with default value of 100.
        int cacheSize = 100;
        scheduleCacheSize = new Parameter(this, "scheduleCacheSize",
                new IntToken(cacheSize));

        try {
            CachedSDFScheduler scheduler = new CachedSDFScheduler(this,
                    uniqueName("Scheduler"), cacheSize);
            setScheduler(scheduler);
        } catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" + e.getMessage());
        }

        allowRateChanges.setToken(BooleanToken.TRUE);
        allowRateChanges.setVisibility(Settable.EXPERT);
        allowRateChanges.setPersistent(false);
    }
}

/* The static scheduler for the continuous time domain.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel;

import ptolemy.actor.sched.FixedPointScheduler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ContinuousScheduler

/**
 The Static scheduler for the CT domain.
 A continuous-time (sub)system can be mathematically represented as:<Br>
 <pre>
 <pre>    dx/dt = f(x, u, t)<Br>
 <pre>    y = g(x, u, t)<BR>
 </pre></pre></pre>
 where x is the state of the system, u is the input, y is the output,
 f() is the state transition map and g() is the output map.
 <P>
 The system is built using actors. That is, all the functions, f() and g(),
 are built up by chains of actors.  For higher order systems,
 x is a vector, built using more than one integrator.
 In general, actors that have the functionality of integration
 from their inputs to their outputs are called <I>dynamic actors</I>.
 Other actors are called <I>arithmetic actors</I>.
 <P>
 In continuous-time simulation, time progresses in a discrete way.
 The distance between two consecutive time points is called the
 <I>integration step size</I> or step size, for short. Some actors
 may put constraints on the choice of the step size.
 These actors are called <I>step size control actors</I>. Examples of step
 size control actors include integrators, which control the
 accuracy and speed of numerical ODE solutions, and event generators,
 which produce discrete events.
 <P>
 If there are loops of arithmetic actors,
 then the (sub)system are not schedulable, and a NotSchedulableException
 will be thrown when schedules are requested.
 <P>
 This scheduler is based on the CTScheduler by Jie Liu, Haiyang Zheng, and
 Ye Zhou. It eliminates the classification of actors into multiple categories
 that that scheduler relied on.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see ptolemy.actor.sched.Scheduler
 */
public class ContinuousScheduler extends FixedPointScheduler {

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ContinuousScheduler(ContinuousDirector container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    // FIXME: add two special schedules: ContinuousStepSizeControlActor 
    // and ContinuousStatefulActor 
}

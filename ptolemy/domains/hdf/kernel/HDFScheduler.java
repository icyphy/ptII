/* A Scheduler for the HDF domain

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.*;
import ptolemy.math.Fraction;
import ptolemy.domains.sdf.kernel.SDFScheduler;

import java.util.*;

///////////////////////////////////////////////////////////
//// HDFScheduler
/**
This class is a scheduler that implements basic scheduling of HDF graphs. 
HDF is similar to SDF in that a schedule is constructed by 
solving the balance equations. In SDF the actors in a graph 
typically have constant rates throughout the execution of a 
model. In HDF, however, each actor has a finite combination 
of port rates, which are allowed to change after each minimal 
period of the schedule. A particular combination of port rates 
for an actor constitutes its type signature.
<p>
Note that SDF scheduler could be used to compute schedules for HDF. 
This could be done be invalidating the current SDF schedule and 
computing a new one whenever a type signature change occurs. 
However, since actor type signatures generally change often at 
runtime, and SDF schedules are expensive to compute, this would 
be very inefficient.
<p>
This class attempts to minimize the cost of computing schedules 
by caching the schedules and reusing them. The method 
<i>setCacheSize()</i> is used to set the cache size. The 
default schedule cache size is 100.

@see ptolemy.actor.sched.Scheduler
@see ptolemy.domains.sdf.kernel.SDFScheduler

@author Brian K. Vogel
@version $Id$
*/
public class HDFScheduler extends SDFScheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public HDFScheduler() {
        super();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public HDFScheduler(Workspace workspace) {
        super(workspace);
    }

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
    public HDFScheduler(Director container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Set the size of the schedule cache. The default value
     *  is 100. An infinite-size cache can be used by invoking
     *  this method with a <i>size</i> less than or equal to zero. 
     *  Note that the number of schedules in an HDF model can be 
     *  exponential in the number of actors. Setting the cache size to a
     *  very large value is therefore not recommended if the
     *  model contains a large number of HDF actors.
     *
     *  @param size The cache size to use.
     *  @exception IllegalActionException If an invalid cache size
     *   is supplied.
     */
    public void setCacheSize(int size) {
	_cacheSize = size;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following 
     *  circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>No integer solution exists for the balance equations.
     *  <li>The graph contains cycles without delays (deadlock).
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  </ul>
     *
     * If the current schedule is not valid (isValid returns false), 
     * then the local schedule cache is checked to see if it 
     * contains the schedule corresponding to the current graph. 
     * If so, then the cached schedule is returned. Otherwise, a 
     * new schedule is computed and returned.
     *
     * @return A Schedule of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     */
    protected Schedule _getSchedule() throws NotSchedulableException {
	return super._getSchedule();
	// FIXME: This method is not implemented as documented. 
	// The cache size parameter is currently ignored.
	// Implement this!
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _cacheSize = 100;
}

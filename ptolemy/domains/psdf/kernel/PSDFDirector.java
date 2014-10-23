/* Director for the synchronous dataflow model of computation.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.psdf.kernel;

import java.util.Iterator;

import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PSDFDirector

/**
 The Parameterized Synchronous Dataflow (PSDF) domain is an
 extension of the Synchronous Dataflow (SDF) domain that allows for
 more extensive reconfiguration of models.  The SDF domain uses
 static analysis of variable dependence to detect cases where rate
 parameters may change.  By default, SDF disallows reconfiguration
 of all rate parameters.  If rate parameters are allowed to change,
 then SDF checks that rate parameters do not change during execution
 of the schedule, and declares that inferred rate parameters for
 external ports change as often as the internal rate parameters.

 <p>This domain offers two key extensions:
 <ol>
 <li> Dependence analysis is used to determine if the modification to the
 rate parameters occurs during execution of the SDF schedule.  If
 this test passes, then a parameterized schedule is constructed.

 <li> The generated schedule is checked for local synchrony, to determine
 if external rate parameters may change.  The correct dependency
 information for external ports is then declared.

 </ol>

 Note that the resulting behavior is identical to the SDF scheduler,
 with rate parameter changes allowed, except much more efficient, since
 scheduling on the fly is not necessary during every reconfiguration.
 Additionally, code can be generated for the PSDF domain that allows
 for efficient reconfiguration.  The added precision of dependency analysis
 for external rate parameters also means that some hierarchical models that
 would be ruled out by the SDF checks are allowed.

 @see ptolemy.domains.psdf.kernel.PSDFScheduler

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 3.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
public class PSDFDirector extends SDFDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The PSDFDirector will have a default scheduler of type PDFScheduler.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PSDFDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The PSDFDirector will have a default scheduler of type PDFScheduler.
     *
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PSDFDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given
     *  name.  The container argument must not be null, or a
     *  NullPointerException will be thrown.  If the name argument is
     *  null, then the name is set to the empty string. Increment the
     *  version number of the workspace.  The PSDFDirector will have a
     *  default scheduler of type PSDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public PSDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Indicate that a schedule for the model may no longer be valid.
     *  This method should be called when topology changes are made,
     *  or for that matter when any change that may invalidate the
     *  schedule is made.  In this base class, this method sets a flag
     *  that forces scheduling to be redone at the next opportunity.
     *  If there is no scheduler, do nothing.
     */
    @Override
    public void invalidateSchedule() {
        super.invalidateSchedule();

        // Kill the firing counts, which may be invalid.  If we don't
        // kill them, then the manager might complain that they cannot
        // be evaluated.
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container != null) {
            for (Iterator entities = container.deepEntityList().iterator(); entities
                    .hasNext();) {
                Entity actor = (Entity) entities.next();
                Variable parameter = (Variable) actor
                        .getAttribute("firingCount");

                if (parameter != null) {
                    parameter.setExpression("0");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the PSDFDirector a
     *  default scheduler of the class PSDFScheduler.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        /* PSDFScheduler scheduler = */new PSDFScheduler(this,
                uniqueName("Scheduler"));
    }
}

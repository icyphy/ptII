/* Director for the synchronous dataflow model of computation.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.psdf.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.domains.sdf.kernel.SDFDirector;

// ssb psdf-related imports
import ptolemy.graph.sched.*;
import synthesis.dif.psdf.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// PSDFDirector
/**
<h1>PSDF overview</h1> The Parameterized Synchronous Dataflow(PSDF)
domain is an extension of the Synchronous Dataflow(SDF) domain that
allows for more extensive reconfiguration of models.  The SDF domain
uses static analysis of variable dependence to detect cases where rate
parameters may change.  By default, SDF disallows reconfiguration of
all rate parameters.  If rate parameters are allowed to change, then
SDF checks that rate parameters do not change during execution of the
schedule, and declares that inferred rate parameters for external
ports change as often as the internal rate parameters.  

This domain offers two key extensions:

1) Dependence analysis is used to determine if the modification to the
   rate parameters occurs during execution of the SDF schedule.  If
   this test passes, then a parameterized schedule is constructed.

2) The generated schedule is checked for local synchrony, to determine
   if external rate parameters may change.  The correct dependency
   information for external ports is then declared.

Note that the resulting behavior is identical to the SDF scheduler,
with rate parameter changes allowed, except much more efficient, since
scheduling on the fly is not necessary during every reconfiguration.
Additionally, code can be generated for the PSDF domain that allows
for efficient reconfiguration.  The added precision of dependency analysis
for external rate parameters also means that some hierarchical models that
would be ruled out by the SDF checks are allowed.


@see ptolemy.domains.sdf.kernel.PSDFScheduler

@author Steve Neuendorffer
@version $Id$
*/
public class PSDFDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The PSDFDirector will have a default scheduler of type PDFScheduler.
     */
    public PSDFDirector() 
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The PSDFDirector will have a default scheduler of type PDFScheduler.
     *
     *  @param workspace The workspace for this object.
     */
    public PSDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
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




    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected boolean _postfirereturns = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Print a debugging message if the debugging flag is turned on.
    private void _debugMessage(String message) {
        if (_debugFlag) {
            System.out.print(message);
        }
    }

    /** Initialize the object.   In this case, we give the PSDFDirector a
     *  default scheduler of the class PSDFScheduler.
     */
    private void _init() throws IllegalActionException, NameDuplicationException
            {
        PSDFScheduler scheduler =
            new PSDFScheduler(this, uniqueName("Scheduler"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Debugging flag.
    private boolean _debugFlag = true;


}

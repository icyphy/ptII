/* An instance of FunctionDependencyOfEnabledCompositeActor describes the
 function dependency between the outputs and the inputs of an enabled
 composite actor.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependenceOfEnabledCompositeActor

/**
 An instance of this class describes the dependency that data at an output port
 has on data at an input port in a firing of an EnabledComposite actor.
 In particular, all output ports depend on the enable input port. The
 dependencies of the ouput ports having on the other inptus are inferred from
 the dependencies of the contained actors.

 @see ptolemy.actor.util.FunctionDependencyOfCompositeActor
 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class FunctionDependencyOfEnabledCompositeActor extends
        FunctionDependencyOfCompositeActor {

    /** Construct a FunctionDependency for the given actor.
     *  The name of this attribute is always "_functionDependency".
     *  @param compositeActor The composite actor with which this function
     *  dependency is associated.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FunctionDependencyOfEnabledCompositeActor(
            CompositeActor compositeActor) throws IllegalActionException,
            NameDuplicationException {
        super(compositeActor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Construct the detailed dependency graph for the container, an
     *  EnabledComposite actor. In particular, all output ports depend
     *  on the enable input port. The dependencies of the ouput ports
     *  having on the other inptus are inferred from the dependencies
     *  of the contained actors.
     */
    protected void _constructDetailedDependencyGraph() {
        super._constructDetailedDependencyGraph();
        // Note: cannot call getDetailedDependencyGraph()! because the
        // _version is not updated yet and it will result in an infinite loop.
        CompositeActor actor = (CompositeActor) getContainer();
        if (actor instanceof EnabledComposite) {
            IOPort enable = ((EnabledComposite) actor).enable;
            Iterator outputs = ((Actor) getContainer()).outputPortList()
                    .listIterator();
            while (outputs.hasNext()) {
                IOPort output = (IOPort) outputs.next();
                _detailedDependencyGraph.addEdge(enable, output);
            }
        } else {
            // If the container is not an EnabledComposite, do nothing. Or,
            // an exception can be thrown here.
        }
    }
}

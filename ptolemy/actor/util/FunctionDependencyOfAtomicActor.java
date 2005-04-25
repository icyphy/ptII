/* An instance of FunctionDependencyOfAtomicActor describes the function
   dependency between the inputs and outputs of an atomic actor.

   Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.util;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfAtomicActor

/**
   An instance of FunctionDependencyOfAtomicActor describes the function
   dependency between the inputs and outputs of an atomic actor.
   By default, each output port of an atomic actor depends on all input
   ports of the actor, meaning that the token sent through an output
   depends on all the tokens received from the input ports.
   For some atomic actors, such as the TimedDelay actor, an output in
   a firing does not depend on an input port.
   (See {@link FunctionDependency} for the definition of dependency.)
   Such actors should override the pruneDependencies() method of
   AtomicActor to remove dependencies between these ports.
   For example, {@link ptolemy.domains.de.lib.TimedDelay}
   declares that its <i>output</i> port is independent of its <i>input</i>
   port by defining this method:
   <pre>
   public void pruneDependencies() {
   super.pruneDependencies();
   removeDependency(input, output);
   }
   </pre>

   @see FunctionDependency
   @see ptolemy.domains.de.lib.TimedDelay
   @see ptolemy.actor.AtomicActor#pruneDependencies()
   @author Haiyang Zheng
   @version $Id: FunctionDependencyOfAtomicActor.java,v 1.2 2004/02/21
   07:57:24 hyzheng Exp $
   @since Ptolemy II 4.0
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (eal)
*/
public class FunctionDependencyOfAtomicActor extends FunctionDependency {
    /** Construct a FunctionDependencyOfAtomicActor in the given actor.
     *  @param atomicActor The atomic actor.
     *  @param name The name for this attribute.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FunctionDependencyOfAtomicActor(AtomicActor atomicActor, String name)
        throws IllegalActionException, NameDuplicationException {
        super(atomicActor, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove the dependency that the specified output port has,
     *  by default, on the specified input port. By default, each
     *  output port is assumed to have a dependency on all input
     *  ports. This method is called by the removeDependency() method
     *  of AtomicActor, which in turn is called by actors in
     *  their pruneDependencies() method.
     *  @param inputPort The input port.
     *  @param outputPort The output port that does not depend on the
     *   input port.
     *  @see ptolemy.actor.AtomicActor#removeDependency(IOPort, IOPort)
     *  @see ptolemy.actor.AtomicActor#pruneDependencies()
     */
    public void removeDependency(IOPort inputPort, IOPort outputPort) {
        // We do not need the validity checking because this method is
        // only called from the _constructDependencyGraph() method, which
        // again can only be accessed from the _validate() method.
        // The _validate() method does the validity checking already
        // and gets the read access of workspace.
        DirectedGraph dependencyGraph = _dependencyGraph;

        // Note we can not use iterator here because the edges() method
        // returns an unmodifiableList. The removeEdge() method will cause
        // a concurrentModification exception.
        Object[] edges = dependencyGraph.edges().toArray();

        for (int i = 0; i < edges.length; i++) {
            Edge edge = (Edge) edges[i];

            if (edge.source().getWeight().equals(inputPort)
                            && edge.sink().getWeight().equals(outputPort)) {
                dependencyGraph.removeEdge(edge);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Construct a dependency graph. This method first constructs
     *  the default dependency graph, which asserts that each output
     *  depends on all inputs, and then calls the
     *  {@link ptolemy.actor.AtomicActor#pruneDependencies()} method.
     */
    protected void _constructDependencyGraph() {
        super._constructDependencyGraph();
        ((AtomicActor) getContainer()).pruneDependencies();
    }
}

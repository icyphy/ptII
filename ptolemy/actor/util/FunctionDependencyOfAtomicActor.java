/* An instance of FunctionDependencyOfAtomicActor describes the function
   dependency information of an atomic actor.

   Copyright (c) 2003-2004 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfAtomicActor
/**
   An instance of FunctionDependencyOfAtomicActor describes the function
   dependence relation of an atomic actor. 
   <p>
   Most atomic actors have all their output ports depend on their input 
   ports. For some atomic actors, such as the TimedDelay actor, its output 
   does not depend on its input port. See {@link FunctionDependency} for 
   accurate definition of dependency. Therefore, this class provides a
   removeDependence() method to specify this special cases.  
   <p>
   Take the {@link ptolemy.domains.de.lib.TimedDelay} actor as an example,
   to declare that its output is independent of its input, specify 
   removeDependency(input, output) inside the removeDependencies() method. 

   @see FunctionDependency
   @see ptolemy.domains.de.lib.TimedDelay
   @author Haiyang Zheng
   @version $Id: FunctionDependencyOfAtomicActor.java,v 1.2 2004/02/21
   07:57:24 hyzheng Exp $
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class FunctionDependencyOfAtomicActor extends FunctionDependency {

    /** Construct a FunctionDependencyOfAtomicActor in the given actor.
     *  @param actor The actor.
     */
    public FunctionDependencyOfAtomicActor(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Declares that an output port does not depend on an input port.
     *
     *  @param inputPort An input port.
     *  @param outputPort An output Port.
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
            if (edge.source().getWeight().equals(inputPort) &&
                edge.sink().getWeight().equals(outputPort)) {
                dependencyGraph.removeEdge(edge); 
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Construct a dependency graph. This method calls the 
     *  {@link #removeDependency} method. 
     */
    protected void _constructDependencyGraph() {
        super._constructDependencyGraph();
        ((AtomicActor)getActor()).removeDependencies();
    }
    
}

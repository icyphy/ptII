/*
   Provides the implementation for actors that are predefined and don't need
   to be analyzed.

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;

import java.util.HashSet;
import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.Port;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Predefined
/**
   Provides the implementation for actors that are predefined and don't need
   to be analyzed.
@author Matthew Koecher

@since Ptolemy II 2.0
*/
public class Predefined {

    private static final String[] _definedNames = {
	"ptolemy.domains.sdf.lib.SampleDelay", "ptolemy.domains.sdf.lib.FIR"
    };

    public Predefined(){
	_definedSet=new HashSet();
	for (int i=0; i < _definedNames.length; i++){
	    _definedSet.add(_definedNames[i]);
	}
    }

    public boolean isDefined(Entity entity){
	return _definedSet.contains(entity.getClass().getName());
    }

    public void convertEntityToGraph(Entity entity, DirectedGraph graph)
	throws IllegalActionException {

	if (!isDefined(entity))
	    throw new IllegalActionException(entity+" not predefined");

	String name=entity.getClass().getName();
	if (name.equals("ptolemy.domains.sdf.lib.SampleDelay"))
	    graphSampleDelay(entity, graph);
	else if (name.equals("ptolemy.domains.sdf.lib.FIR"))
	    graphFIR(entity, graph);
    }

    protected void graphSampleDelay(Entity entity, DirectedGraph graph){
	Port input = entity.getPort("input");
	Port output = entity.getPort("output");
	//CircuitNode delay = new CircuitNode(entity);
	//String delay="delay"+count++;
	graph.addNodeWeight(input);
	graph.addNodeWeight(output);
	graph.addNodeWeight(entity);
	graph.addEdge(input, entity);
	graph.addEdge(entity, output);
    }

    protected void graphFIR(Entity entity, DirectedGraph graph){
	Port input = entity.getPort("input");
	Port output = entity.getPort("output");
	//CircuitNode fir = new CircuitNode(entity);
	//String fir="FIR"+count++;
	graph.addNodeWeight(input);
	graph.addNodeWeight(output);
	graph.addNodeWeight(entity);
	graph.addEdge(input, entity);
	graph.addEdge(entity, output);
    }

    private HashSet _definedSet;
}

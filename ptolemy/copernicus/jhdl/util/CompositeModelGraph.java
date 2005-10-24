/*

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.jhdl.util;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.graph.*;

//import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
////

/**

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
@Pt.ProposedRating Red (cxh)
@Pt.AcceptedRating Red (cxh)
*/
public class CompositeModelGraph extends ModelGraph {
    public CompositeModelGraph(CompositeActor entity, Map options) {
        super(entity);
        _options = options;
        _modelGraphNodes = new Vector();
        _build();
    }

    public CompositeActor getCompositeActor() {
        return (CompositeActor) getEntity();
    }

    protected void _build() {
        // 1. Loop over all the entities in the model. For each entity,
        //    add the following to the top-level graph:
        //    - a Node in the graph for the entity
        //    - a Node in the graph for each input and output
        //      port of the entity
        //    - appropriate edges between input/output ports and entity
        for (Iterator i = getCompositeActor().entityList().iterator();
                    i.hasNext();) {
            ComponentEntity entity = (ComponentEntity) i.next();

            // Create Node for entity
            Object entityWeight = null;

            if (entity instanceof CompositeActor) {
                entityWeight = new CompositeModelGraph((CompositeActor) entity,
                        _options);
            } else if (entity instanceof AtomicActor) {
                entityWeight = new ActorModelGraph((AtomicActor) entity,
                        _options);
            } else {
                entityWeight = new ModelGraph(entity);
            }

            Node entityNode = addNodeWeight(entityWeight);
            _modelGraphNodes.add(entityNode);

            // iterate over all outPorts and add Node corresponding
            // to port. Also add edge between entity and port
            for (Iterator outPorts = ((Actor) entity).outputPortList().iterator();
                        outPorts.hasNext();) {
                Object port = outPorts.next();
                Node outputPortNode = addNodeWeight(port);
                addEdge(entityNode, outputPortNode);
            }

            // iterate over all inPorts and add Node corresponding
            // to port. Also add edge between entity and port
            for (Iterator inPorts = ((Actor) entity).inputPortList().iterator();
                        inPorts.hasNext();) {
                Object port = inPorts.next();
                Node inputPortNode = addNodeWeight(port);
                addEdge(inputPortNode, entityNode);
            }
        }

        // 2. Add top-level outputPorts (no connections)
        for (Iterator outputPorts = getCompositeActor().outputPortList()
                                                .iterator();
                    outputPorts.hasNext();) {
            IOPort port = (IOPort) outputPorts.next();
            Node n = addIOPortNode(port);
        }

        // 3. Add top-level inputPorts & associated connections
        // TODO: Does this catch connections between top-level input
        // ports and top-level output ports?
        for (Iterator inputPorts = getCompositeActor().inputPortList().iterator();
                    inputPorts.hasNext();) {
            IOPort port = (IOPort) inputPorts.next();
            Node n = addIOPortNode(port);

            for (Iterator insideSinks = port.insideSinkPortList().iterator();
                        insideSinks.hasNext();) {
                IOPort insideSink = (IOPort) insideSinks.next();

                //Node insideEntity = portEntityNodeMap.get(insideSink);
                addEdge(port, insideSink);
            }
        }

        // 4. Iterate over all output ports and make connections in graph
        // representing topology of model
        for (Iterator i = getCompositeActor().entityList().iterator();
                    i.hasNext();) {
            Entity entity = (Entity) i.next();

            for (Iterator outPorts = ((Actor) entity).outputPortList().iterator();
                        outPorts.hasNext();) {
                IOPort port = (IOPort) outPorts.next();

                for (Iterator sinkPorts = port.sinkPortList().iterator();
                            sinkPorts.hasNext();) {
                    IOPort sinkPort = (IOPort) sinkPorts.next();

                    addEdge(port, sinkPort);
                }
            }
        }
    }

    public Collection getModelGraphNodes() {
        return _modelGraphNodes;
    }

    protected Map _options;
    protected Collection _modelGraphNodes;
}

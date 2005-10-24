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
package ptolemy.copernicus.jhdl.circuit;

import ptolemy.actor.*;
import ptolemy.copernicus.jhdl.util.*;
import ptolemy.data.type.*;
import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
////

/**
 * This class provides a static method that takes a regular Ptolemy
 * CompositeActor and generates a "JHDLCompositeActor". This method
 * copies the hierarchy of the Ptolemy actor. For leaf Ptolemy actors
 * that need to be "synthesized", this method calls createGraph
 * on ModelGraph2Entity and then generates a JHDLCompositeActor
 * from the atomic actor using the ModelGraph2Entity class.

 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class CompositeActor2JHDLCompositeActor {
    public static JHDLCompositeActor build(CompositeActor oldTopEntity,
        Map options) throws IllegalActionException, NameDuplicationException {
        Map old2newMap = new HashMap();

        // create new actor
        JHDLCompositeActor newTopEntity = new JHDLCompositeActor();
        newTopEntity.setName(oldTopEntity.getName());

        // add matching ports to the actor
        Map portMap = copyPorts(oldTopEntity, newTopEntity);
        old2newMap.putAll(portMap);

        /*
          for (Iterator i = oldTopEntity.portList().iterator(); i.hasNext();) {
          IOPort port = (IOPort) i.next();
          JHDLIOPort newPort = (JHDLIOPort) newTopEntity.newPort(port.getName());
          newPort.setInput(port.isInput());
          old2newMap.put(port,newPort);
          }
        */

        // iterate over entities within composite
        for (Iterator i = oldTopEntity.entityList().iterator(); i.hasNext();) {
            ComponentEntity oldEntity = (ComponentEntity) i.next();
            System.out.println("Copying entity " + oldEntity);

            // create a new entity in JHDL land
            ComponentEntity newEntity = null;

            if (oldEntity instanceof CompositeActor) {
                newEntity = build((CompositeActor) oldEntity, options);
            } else if (oldEntity instanceof AtomicActor) {
                newEntity = build((AtomicActor) oldEntity, options);
            } else {
                System.out.print("actor not built: " + oldEntity);
            }

            // else what?
            newEntity.setContainer(newTopEntity);

            old2newMap.put(oldEntity, newEntity);

            // create mapping between entity old ports and new ports
            for (Iterator k = oldEntity.portList().iterator(); k.hasNext();) {
                IOPort oldPort = (IOPort) k.next();

                for (Iterator j = newEntity.portList().iterator(); j.hasNext();) {
                    IOPort newPort = (IOPort) j.next();

                    if (oldPort.getName().equals(newPort.getName())) {
                        old2newMap.put(oldPort, newPort);
                        break;
                    }
                }
            }
        }

        // iterate over all relations in the graph and copy
        // accordingly
        for (Iterator i = oldTopEntity.relationList().iterator(); i.hasNext();) {
            ComponentRelation oldRelation = (ComponentRelation) i.next();
            ComponentRelation newRelation = newTopEntity.newRelation();
            old2newMap.put(oldRelation, newRelation);

            for (Iterator j = oldRelation.linkedPortList().iterator();
                        j.hasNext();) {
                IOPort oldPort = (IOPort) j.next();
                IOPort newPort = (IOPort) old2newMap.get(oldPort);
                newPort.link(newRelation);
            }
        }

        return newTopEntity;
    }

    public static Map copyPorts(ComponentEntity oldE, ComponentEntity newE)
        throws IllegalActionException, NameDuplicationException {
        Map map = new HashMap();

        for (Iterator i = oldE.portList().iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            JHDLIOPort newPort = (JHDLIOPort) newE.newPort(port.getName());
            newPort.setInput(port.isInput());
            newPort.setSignalWidth(32);
            map.put(port, newPort);

            if (port instanceof TypedIOPort) {
                Type portType = ((TypedIOPort) port).getType();

                if (portType instanceof BaseType.IntType) {
                    newPort.setSignalWidth(32);
                } else if (portType instanceof BaseType.BooleanType) {
                    newPort.setSignalWidth(1);
                }
            }
        }

        return map;
    }

    public static JHDLCompositeActor build(AtomicActor actor, Map options)
        throws IllegalActionException, NameDuplicationException {
        //JHDLCompositeActor newE = new JHDLCompositeActor();
        //newE.setName(actor.getName());
        //copyPorts(actor,newE);
        ActorPortDirectedGraph g = ModelGraph2Entity.createGraph(actor, options);

        //
        PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
        dgToDotty.writeDotFile(".", "actor", g);

        ModelGraph2Entity model = new ModelGraph2Entity(g, actor.getName());
        JHDLCompositeActor newActor = model.getEntity();
        return newActor;
    }
}

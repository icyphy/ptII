/* Schedule Generator example for Graph Chapter

 Copyright (c) 2000 The Regents of the University of California.
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

package doc.design.src.examples;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.graph.*;

import java.util.Iterator;



//////////////////////////////////////////////////////////////////////////
//// GraphGenerateSchedule
/**
Description of the class
@author Yuhong Xiong, Christopher Hylands
@version $Id$
*/
public class GraphGenerateSchedule {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Description
     */

    Object[] generateSchedule(CompositeActor composite) {
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();
        // Add all the actors contained in the composite to the graph.
        Iterator actors = composite.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            dag.add(actor);
        }

        // Add all the connection in the composite as graph edges.
        actors =  composite.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor lowerActor = (Actor)actors.next();

            // Find all the actors "higher" than the current one.
            Iterator outputPorts = lowerActor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort)outputPorts.next();
                Iterator inputPorts =
                    outputPort.deepConnectedInPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort)inputPorts.next();
                    Actor higherActor = (Actor)inputPort.getContainer();
                    if (dag.contains(higherActor)) {
                        dag.addEdge(lowerActor, higherActor);
                    }
                }
            }
        }
        return dag.topologicalSort();
    }
}


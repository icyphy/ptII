/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/* Demonstration of an attribute that uses Dijkstra's algorith to find
 * the shorted path between two actors in a model.
 */
package doc.tutorial.graph;

import java.awt.Frame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

/** This class is an attribute that you can insert in a model
 *  that will calculate the minimum number of hops (the minimum
 *  distance) between a given source actor and a given destination
 *  actor. When you double click on the attribute's icon, a
 *  dialog will pop up asking the user to specify the source
 *  and destination actor. Once these are specified, this class
 *  runs Dijkstra's algorithm and reports the minimum distance.
 *  <p>
 *  To place this attribute in model, in Vergil, select
 *  Graph-&gt;Instantiate Attribute and specify for the class
 *  name: doc.tutorial.graph.ShortestPathFinder. Alternatively,
 *  you can paste the following MoML code into the model:
 *  <pre>
 *  &lt;property name="ShortestPathFinder" class="doc.tutorial.graph.ShortestPathFinder"&gt;
 *    &lt;property name="_location" class="ptolemy.kernel.util.Location" value="[100, 100]"/&gt;
 *  &lt;/property&gt;
 *  </pre>
 *  In the above, the _location property is necessary to ensure
 *  that Vergil shows an icon for the HelloWorld attribute.
 *  It specifies the location on the canvas where that icon
 *  should be placed.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *
 */
public class ShortestPathFinder extends Attribute {

    /** Need this two-argument constructor to be able to instantiate
     *  the attribute in a model using MoML.
     *  @param container The containing model.
     *  @param name The name to give this attribute.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ShortestPathFinder(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create an EditorFactory to handle double clicks.
        new Calculate(this, "Calculate");
    }

    /** Return the minimum distance of the specified end node from the specified
     *  start node in the array of nodes, where each node is an Entity
     *  in a Ptolemy II model. The distance is the number of hops.
     *  @param entities The array of entities.
     *  @param start The index of the start entity.
     *  @param end The index of the end entity.
     *  @return The minimum distance from the start to the end.
     */
    public static int calculateDistance(Object[] entities, int start, int end) {
        // Array indicating which nodes have final distances.
        // This initializes to false for all nodes.
        boolean[] visited = new boolean[entities.length];

        // Array indicating the calculated distances from the start.
        int[] distance = new int[entities.length];
        // Use max value to represent infinity (no path).
        for (int i = 0; i < entities.length; i++) {
            distance[i] = Integer.MAX_VALUE;
        }
        distance[start] = 0;

        // Create a way to get the index of a node given
        // a reference to the node itself.
        Map<Object, Integer> entityToIndex = new HashMap<Object, Integer>();
        for (int i = 0; i < entities.length; i++) {
            entityToIndex.put(entities[i], new Integer(i));
        }

        // Set the start node as the current node and begin.
        Entity current = (Entity) entities[start];
        int currentIndex = start;
        boolean done = false;
        while (!done) {
            // For downstream actor, set its distance to the minimum
            // of the current estimate of its distance and one plus
            // the distance of the current actor from the start.
            int currentDistance = distance[currentIndex];
            List<IOPort> ports = current.portList();
            for (IOPort port : ports) {
                if (port.isOutput()) {
                    List<IOPort> remotePorts = port.connectedPortList();
                    for (IOPort remotePort : remotePorts) {
                        Entity neighbor = (Entity) remotePort.getContainer();
                        int neighborIndex = entityToIndex.get(neighbor)
                                .intValue();
                        if (visited[neighborIndex]) {
                            continue;
                        }
                        if (distance[neighborIndex] > currentDistance + 1) {
                            distance[neighborIndex] = currentDistance + 1;
                        }
                    }
                }
            }
            // Mark the current node visited.
            visited[currentIndex] = true;

            // We are done if the current node is the end node.
            if (currentIndex == end) {
                break;
            }

            // Find the unvisited node with the minimum distance to be the
            // next current node.
            int minimumDistance = Integer.MAX_VALUE;
            done = true;
            for (int i = 0; i < entities.length; i++) {
                if (visited[i]) {
                    continue;
                }
                done = false;
                if (distance[i] <= minimumDistance) {
                    minimumDistance = distance[i];
                    currentIndex = i;
                }
            }
            current = (Entity) entities[currentIndex];
        }
        return distance[end];
    }

    /** Inner class that is itself an Attribute which, when
     *  contained by a Ptolemy object, changes the default
     *  behavior of a double click on the attribute.
     *  A double click will cause the createEditor()
     *  method to be invoked.
     */
    public class Calculate extends EditorFactory {

        /** Construct a Calculate attribute.
         * @param container The container
         * @param name The name of the handler
         * @exception IllegalActionException If the factory is not of an
         *  acceptable attribute for the container.
         * @exception NameDuplicationException If the name coincides with
         *  an attribute already in the container.
         */
        public Calculate(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        @Override
        public void createEditor(NamedObj object, Frame parent) {
            // Get the entities of the model.
            List<Entity> entitiesList = ((CompositeEntity) ShortestPathFinder.this
                    .getContainer()).entityList();
            Object[] entities = entitiesList.toArray();

            // Ask the user for a start and end node.
            Query query = new Query();
            query.addChoice("start", "start node", entities, entities[0]);
            query.addChoice("end", "end node", entities,
                    entities[entities.length - 1]);
            ComponentDialog dialog = new ComponentDialog(parent,
                    "Specify start and end nodes", query);

            // If the user clicks OK, proceed.
            if (dialog.buttonPressed().equals("OK")) {

                // Indices of the start and end node given by the user.
                int start = query.getIntValue("start");
                int end = query.getIntValue("end");

                int distance = calculateDistance(entities, start, end);
                MessageHandler.message("The minimum distance is " + distance);
            }
        }
    }
}

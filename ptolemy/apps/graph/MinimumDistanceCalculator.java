package ptolemy.apps.graph;

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

public class MinimumDistanceCalculator extends Attribute {

    public MinimumDistanceCalculator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new DoubleClickHandler(this, "doubleClickHandler");
    }

    /** Given an array of entities and indexes of a start
     *  and end node in the array, find the minimum number
     *  of hops from the start to the end, where a "hop"
     *  is a connection from a port of an entity through
     *  any number of relations to a port of another entity.
     *  This method considers whether ports are inputs or
     *  outputs, and respects the direction of the connection.
     * 
     * @param entities
     * @param start
     * @param end
     * @return the number of hops from start to end.
     */
    public static int calculateDistance(Object[] entities, int start, int end) {
        boolean[] visited = new boolean[entities.length];
        int[] distance = new int[entities.length];
        for (int i = 0; i < entities.length; i++) {
            distance[i] = Integer.MAX_VALUE;
        }

        distance[start] = 0;

        // Create a way to get the index of a node given
        // a reference to the node itself.
        Map<Object, Integer> entityToIndex = new HashMap<Object, Integer>();
        for (int i = 0; i < entities.length; i++) {
            entityToIndex.put(entities[i], Integer.valueOf(i));
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

    public class DoubleClickHandler extends EditorFactory {

        public DoubleClickHandler(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void createEditor(NamedObj object, Frame parent) {
            // Get the entities of the model.
            List<Entity> entitiesList = ((CompositeEntity) MinimumDistanceCalculator.this
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

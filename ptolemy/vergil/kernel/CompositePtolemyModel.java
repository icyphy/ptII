/* A graph model for basic ptolemy models.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import diva.graph.modular.CompositeModel;

//////////////////////////////////////////////////////////////////////////
//// CompositePtolemyModel
/**
A diva node model for a Ptolemy II composite entity. Each element of
the graph model is represented by an instance of Locatable, which is
an attribute contained by a Ptolemy II object.  If a Ptolemy II object
is found that does not contain a Locatable, then one is created if needed.
The graph model consists of locations for various elements in the composite.
In particular, one location will be included for each contained entity, port,
director, and visible attribute.  In each case except visible attributes,
if there is no location, then a default location is created.
Visible attributes are included in the graph only if they already
contain a location. In addition, for any relation that links more
than two ports and does not contain a Vertex, this class will
create a Vertex.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.kernel.util.Location
 */
public class CompositePtolemyModel implements CompositeModel {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of nodes contained in the graph for the
     *  specified composite.  If the argument is not an instance of
     *  CompositeEntity, then return 0.
     *  @param composite A composite entity.
     *  @return The number of nodes in the graph representing the
     *   specified composite entity.
     */
    public int getNodeCount(Object composite) {
        if (!(composite instanceof NamedObj)) {
            return 0;
        }
        long version = ((NamedObj)composite).workspace().getVersion();
        if (_nodeList == null
                || composite != _composite
                || version != _version) {
            _nodeList = _nodeList((NamedObj)composite);
            _composite = composite;
            _version = version;
        }
        return _nodeList.size();
    }

    /** Return an iterator over all the nodes contained in the graph
     *  for the specified composite. If the argument is not an
     *  instance of CompositeEntity, then return an empty iterator.
     *  @param composite A composite entity.
     *  @return An iterator over nodes in the graph representing the
     *   specified composite entity.
     */
    public Iterator nodes(Object composite) {
        if (!(composite instanceof NamedObj)) {
            return (new LinkedList()).iterator();
        }
        long version = ((NamedObj)composite).workspace().getVersion();
        if (_nodeList == null
                || composite != _composite
                || version != _version) {
            _nodeList = _nodeList((NamedObj)composite);
            _composite = composite;
            _version = version;
        }
        return _nodeList.iterator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the location attribute contained in the given object, or
     *  a new location contained in the given object if there was no location.
     *  @param object The object for which a location is needed.
     *  @return The location of the object, or a new location if none.
     */
    protected Locatable _getLocation(NamedObj object) {
        List locations = object.attributeList(Locatable.class);
        if (locations.size() > 0) {
            return (Locatable)locations.get(0);
        } else {
            try {
                // NOTE: We need the location right away, so we go ahead
                // and create it. However, we also issue a MoMLChangeRequest
                // so that the change propagates, and any models that defer
                // to this one (e.g. subclasses) also have locations.
                // This is necessary so that if the location later moves,
                // then the move can be duplicated in the deferrers.
                Locatable location = new Location(object, "_location");
                
                // Since this isn't delegated to the MoML parser,
                // we have to handle propagation here.
                List heritageList = object.getHeritageList();
                Iterator heritage = heritageList.iterator();
                while (heritage.hasNext()) {
                    NamedObj inherited = (NamedObj)heritage.next();
                    new Location(inherited, "_location");
                }
                
                return location;
            } catch (Exception e) {
                throw new InternalErrorException("Failed to create " +
                        "location, even though one does not exist:" +
                        e.getMessage());
            }
        }
    }

    /** Return a list of all the nodes in the graph corresponding to
     *  the specified Ptolemy II model.  The model can be any NamedObj,
     *  and the returned list will include any entities, ports,
     *  relations, and attributes that it contains, in that order.
     *  Note that this method creates a new list, and should therefore
     *  only be called if the object has changed.
     *  @param composite The composite entity.
     *  @return A list of the nodes in the graph.
     */
    protected List _nodeList(NamedObj composite) {
        List nodes = new LinkedList();

        if (composite instanceof CompositeEntity) {
            // Add a graph node for every class definition.
            // The node is actually the location contained by the entity.
            // If the entity does not contain a location, then create one.
            Iterator classes = ((CompositeEntity)composite)
                    .classDefinitionList().iterator();
            while (classes.hasNext()) {
                ComponentEntity entity = (ComponentEntity)classes.next();
                nodes.add(_getLocation(entity));
            }
            // Add a graph node for every entity.
            // The node is actually the location contained by the entity.
            // If the entity does not contain a location, then create one.
            Iterator entities = ((CompositeEntity)composite)
                    .entityList().iterator();
            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity)entities.next();
                nodes.add(_getLocation(entity));
            }
        }

        if (composite instanceof Entity) {
            // Add a graph node for every external port.
            // The node is actually the location contained by the port.
            // If the port does not contain a location, then create one.
            Iterator ports = ((Entity)composite).portList().iterator();
            while (ports.hasNext()) {
                ComponentPort port = (ComponentPort)ports.next();
                nodes.add(_getLocation(port));
            }
        }

        if (composite instanceof CompositeEntity) {
            // Add a node for every relation that has a vertex and
            // doesn't connect exactly two ports.
            // NOTE: This particular part of the graph model is irrelevant
            // for FSMs, but it is harmless to include it, so there is no
            // real need to subclass this to remove it.
            Iterator relations = ((CompositeEntity)composite)
                    .relationList().iterator();
            while (relations.hasNext()) {
                ComponentRelation relation = (ComponentRelation)relations.next();
                List vertexList = relation.attributeList(Vertex.class);

                if (vertexList.size() != 0) {
                    // Add in all the vertexes.
                    Iterator vertexes = vertexList.iterator();
                    while (vertexes.hasNext()) {
                        Vertex v = (Vertex)vertexes.next();
                        nodes.add(v);
                    }
                } else {
                    // See if we need to create a vertex.
                    // Count the linked ports.
                    int count = relation.linkedPortList().size();
                    if (count != 2) {
                        // A vertex is needed, so create one.
                        try {
                            Vertex vertex = new Vertex(relation,
                                    relation.uniqueName("vertex"));
                            nodes.add(vertex);
                        } catch (Exception e) {
                            throw new InternalErrorException(
                                    "Failed to create a vertex! " +
                                    e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Add a node for every director or visible attribute.
        // The node is again the location.
        // For directors, if there is no location, then create one.
        // For visible attributes, add them only if they already
        // create a location.
        Iterator attributes = composite.attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();

            if (attribute instanceof Director) {
                nodes.add(_getLocation(attribute));
            } else {
                // The object is not a director, so only give a location
                // if one exists already.
                List locations = attribute.attributeList(Locatable.class);
                if (locations.size() > 0) {
                    nodes.add(locations.get(0));
                }
            }
        }

        // Return the final result.
        return nodes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The most recent object whose model was accessed.
    private Object _composite;

    // The workspace version for that object.
    private long _version;

    // The cached list;
    private List _nodeList;
}

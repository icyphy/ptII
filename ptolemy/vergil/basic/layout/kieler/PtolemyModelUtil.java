/* Static methods for manipulating Ptolemy models.
@Copyright (c) 2009-2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.vergil.basic.layout.kieler;

import ptolemy.kernel.util.NamedObj;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.Link;

//////////////////////////////////////////////////////////////////////////
////PtolemyModelUtil
/**
 * Class containing methods for manipulating Ptolemy models for the purpose of layout a graphical
 * Ptolemy diagram. Methods for positioning Actors and creating vertices are available. The changes
 * are performed by MoMLChangeRequests where as long as possible those requests get buffered in
 * order to perform multiple changes at once for performance.
 * 
 * @author Hauke Fuhrmann, <haf@informatik.uni-kiel.de>, Christian Motika
 *         <cmot@informatik.uni-kiel.de>
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtolemyModelUtil {

    /**
     * Construct an instance and initialize the internal request buffer.
     */
    public PtolemyModelUtil() {
        _momlChangeRequest = new StringBuffer();
        _uniqueCounter = 0;
        _nameSet = new HashSet();
    }

    /**
     * Create a new MoMLChangeRequest to add a new Relation. The MoML code is appended to the field
     * MoMLChangeRequest buffer to buffer multiple such requests. Don't actually execute the
     * request.
     * 
     * To flush the request, the method {@link #_performChangeRequest(CompositeActor)} must be
     * called.
     * 
     * @param relationName
     *            Name of the new relation which needs to be unique
     * @return name of newly created relation
     */
    protected String _createRelation(String relationName) {
        String moml = "<relation name=\"" + relationName
                + "\" class=\"ptolemy.actor.TypedIORelation\">" + "</relation>\n";

        _momlChangeRequest.append(moml);
        return relationName;
    }

    public static String getLinkId(Link link) {
        Object object1 = link.getTail();
        Object object2 = link.getHead();
        String obj1Id = object1.hashCode() + "";
        String obj2Id = object2.hashCode() + "";
        if (object1 instanceof NamedObj) {
            obj1Id = ((NamedObj) object1).getName();
        }
        if (object2 instanceof NamedObj) {
            obj2Id = ((NamedObj) object2).getName();
        }
        return (obj1Id + obj2Id);
    }

    public static String getModificationMarker(Link link) {
        NamedObj source = null;
        NamedObj target = null;
        Object objectSource = link.getTail();
        Object objectTarget = link.getHead();
        String sourceTargetLocation = "";
        if (objectSource instanceof Port) {
            source = ((Port) objectSource).getContainer();
        }
        if (objectTarget instanceof Port) {
            target = ((Port) objectTarget).getContainer();
        }
        if (source != null) {
            sourceTargetLocation += source.getAttribute("_location").toString();
        }
        if (target != null) {
            sourceTargetLocation += target.getAttribute("_location").toString();
        }
        if (objectSource instanceof Vertex) {
            sourceTargetLocation += ((Vertex) objectSource).getValueAsString();
        }
        if (objectTarget instanceof Vertex) {
            sourceTargetLocation += ((Vertex) objectTarget).getValueAsString();
        }
        return "" + sourceTargetLocation.hashCode();
    }

    /**
     * Create a new MoMLChangeRequest to add a new StringAttribute with a value. The MoML code is
     * appended to the field MoMLChangeRequest buffer to buffer multiple such requests. Don't
     * actually execute the request.
     * 
     * To flush the request, the method {@link #_performChangeRequest(CompositeActor)} must be
     * called.
     * 
     * @param name
     *            Name of the new StringAttribute which needs to be unique
     * @param value
     *            Value of the StringAttribute
     * @return name of newly created StringAttribute
     */
    public String _layoutHints(String relationName, Link link, String hints) {
        String modificationMarker = PtolemyModelUtil.getModificationMarker(link);
        // calculate marker
        String propertyString1 = "<property name=\"_layoutHints:"
                + PtolemyModelUtil.getLinkId(link)
                + "\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"" + hints + "\"/>";
        String propertyString2 = "<property name=\"_modificationMarker:"
                + PtolemyModelUtil.getLinkId(link)
                + "\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"" + modificationMarker
                + "\"/>";
        // delete hints if there are no hints
        if (hints == null) {
            propertyString1 = "<deleteProperty name=\"_layoutHints:"
                    + PtolemyModelUtil.getLinkId(link) + "\"/>";
            propertyString2 = "<deleteProperty name=\"_modificationMarker:"
                    + PtolemyModelUtil.getLinkId(link) + "\"/>";
        }
        String moml = "<relation name=\"" + relationName + "\" >" + propertyString1
                + propertyString2 + "</relation>\n";
        if (hints == null || !hints.equals("")) {
            _momlChangeRequest.append(moml);
        }
        return relationName;
    }

    /**
     * Create a new MoMLChangeRequest to add a new Relation with a Vertex at a given position. The
     * MoML code is appended to the field MoMLChangeRequest buffer to buffer multiple such requests.
     * Don't actually execute the request.
     * 
     * To flush the request, the method {@link #_performChangeRequest(CompositeActor)} must be
     * called.
     * 
     * @param relationName
     *            Name of the new relation which needs to be unique
     * @param x
     *            coordinate of new vertex
     * @param y
     *            coordinate of new vertex
     * @return name of newly created relation
     */
    protected String _createRelationWithVertex(String relationName, double x, double y) {

        String moml = "<relation name=\"" + relationName
                + "\" class=\"ptolemy.actor.TypedIORelation\">" + "<vertex name=\"" + "vertex"
                + "\" value=\"{" + x + " , " + y + "}\"></vertex></relation>\n";

        _momlChangeRequest.append(moml);
        return relationName;
    }

    /**
     * Hide a specific relation vertex. Set or unset the _hide attribute of a relation vertex.
     * 
     * @param relationName
     *            Name of the relation that contains the vertex.
     * @param vertexName
     *            Name of the vertex.
     * @param hide
     *            True iff the hide attribute should be set. Otherwise if it should be unset.
     */
    protected void _hideVertex(String relationName, String vertexName, boolean hide) {

        String propertyString = "<property name=\"_hide\" class=\"ptolemy.data.expr.Parameter\" value=\""
                + hide + "\"/>";
        if (!hide) {
            propertyString = "<deleteProperty name=\"_hide\"/>";
        }
        String moml = "<relation name=\"" + relationName + "\" >" +

        "<vertex name=\"" + vertexName + "\">" + propertyString + "</vertex></relation>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Create a new MoMLChangeRequest to add a new link between arbitrary objects. The MoML code is
     * appended to the field MoMLChangeRequest buffer to buffer multiple such requests. Don't
     * actually execute the request. Supported types are given by MoML, e.g. "port", "relation".
     * Connecting multiple relations requires to add a number, "relation1", "relation2" to the
     * corresponding type.
     * 
     * To flush the request, the method {@link #_performChangeRequest(CompositeActor)} must be
     * called.
     * 
     * @param type1
     *            type of the first item to be linked, e.g. port, relation, relation1, relation2
     * @param name1
     *            name of the first item to be linked
     * @param type2
     *            type of the second item to be linked, e.g. port, relation, relation1, relation2
     * @param name2
     *            name of the second item to be linked
     */
    protected void _link(String type1, String name1, String type2, String name2) {
        String moml = "<link " + type1 + "=\"" + name1 + "\" " + type2 + "=\"" + name2 + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Link a port with something else (port or relation) at a specific index.
     * 
     * @param portName
     *            Name of the port to be linked.
     * @param type2
     *            Type of the second object, i.e. port or relation.
     * @param name2
     *            Name of the second object to be linked.
     * @param index
     *            Index of the relation in the channel list of the first port.
     */
    protected void _linkPort(String portName, String type2, String name2, int index) {
        String moml = "<link " + "port" + "=\"" + portName + "\" " + type2 + "=\"" + name2
                + "\" insertAt=\"" + index + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Link a port with something else (port or relation) at a specific inside index.
     * 
     * @param portName
     *            Name of the port to be linked.
     * @param type2
     *            Type of the second object, i.e. port or relation.
     * @param name2
     *            Name of the second object to be linked.
     * @param insideIndex
     *            Index of the relation in the channel list of the first port.
     */
    protected void _linkPortInside(String portName, String type2, String name2, int insideIndex) {
        String moml = "<link " + "port" + "=\"" + portName + "\" " + type2 + "=\"" + name2
                + "\" insertInsideAt=\"" + insideIndex + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Remove a single relation.
     * 
     * @param relation
     *            Relation to be removed.
     * @param actor
     *            Parent actor of the relation.
     */
    protected void _removeRelation(Relation relation, CompositeActor actor) {
        StringBuffer moml = new StringBuffer();
        moml.append("<deleteRelation name=\"" + relation.getName() + "\"/>\n");
        _momlChangeRequest.append(moml);
    }

    /**
     * Create a MoMLChangeRequest to remove a set of relations in a Ptolemy model object.
     * 
     * @param relationSet
     *            Set of relation to be removed from the Ptolemy model
     */
    protected void _removeRelations(Set<Relation> relationSet) {
        StringBuffer moml = new StringBuffer();
        for (Relation relation : relationSet) {
            // Delete the relation.
            moml.append("<deleteRelation name=\"" + relation.getName() + "\"/>\n");
        }
        _momlChangeRequest.append(moml);
    }

    /**
     * Remove all unnecessary relations within a composite actor. Unnecessary means that a relation
     * is connected only with 0, 1 or 2 objects. In such case a relation can be either simply
     * removed or replaced by a direct link between the objects. Iterate all relations in the parent
     * actor and for all unnecessary relations with vertices, remove them and if required
     * reestablish the links such that the semantics keeps the same.
     * 
     * @param parent
     *            The composite actor in which to look for unnecessary relations.
     */
    public static void _removeUnnecessaryRelations(CompositeActor parent) {
        PtolemyModelUtil util = new PtolemyModelUtil();
        for (Iterator containedIterator = parent.containedObjectsIterator(); containedIterator
                .hasNext();) {
            Object containedElement = containedIterator.next();
            if (containedElement instanceof Relation) {
                Relation relation = (Relation) containedElement;
                List linkedObjects = relation.linkedObjectsList();

                if (linkedObjects.size() == 0 || linkedObjects.size() == 1) {
                    util._removeRelation(relation, parent);
                    util._performChangeRequest(parent);
                } else {
                    List<Vertex> vertices = relation.attributeList(ptolemy.moml.Vertex.class);
                    if (!vertices.isEmpty()) {
                        // ok, now we found a relation with a relation vertex...
                        if (linkedObjects.size() == 2) {
                            // here we can remove this relation!
                            Object o1 = linkedObjects.get(0);
                            Object o2 = linkedObjects.get(1);
                            if (o1 instanceof Port && o2 instanceof Port) {
                                util._removeRelationVertex(relation);
                            } else if (o1 instanceof Relation && o2 instanceof Relation) {
                                util._link("relation1", ((Relation) o1).getName(), "relation2",
                                        ((Relation) o2).getName());
                                util._removeRelation(relation, parent);
                                util._performChangeRequest(parent);
                            } else { // now we have one port and one relation
                                Port connectedPort = null;
                                Relation connectedRelation = null;
                                if (o1 instanceof Port && o2 instanceof Relation) {
                                    connectedPort = (Port) o1;
                                    connectedRelation = (Relation) o2;
                                } else if (o2 instanceof Port && o1 instanceof Relation) {
                                    connectedPort = (Port) o2;
                                    connectedRelation = (Relation) o1;
                                }
                                if (connectedPort != null && connectedRelation != null) {
                                    // make sure to keep the right channel for
                                    // the new relation
                                    int index = connectedPort.linkedRelationList()
                                            .indexOf(relation);
                                    if (index >= 0) {
                                        util._removeRelation(relation, parent);
                                        util._performChangeRequest(parent);
                                        util._linkPort(connectedPort.getName(parent), "relation",
                                                connectedRelation.getName(), index);
                                        util._performChangeRequest(parent);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        util._performChangeRequest(parent);
    }

    /**
     * Remove a vertex from a relation.
     * 
     * @param relation
     *            The relation to remove the vertex from.
     */
    protected void _removeRelationVertex(Relation relation) {
        List<Vertex> vertices = relation.attributeList(ptolemy.moml.Vertex.class);
        if (vertices != null && vertices.size() > 0) {
            Vertex vertex = vertices.get(0);
            String moml = "<relation name=\"" + relation.getName()
                    + "\" class=\"ptolemy.actor.TypedIORelation\">" + "<deleteProperty name=\""
                    + vertex.getName() + "\"/>" + "</relation>\n";
            _momlChangeRequest.append(moml);
        }
    }

    /**
     * Get the location given by the location attribute of the given input object. If the Ptolemy
     * object has no location attribute, return double zero.
     * 
     * @param namedObj
     *            The Ptolemy object for which the location should be retrieved.
     * @return A double array containing two double values corresponding to the location (x and y)
     *         of the object. Will return double zero if no location attribute is set for the
     *         object.
     */
    protected static double[] _getLocation(NamedObj namedObj) {
        Attribute locationAttribute = namedObj.getAttribute("_location");
        double[] location = { 0, 0 };
        if (locationAttribute != null && locationAttribute instanceof Location) {
            location = ((Location) locationAttribute).getLocation();
        }
        return location;
    }

    /**
     * For a set of relations get a set of relation groups, i.e. for each relation construct a list
     * of relations that are all interconnected, either directly or indirectly.
     * 
     * @param relations
     *            Set of relations
     * @return a Set of relation groups as given by List<Relation> objects by Ptolemy
     */
    protected static Set<List<Relation>> _getRelationGroups(Set<Relation> relations) {
        Set<List<Relation>> relationGroups = new HashSet<List<Relation>>();
        for (Relation relation : relations) {
            List<Relation> relationGroup = relation.relationGroupList();
            // check if we already have this relation group
            // TODO: verify whether relation groups are unique. Then you could
            // perform this check much more efficiently
            boolean found = false;
            for (List<Relation> listedRelationGroup : relationGroups) {
                if (listedRelationGroup.containsAll(relationGroup)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                relationGroups.add(relationGroup);
            }
        }
        return relationGroups;
    }

    /**
     * Get a unique number. Subsequent calls to this method will return an increasing sequence of
     * numbers. This can be used to suffix a NamedObj where the official getUniqueName() methods of
     * the parent composite actors cannot be used (e.g. if to create multiple new objects in just
     * one MoMLChangeRequest.
     * 
     * @return An integer where every following call will give a different one.
     */
    protected int _getUniqueNumber() {
        _uniqueCounter++;
        return _uniqueCounter;
    }

    /**
     * Get a unique String in the namespace of the given composite actor with the prefix that is
     * also given. The idea is the same than the {@link CompositeEntity#uniqueName(String)} method
     * of any CompositeEntity. However, build a local cache of used names and add the names that get
     * created by subsequent calls. I.e. multiple subsequent calls won't return the same name even
     * when the name of a former call is not used yet in the composite actor.
     * <p>
     * The rationale is that for MoMLChangeRequests it is desireable to collect multiple changes
     * (e.g. multiple object creations that all require a unique name).
     * 
     * @param actor
     *            CompositeActor in which to search for the names.
     * @param prefix
     *            Given prefix that shall be suffixed to get a unique name.
     * @return A unique name in the composite actor namespace.
     */
    protected String _getUniqueString(CompositeActor actor, String prefix) {
        // build name cache for the first time
        if (_nameSet.isEmpty()) {
            for (Object attribute : actor.attributeList()) {
                _nameSet.add(((Attribute) attribute).getName());
            }
            for (Iterator iter = actor.containedObjectsIterator(); iter.hasNext();) {
                Object containedObject = iter.next();
                if (containedObject instanceof NamedObj) {
                    _nameSet.add(((NamedObj) containedObject).getName());
                }
            }
        }
        int counter = 2;
        String candidate = prefix;
        while (_nameSet.contains(candidate)) {
            candidate = _stripNumericSuffix(candidate);
            candidate += counter;
            counter++;
        }
        _nameSet.add(candidate);
        return candidate;
    }

    /**
     * Check whether the given Ptolemy model object has any connections, i.e. is connected to any
     * other components via some link.
     * 
     * @param namedObj
     *            The Ptolemy model object which is to be analyzed
     * @return True if the object is an Actor and any port has any relations or is connected to any
     *         other port; true if the object is a Relation; false if the object is an Attribute.
     *         Defaults to false.
     */
    protected static boolean _isConnected(NamedObj namedObj) {
        if (namedObj instanceof Attribute) {
            return false;
        }
        if (namedObj instanceof Actor) {
            Actor actor = (Actor) namedObj;
            List<Port> ports = new ArrayList<Port>();
            ports.addAll(actor.inputPortList());
            ports.addAll(actor.outputPortList());
            for (Port port : ports) {
                // if any port of an actor is conencted to any other
                // assume that there is also no visible connection
                if (!port.connectedPortList().isEmpty() || !port.linkedRelationList().isEmpty()) {
                    return true;
                }

            }
            return false;
        }
        if (namedObj instanceof Relation) {
            return true;
        }
        if (namedObj instanceof Port) {
            // assume all inner ports are connected
            return true;
        }
        // default to false
        return false;
    }

    /**
     * Determine whether a given Port is an input port.
     * 
     * @param port
     *            The port to be analyzed
     * @return True if the port is an input port
     */
    protected static boolean _isInput(Port port) {
        if (port instanceof IOPort) {
            return ((IOPort) port).isInput();
        }
        NamedObj obj = port.getContainer();
        if (obj instanceof Actor) {
            Actor actor = (Actor) obj;
            if (actor.inputPortList().contains(port)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Flush all buffered change requests to the given Actor. Reset the buffer afterwards.
     * 
     * @param actor
     *            The target of the change request, e.g. the composite actor containing the objects
     *            for which changes are requested.
     */
    public void _performChangeRequest(CompositeActor actor) {
        if (_momlChangeRequest.toString().trim().equals("")) {
            // if request is empty, don't do anything.
            return;
        }
        _momlChangeRequest.insert(0, "<group>");
        _momlChangeRequest.append("</group>");
        // System.out.println(_momlChangeRequest);
        MoMLChangeRequest request = new MoMLChangeRequest(this, actor,
                _momlChangeRequest.toString());
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        actor.requestChange(request);
        // reset the current request
        _momlChangeRequest = new StringBuffer();
        _anyRequestsSoFar = true;
    }

    /**
     * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it immediately. The
     * request is addressed to a specific NamedObj in the Ptolemy model and hence does not get
     * buffered because there is only exactly one move request per layout run per node.
     * 
     * @param obj
     *            Ptolemy node to be moved
     * @param x
     *            new coordinate
     * @param y
     *            new coordinate
     */
    protected void _setLocation(NamedObj obj, double x, double y) {
        String moml = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{"
                + x + "," + y + "}\"></property>\n";
        // need to request a MoML Change for a particular NamedObj and not the
        // top level element
        // so we need multiple requests here
        MoMLChangeRequest request = new MoMLChangeRequest(obj, obj, moml);
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        obj.requestChange(request);
        _anyRequestsSoFar = true;
    }

    /**
     * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it immediately. The
     * request is addressed to a specific NamedObj in the Ptolemy model and hence does not get
     * buffered because there is only exactly one move request per layout run per node.
     * 
     * @param vertex
     *            Ptolemy node to be moved
     * @param relation
     *            Ptolemy Relation to be moved
     * @param x
     *            new coordinate
     * @param y
     *            new coordinate
     */
    protected void _setLocation(Vertex vertex, Relation relation, double x, double y) {
        String moml = "<vertex name=\"" + vertex.getName() + "\" value=\"[" + x + "," + y
                + "]\"></vertex>\n";
        // need to request a MoML Change for a particular Vertex
        // and not the
        // top level element
        // so we need multiple requests here
        MoMLChangeRequest request = new MoMLChangeRequest(vertex, relation, moml);
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        relation.requestChange(request);
        _anyRequestsSoFar = true;
    }

    /**
     * Show or hide unnecessary relation vertices. Iterate all relations in the parent composite
     * actor and find unnecessary relations. I.e. relations that are connected to exactly 2 other
     * object. These relation vertices have no semantic impact and are likely only added to
     * manipulate the routing of the corresponding edges. Either hide or show those relation
     * vertices. Relations with connection degree 0 or 1 do not get hidden, because they are not
     * only inserted for layout. Usually those are completely unnecessary and should be removed, so
     * the user should be able to see them.
     * 
     * @param parent
     *            Composite actor that should be searched for unnecessary vertices.
     * @param show
     *            True iff the vertices should be shown, false if hidden.
     */
    protected static void _showUnnecessaryRelations(CompositeActor parent, boolean show) {
        PtolemyModelUtil util = new PtolemyModelUtil();
        for (Iterator containedIterator = parent.containedObjectsIterator(); containedIterator
                .hasNext();) {
            Object containedElement = containedIterator.next();
            if (containedElement instanceof Relation) {
                Relation relation = (Relation) containedElement;
                List<Vertex> vertices = relation.attributeList(ptolemy.moml.Vertex.class);
                for (Vertex vertex : vertices) {
                    // ok, now we found a relation with a relation vertex...
                    List linkedObjects = relation.linkedObjectsList();
                    if (linkedObjects.size() <= 2) {
                        // here we can hide this relation!
                        try {
                            // Parameter hide = new Parameter(vertex, "_hide");
                            if (show) {
                                if (vertex.getAttribute("_hide") != null) {
                                    util._hideVertex(relation.getName(), vertex.getName(), false);
                                } else {/* nothing */
                                }
                            } else if (linkedObjects.size() == 2) { // only hide if exactly 2 linked
                                                                    // objects
                                util._hideVertex(relation.getName(), vertex.getName(), true);
                            }
                        } catch (Exception e) {
                            /* do nothing if there is already such attribute */
                        }
                    }
                }

            }
        }
        util._performChangeRequest(parent);
    }

    /**
     * Toggle show or hide status of unnecessary relation vertices. Iterate all relations in the
     * parent composite actor and find unnecessary relations. I.e. relations that are connected to
     * exactly 2 other object. These relation vertices have no semantic impact and are likely only
     * added to manipulate the routing of the corresponding edges. Either hide or show those
     * relation vertices. Relations with connection degree 0 or 1 do not get hidden, because they
     * are not only inserted for layout. Usually those are completely unnecessary and should be
     * removed, so the user should be able to see them.
     * 
     * @param parent
     *            Composite actor that should be searched for unnecessary vertices.
     */
    public static void _showUnnecessaryRelationsToggle(CompositeActor parent) {
        _showUnnecessaryRelations(parent, !_hide);
        _hide = !_hide;
    }

    /**
     * Return a string that is identical to the specified string except any trailing digits are
     * removed.
     * 
     * @param string
     *            The string to strip of its numeric suffix.
     * @return A string with no numeric suffix.
     */
    protected static String _stripNumericSuffix(String string) {
        int length = string.length();
        char[] chars = string.toCharArray();

        for (int i = length - 1; i >= 0; i--) {
            char current = chars[i];

            if (Character.isDigit(current)) {
                length--;
            } else {
                // if (current == '_') {
                // length--;
                // }
                // Found a non-numeric, so we are done.
                break;
            }
        }

        if (length < string.length()) {
            // Some stripping occurred.
            char[] result = new char[length];
            System.arraycopy(chars, 0, result, 0, length);
            return new String(result);
        }
        return string;
    }

    /**
     * Unlink a port at the given channel index.
     * 
     * @param portName
     *            Name of the Port.
     * @param index
     *            Index of the channel to be unlinked.
     */
    protected void _unlinkPort(String portName, int index) {
        String moml = "<unlink " + "port" + "=\"" + portName + "\" index=\"" + index + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Unlink a port at the given channel inside index.
     * 
     * @param portName
     *            Name of the Port.
     * @param insideIndex
     *            Index of the channel to be unlinked.
     */
    protected void _unlinkPortInside(String portName, int insideIndex) {
        String moml = "<unlink " + "port" + "=\"" + portName + "\" insideIndex=\"" + insideIndex
                + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Unlink two relations.
     * 
     * @param relation1
     *            Name of first relation to unlink.
     * @param relation2
     *            Name of second relation to unlink.
     */
    protected void _unlinkRelations(String relation1, String relation2) {
        String moml = "<unlink " + "relation1" + "=\"" + relation1 + "\" relation2=\"" + relation2
                + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Flag indicating whether there have been any MoMLChangeRequests processed so far or not. This
     * is required for proper undo, because if there were some requests we also want to merge our
     * undo to them.
     */
    private boolean _anyRequestsSoFar = false;

    /**
     * StringBuffer for Requests of Model changes. In Ptolemy the main infrastructure to do model
     * changes is through XML change requests of the XML representation. This field is used to
     * collect all changes in one String and then carry them out in only one operation whereas
     * possible.
     */
    private StringBuffer _momlChangeRequest;

    /**
     * Toggle variable to set the hidden status of unnecessary relation vertices.
     */
    private static boolean _hide = true;

    /**
     * Local cache of used names.
     */
    private Set<String> _nameSet;

    /**
     * A unique number that is used to determine unique String names for relations.
     */
    private static int _uniqueCounter = 0;

}

/*
@Copyright (c) 2009 The Regents of the University of California.
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
/* Static methods for manipulating Ptolemy models.
 * 
 */
package ptolemy.vergil.basic.layout.kieler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;

//////////////////////////////////////////////////////////////////////////
////PtolemyModelUtil
/**
 * Class containing methods for manipulating Ptolemy models for the purpose of
 * layout a graphical Ptolemy diagram. Methods for positioning Actors and
 * creating vertices are available. The changes are performed by
 * MoMLChangeRequests where as long as possible those requests get buffered in
 * order to perform multiple changes at once for performance.
 * 
 * @author Hauke Fuhrmann, <haf@informatik.uni-kiel.de>
 */

public class PtolemyModelUtil {

    /**
     * Construct an instance and initialize the internal request buffer.
     */
    public PtolemyModelUtil() {
        _momlChangeRequest = new StringBuffer();
        _nameSuffix = 0;
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Create a new MoMLChangeRequest to add a new Relation. The MoML code is
     * appended to the field MoMLChangeRequest buffer to buffer multiple such
     * requests. Don't actually execute the request.
     * 
     * To flush the request, the method
     * {@link #_performChangeRequest(CompositeActor)} must be called.
     * 
     * @param relationName
     *            Name of the new relation which needs to be unique
     * @return name of newly created relation
     */
    protected String _createRelation(String relationName) {
        String moml = "<relation name=\"" + relationName
                + "\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>\n";

        _momlChangeRequest.append(moml);
        return relationName;
    }

    /**
     * Create a new MoMLChangeRequest to add a new Relation with a Vertex at a
     * given position. The MoML code is appended to the field MoMLChangeRequest
     * buffer to buffer multiple such requests. Don't actually execute the
     * request.
     * 
     *  * To flush the request, the method
     * {@link #_performChangeRequest(CompositeActor)} must be called.
     * 
     * @param relationName
     *            Name of the new relation which needs to be unique
     * @param x
     *            coordinate of new vertex
     * @param y
     *            coordinate of new vertex
     * @return name of newly created relation
     */
    protected String _createRelationWithVertex(String relationName, double x,
            double y) {
        relationName = _getUniqueName(relationName);
        String vertexName = _getUniqueName(relationName + "V");

        String moml = "<relation name=\"" + relationName
                + "\" class=\"ptolemy.actor.TypedIORelation\">"
                + "<vertex name=\"" + vertexName + "\" value=\"{" + x + " , "
                + y + "}\"></vertex></relation>\n";

        _momlChangeRequest.append(moml);
        return relationName;
    }

    /**
     * Create a new MoMLChangeRequest to add a new link between arbitrary
     * objects. The MoML code is appended to the field MoMLChangeRequest buffer
     * to buffer multiple such requests. Don't actually execute the request.
     * Supported types are given by MoML, e.g. "port", "relation". Connecting
     * multiple relations requires to add a number, "relation1", "relation2" to
     * the corresponding type.
     * 
     * To flush the request, the method
     * {@link #_performChangeRequest(CompositeActor)} must be called.
     * 
     * @param type1
     *            type of the first item to be linked, e.g. port, relation,
     *            relation1, relation2
     * @param name1
     *            name of the first item to be linked
     * @param type2
     *            type of the second item to be linked, e.g. port, relation,
     *            relation1, relation2
     * @param name2
     *            name of the second item to be linked
     */
    protected void _link(String type1, String name1, String type2, String name2) {
        String moml = "<link " + type1 + "=\"" + name1 + "\" " + type2 + "=\""
                + name2 + "\"/>\n";
        _momlChangeRequest.append(moml);
    }

    /**
     * Create a MoMLChangeRequest to remove a set of relations in a Ptolemy
     * model object and schedule it immediately. As mixing creation of new
     * relations and removing of old relation requests in the request buffer
     * might cause name clashes, this request is not buffered but executed 
     * immediately.
     * 
     * @param relationSet
     *            set of relation to be removed from the Ptolemy model
     */
    protected void _removeRelations(Set<Relation> relationSet,
            CompositeActor actor) {
        StringBuffer moml = new StringBuffer();
        for (Relation relation : relationSet) {
            // Delete the relation.
            moml.append("<deleteRelation name=\"" + relation.getName()
                    + "\"/>\n");
        }
        moml.insert(0, "<group>");
        moml.append("</group>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, actor, moml
                .toString());
        request.setUndoable(true);
        actor.requestChange(request);
    }

    /**
     * Get the location given by the location attribute of the 
     * given input object. If the Ptolemy object has no location
     * attribute, return double zero.
     * @param namedObj
     *          The Ptolemy object for which the location should be retrieved.
     * @return
     *          A double array containing two double values corresponding to the
     *          location (x and y) of the object. Will return double zero if no
     *          location attribute is set for the object. 
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
     * For a set of relations get a set of relation groups, i.e. for each
     * relation construct a list of relations that are all interconnected,
     * either directly or indirectly.
     * 
     * @param relations
     *            Set of relations
     * @return a Set of relation groups as given by List<Relation> objects by
     *         Ptolemy
     */
    protected static Set<List<Relation>> _getRelationGroups(
            Set<Relation> relations) {
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
     * Check whether the given Ptolemy model object has any connections, i.e. is
     * connected to any other components via some link.
     * 
     * @param namedObj
     *          The Ptolemy model object which is to be analyzed
     * @return
     *          True if the object is an Actor and any port has any
     *          relations or is connected to any other port; true
     *          if the object is a Relation; false if the object is an
     *          Attribute. Defaults to false.
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
                if (!port.connectedPortList().isEmpty()
                        || !port.linkedRelationList().isEmpty()) {
                    return true;
                }

            }
            return false;
        }
        if (namedObj instanceof Relation) {
            return true;
        }
        // default to false
        return false;
    }

    /**
     * Determine whether a given Port is an input port.
     * @param port
     *          The port to be analyzed
     * @return
     *          True if the port is an input port
     */
    protected static boolean _isInput(Port port) {
        // FIXME: this does not work. A port has no input attribute
        Attribute inputAttribute = port.getAttribute("input");
        if (inputAttribute != null) {
            return true;
        }
        return true;
    }

    /**
     * Flush all buffered change requests to the given Actor. Reset
     * the buffer afterwards. 
     * @param actor
     *          The target of the change request, e.g. the composite
     *          actor containing the objects for which changes
     *          are requested.
     */
    protected void _performChangeRequest(CompositeActor actor) {
        _momlChangeRequest.insert(0, "<group>");
        _momlChangeRequest.append("</group>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, actor,
                _momlChangeRequest.toString());
        request.setUndoable(true);
        actor.requestChange(request);
        // reset the current request
        _momlChangeRequest = new StringBuffer();
    }

    /**
     * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it
     * immediately. The request is addressed to a specific NamedObj in the
     * Ptolemy model and hence does not get buffered because there is only
     * exactly one move request per layout run per node.
     * 
     * @param obj
     *            Ptolemy node to be moved
     * @param x
     *            new coordinate
     * @param y
     *            new coordinate
     */
    protected static void _setLocation(NamedObj obj, double x, double y) {
        String moml = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{"
                + x + "," + y + "}\"></property>\n";
        // need to request a MoML Change for a particular NamedObj and not the
        // top level element
        // so we need multiple requests here
        MoMLChangeRequest request = new MoMLChangeRequest(obj, obj, moml
                .toString());
        request.setUndoable(true);
        obj.requestChange(request);
    }

    /**
     * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it
     * immediately. The request is addressed to a specific NamedObj in the
     * Ptolemy model and hence does not get buffered because there is only
     * exactly one move request per layout run per node.
     * 
     * @param obj
     *            Ptolemy node to be moved
     * @param x
     *            new coordinate
     * @param y
     *            new coordinate
     */
    protected static void _setLocation(Vertex vertex, Relation relation,
            double x, double y) {
        String moml = "<vertex name=\"" + vertex.getName() + "\" value=\"[" + x
                + "," + y + "]\"></vertex>\n";
        // need to request a MoML Change for a particular Vertex
        // and not the
        // top level element
        // so we need multiple requests here
        MoMLChangeRequest request = new MoMLChangeRequest(vertex, relation,
                moml.toString());
        request.setUndoable(true);
        relation.requestChange(request);
    }

    /**
     * Create a unique name in the model nameset prefixed by the given prefix.
     * This method actually does not guarantee this yet but tries brute force by
     * adding some counting String. Problem with the uniqueName() method of
     * CompositeActors is, that they will return always the same name while the
     * corresponding objects are not yet added to the model, e.g. by buffering
     * multiple relation creations in one MoMLChangeRequest which gets executed
     * later.
     * 
     * @param prefix
     * @return Unique name in the model context
     */
    private String _getUniqueName(String prefix) {
        _nameSuffix++;
        return prefix + "_k_" + _nameSuffix;
    }

    /**
     * StringBuffer for Requests of Model changes. In Ptolemy the main
     * infrastructure to do model changes is through XML change requests of the
     * XML representation. This field is used to collect all changes in one
     * String and then carry them out in only one operation whereas possible.
     */
    private StringBuffer _momlChangeRequest;

    /**
     * Some Name suffix to compute unique names.
     */
    private int _nameSuffix = 0;
}

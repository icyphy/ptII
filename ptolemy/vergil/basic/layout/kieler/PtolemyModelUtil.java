/* Static methods for manipulating Ptolemy models.
@Copyright (c) 2009-2011 The Regents of the University of California.
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

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.LocationParameter;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;

///////////////////////////////////////////////////////////////////
//// PtolemyModelUtil
/**
 * Class containing methods for manipulating Ptolemy models for the purpose of
 * layout a graphical Ptolemy diagram. Methods for positioning Actors and
 * creating vertices are available. The changes are performed by
 * MoMLChangeRequests where as long as possible those requests are buffered in
 * order to perform multiple changes at once for performance.
 *
 * @author Hauke Fuhrmann (<a href="mailto:haf@informatik.uni-kiel.de">haf</a>),
 *         Christian Motika (<a href="mailto:cmot@informatik.uni-kiel.de">cmot</a>)
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtolemyModelUtil {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add an {@link Attribute} to a Ptolemy object by means of a
     * {@link MoMLChangeRequest}. Although this might be inefficient, this will
     * take care about correct updating of all things that require the change,
     * e.g. the GUI.
     *
     * @param target the target Ptolemy object
     * @param attribute the attribute to add
     */
    public void addProperty(NamedObj target, Attribute attribute) {
        String moml = attribute.exportMoMLPlain();
        MoMLChangeRequest request = new MoMLChangeRequest(target, target, moml);
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        target.requestChange(request);
        _anyRequestsSoFar = true;
    }

    /**
     * Flush all buffered change requests to the given Actor. Reset the buffer
     * afterwards.
     *
     * @param actor The target of the change request, e.g. the composite actor
     *            containing the objects for which changes are requested.
     */
    public void performChangeRequest(CompositeActor actor) {
        if (_momlChangeRequest.toString().trim().equals("")) {
            // if request is empty, don't do anything.
            return;
        }
        _momlChangeRequest.insert(0, "<group>");
        _momlChangeRequest.append("</group>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, actor,
                _momlChangeRequest.toString());
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        actor.requestChange(request);
        // reset the current request
        _momlChangeRequest = new StringBuilder();
        _anyRequestsSoFar = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the location given by the location attribute of the given input
     * object. If the Ptolemy object has no location attribute, return double
     * zero.
     *
     * @param namedObj The Ptolemy object for which the location should be
     *            retrieved.
     * @return A vector corresponding to the location (x and y) of the object.
     *          Will return a zero vector if no location attribute is set for the object.
     */
    protected static Point2D.Double _getLocation(NamedObj namedObj) {
        Point2D.Double location = new Point2D.Double();
        Location locationAttribute = null;
        if (namedObj instanceof Location) {
            locationAttribute = (Location) namedObj;
        } else if (namedObj instanceof Vertex) {
            double[] coords = ((Vertex) namedObj).getLocation();
            location.x = coords[0];
            location.y = coords[1];
        } else {
            NamedObj object = namedObj;
            Attribute attribute = null;
            // Search for the next entity in the hierarchy that has
            // a location attribute.
            do {
                attribute = object.getAttribute("_location");
                if (attribute == null) {
                    List<Location> locatables = object.attributeList(Location.class);
                    if (!locatables.isEmpty()) {
                        attribute = locatables.get(0);
                    }
                }
                if (object instanceof Relation) {
                    object = null;
                } else {
                    object = object.getContainer();
                }
            } while (attribute == null && object != null);
            if (attribute != null) {
                if (attribute instanceof Location) {
                    locationAttribute = ((Location) attribute);
                } else if (attribute instanceof LocationParameter) {
                    double[] coords = ((LocationParameter) attribute).getLocation();
                    location.x = coords[0];
                    location.y = coords[1];
                }
            }
        }
        if (locationAttribute != null) {
            /* Workaround for a strange behavior: If loading a model
             * from MoML, a Location might have set a valid expression with
             * non trivial values, but it hasn't been validated and therefore
             * the value is still {0,0}
             */
            double[] coords = locationAttribute.getLocation();
            try {
                if (coords[0] == 0 && coords[1] == 0) {
                    locationAttribute.validate();
                    coords = locationAttribute.getLocation();
                }
                location.x = coords[0];
                location.y = coords[1];
            } catch (IllegalActionException e) {
                /* nothing, use default value */
            }
        }
        // double arrays are used call-by-reference, so we return a copy here
        return location;
    }

    /**
     * For a set of relations get a set of relation groups, i.e. for each
     * relation construct a list of relations that are all interconnected,
     * either directly or indirectly.
     *
     * @param relations Set of relations
     * @return a Set of relation groups as given by List<Relation> objects by
     *         Ptolemy
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
     * Check whether the given Ptolemy model object has any connections, i.e. is
     * connected to any other components via some link.
     *
     * @param namedObj The Ptolemy model object which is to be analyzed
     * @return True if the object is an Actor and any port has any relations or
     *         is connected to any other port; true if the object is a Relation;
     *         false if the object is an Attribute. Defaults to false.
     */
    protected static boolean _isConnected(NamedObj namedObj) {
        if (namedObj instanceof Attribute) {
            return false;
        }
        if (namedObj instanceof Actor) {
            Actor actor = (Actor) namedObj;
            // if any port of an actor is connected to any other
            // assume that there is also no visible connection
            for (Object o : actor.inputPortList()) {
                Port port = (Port) o;
                if (!port.connectedPortList().isEmpty()
                        || !port.linkedRelationList().isEmpty()) {
                    return true;
                }
            }
            for (Object o : actor.outputPortList()) {
                Port port = (Port) o;
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
     * @param port The port to be analyzed
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
     * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it
     * immediately. The request is addressed to a specific NamedObj in the
     * Ptolemy model and hence does not get buffered because there is only
     * exactly one move request per layout run per node.
     *
     * @param obj Ptolemy node to be moved
     * @param x new coordinate
     * @param y new coordinate
     */
    protected void _setLocation(NamedObj obj, double x, double y) {
        String moml = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{"
                + x + "," + y + "}\"></property>\n";
        // need to request a MoML Change for a particular NamedObj and not the
        // top level element, so we need multiple requests here
        MoMLChangeRequest request = new MoMLChangeRequest(obj, obj, moml);
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        obj.requestChange(request);
        _anyRequestsSoFar = true;
    }

    /**
     * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it
     * immediately. The request is addressed to a specific NamedObj in the
     * Ptolemy model and hence does not get buffered because there is only
     * exactly one move request per layout run per node.
     *
     * @param vertex Ptolemy node to be moved
     * @param relation Ptolemy Relation to be moved
     * @param x new coordinate
     * @param y new coordinate
     */
    protected void _setLocation(Vertex vertex, Relation relation, double x, double y) {
        String moml = "<vertex name=\"" + vertex.getName() + "\" value=\"[" + x
                + "," + y + "]\"></vertex>\n";
        // need to request a MoML Change for a particular Vertex and not the
        // top level element, so we need multiple requests here
        MoMLChangeRequest request = new MoMLChangeRequest(vertex, relation, moml);
        request.setUndoable(true);
        if (_anyRequestsSoFar) {
            request.setMergeWithPreviousUndo(true);
        }
        relation.requestChange(request);
        _anyRequestsSoFar = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Flag indicating whether there have been any MoMLChangeRequests processed
     * so far or not. This is required for proper undo, because if there were
     * some requests we also want to merge our undo to them.
     */
    private boolean _anyRequestsSoFar = false;

    /**
     * String builder for requests of Model changes. In Ptolemy the main
     * infrastructure to do model changes is through XML change requests of the
     * XML representation. This field is used to collect all changes in one
     * String and then carry them out in only one operation whereas possible.
     */
    private StringBuilder _momlChangeRequest = new StringBuilder();

}

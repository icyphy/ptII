/* Static methods for manipulating Ptolemy models.
@Copyright (c) 2009-2013 The Regents of the University of California.
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

import javax.swing.SwingConstants;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.RelativeLocation;
import ptolemy.vergil.basic.RelativeLocatable;

///////////////////////////////////////////////////////////////////
//// PtolemyModelUtil
/**
 * Utility class for accessing properties of a Ptolemy model in the context
 * of automatic layout. The methods are used by the KIELER layout bridge
 * that integrates KIELER layout algorithms into Ptolemy.
 *
 * @author Hauke Fuhrmann (<a href="mailto:haf@informatik.uni-kiel.de">haf</a>),
 *         Christian Motika (<a href="mailto:cmot@informatik.uni-kiel.de">cmot</a>),
 *         Miro Spoenemann (<a href="mailto:msp@informatik.uni-kiel.de">msp</a>)
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public final class PtolemyModelUtil {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Find a location for the given object.
     *
     * @param namedObj a model object
     * @return the object's location, or {@code null} if there is no location
     */
    protected static Locatable _getLocation(NamedObj namedObj) {
        if (namedObj instanceof Locatable) {
            return (Location) namedObj;
        } else {
            NamedObj object = namedObj;

            // Search for the next entity in the hierarchy that has
            // a location attribute.
            while (object != null) {
                Attribute attribute = object.getAttribute("_location");
                if (attribute instanceof Locatable) {
                    return (Locatable) attribute;
                }
                List<Locatable> locatables = object
                        .attributeList(Locatable.class);
                if (!locatables.isEmpty()) {
                    return locatables.get(0);
                }
                // Relations are directly contained in a composite entity, so
                // don't take any parent location.
                if (object instanceof Relation) {
                    object = null;
                } else {
                    object = object.getContainer();
                }
            }
        }
        return null;
    }

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
    protected static Point2D _getLocationPoint(NamedObj namedObj) {
        Point2D point = _getLocationPoint(_getLocation(namedObj));
        if (point == null) {
            point = new Point2D.Double();
        }
        return point;
    }

    /**
     * Retrieve the actual position from a locatable instance.
     *
     * @param locatable a locatable
     * @return the actual position, or null if none is found
     */
    protected static Point2D _getLocationPoint(Locatable locatable) {
        if (locatable != null) {
            double[] coords = locatable.getLocation();
            try {
                /* Workaround for a strange behavior: If loading a model
                 * from MoML, a Location might have set a valid expression with
                 * non trivial values, but it hasn't been validated and therefore
                 * the value is still {0,0}
                 */
                if (coords[0] == 0 && coords[1] == 0) {
                    locatable.validate();
                    coords = locatable.getLocation();
                }
                Point2D.Double location = new Point2D.Double();
                location.x = coords[0];
                location.y = coords[1];
                return location;
            } catch (IllegalActionException e) {
                // nothing, use default value
            }
        }
        return null;
    }

    /**
     * For a set of relations get a set of relation groups, i.e. for each
     * relation construct a list of relations that are all interconnected,
     * either directly or indirectly.
     *
     * @param relations Set of relations
     * @return a Set of relation groups as given by List &lt;Relation&gt; objects by Ptolemy
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
     * @param namedObj The Ptolemy model object which is to be analyzed
     * @return True if the object is an Actor and any port has any relations or
     *         is connected to any other port; true if the object is a Relation;
     *         false if the object is an Attribute. Defaults to false.
     */
    protected static boolean _isConnected(NamedObj namedObj) {
        if (namedObj instanceof RelativeLocatable) {
            // Relative locatables may be connected to a reference object.
            Locatable locatable = _getLocation(namedObj);
            if (locatable instanceof RelativeLocation) {
                NamedObj referenceObj = _getReferencedObj((RelativeLocation) locatable);
                return referenceObj != null && _isConnected(referenceObj);
            }
        }
        if (namedObj instanceof Attribute) {
            return false;
        }
        if (namedObj instanceof Actor) {
            Actor actor = (Actor) namedObj;
            // If any port of an actor is connected to any other
            // assume that there is also no visible connection.
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
        if (namedObj instanceof ComponentPort) {
            ComponentPort port = (ComponentPort) namedObj;
            return !port.insidePortList().isEmpty()
                    || !port.insideRelationList().isEmpty();
        }
        if (namedObj instanceof State) {
            State state = (State) namedObj;
            return !state.getIncomingPort().connectedPortList().isEmpty()
                    || !state.getOutgoingPort().connectedPortList().isEmpty();
        }
        // default to false
        return false;
    }

    /**
     * Find the reference object for the given relative location.
     *
     * @param location A relative location
     * @return The corresponding reference object, or null if there is none
     */
    protected static NamedObj _getReferencedObj(RelativeLocation location) {
        NamedObj container = location.getContainer();
        if (container != null) {
            NamedObj containersContainer = container.getContainer();
            if (containersContainer instanceof CompositeEntity) {
                CompositeEntity composite = (CompositeEntity) containersContainer;
                String relativeToName = location.relativeTo.getExpression();
                String elementName = location.relativeToElementName
                        .getExpression();
                // The relativeTo object is not necessarily an Entity.
                NamedObj relativeToNamedObj;
                if (elementName.equals("property")) {
                    relativeToNamedObj = composite.getAttribute(relativeToName);
                } else if (elementName.equals("port")) {
                    relativeToNamedObj = composite.getPort(relativeToName);
                } else if (elementName.equals("relation")) {
                    relativeToNamedObj = composite.getRelation(relativeToName);
                } else {
                    relativeToNamedObj = composite.getEntity(relativeToName);
                }
                return relativeToNamedObj;
            }
        }
        return null;
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
     * Get the direction of the edge anchor point of an external port
     * inside a composite actor. It is given as a {@link SwingConstants}, like
     * {@link SwingConstants#NORTH}, {@link SwingConstants#SOUTH},
     * {@link SwingConstants#EAST}, {@link SwingConstants#WEST}.
     * @param port the external port
     * @return a SwingConstant about the direction
     */
    protected static int _getExternalPortDirection(Port port) {
        if (port instanceof IOPort) {
            boolean isInput = ((IOPort) port).isInput();
            boolean isOutput = ((IOPort) port).isOutput();
            if (isInput && !isOutput) {
                return SwingConstants.EAST;
            }
            if (!isInput && isOutput) {
                return SwingConstants.WEST;
            }
            if (isInput && isOutput) {
                return SwingConstants.NORTH;
            }
        }
        return SwingConstants.SOUTH;
    }

}

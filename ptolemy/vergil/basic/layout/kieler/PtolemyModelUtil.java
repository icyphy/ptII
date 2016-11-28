/* Static methods for manipulating Ptolemy models.
@Copyright (c) 2009-2016 The Regents of the University of California.
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
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.KielerLayoutUtil;
import ptolemy.vergil.basic.RelativeLocatable;
import ptolemy.vergil.basic.RelativeLocation;

///////////////////////////////////////////////////////////////////
//// PtolemyModelUtil
/**
 * Utility class for accessing properties of a Ptolemy model in the context
 * of automatic layout. The methods are used by the KIELER layout bridge
 * that integrates KIELER layout algorithms into Ptolemy.
 *
 * @author Hauke Fuhrmann (<a href="mailto:haf@informatik.uni-kiel.de">haf</a>),
 *         Christian Motika (<a href="mailto:cmot@informatik.uni-kiel.de">cmot</a>),
 *         Miro Spoenemann (<a href="mailto:msp@informatik.uni-kiel.de">msp</a>),
 *         Ulf Rueegg
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public final class PtolemyModelUtil {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
            Locatable locatable = KielerLayoutUtil.getLocation(namedObj);
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

    /**
     * Return true if the state is an initial state.
     * @param state a {@link State} to test
     * @return whether the passed state has the isInitialState parameter set to true.
     */
    protected static boolean _isInitialState(State state) {
        return state.isInitialState.getValueAsString()
                .equals(Boolean.TRUE.toString());
    }

    /**
     * Return true if the state is a final state.
     * @param state a {@link State} to test
     * @return whether the passed state has the isFinalState parameter set to true.
     */
    protected static boolean _isFinalState(State state) {
        return state.isFinalState.getValueAsString()
                .equals(Boolean.TRUE.toString());
    }
}

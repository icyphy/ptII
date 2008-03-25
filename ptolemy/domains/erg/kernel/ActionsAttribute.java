/*

@Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.domains.erg.kernel;

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.UnknownResultException;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.CommitActionsAttribute;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see CommitActionsAttribute
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ActionsAttribute extends AbstractActionsAttribute {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ActionsAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * @param workspace
     */
    public ActionsAttribute(Workspace workspace) {
        super(workspace);
    }

    public void execute(ParserScope scope) throws IllegalActionException {
        super.execute();

        if (_destinations != null) {
            Iterator<?> destinations = _destinations.iterator();
            Iterator<?> channels = _numbers.iterator();
            Iterator<?> parseTrees = _parseTrees.iterator();

            while (destinations.hasNext()) {
                NamedObj nextDestination = (NamedObj) destinations.next();

                // Need to get the next channel even if it's not used.
                Integer channel = (Integer) channels.next();
                ASTPtRootNode parseTree = (ASTPtRootNode) parseTrees.next();
                Token token;

                try {
                    token = _parseTreeEvaluator.evaluateParseTree(parseTree,
                            scope);
                } catch (IllegalActionException ex) {
                    throw new IllegalActionException(this, ex,
                            "Expression invalid.");
                }

                if (nextDestination instanceof IOPort) {
                    IOPort destination = (IOPort) nextDestination;

                    try {
                        if (channel != null) {
                            // Clear the far receivers before sending out a
                            // token, so later outputs in the same firing
                            // overwrite previous ones.
                            destination.sendClear(channel.intValue());
                            if (token != null) {
                                destination.send(channel.intValue(), token);
                            }
                        } else {
                            // Clear the far receivers before sending out a
                            // token, so later outputs in the same firing
                            // overwrite previous ones.
                            destination.broadcastClear();
                            if (token != null) {
                                destination.broadcast(token);
                            }
                        }
                    } catch (NoRoomException ex) {
                        throw new IllegalActionException(this,
                                "Cannot complete action: " + ex.getMessage());
                    } catch (UnknownResultException ex) {
                        // Produce no output.
                    }
                } else if (nextDestination instanceof Variable) {
                    Variable destination = (Variable) nextDestination;

                    try {
                        destination.setToken(token);
                        destination.validate();
                    } catch (UnknownResultException ex) {
                        destination.setUnknown(true);
                    }
                } else {
                    throw new IllegalActionException(this,
                            "Destination is neither an IOPort nor a Variable: "
                                    + nextDestination.getFullName());
                }
            }
        }
    }

    protected NamedObj _getDestination(String name)
    throws IllegalActionException {
        Event event = (Event) getContainer();

        if (event == null) {
            throw new IllegalActionException(this,
                    "Action has no container transition.");
        }

        Entity erg = (Entity) event.getContainer();

        if (erg == null) {
            throw new IllegalActionException(this, event,
                    "Transition has no container.");
        }

        IOPort port = (IOPort) erg.getPort(name);

        if (port == null) {
            // No port found.  Try for a variable.
            Attribute variable = erg.getAttribute(name);

            if (variable == null) {
                // Try for a refinement variable.
                int period = name.indexOf(".");

                if (period > 0) {
                    String refinementName = name.substring(0, period);
                    String entryName = name.substring(period + 1);

                    // FIXME: Look in the container of the fsm???
                    // Below we look for an attribute only in the fsm
                    // itself.
                    Nameable container = erg.getContainer();

                    if (container instanceof CompositeEntity) {
                        Entity refinement = ((CompositeEntity) container)
                                .getEntity(refinementName);

                        if (refinement != null) {
                            Attribute entry = refinement
                                    .getAttribute(entryName);

                            if (entry instanceof Variable) {
                                return entry;
                            }
                        }
                    }
                }

                throw new IllegalActionException(erg, this,
                        "Cannot find port or variable with the name: " + name);
            } else {
                if (!(variable instanceof Variable)) {
                    throw new IllegalActionException(erg, this,
                            "The attribute with name \"" + name + "\" is not "
                            + "an instance of Variable.");
                }

                return variable;
            }
        } else {
            if (!port.isOutput()) {
                throw new IllegalActionException(erg, this,
                        "The port is not an output port: " + name);
            }

            return port;
        }
    }
}

/* An attribute associated with Ptera events containing executable actions.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptera.kernel;

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.UnknownResultException;
import ptolemy.data.expr.Variable;
import ptolemy.domains.modal.kernel.AbstractActionsAttribute;
import ptolemy.domains.modal.kernel.CommitActionsAttribute;
import ptolemy.domains.modal.kernel.OutputActionsAttribute;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 An attribute associated with Ptera events containing executable actions. The
 actions are written as assignments separated with semicolons. They can be
 executed either to change the values of the variables assigned to, or to send
 output tokens to ports.
 <p>
 This class is similar to {@link CommitActionsAttribute} and {@link
 OutputActionsAttribute} in FSM. The difference is that in Ptera, no distinction
 is made between commit actions and output actions. The destination of an
 assignment can be either a variable or a port. If a port and a variable have
 the same name in a Ptera model, then assigning to that name causes output to
 be sent to the port instead of changing the value of the variable.
 <p>
 Another difference between this class and those in FSM is that in Ptera, events
 are allowed to receive parameters. The parameters can be referred to in the
 actions associated with that event.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @see CommitActionsAttribute
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ActionsAttribute extends AbstractActionsAttribute {

    /** Construct an ActionsAttribute for a Ptera event.
     *
     *  @param event The event.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the container already
     *   has an attribute with the name.
     */
    public ActionsAttribute(Event event, String name)
            throws IllegalActionException, NameDuplicationException {
        super(event, name);
    }

    /** Construct an ActionsAttribute in the specified workspace with an empty
     *  string as a name.
     *  The attribute is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace that will list the attribute.
     */
    public ActionsAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Execute this action.  For each destination identified in the
     *  action, compute the value in the action and perform the
     *  particular assignment.
     *
     *  @exception IllegalActionException If a destination is not found or if
     *  thrown while evaluating the expressions.
     */
    @Override
    public void execute() throws IllegalActionException {
        super.execute();

        if (_destinations != null && _destinations.size() > 0) {
            Iterator<?> destinations = _destinations.iterator();
            Iterator<Integer> channels = getChannelNumberList().iterator();
            Iterator<?> parseTrees = _parseTrees.iterator();

            while (destinations.hasNext()) {
                NamedObj nextDestination = (NamedObj) destinations.next();

                // Need to get the next channel even if it's not used.
                Integer channel = channels.next();
                ASTPtRootNode parseTree = (ASTPtRootNode) parseTrees.next();
                Token token;

                try {
                    token = _parseTreeEvaluator.evaluateParseTree(parseTree,
                            _getParserScope());
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
                        if (channel == null) {
                            destination.setToken(token);
                        } else {
                            ArrayToken array = (ArrayToken) destination
                                    .getToken();
                            Token[] tokens = array.arrayValue();
                            tokens[channel.intValue()] = token;
                            destination.setToken(new ArrayToken(tokens));
                        }
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

    /** Given a destination name, return a NamedObj that matches that
     *  destination.  This method never returns null (throw an exception
     *  instead).
     *
     *  @param name The name of the destination, or null if none is found.
     *  @return An object (like a port or a variable) with the specified name.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have a destination with the specified name.
     */
    @Override
    protected NamedObj _getDestination(String name)
            throws IllegalActionException {
        Event event = (Event) getContainer();

        if (event == null) {
            throw new IllegalActionException(this,
                    "Action has no container transition.");
        }

        Entity ptera = (Entity) event.getContainer();

        if (ptera == null) {
            throw new IllegalActionException(this, event,
                    "Transition has no container.");
        }

        IOPort port = (IOPort) ptera.getPort(name);

        if (port == null) {
            NamedObj container = event;
            Attribute variable = null;
            while (variable == null && container != null) {
                variable = _getAttribute(container, name);
                NamedObj containerContainer = container.getContainer();
                if (container instanceof ModalController) {
                    State state = ((ModalController) container)
                            .getRefinedState();
                    if (state == null) {
                        container = containerContainer;
                    } else {
                        container = state.getContainer();
                    }
                } else {
                    container = containerContainer;
                }
            }

            if (variable == null) {
                throw new IllegalActionException(ptera, this,
                        "Cannot find port or variable with the name: " + name);
            }

            if (!(variable instanceof Variable)) {
                throw new IllegalActionException(ptera, this,
                        "The attribute with name \"" + name + "\" is not "
                                + "an instance of Variable.");
            }

            return variable;
        } else {
            if (!port.isOutput()) {
                throw new IllegalActionException(ptera, this,
                        "The port is not an output port: " + name);
            }

            return port;
        }
    }

    /** Return a parser scope used to type-check the actions.
     *
     *  @return The parser scope.
     */
    @Override
    protected ParserScope _getParserScope() {
        return ((Event) getContainer())._getParserScope();
    }

    /** Get an attribute with the given name in the given container. The
     *  attribute name may contain "."-separated parts, in which case entities
     *  within the container are searched.
     *
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @return The attribute if found, or null.
     *  @exception IllegalActionException If error occurs when trying to get an
     *  attribute.
     */
    private Attribute _getAttribute(NamedObj container, String name)
            throws IllegalActionException {
        Attribute attribute = container.getAttribute(name);
        if (attribute != null) {
            return attribute;
        }

        int period = name.indexOf(".");
        if (period > 0) {
            String componentName = name.substring(0, period);
            String nameWithinComponent = name.substring(period + 1);
            if (container instanceof PteraController) {
                Entity entity = ((PteraController) container)
                        .getEntity(componentName);
                if (entity instanceof Event) {
                    TypedActor[] refinements = ((Event) entity).getRefinement();
                    if (refinements != null) {
                        for (TypedActor refinement : refinements) {
                            attribute = _getAttribute((NamedObj) refinement,
                                    nameWithinComponent);
                            if (attribute != null) {
                                return attribute;
                            }
                        }
                    }
                }
                return null;
            } else if (container instanceof CompositeEntity) {
                ComponentEntity entity = ((CompositeEntity) container)
                        .getEntity(componentName);
                if (entity != null) {
                    return _getAttribute(entity, nameWithinComponent);
                }
            }
        }

        return null;
    }
}

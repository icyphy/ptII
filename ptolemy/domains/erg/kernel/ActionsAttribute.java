/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 */

package ptolemy.domains.erg.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.UnknownResultException;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.CommitActionsAttribute;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see CommitActionsAttribute
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ActionsAttribute extends StringAttribute {

    /**
     *
     */
    public ActionsAttribute() {
    }

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
        if (_destinationsListVersion != workspace().getVersion()) {
            _updateDestinations();
        }

        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }

        if (_destinations != null) {
            Iterator<NamedObj> destinations = _destinations.iterator();
            Iterator<Integer> channels = _numbers.iterator();
            Iterator<ASTPtRootNode> parseTrees = _parseTrees.iterator();

            while (destinations.hasNext()) {
                NamedObj nextDestination = destinations.next();

                // Need to get the next channel even if it's not used.
                Integer channel = channels.next();
                ASTPtRootNode parseTree = parseTrees.next();
                Token token;

                try {
                    token = _parseTreeEvaluator.evaluateParseTree(parseTree,
                            scope);
                } catch (IllegalActionException ex) {
                    // Chain exceptions to get the actor that
                    // threw the exception.
                    throw new IllegalActionException(this, ex,
                            "Expression invalid.");
                }

                if (nextDestination instanceof IOPort) {
                    IOPort destination = (IOPort) nextDestination;

                    try {
                        if (channel != null) {
                            if (token == null) {
                                destination.sendClear(channel.intValue());

                                if (_debugging) {
                                    _debug(getFullName() + " port: "
                                            + destination.getName()
                                            + " channel: " + channel.intValue()
                                            + ", Clear!");
                                }
                            } else {
                                destination.send(channel.intValue(), token);

                                if (_debugging) {
                                    _debug(getFullName() + " port: "
                                            + destination.getName()
                                            + " channel: " + channel.intValue()
                                            + ", token: " + token);
                                }
                            }
                        } else {
                            if (token == null) {
                                destination.broadcastClear();

                                if (_debugging) {
                                    _debug(getFullName() + " port: "
                                            + destination.getName()
                                            + " broadcast Clear!");
                                }
                            } else {
                                destination.broadcast(token);

                                if (_debugging) {
                                    _debug(getFullName() + " port: "
                                            + destination.getName()
                                            + " broadcast token: " + token);
                                }
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
                        //Token token = variable.getToken();
                        destination.setToken(token);

                        // Force all dependents to re-evaluate.
                        // This makes the parameters in the actors of
                        // the refinement take on new values immediately
                        // after the action is committed.
                        destination.validate();

                        if (_debugging) {
                            _debug(getFullName() + " variable: "
                                    + destination.getName() + ", value: "
                                    + token);
                        }
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

    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        _destinationNames = new LinkedList<String>();
        _numbers = new LinkedList<Integer>();
        _parseTrees = new LinkedList<ASTPtRootNode>();

        if ((expression == null) || expression.trim().equals("")) {
            return;
        }

        PtParser parser = new PtParser();
        Map<?, ?> map = parser.generateAssignmentMap(expression);

        for (Iterator<?> names = map.keySet().iterator(); names.hasNext();) {
            String name = (String) names.next();
            ASTPtAssignmentNode node = (ASTPtAssignmentNode) map.get(name);

            // Parse the destination specification first.
            String completeDestinationSpec = node.getIdentifier();
            int openParen = completeDestinationSpec.indexOf("(");

            if (openParen > 0) {
                // A channel is being specified.
                int closeParen = completeDestinationSpec.indexOf(")");

                if (closeParen < openParen) {
                    throw new IllegalActionException(this,
                            "Malformed action: expected destination = "
                                    + "expression. Got: "
                                    + completeDestinationSpec);
                }

                _destinationNames.add(completeDestinationSpec.substring(0,
                        openParen).trim());

                String channelSpec = completeDestinationSpec.substring(
                        openParen + 1, closeParen);

                try {
                    _numbers.add(Integer.valueOf(channelSpec));
                } catch (NumberFormatException ex) {
                    throw new IllegalActionException(this,
                            "Malformed action: expected destination = "
                                    + "expression. Got: "
                                    + completeDestinationSpec);
                }
            } else {
                // No channel is specified.
                _destinationNames.add(completeDestinationSpec);
                _numbers.add(null);
            }

            // Parse the expression
            _parseTrees.add(node.getExpressionTree());
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
                            "The attribute with name \"" + name
                                    + "\" is not an " + "instance of Variable.");
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

    /** List of destination names. */
    protected List<String> _destinationNames;

    /** List of destinations. */
    protected List<NamedObj> _destinations;

    /** The workspace version number when the _destinations list is last
     *  updated.
     */
    protected long _destinationsListVersion = -1;

    /** List of channels. */
    protected List<Integer> _numbers;

    /** The parse tree evaluator. */
    protected ParseTreeEvaluator _parseTreeEvaluator;

    /** The list of parse trees. */
    protected List<ASTPtRootNode> _parseTrees;

    private void _updateDestinations() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            if (_destinationNames != null) {
                _destinations = new LinkedList<NamedObj>();

                Iterator<String> destinationNames =
                    _destinationNames.iterator();

                while (destinationNames.hasNext()) {
                    String destinationName = destinationNames.next();
                    NamedObj destination = _getDestination(destinationName);
                    _destinations.add(destination);
                }
            }

            _destinationsListVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }
}

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedActor;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.UnknownResultException;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.CommitActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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
    public ActionsAttribute(Event container, String name)
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

    public static class ParametersParserScope implements ParserScope {

        public Token get(String name) throws IllegalActionException {
            throw new IllegalActionException("This parser scope is for "
                    + "type-checking only, and the get() method is not "
                    + "implemented.");
        }

        public Type getType(String name) throws IllegalActionException {
            if (_paramMap.containsKey(name)) {
                return _paramMap.get(name);
            } else {
                return _superscope.getType(name);
            }
        }

        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return _superscope.getTypeTerm(name);
        }

        public Set<?> identifierSet() throws IllegalActionException {
            Set<Object> set = new HashSet<Object>(_paramMap.keySet());
            set.addAll((Set<?>) _superscope.identifierSet());
            return set;

        }

        ParametersParserScope(Map<String, Type> argumentMap,
                ParserScope superscope) {
            _paramMap = argumentMap;
            _superscope = superscope;
        }

        private Map<String, Type> _paramMap;

        private ParserScope _superscope;

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
            NamedObj container = erg;
            Attribute variable = null;
            while (variable == null && container != null) {
                variable = _getAttribute(container, name);
                NamedObj containerContainer = container.getContainer();
                if (container instanceof ModalController) {
                    State state = (State) ((ModalController) container)
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
                throw new IllegalActionException(erg, this,
                        "Cannot find port or variable with the name: " + name);
            }

            if (!(variable instanceof Variable)) {
                throw new IllegalActionException(erg, this,
                        "The attribute with name \"" + name + "\" is not "
                        + "an instance of Variable.");
            }

            return variable;
        } else {
            if (!port.isOutput()) {
                throw new IllegalActionException(erg, this,
                        "The port is not an output port: " + name);
            }

            return port;
        }
    }

    protected ParserScope _getParserScope() {
        return _scope;
    }

    protected void _updateParserScope(ParserScope superscope, List<?> names,
            Type[] types) {
        if (types != null && types.length > 0) {
            Iterator<?> namesIter = names.iterator();
            Map<String, Type> paramMap = new HashMap<String, Type>();
            for (int i = 0; namesIter.hasNext(); i++) {
                String name = (String) namesIter.next();
                paramMap.put(name, types[i]);
            }
            _scope = new ParametersParserScope(paramMap, superscope);
        } else {
            _scope = superscope;
        }
    }

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
            if (container instanceof ERGController) {
                Entity entity =
                    ((ERGController) container).getEntity(componentName);
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

    private ParserScope _scope;
}

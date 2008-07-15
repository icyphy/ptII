/* An attribute associated with ERG events containing executable actions.

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
import ptolemy.domains.fsm.kernel.OutputActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 An attribute associated with ERG events containing executable actions. The
 actions are written as assignments separated with semicolons. They can be
 executed either to change the values of the variables assigned to, or to send
 output tokens to ports.
 <p>
 This class is similar to {@link CommitActionsAttribute} and {@link
 OutputActionsAttribute} in FSM. The difference is that in ERG, no distinction
 is made between commit actions and output actions. The destination of an
 assignment can be either a variable or a port. If a port and a variable have
 the same name in an ERG model, then assigning to that name causes output to be
 sent to the port instead of changing the value of the variable.
 <p>
 Another difference between this class and those in FSM is that in ERG, events
 are allowed to receive parameters. The parameters can be referred to in the
 actions associated with that event.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @see CommitActionsAttribute
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ActionsAttribute extends AbstractActionsAttribute {

    /** Construct an ActionsAttribute for an ERG event.
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

    /** Clone the attribute into the specified workspace. This calls the base
     *  class and then resets the scope in which variable and port names are
     *  looked up.
     *
     *  @param workspace The workspace.
     *  @return A new ActionsAttribute.
     *  @exception CloneNotSupportedException If the superclass throws it.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ActionsAttribute attribute = (ActionsAttribute) super.clone(workspace);
        attribute._scope = null;
        attribute._scopeVersion = -1;
        return attribute;
    }

    /** Execute the actions in this attribute in the given parser scope.
     *
     *  @param scope The parser scope in which the actions are to be executed.
     *  @exception IllegalActionException If the actions cannot be executed.
     */
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

    //////////////////////////////////////////////////////////////////////////
    //// ParametersParserScope

    /**
     The parser scope that contains an parameter map. This parser scope is for
     type-checking only. For evaluation of the actions, a different parser scope
     needs to be

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class ParametersParserScope implements ParserScope {

        /** Throw an exception because this method is not expected to be called
         *  in type-checking.
         *
         *  @param name The name of a variable or a port.
         *  @return None.
         *  @exception IllegalActionException Thrown always.
         */
        public Token get(String name) throws IllegalActionException {
            throw new IllegalActionException("This parser scope is for "
                    + "type-checking only, and the get() method is not "
                    + "implemented.");
        }

        /** Get the type of a variable or a port specified by the given name. If
         *  a parameter is found in the parameter map, the type of that
         *  parameter is returned. Otherwise, the super-scope is looked up.
         *
         *  @param name The name of a variable or a port.
         *  @return The type.
         *  @exception IllegalActionException If the getType() method of the
         *  super-scope throws it.
         */
        public Type getType(String name) throws IllegalActionException {
            if (_paramMap.containsKey(name)) {
                return _paramMap.get(name);
            } else {
                return _superscope.getType(name);
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the variable to be looked up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return _superscope.getTypeTerm(name);
        }

        /** Return a list of names corresponding to the identifiers
         *  defined by this scope.  If an identifier is returned in this
         *  list, then get() and getType() will return a value for the
         *  identifier.  Note that generally speaking, this list is
         *  extremely expensive to compute, and users should avoid calling
         *  it.  It is primarily used for debugging purposes.
         *  @return A list of names corresponding to the identifiers
         *  defined by this scope.
         *  @exception IllegalActionException If constructing the list causes
         *  it.
         */
        public Set<?> identifierSet() throws IllegalActionException {
            Set<Object> set = new HashSet<Object>(_paramMap.keySet());
            set.addAll((Set<?>) _superscope.identifierSet());
            return set;

        }

        /** Construct a parser scope with a parameter map and a super-scope.
         *
         *  @param paramMap The parameter map.
         *  @param superscope The super-scope.
         */
        ParametersParserScope(Map<String, Type> paramMap,
                ParserScope superscope) {
            _paramMap = paramMap;
            _superscope = superscope;
        }

        /** The parameter map. */
        private Map<String, Type> _paramMap;

        /** The super-scope. */
        private ParserScope _superscope;

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

    /** Return a parser scope used to type-check the actions. To evaluate the
     *  actions, use {@link #execute(ParserScope)} with an explicitly given
     *  scope.
     *
     *  @return The parser scope.
     */
    protected ParserScope _getParserScope() {
        if (_scopeVersion != _workspace.getVersion()) {
            try {
                _updateParserScope();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }
        return _scope;
    }

    /** Update the parser scope.
     *
     *  @exception IllegalActionException If the format of the parameter list in
     *  the event has errors.
     */
    protected void _updateParserScope() throws IllegalActionException {
        Event event = (Event) getContainer();
        NamedObj eventContainer = event.getContainer();
        if (eventContainer instanceof ERGController) {
            ERGController controller = (ERGController) event.getContainer();
            ParserScope superscope = controller.getPortScope();
            List<?> names = event.parameters.getArgumentNameList();
            Type[] types = event.parameters.getArgumentTypes();
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
        _scopeVersion = _workspace.getVersion();
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

    /** The parser scope used to type-check the actions. */
    private ParserScope _scope;

    /** The version of the workspace when _scope was last updated. */
    private long _scopeVersion = -1;
}

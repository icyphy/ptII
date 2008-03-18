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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Event extends State {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public Event(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute != isInitialState) {
            super.attributeChanged(attribute);
        }
    }

    public void fire() throws IllegalActionException {
        fire(null);
    }

    public void fire(ArrayToken arguments) throws IllegalActionException {
        ((ERGController) getContainer())._debug(this);
        
        List<?> names = parameters.getArgumentNameList();
        int paramCount = names == null ? 0 : names.size();
        int argCount = arguments == null ? 0 : arguments.length();
        if (paramCount != argCount) {
            throw new IllegalActionException(this, "The number of arguments to "
                    + "this event must be equal to the number of declared "
                    + "parameters, which is " + paramCount + ".");
        }

        ERGController controller = (ERGController) getContainer();
        ParserScope scope = controller.getPortScope();

        if (paramCount > 0) {
            Map<String, Token> argumentMap = new HashMap<String, Token>();
            Iterator<?> namesIter = names.iterator();
            Type[] types = parameters.getArgumentTypes();
            for (int i = 0; namesIter.hasNext(); i++) {
                String name = (String) namesIter.next();
                Token argument = arguments.getElement(i);
                if (!types[i].isCompatible(argument.getType())) {
                    throw new IllegalActionException(this, "Argument " + (i + 1)
                            + "must have type " + types[i]);
                }
                argumentMap.put(name, argument);
            }
            scope = new ParametersParserScope(argumentMap, scope);
        }

        actions.execute(scope);

        ERGDirector director = (ERGDirector) controller.getDirector();
        List<?> schedules = nonpreemptiveTransitionList();
        for (Object scheduleObject : schedules) {
            SchedulingRelation schedule = (SchedulingRelation) scheduleObject;
            if (schedule.isEnabled(scope)) {
                double delay = schedule.getDelay(scope);
                Event nextEvent = (Event) schedule.destinationState();
                if (schedule.isCanceling()) {
                    director.cancel(nextEvent);
                } else {
                    ArrayToken edgeArguments = schedule.getArguments(scope);
                    director.fireAt(nextEvent, director.getModelTime().add(
                            delay), edgeArguments);
                }
            }
        }
    }

    public ActionsAttribute actions;

    public ParametersAttribute parameters;

    public class ParametersParserScope implements ParserScope {

        public Token get(String name) throws IllegalActionException {
            if (_argumentMap.containsKey(name)) {
                return _argumentMap.get(name);
            } else {
                return _superscope.get(name);
            }
        }

        public Type getType(String name) throws IllegalActionException {
            return get(name).getType();
        }

        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return _superscope.getTypeTerm(name);
        }

        public Set<?> identifierSet() throws IllegalActionException {
            return _argumentMap.keySet();
        }

        ParametersParserScope(Map<String, Token> argumentMap,
                ParserScope superscope) {
            _argumentMap = argumentMap;
            _superscope = superscope;
        }

        private Map<String, Token> _argumentMap;

        private ParserScope _superscope;

    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
       refinementName.setVisibility(Settable.NONE);
       isInitialState.setDisplayName("isInitialEvent");
       isFinalState.setDisplayName("isFinalEvent");

       parameters = new ParametersAttribute(this, "parameters");
       parameters.setExpression("()");

       actions = new ActionsAttribute(this, "actions");
       Variable variable = new Variable(actions, "_textHeightHint");
       variable.setExpression("5");
       variable.setPersistent(false);
    }
}

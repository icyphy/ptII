/* This director extends FSMDirector by specifying whether a port should
   consume a token in the state transition.

Copyright (c) 2004 The Regents of the University of California.
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

package ptolemy.domains.fsm.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
//import ptolemy.actor.PeekReceiver;
import ptolemy.actor.Receiver;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ExtendedFSMDirector
/**
   This director extends FSMDirector by specifying whether a port should
   consume a token in the state transition. An input port will consume one
   token if it appears in: (1) at least one guard expressions in all the
   transitions that go from the current state, and/or (2) the output actions
   of the enabled transition. If an input port does not appear in the
   above two cases, it will not consume tokens.

   FIXME: The input ports appeared in SetActions of the enabled transition
   should also be included.
   FIXME: Doesn't handle multi-port.
   FIXME: Currently, the port rate is either 1 or 0, but we are going to
   extend it. The current implementation also does not support state
   refinement.

   @author Rachel Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (cxh)
*/


public class ExtendedFSMDirector extends FSMDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ExtendedFSMDirector() {
        super();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ExtendedFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public ExtendedFSMDirector(Workspace workspace) {
        super(workspace);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Get the enabled transition and the referred input ports in its
     *  outputActions. If they are not referred by the guard expressions,
     *  transfer these inputs before the super class fire() is called.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void fire() throws IllegalActionException {
        FSMActor controller = getController();
        controller._readInputs();
        State currentState = controller.currentState();
        Transition enabledTransition = controller._checkTransition(
                currentState.nonpreemptiveTransitionList());
        getOutputActionsReferredInputPorts(enabledTransition);
        CompositeActor container = (CompositeActor)getContainer();
        List inputPortList = container.inputPortList();
        _consumeToken = true;
        for (int i = 0; i < inputPortList.size(); i ++) {
            IOPort port = (IOPort)inputPortList.get(i);
            if (_outputActionReferredInputPorts.contains(port)
                    && !_guardReferredInputPorts.contains(port)) {
                Receiver[][] insideReceivers
                    = (Receiver[][])port.getReceivers();
                for (int k = 0; k < insideReceivers.length; k ++) {
                    for (int j = 0; j < insideReceivers[k].length; j ++) {
                        //((PeekReceiver)insideReceivers[k][j])
                          //  .setToConsume(_consumeToken);
                    }
                }
                super.transferInputs(port);
                controller._readInputs();
            }
        }
        super.fire();
    }

    /** Given a state, get a list of referred input ports in the guard
     *  expressions of all the transitions that go out from this state.
     * @param currentState The given state.
     * @exception IllegalActionException If there is no controller or if
     *  the guard expression is illegal.
     */
    public void getGuardReferredInputPorts(State currentState)
            throws IllegalActionException {
        //System.out.println("currentState" + currentState.getName());
        _guardReferredInputPorts.clear();
        Iterator transitions =
            currentState.nonpreemptiveTransitionList().iterator();
        while (transitions.hasNext()) {
            Transition transition = (Transition)transitions.next();
            //System.out.println("transition = " + transition.getName());
            String string = transition.getGuardExpression();
            if (string == "") {
                throw new IllegalActionException(this,
                        "guard expression on " + transition.getName() +
                        "is null!");
            }
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree = parser.generateParseTree(string);
            ParseTreeFreeVariableCollector variableCollector
                = new ParseTreeFreeVariableCollector();
            FSMActor controller = getController();
            ParserScope scope = controller.getPortScope();
            Set set = variableCollector.collectFreeVariables(parseTree, scope);
            getReferredInputPorts(set, _guardReferredInputPorts);
        }
    }

    /** Given a transition, get a list of referred input ports in the
     *  outputActions of that transition.
     * @param currentState The given state.
     * @exception IllegalActionException If there is no controller or if
     *  the outputActions is illegal.
     */
    public void getOutputActionsReferredInputPorts(Transition transition)
            throws IllegalActionException {
        //System.out.println("get output action referred ports:");
        _outputActionReferredInputPorts.clear();
        String string = transition.outputActions.getExpression();
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree;
        ParseTreeFreeVariableCollector variableCollector
            = new ParseTreeFreeVariableCollector();
        FSMActor controller = getController();
        ParserScope scope = controller.getPortScope();

        if (string != "") {
            Map map = parser.generateAssignmentMap(string);
            Set set = new HashSet();
            for (Iterator names = map.keySet().iterator(); names.hasNext(); ) {
                String name = (String)names.next();
                //System.out.println("contained ports is " + name);
                ASTPtAssignmentNode node = (ASTPtAssignmentNode)map.get(name);
                parseTree = node.getExpressionTree();
                set = variableCollector.collectFreeVariables(parseTree, scope);
                getReferredInputPorts(set, _outputActionReferredInputPorts);
           }
        }
    }

    /** Given a set of ports, get those are input ports and put them
     *  in the indicated referred list.
     * @param set The given set of ports
     * @param referredList The referred list.
     */
    public void getReferredInputPorts(Set set, List referredList) {
        CompositeActor container = (CompositeActor)getContainer();
        List inputPortList = container.inputPortList();
        for (int i = 0; i < inputPortList.size(); i ++) {
            IOPort inputPort = (IOPort)inputPortList.get(i);
            if (set.contains(inputPort.getName())) {
                referredList.add(inputPort);
                //System.out.println("referred input port is " +
                        //inputPort.getName());
            }
        }
    }

    /** Initialize the director. Get the referred input ports
     *  in the guard expressions of all the transitions that go out
     *  from the initial state.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _consumeToken = false;
        FSMActor controller = getController();
        //System.out.println(controller.getInitialState().getName());
        getGuardReferredInputPorts(controller.getInitialState());
    }

    /** Return a peek receiver that is a one-place buffer.
     *  @return A peek receiver that is a one-place buffer.
     */
    public Receiver newReceiver() {
        return super.newReceiver();
        //return new PeekReceiver();
    }

    /** Get the referred input ports in the guard expressions
     *  of all the transitions that go out from the current state.
     *  Then call super class postfire().
     *  @exception IllegalActionException If the super class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        boolean postfireValue = super.postfire();
        FSMActor controller = getController();
        getGuardReferredInputPorts(controller.currentState());
        _consumeToken = false;
        return postfireValue;
    }

    /** Override by super class by only transferring inputs for those
     *  input ports that appear in at least one guard expression
     *  of all the transitions that go out from the current state.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (_guardReferredInputPorts.contains(port)) {
            //System.out.println("This part should not be called");
            //System.out.println("portName = " + port.getName());
            Receiver[][] insideReceivers =
                (Receiver[][])port.getReceivers();
            for (int i = 0; i < insideReceivers.length; i ++) {
                for (int j = 0; j < insideReceivers[i].length; j ++) {
                    //((PeekReceiver)insideReceivers[i][j])
                      //  .setToConsume(_consumeToken);
                }
            }
            return super.transferInputs(port);
        } else {
            //System.out.println("transferInputs always return true");
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ////

    // A flag indicates whether the receiver is a peek one or not.
    private boolean _consumeToken;

    // A list of input ports that appear at least in one guard
    // expression in all the transitions that go from the current state,
    private LinkedList _guardReferredInputPorts = new LinkedList();

    // A list of input ports that appear in the output actions of
    // the enabled transition.
    private LinkedList _outputActionReferredInputPorts = new LinkedList();
}

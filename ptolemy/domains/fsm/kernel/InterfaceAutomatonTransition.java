/* A transition in an Interface Automaton.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

// import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
// import ptolemy.kernel.ComponentRelation;
// import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
// import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Settable;
// import ptolemy.data.Token;
// import ptolemy.data.BooleanToken;
// import ptolemy.data.StringToken;
// import ptolemy.data.expr.Variable;
// import ptolemy.data.expr.Parameter;
// import ptolemy.data.type.BaseType;

// import java.util.List;
// import java.util.Iterator;
// import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// InterfaceAutomatonTransition
/**
A Transition for Interface Automaton. In the Interface Automata paper
writen by Luca de Alfaro, transitions are called actions. However,
the name action conflicts with the Action class in this package, so this
class uses the name transition.  There are three types of transitions:
input, output, and internal. Each transition has a label. The labels for
input, output, and internal transitions end with "?", "!", and ";",
respectively. The type of transition is determined by the ending character
of the label this way. Each input transition corresponds to an input port of
the InterfaceAutomaton that contains it, and its label, excluding the
ending "?", must be the same as the input port name; Similarly, each output
transition corresponds to an output port, and its label, excluding the
ending "!", must be the same as the output port name;  Each internal
transitions corresponds to a parameter of the InterfaceAutomaton that
contains it, and its label, excluding the ending ";", must be the same as
the parameter name.
<p>
The guard of each transition is set automatically. Users should not set
the guard. For an input transition, the guard is set to <inputPort>_isPresent,
where <inputPort> is the port corresponding to this transition; For ouput
and internal transitions, the guard is set to true. Each of the output
and internal transitions contain an Action. The expression of the Action
is also set automatically. For output transition, the action is set to
<outputPort>=true, where <outputPort> is the output port corresponding to
this transition; for internal transition, the action is set to
<parameter>=true, where <parameter> is the parameter corresponding to this
transition.

@author Yuhong Xiong, Xiaojun Liu and Edward A. Lee
@version $Id$
@see InterfaceAutomaton
@see Action
*/

// FIXME: parameters corresponding to internal transitions are not created
// yet.
public class InterfaceAutomatonTransition extends Transition {

    /** Construct a transition with the specified container and name.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown. This transition will use the workspace of the
     *  container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container An InterfaceAutomaton.
     *  @param name The name of this transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public InterfaceAutomatonTransition(InterfaceAutomaton container,
                                        String name)
	    throws IllegalActionException, NameDuplicationException {

        super(container, name);
        label = new StringAttribute(this, "label");
        outputActions.setVisibility(Settable.NONE);
        setActions.setVisibility(Settable.NONE);  
        preemptive.setVisibility(Settable.NONE);
        reset.setVisibility(Settable.NONE);
        guardExpression.setVisibility(Settable.NONE);
	triggerExpression.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The label of this transition. Must ends with "?" or "!" or ";"
     */
    public StringAttribute label = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  <i>label</i>, check the ending character of the label to
     *  determine the transition type. Increment the version number of
     *  the workspace.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the changed attribute is 
     *   <i>label</i> and it does not ends with "?" or "!" or ";".
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == label) {
	    String labelString = label.getExpression();
	    String name = labelString.substring(0, labelString.length()-1);
            if (labelString.endsWith("?")) {
	        setGuardExpression(name + "_isPresent");
		outputActions.setExpression("");
	    } else if (labelString.endsWith("!")) {
	        setGuardExpression("true");
		outputActions.setExpression(name + "=true");
	    } else if (labelString.endsWith(";")) {
	        // FIXME: create(?) set parameter, set action to <param>=true
		setGuardExpression("true");
                outputActions.setExpression("");
	    } else {
	        throw new IllegalArgumentException(
	                "InterfaceAutomatonTransition.setLabel: The argument "
		        + label + " does not end with ? or ! or ;");
	    }
        }
    }

    /** Return the label of this transition. If the label has not been set,
     *  return an empty string.
     *  @return The label of this transition.
     */
    public String getLabel() {
        String labelStr = label.getExpression();
        if (labelStr == null) {
            labelStr = "";
        }
        return labelStr;
    }

    /** Return the transition type. The transition type is one of
     *  INPUT_TRANSITION, OUTPUT_TRANSITION, or INTERNAL_TRANSITION.
     *  @return the transition type.
     */
    public int getTransitionType() {
        String labelString = label.getExpression();
        if (labelString.endsWith("?")) {
	    return INPUT_TRANSITION;
	} else if (labelString.endsWith("!")) {
	    return OUTPUT_TRANSITION;
	} else if (labelString.endsWith(";")) {
	    return INTERNAL_TRANSITION;
	} else {
	    throw new InternalErrorException(
	            "InterfaceAutomatonTransition.getTransitionType: "
		    + "The label does not end with ? or ! or ;");
        }
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of InterfaceAutomaton or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  this transition from its container.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If setting the container would
     *   result in a recursive containment structure, or if
     *   this transition and container are not in the same workspace, or
     *   if the argument is not an InterfaceAutomaton or null.
     *  @exception NameDuplicationException If the container already has
     *   an relation with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof InterfaceAutomaton) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "Transition can only be contained by instances of "
		    + "InterfaceAutomaton.");
        }
        super.setContainer(container);
    }

    /** Throw an exception. Trigger expression is not used in
     *  InterfaceAutomaton, so this method should not be called in this class.
     *  @param expression The trigger expression.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public void setTriggerExpression(String expression) {
        throw new UnsupportedOperationException(
	        "InterfaceAutomatonTransition.setTriggerExpression: "
	        + "The trigger expression is not used in InterfaceAutomaton, "
		+ "so this method should not be called.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    // transition types.
    protected static final int INPUT_TRANSITION = 0;
    protected static final int OUTPUT_TRANSITION = 1;
    protected static final int INTERNAL_TRANSITION = 2;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}

/* A Parameter is a Variable that interacts with listeners.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.TypeLattice;
import collections.LinkedList;
import java.util.Enumeration;
import ptolemy.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
A Parameter is a Variable that interacts with listeners.
<p>
A parameter can be given a token or an expression as its value. The 
expression of a parameter can only reference the parameters added 
to the scope of this parameter. By default, all parameters contained 
by the same NamedObj and those contained by the NamedObj one level up
in the hierarchy (i.e. contained by the container of the container of 
this parameter, if it has one) are added to the scope of this parameter.
<p>
If another object (e.g. of class Parameter) wants to be notified of 
changes in this parameter, it must implement the ParameterListener 
interface and register itself as a listener with this parameter. Since 
tokens are immutable, the value of this parameter only changes when a 
new token is placed in it or its expression is evaluated.

@author Neil Smyth, Xiaojun Liu
@version $Id$

@see ptolemy.data.expr.Variable
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token

*/

public class Parameter extends Variable implements ParameterListener {

    /** Construct a parameter in the default workspace with an empty 
     *  string as its name. The parameter is added to the list of 
     *  objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Parameter() {
        super();
    }

    /** Construct a parameter in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public Parameter(Workspace workspace) {
        super(workspace);
    }

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public Parameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public Parameter(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Register a ParameterListener with this Parameter.
     *  @param newListener The ParameterListener that is will be notified
     *   whenever the value of this Parameter changes.
     */
    public void addParameterListener(ParameterListener newListener) {
        if (_listeners == null) {
            _listeners = new LinkedList();
        }
        if (_listeners.includes(newListener)) {
            return;
        }
        _listeners.insertLast(newListener);
    }

    /** Clone the parameter.
     *  The state of the cloned parameter will be identical to the original
     *  parameter, but without the ParameterListener dependencies set up.
     *  To achieve this evaluate() should be called after cloning the
     *  parameter.  Evaluate() should only be called after all
     *  the parameters on which this parameter depends have been created.
     *  @param The workspace in which to place the cloned Parameter.
     *  @exception CloneNotSupportedException If the parameter
     *   cannot be cloned.
     *  @see java.lang.Object#clone()
     *  @return An identical Parameter.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Parameter newobj = (Parameter)super.clone(ws);
        return newobj;
    }

    /** Evaluate the current expression to a Token. If this parameter
     *  was last set directly with a Token do nothing. This method is also
     *  called after a Parameter is cloned.
     *  <p>
     *  This method is defined by The ParameterListener interface which
     *  all Parameters implement. When a Parameter changes, it calls
     *  this method on all ParameterListeners registered with it. This
     *  method also detects dependency loops between Parameters.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  @exception IllegalArgumentException If the token resulting
     *   from evaluating the expression cannot be stored in this parameter.
     */
    public void evaluate() {
        int eventId = ParameterEvent.UPDATED;
        if (_noEvaluationYet) {
            eventId = ParameterEvent.SET_FROM_EXPRESSION;
        }
        _event = new ParameterEvent(eventId, this);
        super.evaluate();
    }

    /** A Parameter which the expression in this Parameter references
     *  has changed. Here we just call evaluate() to obtain the new
     *  Token to be stored in this Parameter.
     *  @param event The ParameterEvent containing the information
     *    about why the referenced Parameter changed.
     */
    public void parameterChanged(ParameterEvent event) {
        evaluate();
    }

    /** A Parameter which the expression stored in this Parameter
     *  has been removed. Check if the current expression is still
     *  valid by recreating and evaluating the parse tree.
     *  @param event The ParameterEvent containing information
     *    about the removed Parameter.
     */
    public void parameterRemoved(ParameterEvent event) {
        // removeFromScope(event.getParameter());
        // _destroyParseTree();
        // _buildParseTree();
        return;
    }

    /** Unregister a ParameterListener of this Parameter.
     *  @param oldListener The ParameterListener that is will no
     *  longer be notified when the value of this Parameter changes.
     */
    public void removeParameterListener(ParameterListener oldListener) {
        if (_listeners == null) {
            return;
        }
        _listeners.exclude(oldListener);
        return;
    }

    /** Specify the container NamedObj, adding this Parameter to the
     *  list of attributes in the container.  If the specified container
     *  is null, remove this Parameter from the list of attributes of the
     *  NamedObj and also notify all ParameterListeners which are registered
     *  with this Parameter that this Parameter has been removed.
     *  If the container already
     *  contains an parameter with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this parameter, throw an exception.
     *  If this parameter is already contained by the NamedObj, do nothing.
     *  If the parameter already has a container, remove
     *  this attribute from its attribute list of the NamedObj first.
     *  Otherwise, remove it from the directory of the workspace, if it is
     *  there. This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an parameter with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container == null) {
            if (_listeners == null) {
                // No listeners to notify.
                return;
            }
            ParameterEvent event = new ParameterEvent(this);
            Enumeration list = _listeners.elements();
            while (list.hasMoreElements()) {
                ParameterListener next = (ParameterListener)list.nextElement();
                next.parameterRemoved(event);
            }
        }
    }

    /** Put a new Token in this Parameter. This is the way to give the
     *  give the Parameter a new simple value.
     *  If the previous Token in the Parameter was the result of
     *  evaluating an expression, the dependencies registered with
     *  other Parameters for that expression are cleared.
     *  @param token The new Token to be stored in this Parameter.
     *  @exception IllegalArgumentException If the token cannot be placed
     *   in this parameter.
     */
    public void setToken(ptolemy.data.Token token)
            throws IllegalArgumentException {
        _event = new ParameterEvent(ParameterEvent.SET_FROM_TOKEN, this);
        super.setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of this Parameter.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A String describing the Parameter.
     *  FIXME: needs to be finished, how/what is needed to
     *   describe a Parameter.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result = _getIndentPrefix(indent);
            if (bracket == 1 || bracket == 2) result += "{";
            result += toString();
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return true if the argument is legal to be added to the scope
     *  of this variable. In this base class, this method only checks
     *  that the argument is in the same workspace as this variable.
     *  @param var The variable to be checked.
     *  @return True if the argument is legal.
     */
    protected boolean _isLegalInScope(Variable var) {
        if (!(var instanceof Parameter)) {
            return false;
        }
        return (var.workspace() == this.workspace());
    }

    /*  Notify any ParameterListeners that have registered an
     *  interest/dependency in this parameter.
     */
    protected void _notifyValueDependents() {
        super._notifyValueDependents();
        if (_listeners == null) {
            // No listeners to notify.
            return;
        }
        Enumeration list = _listeners.elements();
        while (list.hasMoreElements()) {
            ((ParameterListener)list.nextElement()).parameterChanged(_event);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of ParameterListeners registered with this parameter.
    private LinkedList _listeners = null;

    // The event to notify listeners.
    private ParameterEvent _event;

}





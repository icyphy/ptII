/* A parameter that contains FSM transition attributes.

   Copyright (c) 2014 The Regents of the University of California.
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



package ptolemy.domains.modal.kernel;



import java.util.Collection;

import ptolemy.actor.gui.LocationAttribute;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.ValueIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;


/** A parameter that contains FSM transition attributes. In large FSMs
 *  with long strings in actions, the graphical representation can be
 *  challenging. This parameter can be used represent configuration of
 *  transitions in a parameter that can be moved independent from the 
 *  transition in vergil.
 *  @author Patricia Derler
 *  @version $Id: $
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class FSMTransitionParameter extends AbstractSettableAttribute {

	
	/** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FSMTransitionParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
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
    public FSMTransitionParameter(NamedObj container, String name, Transition transition)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _transition = transition;
        _init();
    }
    
    /** Clone the transition into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer to
     *  the attributes of the new transition.
     *  @param workspace The workspace for the new transition.
     *  @return A new transition.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FSMTransitionParameter newObject = (FSMTransitionParameter) super.clone(workspace);
        newObject._transition = null;
        return newObject;
    }
    
    /** Return the name of the parameter.
     *  @return The name.
     */
    public String getExpression() {
    	if (_transition != null) {
    		return _transition.getFullLabel();
    	} 
    	return "";
    }
    
    /** Set the transition that corresponds to the parameters.
     * @param transition The transition.
     * @throws IllegalActionException Can happen during initialization.
     * @throws NameDuplicationException Can happen during initialization.
     */
    public void setTransition(Transition transition) throws IllegalActionException, NameDuplicationException {
    	_transition = transition;
    	_init();
    }
    
    /** True if annotation was changed via the annotation parameter in the transition.
     */
    public boolean changedAnnotation = false;
    
    /** True if outputActions was changed via the annotation parameter in the transition.
     */
    public boolean changedOutputActions = false;
    
    /** True if setAction was changed via the annotation parameter in the transition.
     */
    public boolean changedSetActions = false;
    
    /** True if guardExpression was changed via the annotation parameter in the transition.
     */
    public boolean changedGuardExpression = false;
    
    /** React to changes in attributes by updating the corresponding attributes
     *  in the transition.
     */
    @Override
    public void attributeChanged(Attribute attribute)
    		throws IllegalActionException { 
    	if (attribute == annotation) {
	    	if (!changedAnnotation) {
	    		_transition.annotation.setExpression(annotation.getExpression());
	    	} 
	    	changedAnnotation = false;
    	} else if (attribute == outputActions) {
    		if (!changedOutputActions) {
	    		_transition.outputActions.setExpression(outputActions.getExpression());
    		} 
    		changedOutputActions = false;
    	} else if (attribute == setActions) {
    		if (!changedSetActions) {
	    		_transition.setActions.setExpression(setActions.getExpression());
    		} 
    		changedSetActions = false;
    	} else if (attribute == guardExpression) {
    		if (!changedGuardExpression) {
    			_transition.guardExpression.setExpression(guardExpression.getExpression());
    		} 
    		changedGuardExpression = false;
    	} else {
    		super.attributeChanged(attribute);
    	}
    }
    
    /** Upon setting the name of this parameter change the corresponding
     *  attribute in the transition.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    		NameDuplicationException {
    	super.setName(name);
    	if (_transition != null) {
    		_transition.fsmTransitionParameterName.setExpression(name);
    		_transition.fsmTransitionParameterName.setPersistent(true);
    	}
    }
    
    /** If hide is true, hide the parameter and display transition parameters next 
     * to the transition.
     * FIXME this should probably be done in setVisibility.
     * @param hide 
     * @throws IllegalActionException
     */
    public void hide(boolean hide) throws IllegalActionException {
    	Parameter _hide = (Parameter) getAttribute("_hide");
        _hide.setExpression("" + hide);
        Location location = (Location) getAttribute("_location");
        location.validate();
        if (!hide && (location == null || (location.getLocation()[0] == 0 && location.getLocation()[1] == 0))) {
        	if (_transition.sourceState() != null) {
	        	Location sourceStateLocation = (Location)_transition.sourceState().getAttribute("_location");
	        	Location destinationStateLocation = (Location)_transition.destinationState().getAttribute("_location");
	        	try {
					new Location(this, "_location").setLocation(new double[]{
							destinationStateLocation.getLocation()[0] + 
							(sourceStateLocation.getLocation()[0] - destinationStateLocation.getLocation()[0])/2,
							destinationStateLocation.getLocation()[1] + 
							(sourceStateLocation.getLocation()[1] - destinationStateLocation.getLocation()[1])/2});
				} catch (NameDuplicationException e) {
					throw new IllegalActionException(_transition, e.getCause(), e.getMessage());
				}
        	}
        }
    }
    
    /** Add value listener. Nothing to do.
     */
    @Override
	public void addValueListener(ValueListener listener) {
		// nothing to do.
	}

    /** Get visibility. Nothing to do.
     */
	@Override
	public Visibility getVisibility() {
		return NOT_EDITABLE;
	}

	/** Remove value listener. Nothing to do.
	 */
	@Override
	public void removeValueListener(ValueListener listener) {
		// nothing to do.
	}

	/** Set visibility. Nothing to do.
	 */
	@Override
	public void setVisibility(Visibility visibility) {
		// nothing to do, visibility is always not-editable.
	}

	/** Validate. Nothing to do.
	 */
	@Override
	public Collection validate() throws IllegalActionException {
		return null;
	}
	
	/** An annotation that describes the transition. If this is non-empty,
     *  then a visual editor will be expected to put this annotation on
     *  or near the transition to document its function. This is a string
     *  that defaults to the empty string. Note that it can reference
     *  variables in scope using the notation $name.
     */
    public StringParameter annotation;

    /** Attribute specifying the guard expression.
     */
    public StringAttribute guardExpression = null;

    /** The action commands that produce outputs when the transition is taken.
     */
    public StringAttribute outputActions;

    /** The action commands that set parameters when the transition is taken.
     *  By default, this is empty.
     */
    public StringAttribute setActions;


	private void _init() throws IllegalActionException, NameDuplicationException  {
		
		_attachText("_iconDescription", "<svg>\n"
	            + "<rect x=\"-10\" y=\"-10\" width=\"5\" height=\"5\" "
	            + "style=\"fill:blue\"/></svg>");
		
		if (getAttribute("_editorFactor") == null) {
			new VisibleParameterEditorFactory(this, "_editorFactor");
		}
		
		SingletonParameter hide = new SingletonParameter(this, "_hideName");
	    hide.setToken(BooleanToken.TRUE);
	    hide.setVisibility(Settable.EXPERT);
		
	    if (getAttribute("_icon") == null) {
	    	ValueIcon icon = new ValueIcon(this, "_icon");
	    	icon.displayWidth.setExpression("1000");
	    	icon.numberOfLines.setExpression("100");
	    }
	    
	    Parameter _hide = (Parameter) getAttribute("_hide");
	    if (_hide == null) {
	    	_hide = new Parameter(this, "_hide");
	    }
	    _hide.setExpression("true");
	    
	    
	    annotation = (StringParameter) getAttribute(_transition.annotation.getName());
	    if (annotation == null) {
	    	annotation = new StringParameter(this, _transition.annotation.getName());
	    	Variable variable = new Variable(annotation, "_textHeightHint");
	        variable.setExpression("4");
	        variable.setPersistent(false);
	    }
		annotation.setExpression(_transition.annotation.getExpression());
		
		outputActions = (StringAttribute) getAttribute(_transition.outputActions.getName());
		if (outputActions == null) {
			outputActions = new StringAttribute(this, _transition.outputActions.getName());
			Variable variable = new Variable(outputActions, "_textHeightHint");
	        variable.setExpression("4");
	        variable.setPersistent(false);
		}
		outputActions.setExpression(_transition.outputActions.getExpression());
		
		setActions = (StringAttribute) getAttribute(_transition.setActions.getName());
		if (setActions == null) {
			setActions = new StringAttribute(this, _transition.setActions.getName());
			Variable variable = new Variable(setActions, "_textHeightHint");
	        variable.setExpression("4");
	        variable.setPersistent(false);
		}
		setActions.setExpression(_transition.setActions.getExpression());
		
		guardExpression = (StringAttribute) getAttribute(_transition.guardExpression.getName());
		if (guardExpression == null) {
			guardExpression = new StringAttribute(this, _transition.guardExpression.getName());
			Variable variable = new Variable(guardExpression, "_textHeightHint");
	        variable.setExpression("4");
	        variable.setPersistent(false);
		}
		guardExpression.setExpression(_transition.guardExpression.getExpression());
	}

	private Transition _transition;
	
}

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
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/** A parameter that contains FSM transition attributes. In large FSMs
 *  with long strings in actions, the graphical representation can be
 *  challenging. This parameter can be used represent configuration of
 *  transitions in a parameter that can be moved independent from the 
 *  transition in vergil.
 *  @author Patricia Derler
 *  @version $Id$
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
    
    public Transition getTransition() {
    	return _transition;
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
        if (_hide == null) {
            try {
                _hide = new Parameter(this, "_hide");
            } catch (NameDuplicationException e) {
                // not going to happen
                e.printStackTrace();
            }
        }
        _hide.setExpression("" + hide);
        Location location = (Location) getAttribute("_location");
        if (location != null) {
        	location.validate();
        }
        if (!hide && (location == null || (location.getLocation()[0] == 0 && location.getLocation()[1] == 0))) {
        	if (_transition != null && _transition.sourceState() != null) {
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
	
	
	@Override
	public void setContainer(NamedObj container) throws IllegalActionException,
	        NameDuplicationException {
	    super.setContainer(container);
	    if (container == null && _transition != null) {
	        _transition.setFsmTransitionParameter(null);
	        _transition.showFSMTransitionParameter.setToken(new BooleanToken(false));
	    }
	}

	/** Set visibility. Nothing to do.
	 */
	@Override
	public void setVisibility(Visibility visibility) {
		// nothing to do, visibility is always not-editable.
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

    /** Validate. Nothing to do.
	 */
	@Override
	public Collection validate() throws IllegalActionException {
		return null;
	}

	private void _init() throws IllegalActionException, NameDuplicationException  {
		if (getAttribute("_hideName") == null) {
			SingletonParameter hide = new SingletonParameter(this, "_hideName");
		    hide.setToken(BooleanToken.TRUE);
		    hide.setVisibility(Settable.EXPERT);
		}
		
		hide(false);
        setPersistent(true);
	    
	    List<Transition> transitions = ((ModalController)getContainer()).relationList();
	    for (Transition transition : transitions) {
	        if (((StringToken)transition.fsmTransitionParameterName.getToken()).stringValue().equals(this.getName())) {
	            _transition = transition;
	            _transition.setFsmTransitionParameter(this);
	            break;
	        }
	    }
	}

	private Transition _transition;
	
}

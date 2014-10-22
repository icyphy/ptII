/* A matcher to match a state in an FSM controller or an event in a Ptera
   controller.

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

package ptolemy.actor.gt;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.domains.modal.kernel.State;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.gt.GTIngredientsEditor;
import ptolemy.vergil.gt.StateMatcherController;

/**
 A matcher to match a state in an FSM controller or an event in a Ptera
 controller.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class StateMatcher extends State implements GTEntity, TypedActor,
        ValueListener {

    /** Construct a state with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This state will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public StateMatcher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.StateMatcher");

        criteria = new GTIngredientsAttribute(this, "criteria");
        criteria.setExpression("");
        criteria.addValueListener(this);

        operations = new GTIngredientsAttribute(this, "operations");
        operations.setExpression("");
        operations.addValueListener(this);

        patternObject = new PatternObjectAttribute(this, "patternObject");
        patternObject.setExpression("");
        patternObject.addValueListener(this);

        editorFactory = new GTIngredientsEditor.Factory(this, "editorFactory");
    }

    /** Do nothing because a state matcher is not supposed to contain any
     *  initializable.
     *
     *  @param initializable The initializable.
     */
    @Override
    public void addInitializable(Initializable initializable) {
    }

    /** Clone the actor into the specified workspace. Set a type
     *  constraint that the output type is the same as the that of input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StateMatcher newObject = (StateMatcher) super.clone(workspace);
        newObject._causalityInterface = null;
        newObject._causalityInterfaceDirector = null;
        newObject._labelSet = null;
        newObject._version = -1;
        return newObject;
    }

    /** Create receivers for all necessary ports.
     *  In this implementation no receivers will be created
     *  @exception IllegalActionException If any port throws it.
     */
    @Override
    public void createReceivers() throws IllegalActionException {
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void fire() throws IllegalActionException {
    }

    /** Return a causality interface for this actor. In this base class,
     *  if there is a director, we delegate to the director to return
     *  a default causality interface. Otherwise, we return an instance
     *  of CausalityInterface with BooleanDependency.OTIMES_IDENTITY as
     *  its default. This declares the dependency between input ports
     *  and output ports of an actor to be true, the multiplicative identity.
     *  If this is called multiple times, the same object is returned each
     *  time unless the director has changed since the last call, in
     *  which case a new object is returned.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        Director director = getDirector();
        if (_causalityInterface != null
                && _causalityInterfaceDirector == director) {
            return _causalityInterface;
        }
        Dependency defaultDependency = BooleanDependency.OTIMES_IDENTITY;
        if (director != null) {
            defaultDependency = director.defaultDependency();
        }
        _causalityInterface = new DefaultCausalityInterface(this,
                defaultDependency);
        _causalityInterfaceDirector = director;
        return _causalityInterface;
    }

    /** Return the attribute that stores all the criteria for this matcher.
     *
     *  @return The attribute that stores all the criteria.
     */
    @Override
    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    /** Return null.
     *
     *  @return null.
     */
    @Override
    public String getDefaultIconDescription() {
        return null;
    }

    /** Return null.
     *
     *  @return null.
     */
    @Override
    public Director getDirector() {
        return null;
    }

    /** Return null.
     *
     *  @return null.
     */
    @Override
    public Director getExecutiveDirector() {
        return null;
    }

    /** Return null.
     *
     *  @return null.
     */
    @Override
    public Manager getManager() {
        return null;
    }

    /** Return the attribute that stores all the operations for this matcher.
     *
     *  @return The attribute that stores all the operations.
     */
    @Override
    public GTIngredientsAttribute getOperationsAttribute() {
        return operations;
    }

    /** Return the attribute that stores the name of the corresponding entity in
     *  the pattern of the same {@link TransformationRule}, if this entity is in
     *  the replacement, or <tt>null</tt> otherwise.
     *
     *  @return The attribute that stores the name of the corresponding entity.
     *  @see #labelSet()
     */
    @Override
    public PatternObjectAttribute getPatternObjectAttribute() {
        return patternObject;
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void initialize() throws IllegalActionException {
    }

    /** Return an empty list.
     *
     *  @return An empty list.
     */
    @Override
    public List<?> inputPortList() {
        return _EMPTY_LIST;
    }

    /** Return false because backward type inference is not implemented
     *  for this actor.
     *  @return false
     */
    @Override
    public boolean isBackwardTypeInferenceEnabled() {
        return false;
    }

    /** Return true because prefire and fire do nothing.
     *
     *  @return true.
     */
    @Override
    public boolean isFireFunctional() {
        return true;
    }

    /** Return false.
     *
     *  @return false.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Do nothing and return 0.
     *
     *  @param count The number of iteration.
     *  @return 0
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        return 0;
    }

    /** Return the set of names of ingredients contained in this entity that can
     *  be resolved.
     *
     *  @return The set of names.
     */
    @Override
    public Set<String> labelSet() {
        long version = workspace().getVersion();
        if (_labelSet == null || version > _version) {
            _labelSet = new HashSet<String>();
            try {
                int i = 0;
                for (GTIngredient ingredient : criteria.getIngredientList()) {
                    i++;
                    Criterion criterion = (Criterion) ingredient;
                    if (criterion instanceof AttributeCriterion) {
                        _labelSet.add("criterion" + i);
                    }
                }
            } catch (MalformedStringException e) {
                return _labelSet;
            }
        }
        return _labelSet;
    }

    /** Return true if the given object is an instance of State (either the one
     *  in the deprecated FSM domain or the one in modal model since Ptolemy
     *  8.0).
     *
     *  @param object The object to be tested.
     *  @return true if the object is an instance of State.
     */
    @Override
    public boolean match(NamedObj object) {
        return object instanceof State
                || object.getClass().getName()
                        .equals("ptolemy.domains.modal.kernel.State");
    }

    /** Return null.
     *
     *  @return null.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public Receiver newReceiver() throws IllegalActionException {
        return null;
    }

    /** Return an empty list.
     *
     *  @return An empty list.
     */
    @Override
    public List<?> outputPortList() {
        return _EMPTY_LIST;
    }

    /** Do nothing and return false.
     *
     *  @return false
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /** Do nothing and return false.
     *
     *  @return false
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return false;
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
    }

    /** Do nothing.
     *
     *  @param initializable The initializable.
     */
    @Override
    public void removeInitializable(Initializable initializable) {
    }

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.  If this entity is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        Attribute factory = getAttribute("_controllerFactory");
        if (container == null
                || !(container.getContainer() instanceof ModalModelMatcher)) {
            if (factory != null) {
                factory.setContainer(null);
            }
        } else {
            if (factory == null) {
                new StateMatcherController.Factory(this, "_controllerFactory");
            }
        }
    }

    /** Do nothing.
     */
    @Override
    public void stop() {
    }

    /** Do nothing.
     */
    @Override
    public void stopFire() {
    }

    /** Do nothing.
     */
    @Override
    public void terminate() {
    }

    /** Return an empty list.
     *
     *  @return An empty list.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public Set<Inequality> typeConstraints() throws IllegalActionException {
        return _EMPTY_SET;
    }

    /** Update appearance of this entity.
     *
     *  @param attribute The attribute containing ingredients of this entity.
     *  @see GTEntityUtils#updateAppearance(GTEntity, GTIngredientsAttribute)
     */
    @Override
    public void updateAppearance(GTIngredientsAttribute attribute) {
        // GTEntityUtils.updateAppearance(this, attribute);
    }

    /** React to the fact that the specified Settable has changed.
     *
     *  @param settable The object that has changed value.
     *  @see GTEntityUtils#valueChanged(GTEntity, Settable)
     */
    @Override
    public void valueChanged(Settable settable) {
        GTEntityUtils.valueChanged(this, settable);
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
    }

    /** The attribute containing all the criteria in a list
     *  ({@link GTIngredientList}).
     */
    public GTIngredientsAttribute criteria;

    /** The editor factory for ingredients in this matcher.
     */
    public GTIngredientsEditor.Factory editorFactory;

    /** The attribute containing all the operations in a list
     *  ({@link GTIngredientList}).
     */
    public GTIngredientsAttribute operations;

    /** The attribute that specifies the name of the corresponding entity in the
     *  pattern.
     */
    public PatternObjectAttribute patternObject;

    /** An empty list.
     */
    private static final List<?> _EMPTY_LIST = new LinkedList<Object>();

    /** An empty set.
     */
    private static final Set<Inequality> _EMPTY_SET = new HashSet<Inequality>();

    /** The causality interface, if it has been created. */
    private CausalityInterface _causalityInterface;

    /** The director for which the causality interface was created. */
    private Director _causalityInterfaceDirector;

    /** Cache of the label set.
     */
    private Set<String> _labelSet;

    /** The workspace version the last time when _labelSet was updated.
     */
    private long _version = -1;
}

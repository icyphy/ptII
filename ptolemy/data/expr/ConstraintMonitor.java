/* A decorator attribute that monitors decorator values to enforce constraints.

 Copyright (c) 2000-2013 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.HierarchyListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// ConstraintMonitor

/**
 A contract monitor that decorates each entity in a model with a
 <i>value</i> parameter and monitors the sum of all such values.
 If the sum of the values equals or exceeds the value given in
 {@link #threshold} and {@link #warningEnabled} is true, then this
 class will issue a warning when the aggregate value matches or
 exceeds the threshold.
 The decorator values default to 0.0, so the default total
 is 0.0. The default threshold is Infinity, so no warnings will
 be issued by default.
 <p>
 If the {@link #includeOpaqueContents} parameter is true, then this decorator will
 also decorate entities within opaque composite actors. By default,
 this is false.
 <p>
 This object is a {@link Parameter} whose value is the total
 of the values of all the decorator values.  To use it, simply
 drag it into a model.
 <p>
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ConstraintMonitor extends Parameter implements Decorator {

    /** Construct an instance in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container The container, which contains the objects to be decorated
     *  @param name Name of this constraint monitor.
     *  @exception IllegalActionException If this object is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container or if there is a name duplication during
     *   initialization.
     */
    public ConstraintMonitor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(BaseType.DOUBLE);
        setExpression("0.0");
        setVisibility(Settable.NOT_EDITABLE);

        threshold = new Parameter(this, "threshold");
        threshold.setTypeEquals(BaseType.DOUBLE);
        threshold.setExpression("Infinity");

        warningEnabled = new Parameter(this, "warningEnabled");
        warningEnabled.setExpression("true");
        warningEnabled.setTypeEquals(BaseType.BOOLEAN);

        includeOpaqueContents = new Parameter(this, "includeOpaqueContents");
        includeOpaqueContents.setExpression("false");
        includeOpaqueContents.setTypeEquals(BaseType.BOOLEAN);

        includeTransparents = new Parameter(this, "includeTransparents");
        includeTransparents.setExpression("false");
        includeTransparents.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, then this decorator decorates entities within
     *  opaque composite actors. This is a boolean that defaults to
     *  false.
     */
    public Parameter includeOpaqueContents;

    /** If true, then this decorator decorates transparent composite
     *  entities. This is a boolean that defaults to false.
     */
    public Parameter includeTransparents;

    /** Threshold at which this monitor issues a warning, if
     *  {@link #warningEnabled} is true.
     *  This is a double that defaults to Infinity, meaning no
     *  constraint on the sum of the decorator values.
     */
    public Parameter threshold;

    /** If true (the default), then a warning is issued when the
     *  aggregate value equals or exceeds the specified {@link #threshold}.
     *  This is a boolean that defaults to true.
     */
    public Parameter warningEnabled;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to invalidate if parameters have changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == includeOpaqueContents
                || attribute == includeTransparents) {
            invalidate();
        }
        super.attributeChanged(attribute);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ConstraintMonitor newObject = (ConstraintMonitor) super
                .clone(workspace);
        newObject._lastValueWarning = null;
        newObject._decoratedObjects = null;
        newObject._decoratedObjectsVersion = -1L;
        return newObject;
    }

    /** Return the decorated attributes for the target NamedObj, or null
     *  if the target is not decorated by this decorator.
     *  Specification, every Entity that is not a class definition
     *  that is contained (deeply) by the container of this decorator
     *  is decorated. This includes atomic entities and both opaque and
     *  transparent composite entities.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj.
     *  @exception IllegalActionException If some object cannot be determined to
     *   be decorated or not (e.g., a parameter cannot be evaluated).
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target)
            throws IllegalActionException {
        boolean transparents = ((BooleanToken) includeTransparents.getToken())
                .booleanValue();
        boolean opaques = ((BooleanToken) includeOpaqueContents.getToken())
                .booleanValue();
        NamedObj container = getContainer();
        // If the container of this decorator is not a CompositeEntity,
        // then it cannot possibly deeply contain the target.
        if (!(container instanceof CompositeEntity)) {
            return null;
        }
        if (target instanceof Entity
                && !((Entity) target).isClassDefinition()
                && (transparents || !(target instanceof CompositeEntity) || ((CompositeEntity) target)
                        .isOpaque())
                && _deepContains((CompositeEntity) container, (Entity) target,
                        opaques)) {
            try {
                return new ConstraintMonitorAttributes(target, this);
            } catch (NameDuplicationException e) {
                // This should not occur.
                throw new InternalErrorException(e);
            }
        } else {
            return null;
        }
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler. This includes all entities, whether
     *  transparent or opaque, including those inside opaque entities.
     *  It does not include entities that are class definitions.
     *  @return A list of the objects decorated by this decorator.
     *  @exception IllegalActionException If some object cannot be determined to
     *   be decorated or not (e.g., a parameter cannot be evaluated).
     */
    @Override
    public List<NamedObj> decoratedObjects() throws IllegalActionException {
        if (workspace().getVersion() == _decoratedObjectsVersion) {
            return _decoratedObjects;
        }
        boolean transparents = ((BooleanToken) includeTransparents.getToken())
                .booleanValue();
        boolean opaques = ((BooleanToken) includeOpaqueContents.getToken())
                .booleanValue();

        CompositeEntity container = (CompositeEntity) getContainer();

        _decoratedObjectsVersion = workspace().getVersion();

        // Do the easy case (the default case) first.
        if (!transparents && !opaques) {
            _decoratedObjects = container.deepEntityList();
            return _decoratedObjects;
        }

        // Now the more complex case.
        _decoratedObjects = new LinkedList<NamedObj>();
        _addAllContainedEntities(container, _decoratedObjects, transparents,
                opaques);
        return _decoratedObjects;
    }

    /** Return the value of {@link #includeOpaqueContents}.
     *  decorate objects across opaque hierarchy boundaries.
     *  @exception IllegalActionException If there is a problem
     *  getting the boolean valu from the includeOpaqueContents token.
     */
    @Override
    public boolean isGlobalDecorator() throws IllegalActionException {
        boolean opaques = ((BooleanToken) includeOpaqueContents.getToken())
                .booleanValue();
        return opaques;
    }

    /** Override the base class to check whether the threshold constraint
     *  is satisfied.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    @Override
    public Token getToken() throws IllegalActionException {
        Token result = super.getToken();
        // Check to see whether we need to report errors.
        // Avoid duplicate notices.
        boolean enabled = ((BooleanToken) warningEnabled.getToken())
                .booleanValue();
        if (!result.equals(_lastValueWarning)) {
            if (enabled) {
                double thresholdValue = ((DoubleToken) threshold.getToken())
                        .doubleValue();
                double aggregateValue = ((DoubleToken) result).doubleValue();
                if (aggregateValue >= thresholdValue) {
                    _lastValueWarning = result;
                    String message = "WARNING: "
                            + getName()
                            + " constraint monitor: Aggregate value of "
                            + aggregateValue
                            + ((aggregateValue == thresholdValue) ? " hits "
                                    : " exceeds ") + "threshold of "
                            + threshold + ".";
                    MessageHandler.message(message);
                } else {
                    // No warning.
                    _lastValueWarning = null;
                }
            } else {
                // No warning requested.
                _lastValueWarning = null;
            }
        }
        return result;
    }

    /** Override the base class to mark this as needing evaluation even though
     *  there is no expression.
     */
    @Override
    public synchronized void invalidate() {
        super.invalidate();
        _needsEvaluation = true;
    }

    /** Override the base class to establish a connection with any
     *  decorated objects it finds in scope in the container.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        List<NamedObj> decoratedObjects = decoratedObjects();
        for (NamedObj decoratedObject : decoratedObjects) {
            // The following will create the DecoratorAttributes if it does not
            // already exist, and associate it with this decorator.
            ConstraintMonitorAttributes decoratorAttributes = (ConstraintMonitorAttributes) decoratedObject
                    .getDecoratorAttributes(this);
            decoratorAttributes.value.addValueListener(this);
        }
        return super.validate();
    }

    /** Override the base class to mark that evaluation is needed regardless
     *  of the current expression.
     *  @param settable The object that has changed value.
     */
    @Override
    public void valueChanged(Settable settable) {
        if (!_needsEvaluation) {
            _needsEvaluation = true;
            _notifyValueListeners();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add to the specified list all contained entities of the specified container
     *  that are not class definitions. This includes all the entities
     *  returned by {@link CompositeEntity#deepEntityList()}.
     *  If {@link #includeTransparents} is true, then it also
     *  includes transparent composite entities.
     *  If {@link #includeOpaqueContents} is true, then it also
     *  includes the entities contained within opaque composite entities.
     *  @param container The container of the entities.
     *  @param result The list to which to add the entities.
     *  @param transparents Specification of whether to include transparent
     *   composite entities.
     *  @param opaques Specification of whether to include the
     *   contents of opaque composite entities.
     */
    protected void _addAllContainedEntities(CompositeEntity container,
            List<NamedObj> result, boolean transparents, boolean opaques) {
        try {
            _workspace.getReadAccess();
            List<Entity> entities = container.entityList();
            for (Entity entity : entities) {
                boolean isComposite = entity instanceof CompositeEntity;
                if (!isComposite || ((CompositeEntity) entity).isOpaque()
                        || transparents) {
                    result.add(entity);
                }
                if (isComposite
                        && (!((CompositeEntity) entity).isOpaque() || opaques)) {
                    _addAllContainedEntities((CompositeEntity) entity, result,
                            transparents, opaques);
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the specified target is deeply contained by the specified container
     *  subject to the constraints given by the opaques parameter.
     *  @param container The container.
     *  @param target The object that may be contained by the container.
     *  @param opaques If true, then allow one or more intervening opaque composite actors
     *   in the hierarchy.
     *  @return True if the specified target is deeply contained by the container
     */
    protected boolean _deepContains(CompositeEntity container, Entity target,
            boolean opaques) {
        try {
            _workspace.getReadAccess();
            if (target != null) {
                CompositeEntity targetContainer = (CompositeEntity) target
                        .getContainer();
                while (targetContainer != null) {
                    if (targetContainer == container) {
                        return true;
                    }
                    if (!opaques && targetContainer.isOpaque()) {
                        return false;
                    }
                    targetContainer = (CompositeEntity) targetContainer
                            .getContainer();
                }
            }
            return false;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Evaluate the current expression to a token, which in this case means
     *  to sum the values of all the decorated objects.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if a dependency loop is found.
     */
    @Override
    protected void _evaluate() throws IllegalActionException {
        double result = 0.0;
        List<NamedObj> decoratedObjects = decoratedObjects();
        for (NamedObj decoratedObject : decoratedObjects) {
            Parameter value = (Parameter) decoratedObject
                    .getDecoratorAttribute(this, "value");
            result += ((DoubleToken) value.getToken()).doubleValue();
        }
        setToken(new DoubleToken(result));
        _needsEvaluation = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private field                     ////

    /** Cached list of decorated objects. */
    private List<NamedObj> _decoratedObjects;

    /** Version for _decoratedObjects. */
    private long _decoratedObjectsVersion = -1L;

    /** To avoid duplicate warnings, when we issue a warning, record the value. */
    private Token _lastValueWarning = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Class containing the decorator attributes that decorate objects.
     *  In this case, there is exactly one decorator attribute called <i>value</i>.
     */
    static public class ConstraintMonitorAttributes extends DecoratorAttributes
            implements HierarchyListener {

        public ConstraintMonitorAttributes(NamedObj container,
                ConstraintMonitor decorator) throws IllegalActionException,
                NameDuplicationException {
            super(container, decorator);
            _init();
            value.addValueListener(decorator);
            container.addHierarchyListener(this);
        }

        public ConstraintMonitorAttributes(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            _init();
            container.addHierarchyListener(this);
        }

        public Parameter value;

        /** Override the base class so that if the decorator already exists in
         *  scope, the decorator becomes a value listener to the value attribute.
         *  @exception IllegalActionException If the change is not acceptable
         *   to this container (not thrown in this base class).
         */
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            // The following will establish a link to my decorator if it exists.
            super.attributeChanged(attribute);
            if (_decorator != null) {
                value.addValueListener((ConstraintMonitor) _decorator);
            } else {
                super.attributeChanged(attribute);
            }
        }

        /** Notify this object that the containment hierarchy above it has changed.
         *  @exception IllegalActionException If the change is not
         *   acceptable.
         */
        @Override
        public void hierarchyChanged() throws IllegalActionException {
            ConstraintMonitor decorator = (ConstraintMonitor) getDecorator();
            if (_previousDecorator != null && decorator != _previousDecorator) {
                // Force an evaluation of the decorator, since this attribute
                // may no longer be in scope.
                _previousDecorator.invalidate();
            }
        }

        /** Record the current decorator.
         *  @exception IllegalActionException If the change is not acceptable.
         */
        @Override
        public void hierarchyWillChange() throws IllegalActionException {
            _previousDecorator = (ConstraintMonitor) getDecorator();
        }

        private ConstraintMonitor _previousDecorator = null;

        private void _init() throws IllegalActionException,
                NameDuplicationException {
            value = new Parameter(this, "value");
            value.setTypeEquals(BaseType.DOUBLE);
            value.setExpression("0.0");
        }
    }
}

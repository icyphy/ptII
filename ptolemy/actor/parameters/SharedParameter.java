/* A parameter that is shared globally in a model.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.parameters;

import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.ApplicationConfigurer;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SharedParameter

/**
 This parameter is shared throughout a model. Changing the expression of
 any one instance of the parameter will result in all instances that
 are shared being changed to the same expression.  An instance elsewhere
 in the model (within the same top level) is shared if it has the
 same name and its container is of the class specified in the
 constructor (or of the container class, if no class is specified
 in the constructor). Note that two parameters with the same
 expression do not necessarily have the same value, since the
 expression may reference other parameters that are in scope.
 <p>
 One exception is that if this parameter is (deeply) within an
 instance of EntityLibrary, then the parameter is not shared.
 Were this not the case, then opening a library containing this
 parameter would force expansion of all the sublibraries of
 EntityLibrary, which would defeat the lazy instantiation
 of EntityLibrary.
 <p>
 When this parameter is constructed, the specified container
 will be used to infer the parameter value from the container.
 That is, if the container is within a model that has any
 parameters shared with this one, then the value will be
 set to the last of those encountered.
 If the container is subsequently changed, it is up to the
 code implementing the change to use the inferValueFromContext()
 method to reset the value to match the new context.
 Note that this really needs to be done if the container
 of the container, or its container, or any container
 above this parameter is changed.  It is recommended to use
 the four-argument constructor, so you can specify a default
 value to use if there are no shared parameters.
 <p>
 Note that it might be tempting to use a static parameter field
 to achieve this effect, but this would be problematic for two
 reasons. First, the parameter would only be able to have one
 container. Second, the parameter would be shared across all
 models in the same Java virtual machine, not just within a
 single model.

 @author Edward A. Lee, contributor: Christopher Brooks, Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (acataldo)
 */
public class SharedParameter extends Parameter implements Initializable {
    /** Construct a parameter with the given container and name.
     *  The container class will be used to determine which other
     *  instances of SharedParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public SharedParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null, "");
    }

    /** Construct a parameter with the given container, name, and
     *  container class. The specified class will be used to determine
     *  which other instances of SharedParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public SharedParameter(NamedObj container, String name,
            Class<?> containerClass) throws IllegalActionException,
            NameDuplicationException {
        this(container, name, containerClass, "");
    }

    /** Construct a parameter with the given container, name,
     *  container class, and default value.  This is the preferred
     *  constructor to use.
     *  The specified class will be used to determine
     *  which other instances of SharedParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *   An argument of null means simply to use the class of the container,
     *   whatever that happens to be.
     *  @param defaultValue The default value to use if the container's
     *   model has no shared parameters.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container, or an empty string to specify no
     *   default value.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public SharedParameter(NamedObj container, String name,
            Class<?> containerClass, String defaultValue)
                    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        if (containerClass == null) {
            containerClass = container.getClass();
        }
        if (_delayValidation) {
            _suppressingPropagation = true;
            setLazy(true);
        }
        _containerClass = containerClass;
        inferValueFromContext(defaultValue);
        _constructionFinished = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** Override the base class to register the object, since setName()
     *  will not be called.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     *  @see #setDeferringChangeRequests(boolean)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SharedParameter newObject = (SharedParameter) super.clone(workspace);
        SharedParameterRegistry registry = _getSharedParameterRegistry(workspace);
        registry.register(newObject);
        newObject._sharedParameterSet = null;
        return newObject;
    }

    /** Get the token contained by this variable.  The type of the returned
     *  token is always that returned by getType().  Calling this method
     *  will trigger evaluation of the expression, if the value has been
     *  given by setExpression(). Notice the evaluation of the expression
     *  can trigger an exception if the expression is not valid, or if the
     *  result of the expression violates type constraints specified by
     *  setTypeEquals() or setTypeAtMost(), or if the result of the expression
     *  is null and there are other variables that depend on this one.
     *  The returned value will be null if neither an expression nor a
     *  token has been set, or either has been set to null.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     *  @see #setToken(Token)
     */
    @Override
    public Token getToken() throws IllegalActionException {
        if (_delayValidation) {
            boolean previousSuppressing = _suppressingPropagation;
            try {
                _suppressingPropagation = false;
                validate();
                return super.getToken();
            } finally {
                _suppressingPropagation = previousSuppressing;
            }
        } else {
            return super.getToken();
        }
    }

    /** Return the top level of the containment hierarchy, unless
     *  one of the containers is an instance of EntityLibrary,
     *  in which case, return null.
     *  @return The top level, or null if this is within a library.
     */
    public NamedObj getRoot() {
        NamedObj result = this;

        while (result.getContainer() != null) {
            result = result.getContainer();
            if (result instanceof ApplicationConfigurer) {
                // If the results is a Configuration, then go no higher.
                // If we do go higher, then we end up expanding the actor
                // library tree which take a long time and fails if
                // not all the actors are present.  For example, not
                // everyone will have Matlab or quicktime.
                return null;
            }
        }

        return result;
    }

    /** Infer the value of this parameter from the container
     *  context. That is, search for parameters that are
     *  shared with this one, and set the value of this parameter
     *  to match the first one encountered.
     *  If there are no shared parameters, then assign the
     *  default value given as an argument.
     *  @param defaultValue The default parameter value to use.
     */
    public void inferValueFromContext(String defaultValue) {
        SharedParameter candidate = null;
        NamedObj toplevel = getRoot();

        if (toplevel != null && toplevel != this) {
            candidate = _getOneSharedParameter(toplevel);
        }
        if (candidate != null) {
            defaultValue = candidate.getExpression();
        }
        boolean previousSuppressing = _suppressingPropagation;
        try {
            _suppressingPropagation = true;

            setExpression(defaultValue);

            // Try getting the evaluated token.
            if (candidate != null && candidate.isKnown()) {
                setToken(candidate.getToken());
            }

        } catch (IllegalActionException e) {
            // The token is not set when this happens.

        } finally {
            _suppressingPropagation = previousSuppressing;
        }
    }

    /** Do nothing except invoke the initialize methods
     *  of objects that have been added using addInitializable().
     *  @exception IllegalActionException If one of the added objects
     *   throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    /** Return true.
     *  @return True.
     */
    public boolean isFireFunctional() {
        return true;
    }

    /** Return false.
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Return true if this instance is suppressing propagation.
     *  Unless setSuppressingPropagation() has been called, this
     *  returns false.
     *  @return Returns whether this instance is suppressing propagation.
     *  @see #setSuppressingPropagation(boolean)
     */
    public boolean isSuppressingPropagation() {
        return _suppressingPropagation;
    }

    /** Traverse the model and update values.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
        if (_delayValidation) {
            _suppressingPropagation = false;
            validate();
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Override the base class to register as an initializable slave with the
     *  new container. This results in the preinitialize(), initialize(), and
     *  wrapup() methods of this instance being invoked when the corresponding
     *  method of the container are invoked.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        NamedObj previousContainer = getContainer();
        if (previousContainer != container) {
            if (previousContainer instanceof Initializable) {
                ((Initializable) previousContainer).removeInitializable(this);
            }
            if (container instanceof Initializable) {
                ((Initializable) container).addInitializable(this);
            }
        }
        super.setContainer(container);
    }

    /** Override the base class to register as a shared parameter in the workspace.
     *  @param name The proposed name.
     *  @exception IllegalActionException If the name contains a period
     *   or if this variable is referenced in some other expression.
     *  @exception NameDuplicationException If there is already an
     *       attribute with the same name in the container.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        if (name != null && !name.equals(getName())) {
            SharedParameterRegistry registry = _getSharedParameterRegistry(workspace());
            // Unregister under previous name.
            if (getName() != null && !getName().equals("")) {
                registry.unregister(this);
            }
            super.setName(name);
            registry.register(this);
        }
    }

    /** Override the base class to also set the expression of shared
     *  parameters.
     *  @param expression The expression.
     */
    @Override
    public void setExpression(String expression) {
        // The expression may have already been inferred from context,
        // in which case we don't want to set it again. This prevents
        // spurious replication of the parameter in the MoML file.
        if (expression != null && expression.equals(getExpression())) {
            return;
        }
        super.setExpression(expression);

        if (!_suppressingPropagation) {
            Iterator<SharedParameter> sharedParameters = sharedParameterSet()
                    .iterator();

            while (sharedParameters.hasNext()) {
                SharedParameter sharedParameter = sharedParameters.next();

                if (sharedParameter != this) {
                    try {
                        sharedParameter._suppressingPropagation = true;

                        if (!sharedParameter.getExpression().equals(expression)) {
                            sharedParameter.setExpression(expression);
                        }
                    } finally {
                        sharedParameter._suppressingPropagation = false;
                    }
                }
            }
        }
    }

    /** Specify whether this instance should be suppressing
     *  propagation. If this is called with value true, then
     *  changes to this parameter will not propagate to other
     *  shared instances in the model.
     *  @param propagation True to suppress propagation.
     *  @see #isSuppressingPropagation()
     */
    public void setSuppressingPropagation(boolean propagation) {
        _suppressingPropagation = propagation;
    }

    /** Override the base class to also set the token of shared
     *  parameters.
     *  @param token The token.
     *  @exception IllegalActionException Thrown if super class throws it.
     *  @see #getToken()
     */
    @Override
    public void setToken(Token token) throws IllegalActionException {

        super.setToken(token);

        if (!_suppressingPropagation) {
            Iterator<SharedParameter> sharedParameters = sharedParameterSet()
                    .iterator();

            while (sharedParameters.hasNext()) {
                SharedParameter sharedParameter = sharedParameters.next();

                if (sharedParameter != this) {
                    try {
                        sharedParameter._suppressingPropagation = true;
                        sharedParameter.setToken(token);
                    } finally {
                        sharedParameter._suppressingPropagation = false;
                    }
                }
            }
        }
    }

    /** Return a collection of all the shared parameters within the
     *  same model as this parameter.  If there are no such parameters
     *  or if this parameter is deeply contained within an EntityLibrary, then
     *  return an empty collection. Otherwise, the list will include this
     *  instance if this instance has a container. If this instance has
     *  no container, then return an empty collection.
     *  A shared parameter is one that is an instance of SharedParameter,
     *  has the same name as this one, and is contained by the container
     *  class specified in the constructor.
     *  @return A collection of parameters.
     */
    public synchronized Collection sharedParameterSet() {
        if (workspace().getVersion() != _sharedParameterSetVersion) {
            try {
                workspace().getReadAccess();
                _sharedParameterSet = new HashSet<SharedParameter>();
                _sharedParameterSetVersion = workspace().getVersion();
                NamedObj root = getRoot();
                if (root != null) {
                    SharedParameterRegistry registry = _getSharedParameterRegistry(workspace());
                    for (WeakReference<SharedParameter> reference : registry
                            .getSharedParametersWithName(getName())) {
                        if (reference != null) {
                            SharedParameter parameter = reference.get();
                            if (parameter != null) {
                                // Have a candidate. See if the roots match and if
                                // the container classes match.
                                if (parameter.getRoot() == root
                                        && parameter._containerClass == _containerClass) {
                                    _sharedParameterSet.add(parameter);
                                }
                            }
                        }
                    }
                }
            } finally {
                workspace().doneReading();
            }
        }
        return _sharedParameterSet;
    }

    /** Supress propagation.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
        if (_delayValidation) {
            _suppressingPropagation = true;
        }
    }

    /** Override the base class to also validate the shared instances.
     *  @return A Collection of all the shared parameters within the same
     *   model as this parameter, see {@link #sharedParameterSet}.
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        Collection result = super.validate();
        if (result == null) {
            result = new HashSet();
        }

        // NOTE: This is called by setContainer(), which is called from
        // within a base class constructor. That call occurs before this
        // object has been fully constructed. It doesn't make sense at
        // that time to propagate validation to shared instances, since
        // in fact the value of this shared parameter will be inferred
        // from those instances if there are any. So in that case, we
        // just return.
        if (!_constructionFinished) {
            return result;
        }

        if (!_suppressingPropagation) {
            Iterator<SharedParameter> sharedParameters = sharedParameterSet()
                    .iterator();
            while (sharedParameters.hasNext()) {
                SharedParameter sharedParameter = sharedParameters.next();
                if (sharedParameter != this) {
                    try {
                        sharedParameter._suppressingPropagation = true;
                        result.addAll(sharedParameter.validate());
                        result.add(sharedParameter);
                    } finally {
                        sharedParameter._suppressingPropagation = false;
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to do the propagation only if
     *  the specified destination is not shared, because if
     *  it is shared, then the value will be propagated
     *  in through the sharing mechanism.
     *  @param destination Object to which to propagate the
     *   value.
     *  @exception IllegalActionException If the value cannot
     *   be propagated.
     */
    @Override
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
        if (!sharedParameterSet().contains(destination)) {
            super._propagateValue(destination);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find and return one shared parameter deeply contained by
     *  the specified container.  If there is no such parameter, then
     *  return null.
     *  A shared parameter is one that is an instance of SharedParameter,
     *  has the same name as this one, and is contained by the container
     *  class specified in the constructor.
     *  @param container The container.
     *  @return A shared parameter different from this one, or null if
     *   there is none.
     */
    private SharedParameter _getOneSharedParameter(NamedObj container) {
        if (workspace().getVersion() != _sharedParameterVersion) {
            try {
                workspace().getReadAccess();
                _sharedParameter = null;
                _sharedParameterVersion = workspace().getVersion();
                NamedObj root = getRoot();
                if (root != null) {
                    SharedParameterRegistry registry = _getSharedParameterRegistry(workspace());
                    for (WeakReference<SharedParameter> reference : registry
                            .getSharedParametersWithName(getName())) {
                        if (reference != null) {
                            SharedParameter parameter = reference.get();
                            if (parameter != null) {
                                // Have a candidate. See if the roots match and if
                                // the container classes match.
                                if (parameter != this
                                        && parameter.getRoot() == root
                                        && parameter._containerClass == _containerClass) {
                                    _sharedParameter = parameter;
                                    // Successful match. No need to search further.
                                    return _sharedParameter;
                                }
                            }
                        }
                    }
                }
            } finally {
                workspace().doneReading();
            }
        }
        return _sharedParameter;
    }

    /** Return the shared parameter registry associated with this workspace.
     */
    private static synchronized SharedParameterRegistry _getSharedParameterRegistry(
            Workspace workspace) {

        Iterator<SharedParameterRegistry> iterator = _REGISTRY.iterator();
        while (iterator.hasNext()) {
            SharedParameterRegistry registry = iterator.next();
            Workspace registerWorkspace = registry.workspace();
            if (registerWorkspace == workspace) {
                return registry;
            } else if (registerWorkspace == null) {
                // Clean up register.
                // The workspace is a weak reference. If it is null, we know that the
                // workspace is garbage collected and hence we can remove the entry in the
                // list.
                iterator.remove();
            }
        }
        SharedParameterRegistry result = new SharedParameterRegistry(workspace);
        _REGISTRY.add(result);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that the constructor has reached the end. */
    private boolean _constructionFinished = false;

    /** The container class. */
    private Class<?> _containerClass;

    /** True if we are delaying validation.
     *  FIXME: This variable is only present for testing and development.
     *  To try out the delay of validation, set it to true and recompile.
     */
    private static final boolean _delayValidation = false;

    /** Empty list. */
    private static Collection<WeakReference<SharedParameter>> _EMPTY_LIST = new LinkedList<WeakReference<SharedParameter>>();

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    private transient List<Initializable> _initializables;

    /** Cached version of a shared parameter. */
    private SharedParameter _sharedParameter;

    /** Version for the cache. */
    private long _sharedParameterVersion = -1L;

    /** Cached version of the shared parameter set. */
    private HashSet<SharedParameter> _sharedParameterSet;

    /** Version for the cache. */
    private long _sharedParameterSetVersion = -1L;

    /** Registry by workspace. */
    private static List<SharedParameterRegistry> _REGISTRY = new LinkedList<SharedParameterRegistry>();

    /** Indicator to suppress propagation. */
    private boolean _suppressingPropagation = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Registry of shared parameters. This is a data structure
     *  that registers all shared parameters in a workspace. This is
     *  more efficient than searching through a model to find all the
     *  shared parameters. It stores one collection of shared parameters
     *  for each name.
     */
    private static class SharedParameterRegistry {

        public SharedParameterRegistry(Workspace workspace) {
            _workspace = new WeakReference<Workspace>(workspace);
        }

        /** Return all shared parameters with the specified name.
         *  This returns a collection of weak references.
         */
        public synchronized Collection<WeakReference<SharedParameter>> getSharedParametersWithName(
                String name) {
            Collection<WeakReference<SharedParameter>> set = _sharedParametersByName
                    .get(name);
            if (set == null) {
                return _EMPTY_LIST;
            } else {
                return set;
            }
        }

        /** Register the specified shared parameter. */
        public synchronized void register(SharedParameter parameter) {
            Collection<WeakReference<SharedParameter>> set = _sharedParametersByName
                    .get(parameter.getName());
            if (set == null) {
                set = new LinkedList<WeakReference<SharedParameter>>();
                _sharedParametersByName.put(parameter.getName(), set);
            }
            set.add(new WeakReference<SharedParameter>(parameter));
        }

        public synchronized void unregister(SharedParameter parameter) {
            Collection<WeakReference<SharedParameter>> set = _sharedParametersByName
                    .get(parameter.getName());
            if (set != null) {
                set.remove(new WeakReference<SharedParameter>(parameter));
            }
        }

        /** Return the workspace. */
        public Workspace workspace() {
            return _workspace.get();
        }

        private HashMap<String, Collection<WeakReference<SharedParameter>>> _sharedParametersByName = new HashMap<String, Collection<WeakReference<SharedParameter>>>();

        private WeakReference<Workspace> _workspace;
    }
}

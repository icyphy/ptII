/* A parameter in the Ptera-based model transformation that temporarily stores
   the model to be transformed.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ModelParameter

/**
 A parameter in the Ptera-based model transformation that temporarily stores the
 model to be transformed.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelParameter extends Parameter implements Initializable {

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
    public ModelParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(ActorToken.TYPE);
    }

    /** Add the specified object to the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** Clone the parameter.  This creates a new variable containing the
     *  same token (if the value was set with setToken()) or the same
     *  (unevaluated) expression, if the expression was set with
     *  setExpression().  The list of variables added to the scope
     *  is not cloned; i.e., the clone has an empty scope.
     *  The clone has the same static type constraints (those given by
     *  setTypeEquals() and setTypeAtMost()), but none of the dynamic
     *  type constraints (those relative to other variables).
     *  @param workspace The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelParameter newObject = (ModelParameter) super.clone(workspace);
        newObject._model = null;
        newObject._token = null;
        newObject._tokenVersion = -1;
        newObject._initializables = null;
        return newObject;
    }

    /** Write a MoML description of this parameter, unless this parameter is
     *  not persistent.
     *
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_isMoMLSuppressed(depth)) {
            return;
        }

        output.write(_getIndentPrefix(depth) + "<" + _elementName + " name=\""
                + name + "\" class=\"" + getClassName() + "\">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
    }

    /** Return an empty string.
     *
     *  @return An empty string.
     *  @see #setExpression(String)
     */
    @Override
    public String getExpression() {
        return "";
    }

    /** Get the model stored in this parameter.
     *
     *  @return The model.
     *  @see #setModel(CompositeEntity)
     */
    public CompositeEntity getModel() {
        return _model;
    }

    /** Get the token of this parameter, which is an {@link ActorToken}
     *  containing the model.
     *
     *  @return The token.
     *  @exception IllegalActionException If the ActorToken cannot be created.
     *  @see #setToken(Token)
     */
    @Override
    public Token getToken() throws IllegalActionException {
        // FIXME: Coverity points out that this method should call
        // super.getToken().  In particular, isStringMode is not handled
        // and the token is not evaluated if _needsEvaluation is true.

        CompositeEntity model = getModel();

        boolean createEmptyModel = model == null;
        if (!createEmptyModel && model instanceof CompositeActor) {
            Manager manager = ((CompositeActor) model).getManager();
            if (manager != null && manager.getState() != Manager.IDLE) {
                // FIXME: If the model is being executed, we can't clone it.
                createEmptyModel = true;
            }
        }

        if (createEmptyModel) {
            model = new TypedCompositeActor(new Workspace());
            _token = new ActorToken(model);
            _tokenVersion = -1;
        } else if (_token == null
                || _tokenVersion != model.workspace().getVersion()) {
            _token = new ActorToken(model);
            _tokenVersion = model.workspace().getVersion();
        }

        return _token;
    }

    /** Initialize this model parameter.
     *
     *  @exception IllegalActionException If thrown by other initializables
     *   associated to this parameter.
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

    /** React to change of the model in this parameter.
     */
    public void modelChanged() {
        invalidate();
    }

    /** Preinitialize this model parameter and set the current model to be null.
     *
     *  @exception IllegalActionException If thrown by other initializables
     *   associated to this parameter.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        setModel(null);
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
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

    /** Set the new container of this parameter.
     *
     *  @param container The new container.
     *  @exception IllegalActionException If the container will not accept
     *   a variable as its attribute, or this variable and the container
     *   are not in the same workspace, or the proposed container would
     *   result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this variable.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }
        super.setContainer(container);
        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    /** Do nothing.
     *
     *  @param expression The new expression.
     *  @see #getExpression()
     */
    @Override
    public void setExpression(String expression) {
    }

    /** Set the model in this parameter.
     *
     *  @param model The new model.
     *  @see #getModel()
     */
    public void setModel(CompositeEntity model) {
        if (_model != null) {
            _model.workspace().remove(_model);
            try {
                _model.setContainer(null);
            } catch (KernelException e) {
                // Should not occur.
            }
        }
        _model = model;
        _token = null;
        _tokenVersion = -1;
        invalidate();
    }

    /** Set the model in this parameter with an {@link ActorToken}.
     *
     *  @param token The ActorToken.
     *  @exception IllegalActionException If the token is not an ActorToken.
     *  @see #getToken()
     */
    @Override
    public void setToken(Token token) throws IllegalActionException {
        // FIXME: Coverity points out that super.setToken() is not called.
        // In particular, setExpression() and validate() are not called here.
        ActorToken actorToken = (ActorToken) ActorToken.TYPE.convert(token);
        CompositeEntity model = (CompositeEntity) actorToken.getEntity();
        setModel(model);
        _token = actorToken;
        _tokenVersion = model.workspace().getVersion();
    }

    /** Set the model in this parameter to be null.
     *
     *  @exception IllegalActionException If thrown by other initializables
     *   associated to this parameter.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
        setModel(null);
    }

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     */
    private transient List<Initializable> _initializables;

    /** The model in this parameter.
     */
    private CompositeEntity _model;

    /** The most recently created ActorToken.
     */
    private ActorToken _token;

    /** The version of the ActorToken.
     */
    private long _tokenVersion = -1;
}

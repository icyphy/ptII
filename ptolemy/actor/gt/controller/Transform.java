/* An event to transform the model in the model parameter with the encapsulated
   transformation rule.

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
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.TransformationListener;
import ptolemy.actor.gt.TransformationMode;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.ConfigurableEntity;
import ptolemy.domains.modal.kernel.Configurer;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.domains.ptera.kernel.PteraErrorEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.TransformEventController.Factory;

///////////////////////////////////////////////////////////////////
//// Transform

/**
 An event to transform the model in the model parameter with the encapsulated
 transformation rule.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Transform extends GTEvent implements ConfigurableEntity,
TransformationListener {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public Transform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        controllerFactory = new Factory(this, "controllerFactory");

        matched = new Parameter(this, "matched");
        matched.setTypeEquals(BaseType.BOOLEAN);
        matched.setToken(BooleanToken.TRUE);

        _configurer = new EmbeddedConfigurer(_workspace);
        _configurer.setName("Configurer");
        _configurer.setConfiguredObject(this);

        _transformation = new TransformationRule(_configurer, "Transformation");
        TransformationMode helper = new TransformationMode(_transformation,
                "_helper");
        helper.setPersistent(false);
        _clearURI(_transformation);

        mode = new TransformationMode(this, "mode");

        defer = new Parameter(this, "defer");
        defer.setTypeEquals(BaseType.BOOLEAN);
        defer.setToken(BooleanToken.FALSE);
    }

    /** Invoked when the specified object is added to a container.
     *
     *  @param object The added object.
     */
    @Override
    public void addObject(NamedObj object) {
        if (object instanceof Initializable) {
            Initializable initializable = (Initializable) object;
            try {
                initializable.preinitialize();
                initializable.initialize();
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e, "Unable to initialize "
                        + "initializables.");
            }
        }
    }

    /** Clone the event into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Transform newObject = (Transform) super.clone(workspace);
        try {
            newObject._configurer = new EmbeddedConfigurer(workspace);
            newObject._configurer.setName("Configurer");
            newObject._configurer.setConfiguredObject(newObject);
            newObject._transformation = (TransformationRule) _transformation
                    .clone(workspace);
            newObject._transformation.setContainer(newObject._configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Configure this event with a transformation rule (an instance of {@link
     *  TransformationRule}) stored in the moml.
     *
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        // Coverity: MoMLParser.endElement() could call configure() with text==null.
        if (text != null) {
            text = text.trim();
            if (!text.equals("")) {
                MoMLParser parser = new MoMLParser(workspace());
                _configurer.removeAllEntities();
                parser.setContext(_configurer);
                parser.parse(base, source, new StringReader(text));
                _transformation = (TransformationRule) _configurer.entityList()
                        .get(0);
                TransformationMode helper = new TransformationMode(
                        _transformation, "_helper");
                helper.setPersistent(false);
                _clearURI(_transformation);
            }
        }
    }

    /** Process this event and transform (or pattern-match) the model in the
     *  model parameter depending on the transformation model {@link #mode}.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the model cannot be transformed, or
     *   if thrown by the superclass.
     *  @see TransformationMode
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        _debug(new PteraDebugEvent(this, "Start transformation."));

        long start = System.currentTimeMillis();

        CompositeEntity model = getModelParameter().getModel();
        model.setDeferringChangeRequests(false);
        boolean isMatched = false;
        try {
            boolean initializeObjects = false;
            NamedObj toplevel = model.toplevel();
            if (toplevel instanceof CompositeActor) {
                Manager manager = ((CompositeActor) toplevel).getManager();
                if (manager != null && manager.getState() != Manager.IDLE) {
                    initializeObjects = true;
                }
            }
            isMatched = mode.transform(mode.getWorkingCopy(_transformation),
                    model, initializeObjects ? this : null,
                            ((BooleanToken) defer.getToken()).booleanValue());
            if (isMatched) {
                _debug(new PteraDebugEvent(this, "Match found."));
            } else {
                _debug(new PteraDebugEvent(this, "Match not found."));
            }
        } catch (Throwable t) {
            _debug(new PteraErrorEvent(this, t.getMessage()));
            throw new IllegalActionException(this, t, "Error occurred in the "
                    + "transformation in " + getFullName() + ".");
        }
        getModelParameter().setModel(model);
        matched.setToken(BooleanToken.getInstance(isMatched));

        long elapsed = System.currentTimeMillis() - start;
        if (data == null) {
            _debug(new PteraDebugEvent(this, "Finish transformation ("
                    + (double) elapsed / 1000 + " sec)."));
        } else {
            _debug(new PteraDebugEvent(this, "Request refire ("
                    + (double) elapsed / 1000 + " sec)."));
        }

        return data;
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    @Override
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    @Override
    public String getConfigureText() {
        return null;
    }

    /** Get the {@link Configurer} object for this entity.
     *  @return the Configurer object for this entity.
     */
    @Override
    public Configurer getConfigurer() {
        return _configurer;
    }

    /** Get the refinement of this event, which is an instance of {@link
     *  TransformationRule}.
     *
     *  @return The refinement.
     */
    @Override
    public TypedActor[] getRefinement() {
        return new TypedActor[] { _transformation };
    }

    /** The controller factory for this event to specialize the popup menu.
     *
     */
    public Factory controllerFactory;

    /** Whether the transformation should be deferred with a change request.
     */
    public Parameter defer;

    /** Whether the last pattern matching was successful (read-only).
     */
    public Parameter matched;

    ///////////////////////////////////////////////////////////////////
    //// EmbeddedConfigurer

    /** The transformation mode.
     */
    public TransformationMode mode;

    /**
     The configurer to be embedded in the transform event.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class EmbeddedConfigurer extends Configurer {

        /** Construct a configurer in the given workspace.
         *
         *  @param workspace The workspace.
         */
        public EmbeddedConfigurer(Workspace workspace) {
            super(workspace);
        }

        /** Get the container entity.
         *  @return The container, which is an instance of CompositeEntity.
         */
        @Override
        public NamedObj getContainer() {
            if (_container == null) {
                return super.getContainer();
            } else {
                return _container;
            }
        }

        /** Set the object that this configurer configures.
         *
         *  @param configured The object that this configurer configures.
         *  @see #getConfiguredObject()
         */
        @Override
        public void setConfiguredObject(NamedObj configured) {
            super.setConfiguredObject(configured);
            _container = configured;
        }

        /** The container of this configurer.
         */
        private NamedObj _container;
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class are the attributes plus the ports.  This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);

        String sourceSpec = "";

        if (_configureSource != null && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
        }

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec
                + ">\n");
        _transformation.exportMoML(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

    /** Return whether the given refinement is active. The result is false
     *  unless the refinement is not the {@link TransformationRule} instance.
     *
     *  @param refinement The refinement.
     *  @return Whether the refinement is active.
     */
    @Override
    protected boolean _isActiveRefinement(TypedActor refinement) {
        return refinement != _transformation;
    }

    /** The encapsulated transformation rule.
     */
    protected TransformationRule _transformation;

    /** Clear the URI attribute of the given object.
     *
     *  @param object The object.
     *  @exception IllegalActionException If the URI attribute of the object
     *   cannot be removed.
     */
    private static void _clearURI(NamedObj object)
            throws IllegalActionException {
        URIAttribute attribute = (URIAttribute) object.getAttribute("_uri",
                URIAttribute.class);
        if (attribute != null) {
            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(attribute, e,
                        "Unexpected exception.");
            }
        }
    }

    /** The configure source.
     */
    private String _configureSource;

    /** The configurer.
     */
    private Configurer _configurer;
}

/* An attribute encapsulating a model transformation with the Ptera controller.

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

import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.modal.kernel.Configurer;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.ParserAttribute;
import ptolemy.vergil.gt.TransformationAttributeController;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;
import ptolemy.vergil.gt.TransformationAttributeIcon;

///////////////////////////////////////////////////////////////////
//// TransformationAttribute

/**
 An attribute encapsulating a model transformation with the Ptera controller.
 The transformation can be applied to the container of this attribute either
 manually by the model user, or automatically by invoking the {@link
 #executeTransformation()} method.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttribute extends Attribute implements Configurable,
GTAttribute {

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
    public TransformationAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _init();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public TransformationAttribute(Workspace workspace) {
        super(workspace);

        try {
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Add an execution listener to the list of execution listeners, which are
     *  invoked at specific points when the transformation is executed.
     *
     *  @param listener The listener.
     *  @see #removeExecutionListener(ExecutionListener)
     */
    public void addExecutionListener(ExecutionListener listener) {
        _executionListeners.add(listener);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TransformationAttribute newObject = (TransformationAttribute) super
                .clone(workspace);
        try {
            newObject._configurer = new Configurer(workspace);
            newObject._configurer.setName("Configurer");
            new DEDirector(newObject._configurer, "_director");
            newObject._configurer
            .setManager(new Manager(workspace, "_manager"));
            newObject._configurer.setConfiguredObject(newObject);
            newObject._executionListeners = new LinkedList<ExecutionListener>();
            newObject._modelUpdater = (PteraModalModel) _modelUpdater
                    .clone(workspace);
            newObject._modelUpdater.setContainer(newObject._configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The object should interpret the
     *  source first, if it is specified, followed by the literal text,
     *  if that is specified.  The new configuration should usually
     *  override any old configuration wherever possible, in order to
     *  ensure that the current state can be successfully retrieved.
     *  <p>
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
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
        // Coverity says that MoMLParser could call configure with the text = null.
        if (text != null && !text.trim().equals("")) {
            MoMLParser parser = new MoMLParser(workspace());
            _configurer.removeAllEntities();
            parser.setContext(_configurer);
            parser.parse(base, source, new StringReader(text));
            _modelUpdater = (PteraModalModel) _configurer.entityList().get(0);
            _clearURI(_modelUpdater);
        }

        StringParameter typeParameter = (StringParameter) getAttribute("_type");
        String type = typeParameter == null ? null : typeParameter
                .getExpression();
        if ("delayed".equals(type)) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getContainer().requestChange(
                            new ChangeRequest(this,
                                    "Perform delayed transformation.") {
                                @Override
                                protected void _execute() throws Exception {
                                    try {
                                        executeTransformation();
                                    } finally {
                                        setContainer(null);
                                    }
                                }
                            });
                }
            });
        } else if ("immediate".equals(type)) {
            try {
                executeTransformation();
            } finally {
                setContainer(null);
            }
        }
    }

    /** Execute the transformation with the container of this attribute as the
     *  model to be transformed.
     *
     *  @exception Exception If error occurs in the transformation.
     *  @see #executeTransformation(CompositeEntity)
     */
    public void executeTransformation() throws Exception {
        executeTransformation((CompositeEntity) getContainer());
    }

    /** Execute the transformation with the given model as the model to be
     *  transformed.
     *
     *  @param model The model to be transformed.
     *  @exception Exception If error occurs in the transformation.
     */
    public void executeTransformation(CompositeEntity model) throws Exception {
        Manager manager = getModelUpdater().getManager();
        manager.addExecutionListener(new TransformationListener(manager,
                "_transformationListener", model));

        for (ExecutionListener listener : _executionListeners) {
            manager.addExecutionListener(listener);
        }

        NamedObj container = getContainer();
        List<ParserAttribute> parsers = container
                .attributeList(ParserAttribute.class);
        ParserAttribute parserAttribute = parsers.size() > 0 ? parsers.get(0)
                : new ParserAttribute(container,
                        container.uniqueName("_parser"));
        MoMLParser oldParser = parsers.size() > 0 ? parserAttribute.getParser()
                : null;
        parserAttribute.setParser(new MoMLParser());
        manager.enablePrintTimeAndMemory(false);

        try {
            manager.execute();
        } finally {
            manager.enablePrintTimeAndMemory(true);
            if (oldParser == null) {
                parserAttribute.setContainer(null);
            } else {
                parserAttribute.setParser(oldParser);
            }
        }
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

    /** Get the model updater encapsulated in this attribute.
     *
     *  @return The model updater.
     */
    public PteraModalModel getModelUpdater() {
        return _modelUpdater;
    }

    /** Remove an execution listener from the list of execution listeners, which\
     *  are invoked at specific points when the transformation is executed.
     *
     *  @param listener The listener to be removed.
     *  @see #addExecutionListener(ExecutionListener)
     */
    public void removeExecutionListener(ExecutionListener listener) {
        _executionListeners.remove(listener);
    }

    /** The condition under which this attribute is applicable. It must evaluate
     *  to a BooleanToken. If its value is false, execution of the
     *  transformation causes no effect.
     */
    public Parameter condition;

    /** The editor factory for the contents in this attribute (the model
     *  updater).
     */
    public TransformationAttributeEditorFactory editorFactory;

    /** Write a MoML description of the contents of this object, which
     *  in this base class is the attributes.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  Callers of this method should hold read access before
     *  calling this method.  Note that exportMoML() does this for us.
     *
     *  @param output The output stream to write to.
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
        _modelUpdater.exportMoML(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

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

    /** Create the parameters and private fields of this attribute.
     *
     *  @exception IllegalActionException If thrown when creating the
     *   parameters.
     *  @exception NameDuplicationException If thrown when creating the
     *   parameters.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        condition = new Parameter(this, "condition");
        condition.setExpression("true");

        _configurer = new Configurer(workspace());
        _configurer.setName("Configurer");
        new DEDirector(_configurer, "_director");
        _configurer.setManager(new Manager(workspace(), "_manager"));
        _configurer.setConfiguredObject(this);

        String moml = "<entity name=\"ModelUpdater\" "
                + "class=\"ptolemy.actor.gt.controller.ModelUpdater\"/>";
        MoMLParser parser = new MoMLParser();
        parser.setContext(_configurer);
        try {
            parser.parse(moml);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to populate "
                    + "the transformation rule within \"" + getFullName()
                    + "\".");
        }
        _modelUpdater = (PteraModalModel) _configurer.getEntity("ModelUpdater");

        new TransformationAttributeIcon(this, "_icon");
        new TransformationAttributeController.Factory(this,
                "_controllerFactory");
        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }

    /** The configure source.
     */
    private String _configureSource;

    /** The configurer containing the model updater.
     */
    private Configurer _configurer;

    /** The list of execution listeners.
     */
    private List<ExecutionListener> _executionListeners = new LinkedList<ExecutionListener>();

    /** The model updater.
     */
    private PteraModalModel _modelUpdater;

    ///////////////////////////////////////////////////////////////////
    //// TransformationListener

    /**
     An execution listener that sets the model parameter to contain the
     container of the transformation attribute at the beginning of the
     transformation.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class TransformationListener extends Attribute implements
    ExecutionListener {

        /** Construct an attribute with the given name contained by the specified
         *  entity. The container argument must not be null, or a
         *  NullPointerException will be thrown.  This attribute will use the
         *  workspace of the container for synchronization and version counts.
         *  If the name argument is null, then the name is set to the empty string.
         *  Increment the version of the workspace.
         *  @param manager The container of this listener.
         *  @param name The name of this attribute.
         *  @param model The model to be transformed.
         *  @exception IllegalActionException If the attribute is not of an
         *   acceptable class for the container, or if the name contains a period.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public TransformationListener(Manager manager, String name,
                CompositeEntity model) throws IllegalActionException,
                NameDuplicationException {
            super(manager, name);
            _model = model;
        }

        /** Do nothing.
         *
         *  @param manager The manager controlling the execution.
         *  @param throwable The throwable to report.
         */
        @Override
        public void executionError(Manager manager, Throwable throwable) {
        }

        /** Do nothing.
         *
         *  @param manager The manager controlling the execution.
         */
        @Override
        public void executionFinished(Manager manager) {
        }

        /** If the manager's state becomes {@link Manager#INITIALIZING}, set the
         *  model parameter in the model updater of the transformation attribute
         *  to contain the container of the transformation attribute. Do nothing
         *  otherwise.
         *
         *  @param manager The manager controlling the execution.
         */
        @Override
        public void managerStateChanged(Manager manager) {
            if (manager.getState() == Manager.INITIALIZING) {
                ModelParameter modelAttribute = (ModelParameter) _modelUpdater
                        .getController().getAttribute("Model");
                modelAttribute.setModel(_model);
            }
        }

        /** The model to be transformed.
         */
        private CompositeEntity _model;
    }
}

/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// TransformationAttribute

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttribute extends Attribute implements Configurable,
GTAttribute {

    public TransformationAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _init();
    }

    public TransformationAttribute(Workspace workspace) {
        super(workspace);

        try {
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    public TransformationAttribute(Workspace workspace, String name) {
        super(workspace);

        try {
            setName(name);
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    public void addExecutionListener(ExecutionListener listener) {
        _executionListeners.add(listener);
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TransformationAttribute newObject =
            (TransformationAttribute) super.clone(workspace);
        try {
            newObject._configurer = new Configurer(workspace);
            newObject._configurer.setName("Configurer");
            new DEDirector(newObject._configurer, "_director");
            newObject._configurer.setManager(new Manager(workspace,
                    "_manager"));
            newObject._configurer.setConfiguredObject(newObject);
            newObject._modelUpdater = (PteraModalModel) _modelUpdater.clone(
                    workspace);
            newObject._modelUpdater.setContainer(newObject._configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        if (!text.trim().equals("")) {
            MoMLParser parser = new MoMLParser(workspace());
            _configurer.removeAllEntities();
            parser.setContext(_configurer);
            parser.parse(base, source, new StringReader(text));
            _modelUpdater = (PteraModalModel) _configurer.entityList().get(0);
            _clearURI(_modelUpdater);
        }

        StringParameter typeParameter = (StringParameter) getAttribute("_type");
        String type = typeParameter == null ? null :
            typeParameter.getExpression();
        if ("delayed".equals(type)) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    getContainer().requestChange(new ChangeRequest(this,
                            "Perform delayed transformation.") {
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

    public void executeTransformation() throws Exception {
        executeTransformation((CompositeEntity) getContainer());
    }

    public void executeTransformation(CompositeEntity model) throws Exception {
        Manager manager = getModelUpdater().getManager();
        manager.addExecutionListener(new TransformationListener(manager,
                "_transformationListener", model));

        for (ExecutionListener listener : _executionListeners) {
            manager.addExecutionListener(listener);
        }

        NamedObj container = getContainer();
        List<ParserAttribute> parsers = container.attributeList(
                ParserAttribute.class);
        ParserAttribute parserAttribute = parsers.size() > 0 ?
                parsers.get(0) :
                new ParserAttribute(container, container.uniqueName("_parser"));
        MoMLParser oldParser = parsers.size() > 0 ?
                parserAttribute.getParser() : null;
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

    public String getConfigureSource() {
        return _configureSource;
    }

    public String getConfigureText() {
        return null;
    }

    public PteraModalModel getModelUpdater() {
        return _modelUpdater;
    }

    public void removeExecutionListener(ExecutionListener listener) {
        _executionListeners.remove(listener);
    }

    public Parameter condition;

    public TransformationAttributeEditorFactory editorFactory;

    protected void _exportMoMLContents(Writer output, int depth)
    throws IOException {
        super._exportMoMLContents(output, depth);

        String sourceSpec = "";

        if ((_configureSource != null) && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
        }

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec +
                ">\n");
        _modelUpdater.exportMoML(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

    private static void _clearURI(NamedObj object)
    throws IllegalActionException, NameDuplicationException {
        URIAttribute attribute = (URIAttribute) object.getAttribute("_uri",
                URIAttribute.class);
        if (attribute != null) {
            attribute.setContainer(null);
        }
    }

    private void _init() throws IllegalActionException,
            NameDuplicationException {
        condition = new Parameter(this, "condition");
        condition.setExpression("true");

        _configurer = new Configurer(workspace());
        _configurer.setName("Configurer");
        new DEDirector(_configurer, "_director");
        _configurer.setManager(new Manager(workspace(), "_manager"));
        _configurer.setConfiguredObject(this);

        String moml = "<entity name=\"ModelUpdater\" " +
                "class=\"ptolemy.actor.gt.controller.ModelUpdater\"/>";
        MoMLParser parser = new MoMLParser();
        parser.setContext(_configurer);
        try {
            parser.parse(moml);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to populate " +
                    "the transformation rule within \"" + getFullName() +
                    "\".");
        }
        _modelUpdater = (PteraModalModel) _configurer.getEntity("ModelUpdater");

        new TransformationAttributeIcon(this, "_icon");
        new TransformationAttributeController.Factory(this,
                "_controllerFactory");
        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }

    private String _configureSource;

    private Configurer _configurer;

    private List<ExecutionListener> _executionListeners =
        new LinkedList<ExecutionListener>();

    private PteraModalModel _modelUpdater;

    private class TransformationListener extends Attribute
            implements ExecutionListener {

        public TransformationListener(Manager manager, String name,
                CompositeEntity model) throws IllegalActionException,
                NameDuplicationException {
            super(manager, name);
            _model = model;
        }

        public void executionError(Manager manager, Throwable throwable) {
        }

        public void executionFinished(Manager manager) {
        }

        public void managerStateChanged(Manager manager) {
            if (manager.getState() == Manager.INITIALIZING) {
                ModelParameter modelAttribute =
                    (ModelParameter) _modelUpdater.getController().getAttribute(
                            "Model");
                modelAttribute.setModel(_model);
            }
        }

        private CompositeEntity _model;
    }
}

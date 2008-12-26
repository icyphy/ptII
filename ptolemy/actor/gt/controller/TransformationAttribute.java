/*

 Copyright (c) 2008 The Regents of the University of California.
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
import java.util.List;

import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.erg.kernel.ERGModalModel;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
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
public class TransformationAttribute extends GTAttribute
implements Configurable {

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
            newObject._modelUpdater = (ERGModalModel) _modelUpdater.clone(
                    workspace);
            newObject._modelUpdater.setContainer(newObject._configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    public void configure(URL base, String source, String text)
            throws Exception {
        Parameter immediate = (Parameter) getAttribute("_immediate");
        boolean isImmediate = immediate != null &&
                ((BooleanToken) immediate.getToken()).booleanValue();
        if (isImmediate) {
            Effigy masterEffigy = Configuration.findEffigy(toplevel());
            PtolemyEffigy effigy = new PtolemyEffigy(masterEffigy,
                    masterEffigy.uniqueName("effigy"));
            MoMLParser parser = new MoMLParser(workspace());
            final ERGModalModel transformer = (ERGModalModel) parser.parse(base,
                    source, new StringReader(text));
            final Manager manager = new Manager(transformer.workspace(),
                    "_manager");
            final CompositeEntity context = (CompositeEntity) getContainer();
            effigy.setModel(transformer);
            transformer.setManager(manager);


            manager.addExecutionListener(new ExecutionListener() {
                public void executionError(Manager manager,
                        Throwable throwable) {
                }

                public void executionFinished(Manager manager) {
                }

                public void managerStateChanged(Manager manager) {
                    if (manager.getState() == Manager.INITIALIZING) {
                        ModelParameter modelAttribute =
                            (ModelParameter) transformer.getController()
                                    .getAttribute("Model");
                        modelAttribute.setModel(context);
                    }
                }
            });

            List<ParserAttribute> parsers = context.attributeList(
                    ParserAttribute.class);
            ParserAttribute parserAttribute = parsers.size() > 0 ?
                    parsers.get(0) :
                    new ParserAttribute(context, context.uniqueName("_parser"));
            MoMLParser oldParser = parsers.size() > 0 ?
                    parserAttribute.getParser() : null;
            parserAttribute.setParser(new MoMLParser());
            try {
                manager.execute();
            } finally {
                if (oldParser == null) {
                    parserAttribute.setContainer(null);
                } else {
                    parserAttribute.setParser(oldParser);
                }
            }

            setContainer(null);
        } else {
            _configureSource = source;
            text = text.trim();
            if (!text.equals("")) {
                MoMLParser parser = new MoMLParser(workspace());
                _configurer.removeAllEntities();
                parser.setContext(_configurer);
                parser.parse(base, source, new StringReader(text));
                _modelUpdater = (ERGModalModel) _configurer.entityList().get(0);
                _clearURI(_modelUpdater);
            }
        }
    }

    public String getConfigureSource() {
        return _configureSource;
    }

    public String getConfigureText() {
        return null;
    }

    public ERGModalModel getModelUpdater() {
        return _modelUpdater;
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
        _modelUpdater = (ERGModalModel) _configurer.getEntity("ModelUpdater");

        new TransformationAttributeIcon(this, "_icon");
        new TransformationAttributeController.Factory(this,
                "_controllerFactory");
        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }

    private String _configureSource;

    private Configurer _configurer;

    private ERGModalModel _modelUpdater;
}

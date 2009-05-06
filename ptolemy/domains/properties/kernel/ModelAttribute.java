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
package ptolemy.domains.properties.kernel;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import ptolemy.actor.Manager;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;
import ptolemy.vergil.properties.ModelAttributeController;

//////////////////////////////////////////////////////////////////////////
//// ModelAttribute

/**


 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelAttribute extends Attribute implements Configurable {


    public ModelAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _init();
    }

    public ModelAttribute(Workspace workspace) {
        super(workspace);

        try {
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    public ModelAttribute(Workspace workspace, String name) {
        super(workspace);

        try {
            setName(name);
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelAttribute newObject =
            (ModelAttribute) super.clone(workspace);
        try {
            newObject._configurer = new Configurer(workspace);
            newObject._configurer.setName("Configurer");
            new DEDirector(newObject._configurer, "_director");
            newObject._configurer.setManager(new Manager(workspace,
                    "_manager"));
            newObject._configurer.setConfiguredObject(newObject);
            newObject._model = (CompositeEntity) _model.clone(
                    workspace);
            ((CompositeEntity) newObject._model).setContainer(newObject._configurer);
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
            _model = (CompositeEntity) _configurer.entityList().get(0);
            _clearURI(_model);
        }
    }

    public String getConfigureSource() {
        return _configureSource;
    }

    public String getConfigureText() {
        return null;
    }

    public CompositeEntity getContainedModel() {
        return _model;
    }

    // FIXME: This class uses classes from vergil, which means the backend 
    // and the gui are too tighly intertwined.
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
        _model.exportMoML(output, depth + 1);
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

        _configurer = new Configurer(workspace());
        _configurer.setName("Configurer");
        new DEDirector(_configurer, "_director");
        _configurer.setManager(new Manager(workspace(), "_manager"));
        _configurer.setConfiguredObject(this);

        String moml = "<entity name=\"Model\" " +
                "class=\"" + _getContainedModelClassName() + "\"/>";
        MoMLParser parser = new MoMLParser();
        parser.setContext(_configurer);
        try {
            parser.parse(moml);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to populate " +
                    "the lattice model within \"" + getFullName() +
                    "\".");
        }
        _model = (CompositeEntity) _configurer.getEntity("Model");

        //new ModelAttributeIcon(this, "_icon");
	// FIXME: This class uses classes from vergil, which means the backend 
	// and the gui are too tighly intertwined.
        new ModelAttributeController.Factory(this,
                "_controllerFactory");
        //editorFactory = new TransformationAttributeEditorFactory(this, "editorFactory");
    }

    protected String _getContainedModelClassName() {
        return "ptolemy.actor.CompositeActor";
    }

    private String _configureSource;

    private Configurer _configurer;

    private CompositeEntity _model;

}

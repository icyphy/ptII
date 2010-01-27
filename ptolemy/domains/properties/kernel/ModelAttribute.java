/*
 * A base class for attributes that contain a refinement model graph.
 * 
 * Copyright (c) 2008-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 */
package ptolemy.domains.properties.kernel;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import ptolemy.actor.Manager;
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

///////////////////////////////////////////////////////////////////
//// ModelAttribute

/**
 * A base class for attributes that contain a refinement model graph.
 *
 * <p> Note that this class expects that 
 * ptolemy.vergil.properties.ModelAttributeControllerFactory
 * will be used as the _controllerFactory of this class.  Instead of
 * having an explicit compile-time dependency between this class and
 * ModelAttributeControllerFactory, derived classes should use MoML
 * to set up the containment relationship.  For example,      
 * <code>ptII/ptolemy/configs/properties/propertiesConfigurableSolvers.xml</code>
 * contains: 
 * <pre>
 * <property name="PropertyLatticeAttribute" class="ptolemy.domains.properties.kernel.PropertyLatticeAttribute">
 *     <property name="_controllerFactory" class="ptolemy.vergil.properties.ModelAttributeControllerFactory">
 *     </property>
 * </property>
 * </pre>
 * All classes that derive from this class should use a similar pattern.
 *  
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class ModelAttribute extends Attribute implements Configurable {

    /**
     * Create a model attribute with the specified container and name.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the attribute is not of an
     * acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public ModelAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _init();
    }

    /**
     * Construct an attribute in the specified workspace with an empty string as
     * a name. If the workspace argument is null, then use the default
     * workspace. The object is added to the directory of the workspace.
     * @param workspace The workspace that will list the attribute.
     */
    public ModelAttribute(Workspace workspace) {
        super(workspace);

        try {
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    /**
     * Construct an attribute in the specified workspace with the specified
     * name. If the workspace argument is null, then use the default workspace.
     * The object is added to the directory of the workspace.
     * @param workspace The workspace that will list the attribute.
     * @param name The specified name.
     */
    public ModelAttribute(Workspace workspace, String name) {
        super(workspace);

        try {
            setName(name);
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return a clone of this model attribute. This also creates a clone for the
     * contained model.
     * @param workspace The workspace for the cloned object.
     * @return A clone.
     * @exception CloneNotSupportedException Thrown if an error occurs while
     * cloning the attribute or the contained model.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelAttribute newObject = (ModelAttribute) super.clone(workspace);
        try {
            newObject._configurer = new Configurer(workspace);
            newObject._configurer.setName("Configurer");

            //new DEDirector(newObject._configurer, "_director");

            newObject._configurer
                    .setManager(new Manager(workspace, "_manager"));
            newObject._configurer.setConfiguredObject(newObject);

            newObject._model = (CompositeEntity) _model.clone(workspace);
            newObject._model.setContainer(newObject._configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /**
     * Construct and configure the contained model with the specified source and
     * text. It parses the specified MoML text and clear clears the URI
     * information from the constructed model.
     * @param base The base URL for relative references, or null if not known.
     * @param source The URI of the document.
     * @param text The MoML description.
     * @exception Exception If the parsing fails or the URI info cannot be
     * cleared.
     */
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

    /**
     * Return the configure source.
     * @return The configure source.
     */
    public String getConfigureSource() {
        return _configureSource;
    }

    /**
     * Return the configure text. In this base class, this returns null.
     * @return The configure text.
     */
    public String getConfigureText() {
        return null;
    }

    /**
     * Return the contained model.
     * @return The contained model.
     */
    public CompositeEntity getContainedModel() {
        return _model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Write a MoML description of the contents of this object. This wraps the
     * MoML description of the contained model within the &lt;configure&gt; tag.
     * @param output The output stream to write to.
     * @param depth The depth in the hierarchy, to determine indenting.
     * @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);

        String sourceSpec = "";

        if (_configureSource != null && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
        }

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec
                + ">\n");
        _model.exportMoML(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

    /**
     * Return the class name of the contained model. The class name is used to
     * instantiate the contained model. By default, this returns the string
     * "ptolemy.actor.CompositeActor". If base classes can override this method
     * to instantiate a different type of top-level for the contained model.
     * @return the class name of the contained model.
     */
    protected String _getContainedModelClassName() {
        return "ptolemy.actor.CompositeActor";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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

        // FIXME: do we need a director for the configurer?
        //new DEDirector(_configurer, "_director");

        _configurer.setManager(new Manager(workspace(), "_manager"));
        _configurer.setConfiguredObject(this);

        String moml = "<entity name=\"Model\" " + "class=\""
                + _getContainedModelClassName() + "\"/>";
        MoMLParser parser = new MoMLParser();
        parser.setContext(_configurer);
        try {
            parser.parse(moml);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to populate "
                    + "the model within \"" + getFullName() + "\".");
        }
        _model = (CompositeEntity) _configurer.getEntity("Model");

        // Note that derived classes should set _controllerFactory by using
        // MoML.  See the class comment for detail
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _configureSource;

    private Configurer _configurer;

    private CompositeEntity _model;

}

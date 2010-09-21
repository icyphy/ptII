/*
 * An attribute that contains a model described in MoML.
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
package ptolemy.moml;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MoMLModelAttribute

/**
 * An attribute that has a model described in MoML.
 * The MoML is specified by calling {@link #configure(URL, String, String)},
 * or by including the MoML within &lt;configure&gt; tags in a MoML file.
 * The MoML is returned by the {@link #getConfigureText()} method.
 * The {@link #getContainedModel()} method returns the model specified
 * by the MoML.
 * <p>
 * When an instance of this attribute is exported to MoML, the MoML
 * description above will be included in the exported MoML within
 * &lt;configure&gt; tags.
 * <p>
 * An instance of this attribute may wish to override the default
 * "Look Inside" behavior by including an instance of
 * ptolemy.vergil.toolbox.MoMLModelAttributeControllerFactory
 * as attribute contained by this instance.  Instead of
 * having an explicit compile-time dependency between this class and
 * MoMLModelAttributeControllerFactory, derived classes should use MoML
 * to set up the containment relationship.  For example,
 * <pre>
 * <property name="MyAttribute" class="ptolemy.moml.MoMLModelAttribute">
 *     <property name="_controllerFactory" class="ptolemy.vergil.toolbox.MoMLModelAttributeControllerFactory">
 *     </property>
 *     <configure>
 *        ... my MoML text here ...
 *     </configure>
 * </property>
 * </pre>
 *
 * @author Dai Bui, Edward Lee, Ben Lickly
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class MoMLModelAttribute extends Attribute implements Configurable {

    /** Create a model attribute with the specified container and name.
     *  @param container The specified container.
     *  @param name The specified name.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public MoMLModelAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a clone of this model attribute. This also creates a clone for the
     *  contained model.
     *  @param workspace The workspace for the cloned object.
     *  @return A clone.
     *  @exception CloneNotSupportedException Thrown if an error occurs while
     *   cloning the attribute or the contained model.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MoMLModelAttribute newObject = (MoMLModelAttribute) super
                .clone(workspace);
        if (_model != null) {
            newObject._model = (NamedObj) _model.clone(workspace());
        }
        return newObject;
    }

    /** Construct and configure the contained model with the specified source and
     *  text. This parses the specified MoML text.
     *  @param base The base URL for relative references, or null if not known.
     *  @param source The URI of a document providing source, which is ignored in this class.
     *  @param text The MoML description.
     *  @exception Exception If the parsing fails.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        if (!text.trim().equals("")) {
            MoMLParser parser = new MoMLParser(workspace());
            _model = parser.parse(base, null, new StringReader(text));
        }
    }

    /** Return null. This class ignores the source attribute of a configure tag.
     *  @return The configure source.
     */
    public String getConfigureSource() {
        return null;
    }

    /** Return the MoML description of the model, if there is one, and
     *  null otherwise.
     *  @return The text to include in a configure tag.
     */
    public String getConfigureText() {
        if (_model != null) {
            return _model.exportMoML();
        }
        return null;
    }

    /** Return the contained model.
     *  @return The contained model.
     */
    public NamedObj getContainedModel() {
        return _model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description this object, which includes a
     *  MoML description of the contained model within the &lt;configure&gt; tag.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
        if (_model != null) {
            output.write(_getIndentPrefix(depth) + "<configure>\n");
            _model.exportMoML(output, depth + 1);
            output.write(_getIndentPrefix(depth) + "</configure>\n");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The contained model. This is protected so that derived classes
     *  can provide a default model.
     */
    protected NamedObj _model;

}

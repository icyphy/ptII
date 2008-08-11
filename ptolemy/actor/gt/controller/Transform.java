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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.TransformationMode;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.data.ArrayToken;
import ptolemy.data.expr.ParserScope;
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

//////////////////////////////////////////////////////////////////////////
//// Transform

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Transform extends GTEvent implements Configurable {

    public Transform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        configurer = new Configurer(workspace());
        configurer.setContainer(this);

        _transformation = new TransformationRule(configurer, "Transformation");
        TransformationMode helper = new TransformationMode(_transformation,
                "_helper");
        helper.setPersistent(false);
        _clearURI(_transformation);

        mode = new TransformationMode(this, "mode");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Transform newObject = (Transform) super.clone(workspace);
        newObject.configurer = new Configurer(workspace);
        newObject.configurer.setContainer(newObject);
        newObject._transformation = (TransformationRule) _transformation.clone(
                workspace);
        try {
            newObject._transformation.setContainer(newObject.configurer);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        text = text.trim();
        if (!text.equals("")) {
            workspace().remove(_transformation);
            MoMLParser parser = new MoMLParser(workspace());
            _transformation = (TransformationRule) parser.parse(base,
                    new StringReader(text));
            TransformationMode helper = new TransformationMode(
                    _transformation, "_helper");
            helper.setPersistent(false);
            configurer.removeAllEntities();
            _transformation.setContainer(configurer);
            _clearURI(_transformation);
        }
    }

    public void fire(ArrayToken arguments) throws IllegalActionException {
        if (getName().equals("Compute")) {
            int i = 0;
            i++;
        }

        ParserScope scope = _getParserScope(arguments);
        actions.execute(scope);

        CompositeEntity model = _getModelVariable();
        model.setDeferringChangeRequests(false);
        boolean matched = mode.transform(mode.getWorkingCopy(_transformation),
                model);
        _setModelVariable(model);
        _setSuccessVariable(matched);

        _scheduleEvents(scope);
    }

    public String getConfigureSource() {
        return _configureSource;
    }

    public String getConfigureText() {
        return null;
    }

    public TypedActor[] getRefinement() {
        return new TypedActor[] {_transformation};
    }

    public Configurer configurer;

    public TransformationMode mode;

    public static class Configurer extends CompositeActor {

        public Configurer(Workspace workspace) {
            super(workspace);
        }

        public NamedObj getContainer() {
            if (_container == null) {
                return super.getContainer();
            } else {
                return _container;
            }
        }

        public void setContainer(Transform container) {
            _container = container;
        }

        private Transform _container;
    }

    protected void _exportMoMLContents(Writer output, int depth)
    throws IOException {
        super._exportMoMLContents(output, depth);

        String sourceSpec = "";

        if ((_configureSource != null) && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
        }

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec +
                ">\n");
        _transformation.exportMoML(output, depth + 1);
        output.write("</configure>\n");
    }

    protected TransformationRule _transformation;

    private static void _clearURI(NamedObj object)
    throws IllegalActionException, NameDuplicationException {
        URIAttribute attribute = (URIAttribute) object.getAttribute("_uri",
                URIAttribute.class);
        if (attribute != null) {
            attribute.setContainer(null);
        }
    }

    private String _configureSource;
}

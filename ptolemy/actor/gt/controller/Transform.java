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

import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.TransformationMode;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.TransformEventController.Factory;

//////////////////////////////////////////////////////////////////////////
//// Transform

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Transform extends GTEvent implements ConfigurableEntity {

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
    }

    public Parameter matched;

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

    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        text = text.trim();
        if (!text.equals("")) {
            MoMLParser parser = new MoMLParser(workspace());
            _configurer.removeAllEntities();
            parser.setContext(_configurer);
            parser.parse(base, source, new StringReader(text));
            _transformation = (TransformationRule) _configurer.entityList().get(
                    0);
            TransformationMode helper = new TransformationMode(_transformation,
                    "_helper");
            helper.setPersistent(false);
            _clearURI(_transformation);
        }
    }

    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        CompositeEntity model = getModelParameter().getModel();
        model.setDeferringChangeRequests(false);
        boolean isMatched = mode.transform(mode.getWorkingCopy(_transformation),
                model);
        getModelParameter().setModel(model);
        matched.setToken(BooleanToken.getInstance(isMatched));

        return data;
    }

   public String getConfigureSource() {
    return _configureSource;
}

    public String getConfigureText() {
        return null;
    }

    public Configurer getConfigurer() {
        return _configurer;
    }

    public TypedActor[] getRefinement() {
        return new TypedActor[] {_transformation};
    }

    public TransformationMode mode;

    public class EmbeddedConfigurer extends Configurer {

        public EmbeddedConfigurer(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
        }

        public NamedObj getContainer() {
            if (_container == null) {
                return super.getContainer();
            } else {
                return _container;
            }
        }

        public void setConfiguredObject(NamedObj configured) {
            super.setConfiguredObject(configured);
            _container = configured;
        }

        private NamedObj _container;
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

    /** Schedule the given actor, which is a refinement of this event, unless
        *  the refinement is a transformation rule (which is executed in the action
        *  instead).
        *
        *  @param refinement The refinement to be scheduled to fire.
        *  @return true if the refinement is scheduled; false otherwise.
        *  @throws IllegalActionException If thrown when trying to initialize the
        *  schedule of an ERGController refinement.
        */
       protected boolean _scheduleRefinement(TypedActor refinement)
               throws IllegalActionException {
           if (refinement == _transformation) {
               return false;
           } else {
               return super._scheduleRefinement(refinement);
           }
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

    private Configurer _configurer;

    public Factory controllerFactory;
}

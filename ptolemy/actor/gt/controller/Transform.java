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
import ptolemy.domains.fsm.kernel.ConfigurableEntity;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.domains.ptera.kernel.PteraErrorEvent;
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
public class Transform extends GTEvent implements ConfigurableEntity,
        TransformationListener {

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
            throw new IllegalActionException(this, t, "Error occurred in the " +
                    "transformation in " + getFullName() + ".");
        }
        getModelParameter().setModel(model);
        matched.setToken(BooleanToken.getInstance(isMatched));

        long elapsed = System.currentTimeMillis() - start;
        if (data == null) {
            _debug(new PteraDebugEvent(this, "Finish transformation (" +
                    (double) elapsed / 1000 + " sec)."));
        } else {
            _debug(new PteraDebugEvent(this, "Request refire (" +
                    (double) elapsed / 1000 + " sec)."));
        }

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

    public Factory controllerFactory;

    public Parameter matched;

    public TransformationMode mode;

    public Parameter defer;

    public static class EmbeddedConfigurer extends Configurer {

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
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

    protected boolean _isActiveRefinement(TypedActor refinement) {
        return refinement != _transformation;
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

    public void addObject(NamedObj object) {
        if (object instanceof Initializable) {
            Initializable initializable = (Initializable) object;
            try {
                initializable.preinitialize();
                initializable.initialize();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
        }
    }
}

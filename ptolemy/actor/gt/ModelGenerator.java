/*  An actor to open a window to display the specified model and apply its
    inputs to the model.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ModelGenerator

/**
An actor to open a window to display the specified model and apply its inputs
to the model.
If inputs are provided for the moml input port, they are expected to be
MoML strings that are to be applied to the model. This can be used, for
example, to create animations. If inputs are not provided for the moml input
port but for either the trigger port of the modelName port, then empty models
are generated with the name specified by the most updated value of the modelName
PortParameter.

@author  Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class ModelGenerator extends Source {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModelGenerator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.ModelGenerator");

        moml = new TypedIOPort(this, "moml", true, false);
        moml.setTypeEquals(BaseType.STRING);

        modelName = new PortParameter(this, "modelName");
        modelName.setTypeEquals(BaseType.STRING);

        output.setName("model");
        output.setTypeEquals(ActorToken.TYPE);
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelGenerator newObject = (ModelGenerator) super.clone(workspace);
        newObject._emptyModel = null;
        newObject._parser = new MoMLParser();
        return newObject;
    }

    /** Read the input at the input ports. If the moml port has a token, read it
     *  in as a string and parse the string into a model. If not, create an
     *  empty model. If the modelName is not an empty string, set the name of
     *  the model with the value of modelName. Produce the model to the output
     *  port.
     *
     *  @exception IllegalActionException If the ports cannot be read, or if the
     *   model cannot be produced.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        modelName.update();

        try {
            Entity entity;
            if (moml.isOutsideConnected() && moml.hasToken(0)) {
                String momlString = ((StringToken) moml.get(0)).stringValue();
                _parser.reset();
                try {
                    entity = (Entity) _parser.parse(momlString);
                } catch (SecurityException ex) {
                    // MoMLParser.parse(String) will fail in an unsigned applet.
                    try {
                        entity = (Entity) _parser.parse(null, momlString);
                    } catch (Exception ex1) {
                        throw new IllegalActionException(this, ex,
                                "Unable to parse moml:\n" + momlString);
                    }
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Unable to parse moml:\n" + momlString);
                }
            } else {
                if (_emptyModel == null) {
                    _emptyModel = new TypedCompositeActor(new Workspace());
                }
                entity = _emptyModel;
            }

            URI uri;
            String name = "";
            StringToken modelNameToken = (StringToken) modelName.getToken();
            if (modelNameToken != null) {
                name = modelNameToken.stringValue();
            }
            if (name.equals("")) {
                entity.setName("");
                uri = _getModelURI("model");
            } else {
                entity.setName(name);
                uri = _getModelURI(name);
            }
            URIAttribute attribute = (URIAttribute) entity.getAttribute("_uri",
                    URIAttribute.class);
            if (attribute == null) {
                attribute = new URIAttribute(entity, "_uri");
            }
            attribute.setURI(uri);

            output.send(0, new ActorToken(entity));
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Name duplicated.");
        } catch (URISyntaxException e) {
            throw new IllegalActionException(this, e, "URI syntax error.");
        }
    }

    /** Return true if the moml port is connected and has a token, or the
     *  modelName port is connected and has a token, or neither the modelName
     *  port nor the moml port is connected (in which case this actor serves as
     *  a source).
     *
     *  @return true if the actor is ready to fire; false otherwise.
     *  @exception IllegalActionException If connectivity of the input ports
     *   cannot be determined, or availability of the tokens cannot be tested.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        ParameterPort modelNamePort = modelName.getPort();
        return super.prefire()
                && (moml.isOutsideConnected() && moml.hasToken(0)
                        || modelNamePort.isOutsideConnected()
                        && modelNamePort.hasToken(0) || !moml
                        .isOutsideConnected()
                        && !modelNamePort.isOutsideConnected());
    }

    /** The port parameter for the model name.
     */
    public PortParameter modelName;

    /** The port to receive moml strings of the models.
     */
    public TypedIOPort moml;

    /** Given a model name, generate a URI for the model to be created with that
     *  name.
     *
     *  @param modelName The model name.
     *  @return The URI.
     *  @exception URISyntaxException If the URI cannot be determined.
     */
    private URI _getModelURI(String modelName) throws URISyntaxException {
        URI uri = URIAttribute.getModelURI(this);
        if (uri == null) {
            return new URI(modelName);
        } else {
            String path = uri.getPath();
            if (path == null || uri.toString().startsWith("jar:")) {
                // Probably a JarURL in Web Start
                File file = new File(modelName + ".xml");
                URI results = file.toURI();
                return results;
            } else {
                int pos = 0;
                pos = path.lastIndexOf('/');
                if (pos >= 0) {
                    path = path.substring(0, pos + 1) + modelName + ".xml";
                } else {
                    path += "/" + modelName + ".xml";
                }
            }
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                    uri.getPort(), path, uri.getQuery(), uri.getFragment());
        }
    }

    /** The empty model.
     */
    private Entity _emptyModel;

    /** The parser used to parse the moml strings.
     */
    private MoMLParser _parser = new MoMLParser();
}

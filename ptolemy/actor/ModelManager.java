/* Modal models.

 Copyright (c) 1999-2002 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.actor.*;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.HSDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.*;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.data.XmlToken;
import ptolemy.data.type.BaseType;

import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.io.File;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// ModelManager
/**
This is a typed composite actor designed to manage a model.
......



@author Yang Zhao, Xiaojun Liu
@version $Id$
@since Ptolemy II 2.0
*/
public class ModelManager extends TypedCompositeActor{

    /** Construct a model manager in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public ModelManager(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        change = new TypedIOPort(this, "change", true, false);
        change.setMultiport(true);
        change.setTypeEquals(BaseType.STRING);
        modelURL = new Parameter(this, "modelURL", new StringToken(""));
        modelURL.setTypeEquals(BaseType.STRING);
        _init();
    }

    /** Construct a model manager with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ModelManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        change = new TypedIOPort(this, "change", true, false);
        change.setMultiport(true);
        change.setTypeEquals(BaseType.STRING);
        modelURL = new Parameter(this, "modelURL", new StringToken(""));
        modelURL.setTypeEquals(BaseType.STRING);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    public Parameter modelURL;

    public TypedIOPort change = null;
    /** A director className string, configured using a ChoiceStyle
     * attribute. */
    public StringAttribute directorClass;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the _director or other property. */
    public void attributeChanged(Attribute attribute)
	throws IllegalActionException {
        if (attribute == modelURL) {
            try {
                StringToken URLToken = (StringToken)modelURL.getToken();
                if (URLToken == null) {
                    System.out.println("### please provide the URL of the model. \n");

                } else {
                    _source = URLToken.stringValue();
                    if (!_source.equals("")) {
                        URL url = new URL(_source);
                        //System.out.println("--- the URL of the model file: " + url.toString() + "\n");
                        MoMLParser parser = new MoMLParser();
                        parser.setMoMLFilters(BackwardCompatibility.allFilters());

                        // Filter out any graphical classes.
                        parser.addMoMLFilter(new RemoveGraphicalClasses());
                        _topLevel = (NamedObj) parser.parse(null, url.openStream());
                        if (_topLevel instanceof CompositeActor) {
                            /* set the uri attribute. When configuration.openModel(_topLevel)is
                            called, the effigy will be added to the modelDirectory and one can run
                            the model in the created tableau.
                            */
                            URIAttribute uriAttribute
                                     = new URIAttribute(_topLevel, "_uri");
                            uriAttribute.setURL(url);

                            //_topLevel.addChangeListener(this);
                        }
                        else {
                            // currently do nothing...

                        }
                    }
                    else {
                        System.out.println("### please provide the URL of the model. \n");

                }

            }

            } catch (Exception ex) {
                throw new IllegalActionException(this, ex.getMessage());
            }
        }
        super.attributeChanged(attribute);
    }

    /** Override the base class to ensure that the _controller private
     *  variable is reset to the controller of the cloned object.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return The new Entity.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ModelManager newModel = (ModelManager)super.clone(workspace);
        //newModel._controller = (FSMActor)newModel.getEntity("_Controller");
        return newModel;
    }

    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking fire");
        }
        System.out.println("--- invoke fire of model manager.  \n");
        for (int i = 0; i < change.getWidth(); i++) {
            if (change.hasToken(i)) {
                StringToken in = (StringToken)change.get(i);
                if (_debugging) {
                    _debug("change request text: ", in.stringValue());
                }
                MoMLChangeRequest request = new MoMLChangeRequest(
                    this,            // originator
                    _topLevel,          // context
                    in.stringValue(), // MoML code
                    null);           // base

                _topLevel.requestChange(request);

            }
        }
    }

    public void initialize() throws IllegalActionException {
        System.out.println("--- invoke initialize of model manager, and do nothing. \n");
    }

    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking prefire");
        }
        for (int i = 0; i < change.getWidth(); i++) {
            if (change.hasToken(i)) {
                System.out.println("--- receiver new change request. \n");
                return true;
            }
        }
        return false;
    }

    public boolean postfire() {
        return true;
    }

    public void preinitialize() throws IllegalActionException {
        System.out.println("--- invoke preinitialize of model manager, and do nothing. \n");
    }

    public boolean isOpaque() {
        return true;
    }

    public void wrapup() {
        // do nothing
    }




    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the model.
    private void _init()
            throws IllegalActionException, NameDuplicationException {

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        getMoMLInfo().className = "ptolemy.actor.ModelManager";

        //FIXME: add a director to the ModelManager to avoid the error:
        //"_workspace.doneReading() is called before _workspace.getReadAccess().
        //what should be the proper way to fix the error?
        new ptolemy.domains.de.kernel.DEDirector(this, "director");

        // Putting this attribute in causes look inside to be handled
        // by it.
        new ModalTableauFactory(this, "_tableauFactory");

        // Create a more reasonable default icon.
	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" width=\"60\" " +
                "height=\"40\" style=\"fill:red\"/>\n" +
                "<rect x=\"-28\" y=\"-18\" width=\"56\" " +
                "height=\"36\" style=\"fill:lightgrey\"/>\n" +
                "<ellipse cx=\"0\" cy=\"0\"" +
                " rx=\"15\" ry=\"10\"/>\n" +
                "<circle cx=\"-15\" cy=\"0\"" +
                " r=\"5\" style=\"fill:white\"/>\n" +
                "<circle cx=\"15\" cy=\"0\"" +
                " r=\"5\" style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _source;

    private NamedObj _topLevel;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ModalTableauFactory

    /** A tableau factory that opens an editor on the contained controller
     *  rather than this composite actor.  This is triggered by look inside.
     */
    public class ModalTableauFactory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public ModalTableauFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }



        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a tableau for the specified effigy, which is assumed to
         *  be an effigy for an instance of ModalModel.  This class
         *  defers to the configuration containing the specified effigy
         *  to open a tableau for the embedded controller.
         *  @param effigy The model effigy.
         *  @return A tableau for the effigy, or null if one cannot be created.
         *  @exception Exception If the factory should be able to create a
         *   Tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            Configuration configuration = (Configuration)effigy.toplevel();
            ModelManager model = (ModelManager)((PtolemyEffigy)effigy).getModel();
            /*if (model._topLevel instanceof CompositeActor) {
                return configuration.openModel(model._topLevel);
            } else {
                //FIXME: what should it do here?
                return null;
            }*/
            System.out.println(getFullName() + " handle look inside");
            return configuration.openModel(model._topLevel);
        }
    }
}

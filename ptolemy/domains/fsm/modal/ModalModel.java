/* Modal models.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

package ptolemy.domains.fsm.modal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import ptolemy.actor.IODependency;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.HSDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ModalModel
/**
This is a typed composite actor designed to be a modal model.
Inside the modal model is a finite-state machine controller, and
inside each state in the FSM is a refinement model. To use this
actor, just drag it into a model, and look inside to start constructing
the controller.  You may add ports to get inputs and outputs, and
add states to the controller.  You may add one or more refinements
to a state (each of these refinements will be executed when this
actor is executed).  Each refinement is required to have its own
director, so you will need to choose a director.
<p>
The controller is a finite-state machine (FSM), which consists of
states and transitions.  One of the states is an initial state.
When this actor executes, if the current state has a refinement,
then that refinement is executed.  Then the guards on all the outgoing
transitions of the current state are evaluated, and if one of those
guards is true, then the transition is taken.  Taking the transition
means that the actions associated with the transition are executed
(which can result in producing outputs), and the new current state is
the state at the destination of the transition.  It is an error if
more than one of the guards evaluates to true.
<p>
To add a state, click on a state button in the toolbar, or drag
in a state from the library at the left.  To add a transition,
position the mouse over the source state, hold the control button,
and drag to the destination state.  The destination state may be
the same state, in which case the transition is used simply to
execute its actions.
<p>
Adding or removing ports in this actor results in the same ports appearing
or disappearing in the FSM controller and in each of the refinements.
Similarly, adding or removing ports in the controller or in the
refinements results in this actor and the other refinements
reflecting the same change to the ports.  That is, this actor,
the controller, and the refinments all contain the same ports.
<p>
There is one subtlety regarding ports however.  If you add an
output port to a refinement, then the corresponding port in the
controller will be both an input and an output.  The reason for
this is that the controller can access the results of executing
a refinement in order to choose a transition.
<p>
This class is designed to work closely with ModalController and
Refinement, since changes to ports can be initiated in this class
or in those. It works with continuous-time as well as discrete-time
models.
<p>
This class also fulfills the CTEventGenerator interfact so that
it can report events generated inside.

@see ModalController
@see Refinement
@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ModalModel extends CTCompositeActor {

    /** Construct a modal model in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public ModalModel(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a modal model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ModalModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A director className string, configured using a ChoiceStyle
     * attribute. */
    public StringAttribute directorClass;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the _director or other property. */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute.getName().equals("directorClass")) {
            FSMDirector director = (FSMDirector)getDirector();
            Class newDirectorClass = null;
            try {
                if (director != null) {
                    // Delete the old director.
                    director.setContainer(null);
                }
                newDirectorClass =
                    Class.forName
                    (((StringAttribute)attribute).getExpression());
                Constructor newDirectorConstructor =
                    newDirectorClass.getConstructor
                    (new Class[]{CompositeEntity.class, String.class});
                director = (FSMDirector)newDirectorConstructor.newInstance
                    (new Object[]{this, "_Director"});
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (ClassNotFoundException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (NoSuchMethodException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (InstantiationException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (IllegalAccessException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (InvocationTargetException ex) {
                throw new IllegalActionException(ex.toString());
            }
            director.controllerName.setExpression("_Controller");
            String directorClass =
                ((StringAttribute)attribute).getExpression();
            if (directorClass.equals(modalDirectorClassNames[2])) {
                // The director is an HDFFSMDirector.  
                _controller.setHDFFSMActor(true);
                _directorChanged = true;
            } else if (_directorChanged &&
              (directorClass.equals(modalDirectorClassNames[0])
              || (directorClass.equals(modalDirectorClassNames[1])))) {
                _controller.setHDFFSMActor(false);
            }
        }
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
        ModalModel newModel = (ModalModel)super.clone(workspace);
        newModel._controller = (FSMActor)newModel.getEntity("_Controller");
        return newModel;
    }   

    /** Return an instance of DirectedGraph, where the nodes are IOPorts,
     *  and the edges are the relations between ports. The graph shows 
     *  the dependencies between the input and output ports. If there is
     *  a path between a pair, input and output, they are dependent. 
     *  Otherwise, they are independent.
     */
    public IODependency getIODependencies() {
        if (_ioDependency == null) {
            _ioDependency = 
                new IODependencyOfModalModel(this);
        }
        //_ioDependency.validate();
        return _ioDependency;
    }

    /** Get the FSM controller.
     */
    public FSMActor getController() {
       return _controller;
    }

    /** Create a new director for use in this composite.  This base
     *  class returns an instance of FSMDirector, but derived classes
     *  may return a subclass.  Note that this method is called in the
     *  constructor, so derived classes will not have been fully
     *  constructed when it is called.
     *  @exception IllegalActionException If constructing the director
     *   triggers an exception.
     *  @exception NameDuplicationException If there is already a director
     *   with name "_Director".
     *  @return A new director.
     */
    public FSMDirector newDirector()
            throws IllegalActionException, NameDuplicationException {
        // FIXME: we have to be careful on choosing FSMDirector or
        // HSDirector.
        return new HSDirector(this, "_Director");
    }
    
    /** Create a new port with the specified name in this entity, the
     *  controller, and all the refinements.  Link these ports so that
     *  if the new port is set to be an input, output, or multiport, then
     *  the change is mirrored in the other ports.  The new port will be
     *  an instance of ModalPort, which extends TypedIOPort.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    public Port newPort(String name)
            throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            ModalPort port = new ModalPort(this, name);
            // Create mirror ports.
            Iterator entities = entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                if (entity instanceof ModalController) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((ModalController)entity)._mirrorDisable = true;
                            /*Port newPort = */ entity.newPort(name);
                            /* No longer needed since Yuhong modified
                             * the type system to allow UNKNOWN. EAL
                             if (newPort instanceof TypedIOPort) {
                             ((TypedIOPort)newPort).setTypeSameAs(port);
                             }
                            */
                        } finally {
                            ((ModalController)entity)._mirrorDisable = false;
                        }
                    }
                } else if (entity instanceof Refinement) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((Refinement)entity)._mirrorDisable = true;
                            /*Port newPort = */ entity.newPort(name);
                            /* No longer needed since Yuhong modified
                             * the type system to allow UNKNOWN. EAL
                             if (newPort instanceof TypedIOPort) {
                             ((TypedIOPort)newPort).setTypeSameAs(port);
                             }
                            */
                        } finally {
                            ((Refinement)entity)._mirrorDisable = false;
                        }
                    }
                }
            }
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "ModalModel.newPort: Internal error: " +
                    ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Class names of directors compatible with this
     * ModalModel. Derived classes should override this if they desire
     * additional or different directors. */
    protected String[] modalDirectorClassNames = {
        "ptolemy.domains.fsm.kernel.HSDirector",
        "ptolemy.domains.fsm.kernel.FSMDirector",
        "ptolemy.domains.hdf.kernel.HDFFSMDirector"
    };

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the model.
    private void _init()
            throws IllegalActionException, NameDuplicationException {

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        getMoMLInfo().className = "ptolemy.domains.fsm.modal.ModalModel";

        // This actor contains an FSMDirector and an FSMActor.
        // The names are preceded with underscore to minimize the
        // likelihood of a conflict with a user-desired name.
        // NOTE This director will be described in the exported MoML
        // file, so when that is encountered, it will match this director.
        FSMDirector director = newDirector();
        String directorClassName = director.getClass().getName();
        // drop the "L" to get the className
        directorClassName = directorClassName.substring(1);
        // Create and configure a "director" choice attribute of a
        // ModalModel:
        directorClass = new StringAttribute(this, "directorClass");
        directorClass.setExpression(modalDirectorClassNames[0]);
        {
            ChoiceStyle style = new ChoiceStyle(directorClass, "style");
            StringAttribute a;
            for (int i = 0; i < modalDirectorClassNames.length; i++) {
                a = new StringAttribute(style, "style"+i);
                a.setExpression(modalDirectorClassNames[i]);
            }
        }

        // NOTE: It would be much nicer if the director created the
        // controller it likes (or has it configured) and returned it
        // (zk 2002/09/11)

        // NOTE This controller will be described in the exported MoML
        // file, so when that is encountered, it will match this.
        _controller = new ModalController(this, "_Controller");

        director.controllerName.setExpression("_Controller");

        // NOTE This library will be described in the exported MoML
        // file, so when that is encountered, it will match this.
        // Configure the controller so it has the appropriate library.
//        LibraryAttribute attribute = new LibraryAttribute(
//                _controller, "_library");
//        CompositeEntity library = new CompositeEntity(new Workspace("Library"));
//        library.setName("state library");
//        attribute.setLibrary(library);
//        State state = new State(library, "state");
//        new Attribute(state, "_centerName");
//        new HierarchicalStateControllerFactory(state, "_controllerFactory");

        // Import utilities file (including annotations, etc.)
        // Do this as a MoML change request so we can easily read the library
        // spec from a file, rather than replicating it here.
        // NOTE: Because this library has no association with a director,
        // the change will always be executed immediately.
        // This should be OK, since the library is in its own workspace,
        // and modifying the library cannot possibly affect the executing
        // model.
//        String moml = "<input source=\"ptolemy/configs/basicUtilitiesFSM.xml\"/>";
//        MoMLChangeRequest request = new MoMLChangeRequest(
//                this, library, moml);
//        library.requestChange(request);

        // Putting this attribute in causes look inside to be handled
        // by it.
        //new ModalTableauFactory(this, "_tableauFactory");

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

    /** The FSM controller. */
    private FSMActor _controller;
    
    // A flag indicating the director has been changed to HDFFSMDirector
    // by the user. This prevents setting the HDFFSMActor flag before
    // the FSMActor is created.
    private boolean _directorChanged = false;
    
    private IODependencyOfModalModel _ioDependency;
    
}

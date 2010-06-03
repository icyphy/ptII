/*  This parameter, when inserted into a port, causes the port to display its unconsumed inputs.

 @Copyright (c) 2007-2010 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.giotto.kernel;

import java.util.List;
import java.util.Random;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// MonitorReceiverContents
///instantiate ptolemy.vergil.actor.lib.MonitorReceiverAttribute
// This quantity manager leverages the code in ptolemy.vergil.actor.lib.MoniterReceiverContents
// written by Edward A. Lee.
/**
 This parameter, when inserted into a model or an opaque composite actor,
 causes all input ports to acquire an attribute that makes them display
 their contents on the screen.  This works by piggybacking on the
 initialize() method of the container to insert the relevant parameters
 into the ports. It also piggybacks on postfire() and wrapup() to issue
 a ChangeRequest, which causes a repaint of the screen in Vergil.
 To stop monitoring the queue contents, simply delete
 this attribute from the model.

 @author  Edward A. Lee, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class GiottoQuantityManager extends SingletonAttribute implements
        Decorator {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GiottoQuantityManager(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" " + "width=\"180\" height=\"20\" "
                + "style=\"fill:#00FFFE\"/>\n" + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:15; font-family:SansSerif; fill:blue\">\n"
                + "Timing Manager\n" + "</text>\n" + "</svg>\n");

        // Hide the name.
        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);
        generator = new Random();
        try {
            errorAction = new StringParameter(this, "errorAction");
            errorAction.setExpression("Warn");
            errorAction.addChoice("Warn");
            errorAction.addChoice("Reset");
            errorAction.addChoice("TimedUtilityFunction");
            errorAction.addChoice("ErrorTransition");
        } catch (NameDuplicationException ne) {
            if (_debugging) {
                _debug("I should handle this error in a better way later.");
            }
        } catch (IllegalActionException ie) {
            if (_debugging) {
                _debug("I should handle this error in a better way later.");
            }

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify the container. If the container is not the same as the
     *  previous container, then stop monitoring queue contents in the
     *  previous container, and start monitoring them in the new one.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    public void setContainer(final NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        System.out.println("set container method called");
        NamedObj previousContainer = getContainer();
        if (previousContainer == container) {
            return;
        }

        if ((previousContainer != null)
                && (previousContainer instanceof CompositeActor)) {
            // _piggybackContainer should be non-null, but we check anyway.
            if (_piggybackContainer != null) {
                _piggybackContainer.removePiggyback(_executable);
            }
            _executable = null;

            /*   List<Actor> entities = ((CompositeActor) previousContainer)
               .deepEntityList();
            for (Actor entity : entities) {
            List<IOPort> ports = entity.inputPortList();
            for (IOPort port : ports) {
               List<MonitorReceiverAttribute> attributes = port
                       .attributeList(MonitorReceiverAttribute.class);
               for (MonitorReceiverAttribute attribute : attributes) {
                   attribute.setContainer(null);
               }
            }
            }*/
            System.out
                    .println("I should remove all the attributes that were added here.");

        }
        super.setContainer(container);

        if (container != null && container instanceof CompositeActor) {
            if (_executable == null) {
                // The inner class will be piggybacked as an executable for the container to
                // execute change request at the appropriate times. These change request will
                // lead to repaints of the GUI.
                _executable = new Executable() {

                    public void initialize() throws IllegalActionException {

                    }

                    // Request repaint on postfire() and wrapup().
                    public boolean postfire() {
                        ChangeRequest request = new ChangeRequest(this,
                                "SetVariable change request", true /*Although this not a structural change in my point of view
                                                                   , we however for some reason need to specify it is, otherwise the GUI won't update.*/
                        ) {
                            protected void _execute()
                                    throws IllegalActionException {
                            }
                        };
                        // To prevent prompting for saving the model, mark this
                        // change as non-persistent.
                        request.setPersistent(false);
                        requestChange(request);
                        return true;
                    }

                    public void wrapup() {
                        ChangeRequest request = new ChangeRequest(this,
                                "SetVariable change request", true) {
                            protected void _execute()
                                    throws IllegalActionException {
                            }
                        };
                        // To prevent prompting for saving the model, mark this
                        // change as non-persistent.
                        request.setPersistent(false);
                        requestChange(request);
                    }

                    // All other methods are empty.
                    public void fire() throws IllegalActionException {
                        System.out
                                .println("Fire method called in the quantity manager");

                        /*workspace().getWriteAccess();
                        List<Actor> entities = ((CompositeActor) container)
                                .deepEntityList();
                        for (Actor actor : entities) {

                            // Add in execution time stuff here.
                            //  double actorET;
                            double actorWCET;
                            double actorGrace;
                            Attribute executionTime = ((Entity) actor)
                                    .getAttribute("ET");
                            Attribute WCET = ((Entity) actor)
                                    .getAttribute("WCET");
                            Attribute Grace = ((Entity) actor)
                                    .getAttribute("grace");
                            actorWCET = ((DoubleToken) ((Variable) WCET)
                                    .getToken()).doubleValue();
                            actorGrace = ((DoubleToken) ((Variable) Grace)
                                    .getToken()).doubleValue();

                            double t = generator.nextDouble() * actorWCET; // I multiply by actorWCET in an attempt to scale
                            Parameter dummyP = (Parameter) executionTime;
                            if (_debugging) {
                                _debug("in the dummy parameter the name is : "
                                        + dummyP.getName());
                                _debug("it has the value " + t
                                        + " with wcet set to " + actorWCET
                                        + " and grace set to " + actorGrace);
                                _debug(" and is attatched to actor "
                                        + dummyP.getContainer());
                            }
                            dummyP.setExpression(Double.toString(t));
                            if ((t + actorGrace) > actorWCET) {
                                System.out
                                        .println("I should now raise an error");

                                   _handleModelError((NamedObj) actor,
                                           new IllegalActionException(this, "total ET  of ("
                                                   + t + ") is larger than WCET (" + actorWCET
                                                   + ") for actor " + actor.getDisplayName()));
                                                   
                            }
                        }*/
                        System.out
                                .println("At the end of the fire method in the quantity manager");
                    }

                    public boolean isFireFunctional() {
                        return true;
                    }

                    public boolean isStrict() {
                        return true;
                    }

                    public int iterate(int count) {
                        return Executable.COMPLETED;
                    }

                    public boolean prefire() throws IllegalActionException {
                        return true;
                    }

                    public void stop() {
                    }

                    public void stopFire() {
                    }

                    public void terminate() {
                    }

                    public void addInitializable(Initializable initializable) {
                    }

                    public void preinitialize() throws IllegalActionException {
                        /*double wcet = 0;
                        double _periodValue = 0;

                        Attribute dirWCET = container.getAttribute("WCET");
                        if (dirWCET != null) {
                            wcet = ((DoubleToken) ((Variable) dirWCET)
                                    .getToken()).doubleValue();
                        }
                        if (_debugging) {
                            _debug("the WCET time seen by the director is "
                                    + wcet + " and the period is "
                                    + _periodValue);
                        }
                        if (wcet > _periodValue) {

                            if (_debugging) {
                                _debug("throw an exception");
                            }
                            // this is the static check before execution
                            throw new IllegalActionException(
                                    container,
                                    "total WCET of ("
                                            + wcet
                                            + ") is larger than period ("
                                            + _periodValue
                                            + ") for actor "
                                            + ((CompositeActor) (getContainer()))
                                                    .getDisplayName());

                        } //end of if  
                        if (_debugging) {
                            _debug("at the end of preinitialize in the timing quantity manager.");
                        }*/
                    }

                    public void removeInitializable(Initializable initializable) {
                    }
                };
            }

            _piggybackContainer = (CompositeActor) container;
            _piggybackContainer.addPiggyback(_executable);
        }

    }

    /** Set the current type of the decorated attributes.
     *  The type information of the parameters are not saved in the
     *  model hand hence this has to be reset when reading the model
     *  again.
     *  @param decoratedAttributes The decorated attributes.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     */
    public void setTypesOfDecoratedVariables(
            DecoratedAttributes decoratedAttributes)
            throws IllegalActionException {
    }

    /** Remove the decorated attributes
     */
    public void removeDecoratedAttributes(NamedObj target)
            throws IllegalActionException, NameDuplicationException {
        System.out
                .println("create decorated attributes to be called for Giotto quantityManager");
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) target
                .getContainer()).deepEntityList()) {
            NamedObj temp = (NamedObj) actor;
            if (_debugging) {
                _debug("temp has name " + temp.getDisplayName());
            }

            List<Parameter> paramList = temp.attributeList();//new Parameter(temp, "WCET");

            for (Parameter param : paramList) {
                if (param.getDisplayName().equals("WCET")) {
                    param.setPersistent(false);
                }

            }

        }

    }

    /** Return the decorated attributes for the target NamedObj.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj. 
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratedAttributes createDecoratedAttributes(NamedObj target)
            throws IllegalActionException, NameDuplicationException {
        System.out
                .println("create decorated attributes to be called for Giotto quantityManager");
        return new GiottoDecoratedAttributesImplementation2(target, this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The executable that creates the monitor attributes in initialize(). */
    private Executable _executable;

    /** The last container on which we piggybacked. */
    private CompositeActor _piggybackContainer;

    private Random generator;

    public StringParameter errorAction;
}

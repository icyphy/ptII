/*  This parameter, when inserted into a port, causes the port to display its unconsumed inputs.

 @Copyright (c) 2010 The Regents of the University of California.
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
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// MonitorReceiverContents
/**
 * A Parameter that acilitates an aspect oriented approach to
 * management and possible mitigation of timing errors in a
 * specification that uses timing but has no mechanisms in it's
 * specification for timing error handling. This parameter is also a
 * decorator pattern that allows all the actors present to be
 * decorated with WCET, execution time as well as grace parameters.
 * In addition, the presence of the quantity manager indicates a
 * desire to incorporate execution timing as well as error handling
 * into a Giotto specification.  This works by piggybacking on the
 * fire(), and postfire() methods of the container to determine
 * execution times and determine when a timing overrun occurs.  The
 * piggybacks on postfire() and wrapup() issue a ChangeRequest, which
 * causes a repaint of the screen in Vergil.  To remove actual
 * execution timing and timing error management from a model, simply
 * delete this attribute from the model.
 * 
 * <p>The parameter can be instantinated by instantiating an attribute of type
 * {@link ptolemy.domains.giotto.kernel.GiottoQuantityManager}.</p>
 *
 * @author Shanna-Shaye Forbes. Based on the MonitorReceiverContents.java created by Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (sssf)
 *  @Pt.AcceptedRating Red (sssf)
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

        try {
            errorAction = new StringParameter(this, "errorAction");
            errorAction.setExpression("Warn");
            errorAction.addChoice("Warn");
            errorAction.addChoice("TimedUtilityFunctionv1");
            errorAction.addChoice("TimedUtilityFunctionv2");
            errorAction.addChoice("TimedUtilityFunctionv3");
            errorAction.addChoice("TimedUtilityFunctionv4");
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
        generator = new Random();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>filename</i>, then close
     *  the current file (if there is one) and open the new one.
     *  If the specified attribute is <i>period</i> or
     *  <i>synchronizeToRealTime</i>, then cache the new values.
     *  If the specified attribute is <i>timeResolution</i>,
     *  then cache the new value.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>filename</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == errorAction) {
            String errorActionName = errorAction.getExpression().trim();

            if (errorActionName.equals("Warn")) {
                _errorAction = ErrorAction.warn;
            } else if (errorActionName.equals("TimedUtilityFunctionV1")) {
                _errorAction = ErrorAction.timedutilityfunctionv1;
            } else if (errorActionName.equals("TimedUtilityFunctionV2")) {
                _errorAction = ErrorAction.timedutilityfunctionv2;
            } else if (errorActionName.equals("TimedUtilityFunctionV3")) {
                _errorAction = ErrorAction.timedutilityfunctionv3;
            } else if (errorActionName.equals("TimedUtilityFunctionV4")) {
                _errorAction = ErrorAction.timedutilityfunctionv4;
            } else if (errorActionName.equals("ErrorTransition")) {
                _errorAction = ErrorAction.errorTransition;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized action on error: " + errorActionName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

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
        if (_debugging) {
            _debug("set container method called");
        }
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
            String name;
            try {
                workspace().getWriteAccess();
                List<Actor> entities = ((CompositeActor) previousContainer)
                        .deepEntityList();
                for (Actor entity : entities) {
                    List<Attribute> paramList = ((Entity) entity)
                            .attributeList();
                    for (Attribute param : paramList) {
                        name = param.getDisplayName();
                        if (name.equals("WCET") || name.equals("ET")
                                || name.equals("grace")) {
                            param.setPersistent(false);
                        }
                    }
                }
            } catch (Exception ex) { // this should later be replaced with a more specific exception
                throw new InternalErrorException(ex);
            } finally {
                workspace().doneTemporaryWriting();
            }
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
                    public boolean postfire() throws IllegalActionException {
                        System.out
                                .println("I should now check to see if there are cumulative overruns");

                        // here check to see if there were cumulative timing overruns
                        // cumulative execution times
                        double actorExecutionTimes = 0;
                        // cumulative worst case execution times
                        double actorWorstCaseExecutionTimes = 0;
                        // cumulative grace times
                        double actorGraceTimes = 0;
                        //for()
                        List<Actor> entities = ((CompositeActor) container)
                                .deepEntityList();
                        for (Actor actor : entities) {

                            Attribute executionTime = ((Entity) actor)
                                    .getAttribute("ET");
                            Attribute WCET = ((Entity) actor)
                                    .getAttribute("WCET");
                            Attribute Grace = ((Entity) actor)
                                    .getAttribute("grace");
                            try {
                                actorWorstCaseExecutionTimes += ((DoubleToken) ((Variable) WCET)
                                        .getToken()).doubleValue();
                                actorGraceTimes += ((DoubleToken) ((Variable) Grace)
                                        .getToken()).doubleValue();
                                actorExecutionTimes += ((DoubleToken) ((Variable) executionTime)
                                        .getToken()).doubleValue();
                            } catch (IllegalActionException ex) {
                                ex.printStackTrace(); // replace later with more appropriate behavior
                            }

                        }
                        if (_debugging) {
                            _debug("execution times are: "
                                    + actorExecutionTimes
                                    + " grace times are: " + actorGraceTimes
                                    + " actor wcet times are: "
                                    + actorWorstCaseExecutionTimes);
                        }
                        if ((actorExecutionTimes + actorGraceTimes) > actorWorstCaseExecutionTimes) {
                            if (_debugging) {
                                _debug("There was a timing overrun");
                            }
                            boolean bb;
                            bb = _handleModelError(
                                    container,
                                    new IllegalActionException(
                                            container,
                                            "total ET  of ("
                                                    + actorExecutionTimes
                                                    + ") is larger than WCET ("
                                                    + actorWorstCaseExecutionTimes
                                                    + " ) and grace time of ("
                                                    + actorGraceTimes
                                                    + ") for actor "
                                                    + container
                                                            .getDisplayName()));
                            System.out
                                    .println("The model error was handeled successfully: "
                                            + bb);

                        }

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

                        // assign an execution time to each actor

                        workspace().getWriteAccess();
                        List<Actor> entities = ((CompositeActor) container)
                                .deepEntityList();
                        for (Actor actor : entities) {

                            double actorWCET;

                            Attribute executionTime = ((Entity) actor)
                                    .getAttribute("ET");
                            Attribute WCET = ((Entity) actor)
                                    .getAttribute("WCET");
                            actorWCET = ((DoubleToken) ((Variable) WCET)
                                    .getToken()).doubleValue();

                            double t = generator.nextDouble() * actorWCET; // I multiply by actorWCET in an attempt to scale
                            Parameter dummyP = (Parameter) executionTime;

                            dummyP.setExpression(Double.toString(t));
                        }
                        workspace().doneTemporaryWriting();
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

    /** Remove the decorated attributes.
     *  @param target The decorated attribute to remove   
     */
    public void removeDecoratedAttributes(NamedObj target) {
        System.out
                .println("create decorated attributes to be called for Giotto quantityManager");
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) target
                .getContainer()).deepEntityList()) {
            NamedObj temp = (NamedObj) actor;
            if (_debugging) {
                _debug("temp has name " + temp.getDisplayName());
            }

            List<Parameter> paramList = temp.attributeList();

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

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false if the
     *   error is not handled.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.///
     */
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {

        if (_debugging) {
            _debug("Handle Model Error Called for GiottoDirector");
        }

        NamedObj dummyContainer = this.getContainer();
        NamedObj parentContainer = dummyContainer.getContainer();
        if (parentContainer != null) {
            return parentContainer.handleModelError(context, exception);
        } else {
            throw new IllegalActionException(this,
                    "Unable to set error transition. This is the top most director ");
        }

    }

    private boolean _handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {

        if (_errorAction == ErrorAction.warn) {
            if (_debugging) {
                _debug("an error was detected in " + context.getFullName());
            }
            return true;
        } else if (_errorAction == ErrorAction.timedutilityfunctionv2) {
            String temp = exception.toString();

            if (_debugging) {
                _debug("I should check to see if I'm within the acceptable range for the timed utility function in v2");
                _debug("Actor name is: " + context.getName());
                _debug("The exception string contains: " + temp);
            }
            int i, j, k, l = 0;
            i = temp.indexOf("(");
            j = temp.indexOf(")");
            k = temp.lastIndexOf("(");
            l = temp.lastIndexOf(")");
            double wcet = Double.parseDouble(temp.substring(i + 1, j));
            double periodvalue = Double.parseDouble(temp.substring(k + 1, l));
            if (_debugging) {
                _debug("wcet value is " + wcet + " and period value is "
                        + periodvalue);
                //if()
            }
            return false;
        } else if (_errorAction == ErrorAction.timedutilityfunctionv1) {
            if (_debugging) {
                _debug("I should check to see if I'm within the acceptable range for the timed utility function");
            }
            String temp = exception.toString();
            int i, j, k, l = 0;
            i = temp.indexOf("(");
            j = temp.indexOf(")");
            k = temp.lastIndexOf("(");
            l = temp.lastIndexOf(")");
            double wcet = Double.parseDouble(temp.substring(i + 1, j));
            double periodvalue = Double.parseDouble(temp.substring(k + 1, l));
            if (_debugging) {
                _debug("wcet value is " + wcet + " and period value is "
                        + periodvalue);
                //if()
            }
            return true;
        } else if (_errorAction == ErrorAction.errorTransition) {
            if (_debugging) {
                _debug("I should take the errorTransition");
            }
            return handleModelError(context, exception);
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The errorAction operator.  This is a string-valued attribute
     *  that defaults to "warn".
     */
    public StringParameter errorAction;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The executable that creates the monitor attributes in initialize(). */
    private Executable _executable;

    /** The last container on which we piggybacked. */
    private CompositeActor _piggybackContainer;

    private Random generator;

    //  An indicator for the error action to take.
    private ErrorAction _errorAction;

    /// Enumeration of the different ways to handle errors
    private enum ErrorAction {
        warn, timedutilityfunctionv1, timedutilityfunctionv2, timedutilityfunctionv3, timedutilityfunctionv4, errorTransition
    }
}

/** A class representing the type of an ArrayToken.

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
package ptolemy.domains.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * A collection of static methods used in the Ptides domain to determine static
 * actor properties.
 * <p>The properities are:
 * <ul>
 * <li>Is the actor a sensor?</li>
 * <li>Is the actor an actuator?</li>
 * <li>What is the WCET of the actor?</li>
 * <li>Can the actor be triggered by an event on the given port? Some actors,
 * for instance, have trigger ports where events don't cause an immediate firing
 * of the actor but determine if the actor produces an output token at the next
 * scheduled time. An example is the clock actor. Another example is the TDL
 * module, tokens on input ports of TDL modules don't cause a firing of the
 * actor, the firing is caused by pure events scheduled by the TDL module.</li>
 * <li>Does this actor have to be fired at real time?</li>
 * </ul>.</p>
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 8.0
 */
@Deprecated
public class PtidesActorProperties {

    /**
     * Returns true if the actor is an actuator. A parameter of an actuator
     * actor called "isActuator" is true if the actor is an actuator.
     *
     * @param actor
     *            The actor which might be an actuator.
     * @return True if the actor is an actuator.
     */
    public static boolean isActuator(Actor actor) {
        try {
            if (actor == null) {
                return false;
            } else {
                Parameter parameter = (Parameter) ((NamedObj) actor)
                        .getAttribute("isActuator");

                if (parameter != null) {
                    BooleanToken intToken = (BooleanToken) parameter.getToken();

                    return intToken.booleanValue();
                } else {
                    return false;
                }
            }
        } catch (ClassCastException ex) {
            return false;
        } catch (IllegalActionException ex) {
            return false;
        }
    }

    /**
     * Returns true if given actor is a sensor. A parameter "isSensor" is set to
     * true if the actor is a sensor.
     *
     * @param actor
     *            Actor that might be a sensor.
     * @return True if the actor is a sensor.
     */
    public static boolean isSensor(Actor actor) {
        try {
            if (actor == null) {
                return false;
            } else {
                Parameter parameter = (Parameter) ((NamedObj) actor)
                        .getAttribute("isSensor");

                if (parameter != null) {
                    BooleanToken intToken = (BooleanToken) parameter.getToken();

                    return intToken.booleanValue();
                } else {
                    return false;
                }
            }
        } catch (ClassCastException ex) {
            return false;
        } catch (IllegalActionException ex) {
            return false;
        }
    }

    /** Return the execution time of the actor or the worst case
     *  execution time if no execution time is specified, or
     *  0.0 if neither is specified.
     *  @param actor The actor for which the execution time is
     *   requested.
     *  @return The execution time.
     *  @exception IllegalActionException If there is an _executionTime
     *   attribute, but either it is not a Parameter of type double
     *   or it has as its value an expression that cannot be evaluated.
     */
    public static double getExecutionTime(Actor actor)
            throws IllegalActionException {
        // FIXME: Make the other methods in this class look like this one.
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("executionTime");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
                return getWCET(actor);
            }
        } catch (ClassCastException ex) {
            throw new IllegalActionException(actor,
                    "Actor has an attribute _executionTime, but "
                            + "it is not a Parameter or its value is not"
                            + " a double. It is: "
                            + ((NamedObj) actor).getAttribute("_executionTime"));
        }
    }

    /** Return the execution time of the port or the worst case
     *  execution time if no execution time is specified, or
     *  null if neither is specified.
     *  @param port The port for which the execution time is
     *   requested.
     *  @return The execution time.
     *  @exception IllegalActionException If there is an _executionTime
     *   attribute, but either it is not a Parameter of type double
     *   or it has as its value an expression that cannot be evaluated.
     */
    public static Double getExecutionTime(IOPort port)
            throws IllegalActionException {
        // FIXME: Make the other methods in this class look like this one.
        try {
            Parameter parameter = (Parameter) ((NamedObj) port)
                    .getAttribute("executionTime");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
                return getWCET(port);
            }
        } catch (ClassCastException ex) {
            throw new IllegalActionException(port,
                    "Actor has an attribute _executionTime, but "
                            + "it is not a Parameter or its value is not"
                            + " a double. It is: "
                            + ((NamedObj) port).getAttribute("_executionTime"));
        }
    }

    /**
     * Return the worst case execution time of the actor or 0 if no worst case
     * execution time was specified.
     *
     * @param actor
     *            The actor for which the worst case execution time is
     *            requested.
     * @return The worst case execution time.
     * @exception IllegalActionException Thrown if WCET is not a double.
     */
    public static double getWCET(Actor actor) throws IllegalActionException {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("WCET");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
                return 0.0;
            }
        } catch (ClassCastException ex) {
            throw new IllegalActionException(actor,
                    "Actor has an attribute WCET, but "
                            + "it is not a Parameter or its value is not"
                            + " a double. It is: "
                            + ((NamedObj) actor).getAttribute("WCET"));
        } catch (IllegalActionException ex) {
            throw ex;
        }
    }

    /**
     * Return the worst case execution time of the port or null if no worst case
     * execution time was specified.
     *
     * @param port
     *            The port for which the worst case execution time is
     *            requested.
     * @return The worst case execution time.
     * @exception IllegalActionException Thrown if the parameter is not a double.
     */
    public static Double getWCET(IOPort port) throws IllegalActionException {
        try {
            Parameter parameter = (Parameter) ((NamedObj) port)
                    .getAttribute("WCET");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
                return null;
            }
        } catch (ClassCastException ex) {
            throw new IllegalActionException(port,
                    "Actor has an attribute WCET, but "
                            + "it is not a Parameter or its value is not"
                            + " a double. It is: "
                            + ((NamedObj) port).getAttribute("WCET"));
        } catch (IllegalActionException ex) {
            throw ex;
        }
    }

    /**
     * Returns the priority of the actor. The priority is an int value. The
     * default return value is 0.
     *
     * @param actor
     *            Given actor.
     * @return Priority of the given actor.
     */
    public static int getPriority(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("priority");

            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();

                return token.intValue();
            } else {
                return 0;
            }
        } catch (ClassCastException ex) {
            return 0;
        } catch (IllegalActionException ex) {
            return 0;
        }
    }

    /**
     * Returns true if the port is a trigger port, i.e. an event on that port
     * causes a firing of the actor.
     *
     * @param port
     *            Given port.
     * @return True if given port is a trigger port.
     */
    public static boolean portIsTriggerPort(IOPort port) {
        return !(port instanceof ParameterPort);
    }

    /**
     * Returns true if the actor or port object must be fired at real time equal
     * to model time.
     *
     * @param object
     *            Given actor or port object.
     * @return True if the given actor or actor containing the port must be
     *         fired at real time equal to model time.
     */
    public static boolean mustBeFiredAtRealTime(Object object) {
        if (object instanceof IOPort
                && ((Actor) ((IOPort) object).getContainer()) instanceof Source
                && ((Source) ((Actor) ((IOPort) object).getContainer())).trigger == object) {
            // trigger ports don't have to be fired at real time
            return false;
        }

        Actor actor = null;
        if (object instanceof IOPort) {
            actor = (Actor) ((IOPort) object).getContainer();
        } else {
            actor = (Actor) object;
        }
        return isSensor(actor) || isActuator(actor);
    }
}

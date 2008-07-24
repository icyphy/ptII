package ptolemy.domains.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.tt.tdl.kernel.TDLModule;
import ptolemy.domains.tt.tdl.kernel.TDLModuleDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class PtidesActorProperties {

    public static boolean mustBeFiredAtRealTime(Object object) {
        if (object instanceof IOPort && 
                ((Actor) ((IOPort) object).getContainer()) instanceof Source && 
                ((Source) ((Actor) ((IOPort) object).getContainer())).trigger == object) {
            // trigger ports don't have to be fired at real time
            return false;
        }
        
        Actor actor = null;
        if (object instanceof IOPort)
            actor = (Actor) ((IOPort) object).getContainer();
        else 
            actor = (Actor) object;
        return isSensor(actor) || isActuator(actor);
    }
    
    /**
     * Returns true if the actor is an actuator. A parameter of an actuator
     * actor called "isActuator" is true if the actor is an actuator.
     *
     * @param actor
     *                The actor which might be an actuator.
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
     * Returns true if given actor is a sensor. A parameter "isSensor"
     * is set to true if the actor is a sensor.
     *
     * @param actor
     *                Actor that might be a sensor.
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
    
    
    /**
     * Return the worst case execution time of the actor or 0 if no worst case
     * execution time was specified.
     *
     * @param actor
     *                The actor for which the worst case execution time is
     *                requested.
     * @return The worst case execution time.
     */
    public static double getWCET(Actor actor) {
        if (actor instanceof TDLModule) {
            return ((TDLModuleDirector) ((TDLModule) actor).getDirector())
                    .getWCET();
        }
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
            return 0.0;
        } catch (IllegalActionException ex) {
            return 0.0;
        }
    }
    
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
    
    public static boolean portIsTriggerPort(IOPort port) {
        return !(port instanceof ParameterPort) && 
                !(((Actor)port.getContainer()) instanceof TDLModule);
    }
}

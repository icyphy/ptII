package ptolemy.caltrop.ddi;

import caltrop.interpreter.Context;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.environment.Environment;
import ptolemy.caltrop.actors.CalInterpreter;

/**
 * A factory that creates {@link SDF SDF}s.
 *
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public class SDFFactory implements DDIFactory {
   /**
     * Create an <tt>SDF</tt>
     * @param ptActor The instance of {@link ptolemy.actor.Actor ptolemy.actor.Actor} that the plugin will be associated
     * with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     */
    public DDI create(CalInterpreter ptActor, Actor actor, Context context, Environment env) {
        return new SDF(ptActor, actor, context, env);
    }
    /**
     * Create an <tt>SDFFactory</tt>.
     */
    public SDFFactory() {
    }
}

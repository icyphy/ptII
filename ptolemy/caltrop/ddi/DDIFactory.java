package ptolemy.caltrop.ddi;

import caltrop.interpreter.Context;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.environment.Environment;
import ptolemy.caltrop.actors.CalInterpreter;

/**
 * The <tt>DDIFactory</tt> is used to create domain dependent plugins.
 *
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public interface DDIFactory {
    /**
     * Create a domain dependent plugin.
     * @param ptActor The instance of {@link ptolemy.actor.Actor ptolemy.actor.Actor} that the plugin will be associated
     * with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     * @return A <tt>DDI</tt> for this domain.
     */
    DDI create(CalInterpreter ptActor, Actor actor, Context context, Environment env);
}

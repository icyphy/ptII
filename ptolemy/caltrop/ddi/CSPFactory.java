package ptolemy.caltrop.ddi;

import caltrop.interpreter.Context;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.environment.Environment;
import ptolemy.caltrop.actors.CalInterpreter;

public class CSPFactory implements DDIFactory {
    public DDI create(CalInterpreter ptActor, Actor actor, Context context, Environment env) {
        return new CSP(ptActor, actor, context, env);
    }

    public CSPFactory() {
    }
}
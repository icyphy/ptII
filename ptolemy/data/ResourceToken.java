package ptolemy.data;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;
import ptolemy.data.type.Type;

/**
 * Token sent to a ResourceActor containing the Task to be scheduled and the execution 
 * time for that task. 
 * @author Patricia Derler
 *
 */
public class ResourceToken extends Token {

    public ResourceToken(Actor actorToSchedule, Object requestedValue) {
        super();
        this.actorToSchedule = actorToSchedule;
        this.requestedValue = requestedValue;
    }
    
    public Actor actorToSchedule;
    public Object requestedValue;
    
    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return super.getType();
    }
    
}

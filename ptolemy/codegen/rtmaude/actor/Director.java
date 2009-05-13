package ptolemy.codegen.rtmaude.actor;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class Director extends ptolemy.codegen.actor.Director {

    public Director(ptolemy.actor.Director director) {
        super(director);
    }
    
    public List<String> getBlockCodeList(String blockName, String ... args) 
            throws IllegalActionException {
        List<Actor> actors = ((CompositeActor) _director.getContainer()).deepEntityList();
        
        List<String> ret = new LinkedList();
        
        for (Actor actor : actors) {
            RTMaudeAdaptor helper = (RTMaudeAdaptor) _getHelper((NamedObj) actor);
            ret.addAll(helper.getBlockCodeList(blockName, args));
        }
  
        return ret;
    }    
}

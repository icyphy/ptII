package ptolemy.codegen.rtmaude.domains.de.lib;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class SingleEvent extends Entity {

    public SingleEvent(ptolemy.domains.de.lib.SingleEvent component) {
        super(component);
    }
    
    @Override
    public String generateFireCode() throws IllegalActionException {
        ptolemy.domains.de.lib.SingleEvent component = 
            (ptolemy.domains.de.lib.SingleEvent) getComponent();
        String evt = _generateBlockCode("eventBlock",
                component.getName(),
                component.output.getName(),
                _generateBlockCode("firstValueBlock"),
                _generateBlockCode("firstTimeBlock")
                );
        return evt + super.generateFireCode();
    }

}

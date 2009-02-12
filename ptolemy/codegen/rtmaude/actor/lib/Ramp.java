package ptolemy.codegen.rtmaude.actor.lib;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class Ramp extends CodeGeneratorHelper {

    public Ramp(ptolemy.actor.lib.Ramp component) {
        super(component);
    }
    
    public String generateFireCode() throws IllegalActionException {
        NamedObj actor = (NamedObj) getComponent();
        String name = actor.getFullName();
        return name + ": Hello World\n";
    }
}

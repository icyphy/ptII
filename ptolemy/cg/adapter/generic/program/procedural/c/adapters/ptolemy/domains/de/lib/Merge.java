package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

public class Merge 
extends
NamedProgramCodeGeneratorAdapter {

    public Merge(ptolemy.domains.de.lib.Merge actor) {
        super(actor);
    }
    
    @Override
    public String generateFireCode() throws IllegalActionException {
        
        ptolemy.domains.de.lib.Merge actor = (ptolemy.domains.de.lib.Merge) getComponent();
        ArrayList<String> args = new ArrayList<String>();
        CodeStream codeStream = _templateParser.getCodeStream();
        args.add("");
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i).toString());
            codeStream.appendCodeBlock("mergeBlock", args);
        }
        return processCode(codeStream.toString());
    }
    
    
}

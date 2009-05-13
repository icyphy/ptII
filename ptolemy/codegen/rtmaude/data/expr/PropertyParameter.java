package ptolemy.codegen.rtmaude.data.expr;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyParameter extends StringParameter {

    public PropertyParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    @Override
    public String stringValue() throws IllegalActionException {
        String timesign;
        String ret = super.stringValue();
        
        if (ret.matches(".*(with\\s+no\\s+time\\s+limit\\s*|in\\s+time\\s+(<|<=).*)"))
            timesign = "t";
        else
            timesign = "u";
        
        return "mc {init} |=" + timesign + " " + ret.replace("\\", "\\\\");
    }

}

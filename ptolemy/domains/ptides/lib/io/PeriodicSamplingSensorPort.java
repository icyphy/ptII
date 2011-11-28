package ptolemy.domains.ptides.lib.io;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.lib.PeriodicSampler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PeriodicSamplingSensorPort extends SensorPort {
    
    public Parameter samplingTime; 

    public PeriodicSamplingSensorPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        samplingTime = new Parameter(this, "samplingTime");
        samplingTime.setToken(new DoubleToken(1.0));
        samplingTime.setTypeEquals(BaseType.DOUBLE); 
    }
    
    
    
}

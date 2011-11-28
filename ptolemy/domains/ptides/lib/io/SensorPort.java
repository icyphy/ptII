package ptolemy.domains.ptides.lib.io;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexType;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class SensorPort extends TypedIOPort {

    public Parameter deviceDelay;
    public Parameter deviceDelayBound;
    public Parameter timestampCorrection;
    public Parameter valueCorrection;
    public Parameter driver;
    
    public SensorPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.setInput(true);
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE);
        
        timestampCorrection = new Parameter(this, "timestampCorrection");
        timestampCorrection.setTypeEquals(BaseType.DOUBLE);
        timestampCorrection.setExpression("0.0");
        
        timestampCorrection = new Parameter(this, "valueCorrection"); 
        driver = new Parameter(this, "driver");
    }
    
}

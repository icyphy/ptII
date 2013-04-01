package doc.tutorial;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Precocious extends TypedAtomicActor {

    public Precocious(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.INT);
        
        firingPeriod = new Parameter(this, "firingPeriod");
        firingPeriod.setTypeEquals(BaseType.DOUBLE);
        firingPeriod.setExpression("0.1");
    }

    public TypedIOPort output;
    public Parameter firingPeriod;
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = 0;
        getDirector().fireAtCurrentTime(this);
    }
    
    public void fire() throws IllegalActionException {
        super.fire();
        _count++;
        if (output.isOutsideConnected()) {
            output.send(0, new IntToken(_count));
        } else {
            ChangeRequest request = new ChangeRequest(this,"Find recipient") {
                protected void _execute() throws IllegalActionException {
                    CompositeEntity container = (CompositeEntity)
                            getContainer();
                    List<Entity> entities = container.entityList();
                    for (Entity entity : entities) {
                        List<IOPort> ports = entity.portList();
                        for (IOPort port : ports) {
                            if (port.isInput()
                                    && !port.isOutsideConnected()) {
                                container.connect(output, port);
                                return;
                            }
                        }
                    }
                }
            };
            requestChange(request);
        }
        
        double period = ((DoubleToken)firingPeriod.getToken()).doubleValue();
        getDirector().fireAt(this, getDirector().getModelTime().add(period));
    }
    
    private int _count;
}

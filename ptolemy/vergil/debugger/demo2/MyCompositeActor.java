package ptolemy.vergil.debugger.demo2;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;import ptolemy.actor.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.plot.*;

import ptolemy.vergil.debugger.*;
import ptolemy.vergil.debugger.domains.sdf.*;

public class MyCompositeActor extends TypedCompositeActor {

    public MyCompositeActor(TypedCompositeActor container, String name) 
	throws NameDuplicationException, IllegalActionException {
	super(container, name);

	try {
	    output = new TypedIOPort(this, "output", false, true);
	    ramp1 = new MyRamp(this, "ramp1");
	    ramp1.init.setToken(new DoubleToken(0.0));
	    ramp1.step.setToken(new DoubleToken(0.01*Math.PI));
	    ramp2 = new MyRamp(this, "ramp2");
	    ramp2.init.setToken(new DoubleToken(0.0));
	    ramp2.step.setToken(new DoubleToken(0.01*Math.PI));
	    addE1 = new AddSubtract(this, "addE1");
	    connect(ramp1.output, addE1.plus);
	    connect(ramp2.output, addE1.plus);
	    connect(addE1.output, this.output);
	} catch (NameDuplicationException ex) {
	    throw(ex);
	} catch (IllegalActionException ex) {
	    throw(ex);
	}
    }

    public Object clone(Workspace ws) throws CloneNotSupportedException {
	MyCompositeActor newobj = (MyCompositeActor)super.clone(ws);
	newobj.output = (TypedIOPort)newobj.getPort("output");
	return newobj;
    }

    public void fire() throws IllegalActionException {
	    super.fire();
    }

    public void initialize() throws IllegalActionException {
	    super.initialize();
    }

    public boolean prefire() throws IllegalActionException {
	    return super.prefire();
    }

    public boolean postfire() throws IllegalActionException {
	    return super.postfire();
    }

    public void wrapup() throws IllegalActionException {
	    super.wrapup();
    }

    public TypedIOPort output;

    public MyRamp ramp1, ramp2;
    public AddSubtract addE1; 
}

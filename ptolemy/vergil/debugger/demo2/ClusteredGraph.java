package ptolemy.vergil.debugger.demo2;

import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.util.*;
import ptolemy.domains.sdf.kernel.*;

import ptolemy.plot.*;
import ptolemy.vergil.*; 
import ptolemy.vergil.debugger.*;
import ptolemy.vergil.debugger.domains.sdf.*;

////////////////////////////////////////////////////////////////////
////  ClusteredGraph
////  This class implements a model with a composite actor in order 
////  to test the step in and step out functions

public class ClusteredGraph {

    /////////////////////////////////////////////////////////
    //  Composite actor E1                       
    public MyCompositeActor E1;
    public SDFDbgDirector dirE1;

    /////////////////////////////////////////////////////////
    // Top level compositeActor E0
    public TypedCompositeActor E0;
    public SDFDbgDirector dirE0;
    public MyRamp ramp0;
    public AddSubtract addE0;
    public SequencePlotter plotter0;

    /////////////////////////////////////////////////////////
    // Manager 
    public Manager manager;

    /////////////////////////////////////////////////////////
    public ClusteredGraph(Pdb pdb) throws Exception {

	Workspace w = new Workspace("w");
	E0 = new TypedCompositeActor(w);
	E0.setName("E0");
        dirE0 = new SDFDbgDirector(E0, "dirE0");
	dirE0.iterations.setToken(new IntToken(20));
	dirE0.setPdb(pdb);
	manager = new Manager(w, "manager");
	E0.setManager(manager);
	try {
	    /////////////////////////////////////////////////
	    // Construct E0
	    ramp0 = new MyRamp(E0, "ramp0");
            ramp0.init.setToken(new DoubleToken(0.0));
            ramp0.step.setToken(new DoubleToken(0.01*Math.PI));	    
	    addE0 = new AddSubtract(E0, "addE0");
	    plotter0 = new SequencePlotter(E0, "plotter0");
	    //plotter0.plot.setXRange(0.0, 20.0);
	    //plotter0.plot.setYRange(0.0, 2.0);
	    E1 = new MyCompositeActor(E0, "E1");
	    dirE1 = new SDFDbgDirector(E1, "dirE1");
	    dirE1.iterations.setToken(new IntToken(20));
	    dirE1.setPdb(pdb);	    
	
	    ///////////////////////////////////////////////
	    // Connections in E0
	    E0.connect(ramp0.output, addE0.plus);
	    E0.connect(E1.output, addE0.plus);
	    E0.connect(addE0.output, plotter0.input);

	    ///////////////////////////////////////////////
	    // Set breakpoints
	    //(dirE0.breakpoints).addBreakpoint(addE0, "fire");
	    //(dirE1.breakpoints).addBreakpoint(E1.ramp1, "postpostfire");
	    (dirE0.breakpoints).addBreakpoint(ramp0, "prefire");
	    //	    (dirE0.breakpoints).getBreakpoint(ramp0.getFullName() + ".prefire").setCondition("state > 0.4");
	    (dirE0.breakpoints).getBreakpoint(ramp0.getFullName() + ".prefire").setCondition("true");
	    
	    ///////////////////////////////////////////////
	    // Set Watcher
	    ActorWatcher watcher = new ActorWatcher(ramp0);
	    NamedList att = new NamedList();
	    Enumeration e = ramp0.getAttributes();
	    while (e.hasMoreElements()) {
		att.append((Nameable)e.nextElement());
	    }
	    watcher.edit(att);
	    pdb.getDbgController().actorWatcher.append(watcher);
	    pdb.getDebuggerUI().enableAllButtons();
	    ///////////////////////////////////////////////
	    // Start the application
	    manager.run();

	} catch (Exception ex) {
	    System.err.println("Error constructing model." + ex);
	}
    }

    public static void main(String[] args)
	throws IllegalActionException, NameDuplicationException {

	//	VergilApplication vergil = new VergilApplication()
	Pdb pdb;
	try {
	    pdb = new Pdb();
	    ClusteredGraph model = new ClusteredGraph(pdb);
	} catch (NoClassDefFoundError ex) {
	    System.out.println(ex.getMessage());
	} catch (Exception ex) {
	    System.err.println("Setup failed:" + ex);
	}
    }
}


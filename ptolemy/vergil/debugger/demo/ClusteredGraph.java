import java.awt.event.*;
import java.awt.Container;
import java.awt.Dimension;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.conversions.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.util.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;

import ptolemy.plot.*;

////////////////////////////////////////////////////////////////////
////  ClusteredGraph
////  This class implements a model with a composite actor in order 
////  to test the step in and step out functions

public class ClusteredGraph {
    public Expression expr;

    public ClusteredGraph(TypedCompositeActor toplevel, Container Panel,
			  Dimension size) 
	throws Exception {

	Workspace w = new Workspace("w");
	TypedCompositeActor toplevel = new TypedCompositeActor(w);
	toplevel.setName("toplevel");
	SDFDbgDirector dir = new SDFDbgDirector(toplevel, "dir");
	Manager manager = new Manager(w, "manager");
	toplevel.setManager(manager);
	try {
	    Ramp ramp = new Ramp(toplevel, "ramp");
	    ramp.step.setToken(new DoubleToken(Math.PI/100.0));
	    Expression expr = new Expression(toplevel, "expression")
	    AddSubtract add1 = new AddSubtract(toplevel,"add1");
	    
	    SequencePlotter plotter = new SequencePlotter(toplevel, "plot");
	    plotter.plot.setBackground(getBackground());
            plotter.plot.setGrid(false);
            plotter.plot.setTitle("Eye Diagram");
            plotter.plot.setXRange(0.0, 32.0);
            plotter.plot.setWrap(true);
            plotter.plot.setYRange(-1.3, 1.3);
            plotter.plot.setMarksStyle("none");
            plotter.plot.setPointsPersistence(512);

	    toplevel.connect(ramp.output, add.plus);
	    toplevel.connect(expr.sortie, add.plus);
	    toplevel.connect(add.output, plotter.input);

    }
}

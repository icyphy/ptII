// This class is for figure 10.5 of the graph.fm
import ptolemy.graph.*;
//import ptolemy.kernel.util.*;

// An example of forming and solving inequality constraints.
public class TestSolver {
	public static void main(String[] argv) {
		// construct the 4-point CPO in figure 2.3.
		CPO cpo = constructCPO();

		// create inequality terms for constants w, z and
		// variables a, b.
		InequalityTerm tw = new Constant("w");
		InequalityTerm tz = new Constant("z");
		InequalityTerm ta = new Variable();
		InequalityTerm tb = new Variable();

		// form inequalities: a<=w; b<=a; b<=z.
		Inequality iaw = new Inequality(ta, tw);
		Inequality iba = new Inequality(tb, ta);
		Inequality ibz = new Inequality(tb, tz);

		// create the solver and add the inequalities.
		InequalitySolver solver = new InequalitySolver(cpo);
		solver.addInequality(iaw);
		solver.addInequality(iba);
		solver.addInequality(ibz);

		// solve for the least solution
		boolean satisfied = solver.solveLeast();

		// The output should be: 
		// satisfied=true, least solution: a=z b=z
		System.out.println("satisfied=" + satisfied + ", least solution:"
			 + " a=" + ta.getValue() + " b=" + tb.getValue());
		// solve for the greatest solution
		satisfied = solver.solveGreatest();

		// The output should be: 
		// satisfied=true, greatest solution: a=w b=z
		System.out.println("satisfied=" + satisfied + ", greatest solution:"
			 + " a=" + ta.getValue() + " b=" + tb.getValue());
	}

	public static CPO constructCPO() {
		DirectedAcyclicGraph cpo = new DirectedAcyclicGraph();

	 	cpo.addNodeWeight("w");
		cpo.addNodeWeight("x");
	 	cpo.addNodeWeight("y");
	 	cpo.addNodeWeight("z");

		cpo.addEdge("x", "w");
		cpo.addEdge("y", "w");
		cpo.addEdge("z", "x");
		cpo.addEdge("z", "y");

		return cpo;
	}
}

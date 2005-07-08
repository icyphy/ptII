/* Simulation of a Lorenz attractor, a nonlinear CT system.

 Copyright (c) 1998-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.ct.demo.Lorenz;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.MultiplyDivide;
import ptolemy.actor.lib.Scale;
import ptolemy.actor.lib.gui.XYPlotter;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTMultiSolverDirector;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////////////////////
//// Lorenz

/**
 This is a model of a nonlinear feedback system that exhibits chaotic
 behavior.  It is the well-known Lorenz attractor, and is given
 by a set of ordinary differential equations,
 <pre>
 dx1/dt = sigma*(x2-x1)
 dx2/dt = (lambda-x3)*x1 -x2
 dx3/dt = x1*x2-b*x3
 </pre>
 The plot created by the model shows the value of x2 vs. x1.
 <p>
 This class constructs a top-level Ptolemy model containing
 a CT director, which includes a sophisticated ODE numerical solver.

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (cxh)
 */
public class Lorenz extends TypedCompositeActor {
    public Lorenz(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        // Create the model.
        super(workspace);
        setName("LorenzSystem");

        Manager manager = new Manager(workspace, "Manager");
        setManager(manager);

        // Set up the top level composite actor, director and manager
        CTMultiSolverDirector director = new CTMultiSolverDirector(this,
                "CTMultiSolverDirector");
        setDirector(director);
        director.stopTime.setToken(new DoubleToken(50.0));

        // To get debug outputs, uncomment these:
        // director.addDebugListener(new StreamListener());
        // manager.addDebugListener(new StreamListener());
        // Parameters
        stopTime = new Parameter(this, "stopTime", new DoubleToken(50.0));
        lambda = new Parameter(this, "lambda", new DoubleToken(25.0));
        sigma = new Parameter(this, "sigma", new DoubleToken(10.0));
        b = new Parameter(this, "b", new DoubleToken(2.0));

        // Create the actors.
        Const LAMBDA = new Const(this, "LAMBDA");
        LAMBDA.value.setExpression("lambda");

        Scale SIGMA = new Scale(this, "SIGMA");
        SIGMA.factor.setExpression("sigma");

        Scale B = new Scale(this, "B");
        B.factor.setExpression("b");

        AddSubtract ADD1 = new AddSubtract(this, "Add1");
        AddSubtract ADD2 = new AddSubtract(this, "Add2");
        AddSubtract ADD3 = new AddSubtract(this, "Add3");
        AddSubtract ADD4 = new AddSubtract(this, "Add4");

        MultiplyDivide MULT1 = new MultiplyDivide(this, "MULT1");
        MultiplyDivide MULT2 = new MultiplyDivide(this, "MULT2");

        Integrator X1 = new Integrator(this, "IntegratorX1");
        Integrator X2 = new Integrator(this, "IntegratorX2");
        Integrator X3 = new Integrator(this, "IntegratorX3");

        Scale MINUS1 = new Scale(this, "MINUS1");
        Scale MINUS2 = new Scale(this, "MINUS2");
        Scale MINUS3 = new Scale(this, "MINUS3");

        XYPlotter myplot = new XYPlotter(this, "CTXYPlot");
        myplot.plot = new Plot();
        myplot.plot.setGrid(true);
        myplot.plot.setXRange(-25.0, 25.0);
        myplot.plot.setYRange(-25.0, 25.0);
        myplot.plot.setSize(400, 400);
        myplot.plot.addLegend(0, "(x1, x2)");

        // CTConnections
        TypedIORelation x1 = new TypedIORelation(this, "X1");
        TypedIORelation x2 = new TypedIORelation(this, "X2");
        TypedIORelation x3 = new TypedIORelation(this, "X3");
        X1.output.link(x1);
        X2.output.link(x2);
        X3.output.link(x3);
        MINUS1.input.link(x1);
        MINUS2.input.link(x2);
        MINUS3.input.link(x3);

        // dx1/dt = sigma*(x2-x1)
        connect(MINUS1.output, ADD1.plus);
        ADD1.plus.link(x2);
        connect(ADD1.output, SIGMA.input);
        connect(SIGMA.output, X1.input);

        // dx2/dt = (lambda-x3)*x1-x2
        connect(LAMBDA.output, ADD2.plus);
        connect(MINUS3.output, ADD2.plus);
        connect(ADD2.output, MULT1.multiply);
        MULT1.multiply.link(x1);
        connect(MULT1.output, ADD3.plus);
        connect(MINUS2.output, ADD3.plus);
        connect(ADD3.output, X2.input);

        // dx3/dt = x1*x2-b*x3
        MULT2.multiply.link(x1);
        MULT2.multiply.link(x2);
        B.input.link(x3);
        connect(MULT2.output, ADD4.plus);
        connect(B.output, ADD4.minus);
        connect(ADD4.output, X3.input);

        myplot.inputX.link(x1);
        myplot.inputY.link(x2);

        // CT Director parameters
        director.initStepSize.setToken(new DoubleToken(0.01));
        director.minStepSize.setToken(new DoubleToken(1e-6));

        // CTActorParameters
        X1.initialState.setToken(new DoubleToken(1.0));
        X2.initialState.setToken(new DoubleToken(1.0));
        X3.initialState.setToken(new DoubleToken(1.0));

        MINUS1.factor.setToken(new DoubleToken(-1.0));
        MINUS2.factor.setToken(new DoubleToken(-1.0));
        MINUS3.factor.setToken(new DoubleToken(-1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The stop time of the model. This is the top level facet for
     *  the stopTime parameter of the director.
     */
    Parameter stopTime;

    /** The lamda value in the equation.
     */
    Parameter lambda;

    /** The sigma value in the equation.
     */
    Parameter sigma;

    /** The b value in the equation.
     */
    Parameter b;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter changed is the stopTime, then update the
     *  stopTime parameter of the director.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        CTDirector director = (CTDirector) getDirector();

        if ((director != null) && (director.stopTime != null)
                && (stopTime != null)) {
            // This is a hack, we should really use controls=directorparamter
            // in the applet
            director.stopTime.setToken(stopTime.getToken());
        } else {
            super.attributeChanged(attribute);
        }
    }
}

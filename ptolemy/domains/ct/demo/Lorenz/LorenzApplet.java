/* Simulating the Lorenz system, a nonlinear CT system.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.Lorenz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.gui.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.actor.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// LorenzApplet
/**
A demonstration of the Lorenz system. The system is given by a set of
ODEs like:
dx1/dt = sigma*(x2-x1)
dx2/dt = (lambda-x3)*x1 -x2
dx3/dt = x1*x2-b*x3

This demo plots the projection of the state trajectory to the (x1, x2)
plane.
@author Jie Liu
@version $Id$
*/
public class LorenzApplet extends CTApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();
        JPanel controlpanel = new JPanel();
        controlpanel.setLayout(new BorderLayout());
        controlpanel.setBackground(getBackground());
        getContentPane().add(controlpanel, BorderLayout.NORTH);

        _query = new Query();
        _query.addQueryListener(new ParameterListener());
        controlpanel.add(_query, BorderLayout.WEST);
        _query.addLine("stopT", "Stop Time", "50.0");
        _query.addLine("sigma", "Sigma", "10.0");
        _query.addLine("lambda", "Lambda", "25.0");
        _query.addLine("b", "b", "2.0");
        _query.setBackground(getBackground());

        controlpanel.add(_createRunControls(2), BorderLayout.EAST);

        // Creating the model.
        try {
            // Set up the top level composite actor, director and manager
            _toplevel.setName("LorenzSystem");
            _dir = new CTMultiSolverDirector(
                    _toplevel, "CTMultiSolverDirector");
            //_dir.addDebugListener(new StreamListener());
            //_manager.addDebugListener(new StreamListener());
            // ---------------------------------
            // Create the system.
            // ---------------------------------

            // CTActors

            _LAMBDA = new Const(_toplevel, "LAMBDA");
            _SIGMA = new Scale(_toplevel, "SIGMA");
            _B = new Scale(_toplevel, "B");

            AddSubtract ADD1 = new AddSubtract(_toplevel, "Add1");
            AddSubtract ADD2 = new AddSubtract(_toplevel, "Add2");
            AddSubtract ADD3 = new AddSubtract(_toplevel, "Add3");
            AddSubtract ADD4 = new AddSubtract(_toplevel, "Add4");

            MultiplyDivide MULT1 = new MultiplyDivide(_toplevel, "MULT1");
            MultiplyDivide MULT2 = new MultiplyDivide(_toplevel, "MULT2");

            Integrator X1 = new Integrator(_toplevel, "IntegratorX1");
            Integrator X2 = new Integrator(_toplevel, "IntegratorX2");
            Integrator X3 = new Integrator(_toplevel, "IntegratorX3");

            Scale MINUS1 = new Scale(_toplevel, "MINUS1");
            Scale MINUS2 = new Scale(_toplevel, "MINUS2");
            Scale MINUS3 = new Scale(_toplevel, "MINUS3");

            XYPlotter myplot = new XYPlotter(_toplevel, "CTXYPlot");
            myplot.place(getContentPane());
            myplot.plot.setBackground(getBackground());
            myplot.plot.setGrid(true);
            myplot.plot.setXRange(-25.0, 25.0);
            myplot.plot.setYRange(-25.0, 25.0);
            myplot.plot.setSize(400, 400);
            myplot.plot.addLegend(0, "(x1, x2)");

            // CTConnections
            TypedIORelation x1 = new TypedIORelation(_toplevel, "X1");
            TypedIORelation x2 = new TypedIORelation(_toplevel, "X2");
            TypedIORelation x3 = new TypedIORelation(_toplevel, "X3");
            X1.output.link(x1);
            X2.output.link(x2);
            X3.output.link(x3);
            MINUS1.input.link(x1);
            MINUS2.input.link(x2);
            MINUS3.input.link(x3);

            // dx1/dt = sigma*(x2-x1)
            _toplevel.connect(MINUS1.output, ADD1.plus);
            ADD1.plus.link(x2);
            _toplevel.connect(ADD1.output, _SIGMA.input);
            _toplevel.connect(_SIGMA.output, X1.input);

            // dx2/dt = (lambda-x3)*x1-x2
            _toplevel.connect(_LAMBDA.output, ADD2.plus);
            _toplevel.connect(MINUS3.output, ADD2.plus);
            _toplevel.connect(ADD2.output, MULT1.multiply);
            MULT1.multiply.link(x1);
            _toplevel.connect(MULT1.output, ADD3.plus);
            _toplevel.connect(MINUS2.output, ADD3.plus);
            _toplevel.connect(ADD3.output, X2.input);

            // dx3/dt = x1*x2-b*x3
            MULT2.multiply.link(x1);
            MULT2.multiply.link(x2);
            _B.input.link(x3);
            _toplevel.connect(MULT2.output, ADD4.plus);
            _toplevel.connect(_B.output, ADD4.minus);
            _toplevel.connect(ADD4.output, X3.input);

            myplot.inputX.link(x1);
            myplot.inputY.link(x2);

            //System.out.println("Parameters");
            // CT Director parameters
            _dir.initStepSize.setToken(new DoubleToken(0.01));
            _dir.minStepSize.setToken(new DoubleToken(1e-6));

            // CTActorParameters
            X1.initialState.setToken(new DoubleToken(1.0));
            X2.initialState.setToken(new DoubleToken(1.0));
            X3.initialState.setToken(new DoubleToken(1.0));

            MINUS1.factor.setToken(new DoubleToken(-1.0));
            MINUS2.factor.setToken(new DoubleToken(-1.0));
            MINUS3.factor.setToken(new DoubleToken(-1.0));

        } catch (Exception ex) {
            report("Setup failed: ",  ex);
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _go() throws IllegalActionException {
        try {
            _dir.stopTime.setToken(new DoubleToken(
                    _query.doubleValue("stopT")));
            _LAMBDA.value.setToken(new DoubleToken(
                    _query.doubleValue("lambda")));
            _SIGMA.factor.setToken(new DoubleToken(
                    _query.doubleValue("sigma")));
            _B.factor.setToken(new DoubleToken(
                    _query.doubleValue("b")));
            super._go();
        } catch (Exception ex) {
            report(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CTMultiSolverDirector _dir;
    private Query _query;
    private Const _LAMBDA;
    private Scale _SIGMA;
    private Scale _B;

    ///////////////////////////////////////////////////////////////////
    ////                       inner classes                       ////

    /** Listener update the parameter change of stop time.
     */
    private class ParameterListener implements QueryListener {
        public void changed(String name) {
            try {
                _dir.stopTime.setToken(new DoubleToken(
                        _query.doubleValue("stopT")));
                _LAMBDA.value.setToken(new DoubleToken(
                        _query.doubleValue("lambda")));
                _SIGMA.factor.setToken(new DoubleToken(
                        _query.doubleValue("sigma")));
                _B.factor.setToken(new DoubleToken(
                        _query.doubleValue("b")));
            } catch (Exception ex) {
                report(ex);
            }
        }
    }
}

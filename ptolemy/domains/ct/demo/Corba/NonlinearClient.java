/* The square wave response of a second order system with nonlinear effect.

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

@ProposedRating Red
@AcceptedRating Red (cxh)
*/
package ptolemy.domains.ct.demo.Corba;

import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.corba.CorbaActorClient;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.Scale;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTMultiSolverDirector;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;


//////////////////////////////////////////////////////////////////////////
//// NonlinearClient

/**
   The square wave response of a second order CT system with a CORBA
   actor in the feedback. . This simple
   CT system demonstrate the use of CORBA actor solvers over the network.
   The query box allow the users to input the ORB initialization parameter
   and the name of the CORBA actor.
   @author  Jie Liu
   @version $Id$
*/
public class NonlinearClient extends TypedCompositeActor {
    public NonlinearClient(Workspace workspace)
        throws IllegalActionException, NameDuplicationException {
        super(workspace);
        setName("NonlinearSystem");

        ORBInitProperties = new Parameter(this, "ORBInitProperties",
                new StringToken("-ORBInitialPort 1050"));
        remoteActorName = new Parameter(this, "remoteActorName",
                new StringToken("Nonlinear"));
        stopTime = new Parameter(this, "stopTime", new DoubleToken(6.0));

        CTMultiSolverDirector director = new CTMultiSolverDirector(this,
                "CTMultiSolverDirector");
        setDirector(director);
        director.stopTime.setExpression("stopTime");

        //director.addDebugListener(new StreamListener());
        Clock sqwv = new Clock(this, "SQWV");
        AddSubtract add1 = new AddSubtract(this, "Add1");
        Integrator intgl1 = new Integrator(this, "Integrator1");
        Integrator intgl2 = new Integrator(this, "Integrator2");
        Scale gain1 = new Scale(this, "Gain1");
        Scale gain2 = new Scale(this, "Gain2");
        Scale gain3 = new Scale(this, "Gain3");
        CorbaActorClient client = new CorbaActorClient(this, "NonliearClient");

        //client.addDebugListener(new StreamListener());
        client.ORBInitProperties.setExpression("ORBInitProperties");
        client.remoteActorName.setExpression("remoteActorName");

        TypedIOPort cin = new TypedIOPort(client, "input", true, false);
        TypedIOPort cout = new TypedIOPort(client, "output", false, true);
        TimedPlotter myplot = new TimedPlotter(this, "Plot");
        myplot.plot = new Plot();
        myplot.plot.setGrid(true);
        myplot.plot.setXRange(0.0, 6.0);
        myplot.plot.setYRange(-2.0, 2.0);
        myplot.plot.setSize(400, 400);
        myplot.plot.addLegend(0, "response");

        IORelation r1 = (IORelation) connect(sqwv.output, gain1.input, "R1");
        connect(gain1.output, add1.plus, "R2");
        connect(add1.output, intgl1.input, "R3");

        IORelation r4 = (IORelation) connect(intgl1.output, intgl2.input, "R4");
        connect(intgl2.output, cin, "R5");

        IORelation r5a = (IORelation) connect(cout, myplot.input, "R5a");
        gain2.input.link(r4);
        gain3.input.link(r5a);
        connect(gain2.output, add1.plus, "R6");
        connect(gain3.output, add1.plus, "R7");
        myplot.input.link(r1);

        director.startTime.setToken(new DoubleToken(0.0));

        director.initStepSize.setToken(new DoubleToken(0.0001));

        director.minStepSize.setToken(new DoubleToken(1e-7));

        sqwv.period.setToken(new DoubleToken(4));
        sqwv.offsets.setExpression("{0.0, 2.0}");
        sqwv.values.setExpression("{2.0, -2.0}");

        gain1.factor.setToken(new DoubleToken(500.0));

        gain2.factor.setToken(new DoubleToken(-10.0));

        gain3.factor.setToken(new DoubleToken(-1000.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                    ////
    public Parameter ORBInitProperties;
    public Parameter remoteActorName;
    public Parameter stopTime;
}

/* The square wave response of a second order system with nonlinear effect.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.ct.demo.Corba;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.gui.Query;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.corba.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.gui.CTApplet;
import ptolemy.domains.ct.kernel.solver.*;
import ptolemy.domains.ct.lib.*;


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
public class NonlinearClientApplication {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Build the model.
     */
    public static void main(String[] args) {
        CTMultiSolverDirector _dir;
        TypedCompositeActor _toplevel;
        CorbaActorClient _client;
        try {
            Workspace ws = new Workspace("NonlinearClient");
            _toplevel = new TypedCompositeActor(ws);
            _toplevel.setName( "system");
            Manager man = new Manager(ws, "Manager");
            _toplevel.setManager(man);
            _dir = new CTMultiSolverDirector(
                    _toplevel, "DIR");
            _dir.STAT= true;
            //_dir.addDebugListener(new StreamListener());
            Clock sqwv = new Clock(_toplevel, "SQWV");
            AddSubtract add1 = new AddSubtract( _toplevel, "Add1");
            Integrator intgl1 = new Integrator(_toplevel, "Integrator1");
            Integrator intgl2 = new Integrator(_toplevel, "Integrator2");
            Scale gain1 = new Scale( _toplevel, "Gain1");
            Scale gain2 = new Scale( _toplevel, "Gain2");
            Scale gain3 = new Scale( _toplevel, "Gain3");
            _client = new CorbaActorClient( _toplevel, "NonliearClient");
            //_client.addDebugListener(new StreamListener());
            TypedIOPort cin = new TypedIOPort(_client, "input", true, false);
            TypedIOPort cout = new TypedIOPort(_client, "output", false, true);
            TimedPlotter myplot = new TimedPlotter( _toplevel, "Sink");

            myplot.place(null);
            myplot.plot.setGrid(true);
            myplot.plot.setXRange(0.0, 6.0);
            myplot.plot.setYRange(-2.0, 2.0);
            myplot.plot.setSize(600, 400);
            myplot.plot.addLegend(0,"response");

            IORelation r1 = (IORelation)
                _toplevel.connect(sqwv.output, gain1.input, "R1");
            IORelation r2 = (IORelation)
                _toplevel.connect(gain1.output, add1.plus, "R2");
            IORelation r3 = (IORelation)
                _toplevel.connect(add1.output, intgl1.input, "R3");
            IORelation r4 = (IORelation)
                _toplevel.connect(intgl1.output, intgl2.input, "R4");
            IORelation r5 = (IORelation)
                _toplevel.connect(intgl2.output, cin, "R5");
            IORelation r5a = (IORelation)
                _toplevel.connect(cout, myplot.input, "R5a");
            gain2.input.link(r4);
            gain3.input.link(r5a);
            IORelation r6 = (IORelation)
                _toplevel.connect(gain2.output, add1.plus, "R6");
            IORelation r7 = (IORelation)
                _toplevel.connect(gain3.output, add1.plus, "R7");
            myplot.input.link(r1);

            _dir.StartTime.setToken(new DoubleToken(0.0));

            _dir.InitStepSize.setToken(new DoubleToken(0.0001));

            _dir.MinStepSize.setToken(new DoubleToken(1e-6));

            _dir.StopTime.setToken(new DoubleToken(6.0));

            sqwv.period.setToken(new DoubleToken(4));
            double offsets[][] = {{0.0, 2.0}};
            sqwv.offsets.setToken(new DoubleMatrixToken(offsets));
            double values[][] = {{2.0, -2.0}};
            sqwv.values.setToken(new DoubleMatrixToken(values));


            gain1.factor.setToken(new DoubleToken(500.0));

            gain2.factor.setToken(new DoubleToken(-10.0));

            gain3.factor.setToken(new DoubleToken(-1000.0));

            String orbinit = new String("");
            for(int i =0; i < args.length; i++) {
                orbinit = orbinit + args[i] + " ";
            }
            _client.ORBInitProperties.setToken(new StringToken(orbinit));
            _client.remoteActorName.setToken(new StringToken("Nonlinear"));

            man.startRun();
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("NameDuplication");
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("IllegalAction:"+
                    ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


}

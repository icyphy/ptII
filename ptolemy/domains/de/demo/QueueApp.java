/* An applet that uses Ptolemy II DE domain.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// QueueApplet
/** 
An applet that uses Ptolemy II DE domain.

@author Lukito
@version $Id$
*/
public class QueueApp {

    public static final boolean DEBUG = true;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public static void main(String argv[]) {
        QueueApp app = new QueueApp();
        MyPthread thread = app.new MyPthread(app);
        thread.start();
        // wait until the execution thread finishes. 
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        return;
    }


    /** Initialize the applet.
     */	
    public void initSimulation() {

        // Creating the topology.
        try {
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("DE Demo");
        
            // Set up the top level composite actor, director and manager
            _localDirector = new DECQDirector("DE Director");
            sys.setDirector(_localDirector);
            _executiveDirector = new Manager("Manager");
            sys.setManager(_executiveDirector);            

            // ---------------------------------
            // Create the actors.
            // ---------------------------------
            DEClock clock = new DEClock(sys, "Clock", 1.0, 1.0);
            Ramp ramp = new Ramp(sys, "Ramp", 0, 1.0);
            
            DEFIFOQueue fifo1 = new DEFIFOQueue(sys, "FIFO1", 1, true, 10);
            DEPseudoPlot plot1 = new DEPseudoPlot(sys, "Queue 1 Size");
            
            DEServer server1 = new DEServer(sys, "Server1", 1.0);
            DEPassGate passgate = new DEPassGate(sys, "PassGate");
            DEDelay delta = new DEDelay(sys, "DEDelay", 0.0);
            
            DEFIFOQueue fifo2 = new DEFIFOQueue(sys, "FIFO2", 1, true, 1000);
            DEPseudoPlot plot2 = new DEPseudoPlot(sys, "Queue 2 Size");
            
            TestLevel testlevel = new TestLevel(sys, "TestLevel", true, 4);
            Not not = new Not(sys, "Not");

            DEServer server2 = new DEServer(sys, "Server2", 3.0);
            
            DEPseudoPlot plot3 = new DEPseudoPlot(sys, "Blocking signal");
            DEPseudoPlot plot4 = new DEPseudoPlot(sys, "Dispositions of inputs");

            // -----------------------
            // Creating connections
            // -----------------------

            Relation r1 = sys.connect(clock.output, ramp.input);
            Relation r2 = sys.connect(ramp.output, fifo1.inData);
            Relation r3 = sys.connect(fifo1.queueSize, plot1.input);
            
            Relation r4 = sys.connect(passgate.output, fifo1.demand);
            fifo2.inData.link(r4);
            
            Relation r5 = sys.connect(fifo1.outData, server1.input);
            Relation r6 = sys.connect(fifo1.overflow, plot4.input);
            
            Relation r7 = sys.connect(server1.output, passgate.input);
            Relation r8 = sys.connect(delta.output, passgate.gate);
            
            Relation r9 = sys.connect(not.output, delta.input);

            Relation r14 = sys.connect(testlevel.output, not.input);
            plot3.input.link(r14);

            Relation r10 = sys.connect(server2.output, plot4.input);
            fifo2.demand.link(r10);

            Relation r12 = sys.connect(fifo2.queueSize, testlevel.input);
            plot2.input.link(r12);

            Relation r13 = sys.connect(fifo2.outData, server2.input);
                        
        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Run the simulation.
     */
    public void runSimulation() {

        try {
                
                _localDirector.setStopTime(_stopTime);

                _executiveDirector.blockingGo();

        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    

    ////////////////////////////////////////////////////////////////////////
    ////                         private inner class                    ////

    private class MyPthread extends Thread {
        
        public MyPthread(QueueApp qa) {
            super();
            _queueApp = qa;
        }

        public void run() {
            _queueApp.initSimulation();
            _queueApp.runSimulation();
        }
        private QueueApp _queueApp = null;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////
    

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private DECQDirector _localDirector;
    private Manager _executiveDirector;

    private double _stopTime = 5000.0;

}




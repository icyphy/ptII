/* A M/M/1 queue simulation that tests time and the ability to pause.

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


import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// A M/M/1 queue simulation to test time and the ability to pause.
/**
Customer - Buffer - Server
<p>
This demo is similar to the MM1 demo, excpet that immediately after 
starting the model execution, pause() is called on the CSPDirector. 
When the model is successfully paused (the pause() method is blocking), 
resume() is called to allow the model execution to continue.
Thus this demo illustrates pausing, as well as time and conditional 
communication, in the CSP domain.
<p>
Customers arrive with a Poisson distribution, i.e. the inter-arrival
times are exponentially distributed. The buffer stores customers who
have arrived but have not yet been served. The server process also
serves customers with an exponential distribution.
<p>
By varying the rate of customer arrivals, the buffer size and rate
at which the server deals with customers, various trade offs can
be observed. For example, if the rate of customer arrivals is
greater than the service rate, then the buffer will nearly always
be full and customers may be refused.
<p>
This demo illustrates both the use of time and conditional
communication, the buffer uses a CDO, in the CSP domain.
<p>
@author Neil Smyth
@version $Id$
*/
public class PausingMM1 {
    /** Create an instance for executing the Pausing M/M/1 demo.
     * */
    public PausingMM1() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct the topology used in this demo. Here the topology 
     *  consists of a process representing customers arriving, a buffer 
     *  process that represents a queue, and a process to represent the server.
     *  Here a single level of hierarchy is used, with the 
     *  director controlling the CompositeActor being an 
     *  instance of CSPDirector. Thus the execution of the model 
     *  follows CSP semantics.
     */
    public static void main(String[] args) {
        try {
            CompositeActor univ = new CompositeActor();
            univ.setName( "Pausing M/M/1 demo");
            Manager manager = new Manager("Manager");
            CSPDirector localdir = new CSPDirector("Local Director");
            univ.setManager(manager);
            univ.setDirector(localdir);

            Parameter custRate = new Parameter(univ, "arrivalRate");
            custRate.setExpression("50.0");
            custRate.evaluate();

            Parameter bufferSize = new Parameter(univ, "bufferDepth");
            bufferSize.setExpression("5");
            bufferSize.evaluate();

            Parameter servRate = new Parameter(univ, "serviceRate");
            servRate.setExpression("50.0");
            servRate.evaluate();


	    Customer source = new Customer(univ, "Customer");
	    CSPBuffer middle = new CSPBuffer(univ, "Buffer", 5);
            Server server = new Server(univ, "Server");

            Parameter p1 = (Parameter)source.getAttribute("arrivalRate");
            p1.setExpression("arrivalRate");
            Parameter p2 = (Parameter)middle.getAttribute("depth");
            p2.setExpression("bufferDepth");
            Parameter p3 = (Parameter)server.getAttribute("serviceRate");
            p3.setExpression("serviceRate");
            
            IOPort out1 = (IOPort)source.getPort("output");
	    IOPort in1 = (IOPort)middle.getPort("input");
	    IOPort out2 = (IOPort)middle.getPort("output");
            IOPort in2 = (IOPort)server.getPort("input");

            IORelation rel1 = (IORelation)univ.connect(out1, in1, "R1");
            IORelation rel2 = (IORelation)univ.connect(out2, in2, "R2");
            //System.out.println(univ.description(1023));
            System.out.println(univ.getFullName() + " starting!");
            univ.getManager().startRun();
            localdir.pause();
            System.out.println("Model execution successfully paused!");
            localdir.resume();
            System.out.println("Model execution successfully resumed!");
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException(e.getMessage());
        }
    }
}

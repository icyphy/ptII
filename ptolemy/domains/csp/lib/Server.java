/* Model of a server in a M/M/1 queue.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.Token;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// CSPSink
/**

Model of a server in a M/M/1 queue. It serves customers with times
that are exponentially distributed. It is parameterized by the
Parameter "serviceRate". The default service rate is 1. The process 
continues executing until a TerminateProcesException is thrown.
<p>
@author Neil Smyth
@version $Id$

 */
public class Server extends CSPActor {
    
    /** Construct a Server in the default workspace with an empty string
     *  as its name. The actor is parameterized by the rate at which 
     *  customers are served, which is a double. The default service 
     *  rate is 1.0. 
     *  The actor is created with a single input port, of width one, 
     *  called "input".
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the port or parameter cannot 
     *   be contained by this actor.
     *  @exception NameDuplicationException If the port name coincides with
     *   a port already in this actor, or if the parameter name coincides with
     *   a parameter already in this actor
     */
    public Server() throws IllegalActionException, NameDuplicationException {
        super();
        _rate = new Parameter(this, "serviceRate", (new DoubleToken(1)) );
        _input = new IOPort(this, "input", true, false);
    }

    /** Construct a Server in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown. The actor is parameterized 
     *  by the rate at which customers are served, which is a double. 
     *  The default service rate is 1.0. The actor is created with a 
     *  single input port, of width one, called "input".
     *  <p>
     *  @param container The CompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name argument coincides with
     *   an entity already in the container.
     */
    public Server(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         this(cont, name, 1);
    }

    /** Construct a Server in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown. The actor is parameterized 
     *  by the rate at which customers are served, which is a double. 
     *  The service rate is assigned the value passed in. The actor is 
     *  created with a single input port, of width one, called "input".
     *  <p>
     *  @param container The CompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @param rate The rate at which customers are served.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name argument coincides with
     *   an entity already in the container.
     */
    public Server(CompositeActor cont, String name, double rate)
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        _rate = new Parameter(this, "serviceRate", (new DoubleToken(rate)) );
        _input = new IOPort(this, "input", true, false);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Executes the code in this actor. This actor waits for a 
     *  customer to arrive, then delays for a random time, 
     *  representing the service times, described by an 
     *  exponential distribution. A customer arrival is marked by 
     *  the arrival of a message at the input channel of the actor. 
     *  It then repeats. This process continues
     *  executing until a TerminateProcessException is thrown.
     *  @exception IllegalActionException If an error occurs during 
     *   executing the process.
     */
    public void fire() throws IllegalActionException {
        Random rand = new Random();
        double interval = 0;
        try {
            while (true) {
                Token t = _input.get(0);
                //double rate = ((DoubleToken)_rate.getToken()).doubleValue();
                double rate = 1.0;
		// exponential distribution parameterized by rate.
                interval = Math.exp(-(rand.nextDouble())*rate);
                interval = (int)(interval*1000);
                delay(interval/1000);
                System.out.println(getName() + " serviced customer: " +
                      t.toString());
            }
        } catch (NoTokenException ex) {
            throw new IllegalActionException(getName() + ": invalid get.");
        } 
    }

    /** Return false so that the process terminates.
     */
    public boolean postfire() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The input port for this actor.
    private IOPort _input;
    
    // The rate at which customers are served. It parameterizes an 
    // exponential distribution.
    private Parameter _rate;
}

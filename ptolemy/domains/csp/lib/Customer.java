/* Customer arriving in M/M/1 demo.

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
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Customer
/**
Customers arriving in M/M/1 demo. Customers arrive in a Poisson
fashion, i.e the inter-arrival times are exponentially distributed.
It is parameterized by the Parameter "arrivalRate". The default rate
of arrival is 1.
<p>
It continues executing until 10 customers have arrived or a 
TerminateProcessException is thrown.
<p>
@author Neil Smyth
@version $Id$

*/

public class Customer extends CSPActor {

    /** Construct a Customer in the default workspace with an empty string
     *  as its name. The actor is parameterized by the rate of customer 
     *  arrivals, which is a double. The default rate of arrival is 1.0. 
     *  The actor is created with a single output 
     *  port, of width one, called "output".
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the port or parameter cannot 
     *   be contained by this actor.
     *  @exception NameDuplicationException If the port name coincides with
     *   a port already in this actor, or if the parameter name coincides with
     *   a parameter already in this actor
     */
    public Customer() throws IllegalActionException, NameDuplicationException {
        super();
        _rate = new Parameter(this, "arrivalRate", (new DoubleToken(1)) );
        _output = new IOPort(this, "output", false, true);
    }

    /** Construct a Customer in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown. The actor is parameterized 
     *  by the rate of customer arrivals, which is a double. The default 
     *  rate of arrival is 1.0. The actor is created with a single output 
     *  port, of width one, called "output".
     *  <p>
     *  @param container The CompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name argument coincides with
     *   an entity already in the container.
     */
    public Customer(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         this(cont, name, 1);
    }

    /** Construct a Customer in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown. The actor is parameterized 
     *  by the rate of customer arrivals, which is a double. The arrival 
     *  rate is assigned to the value passed in.
     *  The actor is created with a single output port, of width one, 
     *  called "output".
     *  <p>
     *  @param container The CompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @param rate The rate of customer arrivals.
     *  @exception NameDuplicationException If the name argument coincides with
     *   an entity already in the container.
     */
    public Customer(CompositeActor cont, String name, double rate)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         _rate = new Parameter(this, "arrivalRate", (new DoubleToken(rate)) );
         _output = new IOPort(this, "output", false, true);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Executes the code in this actor. This actor delays for a 
     *  random time, representing the customer inter-arrival times, 
     *  described by an exponential distribution. It then sends a 
     *  message on its output channel to signal the fact that a 
     *  customer has arrived, and then repeats. It continues
     *  executing until 10 customers have arrived or a 
     *  TerminateProcessException is thrown.
     *  @exception IllegalActionException If an error occurs during 
     *   executing the process.
     */
    public void fire() throws IllegalActionException {
        Random rand = new Random();
        int count = 0;
        double interval = 0;
        while (count < 10 ) {
            //double rate = ((DoubleToken)_rate.getToken()).doubleValue();
            double rate = 1.0;
            // exponential distribution parameterized by rate.
            interval = Math.exp(-(rand.nextDouble())*rate);
            interval = (int)(interval*1000);
            delay(interval/1000);
            Token t = new IntToken(count);
            _output.send(0,t);
            System.out.println(getName() + " sent: " +
                    t.toString());
            count++;
        }
        return;
    }

    /** Return false to terminate the process.
     */
    public boolean postfire() {
        System.out.println("Customer(" + getName() +
		"):finished normally.");
	return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The rate at which customers arrive. It parameterizes an 
    // exponential distribution.
    private Parameter _rate;

    // The output port for this actor.
    private IOPort _output;
}

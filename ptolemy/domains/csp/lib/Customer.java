/* Customer arriving in M/M/1 demo.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
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
Customer arriving in M/M/1 demo. Customers arrive in a Poisson
fashion, i.e the inter-arrival times are exponentially distributed.
It is parameterized by the Parameter "arrivalRate". The default rate
of arrival is 1.
<p>
@author Neil Smyth
@version $Id$

*/

public class Customer extends CSPActor {
    public Customer() throws IllegalActionException, NameDuplicationException {
        super();
        _rate = new Parameter(this, "arrivalRate", (new DoubleToken(1)) );
        output = new IOPort(this, "output", false, true);
    }

    public Customer(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         this(cont, name, 1);
    }

    public Customer(CompositeActor cont, String name, double rate)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         _rate = new Parameter(this, "arrivalRate", (new DoubleToken(rate)) );
         output = new IOPort(this, "output", false, true);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        try {
            Random rand = new Random();
            int count = 0;
            double interval = 0;
            while (count < 100 ) {
                double rate = ((DoubleToken)_rate.getToken()).doubleValue();
                // exponential distribution parameterised by rate.
                interval = Math.exp(-(rand.nextDouble())*rate);
                interval = (int)(interval*1000);
                delay(interval/1000);
                Token t = new IntToken(count);
                output.send(0,t);
                System.out.println(getName() + " sent: " +
                        t.toString());
                count++;
            }
            System.out.println("Customer(" + getName() +
                    "):finished normally.");
            finish();
            return;
        } catch (IllegalActionException ex) {
            System.out.println("Customer: illegalActionException, exiting");
        } finally {

            /*try {
                setContainer(null);
            } catch (NameDuplicationException ex) {
                // should not happen.
                throw new InternalErrorException("CSPBuffer: cannot" +
                        "set container to null.");
            } catch (IllegalActionException ex) {
                // should not happen.
                throw new InternalErrorException("CSPBuffer: cannot" +
                        "set container to null.");
                        }*/
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    public IOPort output;


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Parameter _rate;
}

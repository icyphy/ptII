/* Model of a server in a M/M/1 queue.

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
Parameter "servicelRate". The default service rate is 1.
<p>
@author Neil Smyth
@version $Id$

 */
public class Server extends CSPActor {
    public Server() throws IllegalActionException, NameDuplicationException {
        super();
        _rate = new Parameter(this, "serviceRate", (new DoubleToken(1)) );
        _input = new IOPort(this, "input", true, false);
    }

    public Server(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         this(cont, name, 1);
    }

    public Server(CompositeActor cont, String name, double rate)
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        _rate = new Parameter(this, "serviceRate", (new DoubleToken(rate)) );
        _input = new IOPort(this, "input", true, false);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        Random rand = new Random();
        int count = 0;
        double interval = 0;
        try {
            while (count < 10 ) {
                Token t = _input.get(0);
                //double rate = ((DoubleToken)_rate.getToken()).doubleValue();
                double rate = 1.0;
		// exponential distribution parameterised by rate.
                interval = Math.exp(-(rand.nextDouble())*rate);
                interval = (int)(interval*1000);
                delay(interval/1000);
                System.out.println(getName() + " serviced customer: " +
                      t.toString());
                count++;
            }
            System.out.println("Server(" + getName() + "):finished normally.");
            return;
        } catch (IllegalActionException ex) {
            throw new TerminateProcessException(getName() + ": invalid get.");
	} catch (NoTokenException ex) {
            throw new TerminateProcessException(getName() + ": invalid get.");
        } 
    }

    /** Return false so that the process terminates.
     */
    public boolean postfire() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private IOPort _input;
    
    private Parameter _rate;
}

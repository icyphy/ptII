/* This interleaves elements from its different input streams into one 
output stream.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.actor.lib;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Commutator
/**
Merges its input streams into one output stream by alternating/circulating
between its input receivers and directing them to the output.
This actor is a polymorphic actor and should work with most of the domains.
It reads a token from one of the input receivers and writes a token to the
output port. Then it reads a token from the next input receiver and sends 
it to the next relation. The order in which the tokens are read from the 
input port is the order of their creation.

This actor can handle mutations of some types. 
The smallest granularity of mutations that this can handle are the mutations
that can occur between every read from the input channel.

In case of domains like SDF, which need to know the token consumption or
production rate for all ports to construct a firing schedule, 
this actor defines and sets three different port parameters, namely:
<UL>
<LI>Token Consumption Rate
<LI>Token Production Rate
<LI>Token Init Production
</UL>
These parameters can be ignored by domains that do not require this information.

These parameters are safe for computing a schedule only after a call to the 
initialize() method of the actor, as they are computed and set in that method.


@author Mudit Goel
@version $Id$
*/
public class Commutator extends AtomicActor{

    /** Constructor Create ports and make the input port a multiport.
     *  @param container This is the compositeActor containing this actor
     *  @param name This is the name of this actor.
     *
     *  @exception NameDuplicationException If an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException If one of the
     *  called methods throws it.
     */
    public Commutator (CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _input.setMultiport(true);
        Parameter param = new Parameter(_input, "Token Consumption Rate",
                new IntToken(1));
        param = new Parameter(_input,"Token Production Rate",
                new IntToken(1));
        param = new Parameter(_input,"Token Init Production",
                new IntToken(0));
        
        _output = new IOPort(this, "output", false, true);
        param = new Parameter(_output, "Token Consumption Rate",
                new IntToken(1));
        param = new Parameter(_output,"Token Production Rate",
                new IntToken(1));
        param = new Parameter(_output,"Token Init Production",
                new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads tokens from each of its input receivers in a circular 
     *  fashion and redirects them each to the output port. 
     *  
     *  @exception IllegalActionException This can be thrown by a called
     *  method
     */
    public void fire() throws IllegalActionException {
        for (int i=0; i<_input.getWidth(); i++) {
            _output.broadcast(_input.get(i));
        }
    }
    
    /** Initializes the actor. Sets the parameter representing the number of 
     *  tokens that the output port will produce. This parameter is required 
     *  only for domains like SDF that need this information to calculate
     *  a static schedule.
     *  
     *  @exception IllegalActionException Not thrown in this class
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Parameter param = (Parameter)_output.getAttribute("Token " +
                "Production Rate");
        param.setToken(new IntToken(_input.getWidth()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Input ports
    private IOPort _input;
    // Output port
    private IOPort _output;

}

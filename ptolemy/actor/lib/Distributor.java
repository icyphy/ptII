/* The actor that distributes its input data cyclically to different output ports

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
//// Distributor
/**
The actor reads tokens from its input stream and redirects those tokens to 
the different relations connected to the output port in a cyclic way.
This actor is a polymorphic actor and should work with most of the domains.
It reads a token from the input stream and writes a token to an output 
relation. On reading the next token from the input, it sends this token to the
next relation. The order of the relations on the output side is the order
of their creation.

This actor can handle mutations of some types. 
The smallest granularity of mutations that this can handle are the mutations
that can occur between every read from the input channel.

In case of domains like SDF, which need to know the token consumption or
production rate for all ports before they can construct a firing schedule, 
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
public class Distributor extends AtomicActor {

    /** Constructor. Creates ports and makes the output port a multiport.
     * @exception NameDuplicationException If more than one port
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public Distributor(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        Parameter param = new Parameter(_input, "Token Consumption Rate",
                new IntToken(1));
        param = new Parameter(_input,"Token Production Rate",
                new IntToken(1));
        param = new Parameter(_input,"Token Init Production",
                new IntToken(0));

        _output = new IOPort(this, "output", false, true);
        _output.setMultiport(true);
        param = new Parameter(_output, "Token Consumption Rate",
                new IntToken(1));
        param = new Parameter(_output,"Token Production Rate",
                new IntToken(1));
        param = new Parameter(_output,"Token Init Production",
                new IntToken(0));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one Token from its input port and writes this token to
     *  its output port. Needs to read one token for every relation 
     *  connected to the output port.
     *
     *  @exception IllegalActionException If there is an error in reading
     *  data from the input or writing data to the output. 
     */
    public void fire() throws IllegalActionException {
        for (int i=0; i < _output.getWidth(); i++) {
            _output.send(i, _input.get(0));
        }
    }

    /** Initializes the actor. Sets the parameter representing the number of 
     *  tokens that the input port should consume. This parameter is required 
     *  only for domains like SDF that need this information to calculate
     *  a static schedule.
     *  
     *  @exception IllegalActionException Not thrown in this class
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Parameter param = (Parameter)_input.getAttribute("Token " +
                "Consumption Rate");
        param.setToken(new IntToken(_output.getWidth()));
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The input port
    private IOPort _input;
    // The output port
    private IOPort _output;
}



/* A polymorphic commutator.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (mudit@eecs.berkeley.edu)
@AcceptedRating Red
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
A polymorphic commutator.

The commutator has an input port which is a multiport and an output port.
The types of the ports are undeclared and will be resolved by the type 
resolution mechanism. During each call to the fire method of the actor, the 
actor reads the tokens from all the input channels that have tokens and 
redirects them to the output port. The order in which the tokens are read from
the various input channels is determined by the order specified by the 
behaviour of the multiport.
<p>
This actor is not strict. That is, it does not require that each input
channel have a token upon firing. It will take the available tokens at the 
inputs and ignore the channels that do not have tokens. If no input tokens are
available at all, then no output is produced.

<p>
In case of domains like SDF, which need to know the token consumption or
production rate for all ports before they can construct a firing schedule,
this actor defines and sets three different port parameters, namely:
<UL>
<LI>TokenConsumptionRate = 1 (for the input port).
<LI>TokenProductionRate = number of input channels (for the output port).
<LI>TokenInitProduction = 0 (for output port)
</UL>
These parameters are set in the initialize() method of the actor and can be 
safely used only after a call to initialize(). These parameters can be ignored
by domains that do not require this information.
<p>
In the current form, this actor might not be safe with mutations for some of 
the domains, like SDF, as the SDF parameters are initialized only by a call to
the initialize() method of the actor..

@author Mudit Goel
@version $Id$
*/
public class Commutator extends TypedAtomicActor{

    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport. Create 
     *  the actor parameters.
     * 
     *  @param container The container.
     *  @param name This is the name of this commutator within the container.
     *  @exception NameDuplicationException If an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public Commutator(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        new Parameter(input, "TokenConsumptionRate", new IntToken(1));
        output = new TypedIOPort(this, "output", false, true);
        new Parameter(output,"TokenProductionRate", new IntToken(1));
        new Parameter(output,"TokenInitProduction", new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // Input ports
    public TypedIOPort input;
    // Output port
    public TypedIOPort output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Clone the actor into the specified workspace. This calls the base
     *  class method and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Commutator newobj = (Commutator)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }
    

    /** Read one token from each of the input channels that has tokens, and 
     *  send them to the output port. Read at most one token 
     *  from each channel, so if more than one token is pending, then leave
     *  the rest for future firings.  If none of the input
     *  channels has a token, do nothing. The order in which the tokens are
     *  read from various channels is specified by the behaviour of a 
     *  multiport. The order of the tokens at the output port is the order in 
     *  which the tokens are read from the input channels.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                output.broadcast(input.get(i));
            }
        }
    }

    /** Initialize the actor. Call the base class method. Set the parameter 
     *  representing the number of tokens that the output port will produce to
     *  the number of input channels (as returned by the getWidth() method of 
     *  the port). This parameter is required only for domains like SDF that 
     *  need this information to calculate a static schedule.
     *
     *  @exception IllegalActionException Not thrown in this class
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Parameter param = (Parameter)output.getAttribute("Token" +
                "ProductionRate");
        param.setToken(new IntToken(input.getWidth()));
    }
}







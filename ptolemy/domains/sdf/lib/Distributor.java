/* A polymorphic distributor.

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
//// Distributor
/**
A polymorphic distributor.

The distributor has an input port and an output port which is a multiport.
The types of the ports are undeclared and will be resolved by the type 
resolution mechanism. During each call to the fire method of the actor, the 
actor reads tokens from the input channel if it has tokens and 
redirects them to the output channels. The order in which the tokens are sent 
to the various output channels is determined by the order in which they were 
connected to the port according to the behaviour of a multiport.
<p>
This actor is not strict. If no input tokens are
available at all, then no output is produced. If there are more tokens at the
input channel than required for the current firing, then the tokens are left
on the input channel for future firings.

<p>
In case of domains like SDF, which need to know the token consumption or
production rate for all ports before they can construct a firing schedule,
this actor defines and sets three different port parameters, namely:
<UL>
<LI>TokenConsumptionRate =  number of output channels (for the input port).
<LI>TokenProductionRate = 1 (for the output port).
<LI>TokenInitProduction = 0 (for output port)
</UL>
These parameters are set in the initialize() method of the actor and can be 
safely used only after a call to initialize(). These parameters can be ignored
by domains that do not require this information.
<p>
In the current form, this actor might not be safe with mutations for some of 
the domains like SDF, as the SDF parameters are set only by a call to the 
initialize() method.


@author Mudit Goel
@version $Id$
*/
public class Distributor extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport. Create 
     *  the actor parameters.
     *
     *  @param container The container.
     *  @param name This is the name of this distributor within the container.
     *  @exception NameDuplicationException If an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public Distributor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        //These parameters are required for SDF
        Parameter param = new Parameter(input, "TokenConsumptionRate",
                new IntToken(1));

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        param = new Parameter(output,"TokenProductionRate",
                new IntToken(1));
        param = new Parameter(output,"TokenInitProduction",
                new IntToken(0));
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
            Distributor newobj = (Distributor)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }
    
    /** Read as many tokens from its input port as the width of its output
     *  port. Write one token to each of the output channels. If  there are
     *  more tokens than required on the input channel, then they are left
     *  in the channel for future firings. If there are less tokens than
     *  required for the firing, then the behavior is specified by the 
     *  behavior of the get method in the receivers of that domain.
     *  The order in which the tokens are
     *  written to various channels is according to the behaviour of a 
     *  multiport. 
     *
     *  @exception IllegalActionException If there is an error in reading
     *  data from the input or writing data to the output.
     */
    public void fire() throws IllegalActionException {
        for (int i = 0; i < output.getWidth(); i++) {
            output.send(i, input.get(0));
        }
    }

    /** Initialize the actor. Call the base class method. Set the parameter 
     *  representing the number of tokens that the input port will consume to
     *  the number of output channels (as returned by the getWidth() method of 
     *  the port). This parameter is required only for domains like SDF that 
     *  need this information to calculate a static schedule.
     *
     *  @exception IllegalActionException Not thrown in this class
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Parameter param = (Parameter)input.getAttribute("Token" +
                "ConsumptionRate");
        param.setToken(new IntToken(output.getWidth()));
    }

}



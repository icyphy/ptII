/* A base class for SDF actors that transform an input stream 
   into an output stream.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating 
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// SDFTransformer
/**
A base class for SDF actors that transform an input stream 
into an output stream. It provides
improved performance over actor.lib.Transformer in the SDF domain.
<p>
It provides an input port, an output port, and a rate parameter 
and manages the cloning of these ports and parameter. 
The <i>rate</i> parameter that can be used to
adjust the token production/consumption rate. The default
value of <i>rate</i> is 256.

@author Brian K. Vogel. Based on Transformer by Edward A. Lee
@version $Id$
*/

public class SDFTransformer extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFTransformer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        output = new SDFIOPort(this, "output", false, true);

	//output.setMultiport(true);

	// parameters
	rate = new Parameter(this, "rate", new IntToken(256));
	rate.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public SDFIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public SDFIOPort output;

    /** The token consumption and production rate. This parameter
     *  is semantically meaningless, and only affects performance.
     *  Choosing a value that is much larger than 1 can significantly
     *  improve performance.
     *  The default value is 256.
     */
    public Parameter rate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SDFTransformer newobj = (SDFTransformer)super.clone(ws);
        newobj.input = (SDFIOPort)newobj.getPort("input");
        newobj.output = (SDFIOPort)newobj.getPort("output");
	newobj.rate = (Parameter)newobj.getAttribute("rate");
        return newobj;
    }

    /** Set up the port's consumption/production rates.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();

	_rate =
	    ((IntToken)rate.getToken()).intValue();

	input.setTokenConsumptionRate(_rate);
	output.setTokenProductionRate(_rate);
    }

    //////////////////////////////////////////////////////////
    ////          Private Variables                       ////

    // Contains the token consumption/production rate. Derived
    // classes may wish to access the variable in the fire()
    // method.
    protected int _rate;
}

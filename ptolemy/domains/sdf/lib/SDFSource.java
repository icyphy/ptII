/* Base class for simple SDF source actors.

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
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// SDFSource
/**
Base class for simple SDF data sources. This actor is similar to
actor.lib.Source, but is optimized for better performance in the
SDF domain.
<p>
This class provides an output port
as a public variable. This class has a <i>rate</i> parameter that can
be used to adjust the token production rate. The default
value of <i>rate</i> is 256. Values larger than 1 may result in
increased performance at the expense of greater latency and higher
memory usage.

@author Brian K. Vogel. Based on actor.lib.Source by Edward A. Lee
@version $Id$
*/

public abstract class SDFSource extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  The output port is also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	output = new SDFIOPort(this, "output", false, true);
	output.setMultiport(true);
	// parameters
	rate = new Parameter(this, "rate", new IntToken(256));
	rate.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.  The type of this port is unspecified.
     *  Derived classes may set it.
     */
    public SDFIOPort output = null;

    /** The token production rate. This parameter
     *  is semantically meaningless, and only affects performance.
     *  Choosing a value that is much larger than 1 can significantly
     *  improve performance.
     *  The default value is 256.
     */
    public Parameter rate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>output</code>
     *  variable to equal the new port.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SDFSource newobj = (SDFSource)super.clone(ws);
        newobj.output = (SDFIOPort)newobj.getPort("output");
        return newobj;
    }


    /** Set up the output port's production rates.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();

	_rate =
	    ((IntToken)rate.getToken()).intValue();

	output.setTokenProductionRate(_rate);
    }

    //////////////////////////////////////////////////////////
    ////          Private Variables                       ////

    // Contains the token consumption/production rate. Derived
    // classes may wish to access the variable in the fire()
    // method.
    protected int _rate;
}

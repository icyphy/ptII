/* An actor that repeats each input sample a specified number of times.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (srao@eecs.berkeley.edu)
@AcceptedRating Red (srao@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFAtomicActor;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;





//////////////////////////////////////////////////////////////////////////
//// Repeat
/**
An actor that repeats each input token the specified number of times
(<i>numberOfTimes</i>) on the output. Note that this is a sample rate 
change, and hence affects the number of invocations of downstream actors.

@author Shankar Rao
*/

public class Repeat extends SDFAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this Repeat actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Repeat(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
      	
	input = new SDFIOPort(this, "input", true, false);
	output = new SDFIOPort(this, "output", false, true);
	output.setTypeSameAs(input);
	
	// parameters
	numberOfTimes = new Parameter(this, "numberOfTimes", new IntToken(2));
	numberOfTimes.setTypeEquals(BaseType.INT);
	blockSize = new Parameter(this, "blockSize", new IntToken(1));
	blockSize.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. This class imposes no type constraints on the 
     *  type of the input.
     */
    public SDFIOPort input;

    /** The output port. This class requires that the output port be the same
     *  type as the input.
     */
    public SDFIOPort output;

    /** The repetition factor. */
    public Parameter numberOfTimes;

    /** The number of tokens in a block. */
    public Parameter blockSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
	Repeat newobj = (Repeat) super.clone(ws);
	newobj.input = (SDFIOPort)newobj.getPort("input");
        newobj.output = (SDFIOPort)newobj.getPort("output");
	newobj.numberOfTimes = (Parameter) newobj.getAttribute("numberOfTimes");
	newobj.blockSize = (Parameter) newobj.getAttribute("blockSize");
	return newobj;
    }

    /** Repeat a block of <i>blockSize</i> input tokens <i>numberOfTimes</i> 
     *  times on the output. For example, if <i>blockSize</i> = 3 and 
     *  <i>numberOfTimes</i> = 2, then on the following input:<br> 
     *  <pre>  1 2 3 4 5 6</pre><br>
     *  this method will send the following output:<br>
     *  <pre>  1 2 3 1 2 3 4 5 6 4 5 6</pre><br>
     *  @exception IllegalActionException If there is no director
     */
    public void fire() throws IllegalActionException {
	int nt = ((IntToken)numberOfTimes.getToken()).intValue();
	int bs = ((IntToken)blockSize.getToken()).intValue();
	Token[] inputBlock = new Token[bs];
	for (int j = 0; j < bs; j += 1)
	    inputBlock[j] = input.get(0);
	for (int i = 0; i < nt; i += 1) 
	    for (int j = 0; j < bs; j += 1)
		output.send(0, inputBlock[j]);	
	
    }
    /** Calculate the token production rate and the token consumption
     *  rate based on the parameters <i>blockSize</i> and <i>numberOfTimes</i>.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
	int bs = ((IntToken)blockSize.getToken()).intValue();
	int nt = ((IntToken)numberOfTimes.getToken()).intValue();
	input.setTokenConsumptionRate(bs);
	output.setTokenProductionRate(bs * nt);
    }

}









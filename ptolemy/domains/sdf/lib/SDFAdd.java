/* An adder for the SDF domain.

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

import ptolemy.actor.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFAdd
/**
An adder for the SDF domain.
@author Brian K. Vogel.
@version $Id$
// FIXME: Add documentation!
*/

public class SDFAdd extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFAdd(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
	
	// parameters
	input2 = new SDFIOPort(this, "input2", true, false);
	input2.setTypeEquals(BaseType.DOUBLE);

        

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Second input port to add.
     */
    public SDFIOPort input2;

   

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SDFAdd newobj = (SDFAdd)super.clone(ws);
	newobj.input2 = (SDFIOPort)newobj.getPort("input2");
	
        return newobj;
    }

    /** Do Add.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	// Check parameter values.
	

	input2.getArray(0, _tokenArray2);
	input.getArray(0, _tokenArray1);
	

	for (int i = 0; i < _rate; i++) {
	    // Convert to double[].
	    _resultTokenArray[i] = 
		(DoubleToken)_tokenArray1[i].add(_tokenArray2[i]);
		//  *******  OR *********** code below seems slightly faster.
		//new DoubleToken((_tokenArray1[i].doubleValue())*(_tokenArray2[i].doubleValue()));
	}
	

	output.sendArray(0, _resultTokenArray);
    }

    /**  Allocate DoubleToken arrays for use in the fire() method.
      *  @exception IllegalActionException If the parent class throws it.
      */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tokenArray1 = new DoubleToken[_rate];
	_tokenArray2 = new DoubleToken[_rate];
	_resultTokenArray = new DoubleToken[_rate];
    }  

    /** Set up the port's consumption rates.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
	input2.setTokenConsumptionRate(_rate);
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    DoubleToken[] _tokenArray1;
    DoubleToken[] _tokenArray2;
    DoubleToken[] _resultTokenArray;

}

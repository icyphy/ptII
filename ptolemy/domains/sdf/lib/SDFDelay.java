/* A delay actor for the SDF domain.

@Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.type.BaseType;

/** This actor outputs a set of initial tokens during the initialize()
 *  method, and subsequently delays the input tokens before they
 *  are sent to the output.
 *  This actor is used when a fixed integer delay is required. The
 *  default value for the <i>initialOutputs</i> parameter causes a
 *  single integer token with value zero to be produced in
 *  initialize().
 *
 *  @author Brian K. Vogel
 *  @version $Id$
 */
// FIXME: clean up code & finish docs.
// FIXME: currently, only double valued tokens are allowed.
public class SDFDelay extends SDFTransformer {
    public SDFDelay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	// FIXME:
        //new Parameter(output, "tokenInitProduction", new IntToken(1));
        delayAmount = new Parameter(this, "delayAmount", new IntToken(1));
	output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of samples to delay the input tokens before they
     *  are sent to the output. This is also the number of zero-valued
     *  tokens that are produced when initialize() is called.
     *  Changes to this parameter after initialize() has been invoked
     *  are ignored until the next execution of the model.
     *  This parameter is an integer with the default value of 1.
     */
    public Parameter delayAmount;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     *  @exception IllegalActionException If type changes are not
     *   allowed on the specified attribute.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute != delayAmount) {
            // The base class will probably throw an exception.
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This sets the port
     *  and parameter public members of the new object and the type
     *  constraints among them.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If the base class throws it.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        SDFDelay newobj = (SDFDelay)(super.clone(ws));
        newobj.delayAmount =
            (Parameter)newobj.getAttribute("delayAmount");
	newobj.output.setTypeEquals(BaseType.DOUBLE);
        return newobj;
    }

    /** 
     *  @exception IllegalActionException If the get() or send() methods
     *   of the ports throw it.
     */
    public void fire() throws IllegalActionException {
       
	input.getArray(0, _tokenArray);
	// For each samples in the current channel:
	for (int i = 0; i < _rate; i++) {
	    // Write the current input sample into the input circular array.
	    _circBuf[_writePos % _circBufferSize] = _tokenArray[i].doubleValue();
	    _resultTokenArray[i] =  new DoubleToken(_circBuf[_readPos]);

	     // Update the pointers.
	    _writePos++;
	    // Make the write postition pointer wrap back to 
	    // the begining after it
	    // reaches the end of the buffer.
	    _writePos %= _circBufferSize;

	    _readPos++;
	    // Make the write postition pointer wrap back to 
	    // the begining after it
	    // reaches the end of the buffer.
	    _readPos %= _circBufferSize;
	}        
	output.sendArray(0, _resultTokenArray);
    }

    /**
     *  @exception IllegalActionException If 
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
        _tokenArray = new DoubleToken[_rate];
	_resultTokenArray = new DoubleToken[_rate];

	// FIXME: use sendarray() here.
        for (int i = 0; i < _delayAmount; i++) {
            output.send(0, new DoubleToken(0.0));
        }
    }

    /** FIXME
     *
     *  @exception IllegalActionException 
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

	_delayAmount = ((IntToken)delayAmount.getToken()).intValue();

	System.out.println("delay amount: " + _delayAmount);

        Parameter production =
            (Parameter)output.getAttribute("tokenInitProduction");
        production.setToken(new IntToken(_delayAmount));

       

	_circBufferSize = 2*_delayAmount;

	_circBuf = new double[_circBufferSize];

	_writePos = 0;
	//System.out.println("buffer size: " + _circBufferSize);
	_readPos = (_writePos + _delayAmount + _circBufferSize) %
                    _circBufferSize;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The size of the circular buffer to use for the delay.
    private int _circBufferSize;

    // The circular buffer array.
    private double[] _circBuf;

    // The current position in the circular buffer where
    // the input token will be written.
    private int _writePos;

    //  Current element to read from in output circular buffer.
    private int _readPos;

    // The amount of delay.
    private int _delayAmount;

    private DoubleToken[] _tokenArray;
    private DoubleToken[] _resultTokenArray;

}

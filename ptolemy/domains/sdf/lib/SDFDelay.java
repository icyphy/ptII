/* Output a variable number of initial tokens, then pass through.

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
import ptolemy.data.IntMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.actor.TypedCompositeActor;

/** This actor outputs a set of initial tokens during the initialize()
 *  method, and subsequently passes the input tokens to the output.
 *  It is used to break dependency cycles in directed loops (the
 *  initial outputs permit the computation to get started). The
 *  default value for the <i>delayVal</i> parameter causes a
 *  single integer token with value zero to be produced in
 *  initialize().
 *
 *  @author Brian K. Vogel. Based on Delay, by Steve Neuendorffer and 
 *  Edward A. Lee
 *  @version $Id$
 */
public class SDFDelay extends Transformer {
    public SDFDelay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Parameter(output, "tokenInitProduction", new IntToken(1));
        delayVal = new Parameter(this, "delayVal",
                new IntToken(1));
        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of initial tokens to produce in initialize(). This
     *  parameter must contain a nonnegative integer. It defaults to
     *  contain the value 1, corresponding to a single token delay.
     *  Changes to this parameter after initialize() has been invoked
     *  are ignored until the next execution of the model.
     */
    public Parameter delayVal;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to allow type changes on
     *  <i>delayVal</i>.
     *  @exception IllegalActionException If type changes are not
     *   allowed on the specified attribute.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute != delayVal) {
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
        newobj.delayVal =
            (Parameter)newobj.getAttribute("delayVal");
        newobj.output.setTypeAtLeast(newobj.input);
        return newobj;
    }

    /** Read exactly one input token and send it to the output.
     *  @exception IllegalActionException If the get() or send() methods
     *   of the ports throw it.
     */
    public void fire() throws IllegalActionException {
        Token message = input.get(0);
        output.send(0, message);
    }

    /** Produce on the output a sequence tokens, one for each element
     *  of the <i>delayVal</i> parameter (which is an array).
     *  @exception IllegalActionException If the send() method
     *   of the output port throws it.
     */
    public void initialize() throws IllegalActionException {
	int numInitTokens = ((IntToken)delayVal.getToken()).intValue();
        for (int i = 0; i < numInitTokens; i++) {
            output.send(0, new IntToken(0));
        }
    }

    /** Check the <i>delayVal</i> parameter for conformance (it
     *  must be a nonnegative integer token  Note that the value and type
     *  <i>delayVal</i> are observed only here.  If the value
     *  or type change during execution
     *  of the model, the change will not take effect until the next
     *  execution.
     *  The <i>tokenInitProduction</i> parameter of the output port is
     *  set equal to the value in parameter <i>delayVal</i>.
     *
     *  @exception IllegalActionException If <i>delayVal</i> parameter
     *   is invalid, or if the base class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
	// FIXME: Actually check that delayVal is valid.
       
        Parameter production =
            (Parameter)output.getAttribute("tokenInitProduction");
        production.setToken(delayVal.getToken());

        
    }
}

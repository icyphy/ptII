/* Output an initial token during initialize(), then pass through.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
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

/** This actor outputs a set of initial tokens during the initialize() method, and
 *  subsequently passes the input tokens to the output.  It is used to
 *  break dependency cycles in directed loops (the initial outputs permit
 *  the computation to get started). The default value for
 *  the <i>initialOutputs</i> parameter causes a single integer token
 *  with value zero to be produced in initialize().
 *
 *  @author Steve Neuendorffer, Edward A. Lee
 *  @version $Id$
 */
public class Delay extends Transformer {
    public Delay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Parameter(output, "TokenInitProduction", new IntToken(1));
        initialOutputs = new Parameter(this, "initialOutputs",
                _defaultInitialOutputs);
        _dummy = new Variable(this, "_dummy", new IntToken(0));
	output.setTypeAtLeast(_dummy);
        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The values that will be produced in the initialize method.
     *  This parameter must contain a MatrixToken with one row.
     *  It defaults to contain a single zero-valued integer token.
     *  Changes to this parameter after initialize() has been invoked
     *  are ignored until the next execution of the model.
     */
    public Parameter initialOutputs;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to allow type changes on
     *  <i>initialOutputs</i>.
     *  @exception IllegalActionException If type changes are not
     *   allowed on the specified attribute.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute != initialOutputs && attribute != _dummy) {
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
        Delay newobj = (Delay)(super.clone(ws));
        newobj.initialOutputs =
            (Parameter)newobj.getAttribute("initialOutputs");
        newobj._dummy =
            (Variable)newobj.getAttribute("_dummy");
	newobj.output.setTypeAtLeast(newobj._dummy);
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
     *  of the <i>initialOutputs</i> parameter (which is an array).
     *  @exception IllegalActionException If the send() method
     *   of the output port throws it.
     */
    public void initialize() throws IllegalActionException {
        for (int i = 0; i < _columnCount; i++) {
            output.send(0, _outputsArray.getElementAsToken(0, i));
        }
    }

    /** Check the <i>initialOutputs</i> parameter for conformance (it
     *  must be an array token containing a single row), and check the
     *  type of token it contains so that type resolution properly sees
     *  the constraint that the output type be at least that of the
     *  elements of this array.  Note that the value and type
     *  <i>initialOutputs</i> are observed only here.  If the value
     *  or type change during execution
     *  of the model, the change will not take effect until the next
     *  execution.
     *
     *  @exception IllegalActionException If <i>initialOutputs</i> parameter
     *   is invalid, or if the base class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Token contents = initialOutputs.getToken();
        if (!(contents instanceof MatrixToken)) {
            throw new IllegalActionException(this,
                    "Cannot set initialOutputs parameter to a non-matrix.");
        }
        _outputsArray = (MatrixToken)contents;
        int rowCount = _outputsArray.getRowCount();
        if (rowCount != 1) {
            throw new IllegalActionException(this,
                    "Cannot set initialOutputs parameter to a non-row vector.");
        }
        _columnCount = _outputsArray.getColumnCount();
        Parameter production =
            (Parameter)output.getAttribute("TokenInitProduction");
        production.setToken(new IntToken(_columnCount));

        // Set _dummy so that type constraints work properly.
        try {
            Token prototype = _outputsArray.getElementAsToken(0, 0);
            _dummy.setToken(prototype);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(this,
                    "Cannot set initialOutputs to an empty array.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Default initial outputs: a single integer token with value 0.
    private int defaultValues[][] = {{0}};

    // Default initial outputs as a matrix token.
    private MatrixToken _defaultInitialOutputs =
    new IntMatrixToken(defaultValues);

    // The outputs to be produced in the initialize method.
    private MatrixToken _outputsArray;

    // The size of the array.
    private int _columnCount;

    // Variable containing an element from initial outputs array.
    private Variable _dummy;
}

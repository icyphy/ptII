/* Output an initial token during initialize(), then pass through.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
 */
package ptolemy.domains.sdf.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SampleDelay

/**
 This actor outputs a set of initial tokens during the initialize()
 method, and subsequently passes the input tokens to the output.
 It is used to break dependency cycles in directed loops of SDF models.
 This actor declares an initial production parameter in its output port
 that is used by the SDF scheduler to properly schedule the model, and
 the initial outputs permit the computation to get started. The
 default value for the <i>initialOutputs</i> parameter causes a
 single integer token with value zero to be produced in
 initialize().

 @author Steve Neuendorffer, Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class SampleDelay extends SDFTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SampleDelay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initialOutputs = new Parameter(this, "initialOutputs");
        initialOutputs.setExpression("{0}");

        output_tokenInitProduction.setExpression("initialOutputs.length()");

        // set type constraints.
        output.setTypeAtLeast(ArrayType.elementType(initialOutputs));
        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The values that will be produced in the initialize method.
     *  This parameter must contain an ArrayToken.
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
    @Override
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute != initialOutputs) {
            super.attributeTypeChanged(attribute);
        } else {
            _typesValid = false; // Set flag to invalidate cached type constraints
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SampleDelay newObject = (SampleDelay) super.clone(workspace);

        // set the type constraints
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.initialOutputs));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        newObject.output.setTypeAtLeast(newObject.input);

        return newObject;
    }

    /** Read exactly one input token and send it to the output.
     *  @exception IllegalActionException If the get() or send() methods
     *   of the ports throw it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Token message = input.get(0);
        output.send(0, message);
    }

    /** Produce on the output a sequence tokens, one for each element
     *  of the <i>initialOutputs</i> parameter (which is an array).
     *  @exception IllegalActionException If the send() method
     *   of the output port throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        output.send(0, _outputsArray.arrayValue(), _outputsArray.length());
    }

    /** Check that the <i>initialOutputs</i> parameter contains an
     *  array token.  Set the <i>tokenInitProduction</i> parameter of
     *  the output port to the length of the value of <i>initialOutputs</i>
     *  Note that the value and type <i>initialOutputs</i> are observed
     *  only here.  If the value or type change during execution
     *  of the model, the change will not take effect until the next
     *  execution.
     *
     *  @exception IllegalActionException If <i>initialOutputs</i> parameter
     *   is invalid, or if the base class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Token contents = initialOutputs.getToken();

        if (!(contents instanceof ArrayToken)) {
            throw new IllegalActionException(this, "InitialOutputs was "
                    + contents + " which is not an" + " array token.");
        }

        _outputsArray = (ArrayToken) contents;

        getDirector().invalidateResolvedTypes();
    }

    /** Sets up backward type constraint that sets output &lt; input
     *  if backward type inference is enabled.
     *  @return A set of Inequalities.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()) {
            result.add(new Inequality(output.getTypeTerm(), input.getTypeTerm()));
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The outputs to be produced in the initialize method.
    private ArrayToken _outputsArray;
}

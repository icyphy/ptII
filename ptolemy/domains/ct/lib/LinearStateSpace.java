/* Linear state space model in the CT domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Scale;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// LinearStateSpace
/**
Linear state space model in the CT domain.

<p>The State-Space model implements a system whose behavior is defined by:
<pre>
    dx/dt = Ax + Bu
        y = Cx + Du
     x(0) = x0
</pre>
where x is the state vector, u is the input vector, and y is the output
vector. The matrix coefficients must have the following characteristics:
<pre>
A must be an n-by-n matrix, where n is the number of states.
B must be an n-by-m matrix, where m is the number of inputs.
C must be an r-by-n matrix, where r is the number of outputs.
D must be an r-by-m matrix.
</pre>
The actor accepts <i>m</i> inputs and generates <i>r</i> outputs
through a multi-input port and a multi-output port. The widths of the
ports must match the number of rows and columns in corresponding
matrices, otherwise, an exception will be thrown.
<P>
This actor works like a higher-order function. It is opaque after
construction or the change of parameters. Upon preinitialization,
the actor will create a subsystem using integrators, adders, and
scales. After that, the actor becomes transparent, and the director
takes over the control of the actors contained by this actor.

@author Jie Liu
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.domains.ct.kernel.CTBaseIntegrator
*/
public class LinearStateSpace extends TypedCompositeActor {

    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public LinearStateSpace(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        stateOutput = new TypedIOPort(this, "stateOutput", false, true);
        stateOutput.setMultiport(true);
        _opaque = true;
        _requestInitialization = true;
        double[][] one = {{1.0}};
        double[][] zero = {{0.0}};

        A = new Parameter(this, "A", new DoubleMatrixToken(one));
        A.setTypeEquals(BaseType.DOUBLE_MATRIX);

        B = new Parameter(this, "B", new DoubleMatrixToken(one));
        B.setTypeEquals(BaseType.DOUBLE_MATRIX);

        C = new Parameter(this, "C", new DoubleMatrixToken(one));
        C.setTypeEquals(BaseType.DOUBLE_MATRIX);

        D = new Parameter(this, "D", new DoubleMatrixToken(zero));
        D.setTypeEquals(BaseType.DOUBLE_MATRIX);

        initialStates = new Parameter(this, "initialStates",
                new DoubleMatrixToken(zero));
        initialStates.setTypeEquals(BaseType.DOUBLE_MATRIX);
        getMoMLInfo().className = "ptolemy.domains.ct.lib.LinearStateSpace";

        // icon
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-30\" "
                + "width=\"100\" height=\"60\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-45\" y=\"-10\" "
                + "style=\"font-size:14\">\n"
                + "dx/dt=Ax+Bu "
                + "</text>\n"
                + "<text x=\"-45\" y=\"10\" "
                + "style=\"font-size:14\">\n"
                + "    y=Cx+Du"
                + "</text>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        ports and parameters                 ////

    /** Multi-input port.
     */
    public TypedIOPort input;

    /** Multi-output port.
     */
    public TypedIOPort output;

    /** State output multiport.
     */
    public TypedIOPort stateOutput;


    /** The A matrix in the state-space representation. It must be a
     *  square matrix.
     *  The default value is [[1.0]].
     */
    public Parameter A;

    /** The B matrix in the state-space representation. The number of
     *  rows must equal to the number of rows of the A matrix. The number
     *  of columns must equal to the width of the input port.
     *  The default value is [[1.0]].
     */
    public Parameter B;

    /** The C matrix in the state-space representation. The number of
     *  columns must equal to the number of columns of the A matrix.
     *  The number of rows must equal to the width of the output port.
     *  The default value is [[1.0]].
     */
    public Parameter C;

    /** The D matrix in the state-space representation. The number of
     *  columns must equal to the width of the input port.
     *  The number of rows must equal to the width of the output port.
     *  The default value is [[0.0]].
     */
    public Parameter D;

    /** The initial condition for the state variables. This must be
     *  a vector (double matrix with only one row) whose length equals
     *  to the number of state variables.
     *  The default value is [0.0].
     */
    public Parameter initialStates;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is <i>A, B, C, D</i> or <i>initialState</i>
     *  parameters, check that they are indeed matrices and vectors,
     *  and request for initialization from the director if there is one.
     *  Other sanity checks like the dimensions of the matrices will
     *  be done in the preinitialize() method.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == A) {
            // Check that it is a square matrix.
            DoubleMatrixToken token = (DoubleMatrixToken)A.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0 ||
                    token.getRowCount() != token.getColumnCount()) {
                throw new IllegalActionException(this,
                        "The A matrix must be a nonempty square matrix.");
            }
            _requestInitialization = true;
        } else if (attribute == B) {
            // Check that B is a matrix.
            DoubleMatrixToken token = (DoubleMatrixToken)B.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this,
                        "The B matrix must be a nonempty matrix.");
            }
            _requestInitialization = true;
        } else if (attribute == C) {
            // Check that C is a matrix.
            DoubleMatrixToken token = (DoubleMatrixToken)C.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this,
                        "The C matrix must be a nonempty matrix.");
            }
            _requestInitialization = true;
        } else if (attribute == D) {
            DoubleMatrixToken token = (DoubleMatrixToken)D.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this,
                        "The D matrix must be a nonempty matrix.");
            }
            _requestInitialization = true;
        } else if (attribute == initialStates) {
            // The initialStates parameter should be a row vector.
            DoubleMatrixToken token =
                (DoubleMatrixToken)initialStates.getToken();
            if (token.getRowCount() != 1 || token.getColumnCount() < 1) {
                throw new IllegalActionException(this,
                        "The initialStates must be a row vector.");
            }
            // Changes of the initialStates parameter are ignored after
            // the execution.
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the executive director, regardless what isOpaque() returns.
     *  @return the executive director.
     */
    public Director getDirector() {
        if (_opaque) {
            return null;
        } else {
            return getExecutiveDirector();
        }
    }

    /** Return the opaqueness of this composite actor. This actor is
     *  opaque if it has not been preinitialized after creation or
     *  changes of parameters. Otherwise, it is not opaque.
     */
    public boolean isOpaque() {
        return _opaque;
    }

    /** Request the reinitialization.
     *  @return True if the super class returns true.
     *  @exception IllegalActionException If thrown by super class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_requestInitialization) _requestInitialization();
        return super.postfire();
    }

    /** Sanity check the parameters; if the parameters are legal
     *  create a continuous-time subsystem that implement the model,
     *  preinitialize all the actors in the subsystem,
     *  and set the opaqueness of this actor to true.
     *  This method needs the write access on the workspace.
     *  @exception IllegalActionException If there is no CTDirector,
     *  or any contained actors throw it in its preinitialize() method.
     */
    public void preinitialize() throws IllegalActionException {
        // Check parameters.
        _checkParameters();
        // We work at the token level with out copying the matrices.
        DoubleMatrixToken a = (DoubleMatrixToken)A.getToken();
        int n = a.getRowCount();
        DoubleMatrixToken b = (DoubleMatrixToken)B.getToken();
        int m = b.getColumnCount();
        DoubleMatrixToken c = (DoubleMatrixToken)C.getToken();
        int r = c.getRowCount();
        /* DoubleMatrixToken d = (DoubleMatrixToken)*/D.getToken();
        /* DoubleMatrixToken x0 = (DoubleMatrixToken)*/initialStates.getToken();

        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();
            // Create the model
            Integrator[] integrators = new Integrator[n];
            IORelation[] states = new IORelation[n];

            AddSubtract[] stateAdders = new AddSubtract[n];
            // Integrators
            for (int i = 0; i < n; i++) {
                integrators[i] = new Integrator(this, "state_" + i);
                integrators[i].initialState
                    .setExpression("initialStates(0," + i + ")");
                states[i] = new TypedIORelation(this, "relation_state_" + i);
                integrators[i].output.link(states[i]);
                // One adder per integrator.
                stateAdders[i] = new AddSubtract(this, "stateAdder_"+i);
                connect(stateAdders[i].output, integrators[i].input);
                stateOutput.link(states[i]);
            }
            // State feedback
            Scale[][] feedback = new Scale[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    // We don't create the Scale if the corresponding element
                    // in the A matrix is 0.
                    feedback[i][j] = new Scale(this,
                            "feedback_" + i + "_" +j);
                    feedback[i][j].factor
                        .setExpression("A(" + i + ", " + j + ")");
                    feedback[i][j].input.link(states[j]);
                    connect(feedback[i][j].output, stateAdders[i].plus);
                }
            }
            // Inputs
            Scale[][] inputScales = new Scale[n][m];
            IORelation[] inputs = new IORelation[m];
            for (int j = 0; j < m; j++) {
                inputs[j] = new TypedIORelation(this, "relation_input_" + j);
                input.link(inputs[j]);
                // Create input scales.
                for (int i = 0; i < n; i++) {
                    // We create a scale for each input even if the
                    // corresponding element in B is 0. Otherwise,
                    // if the elements of A's in this row are also zero,
                    // then we will have an illegal topology.
                    inputScales[i][j] = new Scale(this, "b_" + i + "_" + j);
                    inputScales[i][j].factor
                        .setExpression("B(" + i + ", " + j + ")");
                    inputScales[i][j].input.link(inputs[j]);
                    connect(inputScales[i][j].output, stateAdders[i].plus);
                }
            }
            // Outputs
            AddSubtract[] outputAdders = new AddSubtract[r];
            Scale[][] outputScales = new Scale[r][n];
            for (int l = 0; l < r; l++) {
                outputAdders[l] = new AddSubtract(this, "outputAdder" + l);
                connect(outputAdders[l].output, output);
                // Create the output scales only if the corresponding
                // 'c' element is not 0.
                for (int i = 0; i < n; i++) {
                    outputScales[l][i] = new Scale(this,
                            "outputScale_" + l + "_" + i);
                    outputScales[l][i].factor
                        .setExpression("C(" + l + ", " + i + ")");
                    outputScales[l][i].input.link(states[i]);
                    connect(outputScales[l][i].output,
                            outputAdders[l].plus);
                }
            }
            // Direct feed through.
            Scale[][] feedThrough = new Scale[r][m];
            for (int l = 0; l < r; l++) {
                for (int j = 0; j < m; j++) {
                    // Create the scale only if the element is not 0.
                    feedThrough[l][j] = new Scale(this,
                            "feedThrough_" + l + "_" + j);
                    feedThrough[l][j].factor
                        .setExpression("D(" + l + ", " + j + ")");
                    feedThrough[l][j].input.link(inputs[j]);
                    connect(feedThrough[l][j].output,
                            outputAdders[l].plus);
                }
            }
            _opaque = false;
            _workspace.incrVersion();
        } catch (NameDuplicationException ex) {
            // Should never happen.
            throw new InternalErrorException("Duplicated name when "
                    + "constructing the subsystem" + ex.getMessage());
        }finally {
            _workspace.doneWriting();
        }
        // preinitialize all contained actors.
        for (Iterator i = deepEntityList().iterator(); i.hasNext();) {
            Actor actor = (Actor)i.next();
            actor.preinitialize();
        }
    }

    /** Stop the current firing. This method overrides the stopFire()
     *  method in TypedCompositeActor base class, so that it will not
     *  invoke the local director (since there is none). This method
     *  should not be called after initialization phase, i.e. when
     *  the actor is transparent.
     */
    public void stopFire() {
        return;
    }

    /** Set the opaqueness back to true and call the wrapup() method
     *  of the super class.
     *  @exception IllegalActionException If there is no director.
     */
    public void wrapup() throws IllegalActionException {
        _opaque = true;
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    private void _checkParameters() throws IllegalActionException {
        DoubleMatrixToken a = (DoubleMatrixToken)A.getToken();
        int n = a.getRowCount();
        DoubleMatrixToken b = (DoubleMatrixToken)B.getToken();
        if (b.getRowCount() != n) {
            throw new IllegalActionException(this,
                    "The number of rows of the B matrix (" + b.getRowCount() +
                    ") should be equal to "
                    + "the number of rows of the A matrix (" + n + ").");
        }
        int m = b.getColumnCount();
        if (input.getWidth() != m) {
            throw new IllegalActionException(this,
                    "The number of columns of the B matrix (" +
                    b.getColumnCount() + ") should be equal to "
                    + "the width of the input port ("
                    + input.getWidth() + ").");
        }
        DoubleMatrixToken c = (DoubleMatrixToken)C.getToken();
        if (c.getColumnCount() != n) {
            throw new IllegalActionException(this,
                    "The number of columns of the C matrix (" +
                    c.getColumnCount() + ") should be equal to "
                    + "the number of rows of the A matrix (" + n + ").");
        }
        // The output width is not checked, since we may only want
        // to use some of the outputs
        DoubleMatrixToken d = (DoubleMatrixToken)D.getToken();
        if (c.getRowCount() != d.getRowCount()) {
            throw new IllegalActionException(this,
                    "The number of rows of the D matrix (" + d.getRowCount() +
                    ") should be equal to "
                    + "the number of rows of the C matrix (" +
                    c.getRowCount() + ").");
        }
        if (d.getColumnCount() != input.getWidth()) {
            throw new IllegalActionException(this,
                    "The number of columns of the D matrix (" +
                    d.getColumnCount() + ") should be equal to "
                    + "the width of the input port ("
                    + input.getWidth() + ").");
        }
        DoubleMatrixToken x0 = (DoubleMatrixToken)initialStates.getToken();
        if (x0.getColumnCount() != n) {
            throw new IllegalActionException(this,
                    "The number of initial states (" + x0.getColumnCount() +
                    ") should equal to "
                    + "the number of columns of the A matrix (" + n + ").");
        }
    }

    /** Set this composite actor to opaque and request for reinitialization
     *  from the director if there is one.
     */
    private void _requestInitialization() {
        // Request for initialization.
        Director dir = getDirector();
        if (dir != null) {
            dir.requestInitialization(this);
        }
        // Set this composite to opaque.
        _opaque = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // opaqueness.
    private boolean _opaque;
    // request for initialization.
    private boolean _requestInitialization;
}

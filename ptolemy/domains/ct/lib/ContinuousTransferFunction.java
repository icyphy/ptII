/* Transfer function in the CT domain.

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
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Scale;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ContinuousTransferFunction
/**
A transfer function in the continuous time domain.
This actor implements a transfer function where the single input (u) and
single output (y) can be expressed in (Laplace) transfer function
form as the following equation:
<pre>
    y(s)    b(1)*s^(m-1) + b(2)*s^(m-2) + ... + b(m)
   ----- = -------------------------------------------
    u(s)    a(1)*s^(n-1) + a(2)*s^(n-2) + ... + a(n)
</pre>
where m and n are the number of numerator and denominator coefficients,
respectively. This actors has two parameters -- numerator and denominator --
containing the coefficients of the numerator and denominator in
descending powers of s. These coefficients are double numbers.
The order of the denominator (n) must be greater
than or equal to the order of the numerator (m).
<p>
This actor extends TypedCompositeActor and works as a higher-order function.
Whenever the parameters are changed, the actor will build a transparent
subsystem inside it using integrators, adders, and scales. This is called
a realization of the transfer function. Notice that there are infinite
number of realizations of a transfer function, and they are equivalent if and
only if the initial conditions are all zero. Here we choose the controllable
canonical form and preset all initial states of the integrators to zero.
If you need to set arbitrary initial
conditions you have to use the state-space representation, for example,
use the LinearStateSpace actor.

@author Jie Liu
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.domains.ct.kernel.CTBaseIntegrator
*/
public class ContinuousTransferFunction extends TypedCompositeActor {

    /** Construct the composite actor with a name and a container.
     * @see ptolemy.domains.ct.kernel.CTBaseIntegrator
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public ContinuousTransferFunction(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        _opaque = true;

        numerator = new Parameter(this, "numerator");
        numerator.setExpression("{1.0}");
        numerator.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        denominator = new Parameter(this, "denominator");
        denominator.setExpression("{1.0}");
        denominator.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Do not use TypedCompositeActor as the MoML name for this actor.
        getMoMLInfo().className =
            "ptolemy.domains.ct.lib.ContinuousTransferFunction";

        // icon
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-25\" y=\"0\" "
                + "style=\"font-size:14\">\n"
                + "b(s)/a(s) \n"
                + "</text>\n"
                + "style=\"fill:blue\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        ports and parameters                 ////

    /** Single input port.
     */
    public TypedIOPort input;

    /** Single output port.
     */
    public TypedIOPort output;

    /** The coefficients of the numerator, containing an array of
     *  DoubleTokens.
     *  The default value is {1.0}.
     */
    public Parameter numerator;

    /** The coefficients of the denominator, containing an array
     *  of DoubleTokens.
     *  The array must have a length greater
     *  than or equal to the length of the numerator.
     *  The default value is {1.0}.
     */
    public Parameter denominator;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>numerator</i> or the <i>denominator</i>
     *  parameters, request for initialization from the director if
     *  there is one. Also check that the <i>denominator</i> vector
     *  cannot start with 0.
     *  Other sanity checks, like that the denominator must have a higher
     *  order than that of the numerator, and that the first element of the
     *  denominator should not be zero, are done in the preinitialize()
     *  method.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numerator) {
            // Set this composite to opaque.
            _opaque = true;
            // Request for initialization.
            Director dir = getDirector();
            if (dir != null) {
                dir.requestInitialization(this);
            }
        } else if (attribute == denominator) {
            // Check that a_0 is not 0.0
            ArrayToken aToken = (ArrayToken)(denominator.getToken());
            if (((DoubleToken)aToken.getElement(0)).doubleValue() == 0.0) {
                throw new IllegalActionException(this,
                        "The denominator coefficient cannot start with 0.");
            }
            // Set this composite to opaque.
            _opaque = true;
            // Request for initialization.
            Director dir = getDirector();
            if (dir != null) {
                dir.requestInitialization(this);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the executive director, regardless what isOpaque returns.
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

    /** Sanity check the parameters; if the parameters are legal
     *  create a continuous-time subsystem that implement the transfer
     *  function, preinitialize all the actors in the subsystem,
     *  and set the opaqueness of this actor to true.
     *  This method need the write access on the workspace.
     *  @exception IllegalActionException If there is no CTDirector,
     *  or any contained actors throw it in its preinitialize() method
     *  .
     */
    public void preinitialize() throws IllegalActionException {
        // Construct local double[] and Check dimensions.
        ArrayToken bToken = (ArrayToken)numerator.getToken();
        int m = bToken.length();
        double[] bRow = new double[m];
        for (int i = 0; i < m; i++) {
            bRow[i] = ((DoubleToken)bToken.getElement(i)).doubleValue();
        }
        ArrayToken aToken = (ArrayToken)denominator.getToken();
        int n = aToken.length();
        double[] a = new double[n];
        for (int i = 0; i < n; i++) {
            a[i] = ((DoubleToken)aToken.getElement(i)).doubleValue();
        }
        if (m > n) {
            throw new IllegalActionException(this,
                    "The order of the denominator must be greater than or "
                    + "equal to the order of the numerator.");
        }
        // Add leading zeros to bRow such that b has the same length as a.
        double[] b = new double[n];
        // Note that b is initialized to contain all zeros.
        for (int i = 1; i <= m; i++) {
            b[n-i] = bRow[m-i];
        }
        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();
            if (n == 1) {
                // Algebraic system
                if (a[0] == b[0]) {
                    connect(input, output);
                } else {
                    Scale scaleD = new Scale(this, "ScaleD");
                    scaleD.factor.setToken(new DoubleToken(b[0]/a[0]));
                    connect(input, scaleD.input);
                    connect(output, scaleD.output);
                }
            } else {
                double d = b[0]/a[0];
                int order = n-1;
                AddSubtract inputAdder = new AddSubtract(this, "InputAdder");
                AddSubtract outputAdder = new AddSubtract(this, "OutputAdder");
                Integrator[] integrators = new Integrator[order];
                IORelation[] nodes = new IORelation[order];
                Scale[] feedback = new Scale[order];
                Scale[] feedforward = new Scale[order];
                for (int i = 0; i < order; i++) {
                    // The integrator names are d0x, d1x, etc.
                    integrators[i] = new Integrator(this, "Integrator" + i);
                    feedback[i] = new Scale(this, "Feedback" + i);
                    feedback[i].factor.setToken(new DoubleToken(-a[i+1]/a[0]));
                    feedforward[i] = new Scale(this, "Feedforward" +i);
                    feedforward[i].factor.setToken(new
                        DoubleToken((b[i+1] - d * a[i+1])/a[0]));
                    // connections
                    nodes[i] = (IORelation)connect(integrators[i].output,
                            feedforward[i].input, "node" + i);
                    feedback[i].input.link(nodes[i]);
                    connect(feedback[i].output, inputAdder.plus);
                    connect(feedforward[i].output, outputAdder.plus);
                    if (i >= 1) {
                        integrators[i].input.link(nodes[i-1]);
                    }
                }
                connect(inputAdder.output, integrators[0].input);
                IORelation inputRelation = (IORelation)
                    connect(input, inputAdder.plus, "inputRelation");
                connect(output, outputAdder.output, "outputRelation");
                if (d != 0) {
                    Scale scaleD = new Scale(this, "ScaleD");
                    scaleD.factor.setToken(new DoubleToken(d));
                    scaleD.input.link(inputRelation);
                    connect(scaleD.output, outputAdder.plus);
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

    /** Set the opaqueness to true and wrapup.
     *  @exception IllegalActionException If there is no director.
     */
    public void wrapup() throws IllegalActionException {
        _opaque = true;
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // opaqueness.
    private boolean _opaque;
}

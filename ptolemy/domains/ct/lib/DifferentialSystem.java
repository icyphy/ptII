/* A differential system in the CT domain.

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
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// DifferentialSystem
/**
The differential system  model implements a system whose behavior
is defined by:
<pre>
    dx/dt = f(x, u, t)
        y = g(x, u, t)
     x(0) = x0
</pre>
where x is the state vector, u is the input vector, and y is the output
vector, t is the time. Users must give the name of the variables
by filling in parameters
<P>
The actor, upon creation, has no input and no output. Upon filling
in the names in the <i>inputVariableNames</i> and
<i>outputVariableNames</i> parameters, the ports will be created.
The name of the state variables are manually added by filling in
the <i>stateVariableNames</i> parameter.
<P>
The state equations and output maps must be manually created by users.
If there are <i>n</i> state variables <i>x</i><sub>1</sub>, ...
<i>x</i><sub>n</sub>,
then users must create <i>n</i> additional parameters, one
for each state equation. And the parameters must be named as:
"<i>x</i><sub>1</sub>_dot", ..., "<i>x</i><sub>n</sub>_dot" respectively.
Similarly, if the output ports have name <i>y</i><sub>1</sub>, ...,
<i>y</i><sub>r</sub>, then users must create additional <i>r</i>
parameters for output maps. These parameters should be named
"<i>y</i><sub>1</sub>", ... "<i>y</i><sub>r</sub>" respectively.
<P>
This actor works like a higher-order function. Upon preinitialization,
the actor will create a subsystem using integrators and expressions.
After that, the actor becomes transparent, and the director
takes over the control of the actors contained by this actor.

@author Jie Liu
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.domains.ct.kernel.CTBaseIntegrator
*/
public class DifferentialSystem extends TypedCompositeActor {

    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public DifferentialSystem(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        StringToken[] empty = new StringToken[1];
        stateVariableNames = new Parameter(this, "stateVariableNames");
        empty[0] = new StringToken("");
        stateVariableNames.setToken(new ArrayToken(empty));
        initialStates = new Parameter(this, "initialStates");
        initialStates.setTypeEquals(BaseType.DOUBLE_MATRIX);

        getMoMLInfo().className = "ptolemy.domains.ct.lib.DifferentialSystem";

        // icon
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-30\" "
                + "width=\"100\" height=\"60\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-45\" y=\"-10\" "
                + "style=\"font-size:14\">\n"
                + "dx/dt=f(x, u, t)"
                + "</text>\n"
                + "<text x=\"-45\" y=\"10\" "
                + "style=\"font-size:14\">\n"
                + "     y=g(x, u, t)"
                + "</text>\n"
                + "style=\"fill:blue\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                        ////

    /** The names of the state variables, in an array of strings.
     *  The default is an ArrayToken of an empty String.
     */
    public Parameter stateVariableNames;

    /** The initial condition for the state variables. This must be
     *  a vector (double matrix with only one row) whose
     *  default value is empty.
     */
    public Parameter initialStates;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>initialState</i>
     *  parameters, check that it is a row vector;
     *  Other sanity checks, like whether a differential equation matches
     *  a state variable name, are done in preinitialize() and run time.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialStates) {
            // The initialStates parameter should be a row vector.
            DoubleMatrixToken token =
                (DoubleMatrixToken)initialStates.getToken();
            if (token == null) {
                return;
            }
            if (token.getRowCount() != 1 || token.getColumnCount() < 1) {
                throw new IllegalActionException(this,
                        "The initialStates must be a row vector.");
            }
            // Changes of the initialStates parameter are ignored after
            // the execution.
            if (getManager()!= null &&
                    getManager().getState() == Manager.IDLE) {
                _requestInitialization();
            }
        } else if (attribute instanceof Parameter) {
            // Change of other parameters triggers reinitialization.
            super.attributeChanged(attribute);
            _requestInitialization();
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
     *  create a continuous-time subsystem that implement the model,
     *  preinitialize all the actors in the subsystem,
     *  and set the opaqueness of this actor to true.
     *  This method need the write access on the workspace.
     *  @exception IllegalActionException If there is no CTDirector,
     *  or any contained actors throw it in its preinitialize() method.
     *
     */
    public void preinitialize() throws IllegalActionException {
        // Check parameters.
        _checkParameters();

        ArrayToken stateNames = (ArrayToken)stateVariableNames.getToken();
        int n = stateNames.length();
        int m = inputPortList().size();
        int r = outputPortList().size();
        DoubleMatrixToken initial = (DoubleMatrixToken)
            initialStates.getToken();

        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();
            // Create the model
            Integrator[] integrators = new Integrator[n];
            String[] states = new String[n];
            IORelation[] stateRelations = new IORelation[n];
            Expression[] equations = new Expression[n];
            // Integrators and feedback expressions
            for (int i = 0; i < n; i++) {
                states[i] = ((StringToken)stateNames.getElement(i)).
                    stringValue().trim();
                integrators[i] = new Integrator(this, states[i]);
                integrators[i].initialState
                        .setExpression("initialStates(0," + i + ")");
                stateRelations[i] = new TypedIORelation(this,
                        "relation_" + states[i]);

                integrators[i].output.link(stateRelations[i]);
                // One Expression per integrator.
                equations[i] = new Expression(this, states[i] + "_dot");
                equations[i].expression.setExpression(((StringToken)
                        ((Parameter)getAttribute(states[i] + "_dot")).
                        getToken()).stringValue());
                //FIXME: Why should I set type here?
                equations[i].output.setTypeEquals(BaseType.DOUBLE);
                connect(equations[i].output, integrators[i].input);
            }
            // Inputs
            String[] inputs = new String[m];
            IORelation[] inputRelations = new IORelation[m];
            Iterator inputPorts = inputPortList().iterator();
            int inputIndex = 0;
            while (inputPorts.hasNext()) {
                inputs[inputIndex] = ((NamedObj)inputPorts.next()).getName();
                inputRelations[inputIndex] = new TypedIORelation(this,
                        "relation_" + inputs[inputIndex]);
                getPort(inputs[inputIndex]).link(inputRelations[inputIndex]);
                inputIndex++;
            }
            // Outputs and output expressions.
            String[] outputs = new String[r];
            Expression[] maps = new Expression[r];
            int outIndex = 0;
            Iterator outputPorts = outputPortList().iterator();
            while (outputPorts.hasNext()) {
                outputs[outIndex] = ((NamedObj)outputPorts.next()).getName();
                maps[outIndex] = new Expression(this, "output_" +
                        outputs[outIndex]);

                maps[outIndex].expression.setExpression(((StringToken)
                        ((Parameter)getAttribute(outputs[outIndex])).
                        getToken()).stringValue());
                maps[outIndex].output.setTypeEquals(BaseType.DOUBLE);
                connect(maps[outIndex].output,
                        (TypedIOPort)getPort(outputs[outIndex]));
                outIndex++;
            }
            // Connect state feedback expressions.
            for (int i = 0; i < n; i++) {
                // One port for each state variable.
                for (int k = 0; k < n; k++) {
                    TypedIOPort port = new TypedIOPort(equations[i],
                            states[k], true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(stateRelations[k]);
                }
                // One port for each input variable.
                for (int k = 0; k < m; k++) {
                    TypedIOPort port = new TypedIOPort(equations[i],
                            inputs[k], true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(inputRelations[k]);
                }
            }
            // Connect output expressions.
            // The policy now is that the output should never directly
            // depend on the input. The output Expression actors will no
            // longer have input ports that represent the input of this
            // composite actor.

            for (int l = 0; l < r; l++) {
                // One port for each state variable.
                for (int k = 0; k < n; k++) {
                    TypedIOPort port = new TypedIOPort(maps[l], states[k],
                            true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(stateRelations[k]);
                }
                /*
                  // One port for each input variable.

                  for (int k = 0; k < m; k++) {
                  TypedIOPort port = new TypedIOPort(maps[l], inputs[k],
                  true, false);
                  port.setTypeEquals(BaseType.DOUBLE);
                  port.link(inputRelations[k]);
                  }
                */
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

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    private void _checkParameters() throws IllegalActionException {
        // Check state variable names.
        ArrayToken stateNames = (ArrayToken)stateVariableNames.getToken();
        int n = stateNames.length();
        if (n < 1) {
            throw new IllegalActionException(this, "There must be at "
                    + "least one state variable for a differential system.");
        }
        // Check if any of the state variable names is an empty string.
        for (int i = 0; i < n; i++) {
            String name = (((StringToken)stateNames.getElement(i)).
                    stringValue()).trim();
            if (name.equals("")) {
                throw new IllegalActionException(this, "A state variable "
                        + "name should not be an empty string.");
            }
            // Check state equations.
            String equation = name + "_dot";
            if (getAttribute(equation) == null) {
                throw new IllegalActionException(this, "Please add a "
                        + "parameter with name \""
                        + equation + "\" to specify the state equation.");
            }
        }

        // Check output names.
        Iterator outputPorts = outputPortList().iterator();
        // Note there could be no output. If there are outputs,
        // check if any of the output variable names is an empty string,
        // and also that there is an output port with the same name.
        while (outputPorts.hasNext()) {
            TypedIOPort output = (TypedIOPort)outputPorts.next();
            String name = output.getName().trim();
            if (name.equals("")) {
                throw new IllegalActionException(this, "A output variable "
                        + "name should not be an empty string.");
            }
            // Check output maps.
            if (getAttribute(name) == null) {
                throw new IllegalActionException(this, "Please add a "
                        + "parameter with name \""
                        + name + "\" to specify the output map.");
            }
        }
    }

    /** Set this composite actor to opaque and request for reinitialization
     *  from the director if there is one.
     */
    private void _requestInitialization() {
        // Set this composite to opaque.
        _opaque = true;
        // Request for initialization.
        Director dir = getDirector();
        if (dir != null) {
            dir.requestInitialization(this);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // opaqueness.
    private boolean _opaque;


}

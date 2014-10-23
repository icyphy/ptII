/* A differential system in the Continuous domain.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 */
package ptolemy.domains.continuous.lib;

import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DifferentialSystem

/**
 A differential system in the Continuous domain.

 <p>The differential system  model implements a system whose behavior
 is defined by:
 <pre>
 dx/dt = f(x, u, t)
 y = g(x, u, t)
 x(0) = x0
 </pre>
 where x is the state vector, u is the input vector, and y is the output
 vector, t is the time. To use this actor, proceed through the following
 steps:
 <ul>
 <li> For each input in <i>u</i>, create an input port.
 Each input may have any name, since you will refer to it by
 name rather than by the symbol <i>u</i>. This actor will
 automatically create a parameter with the same name as the
 input port. That parameter will have its value set during
 execution to match the value of the input.
 Note that at this time, multiport inputs are not supported.

 <li> Fill in the <i>stateVariableNames</i> parameter, which is
 an array of strings, with the names of the state variables in <i>x</i>.
 These names can be anything you like, since you will refer them to
 by name rather than by the symbol <i>x</i>.

 <li> For each state variable name in <i>stateVariableNames</i>,
 create a parameter with a value equal to the initial value of that
 particular state variable.

 <li> Specify an update function (part of <i>f</i> above) for each
 state variable by creating a parameter named <i>name</i>_dot, where
 <i>name</i> is the name of the state variable. The value of this
 parameter should be an expression giving the rate of change of
 this state variable as a function of any of the state variables,
 any input, any other actor parameter, and (possibly), the variable
 <i>t</i>, representing current time.

 <li> For each output in <i>y</i>, create an output port.
 The output may have any name. This actor will automatically
 create a parameter with the same name as the output port.

 <li> For each parameter matching an output port, set its
 value to be an expression giving the output
 value as a function of the state variables, the inputs, any other
 actor parameter, and (possibly), the variable
 <i>t</i>, representing current time.

 </ul>
 <P>
 This actor is a higher-order component. Upon preinitialization,
 the actor will create a subsystem using integrators and expressions.
 These are not persistent (they are not exported in the MoML file),
 and will instead by created each time the actor is preinitialized.
 <p>
 This actor is based on the ptolemy.domain.ct.lib.DifferentialSystem
 actor by Jie Liu.

 @author Jie Liu and Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.domains.continuous.lib.Integrator
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
        _init();
    }

    /** Construct a DifferentialSystem in the specified
     *  workspace with no container and an empty string as a name. You
     *  can then change the name with setName(). If the workspace
     *  argument is null, then use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DifferentialSystem(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                        ////

    /** The names of the state variables, in an array of strings.
     *  The default is an ArrayToken of an empty String.
     */
    public Parameter stateVariableNames;

    /** The value of current time. This parameter is not visible in
     *  the expression screen except in expert mode. Its value initially
     *  is just 0.0, a double, but upon each firing, it is given a
     *  value equal to the current time as reported by the director.
     */
    public Parameter t;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is any parameter other than <i>stateVariableNames</i>
     *  <i>t</i>, or any parameter matching an input port,
     *  then request reinitialization.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute instanceof Parameter && attribute != t
                && attribute != stateVariableNames) {
            // If the attribute name matches an input port name,
            // do not reinitialize.
            TypedIOPort port = (TypedIOPort) getPort(attribute.getName());
            if (port == null || !port.isInput()) {
                // Change of any parameter triggers reinitialization.
                _requestInitialization();
            }
        }
        // If any parameter changes, then the next preinitialize()
        // will recreate the contents.
        _upToDate = false;
    }

    /** Override the base class to first set the value of the
     *  parameter <i>t</i> to match current time, then to set
     *  the local parameters that mirror input values,
     *  and then to fire the contained actors.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Set the time variable.
        double currentTime = getDirector().getModelTime().getDoubleValue();
        t.setToken(new DoubleToken(currentTime));

        // Set the input parameters.
        /* NOTE: There is no need to set the values of these shadow
         * variables. They are not used.
        List<TypedIOPort> inputs = inputPortList();
        for (TypedIOPort input : inputs) {
            String name = input.getName();
            if (input.getWidth() > 0 && input.isKnown(0) && input.hasToken(0)) {
                Parameter parameter = (Parameter)getAttribute(name);
                parameter.setToken(input.get(0));
            }
        }
         */

        super.fire();
    }

    /** Create the model inside from the parameter values.
     *  This method gets write access on the workspace.
     *  @exception IllegalActionException If there is no director,
     *   or if any contained actors throws it in its preinitialize() method.
     *
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (_upToDate) {
            super.preinitialize();
            return;
        }
        // Check parameters.
        _checkParameters();

        ArrayToken stateNames = (ArrayToken) stateVariableNames.getToken();
        int n = stateNames.length();
        int m = inputPortList().size();
        int r = outputPortList().size();

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
                states[i] = ((StringToken) stateNames.getElement(i))
                        .stringValue().trim();
                integrators[i] = new Integrator(this, states[i]);
                integrators[i].setPersistent(false);
                integrators[i].initialState.setExpression(states[i]);
                stateRelations[i] = new TypedIORelation(this, "relation_"
                        + states[i]);
                stateRelations[i].setPersistent(false);

                integrators[i].state.link(stateRelations[i]);

                // One Expression actor per integrator.
                equations[i] = new Expression(this, states[i] + "_dot");
                equations[i].setPersistent(false);
                equations[i].expression
                .setExpression(((Parameter) getAttribute(states[i]
                        + "_dot")).getExpression());

                connect(equations[i].output, integrators[i].derivative);
            }

            // Inputs
            String[] inputs = new String[m];
            IORelation[] inputRelations = new IORelation[m];
            Iterator inputPorts = inputPortList().iterator();
            int inputIndex = 0;

            while (inputPorts.hasNext()) {
                inputs[inputIndex] = ((NamedObj) inputPorts.next()).getName();
                inputRelations[inputIndex] = new TypedIORelation(this,
                        "relation_" + inputs[inputIndex]);
                inputRelations[inputIndex].setPersistent(false);
                getPort(inputs[inputIndex]).link(inputRelations[inputIndex]);
                inputIndex++;
            }

            // Outputs and output expressions.
            String[] outputs = new String[r];
            Expression[] maps = new Expression[r];
            int outIndex = 0;
            Iterator outputPorts = outputPortList().iterator();

            while (outputPorts.hasNext()) {
                outputs[outIndex] = ((NamedObj) outputPorts.next()).getName();
                maps[outIndex] = new Expression(this, "output_"
                        + outputs[outIndex]);
                maps[outIndex].setPersistent(false);

                maps[outIndex].expression
                .setExpression(((Parameter) getAttribute(outputs[outIndex]))
                        .getExpression());
                maps[outIndex].output.setTypeEquals(BaseType.DOUBLE);
                connect(maps[outIndex].output,
                        (TypedIOPort) getPort(outputs[outIndex]));
                outIndex++;
            }

            // Connect state feedback expressions.
            for (int i = 0; i < n; i++) {
                // Create ports for each state update Expression actor
                // and connect them.
                // One port for each state variable.
                for (int k = 0; k < n; k++) {
                    TypedIOPort port = new TypedIOPort(equations[i], states[k],
                            true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(stateRelations[k]);
                }

                // One port for each input variable.
                // Create and connect the port only if the input
                // is used.
                for (int k = 0; k < m; k++) {
                    Parameter stateUpdateSpec = (Parameter) getAttribute(states[i]
                            + "_dot");
                    Set<String> freeIdentifiers = stateUpdateSpec
                            .getFreeIdentifiers();
                    // Create an output port only if the expression references the input.
                    if (freeIdentifiers.contains(inputs[k])) {
                        TypedIOPort port = new TypedIOPort(equations[i],
                                inputs[k], true, false);
                        port.setTypeEquals(BaseType.DOUBLE);
                        port.link(inputRelations[k]);
                    }
                }
            }

            // Connect output expressions.
            // For each output expression/port:
            for (int l = 0; l < r; l++) {
                // Create ports for each state update Expression actor
                // and connect them.
                // One port for each state variable.
                for (int k = 0; k < n; k++) {
                    TypedIOPort port = new TypedIOPort(maps[l], states[k],
                            true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(stateRelations[k]);
                }

                // One port for each input variable.
                // NOTE: Do not reference input ports
                // in the expression for an output port
                // if you want that output port in a feedback loop.
                for (int k = 0; k < m; k++) {
                    Parameter outputSpec = (Parameter) getAttribute(outputs[l]);
                    Set<String> freeIdentifiers = outputSpec
                            .getFreeIdentifiers();
                    // Create an output port only if the expression references the input.
                    if (freeIdentifiers.contains(inputs[k])) {
                        TypedIOPort port = new TypedIOPort(maps[l], inputs[k],
                                true, false);
                        port.setTypeEquals(BaseType.DOUBLE);
                        port.link(inputRelations[k]);
                    }
                }
            }
            _upToDate = true;
        } catch (NameDuplicationException ex) {
            // Should never happen.
            throw new InternalErrorException("Duplicated name when "
                    + "constructing the subsystem" + ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }

        // Preinitialize the contained model.
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a port to this actor. This overrides the base class to
     *  add a parameter with the same name as the port. This parameter
     *  is not persistent and is visible only in expert mode. It will
     *  be used to mirror the values of the inputs.
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    @Override
    protected void _addPort(Port port) throws IllegalActionException,
    NameDuplicationException {
        super._addPort(port);

        // Add the parameter, if it does not already exist.
        String name = port.getName();
        if (getAttribute(name) == null) {
            Parameter parameter = new Parameter(this, name);
            parameter.setExpression("0.0");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    private void _checkParameters() throws IllegalActionException {
        // Check state variable names.
        ArrayToken stateNames = (ArrayToken) stateVariableNames.getToken();
        int n = stateNames.length();

        if (n < 1) {
            throw new IllegalActionException(this, "There must be at "
                    + "least one state variable for a differential system.");
        }

        // Check if any of the state variable names is an empty string.
        for (int i = 0; i < n; i++) {
            String name = ((StringToken) stateNames.getElement(i))
                    .stringValue().trim();

            if (name.equals("")) {
                throw new IllegalActionException(this, "A state variable "
                        + "name should not be an empty string.");
            }

            // Check state equations.
            String equation = name + "_dot";

            if (getAttribute(equation) == null) {
                throw new IllegalActionException(this, "Please add a "
                        + "parameter with name \"" + equation
                        + "\" that gives the state update expression.");
            }
        }

        // Check output names.
        Iterator outputPorts = outputPortList().iterator();

        // Note there could be no output. If there are outputs,
        // check if any of the output variable names is an empty string,
        // and also that there is an output port with the same name.
        while (outputPorts.hasNext()) {
            TypedIOPort output = (TypedIOPort) outputPorts.next();
            String name = output.getName().trim();

            if (name.equals("")) {
                throw new IllegalActionException(this, "A output variable "
                        + "name should not be an empty string.");
            }

            // Check output maps.
            if (getAttribute(name) == null) {
                throw new IllegalActionException(this, "Please add a "
                        + "parameter with name \"" + name
                        + "\" to specify the output map.");
            }
        }
    }

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        StringToken[] empty = new StringToken[1];
        stateVariableNames = new Parameter(this, "stateVariableNames");
        empty[0] = new StringToken("");
        stateVariableNames.setToken(new ArrayToken(BaseType.STRING, empty));

        setClassName("ptolemy.domains.ct.lib.DifferentialSystem");

        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");

        // This actor contains a ContinuousDirector.
        // This director is not persistent, however.
        // There is no need to store it in the MoML file, since
        // it is created here in the constructor.
        new ContinuousDirector(this, "ContinuousDirector").setPersistent(false);

        // icon
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-30\" " + "width=\"100\" height=\"60\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-45\" y=\"-10\" "
                + "style=\"font-size:14\">\n" + "dx/dt=f(x, u, t)"
                + "</text>\n" + "<text x=\"-45\" y=\"10\" "
                + "style=\"font-size:14\">\n" + "     y=g(x, u, t)"
                + "</text>\n" + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    /** Set this composite actor to opaque and request for reinitialization
     *  from the director if there is one.
     */
    private void _requestInitialization() {
        // Request for initialization.
        Director dir = getExecutiveDirector();

        if (dir != null) {
            dir.requestInitialization(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Flag indicating whether the contained model is up to date. */
    private boolean _upToDate = false;
}

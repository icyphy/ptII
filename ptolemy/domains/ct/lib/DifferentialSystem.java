/* A differential system in the CT domain.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.Expression;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;

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
The actor, upon creation, has no input and no output. Users must 
manually create the inputs/outputs, and fill in their name in the 
<i>inputVariableNames</i>/<i>outputVariableNames</i> parameters.
The name of the state variables are manually added by filling in
the <i>stateVariableNames</i> parameter.
<P>
The state equations and output maps must be manually created by users.
If there are <i>n</i> state variables <i>x</i><sub>1</sub>, ... 
<i>x</i><sub>n</sub>,
then users must create <i>n</i> additional parameters, one
for each state equation. And the parameters must be named as:
"<i>d_x</i><sub>1</sub>", ..., "<i>d_x</i><sub>n</sub>" respectively.
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
        empty[0] = new StringToken("");
        stateVariableNames = new Parameter(this, "stateVariableNames",
                new ArrayToken(empty));
        inputVariableNames = new Parameter(this, "inputVariableNames",
                new ArrayToken(empty));
        outputVariableNames = new Parameter(this, "outputVariableNames",
                new ArrayToken(empty));
        initialStates = new Parameter(this, "initialStates"); 
        initialStates.setTypeEquals(BaseType.DOUBLE_MATRIX);
        
        getMoMLInfo().className = "ptolemy.domains.ct.lib.DifferentialSystem";

        // icon
	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" "
                + "width=\"100\" height=\"60\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"5\" y=\"20\" "
                + "style=\"font-size:14\">\n"
                + "dx/dt=f(x, u, t) \n"
                + "    y=g(x, u, t) \n"
                + "</text>\n"
                + "style=\"fill:blue\"/>\n"
                + "</svg>\n");
    }

    /////////////////////////////////////////////////////////////////////
    ////                           parameters                        ////
    
    /** The names of the state variables, in an array of strings.
     *  The default is an ArrayToken of an empty String.
     */
    public Parameter stateVariableNames;
    
    /** The names of the input variables, in an array of strings.
     *  The default is  an ArrayToken of an empty String.
     */
    public Parameter inputVariableNames;

    /** The names of the output variables, in an array of strings.
     *  The default is  an ArrayToken of an empty String.
     */
    public Parameter outputVariableNames;

    /** The initial condition for the state variables. This must be
     *  a vector (double matrix with only one row) whose
     *  The default value is empty.
     */
    public Parameter initialStates;
    
    //////////////////////////////////////////////////////////////////////
    ////                      public methods                          ////
    
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
        if(attribute == initialStates) {
            // The initialStates parameter should be a row vector.
            DoubleMatrixToken token = 
                (DoubleMatrixToken)initialStates.getToken();
            if(token.getRowCount() != 1 || token.getColumnCount() < 1) {
                throw new IllegalActionException(this,
                        "The initialStates must be a row vector.");
            }
            // Changes of the initialStates parameter are ignored after
            // the execution.
        } else if (attribute instanceof Parameter) {
            // Change of other parameters triggers reinitialization.
            super.attributeChanged(attribute);
            _requestInitialization();
        } else {
            super.attributeChanged(attribute);
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
     *  @throws IllegalActionException If there is no CTDirector,
     *  or any contained actors throw it in its preinitialize() method.
     *  
     */
    public void preinitialize() throws IllegalActionException {
        // Check parameters.
        _checkParameters();

        ArrayToken stateNames = (ArrayToken)stateVariableNames.getToken();
        int n = stateNames.length();
        ArrayToken inputNames = (ArrayToken)inputVariableNames.getToken();
        int m = inputNames.length();
        ArrayToken outputNames = (ArrayToken)outputVariableNames.getToken();
        int r = outputNames.length();
        
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
            for(int i = 0; i < n; i++) {
                states[i] = ((StringToken)stateNames.getElement(i)).
                    stringValue().trim();
                integrators[i] = new Integrator(this, states[i]);
                stateRelations[i] = new TypedIORelation(this, 
                        "relation_" + states[i]);
                integrators[i].output.link(stateRelations[i]);
                // One Expression per integrator.
                equations[i] = new Expression(this, "d_" + states[i]);
                equations[i].expression.setExpression(((StringToken)
                        ((Parameter)getAttribute("d_" + states[i])).
                        getToken()).stringValue());
                //FIXME: Why should I set type here?
                equations[i].output.setTypeEquals(BaseType.DOUBLE);
                connect(equations[i].output, integrators[i].input);
            }
            // Inputs
            String[] inputs = new String[m];
            IORelation[] inputRelations = new IORelation[m];
            for(int j = 0; j < m; j++) {
                inputs[j] = ((StringToken)inputNames.getElement(j)).
                    stringValue().trim();
                inputRelations[j] = new TypedIORelation(this, 
                        "relation_" + inputs[j]);
                getPort(inputs[j]).link(inputRelations[j]);
            }
            // Outputs and output expressions.
            String[] outputs = new String[r];
            Expression[] maps = new Expression[r];
            for(int l = 0; l < r; l++) {
                outputs[l] = ((StringToken)outputNames.getElement(l)).
                    stringValue().trim();
                maps[l] = new Expression(this, "output_" + outputs[l]);
                
                maps[l].expression.setExpression(((StringToken)((Parameter)
                        getAttribute(outputs[l])).getToken()).stringValue());
                maps[l].output.setTypeEquals(BaseType.DOUBLE);
                connect(maps[l].output, (TypedIOPort)getPort(outputs[l]));
            }
            // Connect state feedback expressions.
            for(int i = 0; i < n; i++) {
                // One port for each state variable.
                for(int k = 0; k < n; k++) {
                    TypedIOPort port = new TypedIOPort(equations[i], 
                            states[k], true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(stateRelations[k]);
                }
                // One port for each input variable.
                for(int k = 0; k < m; k++) {
                    TypedIOPort port = new TypedIOPort(equations[i], 
                            inputs[k], true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(inputRelations[k]);
                }
            }
            // Connect output expressions.
            // FIXME: If an output map does not relie on an input,
            // the port and corresponding relations should not be
            // created. Otherwise, there could be unnecessary algebric
            // loops.
            for(int l = 0; l < r; l++) {
                // One port for each state variable.
                for(int k = 0; k < n; k++) {
                    TypedIOPort port = new TypedIOPort(maps[l], states[k],
                            true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(stateRelations[k]);
                }
                // One port for each input variable.
                for(int k = 0; k < m; k++) {
                    TypedIOPort port = new TypedIOPort(maps[l], inputs[k],
                            true, false);
                    port.setTypeEquals(BaseType.DOUBLE);
                    port.link(inputRelations[k]);
                }
            }
            _opaque = false;
            _workspace.incrVersion();
            System.out.println("Finish creating the model.");
        } catch (NameDuplicationException ex) {
            // Should never happen.
            throw new InternalErrorException("Duplicated name when "
                    + "constructing the subsystem" + ex.getMessage());
        }finally {
            _workspace.doneWriting();
        }
        // preinitialize all contained actors.
        for(Iterator i = deepEntityList().iterator(); i.hasNext();) {
            Actor actor = (Actor)i.next();
            actor.preinitialize();
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
    
    /** Wrapup.
     */
    public void wrapup() throws IllegalActionException {
        _opaque = true;
    }

    //////////////////////////////////////////////////////////////////////
    ////                      private variables                       ////

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    private void _checkParameters() throws IllegalActionException {
        // Check state variable names.
        ArrayToken stateNames = (ArrayToken)stateVariableNames.getToken();
        int n = stateNames.length();
        if(n < 1) {
            throw new IllegalActionException(this, "There must be at "
                    + "least one state variable for a differential system.");
        }
        // Check if any of the state variable names is an empty string.
        for(int i = 0; i < n; i++) {
            String name = (((StringToken)stateNames.getElement(i)).
                stringValue()).trim();
            if(name.equals("")) {
                throw new IllegalActionException(this, "A state variable "
                        + "name should not be an empty string.");
            }
            // Check state equations.
            String equation = "d_" + name;
            if(getAttribute(equation) == null) {
                 throw new IllegalActionException(this, "Please add a "
                         + "parameter with name \"" 
                         + equation + "\" to specify the state euqation.");
            }
        }

        // Check input names.
        ArrayToken inputNames = (ArrayToken)inputVariableNames.getToken();
        int m = inputNames.length();
        // Note there could be no input.
        // Check if any of the input variable names is an empty string,
        // and also that there is an input port with the same name.
        for(int j = 0; j < m; j++) {
            String name = (((StringToken)inputNames.getElement(j)).
                stringValue()).trim();
            if(name.equals("")) {
                throw new IllegalActionException(this, "A input variable "
                        + "name should not be an empty string.");
            }
            if(getPort(name) == null || 
                    !((IOPort)getPort(name)).isInput()) {
                throw new IllegalActionException(this, "There must be "
                        + "an input port with name " + name);
            }           
        }

        // Check output names.
        ArrayToken outputNames = (ArrayToken)outputVariableNames.getToken();
        int r = outputNames.length();
        // Note there could be no output.
        // Check if any of the output variable names is an empty string,
        // and also that there is an output port with the same name.
        for(int l = 0; l < r; l++) {
            String name = (((StringToken)outputNames.getElement(l)).
                stringValue()).trim();
            if(name.equals("")) {
                throw new IllegalActionException(this, "A output variable "
                        + "name should not be an empty string.");
            }
            if(getPort(name) == null || 
                    !((IOPort)getPort(name)).isOutput()) {
                throw new IllegalActionException(this, "There must be "
                        + "an output port with name " + name);
            }        
            // Check output maps.
            if(getAttribute(name) == null) {
                 throw new IllegalActionException(this, "Please add a "
                         + "parameter with name \"" 
                         + name + "\" to specify the output map.");
            }
        }
    }
        
    /** Set this composite actor to opaque and request for reinitialization
     *  from the director if there is one.
     */
    protected void _requestInitialization() {
        // Set this composite to opaque.
        _opaque = true;
        // Request for initialization.
        Director dir = getDirector();
        if(dir != null) {
            dir.requestInitialization(this);
        }
    }


    //////////////////////////////////////////////////////////////////////
    ////                      private variables                       ////
    // opaqueness.
    private boolean _opaque;


}

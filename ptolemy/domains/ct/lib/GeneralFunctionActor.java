/* An actor implementing general functions.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.lib;

import ptolemy.automata.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// GeneralFunctionActor
/**
An actor implementing general function. Upon each fire, the actor updates
its internal variables using input tokens. Then the output variables are
evaluated, the resulting tokens are put to the output ports.

@author Xiaojun Liu
@version $Id$
*/
public class GeneralFunctionActor extends TypedAtomicActor {

    /** Create a general function actor with the specified container and 
     *  name.
     */
    public GeneralFunctionActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _outputVarList = new VariableList(this, OUTPUT_VAR_LIST);
        _outputVarList.setRespondToChange(false);
        _inputVarList = new VariableList(this, INPUT_VAR_LIST);
        _inputVarList.setReportChange(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the expression for an output port. The expression can refer
     *  to input ports and the actor's parameters.
     *  @param name The name of an output port.
     *  @param expr The desired expression.
     *  @exception IllegalActionException If the actor does not have an
     *   output port with the given name.
     */
    public void setOutputExpression(String name, String expr) 
            throws IllegalActionException {
        try {
            IOPort port = (IOPort)getPort(name);
            if (port == null || !port.isOutput()) {
                throw new IllegalActionException(this, "Actor does not have "
                        + "an output port with name " + name);
            }
            Variable var = (Variable)_outputVarList.getAttribute(name);
            if (var == null) {
                // create the output variable
                var = new Variable(_outputVarList, name);
            }
            var.setExpression(expr);
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this, "Error in "
                    + "setOutputExpression() that should not happen.");
        }
    }

    public void initialize() {
        try {
            // create input variables
            _inputVarList.createVariables(inputPorts());
            Enumeration outVars = _outputVarList.getVariables();
            while (outVars.hasMoreElements()) {
                Variable var = (Variable)outVars.nextElement();
                var.addToScope(_inputVarList);
            }
        } catch (KernelException ex) {
            throw new InvalidStateException(this, "Error in "
                    + "initialize(): " + ex.getMessage());
        }
    }

    public void fire() throws IllegalActionException {
        // set input variables.
        Enumeration inports = inputPorts();
        while (inports.hasMoreElements()) {
            IOPort inport = (IOPort)inports.nextElement();
            if (inport.hasToken(0)) {
                Variable var = 
                        (Variable)_inputVarList.getAttribute(inport.getName());
                var.setToken(inport.get(0));
            }
        }
        // evaluate and output
        Enumeration outvars = _outputVarList.getVariables();
        while (outvars.hasMoreElements()) {
            Variable var = (Variable)outvars.nextElement();
            var.evaluate();
            IOPort outport = (IOPort)getPort(var.getName());
            outport.broadcast(var.getToken());

System.out.println("GFToken on " + outport.getFullName() + " "
        + var.getToken().stringValue());

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                 ////

    public static final String INPUT_VAR_LIST = "_InputVarList";
    public static final String OUTPUT_VAR_LIST = "_OutputVarList";
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The input variable list.
    private VariableList _inputVarList;
    private VariableList _outputVarList;
}

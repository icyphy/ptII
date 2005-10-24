/* The integrator in the CT domain.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.hs.lib;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.hs.kernel.HSBaseIntegrator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Integrator

/**
 An integrator in the continuous time (CT) domain.
 This actor has one input port and one output port. Conceptually,
 the input is the derivative of the output w.r.t. time. So an ordinary
 differential equation dx/dt = f(x, t) can be built by:
 <P>
 <pre>
 <pre>               +---------------+
 <pre>        dx/dt  |               |   x
 <pre>    +--------->|   Integrator  |---------+----->
 <pre>    |          |               |         |
 <pre>    |          +---------------+         |
 <pre>    |                                    |
 <pre>    |             |---------|            |
 <pre>    +-------------| f(x, t) |<-----------+
 <pre>                  |---------|
 </pre></pre></pre></pre></pre></pre></pre></pre></pre></pre>

 <P>
 An integrator
 is a dynamic actor that can emit a token (the state) without knowing the
 input. An integrator is a step size control actor that can control
 the accuracy of the ODE solution by adjusting step sizes.
 An integrator has memory, which is its state.
 <P>
 An integrator has one parameter: the <i>initialState</i>. At the
 initialization stage of the simulation, the state of the integrator is
 set to the initial state. Changes of the <i>initialState</i> parameter
 are ignored after the execution starts, unless the initialize() method
 is called again. The default value of the parameter is 0.0 of type
 DoubleToken.
 <P>
 To help solving the ODE, a set of variables are used:<BR>
 <I>state</I>: This is the value of the state variable at a time point,
 which has beed confirmed by all the step size control actors.
 <I>tentative state</I>: This is the value of the state variable
 which has not been confirmed. It is a starting point for other actors
 to estimate the accuracy of this integration step.
 <I>history</I>: The previous states and their derivatives. History may
 be used by multistep methods.
 <P>
 For different ODE solving methods, the functionality
 of an integrator may be different. The delegation and strategy design
 patterns are used in this class, ODESolver class, and the concrete
 ODE solver classes. Some solver-dependent methods of integrators are
 delegated to the ODE solvers.
 <P>
 An integrator can possibly have several auxiliary variables for the
 the ODE solvers to use. The number of the auxiliary variables is checked
 before each iteration. The ODE solver class provides the number of
 variables needed for that particular solver.
 The auxiliary variables can be set and get by setAuxVariables()
 and getAuxVariables() methods.
 <P>
 This is a wrapper for the CTBaseIntegrator class.

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.domains.hs.kernel.HSBaseIntegrator
 */
public class Integrator extends HSBaseIntegrator {
    /** Construct an integrator.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another star already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public Integrator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        new Parameter(input, "signalType", new StringToken("CONTINUOUS"));

        //        new Parameter(discreteInput, "signalType", new StringToken("DISCRETE"));
        new Parameter(output, "signalType", new StringToken("CONTINUOUS"));
    }
}

/* The integrator in the continuous domain.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

import ptolemy.domains.continuous.kernel.ContinuousIntegrator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Integrator

/**
 The integrator in the continuous domain.
 <p>The <i>derivative</i> port receives the derivative of the state of the integrator
 with respect to time. The <i>state</i> output port shows the state of the
 integrator. So an ordinary differential equation (ODE),
 dx/dt = f(x, t), can be built as follows:</p>
 <pre>
            +---------------+
     dx/dt  |               |   x
 +---------&gt;|   Integrator  |---------+-----&gt;
 |          |               |         |
 |          +----^-----^----+         |
 |                                    |
 |             |---------|            |
 +-------------| f(x, t) |&lt;-----------+
               |---------|
 </pre>

 <p> An integrator also has a port-parameter called <i>initialState</i>. The
 parameter provides the initial state for integration during the initialization
 stage of execution. If during execution an input token is provided on
 the port, then the state of the integrator will be reset at that time
 to the value of the token. The default value of the parameter is 0.0.</p>

 <p> An integrator also has an input port named <i>impulse</i>.
 When present, a token at the <i>impulse</i> input
 port is interpreted as the weight of a Dirac delta function.
 It cause an instantaneous increment or decrement to the state.
 If both <i>impulse</i> and <i>initialState</i> have data, then
 <i>initialState</i> dominates.</p>

 <p> An integrator can generate an output (its current state) before
 the derivative input is known, and hence can be used in feedback
 loops like that above without creating a causality loop.
 The <i>impulse</i> and <i>initialState</i> inputs
 ports must be known, however, before an output can be produced.
 The effect of data at these inputs on the output is instantaneous.</p>
 
 <p>For different ODE solving methods, the functionality
 of an integrator may be different. The delegation and strategy design
 patterns are used in this class, the abstract ODESolver class, and the
 concrete ODE solver classes. Some solver-dependent methods of integrators
 delegate to the concrete ODE solvers.</p>

 <p>An integrator can possibly have several auxiliary variables for the
 the ODE solvers to use. The ODE solver class provides the number of
 variables needed for that particular solver.
 The auxiliary variables can be set and get by setAuxVariables()
 and getAuxVariables() methods.</p>

 <p>This class is based on the CTIntegrator by Jie Liu.</p>

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (eal)
 */
public class Integrator extends ContinuousIntegrator {

    // NOTE: This is simply a wrapper for ContinuousIntegrator to make
    // it appear in the lib package.

    /** Construct an integrator.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If the name is used by
     *  another actor in the container.
     *  @exception IllegalActionException If ports can not be created, or
     *   thrown by the super class.
     *  @see ptolemy.domains.continuous.kernel.ContinuousIntegrator
     */
    public Integrator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
}

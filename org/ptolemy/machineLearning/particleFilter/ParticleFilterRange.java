/* Particle Filter Subclass for Range-only sensor measurements

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
package org.ptolemy.machineLearning.particleFilter;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
A Particle Filter Implementation for Range-only measurement models.


<p>The particle filter runs on a state space model given by
<pre>
X_{t+1} = f(X_t, U_t, t)
Y_{t} = g(X_t, U_t, t)
X(0) = X0
</pre>
where X is the state vector, U is the input vector, Y is the observation
vector, and t is the model time. To use this actor :
<ul>
<li> For each input in <i>U</i>, create an input port with an arbitrary name.
This actor will automatically create a parameter with the same name as the
input port. That parameter will have its value set during execution to match
the value of the input.

<li> Fill in the <i>stateVariableNames</i> parameter, which is
an array of strings, with the names of the state variables in <i>X</i>.
These names can be arbitrary, since you will refer them to
by name rather than by the symbol <i>X</i>.

<li> For each state variable name in <i>stateVariableNames</i>,
create a parameter with a value equal to the initial value of that
particular state variable.

<li> Specify an update function (part of <i>f</i> above) for each
state variable by creating a parameter named <i>name</i>_update, where
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


The preinitialize() method of this actor is based on the ptolemy.domain.ct.lib.DifferentialSystem
actor by Jie Liu.

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating Red (ilgea)
@see org.ptolemy.machineLearning.particleFilter.ParticleFilter
 */
public class ParticleFilterRange extends ParticleFilter {

    /**
     * Constructs a ParticleFilterRange object.
     *
     * @param container  a CompositeEntity object
     * @param name       a String ...
     * @exception NameDuplicationException ...
     * @exception IllegalActionException ...
     */
    public ParticleFilterRange(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }

    /**
     * Constructs a ParticleFilterRange object.
     *
     * @param workspace  a Workspace object
     * @exception IllegalActionException ...
     * @exception NameDuplicationException ...
     */
    public ParticleFilterRange(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Incoming measurements */
    public TypedIOPort z_m;

    /**
     * Observation expression.
     */
    public Parameter z;
    /**
     * x component update equation.
     */
    public Parameter x_update;
    /**
     * y component update equation.
     */
    public Parameter y_update;

    /**
     * Position of the observer.
     */
    public PortParameter observerPosition;

    /**
     * Initialize particle filter parameters.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {

        StringToken[] stateNames = new StringToken[2];
        stateNames[0] = new StringToken("x");
        stateNames[1] = new StringToken("y");
        stateVariableNames
        .setToken(new ArrayToken(BaseType.STRING, stateNames));
        stateVariableNames.setVisibility(Settable.EXPERT);

        observerPosition = new PortParameter(this, "observerPosition");
        observerPosition.setExpression("{0.0,0.0}");
        SingletonParameter showName = (SingletonParameter) observerPosition.getPort()
                .getAttribute("_showName");
        if (showName == null) {
            showName = new SingletonParameter(observerPosition.getPort(), "_showName");
            showName.setToken("true");
        } else {
            showName.setToken("true");
        }

        // The input port for range measurements.
        z_m = new TypedIOPort(this, "z_m", true, false);
        z_m.setTypeEquals(BaseType.DOUBLE);
        showName = (SingletonParameter) z_m
                .getAttribute("_showName");
        z_m.setDisplayName("rangeMeasurement");
        if (showName == null) {
            showName = new SingletonParameter(z_m, "_showName");
            showName.setToken("true");
        } else {
            showName.setToken("true");
        }

        // The parameter that contains the measurement expression.
        z = new Parameter(this, "z");
        z.setExpression("sqrt((x-observerPosition(0))^2 + (y-observerPosition(1))^2)");
        z.setVisibility(Settable.EXPERT);

        x_update = new Parameter(this, "x_update");
        x_update.setExpression("x");


        y_update = new Parameter(this, "y_update");
        y_update.setExpression("y");

        measurementCovariance.setExpression("[2.0]");
        prior.setExpression("{random()*200-100,random()*200-100}");

        processNoise
        .setExpression("multivariateGaussian({0.0,0.0},[3.0,0.0;0.0,3.0])");

        particleCount.setExpression("2000");

        bootstrap.setVisibility(Settable.EXPERT);
        lowVarianceSampler.setVisibility(Settable.EXPERT);
    }
}

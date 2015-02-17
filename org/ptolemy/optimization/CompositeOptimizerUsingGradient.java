/* An actor that optimizes a function defined by the inner composite  as an sdf model.

 Copyright (c) 2014 The Regents of the University of California.
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
package org.ptolemy.optimization;

import java.util.HashMap;
import java.util.Iterator;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.hoc.MirrorPort;
import ptolemy.actor.lib.hoc.ReflectComposite;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
This actor implements a composite optimizer that optimizes a function
provided as an SDF model in the inner composite.
To use the composite optimizer, construct an SDF model on the inside
that operates on the x input to output four values: (i) an intermediate
result, that is a function of x and possibly other inputs, (ii) a
double array containing the values of the constraints, calculated for
the specific x input, (iii) a double matrix that is a gradient of a 
function of x, and (iv) an array of double matrices that is a gradient of
the constraints. If the gradients (iii) and (iv) can't be defined, un-check 
the parameter "useGradient" in expert mode so that (iii) and (iv) are not used.
<p>
In the case that the objective function is only a function of x, the
trigger input should be used to start a new optimization round.
<p>
The variable named "maxEvaluations" can be used to limit the number
of evaluations. Specifically, the inside SDF model will be fired at most
 <i>maxEvaluations</i> times. There may be several reasons it is fired
 less than this number. The iteration may terminate due to any of the
 following reasons: (i) convergence criteria met (ii) user requests
 termination (iii) the roundoff errors become damaging, especially if
 the function is not smooth, and no optimal value can be found under
 given constraints (iv) maximum number of evaluations are reached.
<p>
This actor is properly viewed as a "higher-order component" in
that its contained actor is a parameter that specifies how to
operate on input arrays.  It is inspired by the higher-order
functions of functional languages, but unlike those, the
contained actor need not be functional. That is, it can have
state.
<p>
If the gradients are used (the parameter "useGradient" is checked), 
Barrier Method is used as the solver. Barrier Method performs optimization 
efficiently using gradients and estimating the hessian of the objective function 
and the constraints. If the parameter "useGradient" is un-checked, 
current implementation uses Cobyla as the solver. Cobyla implements
the trust-region-reflective algorithm and performs a type of gradient
descent optimization, ideal for objective functions that are non-convex
and/or with unknown gradient.

@author Shuhei Emoto, Ilge Akkaya, Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (shuhei)
@Pt.AcceptedRating
 */
public class CompositeOptimizerUsingGradient extends ReflectComposite {

    /**
     * Constructs a CompositeOptimizer object.
     *
     * @param workspace  a Workspace object
     * @exception IllegalActionException ...
     * @exception NameDuplicationException ...
     */
    public CompositeOptimizerUsingGradient(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /**
     * Constructs a CompositeOptimizer object.
     *
     * @param container  a CompositeEntity object
     * @param name       a String ...
     * @exception IllegalActionException ...
     * @exception NameDuplicationException ...
     */
    public CompositeOptimizerUsingGradient(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * The expert parameter that denotes the beginning step-size.
     */
    public Parameter rhoBeg;
    /**
     * The expert parameter that denotes the final step-size.
     */
    public Parameter rhoEnd;
    /**
     * The expert parameter that decides whether to reuse previous result as the initial value.
     */
    public Parameter reusePreviousResult;
    /**
     * The expert parameter that decides whether to use Interior point method or not.
     */
    public Parameter useGradient;
    /**
     * Maximum number of function evaluations per iteration.
     */
    public Parameter maxEvaluations;
    /**
     * Optimization mode. ( min or max)
     */
    public Parameter mode;
    /**
     * The dimension of optimization space. Also equal to the length of the optimization variable array, x.
     */
    public Parameter dimensionOfOptimizationSpace;
    /**
     * Number of constraints checked at each evaluation. The expected type of constraints is a double array with length <i>numberOfConstraints</i>.
     */
    public Parameter numberOfConstraints;

    /**
     *  Optimal Value
     */
    public TypedIOPort optimalValue;

    /**
     *  Trigger.
     */
    public TypedIOPort trigger;

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rhoBeg) {
            double rho = ((DoubleToken) rhoBeg.getToken()).doubleValue();
            _rhobeg = rho;
        } else if (attribute == rhoEnd) {
            double rho = ((DoubleToken) rhoEnd.getToken()).doubleValue();
            _rhoend = rho;
        } else if (attribute == reusePreviousResult) {
            _reusePreviousResult = ((BooleanToken) reusePreviousResult.getToken()).booleanValue();
        } else if (attribute == useGradient) {
            _useGradient = ((BooleanToken) useGradient.getToken()).booleanValue();
        } else if (attribute == dimensionOfOptimizationSpace) {
            _dimension = ((IntToken) dimensionOfOptimizationSpace.getToken())
                    .intValue();
        } else if (attribute == mode) {
            String modeStr = ((StringParameter) mode).stringValue();
            if (modeStr.equalsIgnoreCase("MAX")) {
                _mode = MAXIMIZE;
            } else if (modeStr.equalsIgnoreCase("MIN")) {
                _mode = MINIMIZE;
            }
        } else if (attribute == maxEvaluations) {
            _maxEvaluations = ((IntToken) maxEvaluations.getToken()).intValue();
        } else if (attribute == numberOfConstraints) {
            _numConstraints = ((IntToken) numberOfConstraints.getToken())
                    .intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {

        CompositeOptimizerUsingGradient result = (CompositeOptimizerUsingGradient) super.clone(workspace);
        result._optInput = null;
        result._firstIteration = true;
        try {
            // Remove the old inner director(s) that is(are) in the wrong workspace.
            String directorName = null;
            Iterator dirs = result.attributeList(OptimizerDirector.class)
                    .iterator();
            while (dirs.hasNext()) {
                OptimizerDirector oldDirector = (OptimizerDirector) dirs.next();
                if (directorName == null) {
                    directorName = oldDirector.getName();
                }
                oldDirector.setContainer(null);
            }

            // Create a new IterateDirector that is in the right workspace.
            OptimizerDirector directorNew = result.new OptimizerDirector(
                    workspace);
            directorNew.setContainer(result);
            directorNew.setName(directorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        return result;
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        setClassName("org.ptolemy.optimization.CompositeOptimizer");
        OptimizerDirector director = new OptimizerDirector(workspace());
        director.setContainer(this);
        director.setName(uniqueName("OptimizerDirector"));
        director.setPersistent(false);

        optimalValue = new TypedIOPort(this, OPTIMAL_VALUE_PORT_NAME, false,
                true);
        optimalValue.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);
        SingletonParameter showName = (SingletonParameter) trigger
                .getAttribute("_showName");
        if (showName == null) {
            showName = new SingletonParameter(trigger, "_showName");
            showName.setToken("true");
        } else {
            showName.setToken("true");
        }

        mode = new StringParameter(this, "mode");
        mode.setExpression("MIN");
        mode.addChoice("MIN");
        mode.addChoice("MAX");

        maxEvaluations = new Parameter(this, "maxEvaluations");
        maxEvaluations.setExpression("100000");
        maxEvaluations.setTypeEquals(BaseType.INT);

        dimensionOfOptimizationSpace = new Parameter(this,
                "dimensionOfOptimizationSpace");
        dimensionOfOptimizationSpace.setTypeEquals(BaseType.INT);
        dimensionOfOptimizationSpace.setExpression("1");
        numberOfConstraints = new Parameter(this, "numberOfConstraints");
        numberOfConstraints.setTypeEquals(BaseType.INT);
        numberOfConstraints.setExpression("1");
        _numConstraints = 1;

        rhoBeg = new Parameter(this, "rhoBeg");
        rhoBeg.setExpression("0.1");
        rhoBeg.setVisibility(Settable.EXPERT);
        _rhobeg = 0.1;

        rhoEnd = new Parameter(this, "rhoEnd");
        rhoEnd.setExpression("1E-6");
        rhoEnd.setVisibility(Settable.EXPERT);
        _rhoend = 1E-6;

        reusePreviousResult = new Parameter(this, "reusePreviousResult");
        reusePreviousResult.setTypeEquals(BaseType.BOOLEAN);
        reusePreviousResult.setVisibility(Settable.EXPERT);
        reusePreviousResult.setExpression("false");
        _firstStep = true;

        useGradient = new Parameter(this, "useGradient");
        useGradient.setTypeEquals(BaseType.BOOLEAN);
        useGradient.setVisibility(Settable.EXPERT);
        useGradient.setExpression("true");
        _useGradient = true;

        //x.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        _tokenMap = new HashMap<IOPort, Token>();
        _firstIteration = true;
    }
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _firstStep = true;
    }
    ///////////////////////////////////////////////////////////////////
    //// IterateComposite

    /** This is a specialized composite actor for use in CompositeOptimizer.
     *  In particular, it ensures that if ports are added or deleted
     *  locally, then corresponding ports will be added or deleted
     *  in the container.  That addition will result in appropriate
     *  connections being made.
     */
    public static class OptimizerComposite extends
    ReflectComposite.ReflectCompositeContents {
        // NOTE: This has to be a static class so that MoML can
        // instantiate it.

        /** Construct an actor with a name and a container.
         *  @param container The container.
         *  @param name The name of this actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public OptimizerComposite(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            SDFDirector director = new SDFDirector(workspace());
            director.setContainer(this);
            director.setName(uniqueName("SDFDirector"));
            _init();

        }

        private void _init() throws IllegalActionException,
        NameDuplicationException {

            MirrorPort intermediate = new MirrorPort(workspace());
            intermediate.setContainer(this);
            intermediate.setName(INTERMEDIATE_VALUE_PORT_NAME);
            intermediate.setTypeEquals(BaseType.DOUBLE);
            intermediate.setOutput(true);

            MirrorPort insideConstraints = new MirrorPort(workspace());
            insideConstraints.setContainer(this);
            insideConstraints.setName(CONSTRAINTS_PORT_NAME);
            insideConstraints.setTypeEquals(new ArrayType(BaseType.DOUBLE));
            insideConstraints.setOutput(true);

            MirrorPort xIn = new MirrorPort(workspace());
            xIn.setContainer(this);
            xIn.setName(OPTIMIZATION_VARIABLE_NAME);
            xIn.setTypeEquals(new ArrayType(BaseType.DOUBLE));
            xIn.setInput(true);

            MirrorPort intermediate_dfx = new MirrorPort(workspace());
            intermediate_dfx.setContainer(this);
            intermediate_dfx.setName(GRADIENT_VALUE_PORT_NAME);
            intermediate_dfx.setTypeEquals(BaseType.DOUBLE_MATRIX);
            intermediate_dfx.setOutput(true);

            MirrorPort intermediate_dgx = new MirrorPort(workspace());
            intermediate_dgx.setContainer(this);
            intermediate_dgx.setName(GRADIENT_CONSTRAINTS_PORT_NAME);
            intermediate_dgx.setTypeEquals(new ArrayType(BaseType.DOUBLE_MATRIX));
            intermediate_dgx.setOutput(true);
            
            // hide the mirror ports in top level composite

            IOPort p = (IOPort) (((CompositeActor) getContainer())
                    .getPort(INTERMEDIATE_VALUE_PORT_NAME));
            if (p != null) {
                SingletonParameter hidden = (SingletonParameter) p
                        .getAttribute("_hide");
                if (hidden == null) {
                    hidden = new SingletonParameter(p, "_hide");
                    hidden.setToken("true");
                } else {
                    hidden.setToken("true");
                }
            }
            p = (IOPort) (((CompositeEntity) getContainer())
                    .getPort(CONSTRAINTS_PORT_NAME));
            if (p != null) {
                SingletonParameter hidden = (SingletonParameter) p
                        .getAttribute("_hide");
                if (hidden == null) {
                    hidden = new SingletonParameter(p, "_hide");
                    hidden.setToken("true");
                } else {
                    hidden.setToken("true");
                }
            }
            p = (IOPort) (((CompositeEntity) getContainer())
                    .getPort(OPTIMIZATION_VARIABLE_NAME));
            if (p != null) {
                SingletonParameter hidden = (SingletonParameter) p
                        .getAttribute("_hide");
                if (hidden == null) {
                    hidden = new SingletonParameter(p, "_hide");
                    hidden.setToken("true");
                } else {
                    hidden.setToken("true");
                }
            }
            p = (IOPort) (((CompositeEntity) getContainer())
                    .getPort(GRADIENT_VALUE_PORT_NAME));
            if (p != null) {
                SingletonParameter hidden = (SingletonParameter) p.getAttribute("_hide");
                if (hidden == null) {
                    hidden = new SingletonParameter(p, "_hide");
                    hidden.setToken("true");
                } else {
                    hidden.setToken("true");
                }
            }
            p = (IOPort) (((CompositeEntity) getContainer())
                    .getPort(GRADIENT_CONSTRAINTS_PORT_NAME));
            if (p != null) {
                SingletonParameter hidden = (SingletonParameter) p.getAttribute("_hide");
                if (hidden == null) {
                    hidden = new SingletonParameter(p, "_hide");
                    hidden.setToken("true");
                } else {
                    hidden.setToken("true");
                }
            }
        }
    }

    /** This is a specialized director that fires the input SDF model
     *  repeatedly for different values of the optimization variable.
     *  The director stores the tokens from input ports and re-sends them
     *  to the inside receivers for each execution of the inside composite.
     *  The fire() method makes a call to Cobyla.FindMinimum, which is a
     *  derivative-free optimizer that uses a sequential trust-region algorithm
     *  with linear approximations.
     *
     *  @see com.cureos.numerics.Cobyla
     */
    public class OptimizerDirector extends Director {

        /**
         * Constructs a CompositeOptimizer$OptimizerDirector object.
         *
         * @param workspace  a Workspace object
         * @exception IllegalActionException ...
         * @exception NameDuplicationException ...
         */
        public OptimizerDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
        }

        /** iterate inner model one step */
        private double oneStepIteration (double[] x, double[] gx, double[] dfx, double[][] dgx) 
                throws IllegalActionException {
            DoubleToken[] xTokens = new DoubleToken[x.length];
            for (int i = 0; i < xTokens.length; i++) {
                xTokens[i] = new DoubleToken(x[i]);
            }
            // convert x into an array token
            ArrayToken xAsToken = new ArrayToken(xTokens);

            // get the optimization variable port
            MirrorPort xPort = (MirrorPort) (((CompositeActor) getContainer())
                    .getPort(OPTIMIZATION_VARIABLE_NAME));
            if (xPort == null) {
                throw new IllegalActionException(getContainer(),
                        OPTIMIZATION_VARIABLE_NAME
                        + " port could not be found.");
            }
            // send x value to the inside port for the new execution
            xPort.sendInside(0, xAsToken);

            // before firing the inside composite, make sure transferInputs are called
            CompositeActor container = (CompositeActor) getContainer();
            if (_firstIteration) {
                _firstIteration = false;
            } else {
                // transferInputs has already been called
                Iterator<IOPort> inports = container.inputPortList().iterator();
                while (inports.hasNext()) {
                    IOPort p = inports.next();
                    if (!(p instanceof ParameterPort)) {
                        _retransferInputs(p);
                    }
                }
            }

            // fire the inside composite.
            OptimizerDirector.super.fire();
            // if stop has been requested, the inside might not have produced tokens. so do not proceed
            if (_stopRequested) {
                return 0;
            } else {
                Iterator<IOPort> outports = container.outputPortList().iterator();

                double ret_val = 0;
                while (outports.hasNext()) {
                    IOPort p = outports.next();
                    // these we don't need. will find a way to do this cleanly. perhaps don't even link the ports. which should already be the case really.

                    if (p.getName()
                            .equals(INTERMEDIATE_VALUE_PORT_NAME)) {
                        if (p.hasTokenInside(0)) {
                            ret_val = ((DoubleToken)(p.getInside(0))).doubleValue();
                        } else {
                            throw new IllegalActionException(
                                    getContainer(),
                                    "Cannot read token from "
                                            + INTERMEDIATE_VALUE_PORT_NAME
                                            + ". Make sure a token is produced at each output of the inside model in CompositeOptimizer.");
                        }
                    } else if (p.getName().equals(CONSTRAINTS_PORT_NAME)) {
                        if (p.hasTokenInside(0)) {
                            Token[] gx_token = ((ArrayToken)(p.getInside(0))).arrayValue();
                            for (int i = 0; i < gx_token.length; i++) {
                                gx[i] = ((DoubleToken) gx_token[i]).doubleValue();
                            }
                        } else {
                            throw new IllegalActionException(
                                    getContainer(),
                                    "Cannot read token from "
                                            + CONSTRAINTS_PORT_NAME
                                            + ". Make sure a token is produced at each output of the inside model in CompositeOptimizer.");
                        }
                    } else if (p.getName().equals(GRADIENT_CONSTRAINTS_PORT_NAME)) {
                        if (p.hasTokenInside(0)) {
                            Token[] dgx_token = ((ArrayToken)(p.getInside(0))).arrayValue();
                            if(dgx!=null) {
                                for (int i = 0; i < dgx_token.length; i++) {
                                    double[][] dgix = ((DoubleMatrixToken)dgx_token[i]).doubleMatrix(); //This matrix should be row matrix (1 X N).
                                    for(int j=0; j < dgix[0].length; j++) {
                                        dgx[i][j] = dgix[0][j];
                                    }
                                }
                            }
                        } else if(dgx!=null) {
                            throw new IllegalActionException(
                                    getContainer(),
                                    "Cannot read token from "
                                            + GRADIENT_CONSTRAINTS_PORT_NAME
                                            + ". Make sure a token is produced at each output of the inside model in CompositeOptimizerUsingGradient.");
                        }
                    } else if (p.getName().equals(GRADIENT_VALUE_PORT_NAME)) {
                        if (p.hasTokenInside(0)) {
                            DoubleMatrixToken dfx_token = ((DoubleMatrixToken)(p.getInside(0)));
                            double[][] dfx_matrix = dfx_token.doubleMatrix(); //This matrix should be row matrix (1 X N).
                            if(dfx!=null) {
                                for (int i = 0; i < dfx_matrix[0].length; i++) {
                                    dfx[i] = dfx_matrix[0][i];
                                }
                            }
                        } else if(dfx!=null) {
                            throw new IllegalActionException(
                                    getContainer(),
                                    "Cannot read token from "
                                            + GRADIENT_VALUE_PORT_NAME
                                            + ". Make sure a token is produced at each output of the inside model in CompositeOptimizerUsingGradient.");
                        }
                    }
                }
                return ret_val;
            }
        }
        @Override
        public void fire() throws IllegalActionException {
            Calcfc calcfc = new Calcfc() {
                @Override
                public double Compute(int n, int m, double[] x, double[] con,
                        boolean[] terminate) throws IllegalActionException {
                    double evalX = oneStepIteration(x, con, null, null);
                    // if stop has been requested, the inside might not have produced tokens. so do not proceed
                    if (_stopRequested) {
                        terminate[0] = _stopRequested;
                        // set one constraint value to negative, so that this iteration is not considered by FindMinimum()
                        con[0] = -1;
                        return evalX;
                    } else {
                        if (_mode == MAXIMIZE) {
                            evalX = -1.0 * evalX; // minimize -f(x) = maximize f(x)
                        }
                        // if stop() has been called, this method will not be called again.
                        terminate[0] = _stopRequested;
                    }
                    return evalX;
                }
            };
            
            boolean need_to_initialize = false;
            if(_reusePreviousResult) {
                if(_firstStep||(_optInput==null)||(_optInput.length!=_dimension)) {
                    _optInput = new double[_dimension];
                    _firstStep = false; //Keeping the optimized values for next step.
                    need_to_initialize = true;
                }
            } else {
                _optInput = new double[_dimension];
                need_to_initialize = true;
            }
            // nConstraints is 1 because we transfer the task of computing constraints to an inside actor.
            _firstIteration = true;
            boolean[] terminateArray = new boolean[1];
            terminateArray[0] = _stopRequested;
            //////////////////////////////////////////////////////
            // Objective function
            if(need_to_initialize) {
                calc_func = new ObjectiveFunction(_dimension, _numConstraints) {
                    @Override
                    public boolean calcFunc(double[] x) {
                        try {
                            f0_result = oneStepIteration(x, fi_results, f0_gradient, fi_gradients);
                            //constraints must be minus value (g(x) < 0)
                            for(int i=0; i<fi_results.length; i++) {
                                fi_results[i] = -fi_results[i];
                                for(int j=0; j<fi_gradients[i].length; j++) {
                                    fi_gradients[i][j] = -fi_gradients[i][j];
                                }
                            }
                            if (_mode == MAXIMIZE) {
                               // minimize -f(x) = maximize f(x)
                                f0_result = -f0_result;
                                for(int i=0; i<f0_gradient.length; i++) {
                                    f0_gradient[i] = -f0_gradient[i];
                                }
                            }
                            if(_stopRequested) return false;
                        } catch (IllegalActionException iae) {
                            iae.printStackTrace();
                        }
                        return true;
                    }
                };
            }

            // optimization
            BarrierMethod opt = new BarrierMethod();
            opt.setTolerance(_rhoend);
            opt.setMaxIterationNum(_maxEvaluations);
            
            if(!_useGradient) {
                int nVariables = _dimension;
                CobylaExitStatus status = Cobyla.FindMinimum(calcfc, nVariables,
                        _numConstraints, _optInput, _rhobeg, _rhoend, iprint,
                        _maxEvaluations, terminateArray);
                // this is a post-result warning.
                switch (status) {
                case Normal:
                    break;
                case MaxIterationsReached:
                    //throw new IllegalActionException(CompositeOptimizer.this, "Optimizer terminated prematurely " +
                    //                "because maximum number of iterations limit has been reached. Perhaps increase maxEvaluations?");
                    break;
                case DivergingRoundingErrors:
                    //throw new IllegalActionException(CompositeOptimizer.this, "Optimizer terminated prematurely because " +
                    //                "rounding errors are becoming damaging");
                    break;
                case TerminateRequested:
                    //throw new IllegalActionException(CompositeOptimizer.this, "Optimizer terminated upon " +
                    //        "request. The results may not be reliable!");
                    break;

                }
            } else {
                int returnCode = opt.optimize(calc_func);
                for(int i=0; i<_optInput.length; i++) {
                    _optInput[i] = calc_func.current_point[i];
                }
                // TODO: if returnCode!= normal, throw exception
            }
//            System.out.println("itration "+iteration_counter+" steps.");
            //////////////////////////////////////////////////////

            _firstIteration = true;

            DoubleToken[] outTokens = new DoubleToken[_dimension];

            for (int i = 0; i < outTokens.length; i++) {
                outTokens[i] = new DoubleToken(_optInput[i]);
            }
            ArrayToken outputArrayToken = new ArrayToken(outTokens);
            ((TypedIOPort) ((CompositeOptimizerUsingGradient) getContainer())
                    .getPort(OPTIMAL_VALUE_PORT_NAME))
                    .send(0, outputArrayToken);
        }
        private ObjectiveFunction calc_func;
        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method reads tokens from the outside port if any, and writes to
         *  the input port. if no tokens available outside, the last received value
         *  is transferred.
         *  @param port The port to transfer tokens from.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @return True if at least one data token is transferred.
         */
        @Override
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;

            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (port.isKnown(i)) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);

                            if (_debugging) {
                                _debug(getName(), "transferring input from "
                                        + port.getName());
                            }
                            // save token
                            _tokenMap.put(port, t);
                            port.sendInside(i, _tokenMap.get(port));
                            result = true;
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }

            return result;
        }

        /** Transfer data previously read from the outside ports to the inside ports
         *  do not re-read values from outside ports until the firing is complete.
         *  @param port The port to transfer tokens from.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @return True if at least one data token is transferred.
         */
        private boolean _retransferInputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;

            if (_tokenMap.get(port) != null) {
                port.sendInside(0, _tokenMap.get(port));
                result = true;
            }

            return result;
        }

    }

    /** Beginning rho. Optimization step size.*/
    private double _rhobeg;
    /** Ending rho. Optimization step size.  */
    private double _rhoend;
    private int _mode;
    private int _dimension;
    private int _numConstraints;

    // Array that holds the argument of the objective function
    private double[] _optInput;

    /**Maximum number of function calls per optimization step
     */
    private int _maxEvaluations = 10000;
    private int iprint = 0;

    private final static int MAXIMIZE = 1;
    private final static int MINIMIZE = 0;

    private final static String OPTIMAL_VALUE_PORT_NAME = "optimalValue";
    private final static String INTERMEDIATE_VALUE_PORT_NAME = "f(x)";
    private final static String CONSTRAINTS_PORT_NAME = "g(x)";
    private final static String OPTIMIZATION_VARIABLE_NAME = "x";
    private final static String GRADIENT_CONSTRAINTS_PORT_NAME = "dg(x)";
    private final static String GRADIENT_VALUE_PORT_NAME = "df(x)";

    /** Hold the last received value at outside port to transfer to the inside composite multiple times */
    private HashMap<IOPort, Token> _tokenMap;

    private boolean _firstIteration;
    private boolean _firstStep;
    private boolean _reusePreviousResult;
    private boolean _useGradient;
}

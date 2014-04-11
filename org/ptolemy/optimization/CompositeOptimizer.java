/* An actor that optimizes a function defined by the inner composite
 * as an sdf model. 

 Copyright (c) 2004-2013 The Regents of the University of California.
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

import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.hoc.MirrorComposite;
import ptolemy.actor.lib.hoc.MirrorPort; 
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ArrayToken;
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
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
///////////////////////////////////////////////////////////////////
////CompositeOptimizer

/**
This actor implements a composite optimizer that optimizes a function 
provided as an sdf model in the inner composite.
To use the composite optimizer, construct an SDF model on the inside
that operates on the x input to output two values: (i) an intermediate
result, that is a function of x and possibly other inputs and (ii) a
double array containing the values of the constraints, calculated for
the specific x input.
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
Current implementation uses Cobyla as the solver. Cobyla implements
the trust-region-reflective algorithm and performs a type of gradient
descent optimization, ideal for objective functions that are non-convex
and/or with unknown gradient.

@author Ilge Akkaya
@version
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating  
*/
public class CompositeOptimizer extends MirrorComposite {


    public CompositeOptimizer(Workspace workspace) 
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init(); 
    }

    public CompositeOptimizer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init(); 
    }


    /**
     * The optimal value of x.
     */
    public OptimizerPort optimalValue;
    /**
     * Trigger that starts optimization routine.
     */
    public OptimizerPort trigger;

    /**
     * The output port that provides the evaluated constraint values at each
     * evaluation of the objective function
     */
    public static OptimizerPort constraints;
    /**
     * The optimization variable. 
     */
    public static OptimizerPort x;
    /** 
     * Value of the objective function f(x) for a given value of x
     */
    public static OptimizerPort intermediateValue;

    /**
     * The expert parameter that denotes the beginning step-size
     */
    public Parameter rhoBeg;
    /**
     * The expert parameter that denotes the final step-size
     */
    public Parameter rhoEnd;
    /**
     * Maximum number of function evaluations per iteration
     */
    public Parameter maxEvaluations;
    /**
     * Optimization mode. ( min or max)
     */
    public Parameter mode; 
    /**
     * Time horizon over which f(x) is optimized. Not implemented at the moment.
     */
    public Parameter timeHorizon;
    /**
     * The dimension of optimization space. Also equal to the length of the optimization variable array, x.
     */
    public Parameter dimensionOfOptimizationSpace;
    /**
     * Number of constraints checked at each evaluation. The expected type of constraints is a double array with length <i>numberOfConstraints</i>.
     */
    public Parameter numberOfConstraints;


    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if( attribute == rhoBeg){
            double rho = ((DoubleToken)rhoBeg.getToken()).doubleValue();
            _rhobeg = rho; 
        }else if( attribute == rhoEnd){
            double rho = ((DoubleToken)rhoEnd.getToken()).doubleValue();
            _rhoend = rho; 
        }else if (attribute == timeHorizon){
            int th = ((IntToken)timeHorizon.getToken()).intValue();
            if(th > 0){
                _timeHorizon = th;
            }else{
                throw new IllegalActionException(this,"Time horizon must be a positive integer");
            }
        }else if(attribute == dimensionOfOptimizationSpace){
            _dimension = ((IntToken)dimensionOfOptimizationSpace.getToken()).intValue();
        }else if(attribute == mode)
        {
            String modeStr = ((StringParameter)mode).stringValue();
            if(modeStr.equalsIgnoreCase("MAX")){
                _mode = MAXIMIZE;
            }else if(modeStr.equalsIgnoreCase("MIN")){
                _mode = MINIMIZE;
            }
        }else if( attribute == maxEvaluations){
            _maxEvaluations = ((IntToken) maxEvaluations.getToken()).intValue();
        }else if( attribute == numberOfConstraints){
            _numConstraints = ((IntToken) numberOfConstraints.getToken()).intValue();
        }else{
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. This overrides
     *  the base class to instantiate a new IterateDirector and to set
     *  the association with iterationCount.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CompositeOptimizer result = (CompositeOptimizer) super.clone(workspace);
        try {
            // Remove the old inner OptimzizerDirector(s) that is(are) in the wrong workspace.
            String directorName = null;
            Iterator directors = result.attributeList(
                    OptimizerDirector.class).iterator();
            while (directors.hasNext()) {
                OptimizerDirector oldDirector = (OptimizerDirector) directors.next();
                if (directorName == null) {
                    directorName = oldDirector.getName();
                }
                oldDirector.setContainer(null);
            }

            // Create a new OptimizerDirector in the right workspace.
            OptimizerDirector director = result.new OptimizerDirector(
                    workspace);
            director.setContainer(result);
            director.setName(directorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        result._tokenMap = new HashMap<IOPort, Token>();
        return result;
    }

    //    /** Add a port to this actor. This overrides the base class to
    //     *  mirror the new port in the contained actor, if there is one,
    //     *  and to establish a connection to a port on the contained actor.
    //     *  @param port The TypedIOPort to add to this actor.
    //     *  @exception IllegalActionException If the port is not an instance
    //     *   of IteratePort, or the port has no name.
    //     *  @exception NameDuplicationException If the port name collides with a
    //     *   name already in the actor.
    //     */
    //    @Override
    //    protected void _addPort(Port port) throws IllegalActionException,
    //    NameDuplicationException {
    //        super._addPort(port); 
    //
    //
    //        // remove the optimal value port to the inside composite
    //        if( port.getName().equals(OPTIMAL_VALUE_PORT_NAME) ||
    //                port.getName().equals("trigger")){
    //            // NOTE: Do not use MoML here because we do not want to generate
    //            // undo actions to recreate the inside relation and port.
    //            // This is because _addPort() will take care of that.
    //            // The cast is safe because all my ports are instances of IOPort.
    //            Iterator relations = ((IOPort) port).insideRelationList().iterator();
    //
    //            while (relations.hasNext()) {
    //                ComponentRelation relation = (ComponentRelation) relations.next();
    //
    //                try {
    //                    relation.setContainer(null);
    //                } catch (KernelException ex) {
    //                    throw new InternalErrorException(ex);
    //                }
    //            }
    //
    //            // Remove the ports from the inside entity only if this
    //            // is not being called as a side effect of calling _removeEntity().
    //            //if (super._inRemoveEntity) {
    //            //    return;
    //            //}
    //
    //            Iterator entities = entityList().iterator();
    //
    //            while (entities.hasNext()) {
    //                Entity insideEntity = (Entity) entities.next();
    //                Port insidePort = insideEntity.getPort(port.getName());
    //
    //                if (insidePort != null) {
    //                    try {
    //                        insidePort.setContainer(null);
    //                    } catch (KernelException ex) {
    //                        throw new InternalErrorException(ex);
    //                    }
    //                }
    //            }
    //        }
    //
    //    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        setClassName("org.ptolemy.Optimization.CompositeOptimizer");
        OptimizerDirector director = new OptimizerDirector(workspace());
        director.setContainer(this);
        director.setName(uniqueName("OptimizerDirector"));

        optimalValue = new OptimizerPort(this, OPTIMAL_VALUE_PORT_NAME);
        optimalValue.setOutput(true);
        optimalValue.setDisplayName(OPTIMAL_VALUE_PORT_NAME);
        optimalValue.setTypeEquals(new ArrayType(BaseType.DOUBLE));


        trigger = new OptimizerPort(this, "trigger");
        trigger.setMultiport(true);
        trigger.setInput(true);
        SingletonParameter showName = (SingletonParameter)trigger.getAttribute("_showName");
        if(showName == null){
            showName = new SingletonParameter(trigger,"_showName");
            showName.setToken("true");
        }else{
            showName.setToken("true");
        }
        //optimalValue.setTypeEquals(new ArrayType(BaseType.DOUBLE));


        // the following ports will be hidden at the top level hierarchy.
        intermediateValue = new OptimizerPort(this, INTERMEDIATE_VALUE_PORT_NAME);
        intermediateValue.setTypeEquals(BaseType.DOUBLE);
        intermediateValue.setOutput(true);
        SingletonParameter hidden = (SingletonParameter)intermediateValue.getAttribute("_hide");
        if(hidden == null){
            hidden = new SingletonParameter(intermediateValue,"_hide");
            hidden.setToken("true");
        }else{
            hidden.setToken("true");
        }



        constraints = new OptimizerPort(this, CONSTRAINTS_PORT_NAME);
        constraints.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        constraints.setOutput(true);
        hidden = (SingletonParameter)constraints.getAttribute("_hide");
        if(hidden == null){
            hidden = new SingletonParameter(constraints,"_hide");
            hidden.setToken("true");
        }else{
            hidden.setToken("true");
        }

        x = new OptimizerPort(this, OPTIMIZATION_VARIABLE_NAME);
        x.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        x.setInput(true);
        hidden = (SingletonParameter)x.getAttribute("_hide");
        if(hidden == null){
            hidden = new SingletonParameter(x,"_hide");
            hidden.setToken("true");
        }else{
            hidden.setToken("true");
        }




        mode = new StringParameter(this, "mode");
        mode.setExpression("MIN");
        mode.addChoice("MIN");
        mode.addChoice("MAX");

        maxEvaluations = new Parameter(this, "maxEvaluations");
        maxEvaluations.setExpression("100000");
        maxEvaluations.setTypeEquals(BaseType.INT);

        dimensionOfOptimizationSpace = new Parameter(this, "dimensionOfOptimizationSpace");
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

        timeHorizon = new Parameter(this,"timeHorizon");
        timeHorizon.setExpression("1");
        //timeHorizon.setTypeEquals(BaseType.INT);

        //x.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        _tokenMap = new HashMap<IOPort, Token>();
        _firstIteration =true;

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
    MirrorComposite.MirrorCompositeContents {
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

        /** Override the base class to return a specialized port.
         *  @param name The name of the port to create.
         *  @return A new instance of IteratePort, an inner class.
         *  @exception NameDuplicationException If the container already has
         *  a port with this name.
         */
        public Port newPort(String name) throws NameDuplicationException {
            try {
                return new OptimizerPort(this, name);
            } catch (IllegalActionException ex) {
                // This exception should not occur, so we throw a runtime
                // exception.
                throw new InternalErrorException(this, ex, null);
            }
        }

        private void _init() throws IllegalActionException, NameDuplicationException{

            OptimizerPort intermediate = new OptimizerPort(this, INTERMEDIATE_VALUE_PORT_NAME);
            intermediate.setTypeEquals(BaseType.DOUBLE);
            intermediate.setOutput(true);  

            OptimizerPort insideConstraints = new OptimizerPort(this, CONSTRAINTS_PORT_NAME);
            insideConstraints.setTypeEquals(new ArrayType(BaseType.DOUBLE));
            insideConstraints.setOutput(true); 

            OptimizerPort xIn = new OptimizerPort(this, OPTIMIZATION_VARIABLE_NAME);
            xIn.setTypeEquals(new ArrayType(BaseType.DOUBLE));
            xIn.setInput(true); 

        }
    }
    public class OptimizerDirector extends Director {


        public OptimizerDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);   
        }




        @Override
        public void fire() throws IllegalActionException{


            Calcfc calcfc = new Calcfc() {
                @Override
                public double Compute(int n, int m, double[] x, double[] con, boolean[] terminate) throws IllegalActionException {
                    double evalX = 0;
                    DoubleToken[] xTokens = new DoubleToken[x.length];
                    for(int i = 0; i < xTokens.length; i++){
                        xTokens[i] = new DoubleToken(x[i]);
                    }  
                    // convert x into an array token
                    ArrayToken xAsToken = new ArrayToken(xTokens);
                    // send new x value to the x port of the inside port for the new iteration
                    // maybe we need not override transferinputs at all? 
                    CompositeOptimizer.this.x.sendInside(0, xAsToken);

                    // before firing the inside composite, make sure transferInputs are called
                    CompositeActor container = (CompositeActor) getContainer();
                    if(_firstIteration){
                        _firstIteration = false;
                    }else{
                        // transferInputs has already been called
                        Iterator<IOPort> inports = container.inputPortList().iterator(); 
                        while( inports.hasNext()){
                            IOPort p = inports.next();
                            if(!(p instanceof ParameterPort)){
                                _retransferInputs(p);
                            }
                        }
                    }

                    // fire the inside composite.
                    OptimizerDirector.super.fire(); 
                    // if stop has been requested, the inside might not have produced tokens. so do not proceed
                    if(_stopRequested){
                        terminate[0] = _stopRequested;
                        // set one constraint value to negative, so that this iteration is not considered by FindMinimum()
                        con[0] = -1;
                        return evalX;
                    }else{ 
                        Iterator<IOPort> outports = container.outputPortList().iterator(); 
    
                        while(outports.hasNext()){
                            IOPort p = outports.next();
                            // these we don't need. will find a way to do this cleanly. perhaps don't even link the ports. which should already be the case really.
    
                            if(p.getName().equals(INTERMEDIATE_VALUE_PORT_NAME)){
                                if (p.hasTokenInside(0)) {
                                    Token t = p.getInside(0); 
                                    evalX = ((DoubleToken)t).doubleValue();
                                }else{
                                    throw new IllegalActionException(CompositeOptimizer.this, "Cannot read token from " + INTERMEDIATE_VALUE_PORT_NAME + 
                                            ". Make sure a token is produced at each output of the inside model in CompositeOptimizer.");
                                }
                            }else if(p.getName().equals(CONSTRAINTS_PORT_NAME)){
                                if (p.hasTokenInside(0)) {
                                    Token t = p.getInside(0);
                                    Token[] constraintArray = ((ArrayToken)t).arrayValue();
                                    for(int i = 0 ; i < constraintArray.length; i++){
                                        con[i] = ((DoubleToken)constraintArray[i]).doubleValue();
                                    }
                                }else{
                                    throw new IllegalActionException(CompositeOptimizer.this, "Cannot read token from " + CONSTRAINTS_PORT_NAME + 
                                            ". Make sure a token is produced at each output of the inside model in CompositeOptimizer.");
                                }
                            } 
                        }  
                        if(_mode == MAXIMIZE){
                            evalX = -1.0*evalX; // minimize -f(x) = maximize f(x)
                        }else{
                            //do nothing
                        } 
                        
                        // if stop() has been called, this method will not be called again.
                        terminate[0] = _stopRequested;
                    }

                    return evalX;
                }
            };


            _optInput = new double[_dimension]; 
            int nVariables   = _dimension;

            // nConstraints is 1 because we transfer the task of computing constraints to an inside actor.
            _firstIteration = true;
            boolean[] terminateArray = new boolean[1];
            terminateArray[0] =_stopRequested;
            CobylaExitStatus status = Cobyla.FindMinimum(calcfc, nVariables, _numConstraints, _optInput, _rhobeg, _rhoend, iprint, _maxEvaluations, terminateArray);
             
            _firstIteration = true; 

         // TODO: if status!= normal, throw exception
            DoubleToken[] outTokens = new DoubleToken[_dimension];

            for(int i = 0; i < outTokens.length; i++){
                outTokens[i] = new DoubleToken(_optInput[i]);
            }
            ArrayToken outputArrayToken = new ArrayToken(outTokens);
            optimalValue.send(0, outputArrayToken);
            
            // this is a post-result warning.
            switch (status)
            {
                case Normal:  
                    break;
                case MaxIterationsReached:
                    throw new IllegalActionException(CompositeOptimizer.this, "Optimizer terminated prematurely " +
                    		"because maximum number of iterations limit has been reached. Perhaps increase maxEvaluations?"); 
                case DivergingRoundingErrors:
                    throw new IllegalActionException(CompositeOptimizer.this, "Optimizer terminated prematurely because " +
                    		"rounding errors are becoming damaging");  
                case TerminateRequested:
                    throw new IllegalActionException(CompositeOptimizer.this, "Optimizer terminated upon " +
                            "request. The results may not be reliable!");  
                
            }
            



        }

        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method reads tokens from the outside port if any, and writes to
         *  the input port. if no tokens available outside, the last received value
         *  is transferred.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         */
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
         *  @exception IllegalActionException Not thrown in this base class.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         */
        private boolean _retransferInputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;

            if(_tokenMap.get(port)!=null){
                port.sendInside(0, _tokenMap.get(port));
                result = true;
            }

            return result;
        }



    }

    public static class OptimizerPort extends MirrorPort {
        /** Construct a port in the specified workspace with an empty
         *  string as a name. You can then change the name with setName().
         *  If the workspace argument
         *  is null, then use the default workspace.
         *  The object is added to the workspace directory.
         *  Increment the version number of the workspace.
         *  @param workspace The workspace that will list the port.
         * @throws IllegalActionException If port parameters cannot be initialized.
         */
        public OptimizerPort(Workspace workspace) throws IllegalActionException {
            // This constructor is needed for Shallow codgen.
            super(workspace);
        }

        // NOTE: This class has to be static because otherwise the
        // constructor has an extra argument (the first argument,
        // actually) that is an instance of the enclosing class.
        // The MoML parser cannot know what the instance of the
        // enclosing class is, so it would not be able to instantiate
        // these ports.

        /** Create a new instance of a port for IterateOverArray.
         *  @param container The container for the port.
         *  @param name The name of the port.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @exception NameDuplicationException Not thrown in this base class.
         */
        public OptimizerPort(TypedCompositeActor container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            // NOTE: Ideally, Port are created when an entity is added.
            // However, there appears to be no clean way to do this.
            // Instead, ports are added when an entity is added via a
            // change request registered with this IterateOverArray actor.
            // Consequently, these ports have to be persistent, and this
            // constructor and class have to be public.
            // setPersistent(false);
        } 
    }

    /** Beginning rho. Optimization step size.*/
    private double _rhobeg;  
    /** Ending rho. Optimization step size.  */
    private double _rhoend; 
    // TODO : implement time horizon, maybe?
    private int _timeHorizon;
    private int _mode;
    private int _dimension;
    private int _numConstraints;

    // Array that holds the argument of the objective function
    private double[] _optInput;

    /**Maximum number of function calls per optimization step
     */
    private int _maxEvaluations = 10000;
    private int iprint = 0;

    private final int MAXIMIZE = 1;
    private final int MINIMIZE = 0;

    private final String OPTIMAL_VALUE_PORT_NAME = "optimalValue";
    private final static String INTERMEDIATE_VALUE_PORT_NAME = "intermediateValue";
    private final static String CONSTRAINTS_PORT_NAME = "constraints";
    private final static String OPTIMIZATION_VARIABLE_NAME = "x";

    /** Flag indicating that we are executing _addPort(). */
    private boolean _inAddPort = false;

    /** Hold the last received value at outside port to transfer to the inside composite multiple times */
    private HashMap<IOPort, Token> _tokenMap ;

    private boolean _firstIteration;



}

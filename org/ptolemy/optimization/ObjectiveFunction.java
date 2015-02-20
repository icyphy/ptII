package org.ptolemy.optimization;

import ptolemy.math.DoubleMatrixMath;

/**
 * Definition of the objective function and constraint functions.
 */
abstract class ObjectiveFunction {
    /**
     * Constructor of ObjectiveFunction.
     * In the constructor, double arrays for fi_results, gradients, 
     * and hessians are allocated.
     */
    public ObjectiveFunction(int dimensionX, int numOfConstraints) {
        fiResults = new double[numOfConstraints];
        f0Gradient = new double[dimensionX];
        _f0GradientPrevious = new double[dimensionX];
        fiGradients = new double[numOfConstraints][dimensionX];
        _fiGradientsPrevious = new double[numOfConstraints][dimensionX];
        f0Hessian = new double[dimensionX][dimensionX];
        fiHessians = new double[numOfConstraints][dimensionX][dimensionX];
        currentX = new double[dimensionX];
        _previousX = new double[dimensionX];
        _fiRelax = new double[numOfConstraints];
        iterationCounter = 0;
        stopRequested = false;

        for(int i=0; i<dimensionX; i++) {
            currentX[i] = 0.0;
        }
        for(int i=0; i<dimensionX; i++) {
            f0Hessian[i][i] = 1;
        }

        for(int i=0; i<numOfConstraints; i++) {
            for(int it=0; it<dimensionX; it++) {
                fiHessians[i][it][it] = 1;
            }
        }
    }

    /**
     * Add a constant value to the specified constraint function.
     * @param id : index of a constraint function which is added the value.
     * @param val : adding value
     */
    public void addConstraints(int id, double val) {
        _fiRelax[id] += val;
    }

    /**
     * Objective function and constraint functions.
     * In this function, all results(f0_result, fi_results) 
     * and gradients(f0_gradient, fi_gradients) must be updated.
     * @param x : input variables
     * @return If calculation was terminated by user input, return "false".
     */
    abstract public boolean calcFunction(double[] x);

    /**
     * Objective function called by a solver.
     * @param x : input variables
     */
    public void calcFuncInternal(double[] x) {
        if(stopRequested) return;
        for(int i=0; i<fiGradients.length; i++) {
            for(int it=0; it<fiGradients[i].length; it++) {
                _fiGradientsPrevious[i][it] = fiGradients[i][it];
            }
        }
        for(int i=0; i<f0Gradient.length; i++) {
            _f0GradientPrevious[i] = f0Gradient[i];
            _previousX[i] = currentX[i];
            currentX[i] = x[i];
        }

        if(!calcFunction(x)) stopRequested = true;
        for(int i=0; i<fiResults.length; i++) {
            fiResults[i] += _fiRelax[i];
        }
        
        if(iterationCounter==0) {
            //Copy all previous values 
            for(int i=0; i<fiGradients.length; i++) {
                for(int it=0; it<fiGradients[i].length; it++) {
                    _fiGradientsPrevious[i][it] = fiGradients[i][it];
                }
            }
            for(int i=0; i<f0Gradient.length; i++) {
                _f0GradientPrevious[i] = f0Gradient[i];
                _previousX[i] = currentX[i];
            }
        }
        updateHessian();
        iterationCounter++;
    }
    /**
     * get current point
     * @return : current point X
     */
    public double[] getCurrentPoint() {
        double[] ret = new double[currentX.length];
        for(int i=0; i<ret.length; i++) {
            ret[i] = currentX[i];
        }
        return ret;
    }
    /**
     * Reset all constant values which are added to constraint functions.
     */
    public void resetConstraints() {
        for(int i=0; i<_fiRelax.length; i++) {
            _fiRelax[i] = 0;
        }
    }

    /*
     * Public Variables 
     */
    public double f0Result;            //Results of objective function.
    public double[] fiResults;   //Results of constraint functions. An array length is m, the number of constraints. 
    public double[] f0Gradient;        //Gradient of result of f0. An array length is n, the number of variables of objective function.
    public double[][] fiGradients;     //Gradients of constraints. The number of col is n.
    public double[][] f0Hessian;       //Hessian of f0. Lengths of array is [n][n]
    public double[][][] fiHessians;    //Hessians of constraints. Lengths of array is [m][n][n].
    public double[] currentX;
    public int iterationCounter;
    public boolean stopRequested;


    /*
     * Protected method
     */
    /**
     * Clear Matrix
     * @param matrix : matrix which is to be zero-matrix.
     */
    protected void clearMatrix(double[][] matrix) {
        for(int row=0; row<matrix.length; row++) {
            for(int col=0; col<matrix[row].length; col++) {
                matrix[row][col] = 0;
            }
        }
    }
    
    /*
     * Private Methods
     */
    private void printVector(double[] array, String label) {
        System.out.print(label);
        for(int col=0; col<array.length; col++) {
            System.out.print(array[col]+ "\t");
        }
        System.out.println();
    }
    /**
     * Update estimates of Hessian. 
     * This function is based on quasi-Newton method BFGS.
     */
    private boolean updateHessian() {
        // 
        //Hessian of f0 
        double[] dg0 = new double[f0Gradient.length];
        for(int it=0; it<dg0.length; it++) {
            dg0[it] = f0Gradient[it] - _f0GradientPrevious[it];
        }
        double[] dx = new double[_previousX.length];
        for(int it=0; it<dx.length; it++) {
            dx[it] = currentX[it] - _previousX[it];
        }
        boolean is_hessian_calculated = updateHessianByBFGS(f0Hessian, dg0, dx);

        //Hessian of fi
        for(int i=0; i<fiHessians.length; i++) {
            double[] dgi = new double[fiGradients[i].length];
            for(int it=0; it<dgi.length; it++) {
                dgi[it] = fiGradients[i][it] - _fiGradientsPrevious[i][it];
            }
            is_hessian_calculated = is_hessian_calculated & updateHessianByBFGS(fiHessians[i], dgi, dx);
        }
        return is_hessian_calculated; //if at least one of the hessians are not calculated, return false.
    }
    
    /**
     * implementation of quasi-newton method(BFGS)
     * @param est_hessian_current : estimated hessian matrix which is updated in this function.
     * @param dg : change of gradient
     * @param dx : change of searching point X
     * @return : if updating finished successfully, return ture.
     */
    private boolean updateHessianByBFGS(double[][] est_hessian_current, double[] dg, double[] dx) {
        // Hk+1 = Hk + y*yT/(yT*s) - Hk*s(Hk*s)T/(sT*Hk*s)
        //   y = gk+1 - gk
        //   s = xk+1 - xk
        double dgTdx = 0;
        for(int i=0; i<dx.length; i++) {
            dgTdx += dx[i]*dg[i];
        }
        if(dgTdx<-1.0E-15) {
            //If hessian is not positive-definite, hessian must converge to zero-matrix.
            for(int row=0; row<est_hessian_current.length; row++) {
                for(int col=0; col<est_hessian_current[0].length; col++) {
                    est_hessian_current[row][col] *= 0.1;
                }
            }
        }
        if(dgTdx<1.0E-10) {
            return false;
        }
        double[][] dgdgT_dgTdx= new double[dx.length][dx.length];
        for(int i=0; i<dgdgT_dgTdx.length; i++) {
            for(int j=0; j<dgdgT_dgTdx[0].length; j++) {
                dgdgT_dgTdx[i][j] = dg[i]*dg[j]/dgTdx;
            }
        }
        double[] Hdx = DoubleMatrixMath.multiply(dx, est_hessian_current);
        double dxTHdx = 0;
        for(int i=0; i<dx.length; i++) {
            dxTHdx += dx[i]*Hdx[i];
        }
        if(Math.abs(dxTHdx)<1.0E-10) return false;
        double[][] HdxHdxT_dxTHdx = new double[dx.length][dx.length];
        for(int i=0; i<HdxHdxT_dxTHdx.length; i++) {
            for(int j=0; j<HdxHdxT_dxTHdx[0].length; j++) {
                HdxHdxT_dxTHdx[i][j] = Hdx[i]*Hdx[j]/dxTHdx;
            }
        }
        for(int i=0; i<est_hessian_current.length; i++) {
            for(int j=0; j<est_hessian_current[0].length; j++) {
                est_hessian_current[i][j] = est_hessian_current[i][j] + dgdgT_dgTdx[i][j] - HdxHdxT_dxTHdx[i][j];
            }
        }
        return true;
    }

    private double[] _f0GradientPrevious; //The previous value of f0Gradient
    private double[][] _fiGradientsPrevious; //The previous value of fiGradients
    private double[] _previousX;        //The previous value of currentX
    private double[] _fiRelax; //If there is no feasible point, constraints are relaxed using this value. Default value is 0.
}

/**
 * The objective function class which is used in Phase 1 of the interior point method.
 * In the interior point method, a starting point X must be a feasible point that satisfies all 
 * constraints. To find the feasible point, the interior point method calculate Phase 1 
 * using this class.
 * @author shuhei emoto
 */
class ObjectiveFunctionForPhaseI extends ObjectiveFunction{
    public ObjectiveFunctionForPhaseI(ObjectiveFunction a_source) {
        super(a_source.currentX.length+1, a_source.fiResults.length);
        _source = a_source;
        
        for(int i=0; i<_source.currentX.length; i++) {
            currentX[i] = _source.currentX[i];
        }
        currentX[currentX.length-1] = 0;
    }
    
    @Override
    public void addConstraints(int id, double val) {
        _source.addConstraints(id, val);
    }
    
    @Override
    public boolean calcFunction(double[] x) {
        return false;
    }
    /**
     * Objective function called in Phase I. (Searching for feasible initial point)
     * @param x : input variables
     * @param s : extended input variables
     */
    public void calcFuncInternal(double[] x) {
        setCurrentPoint(x);
        double[] input_x = new double[x.length-1];
        for(int i=0; i<input_x.length; i++) {
            input_x[i] = x[i];
        }
        _source.calcFuncInternal(input_x);
        //objective function: f(x,s) = s
        f0Result = x[x.length-1];
        for(int col=0; col<f0Gradient.length-1; col++) {
            f0Gradient[col] = 0;
        }
        f0Gradient[f0Gradient.length-1] = 1;
        for(int row=0; row<f0Hessian.length; row++) {
            for(int col=0; col<f0Hessian[0].length; col++) {
                f0Hessian[row][col] = 0;
            }
        }
        // Inequality constraints: fi(x, s) = fi(x)-s
        for(int i=0; i<fiResults.length; i++) {
            fiResults[i] = _source.fiResults[i]-x[x.length-1];
            for(int it=0; it<_source.fiGradients[i].length; it++) {
                fiGradients[i][it] = _source.fiGradients[i][it];
            }
            fiGradients[i][fiGradients[i].length-1] = -1;
            clearMatrix(fiHessians[i]);
            for(int row=0; row<_source.fiHessians[i].length; row++) {
                for(int col=0; col<_source.fiHessians[i][row].length; col++) {
                    fiHessians[i][row][col] = _source.fiHessians[i][row][col];
                }
            }
        }
    }
    
    @Override
    public void resetConstraints() {
        _source.resetConstraints();
    }
    /**
     * set current searching point X
     * @param x : current point which is to be set.
     */
    public void setCurrentPoint(double[] x) {
        for(int i=0; i<currentX.length; i++) {
            currentX[i] = x[i];
            if(i<currentX.length-1) {
                _source.currentX[i] = x[i];
            }
        }
    }
    
    /*
     * Private variables
     */
    private ObjectiveFunction _source;
}

/**
 * The class of approximated objective function.
 * In this class, non-linear multivariate function is approximated 
 * using hessians and gradients.
 * F0(X+dx) = F0(X) + 1/2 * dxT*H0*dx + g0*dx
 * Fi(X+dx) = Fi(X) + gi*dx
 * @author shuhei emoto
 */
class ApproximatedObjectiveFunction extends ObjectiveFunction{
    public ApproximatedObjectiveFunction(ObjectiveFunction a_source) {
        super(a_source.currentX.length, a_source.fiResults.length);
        _source = a_source;
        
        for(int i=0; i<_source.currentX.length; i++) {
            currentX[i] = _source.currentX[i];
        }

        // Copy f0 Hessian and fi_gradients
        for(int row=0; row<f0Hessian.length; row++) {
            for(int col=0; col<f0Hessian[row].length; col++) {
                f0Hessian[row][col] = _source.f0Hessian[row][col];
            }
        }
        for(int i=0; i<fiResults.length; i++) {
            for(int it=0; it<fiGradients[i].length; it++) {
                fiGradients[i][it] = _source.fiGradients[i][it];
            }
            clearMatrix(fiHessians[i]); //fi_hessians are all zero.
        }
    }
    @Override
    public void addConstraints(int id, double val) {
        _source.addConstraints(id, val);
    }
    @Override
    public boolean calcFunction(double[] x) {
        return false;
    }
    /**
     * Objective function called in QPSolver.
     * @param x : input variables
     */
    @Override
    public void calcFuncInternal(double[] x) {
        double[] dx = new double[x.length];
        for(int i=0; i<x.length; i++) {
            currentX[i] = x[i];
            dx[i] = x[i]-_source.currentX[i];
        }
        
        /////////////////////////////////////////
        ///// result of objective function///////
        // f(x+dx) = f(x)+(1/2)(dxTHdx)+g*dx 
        double[] Qx = DoubleMatrixMath.multiply(dx, _source.f0Hessian);
        f0Result = _source.f0Result;
        for(int i=0; i<Qx.length; i++) {
            f0Result += (0.5*Qx[i]*dx[i] + _source.f0Gradient[i]*dx[i]);
        }
        // df(x+dx) = df(x)+H*dx
        for(int col=0; col<f0Gradient.length; col++) {
            f0Gradient[col] = _source.f0Gradient[col]+Qx[col];
        }
        // Hessian(x+dx) = Hessian(x) was already copied in the constructor.
        
        ///////////////////////////////////////////////////
        ////// Inequality constraints: 
        // fi(x+dx) = fi(x)+g*dx
        for(int i=0; i<fiResults.length; i++) {
            fiResults[i] = _source.fiResults[i];
            for(int col=0; col<dx.length; col++) {
                fiResults[i] += _source.fiGradients[i][col]*dx[col];
            }
        }
        // dfi(x+dx) = dfi(x) were already copied in the constructor.
        // Hessian_i(x+dx) = 0 were initialized in the constructor.
    }
    @Override
    public void resetConstraints() {
        _source.resetConstraints();
    }
    
    /*
     * Private variables
     */
    private ObjectiveFunction _source;
}
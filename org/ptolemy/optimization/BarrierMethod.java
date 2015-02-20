package org.ptolemy.optimization;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.DoubleMatrixMath;

/**
 * BarrierMethod.
 * Convex Optimization P.583, 11.3.The Barrier method
 * This class solves min(BarrierFunction B = f0(x)-(1/t)sum(log(-fi(x)))) using Newton decrement.
 */
public class BarrierMethod  {
    private boolean flag_debug_print = false; 
    private static final double MIN_STEP_SIZE = 1.0E-20;
    public static final int CONVERGED = 0;
    public static final int FAILED_MAX_ITERATION_LIMIT = 1;
    public static final int FAILED_IMPOSSIBLE_TO_REMAIN_WITHIN_FEASIBLE = 2;
    public static final int TERMINATED_BY_USER = 3;
    private static final int INNER_LOOP_FINISH = -1;
    private double _tolerance = 1.0E-5;
    private int _maxIterationNum = 50;
    private double _alpha = 0.05;
    private double _beta = 0.5;
    private double _mu = 10.0;
    
    public void setTolerance(double a_tolerance) {
        _tolerance = a_tolerance;
    }
    public void setMaxIterationNum(int a_maxIterationNum) {
        _maxIterationNum = a_maxIterationNum;
    }
    public void setAlpha(double a_alpha) {
        _alpha = a_alpha;
    }
    public void setBeta(double a_beta) {
        _beta = a_beta;
    }
    public void setMu(double a_mu) {
        _mu = a_mu;
    }
    

    public int optimize(ObjectiveFunction objective) throws IllegalActionException {
        objective.resetConstraints();

        int return_val = FAILED_MAX_ITERATION_LIMIT;
        ////////////////////////////////////////////////////
        //Phase I: Search for a feasible initial point.
        objective.iterationCounter = 0;
        ObjectiveFunctionForPhaseI objective_ph1 = new ObjectiveFunctionForPhaseI(objective);
        //calculate at (X, 0)
        objective_ph1.calcFuncInternal(objective_ph1.currentX);
        //set initial s
        objective_ph1.currentX[objective_ph1.currentX.length-1] = maxValue(objective_ph1.fiResults)+_tolerance;
        //check X0 feasibility
        if(objective_ph1.currentX[objective_ph1.currentX.length-1] < 0){
            return_val = CONVERGED; //given starting point is already feasible
        } else {
            return_val = optimizationOfGivenPhase(objective_ph1, 1);
        }
        if(return_val != CONVERGED) relaxConstraints(objective_ph1, _tolerance); //relax the constraints of objective_ph and objective.
        if(flag_debug_print) {
            System.out.print("Phase1: "+objective.iterationCounter);
            if(return_val != CONVERGED) {
                System.out.print(" Terminated by criteria No."+return_val);
            }
            System.out.println();
        }
        /////////////////////////////////////////////////////

        /////////////////////////////////////////////////////
        //Phase II: optimize objective.current_point
        objective.iterationCounter = 0;
        return_val = optimizationOfGivenPhase(objective, 2);
        if(flag_debug_print) {
            System.out.print("Phase2: "+objective.iterationCounter);
            if(return_val != CONVERGED) {
                System.out.print(" Terminated by criteria No."+return_val);
            }
            System.out.println();
        }
        /////////////////////////////////////////////////////

        return return_val;
    }
    private int optimizationOfGivenPhase(ObjectiveFunction objective, int phase) throws IllegalActionException {
        //calculate at initial_point
        objective.calcFuncInternal(objective.currentX);
        
        double t = 1.0; //Parameter which defines the gap of constraints. t will increase through the iteration.
        if(phase == 1) t=100.0;
        for (int outer_it = 0; ; outer_it++) {
            if(outer_it>_maxIterationNum) {
                return FAILED_MAX_ITERATION_LIMIT;
            }

            // optimize at current t
            int ret_val = calcInnerLoop(objective, phase, t);
            if(ret_val != INNER_LOOP_FINISH) return ret_val;
            
            // increase t
            t *= _mu;
            //exit condition of outer loop
            if ((phase==2)&&(1.0/t < _tolerance)){
                return CONVERGED;
            }
        }
    }

    ////////////////////////////////////////////
    // private methods 
    //
    /**
     *  compute inner loop of Barrier method.
     * @param objective : data set of the cost function, constraints function, etc.
     * @param phase : flag of phase (phase 1 or 2)
     * @param t : the parameter (gap) of the barrier function.
     * @return return code
     */
    private int calcInnerLoop(ObjectiveFunction objective, int phase, double t) {
        for (int iteration = 0; ; iteration++) {
            if(objective.stopRequested) return TERMINATED_BY_USER;
            // iteration limit condition
            if (iteration == _maxIterationNum) {
                return FAILED_MAX_ITERATION_LIMIT;
            }

            ////////////////////////////////////////////////
            //calculate the current cost of Barrier Function.
            //B = t*f0(x)-sum(log(-fi(x))
            double F0X = objective.f0Result;
            F0X *= t;
            for(int i=0; i<objective.fiResults.length; i++) {
                if(objective.fiResults[i]>=0.0) {
                    return FAILED_IMPOSSIBLE_TO_REMAIN_WITHIN_FEASIBLE;
                }
                F0X -= Math.log(-objective.fiResults[i]);
            }
            
            // calculate the Hessian of Barrier Function
            double[][] hess_br = calcHessianOfBarrierFunction(objective, t);

            // calculate the Gradient of Barrier Function
            double[] g_br = calcGradientOfBarrierFunction(objective, t);

            ////////////////////////////////////////////////////////////////////
            // check exit condition
            double gradXNorm = Math.sqrt(dotProduct(g_br, g_br));
            if(gradXNorm < (_tolerance*_tolerance)){
                return INNER_LOOP_FINISH; //exit from inner loop
            }
            if (phase == 1) {
                double[] X = objective.currentX; // get reference to the current X.
                if(((maxValue(objective.fiResults)+X[X.length-1]) < -_tolerance)) {
                    return CONVERGED;
                }
            }
            ////////////////////////////////////////////////////////////////////

            ////////////////////////////////////////////////////////////////////
            // solving Hbr*dx = -grad_sum
            double[] dx = solveEquaitionWithCholeskyDecomposition(hess_br, g_br);
            //TODO: If error of solver is greater than threshold, 
            // Hessian needs to be modified because Hessian might not be positive definite.
            double max_error = 0;
            int count =0;
            int count_max = 400;
            for(count = 0; count<count_max; count++) {
                for(int i=0; i<dx.length; i++) {
                    if(Double.isNaN(dx[i])) {
                        dx[i] = 0.0;
                    }
                }
                //check the error of solution
                double[] check = DoubleMatrixMath.multiply(dx, hess_br);
                max_error = 0;
                for(int row=0; row<check.length; row++) {
                    if(max_error<Math.abs(check[row]+g_br[row]))
                        max_error = Math.abs(check[row]+g_br[row]);
                }
                if(max_error<1.0E-8) break;
                // Hessian needs to be modified to be positive definite.
                for(int i=0; i<hess_br.length; i++) {
                    hess_br[i][i] += Math.abs(hess_br[i][i])*0.0000001;
                }
                dx = solveEquaitionWithCholeskyDecomposition(hess_br, g_br);
            }
            if(count>0) {
                if(flag_debug_print) System.out.println("Hessian modified "+count);
                if(count==count_max) {
                    // If Hessian was not positive definite, 
                    // dx is defined by the gradient in the same way as the gradient decent method.
                    for(int i=0; i<dx.length; i++) {
                        dx[i] = -g_br[i];
                    }
                }
            }
            ////////////////////////////////////////////////////////////////////

            ////////////////////////////////////////////////////////////////////
            // check exit condition: check the Newton decrement
            double[] step_H = DoubleMatrixMath.multiply(hess_br, dx);
            double lambda = dotProduct(step_H, dx);
            if ((lambda/4.0 <= _tolerance*_tolerance)) {
                return INNER_LOOP_FINISH; //exit from inner loop
            }
            ////////////////////////////////////////////////////////////////////

            ////////////////////////////////////////////////////////////////////
            // line search and update
            double s = 1;
            double df = dotProduct(g_br, dx);
            double[] X_pre = objective.getCurrentPoint(); //copy the previous value.

            ////////////////////////////////////////////////////////
            // To reduce the number of evaluation, at first we search the optimal point using 
            // approximated function. Approximated function calculates the cost and constraints 
            // using current gradients and hessians.
            ApproximatedObjectiveFunction approx_objective = new ApproximatedObjectiveFunction(objective);
            double[] X1 = new double[dx.length];
            double condSX_pre = 0;
            double back_tracking_prop = 0.95;
            for(int search_it=0; search_it<250; search_it++) {
                for(int row=0; row<dx.length; row++) {
                    X1[row] = X_pre[row] + dx[row]*s; //X1 =  x + s*dx
                }
                approx_objective.calcFuncInternal(X1);
                boolean areAllNegative = true;
                for (int j = 0; areAllNegative && j < approx_objective.fiResults.length; j++) {
                    areAllNegative = (approx_objective.fiResults[j] < 0.0);
                }
                if(areAllNegative) {
                    // Calculate the total cost of the barrier function at new point X1.
                    double condSX = approx_objective.f0Result * t;
                    for(int i=0; i<approx_objective.fiResults.length; i++) {
                        condSX -= Math.log(-approx_objective.fiResults[i]);
                    }
                    if((search_it!=0)&&(!Double.isNaN(condSX_pre))&&(condSX_pre<=condSX)) {
                        s = s/back_tracking_prop; //condSX_pre is minimum so previous s is used. 
                        break;
                    }
                    condSX_pre = condSX;
                } else {
                    condSX_pre = Double.NaN;
                }
                s = back_tracking_prop * s;
            }
            ////////////////////////////////////////////////////////
            
            ////////////////////////////////////////////////////////
            // backtracking line search with actual objective function
            for(int search_it=0; search_it<250; search_it++) {
                // x + s*step
                for(int row=0; row<dx.length; row++) {
                    X1[row] = X_pre[row] + dx[row]*s;
                }
                objective.calcFuncInternal(X1);
                boolean areAllNegative = true;
                for (int j = 0; areAllNegative && j < objective.fiResults.length; j++) {
                    areAllNegative = (objective.fiResults[j] < 0.0);
                }
                if(areAllNegative) {
                    double condSX = objective.f0Result * t;
                    for(int i=0; i<objective.fiResults.length; i++) {
                        condSX -= Math.log(-objective.fiResults[i]);
                    }
                    
                    double condDX = F0X + _alpha  * s * df;
                    if((flag_debug_print)&&(objective.iterationCounter>50)) {
                        System.out.println(objective.iterationCounter+"f0:"+objective.f0Result+"\t condSX:"+condSX);
                    }
                    if (condSX <= condDX) {
                        break;
                    }
                }
                s = _beta * s;
                if(s<1.0E-5) {
                    if(flag_debug_print) System.out.println("Step s = "+s);
                }
                if(s<MIN_STEP_SIZE) {
                    return FAILED_IMPOSSIBLE_TO_REMAIN_WITHIN_FEASIBLE;
                }
            }
            ////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
        }
    }
    
    /**
     * Relax the constraints. This function called when Phase1 is converged and some of the constraints 
     * are not satisfied. This function overwrite the constraint function as below.
     * g(x) -> g(x)-val
     * Where "val" is the residual of the constraint function.
     * @param objective : the set of objective function, current_results,... which is converged in Phase1.
     * @param tolerance : all constraints are forced to satisfy g(x) < -tolerance 
     */
    private void relaxConstraints(ObjectiveFunction objective, double tolerance) {
        double residual = objective.currentX[objective.currentX.length-1];
        if(flag_debug_print) System.out.print("constraint No.");
        for(int i=objective.fiResults.length-1; i>=0; i--) {
            if(objective.fiResults[i]+residual >= -tolerance) {
                objective.addConstraints(i, -(objective.fiResults[i]+residual + tolerance));
                if(flag_debug_print) System.out.print(i+" ");
            }
        }
        if(flag_debug_print) System.out.println("was relaxed");
    }

    /**
     * calculate Hbr = t*H0 + SUM(-1/fi(x) * Hi) + SUM(1/fi(x)^2 * gi*giT)
     * @param objective: date set of Hessians, gradients, and results of the cost function and constraints.
     * @param t: a parameter (gap) of Barrier Function.
     * @return double matrix which is a Hessian of Barrier Function (B = t*f0(x)-SUM(log(-fi(x)))
     */
    private double[][] calcHessianOfBarrierFunction(ObjectiveFunction objective, double t) {
        // get reference to results and gradients of the cost function and constraints.
        double[] gradF0X = objective.f0Gradient;
        double[] fiX = objective.fiResults;
        double[][] gradFiX = objective.fiGradients;

        //////////////////////////////////////////////////////////////////////
        // Hessians of f0-fi are estimated in objective.calcFuncInternal().
        // If the function is not convex, corresponding hessian is converged to zero-matrix.
        double[][] HessSum = DoubleMatrixMath.multiply(objective.f0Hessian, t);
        for (int i = 0; i < fiX.length; i++) {
            double[][] HessFiX = DoubleMatrixMath.multiply(objective.fiHessians[i], -1.0/fiX[i]);
            HessSum = DoubleMatrixMath.add(HessSum, HessFiX);
        }
        
        double[][] GradSum = new double[gradF0X.length][gradF0X.length];
        for (int i = 0; i < fiX.length; i++) {
            // calculate gi*giT *(1/fi)*(1/fi)
            double[] gi = gradFiX[i];
            for(int row=0; row<gi.length; row++) {
                for(int col=0; col<row; col++) {
                    //To make GradSum be positive-definite, it needs to reduce non-diagonal elements.
                    double add_val = (gi[row]/fiX[i])*(gi[col]/fiX[i])*0.999; 
                    GradSum[row][col] += add_val;
                    GradSum[col][row] += add_val;
                }
                GradSum[row][row] += (gi[row]/fiX[i])*(gi[row]/fiX[i]); //Element at diagonal
            }
        }
        return DoubleMatrixMath.add(HessSum, GradSum);
    }
    
    /**
     * calculate Gbr = t*g0 + SUM(-1/fi(x) * gi)
     * @param objective :  date set of gradients, and results of the cost function and constraints.
     * @param t : a parameter (gap) of Barrier Function.
     * @return double matrix which is a gradient of Barrier Function (B = t*f0(x)-SUM(log(-fi(x)))
     */
    private double[] calcGradientOfBarrierFunction(ObjectiveFunction objective, double t) {
        // get reference to results and gradients of the cost function and constraints.
        double[] gradF0X = objective.f0Gradient;
        double[] fiX = objective.fiResults;
        double[][] gradFiX = objective.fiGradients;

        double[] gradient = new double[gradF0X.length];
        for(int col=0; col<gradF0X.length; col++) {
            gradient[col] = t*gradF0X[col];
        }
        for (int i = 0; i < fiX.length; i++) {  //sum((1/-fiX) * dfiX)
            for(int col=0; col<gradFiX[i].length; col++) {
                gradient[col] += (gradFiX[i][col] /(-fiX[i]));
            }
        }
        return gradient;
    }
    /**
     * Find the maximum value in vector.
     * @param vec input vector
     * @return maximum value
     */
    private double maxValue(double[] vec) {
        double ret = vec[0];
        for(int i=1; i<vec.length; i++) {
            if(ret<vec[i]) ret = vec[i];
        }
        return ret;
    }
    private double dotProduct(double[] v1, double[] v2) {
        if(v1.length != v2.length) {
            System.err.println("error in dotProduct: length of v1 and v2 must be the same. (v1:" + v1.length+" v2:"+v2.length+")");
        }
        double[] sum_vals = new double[v1.length];
        for(int i=0; i<v1.length; i++) {
            sum_vals[i]= v1[i]*v2[i];
        }
        return sumVector(sum_vals, v1.length);
    }

    /**
     * Calculate step vector of Newton decrement solving the KKT system 
     * without constraint:
     * H.v  = -g, <br>
     * (H: hessian matrix,  g: gradient vector)
     * This method returns the vector v which satisfies an equation above.
     */
    private double[] solveEquaitionWithCholeskyDecomposition(double[][] H, double[] g) {
        // Decompose H = L.LT
        double [][] L = new double[H.length][H.length];
        choleskyDecomposition(H, L, 1.0E-50);

        // Solve y (L.y =  -g)
        double[] y = new double[L.length];
        double[] sum_vals = new double[L.length];
        for (int i = 0; i < L.length; i++) {
            if(L[i][i]==0.0) {
                y[i] = 0;
                continue;
            }
            for (int j = 0; j < i; j++) {
                sum_vals[j] = L[i][j] * y[j];
            }
            double sum = sumVector(sum_vals, i);
            y[i] = (-g[i] - sum)/ L[i][i];
        }

        // Solve LT.V = y
        double[] V = new double[L.length];
        for (int i = L.length-1; i > -1; i--) {
            if(L[i][i]==0.0) {
                V[i] = 0;
                continue;
            }
            for (int j = L.length-1; j > i; j--) {
                sum_vals[L.length-1-j] = L[j][i] * V[j];
            }
            double sum = sumVector(sum_vals, L.length-1-i);
            V[i] = (y[i] - sum)/ L[i][i];
        }
        return V;
    }
    /**
     * return summation of vector whose size is "size"
     * this function changes argument v to zero vector.
     * @param v
     * @param size
     * @return summation of v
     */
    private double sumVector(double[] v, int size) {
        double sum = 0;
        for(int i=0; i<size; i++) {
            double max_abs = 0;
            int max_index = -1;
            for(int it=0; it<size; it++) {
                if(Math.abs(max_abs)<Math.abs(v[it])) {
                    max_abs = v[it];
                    max_index = it;
                }
            }
            if(max_abs==0.0) break;
            sum += max_abs;
            v[max_index] = 0.0;
        }
        return sum;
    }
    /**
     * TODO: This function should be defined in DoubleMatrixMath.
     * Return a matrix that is the decomposition of input matrix.
     * Input matrix A is decomposed into the matrix product of L x Lt.
     * @param A input matrix which is decomposed.
     * @param L output matrix (Lower triangular matrix)
     */
    private static boolean choleskyDecomposition(double[][] A, double[][] L, double tolerance) {
        for(int row = 0; row < L.length; row++) {
            for(int col = 0; col < L[0].length; col++) {
                L[row][col] = 0;
            }
        }
        ///////////////////////////////////////////////////////////
        //Implementation of modified cholesky decomposition
        double[] d = new double[A.length]; //singular values
        if(A[0][0] == 0) {
            L[0][0] = tolerance;
        } else {
            L[0][0] = A[0][0];
        }
        d[0] = 1.0/L[0][0];
        for(int i = 1; i < A.length; ++i){
            for(int j = 0; j <= i; ++j){
                double lld = A[i][j];
                for(int k = 0; k < j; ++k){
                    lld -= L[i][k]*L[j][k]*d[k];
                }
                L[i][j] = lld;
            }
            if(L[i][i]>tolerance) {
                d[i] = 1.0/L[i][i];
            } else if(L[i][i]<-tolerance) {
                return false;
            } else {
                d[i] = 1.0/tolerance;
            }
        }
        ///////////////////////////////////////////////////////////
        for(int col = 0; col < L[0].length; col++) {
            if(d[col] > 0) {
                double scale = Math.sqrt(d[col]);
                for(int row = 0; row <L.length; row++) {
                    L[row][col] *= scale;
                }
            } else {
                return false;
            }
        }
        return true;
    }
    private void printMatrix(double[][] matrix, String label) {
        System.out.println(label);
        for(int row=0; row<matrix.length; row++) {
            for(int col=0; col<matrix[0].length; col++) {
                System.out.print(matrix[row][col]+ " ");
            }
            System.out.println();
        }
    }
}

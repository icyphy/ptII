package org.ptolemy.optimization;

import ptolemy.math.DoubleMatrixMath;

/**
 * Definition of objective function and constraint functions.
 */
abstract class ObjectiveFunction {
    public double f0_result;            //Results of objective function.
    public double[] fi_results;   //Results of constraint functions. An array length is m, the number of constraints. 
    public double[] f0_gradient;        //Gradient of result of f0. An array length is n, the number of variables of objective function.
    public double[] f0_gradient_pre;
    public double[][] fi_gradients;     //Gradients of constraints. The number of col is n.
    public double[][] fi_gradients_pre;
    public double[][] f0_hessian;       //Hessian of f0. Lengths of array is [n][n]
    public double[][][] fi_hessians;    //Hessians of constraints. Lengths of array is [m][n][n].
    public double[] current_point;
    private double[] x_pre;
    private double[] fi_relax_val; //If there is no feasible point, constraints are relaxed using this value. Default value is 0.
    public int iteration_counter;
    public boolean stop_requested;

    /**
     * Constructor of CalcFunction.
     * In the constructor, double arrays for fi_results, gradients, 
     * and hessians are allocated.
     */
    public ObjectiveFunction(int dimensionX, int numOfConstraints) {
        fi_results = new double[numOfConstraints];
        f0_gradient = new double[dimensionX];
        f0_gradient_pre = new double[dimensionX];
        fi_gradients = new double[numOfConstraints][dimensionX];
        fi_gradients_pre = new double[numOfConstraints][dimensionX];
        f0_hessian = new double[dimensionX][dimensionX];
        fi_hessians = new double[numOfConstraints][dimensionX][dimensionX];
        current_point = new double[dimensionX];
        x_pre = new double[dimensionX];
        fi_relax_val = new double[numOfConstraints];
        iteration_counter = 0;
        stop_requested = false;

        for(int i=0; i<dimensionX; i++) {
            current_point[i] = 0.0;
        }
        for(int i=0; i<dimensionX; i++) {
            f0_hessian[i][i] = 1;
        }

        for(int i=0; i<numOfConstraints; i++) {
            for(int it=0; it<dimensionX; it++) {
                fi_hessians[i][it][it] = 1;
            }
        }
    }

    /**
     * Objective function and constraint functions.
     * In this function, all results(f0_result, fi_results) 
     * and gradients(f0_gradient, fi_gradients) must be updated.
     * @param x : input variables
     */
    abstract public void calcFunc(double[] x);

    /**
     * Objective function called by a solver.
     * @param x : input variables
     */
    public void calcFuncInternal(double[] x) {
        if(stop_requested) return;
        for(int i=0; i<fi_gradients.length; i++) {
            for(int it=0; it<fi_gradients[i].length; it++) {
                fi_gradients_pre[i][it] = fi_gradients[i][it];
            }
        }
        for(int i=0; i<f0_gradient.length; i++) {
            f0_gradient_pre[i] = f0_gradient[i];
            x_pre[i] = current_point[i];
            current_point[i] = x[i];
        }

        calcFunc(x);
        for(int i=0; i<fi_results.length; i++) {
            fi_results[i] += fi_relax_val[i];
        }
        
        if(iteration_counter==0) {
            //Copy all previous values 
            for(int i=0; i<fi_gradients.length; i++) {
                for(int it=0; it<fi_gradients[i].length; it++) {
                    fi_gradients_pre[i][it] = fi_gradients[i][it];
                }
            }
            for(int i=0; i<f0_gradient.length; i++) {
                f0_gradient_pre[i] = f0_gradient[i];
                x_pre[i] = current_point[i];
            }
        }
        updateHessian();
        iteration_counter++;
    }
    public void clearMatrix(double[][] matrix) {
        for(int row=0; row<matrix.length; row++) {
            for(int col=0; col<matrix[row].length; col++) {
                matrix[row][col] = 0;
            }
        }
    }
    public double[] getCurrentPoint() {
        double[] ret = new double[current_point.length];
        for(int i=0; i<ret.length; i++) {
            ret[i] = current_point[i];
        }
        return ret;
    }
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
        double[] dg0 = new double[f0_gradient.length];
        for(int it=0; it<dg0.length; it++) {
            dg0[it] = f0_gradient[it] - f0_gradient_pre[it];
        }
        double[] dx = new double[x_pre.length];
        for(int it=0; it<dx.length; it++) {
            dx[it] = current_point[it] - x_pre[it];
        }
        boolean is_hessian_calculated = updateHessianByBFGS(f0_hessian, dg0, dx);

        //Hessian of fi
        for(int i=0; i<fi_hessians.length; i++) {
            double[] dgi = new double[fi_gradients[i].length];
            for(int it=0; it<dgi.length; it++) {
                dgi[it] = fi_gradients[i][it] - fi_gradients_pre[i][it];
            }
            is_hessian_calculated = is_hessian_calculated & updateHessianByBFGS(fi_hessians[i], dgi, dx);
        }
        return is_hessian_calculated; //if at least one of the hessians are not calculated, return false.
    }
    public void addConstraints(int id, double val) {
        fi_relax_val[id] += val;
    }
    public void resetConstraints() {
        for(int i=0; i<fi_relax_val.length; i++) {
            fi_relax_val[i] = 0;
        }
    }

    static boolean updateHessianByBFGS(double[][] est_hessian_current, double[] dg, double[] dx) {
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
}

class ObjectiveFunctionForPhaseI extends ObjectiveFunction{
    private ObjectiveFunction source;
    public ObjectiveFunctionForPhaseI(ObjectiveFunction a_source) {
        super(a_source.current_point.length+1, a_source.fi_results.length);
        source = a_source;
        
        for(int i=0; i<source.current_point.length; i++) {
            current_point[i] = source.current_point[i];
        }
        current_point[current_point.length-1] = 0;
    }
    public void calcFunc(double[] x) {
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
        source.calcFuncInternal(input_x);
        //objective function: f(x,s) = s
        f0_result = x[x.length-1];
        for(int col=0; col<f0_gradient.length-1; col++) {
            f0_gradient[col] = 0;
        }
        f0_gradient[f0_gradient.length-1] = 1;
        for(int row=0; row<f0_hessian.length; row++) {
            for(int col=0; col<f0_hessian[0].length; col++) {
                f0_hessian[row][col] = 0;
            }
        }
        // Inequality constraints: fi(x, s) = fi(x)-s
        for(int i=0; i<fi_results.length; i++) {
            fi_results[i] = source.fi_results[i]-x[x.length-1];
            for(int it=0; it<source.fi_gradients[i].length; it++) {
                fi_gradients[i][it] = source.fi_gradients[i][it];
            }
            fi_gradients[i][fi_gradients[i].length-1] = -1;
            clearMatrix(fi_hessians[i]);
            for(int row=0; row<source.fi_hessians[i].length; row++) {
                for(int col=0; col<source.fi_hessians[i][row].length; col++) {
                    fi_hessians[i][row][col] = source.fi_hessians[i][row][col];
                }
            }
        }
    }
    public void addConstraints(int id, double val) {
        source.addConstraints(id, val);
    }
    public void resetConstraints() {
        source.resetConstraints();
    }
    public void setCurrentPoint(double[] x) {
        for(int i=0; i<current_point.length; i++) {
            current_point[i] = x[i];
            if(i<current_point.length-1) {
                source.current_point[i] = x[i];
            }
        }
    }
}

class ApproximatedObjectiveFunction extends ObjectiveFunction{
    private ObjectiveFunction source;
    public ApproximatedObjectiveFunction(ObjectiveFunction a_source) {
        super(a_source.current_point.length, a_source.fi_results.length);
        source = a_source;
        
        for(int i=0; i<source.current_point.length; i++) {
            current_point[i] = source.current_point[i];
        }

        // Copy f0 Hessian and fi_gradients
        for(int row=0; row<f0_hessian.length; row++) {
            for(int col=0; col<f0_hessian[row].length; col++) {
                f0_hessian[row][col] = source.f0_hessian[row][col];
            }
        }
        for(int i=0; i<fi_results.length; i++) {
            for(int it=0; it<fi_gradients[i].length; it++) {
                fi_gradients[i][it] = source.fi_gradients[i][it];
            }
            clearMatrix(fi_hessians[i]); //fi_hessians are all zero.
        }
    }
    public void calcFunc(double[] x) {
    }
    /**
     * Objective function called in QPSolver.
     * @param x : input variables
     */
    public void calcFuncInternal(double[] x) {
        double[] dx = new double[x.length];
        for(int i=0; i<x.length; i++) {
            current_point[i] = x[i];
            dx[i] = x[i]-source.current_point[i];
        }
        
        /////////////////////////////////////////
        ///// result of objective function///////
        // f(x+dx) = f(x)+(1/2)(dxTQdx)+Pdx 
        double[] Qx = DoubleMatrixMath.multiply(dx, source.f0_hessian);
        f0_result = source.f0_result;
        for(int i=0; i<Qx.length; i++) {
            f0_result += (0.5*Qx[i]*dx[i] + source.f0_gradient[i]*dx[i]);
        }
        // df(x+dx) = df(x)+Qdx
        for(int col=0; col<f0_gradient.length; col++) {
            f0_gradient[col] = source.f0_gradient[col]+Qx[col];
        }
        // Hessian(x+dx) = Hessian(x) was already copied in the constructor.
        
        ///////////////////////////////////////////////////
        ////// Inequality constraints: 
        // fi(x+dx) = fi(x)+Pdx
        for(int i=0; i<fi_results.length; i++) {
            fi_results[i] = source.fi_results[i];
            for(int col=0; col<dx.length; col++) {
                fi_results[i] += source.fi_gradients[i][col]*dx[col];
            }
        }
        // dfi(x+dx) = dfi(x) were already copied in the constructor.
        // Hessian_i(x+dx) = 0 were initialized in the constructor.
    }
    public void addConstraints(int id, double val) {
        source.addConstraints(id, val);
    }
    public void resetConstraints() {
        source.resetConstraints();
    }
}
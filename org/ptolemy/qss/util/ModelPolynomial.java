/* Model how a variable changes with time, using a polynomial.

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


package org.ptolemy.qss.util;


import ptolemy.actor.util.Time;


//////////////////////////////////////////////////////////////////////////
//// ModelPolynomial


/** Model how a variable changes with time, using a polynomial.
 *
 * <p>Represent a polynomial model:</p>
 * <p><i>xMdl{t} = c0 + c1*dt + c2*dt^2 + ...</i></p>
 *
 * <p>where</p>
 * <ul>
 * <li><i>xMdl{t}</i>, model of some scalar variable <i>x</i>, as a function of time.</li>
 * <li><i>t</i>, time of interest.</li>
 * <li><i>dt = t - tMdl</i>, time difference.</li>
 * <li><i>tMdl</i>, time of model formation.</li>
 * <li><i>c0, c1, c2, ...</i>, model coefficients.  Equal to the value, first
 * derivative, second derivative, and so on, at time <i>tMdl</i>.</li>
 * <li>The notation <i>g{y}</i> means that <i>g</i> is a function
 * of <i>y</i>.</li>
 * </ul>
 *
 *
 * <h2>Need for a model time</h2>
 *
 * <p>Each <code>ModelPolynomial</code> object needs to have an associated simulation
 * time, in order to define the model.
 * Add this simulation time by setting field <code>this.tMdl</code> to the
 * desired <code>Time</code> object.
 * Failing to do so will cause a <code>NullPointerException</code> for most of
 * the useful methods on this class.</p>
 *
 * <p>Alternative designs that would enforce this condition include:</p>
 * <ul>
 * <li>Add a <code>Time> object to the constructor.
 * This design was rejected because it implies that the model time is fixed,
 * and should not be changed during the lifetime of the model.</li>
 * <li>Allocate a new <code>Time</code> object, with a default
 * time, when constructing a <code>ModelPoly</code> object.
 * This design was rejected because class <code>Time</code> is meant
 * to be able to be swapped with other implementations, which may have
 * different constructor needs.</li>
 * </ul>
 *
 *
 * <h2>Purpose</h2>
 *
 * <p>This class is meant to provide a lightweight capability for working with
 * and exchanging models.
 * The majority of data are kept public, in order to facilitate changing model
 * coefficients without the overhead of accessor (setter, getter) calls.</p>
 *
 * <p>The design intention is that multiple objects in a system can share a
 * single <code>ModelPoly<code>, in order to share a model of a common variable.
 * In principle, at any given simulation time, only one of those objects should
 * have "write access", and it alone should set the model parameters.
 * The other objects in the system can use the model, but should refrain from
 * changing it.</p>
 *
 * <p>Note that the <code>ModelPoly</code> object does nothing to enforce
 * cooperation among readers and writers.
 * It does, however, provide a simple counter for how many objects claim
 * write access.
 * This allows objects that intend to cooperate to double-check that they
 * are doing so correctly.</p>
 *
 * @author David M. Lorenzetti
 * @version $id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public final class ModelPolynomial {


    /** 
     * Construct a <code>ModelPolynomial</code>.
     *
     * @param maxOrder Maximum order of the polynomial (0=constant, 1=line, etc).
     */
    public ModelPolynomial(final int maxOrder) {

        // Check inputs.
        assert( maxOrder >= 0 );

        // Initialize.
        _maxCoeffIdx = maxOrder;
        coeffs = new double[_maxCoeffIdx+1];

        }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods


    /** 
     * Claim write access.
     *
     * @return Count of all unique claims of write access (including caller).
     */
    public final int claimWriteAccess() {
        return(++_writerCt);
    }

    /** 
     * Evaluate the model at a simulation time.
     *
     * @param simTime Simulation time at which to evaluate the model.
     * @return The model evaluated at a simulation time.
     */
    public final double evaluate(final Time simTime) {
        return(
            this.evaluate(simTime.subtractToDouble(tMdl))
            );
    }  

    /** 
     * Evaluate the model at a delta-time.
     *
     * @param dt Difference (simTime - tMdl) at which to evaluate the model.
     * @return The model evaluated at a delta-time.     
     */
    public final double evaluate(final double dt) {

        // Initialize.
        double val;

        // Model:
        // xMdl{t} = c0 + c1*dt + c2*dt^2 + ...
        // xMdl{t} = c0 + dt*(c1 + dt*(c2 + ...))

        // TODO: Consider adding a short-circuit test for dt==0.

        // Evaluate.
        switch( _maxCoeffIdx ) {

        case 0:
            val = coeffs[0];
            break;

        case 1:
            val = coeffs[0] + dt*coeffs[1];
            break;

        case 2:
            val = coeffs[0] + dt*(coeffs[1] + dt*coeffs[2]);
            break;

        default:
            // Evaluate contributions from cubic terms.
            val = coeffs[0] + dt*(coeffs[1] + dt*(coeffs[2] + dt*coeffs[3]));
            // Add contributions from higher-order terms.
            if( _maxCoeffIdx > 3 ) {
                double valHot = coeffs[_maxCoeffIdx];
                for( int ii=_maxCoeffIdx-1; ii>3; --ii ) {
                    valHot = coeffs[ii] + dt*valHot;
                }
                val += valHot*dt*dt*dt*dt;
            }
            break;

        }

        return( val );

    }  


    /** 
     * Evaluate d{model}/d{t} at a simulation time.
     *
     * @param simTime Simulation time at which to evaluate the derivative.
     * @return The model derivative evaluated at a simulation time.
     */
    public final double evaluateDerivative(final Time simTime) {
        return(
            this.evaluateDerivative(simTime.subtractToDouble(tMdl))
            );
    } 


    /** 
     * Evaluate d{model}/d{t} at a delta-time.
     *
     * @param dt Difference (simTime - tMdl) at which to evaluate the derivative.
     * @return The model derivative evaluated at a delta-time.
     */
    public final double evaluateDerivative(final double dt) {

        // Initialize.
        double deriv;

        // Model:
        // xMdl{t} = c0 + c1*dt + c2*dt^2 + c3*dt^3 + ...
        // d{xMdl}/d{t} = c1 + 2*c2*dt + 3*c3*dt^2 + ...
        // d{xMdl}/d{t} = c1 + dt*(2*c2 + dt*(3*c3 + ...))

        // Evaluate.
        switch( _maxCoeffIdx ) {

        case 0:
            deriv = 0.0;
            break;

        case 1:
            deriv = coeffs[1];
            break;

        case 2:
            deriv = coeffs[1] + dt*2*coeffs[2];
            break;

        default:
            // Evaluate contributions from cubic terms.
            deriv = coeffs[1] + dt*(2*coeffs[2] + dt*3*coeffs[3]);
            // Add contributions from higher-order terms.
            if( _maxCoeffIdx > 3 ) {
                double derivHot = _maxCoeffIdx*coeffs[_maxCoeffIdx];
                for( int ii=_maxCoeffIdx-1; ii>3; --ii ) {
                    derivHot = ii*coeffs[ii] + dt*derivHot;
                }
                deriv += derivHot*dt*dt*dt;
            }
            break;

        }

        return( deriv );

    }  


    /** 
     * Evaluate d^2{model}/d{t}^2 at a simulation time.
     *
     * @param simTime Simulation time at which to evaluate the derivative.
     * @return The model second derivative evaluated at a simulation time.
     */
    public final double evaluateDerivative2(final Time simTime) {
        return(
            this.evaluateDerivative2(simTime.subtractToDouble(tMdl))
            );
    } 


    /** 
     * Evaluate d^2{model}/d{t}^2 at a delta-time.
     *
     * @param dt Difference (simTime - tMdl) at which to evaluate the derivative.
     * @return The model second derivative evaluated at a delta-time.
     */
    public final double evaluateDerivative2(final double dt) {

        // Initialize.
        double deriv2;

        // Model:
        // xMdl{t} = c0 + c1*dt + c2*dt^2 + c3*dt^3 + c4*dt^4 + ...
        // d{xMdl}/d{t} = c1 + 2*c2*dt + 3*c3*dt^2 + 4*c4*dt^3 + ...
        // d^2{xMdl}/d{t}^2 = 2*c2 + 6*c3*dt + 12*c4*dt^2 + ...
        // d^2{xMdl}/d{t}^2 = 2*c2 + dt*(6*c3 + dt*(12*c4 + ...))

        // Evaluate.
        switch( _maxCoeffIdx ) {

        case 0:
            deriv2 = 0.0;
            break;

        case 1:
            deriv2 = 0.0;
            break;

        case 2:
            deriv2 = 2*coeffs[2];
            break;

        default:
            // Evaluate contributions from cubic terms.
            deriv2 = 2*coeffs[2] + dt*6*coeffs[3];
            // Add contributions from higher-order terms.
            if( _maxCoeffIdx > 3 ) {
                double deriv2Hot = _maxCoeffIdx*(_maxCoeffIdx-1)*coeffs[_maxCoeffIdx];
                for( int ii=_maxCoeffIdx-1; ii>3; --ii ) {
                    deriv2Hot = ii*(ii-1)*coeffs[ii] + dt*deriv2Hot;
                }
                deriv2 += deriv2Hot*dt*dt;
            }
            break;

        }

        return( deriv2 );

    } 
    
    /** 
     * Get the maximum order of the polynomial.
     * 
     * @return The maximum order of the polynomial.
     */
    public final int getMaximumOrder() {
        return(_maxCoeffIdx);
    }

    /** 
     * Find out how many objects claim write access.
     * 
     * @return The number of objects that claim write access.
     */
    public final int getWriterCount() {
        return(_writerCt);
    }

    /** Release claim of write access.
     * @return Count of all remaining claims of write access.
     */
    public final int releaseWriteAccess() {
        return(--_writerCt);
    }


    /** 
     * Return a string representation of the model.
     * 
     * @return The string representation of the model.
     */
    public final String toString() {

        // Model:
        // xMdl{t} = c0 + c1*dt + c2*dt^2 + ...
        // Start with c0.
        String res = String.format("%.4g", coeffs[0]);
        StringBuilder res_sb = new StringBuilder();
        // Initialize string builder with c0.
        res_sb.append(res);

        if( _maxCoeffIdx > 0 ) {

            // Form string "(t-tMdl)".
            String dtStr;
            final double tMdlDbl = tMdl.getDoubleValue();
            if( tMdlDbl > 0 ) {
                dtStr = String.format("(t-%.4g)", tMdlDbl);
            } else if( tMdlDbl < 0 ) {
                dtStr = String.format("(t+%.4g)", Math.abs(tMdlDbl));
            } else {
                dtStr = "t";
            }

            // Add c1*dt.
            if( coeffs[1] != 0 ) {
                //res += String.format(" %+.4g*%s", coeffs[1], dtStr);
                res = String.format(" %+.4g*%s", coeffs[1], dtStr);
                res_sb.append(res);
            }

            // Add higher-order terms.        
            for( int ii=2; ii<=_maxCoeffIdx; ++ii ) {
                if( coeffs[ii] != 0 ) {
                    //res += String.format(" %+.4g*%s^%d", coeffs[ii], dtStr, ii);
                    res = String.format(" %+.4g*%s^%d", coeffs[ii], dtStr, ii);
                    res_sb.append(res);
                }
            }

        }

        return( res_sb.toString() );

    } 


    ///////////////////////////////////////////////////////////////////
    ////                         public variables


    // Simulation time at which model was formed, such that xMdl{tMdl} = c0.
    public Time tMdl;

    // Polynomial coefficients, in order: [c0, c1, c2, ...].
    public final double[] coeffs;

    // TODO: Consider hard-coding c0..c3 directly in the type, and only
    // allocating an array if have coefficients higher than that.
    //   So far, finding that during use, the user already knows what
    // coefficient is getting set (e.g., is it the model value, the model
    // derivative, etc.).  So the accesses are always hard-coded, for
    // example, to "coeffs[0]" rather than to "coeffs[ii]".
    //   Not likely, in current plans, to go beyond a cubic.  So could just
    // hard-code derivations up to cubic, and then append array access when
    // needed.
    //   The possible "win" for this redesign is speed, by helping reduce
    // cache misses when access a model (because its time and its coefficients
    // will be stored closer together in memory).  Might also save a bit of
    // time by removing the indirection through the array, although this is
    // not likely a big deal.


    ///////////////////////////////////////////////////////////////////
    ////                         private variables

    // Store the model coefficients in coeffs[0] .. coeffs[_maxCoeffIdx].
    private final int _maxCoeffIdx;

    // Count how many objects claim write access.
    private int _writerCt;


}

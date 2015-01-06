/* Implement the QSS1 method for solving ordinary differential equations.

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


package org.ptolemy.qss.solver;


import org.ptolemy.qss.util.ModelPolynomial;

import ptolemy.actor.util.Time;


//////////////////////////////////////////////////////////////////////////
//// QSS1


/** Implement the QSS1 method for solving ordinary differential equations.
 *
 * @author David M. Lorenzetti, Contributor: Thierry S. Nouidui
 * @version $id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public final class QSS1
    extends QSSBase {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods


    /** Initialize object fields (QSS-specific).
     */
    public final void initializeWorker() {

        // Check internal consistency.
        assert( _stateVals_xx == null );
        assert( _stateCt > 0 );
        assert( _ivCt >= 0 );

        // Allocate scratch memory.
        _stateVals_xx = new double[_stateCt];
        _stateDerivs_xx = new double[_stateCt];
        if( _ivCt > 0 ) {
            _ivVals_xx = new double[_ivCt];
        }

    }  


    /** Get the order of the external, quantized state models exposed by the integrator.
     */
    public final int getStateModelOrder() {
        return( 0 );
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods


    /** Form a new external, quantized state model (QSS-specific).
     *  @param stateIdx The state index.
     */
    protected final void _triggerQuantizationEventWorker(final int stateIdx) {

        // Note the superclass takes care of updating status variables and so on.

        // Initialize.
        final ModelPolynomial qStateMdl = _qStateMdls[stateIdx];
        final ModelPolynomial cStateMdl = _cStateMdls[stateIdx];

        // Update the external, quantized state model.
        qStateMdl.tMdl = _currSimTime;
        qStateMdl.coeffs[0] = cStateMdl.evaluate(_currSimTime);

    }  


    /** Form new internal, continuous state models (QSS-specific).
     */
    protected final void _triggerRateEventWorker()
        throws Exception {

        // Note the superclass takes care of updating status variables and so on.

        // Get values, at {_currSimTime}, of arguments to derivative function.
        //   In general, expect the integrator formed all of its
        // continuous state models at the same time.  If so, can find a
        // single delta-time, rather than having to find multiple differences
        // from {_currSimTime}.  Know that finding time differences is
        // expensive in Ptolemy, so want to avoid doing that if possible.
        //   However, there is a chance that the continuous state models were
        // formed at different times.  For example:
        // (1) User can reset a single state at any simulation time.
        // (2) In future, might be possible to avoid updating a
        // continuous state model if know none of its arguments changed.
        Time tStateMdl = null;
        double dtStateMdl = 0;
        for( int ii=0; ii<_stateCt; ++ii ) {
            final ModelPolynomial cStateMdl = _cStateMdls[ii];
            // Check for different model time.  Note testing object identity OK.
            if( cStateMdl.tMdl != tStateMdl ) {
                tStateMdl = cStateMdl.tMdl;
                dtStateMdl = _currSimTime.subtractToDouble(tStateMdl);
            }
            _stateVals_xx[ii] = cStateMdl.evaluate(dtStateMdl);
        }
        // In general, don't expect input variable models to have same times.
        for( int ii=0; ii<_ivCt; ++ii ) {
            _ivVals_xx[ii] = _ivMdls[ii].evaluate(_currSimTime);
        }

        // Evaluate derivative function at {_currSimTime}.
        final int retVal = _derivFcn.evaluateDerivatives(_currSimTime, _stateVals_xx, _ivVals_xx,
            _stateDerivs_xx);
        if( 0 != retVal ) {
            throw new Exception("_derivFcn.evalDerivs() returned " +retVal);
        }

        // Update the internal, continuous state models.
        //   This also updates the rate model, which is just the derivative of
        // the state model.
        for( int ii=0; ii<_stateCt; ++ii ) {
            final ModelPolynomial cStateMdl = _cStateMdls[ii];
            cStateMdl.tMdl = _currSimTime;
            cStateMdl.coeffs[0] = _stateVals_xx[ii];
            cStateMdl.coeffs[1] = _stateDerivs_xx[ii];
        }

    } 


    /** Get the predicted quantization-event time for a state (QSS-specific).
     *  @param stateIdx The state index.
     *  @param quantEvtTimeMax The maximum quantization event time.
     */
    protected final Time _predictQuantizationEventTimeWorker(
        final int stateIdx, final Time quantEvtTimeMax) {

        // Note the superclass takes care of updating status variables and
        // storing the returned result.

        // Initialize.
        final ModelPolynomial qStateMdl = _qStateMdls[stateIdx];
        final ModelPolynomial cStateMdl = _cStateMdls[stateIdx];
        final double dq = _dqs[stateIdx];
        final double cStateDeriv = cStateMdl.coeffs[1];

        // Check internal consistency.
        assert( dq > 0 );
        assert( quantEvtTimeMax.getDoubleValue() > 0 );
        assert( quantEvtTimeMax.compareTo(qStateMdl.tMdl) > 0 );
        assert( quantEvtTimeMax.compareTo(cStateMdl.tMdl) > 0 );

        // Early return.
        if( cStateDeriv == 0 ) {
            return( quantEvtTimeMax );
        }

        // Find predicted quantization-event time, as change from {tMostRecent}.
        Time tMostRecent;
        double dt;
        if( qStateMdl.tMdl.compareTo(cStateMdl.tMdl) > 0 ) {
            // Here, most recent event was a quantization-event.
            tMostRecent = qStateMdl.tMdl;
            dt = dq / Math.abs(cStateDeriv);
        } else {
            // Here, most recent event was a rate-event.
            tMostRecent = cStateMdl.tMdl;
            final double constDiff = qStateMdl.coeffs[0] - cStateMdl.coeffs[0];
            if( cStateDeriv > 0 ) {
                dt = (constDiff + dq) / cStateDeriv;
            } else {
                dt = (constDiff - dq) / cStateDeriv;
            }
        }

        // Require {dt} > 0.
        if( dt <= 0 ) {
            // In exact arithmetic, and if the integrator is being stepped properly,
            // this should never happen.  However, if the integrator stepped to a
            // time very close to the previous predicted quantization-event time,
            // or given a small numerator and large denominator in expressions
            // above, can get nonpositive {dt}.
            //   Reset to as small a value as can manage.
            //   Use the `ulp`, the "units in the last place".  From the
            // documentation at {http://docs.oracle.com/javase/7/docs/api/java/lang/Math.html}:
            // "For a given floating-point format, an ulp of a specific real
            // number value is the distance between the two floating-point
            // values bracketing that numerical value."
            // TODO: Construct integrator with "min time step" parameter,
            // and pass it in for use it here.
            dt = java.lang.Math.ulp(tMostRecent.getDoubleValue());
        }

        // Bound result to reasonable limits.
        //   At lower end, need a positive number that, when added to {tMostRecent},
        // produces a distinct time.
        //   At upper end, can't be larger than {quantEvtTimeMax}.
        Time predQuantEvtTime;
        while( true ) {
            if( quantEvtTimeMax.subtractToDouble(tMostRecent) <= dt ) {
                // Here, tMostRecent + dt >= quantEvtTimeMax.
                //   Note determined this case in a slightly roundabout way, since
                // simply adding {dt} to {tMostRecent} may cause problems if {quantEvtTimeMax}
                // reflects some inherent limitation of class {Time}.
                predQuantEvtTime = quantEvtTimeMax;
                break;
            }
            // Here, tMostRecent + dt < quantEvtTimeMax.
            predQuantEvtTime = tMostRecent.addUnchecked(dt);
            if( predQuantEvtTime.compareTo(tMostRecent) > 0 ) {
                // Here, added {dt} and got a distinct, greater, time.
                break;
            }
            // Here, {dt} so small that can't resolve difference from {tMostRecent}.
            dt *= 2;
        }

        return( predQuantEvtTime );

    }  


    ///////////////////////////////////////////////////////////////////
    ////                         private variables

    // Scratch memory.
    //   For local (per-method) calculations, not inter-method communication.
    // When a method returns, it should be possible to write random values to
    // this memory, without losing any information about the solver state.
    //   Thus, if the integrator got serialized (marshalled) to disk, this
    // memory could be ignored.
    private double[] _stateVals_xx;
    private double[] _stateDerivs_xx;
    private double[] _ivVals_xx;


} 

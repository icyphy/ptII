/* Implement the LIQSS2 method for solving ordinary differential equations.

Copyright (c) 2014-2016 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// LIQSS2Fd

/** Implement the LIQSS2 method for solving ordinary differential equations.
 *
 * <p><i>Fd</i>:
 * When handling a rate-event, select the time at which to estimate the
 * second derivative for the internal, continuous state model using a
 * standard finite-difference perturbation procedure.</p>
 *
 * @author David M. Lorenzetti, Contributor: Thierry S. Nouidui
 * @version $Id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public final class LIQSS2Fd extends QSSBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods

    /**
     * Get the order of the external, quantized state models exposed by the integrator.
     */
    public final int getStateModelOrder() {
        return (1);
    }

    /**
     * Initialize object fields (QSS-specific).
     */
    public final void _initializeWorker() {

        // Check internal consistency.
        assert (_stateVals_xx == null);
        assert (_stateCt > 0);
        assert (_ivCt >= 0);

        // Allocate memory for diagonalized state model.
        _jacDiags = new double[_stateCt];
        _inputTermModels = new ModelPolynomial[_stateCt];
        for (int ii = 0; ii < _stateCt; ++ii) {
            final ModelPolynomial inputTermModel = new ModelPolynomial(1);
            inputTermModel.claimWriteAccess();
            _inputTermModels[ii] = inputTermModel;
        }
        _qStateModelDiffs = new double[_stateCt];

        // Allocate memory for finding predicted quantization-event time.
        _cStatesLastQevt = new double[_stateCt];

        // Allocate scratch memory.
        _stateVals_xx = new double[_stateCt];
        _stateValsSample_xx = new double[_stateCt];
        _stateValsSample2_xx = new double[_stateCt];
        _stateValsSample3_xx = new double[_stateCt];
        _stateDerivs_xx = new double[_stateCt];
        _stateDerivs2_xx = new double[_stateCt];
        _stateDerivsSample_xx = new double[_stateCt];
        if (_ivCt > 0) {
            _ivVals_xx = new double[_ivCt];
            _ivValsSample_xx = new double[_ivCt];
            _ivValsSample2_xx = new double[_ivCt];
            _ivValsSample3_xx = new double[_ivCt];
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods

    /**
     *  Get the predicted quantization-event time for a state (QSS-specific).
     *
     *  @param stateIdx The state index.
     *  @param quantEvtTimeMax The maximum quantization event time.
     *  @return the predicted quantization-event time for a state (QSS-specific).
     */
    protected final Time _predictQuantizationEventTimeWorker(
            final int stateIdx, final Time quantEvtTimeMax) {

        // Note the superclass takes care of updating status variables and
        // storing the returned result.

        // Initialize.
        final ModelPolynomial qStateModel = _qStateModels[stateIdx];
        final ModelPolynomial cStateModel = _cStateModels[stateIdx];
        final double dq = _dqs[stateIdx];

        // Check internal consistency.
        assert (dq > 0);
        assert (quantEvtTimeMax.getDoubleValue() > 0);
        assert (quantEvtTimeMax.compareTo(qStateModel.tModel) > 0);
        assert (quantEvtTimeMax.compareTo(cStateModel.tModel) > 0);

        // Find predicted quantization-event time, as change from {tMostRecent}.
        Time tMostRecent;
        double dt;
        if (qStateModel.tModel.compareTo(cStateModel.tModel) > 0) {
            // Here, most recent event was a quantization-event.
            tMostRecent = qStateModel.tModel;
            // Note the math never refers to {qStateModel.coeffs[0]}, so no need
            // to force it to use {_cStatesLastQevt[stateIdx]}.
            dt = _predictQuantizationEventDeltaTimeQSS2QFromC(qStateModel,
                    cStateModel, dq, _exactInputs);
        } else {
            // Here, most recent event was a rate-event.
            tMostRecent = cStateModel.tModel;
            // Note use a modified quantized state model, for which the value
            // coefficient is reset to the value of continuous state at the time
            // the quantized state model was formed.
            // TODO: Consider modifying method _predictQuantEvtDeltaTime_qss2_general()
            // to handle this as an added input argument.
            final double qHold = qStateModel.coeffs[0];
            qStateModel.coeffs[0] = _cStatesLastQevt[stateIdx];
            dt = _predictQuantizationEventDeltaTimeQSS2General(qStateModel,
                    cStateModel, dq, _exactInputs);
            qStateModel.coeffs[0] = qHold;
        }

        // Require {dt} > 0.
        if (dt <= 0) {
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
        while (true) {
            if (quantEvtTimeMax.subtractToDouble(tMostRecent) <= dt) {
                // Here, tMostRecent + dt >= quantEvtTimeMax.
                //   Note determined this case in a slightly roundabout way, since
                // simply adding {dt} to {tMostRecent} may cause problems if {quantEvtTimeMax}
                // reflects some inherent limitation of class {Time}.
                predQuantEvtTime = quantEvtTimeMax;
                break;
            }
            // Here, tMostRecent + dt < quantEvtTimeMax.
            predQuantEvtTime = tMostRecent.addUnchecked(dt);
            if (predQuantEvtTime.compareTo(tMostRecent) > 0) {
                // Here, added {dt} and got a distinct, greater, time.
                break;
            }
            // Here, {dt} so small that can't resolve difference from {tMostRecent}.
            dt *= 2;
        }

        return (predQuantEvtTime);

    }

    /**
     *  Form a new external, quantized state model (QSS-specific).
     *
     *  @param stateIdx The state index.
     */
    protected final void _triggerQuantizationEventWorker(final int stateIdx) {

        // Note the superclass takes care of updating status variables and so on.

        // Initialize.
        final ModelPolynomial qStateModel = _qStateModels[stateIdx];
        final ModelPolynomial cStateModel = _cStateModels[stateIdx];
        final double dtStateModel = _currSimTime.subtractToDouble(cStateModel.tModel);

        final double cState = cStateModel.evaluate(dtStateModel);
        final double cStateDeriv = cStateModel.evaluateDerivative(dtStateModel);

        final double qStateLastModel = qStateModel.evaluate(_currSimTime);
        final double jacDiag = _jacDiags[stateIdx];

        // Save values needed for finding predicted quantization-event time.
        _cStatesLastQevt[stateIdx] = cState;

        // Determine new quantized state.
        double qTest, qTestDeriv;
        if (cStateDeriv > 0) {
            qTest = cState + _dqs[stateIdx];
        } else {
            qTest = cState - _dqs[stateIdx];
        }
        qTestDeriv = cStateDeriv;
        if (jacDiag != 0) {
            // Check whether {qTest} gives consistent diagonalized state model.
            final ModelPolynomial inputTermModel = _inputTermModels[stateIdx];
            final double inputTermAtCurrSimTime = inputTermModel
                    .evaluate(dtStateModel);
            final double diagStateModelSlope2 = jacDiag * jacDiag * qTest
                    + jacDiag * inputTermAtCurrSimTime
                    + inputTermModel.evaluateDerivative(dtStateModel);
            final double cStateDeriv2 = cStateModel
                    .evaluateDerivative2(dtStateModel);
            if (diagStateModelSlope2 * cStateDeriv2 <= 0) {
                // Here, slopes of the state and diagonalized state models do
                // not have the same sign.
                //   Choose quantized state using diagonalized state model.
                qTestDeriv = -inputTermModel.coeffs[1] / jacDiag;
                qTest = (qTestDeriv - inputTermAtCurrSimTime) / jacDiag;
            }
        }

        // Update the external, quantized state model.
        qStateModel.tModel = _currSimTime;
        qStateModel.coeffs[0] = qTest;
        qStateModel.coeffs[1] = qTestDeriv;

        // Update information needed to form diagonalized state model.
        _qStateModelDiffs[stateIdx] = qTest - qStateLastModel;

    }

    /**
     * Form new internal, continuous state models (QSS-specific).
     */
    protected final void _triggerRateEventWorker() throws Exception {

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
        Time tStateModel = null;
        double dtStateModel = 0;
        for (int ii = 0; ii < _stateCt; ++ii) {
            final ModelPolynomial cStateModel = _cStateModels[ii];
            // Check for different model time.  Note testing object identity OK.
            if (cStateModel.tModel != tStateModel) {
                tStateModel = cStateModel.tModel;
                dtStateModel = _currSimTime.subtractToDouble(tStateModel);
            }
            _stateVals_xx[ii] = cStateModel.evaluate(dtStateModel);
        }
        // In general, don't expect input variable models to have same times.
        for (int ii = 0; ii < _ivCt; ++ii) {
            _ivVals_xx[ii] = _ivModels[ii].evaluate(_currSimTime);
        }

        // Evaluate derivative function at {_currSimTime}.
        int retVal = _derivFcn.evaluateDerivatives(_currSimTime, _stateVals_xx,
                _ivVals_xx, _stateDerivs_xx);
        if (0 != retVal) {
            throw new Exception("_derivFcn.evalDerivs() returned " + retVal);
        }

        // Update the diagonalized state models.
        //   Note have to do this before update the internal, continuous state models,
        // since need the rate of the old one.
        for (int ii = 0; ii < _stateCt; ++ii) {
            final ModelPolynomial qStateModel = _qStateModels[ii];
            final ModelPolynomial cStateModel = _cStateModels[ii];
            // Estimate the diagonal element of the Jacobian.
            //   If component {ii} did not just have a quantization-event, set
            // estimate to zero.
            // TODO: Test whether better to simply retain last estimate.
            double jacDiag = 0;
            if (qStateModel.tModel.compareTo(_currSimTime) == 0
                    && cStateModel.tModel.compareTo(_currSimTime) != 0) {
                // Here:
                // (1) Component {ii} just had a quantization-event.
                // (2) Did not just start simulation, or reset value of
                // component {ii}, so new quantized state model replaces
                // a "nearby" old one.
                // Therefore have information need to estimate Jacobian diagonal.
                final double qStateModelDiff = _qStateModelDiffs[ii];
                if (qStateModelDiff != 0) {
                    jacDiag = (_stateDerivs_xx[ii]
                            - cStateModel.evaluateDerivative(_currSimTime))
                            / qStateModelDiff;
                }
            }
            _jacDiags[ii] = jacDiag;
            // Update the input terms.
            final ModelPolynomial inputTermModel = _inputTermModels[ii];
            final double dtQStateModel = _currSimTime
                    .subtractToDouble(qStateModel.tModel);
            inputTermModel.coeffs[0] = _stateDerivs_xx[ii]
                    - jacDiag * qStateModel.evaluate(dtQStateModel);
            inputTermModel.coeffs[1] = 2 * cStateModel.coeffs[2]
                    - jacDiag * qStateModel.evaluateDerivative(dtQStateModel);
        }

        // Update the internal, continuous state models.
        //   Note this is a partial update, since don't yet know the second
        // derivatives.  Do the update now so that, when find the sample point
        // needed to estimate second derivatives, will be able to use as much
        // current information about the continuous state as possible.
        //   This also updates the rate model, which is just the derivative of
        // the state model.
        for (int ii = 0; ii < _stateCt; ++ii) {
            final ModelPolynomial cStateModel = _cStateModels[ii];
            cStateModel.tModel = _currSimTime;
            cStateModel.coeffs[0] = _stateVals_xx[ii];
            cStateModel.coeffs[1] = _stateDerivs_xx[ii];
            cStateModel.coeffs[2] = 0;
        }

        // Choose a sample time, different from {_currSimTime}.
        //   For estimating second derivatives.
        final double dtSample = 1e-8
                * Math.max(1, Math.abs(_currSimTime.getDoubleValue()));
        final Time tSample = minimumTime(_currSimTime.addUnchecked(dtSample),
                _quantEvtTimeMax);

        // Get values, at {tSample}, of arguments to derivative function.
        //   Note that here, know all continous state models have same time.
        // Therefore can use same delta-time for all evals.
        for (int ii = 0; ii < _stateCt; ++ii) {
            _stateVals_xx[ii] = _cStateModels[ii].evaluate(dtSample);
        }
        for (int ii = 0; ii < _ivCt; ++ii) {
            _ivVals_xx[ii] = _ivModels[ii].evaluate(tSample);
        }

        // Evaluate derivative function at {tSample}.
        retVal = _derivFcn.evaluateDerivatives(tSample, _stateVals_xx,
                _ivVals_xx, _stateDerivsSample_xx);
        if (0 != retVal) {
            throw new Exception("_derivFcn.evalDerivs() returned " + retVal);
        }

        // Update the internal, continuous state models.
        final double oneOverTwoDtSample = 0.5 / dtSample;
        for (int ii = 0; ii < _stateCt; ++ii) {
            _cStateModels[ii].coeffs[2] = oneOverTwoDtSample
                    * (_stateDerivsSample_xx[ii] - _stateDerivs_xx[ii]);
        }

        if (_evtIndCt > 0) {
            // Choose a sample time, different from {_currSimTime} and different from {tSample}.
            //   For estimating third derivatives.
            final double dtSample2 = Math.sqrt(dtSample);
            final Time tSample2 = minimumTime(
                    _currSimTime.addUnchecked(dtSample2), _quantEvtTimeMax);

            // Get values, at {tSample2}, of arguments to derivative function.
            //   Note that here, know all continuous state models have same time.
            // Therefore can use same delta-time for all evals.
            for (int ii = 0; ii < _stateCt; ++ii) {
                _stateValsSample2_xx[ii] = _cStateModels[ii].evaluate(dtSample2);
            }
            for (int ii = 0; ii < _ivCt; ++ii) {
                _ivValsSample2_xx[ii] = _ivModels[ii].evaluate(tSample2);
            }
            // Choose a sample time, different from {_currSimTime} and different from {tSample2}.
            //   For estimating third derivatives.
            final double dtSample3 = dtSample + dtSample2;
            final Time tSample3 = minimumTime(
                    _currSimTime.addUnchecked(dtSample3), _quantEvtTimeMax);

            // Get values, at {tSample3}, of arguments to derivative function.
            //   Note that here, know all continuous state models have same time.
            // Therefore can use same delta-time for all evals.
            for (int ii = 0; ii < _stateCt; ++ii) {
                _stateValsSample3_xx[ii] = _cStateModels[ii].evaluate(dtSample3);
            }
            for (int ii = 0; ii < _ivCt; ++ii) {
                _ivValsSample3_xx[ii] = _ivModels[ii].evaluate(dtSample3);
            }

            // Provide inputs to evaluate derivative function at {_currSimTime}.
            retVal = _derivFcn.eventIndicatorDerivativeInputs(_currSimTime,
                    _stateVals_xx, _ivVals_xx, tSample, _stateValsSample_xx,
                    _ivValsSample_xx, dtSample, tSample2, _stateValsSample2_xx,
                    _ivValsSample2_xx, dtSample2, tSample3,
                    _stateValsSample3_xx, _ivValsSample3_xx, dtSample3,
                    null, null, null, 0.0, null, null, null, 0.0,
                    getStateModelOrder());
        }
    }

    /**
     * Form new internal, continuous state models (QSS-specific).
     */
    protected final void _triggerRateEventWorkerEventDetection()
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
        Time tStateModel = null;
        double dtStateModel = 0;
        for (int ii = 0; ii < _stateCt; ++ii) {
            final ModelPolynomial cStateModel = _cStateModels[ii];
            // Check for different model time.  Note testing object identity OK.
            if (cStateModel.tModel != tStateModel) {
                tStateModel = cStateModel.tModel;
                dtStateModel = _currSimTime.subtractToDouble(tStateModel);
            }
            _stateVals_xx[ii] = cStateModel.evaluate(dtStateModel);
        }
        // Initialize dtSample
        double dtSample[] = { 0.0, 0.0, 0.0 };

        // Evaluate derivative function at {_currSimTime}.
        int retVal = _derivFcn.evaluateDerivatives(_currSimTime, dtSample,
                _stateDerivs_xx, _stateDerivs2_xx, null, getStateModelOrder());

        if (0 != retVal) {
            throw new Exception("_derivFcn.evalDerivs() returned " + retVal);
        }

        // Update the internal, continuous state models.
        //   Note this is a partial update, since don't yet know the second
        // derivatives.  Do the update now so that, when find the sample point
        // needed to estimate second derivatives, will be able to use as much
        // current information about the continuous state as possible.
        //   This also updates the rate model, which is just the derivative of
        // the state model.
        for (int ii = 0; ii < _stateCt; ++ii) {
            final ModelPolynomial cStateModel = _cStateModels[ii];
            cStateModel.tModel = _currSimTime;
            cStateModel.coeffs[0] = _stateVals_xx[ii];
            cStateModel.coeffs[1] = _stateDerivs_xx[ii];
            cStateModel.coeffs[2] = 0;
        }

        // Update the internal, continuous state models.
        final double oneOverTwoDtSample = 0.5 / dtSample[1];
        for (int ii = 0; ii < _stateCt; ++ii) {
            _cStateModels[ii].coeffs[2] = oneOverTwoDtSample
                    * (_stateDerivs2_xx[ii] - _stateDerivs_xx[ii]);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables

    // Memory for diagonalized state model.
    private double[] _jacDiags;
    private ModelPolynomial[] _inputTermModels;
    private double[] _qStateModelDiffs; // Difference between previous and current
    // quantized state models, at the time current one was formed.

    // Memory for finding predicted quantization-event time.
    private double[] _cStatesLastQevt; // Value of continuous state when last formed quantized state model.

    // Scratch memory.
    //   For local (per-method) calculations, not inter-method communication.
    // When a method returns, it should be possible to write random values to
    // this memory, without losing any information about the solver state.
    //   Thus, if the integrator got serialized (marshalled) to disk, this
    // memory could be ignored.
    private double[] _stateVals_xx;
    private double[] _stateDerivs_xx;
    private double[] _stateDerivs2_xx;
    private double[] _stateDerivsSample_xx;
    private double[] _ivVals_xx;
    private double[] _stateValsSample_xx;
    private double[] _ivValsSample_xx;
    private double[] _stateValsSample2_xx;
    private double[] _ivValsSample2_xx;
    private double[] _stateValsSample3_xx;
    private double[] _ivValsSample3_xx;

}

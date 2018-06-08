/*
 Copyright (c) 1998-2018 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
The class which convert input particles to covariance matrix.

<p> This class calculates covariance matrix of input particles.
    Assuming each particle is a record(x, y, vx, vy, weight), this class calculate
    average of (x, y) and following 2x2 matrix,
    (Cxx, Cxy)
    (Cyx, Cyy),
    where Cxx and Cyy are variance of x and y, respectively,
    and Cxy and Cyx are covariance between x and y (Cxy = Cyx).
</p>

@author Shuhei Emoto
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (shuhei)
@Pt.AcceptedRating
 */
public class ConvertParticlesToCovarianceMatrix extends TypedAtomicActor {

    /** Construct the actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     *
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public ConvertParticlesToCovarianceMatrix(CompositeEntity container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        ArrayToken names = new ArrayToken(
                "{\"x\",\"y\",\"vx\",\"vy\",\"weight\"}");
        String stateName;
        _labels = new String[names.length()];
        _types = new Type[names.length()];
        for (int i = 0; i < names.length(); i++) {
            stateName = ((StringToken) names.getElement(i)).stringValue();
            _labels[i] = stateName;
            _types[i] = BaseType.DOUBLE; // preset to be double
        }

        // initialize input port.
        particleInput = new TypedIOPort(this, "particles", true, false);
        particleInput.setMultiport(true);
        particleInput
                .setTypeEquals(new ArrayType(new RecordType(_labels, _types)));

        // initialize output port.
        covarianceOfStates = new TypedIOPort(this, "covarianceMatrix", false,
                true);
        covarianceOfStates.setMultiport(true);
        covarianceOfStates.setTypeEquals(BaseType.DOUBLE_MATRIX);
    }

    /**
     * Input port for particles.
     */
    public TypedIOPort particleInput;

    /**
     * Output port for covariance matrix.
     */
    public TypedIOPort covarianceOfStates;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        for (int ch = 0; ch < particleInput.getWidth(); ch++) {
            /// parsing input
            if (particleInput.hasToken(ch)) {
                ArrayToken incoming = (ArrayToken) particleInput.get(ch);
                _particles = new double[incoming.length()][5];
                for (int i = 0; i < _particles.length; i++) {
                    RecordToken inputParticle = (RecordToken) incoming
                            .getElement(i);
                    _particles[i][0] = ((DoubleToken) inputParticle.get("x"))
                            .doubleValue();
                    _particles[i][1] = ((DoubleToken) inputParticle.get("y"))
                            .doubleValue();
                    _particles[i][2] = ((DoubleToken) inputParticle.get("vx"))
                            .doubleValue();
                    _particles[i][3] = ((DoubleToken) inputParticle.get("vy"))
                            .doubleValue();
                    _particles[i][4] = ((DoubleToken) inputParticle
                            .get("weight")).doubleValue();
                }
            }
            //calculate average of particles
            _meanState = new double[4];
            for (int i = 0; i < _particles.length; i++) {
                for (int j = 0; j < _meanState.length; j++) {
                    _meanState[j] += _particles[i][j] * _particles[i][4];
                }
            }

            //calculate covariance of current state.
            _covariance = new double[4][4];
            for (int i = 0; i < _particles.length; i++) {
                double[] diff = new double[4];
                for (int it = 0; it < _meanState.length; it++) {
                    diff[it] = _particles[i][it] - _meanState[it];
                }
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        _covariance[row][col] += (diff[row] * diff[col]
                                * _particles[i][4]);
                    }
                }
            }
            covarianceOfStates.send(ch, new DoubleMatrixToken(_covariance));
        }
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        if (particleInput.hasToken(0)) {
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double[][] _particles;
    private double[] _meanState;
    private double[][] _covariance;
    private String[] _labels;
    private Type[] _types;
}

package org.ptolemy.machineLearning.particleFilter;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/* Black-box optimizer class - to be modified

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

/**
The class for calculation of constraints of Swarm-Robots.

<p> Calculates various constraints required by Robots (Ex: Maximum Speed, Distance between each other).

@author Shuhei Emoto
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (shuhei)
@Pt.AcceptedRating Red (shuhei)

 */

public class ConstraintsCalculator extends TypedAtomicActor {
    public ConstraintsCalculator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub

        // an array of control value of robots
        xValue = new TypedIOPort(this, "xValue", true, false);
        xValue.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // an array of robot locations
        locations = new TypedIOPort(this, "locations", true, false);

        //estimated position of target
        targetLocation = new TypedIOPort(this, "targetLocation", true, false);

        ArrayToken names = new ArrayToken("{\"x\",\"y\"}"); //
        String stateName;
        _labels = new String[names.length()];
        _types = new Type[names.length()];
        for (int i = 0; i < names.length(); i++) {
            stateName = ((StringToken) names.getElement(i)).stringValue();
            _labels[i] = stateName;
            _types[i] = BaseType.DOUBLE; // preset to be double
        }
        targetLocation.setTypeEquals(new RecordType(_labels, _types));

        minDistance = new TypedIOPort(this, "minDistance", false, true);
        minDistance.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        distToTarget = new TypedIOPort(this, "distToTarget", false, true);
        distToTarget.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        speed = new TypedIOPort(this, "speed", false, true);
        speed.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        /// parsing of input
        if (xValue.hasToken(0)) {
            ArrayToken xArray = ((ArrayToken) xValue.get(0));
            _xValue = new double[xArray.length()];
            for (int i = 0; i < xArray.length(); i++) {
                _xValue[i] = ((DoubleToken) xArray.getElement(i)).doubleValue();
            }
        }

        if (targetLocation.hasToken(0)) {
            RecordToken incoming = (RecordToken) (targetLocation.get(0));
            _targetX = new double[1];
            _targetY = new double[1];
            _targetX[0] = ((DoubleToken) incoming.get(_labels[0]))
                    .doubleValue();
            _targetY[0] = ((DoubleToken) incoming.get(_labels[1]))
                    .doubleValue();
        }

        if (locations.hasToken(0)) {
            ArrayToken robotLocations = (ArrayToken) locations.get(0);
            _robotX = new double[robotLocations.length()];
            _robotY = new double[robotLocations.length()];
            for (int i = 0; i < robotLocations.length(); i++) {
                RecordToken robotLocation = (RecordToken) robotLocations
                        .getElement(i);
                _robotX[i] = ((DoubleToken) robotLocation.get(_labels[0]))
                        .doubleValue() + _xValue[i * 2] * 0.1;
                _robotY[i] = ((DoubleToken) robotLocation.get(_labels[1]))
                        .doubleValue() + _xValue[i * 2 + 1] * 0.1;
            }
        }

        funcConstraints();
        minDistance.send(0, new ArrayToken(_minimum_distances));
        distToTarget.send(0, new ArrayToken(_dist_to_target));
        speed.send(0, new ArrayToken(_speed));

    }

    /**
     * Minimum distance to the robot team.
     */
    public TypedIOPort minDistance;
    
    /**
     * Distance to target.
     */
    public TypedIOPort distToTarget;
    
    /**
     * Current speed.
     */
    public TypedIOPort speed;
    /**
     * Particles input that accepts an array of record tokens. One field of the record must be labeled as "weight".
     * Other fields will be resolved to state variables.
     */
    public TypedIOPort targetLocation;
    public TypedIOPort locations;
    public TypedIOPort xValue;

    // code for computing the constraints between robots and target
    private void funcConstraints() {
        // constraints about distance between robots.
        // For each robots, this method compute the minimum distance to the others.
        _minimum_distances = new DoubleToken[_robotX.length];
        _dist_to_target = new DoubleToken[_robotX.length];
        _speed = new DoubleToken[_robotX.length];
        for (int n = 0; n < _minimum_distances.length; n++) {
            double minimum_distance = -1;
            for (int m = 0; m < _robotX.length; m++) {
                if (n == m) {
                    continue;
                }
                double dx = _robotX[m] - _robotX[n];
                double dy = _robotY[m] - _robotY[n];
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (minimum_distance < 0 || minimum_distance > distance) {
                    minimum_distance = distance;
                }
            }
            {
                double dx = _targetX[0] - _robotX[n];
                double dy = _targetY[0] - _robotY[n];
                double distance = Math.sqrt(dx * dx + dy * dy);
                _dist_to_target[n] = new DoubleToken(distance);
                if (minimum_distance < 0 || minimum_distance > distance) {
                    minimum_distance = distance;
                }
            }
            _minimum_distances[n] = new DoubleToken(minimum_distance);
            _speed[n] = new DoubleToken(Math.sqrt(_xValue[n * 2]
                    * _xValue[n * 2] + _xValue[n * 2 + 1] * _xValue[n * 2 + 1]));
        }
    }

    DoubleToken[] _minimum_distances; //minimum distances calculated by funcConstraints
    DoubleToken[] _dist_to_target; //distances to target calculated by funcConstraints
    DoubleToken[] _speed; //speeds of each robots calculated by funcConstraints
    private double[] _targetX;
    private double[] _targetY;
    private double[] _robotX;
    private double[] _robotY;
    private double[] _xValue;
    private String[] _labels;
    private Type[] _types;
}

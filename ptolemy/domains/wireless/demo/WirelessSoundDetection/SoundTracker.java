/* A sound tracker that uses triangulation to identify the origin of a sound.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.demo.WirelessSoundDetection;


import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SoundTracker
/**
FIXME

@author TODO: Philip Baldwin, Xioajun Liu, Edward A. Lee
@version $Id$
*/

public class SoundTracker extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SoundTracker(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new TypedIOPort (this, "input", true, false);
        // FIXME: Set type?
        //TypeAttribute inputType = new TypeAttribute(input, "type");
        //inputType.setExpression("{location=[double], time=double}");

        outputX = new TypedIOPort (this, "outputX", false, true);
        outputX.setTypeEquals(BaseType.DOUBLE);

        outputY = new TypedIOPort (this, "outputY", false, true);
        outputY.setTypeEquals(BaseType.DOUBLE);

        // Create parameters.
        signalPropagationSpeed = new Parameter(this, "signalPropagationSpeed");
        signalPropagationSpeed.setToken("344.0");
        signalPropagationSpeed.setTypeEquals(BaseType.DOUBLE);

        timeWindow = new Parameter(this, "timeWindow");
        timeWindow.setToken("0.5");
        timeWindow.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** TODO: Describe port and its type constraints.
     */
    public TypedIOPort input;

    /** The output producing the X coordinate of the sound source.
     *  This has type double.
     */
    public TypedIOPort outputX;

    /** The output producing the Y coordinate of the sound source.
     *  This has type double.
     */
    public TypedIOPort outputY;

    /** Speed of propagation of the signal to be used for triangulation.
     *  This is a double that defaults to 344.0 (the speed of sound in
     *  air at room temperature, in meters per second).
     */
    public Parameter signalPropagationSpeed;

    /** Time window within which observations are assumed to come from
     *  the same sound event.
     *  This is a double that defaults to 0.5 (in seconds).
     */
    public Parameter timeWindow;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read an input token and attempt to use it to triangulate
     *  the signal source. If the attempt is successful, then output
     *  the location of the signal source. Otherwise, output nothing.
     *  This method keeps a buffer of the three most recently seen
     *  inputs If the new input token has a location field that matches
     *  a location already in the buffer, then the new input simply
     *  replaces that previous observation.  Otherwise, it replaces
     *  the oldest observation in the buffer.  If there are three
     *  observations in the buffer with time stamps within the
     *  <i>timeWindow</i> parameter value, then triangulation is
     *  performed, and if the the three observations are consistent,
     *  then an output is produced.
     *  @exception IllegalActionException TODO: Describe when this is thrown.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        while (input.hasToken(0)) {
            RecordToken recordToken = (RecordToken)input.get(0);
            
            DoubleMatrixToken locationMatrix = (DoubleMatrixToken)recordToken.get("location");
            double time = ((DoubleToken)recordToken.get("time")).doubleValue();
            double locationX = locationMatrix.getElementAt(0,0);
            double locationY = locationMatrix.getElementAt(0,1);
            
            // First check whether the location matches one already in the
            // buffer.  At the same time, identify the entry with the
            // oldest time and the newest time.
            boolean foundMatch = false;
            int oldestTimeIndex = 0;
            double oldestTime = Double.POSITIVE_INFINITY;
            double newestTime = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 3; i++) {
                if (_locationsX[i] == locationX && _locationsY[i] == locationY) {
                    _times[i] = time;
                    foundMatch = true;
                }
                if (_times[i] < oldestTime) {
                    oldestTime = _times[i];
                    oldestTimeIndex = i;
                }
                if (_times[i] > newestTime) {
                    newestTime = _times[i];
                }
            }
            if (!foundMatch) {
                _locationsX[oldestTimeIndex] = locationX;
                _locationsY[oldestTimeIndex] = locationY;
                _times[oldestTimeIndex] = time;

                // Have to recalculate the oldest time now
                // since it has changed.
                oldestTime = Double.POSITIVE_INFINITY;
                for (int i = 0; i < 3; i++) {
                    if (_times[i] < oldestTime) {
                        oldestTime = _times[i];
                    }                    
                }                
            }
            
            // Next check whether we have three observations within
            // the specified time window.  Since the time entries are
            // all initialized to negative infinity, the time span
            // will be infinity if we have not seen three obsersations
            // from three distinct locations.
            double timeSpan = newestTime - oldestTime;
            double timeWindowValue
                    = ((DoubleToken)timeWindow.getToken()).doubleValue();
            if (timeSpan > timeWindowValue) {
                // We do not have enough data.
                return;
            }

            // Get signal speed, from the signalPropagationSpeed parameter.
            double speed = ((DoubleToken)(signalPropagationSpeed.getToken())).doubleValue();
            // FIXME: Pass in the arrays for scalability.
            // FIXME: Replace naked 3 everywhere.
            double[] result = _locate(
                    _locationsX[0], 
                    _locationsY[0], 
                    _times[0], 
                    _locationsX[1], 
                    _locationsY[1],
                    _times[1],
                    _locationsX[2], 
                    _locationsY[2], 
                    _times[2], 
                    speed);
                    
            // FIXME
            System.out.println("source at " + result[0] + ", " + result[1] + ", at time " + result[2]);
                
            if (Double.isInfinite(result[2]) || Double.isNaN(result[2])) {
                // Result is not valid (inconsistent data).
                return;
            } 
        
            outputX.send(0, new DoubleToken(result[0]));
            outputY.send(0, new DoubleToken(result[1]));
        }
    }

    /** Override the base class to initialize the signal count.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        for (int i = 0; i < 3; i++) {
            _locationsX[i] = Double.NEGATIVE_INFINITY;
            _locationsY[i] = Double.NEGATIVE_INFINITY;
            _times[i] = Double.NEGATIVE_INFINITY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private methods                        ////

    //  Check whether the calculated location and time of a sound event is
    //  consistent with the observations.
    // @param result The calculated location and time of a sound event.
    // @param x1 The x-coordinate of the first observation.
    // @param y1 The y-coordinate of the first observation.
    // @param t1 The time of the first observation.
    // @param x2 The x-coordinate of the second observation.
    // @param y2 The y-coordinate of the second observation.
    // @param t2 The time of the second observation.
    // @param x3 The x-coordinate of the third observation.
    // @param y3 The y-coordinate of the third observation.
    // @param t3 The time of the third observation.
    // @param v The speed of sound propagation.
    // @return True if the calculated location and time is consistent with the
    //  observations.
    private static boolean _checkResult(double[] result,
            double x1, double y1, double t1, 
            double x2, double y2, double t2, 
            double x3, double y3, double t3,
            double v) {
        if (result[2] > t1 || result[2] > t2 || result[2] > t3) {
            return false;
        }
        double tdiff1 =
                Math.abs(_distance(x1, y1, result[0], result[1])/v - (t1 - result[2]));
        double tdiff2 =
                Math.abs(_distance(x2, y2, result[0], result[1])/v - (t2 - result[2]));
        double tdiff3 = 
                Math.abs(_distance(x3, y3, result[0], result[1])/v - (t3 - result[2]));
        //TODO: make the check threshold a parameter?
        if (tdiff1 > 1e-5 || tdiff2 > 1e-5 || tdiff3 > 1e-5) {
            return false;
        } else {
            return true;
        }
    }
    
    /** Return the Cartesian distance between (x1, y1) and (x2, y2).
     *  @param x1 The first x coordinate.
     *  @param y1 The first y coordinate.
     *  @param x2 The second x coordinate.
     *  @param y2 The second y coordinate.
     *  @return The distance.
     */
    private static double _distance(
            double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /** Calculate the location and time of a sound event from the given
     *  observations of the event. In the returned array, the first element
     *  is the x-coordinate of the event, the second is the y-coordinate,
     *  and the third is the time of the event. These are obtained by
     *  solving the following equations:
     *  <pre>
     *  distance(x, y, x1, y1)/v = t1 - t
     *  distance(x, y, x2, y2)/v = t2 - t
     *  distance(x, y, x3, y3)/v = t3 - t
     *  </pre>
     *  If there is no valid solution to these equations, possible when the
     *  observations are of different sound events, the returned values
     *  are all negative infinity (Double.NEGATIVE_INFINITY). It is also
     *  possible that a valid solution to the equations exists even though
     *  the observations are of different sound events. This algorithm
     *  does not rule out such false positives.
     *
     * @param x1 The x-coordinate of the first observation.
     * @param y1 The y-coordinate of the first observation.
     * @param t1 The time of the first observation.
     * @param x2 The x-coordinate of the second observation.
     * @param y2 The y-coordinate of the second observation.
     * @param t2 The time of the second observation.
     * @param x3 The x-coordinate of the third observation.
     * @param y3 The y-coordinate of the third observation.
     * @param t3 The time of the third observation.
     * @param v The speed of sound propagation.
     * @return The location and time of the sound event consistent with
     *  the observations.
     */
    private static double[] _locate(
            double x1, double y1, double t1,
            double x2, double y2, double t2,
            double x3, double y3, double t3,
            double v) {
        double[] result = new double[3];
        double v2 = v * v;
        double[][] m = {
            { 2 * (x2 - x1), 2 * (y2 - y1) },
            { 2 * (x3 - x1), 2 * (y3 - y1) }
        };
        double[] b = { 2 * v2 * (t2 - t1), 2 * v2 * (t3 - t1) };
        double[] c = {
            t1 * t1 * v2 - t2 * t2 * v2 + x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1,
            t1 * t1 * v2 - t3 * t3 * v2 + x3 * x3 - x1 * x1 + y3 * y3 - y1 * y1
        };
        // FIXME: what if det_m is 0? That is, the three sensors are located on
        // a straight line.
        double det_m = m[0][0] * m[1][1] - m[1][0] * m[0][1];
        double[][] m_inv = {
            { m[1][1] / det_m, -m[0][1] / det_m },
            { -m[1][0] / det_m, m[0][0] / det_m }
        };
        double[] m_inv_b = {
            m_inv[0][0] * b[0] + m_inv[0][1] * b[1],
            m_inv[1][0] * b[0] + m_inv[1][1] * b[1]
        };
        double[] m_inv_c = {
            m_inv[0][0] * c[0] + m_inv[0][1] * c[1],
            m_inv[1][0] * c[0] + m_inv[1][1] * c[1]
        };
        double ea = m_inv_b[0] * m_inv_b[0] + m_inv_b[1] * m_inv_b[1] - v2;
        double eb = 2 * m_inv_b[0] * (m_inv_c[0] - x1)
                + 2 * m_inv_b[1] * (m_inv_c[1] - y1) + 2 * v2 * t1;
        double ec = (m_inv_c[0] - x1) * (m_inv_c[0] - x1)
                + (m_inv_c[1] - y1) * (m_inv_c[1] - y1) - t1 * t1 * v2;
        double delta = eb * eb - 4 * ea * ec;
        //System.out.println("delta is " + delta);
        if (delta >= 0) {
            result[2] = (-eb + Math.sqrt(delta)) / ea / 2;
            result[0] = m_inv_b[0] * result[2] + m_inv_c[0];
            result[1] = m_inv_b[1] * result[2] + m_inv_c[1];
            if (_checkResult(result, x1, y1, t1, x2, y2, t2, x3, y3, t3, v)) {
                return result;
            } else {
                result[2] = (-eb - Math.sqrt(delta)) / ea / 2;
                result[0] = m_inv_b[0] * result[2] + m_inv_c[0];
                result[1] = m_inv_b[1] * result[2] + m_inv_c[1];
                if (_checkResult(result, x1, y1, t1, x2, y2, t2, x3, y3, t3, v)) {
                    return result;
                } else {
                    result[0] = Double.NEGATIVE_INFINITY;
                    result[1] = Double.NEGATIVE_INFINITY;
                    result[2] = Double.NEGATIVE_INFINITY;
                    return result;
                }
            }
        } else {
            result[2] = -eb / ea / 2;
            result[0] = m_inv_b[0] * result[2] + m_inv_c[0];
            result[1] = m_inv_b[1] * result[2] + m_inv_c[1];
            if (_checkResult(result, x1, y1, t1, x2, y2, t2, x3, y3, t3, v)) {
                return result;
            } else {
                result[0] = Double.NEGATIVE_INFINITY;
                result[1] = Double.NEGATIVE_INFINITY;
                result[2] = Double.NEGATIVE_INFINITY;
                return result;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // Buffer of three readings.
    private double[] _locationsX = new double[3];
    private double[] _locationsY = new double[3];
    private double[] _times = new double[3];
}

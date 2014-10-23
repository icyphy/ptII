/* An actor that provides terrain properties.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.domains.wireless.lib;

import java.awt.Polygon;
import java.awt.Shape;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.FilledShapeAttribute;

///////////////////////////////////////////////////////////////////
//// TerrainProperty

/**
 This actor models an obstacle that attenuates signals that
 traverse it. The obstacle has a geometry given by the <i>xyPoints</i>
 parameter. By default, the attenuation is infinite, meaning
 that the obstacle completely blocks a signal. By increasing
 the <i>attenuationDepth</i> parameter you can specify partial
 attenuation that depends on the geometry of the obstacle.
 If <i>attenuationDepth</i> has value <i>x</i> and the signal
 traverses distance <i>y</i> through the obstacle, then the
 power of the signal is reduced by a factor of
 <pre>
   exp(log(0.5) * y / x)
 </pre>
 Thus, if <i>x</i> = <i>y</i>, then the power is reduced to
 half (3dB).  The algorithm used here only works for convex
 obstacle shapes.
 <p>
 This actor implements the PropertyTransformer interface.
 It register itself with the wireless channel specified by
 the <i>channelName</i> parameter. The channel may call it
 getProperty() method to get the property.

 @author Yang Zhao and William Douglas (contributor: Edward A. Lee)
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (pjb2e)
 */
public class TerrainProperty extends TypedAtomicActor implements
PropertyTransformer {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TerrainProperty(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        channelName = new StringParameter(this, "channelName");
        channelName.setExpression("TerrainChannel");

        xyPoints = new Parameter(this, "xyPoints");
        xyPoints.setExpression("{{0, 0}, {0, 5}, {20, 5}, {20, 0}}");

        attenuationDepth = new Parameter(this, "attenuationDepth");
        attenuationDepth.setExpression("0.0");
        attenuationDepth.setTypeEquals(BaseType.DOUBLE);

        //create the default icon.
        _numberOfPoints = 4;
        _xPoints = new int[_numberOfPoints];
        _yPoints = new int[_numberOfPoints];
        _xPoints[0] = 0;
        _yPoints[0] = 0;
        _xPoints[1] = 0;
        _yPoints[1] = 5;
        _xPoints[2] = 20;
        _yPoints[2] = 5;
        _xPoints[3] = 20;
        _yPoints[3] = 0;

        //Create the icon.
        _icon = new EditorIcon(this, "_icon");
        _terrain = new FilledShapeAttribute(_icon, "terrain") {
            @Override
            protected Shape _newShape() {
                return new Polygon(_xPoints, _yPoints, _numberOfPoints);
            }
        };

        // Set the color to green.
        _terrain.fillColor.setToken("{0.0, 1.0, 0.0, 1.0}");

        // NOTE: The width is not used, but this triggers a
        // call to _newShape().
        _terrain.width.setToken(new IntToken(10));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the channel.  The default name is "TerrainChannel".
     */
    public StringParameter channelName;

    /** The width of material that will attenuate a signal power by
     *  50% (3dB).  This has the same units as the xyPoints parameter,
     *  which specifies the geometry of the obstacle.
     *  The default value is 0.0, which means infinite attenuation
     *  (no signal gets through).
     */
    public Parameter attenuationDepth;

    /** The x/y coordinates of the obstacle. This is an array
     *  of integers specifying a geometry. The icon will have
     *  the same geometry.
     *  The default value is an array of integer pairs:
     *  {{0, 0}, {0, 5}, {20, 5}, {20, 0}}
     */
    public Parameter xyPoints;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to parse the model specified if the
     *  attribute is modelFileOrURL.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xyPoints) {
            Token xypoints = xyPoints.getToken();

            if (xypoints instanceof ArrayToken) {
                ArrayToken xypointsArray = (ArrayToken) xypoints;

                if (xypointsArray.length() != _numberOfPoints) {
                    _numberOfPoints = xypointsArray.length();
                    _xPoints = new int[_numberOfPoints];
                    _yPoints = new int[_numberOfPoints];
                }

                for (int i = 0; i < xypointsArray.length(); i++) {
                    ArrayToken xypointArray = (ArrayToken) xypointsArray
                            .getElement(i);
                    _xPoints[i] = ((IntToken) xypointArray.getElement(0))
                            .intValue();
                    _yPoints[i] = ((IntToken) xypointArray.getElement(1))
                            .intValue();
                }

                _number++;

                //set the width different to trigger the shape change...
                _terrain.width.setToken(new IntToken(_number));
            } else {
                throw new IllegalActionException(this,
                        "xPoints is required to be an integer array");
            }
        } else if (attribute == attenuationDepth) {
            _attenuation = ((DoubleToken) attenuationDepth.getToken())
                    .doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TerrainProperty newObject = (TerrainProperty) super.clone(workspace);
        ArrayToken xypointsArray;
        try {
            xypointsArray = (ArrayToken) newObject.xyPoints.getToken();
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException(e.toString());
        }
        newObject._xPoints = new int[newObject._numberOfPoints];
        newObject._yPoints = new int[newObject._numberOfPoints];
        for (int i = 0; i < xypointsArray.length(); i++) {
            ArrayToken xypointArray = (ArrayToken) xypointsArray.getElement(i);
            newObject._xPoints[i] = ((IntToken) xypointArray.getElement(0))
                    .intValue();
            newObject._yPoints[i] = ((IntToken) xypointArray.getElement(1))
                    .intValue();
        }

        newObject._icon = (EditorIcon) newObject.getAttribute("_icon");
        newObject._terrain = (FilledShapeAttribute) newObject
                .getAttribute("terrain");
        return newObject;
    }

    /** Register PropertyTransformers with the Channel.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _terrain.width.setToken(new IntToken(10));
        _number = 10;
        _offset = new double[2];

        Locatable location = (Locatable) getAttribute(LOCATION_ATTRIBUTE_NAME,
                Locatable.class);

        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for entity " + getName() + ".");
        }

        double[] center = _polygonCenter();

        //Note: the polygon is not centered, but the location
        //refers to the center of the polygon. We adjust the
        //offset here.
        _offset[0] = location.getLocation()[0] + center[0];
        _offset[1] = location.getLocation()[1] + center[1];

        CompositeEntity container = (CompositeEntity) getContainer();
        _channelName = channelName.stringValue();

        Entity channel = container.getEntity(_channelName);

        if (channel instanceof WirelessChannel) {
            _channel = (WirelessChannel) channel;
            ((WirelessChannel) channel).registerPropertyTransformer(this, null);
        } else {
            throw new IllegalActionException(this,
                    "The channel name does not refer to a valid channel.");
        }
    }

    /** Check whether the path between the sender and receiver is
     *  intersected with the terrain shape. If yes, set the "power" field
     *  in the property to be zero, otherwise, do nothing.
     * @param properties The transform properties.
     * @param sender The sending port.
     * @param destination The receiving port.
     * @return The modified transform properties.
     * @exception IllegalActionException If failed to execute the model.
     */
    @Override
    public RecordToken transformProperties(RecordToken properties,
            WirelessIOPort sender, WirelessIOPort destination)
                    throws IllegalActionException {

        RecordToken newProperties = properties;
        double[] p1 = _locationOf(sender);
        double[] p2 = _locationOf(destination);
        double depth = _polygonDepthBetweenPorts(p1, p2);

        if (depth > 0.0) {
            Token transmitPower = properties.get("power");
            double currentPower = ((DoubleToken) transmitPower).doubleValue();
            double multiplier = 0.0;
            // Avoid divide by zero, though in theory it's OK.
            if (_attenuation > 0.0) {
                multiplier = Math.exp(Math.log(0.5) / _attenuation * depth);
            }

            // Create a record token with the receive power.
            String[] names = { "power" };
            Token[] values = { new DoubleToken(currentPower * multiplier) };
            RecordToken newPower = new RecordToken(names, values);

            // Merge the receive power into the merged token.
            newProperties = RecordToken.merge(newPower, properties);
        }
        return newProperties;
    }

    /** Override the base class to call wrap up to unregister this with the
     *  channel.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (_channel != null) {
            _channel.unregisterPropertyTransformer(this, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the location of the given WirelessIOPort.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @exception IllegalActionException If a valid location attribute cannot
     *   be found.
     */
    private double[] _locationOf(WirelessIOPort port)
            throws IllegalActionException {
        Entity container = (Entity) port.getContainer();
        Locatable location = null;
        location = (Locatable) container.getAttribute(LOCATION_ATTRIBUTE_NAME,
                Locatable.class);

        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port " + port.getName()
                    + ".");
        }

        return location.getLocation();
    }

    /** Return the center coordinates of the polygon icon.
     *  @return The location of the polygon center.
     */
    private double[] _polygonCenter() {
        double xMax = Double.NEGATIVE_INFINITY;
        double xMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double[] center = new double[2];

        // First, read vertex values and find the bounds.
        for (int j = 0; j < _numberOfPoints; j++) {
            if (_xPoints[j] > xMax) {
                xMax = _xPoints[j];
            }

            if (_xPoints[j] < xMin) {
                xMin = _xPoints[j];
            }

            if (_yPoints[j] > yMax) {
                yMax = _yPoints[j];
            }

            if (_yPoints[j] < yMin) {
                yMin = _yPoints[j];
            }
        }

        center[0] = (xMin - xMax) / 2;
        center[1] = (yMin - yMax) / 2;

        return center;
    }

    /** Return the depth of the terrain that resides directly between the two
     *  points. The order of the points is not important.
     *  @param p1 The first reference point.
     *  @param p2 The second reference point.
     *  @return The depth of the terrain object between the supplied points.
     */
    private double _polygonDepthBetweenPorts(double[] p1, double[] p2) {

        int state = 0;
        double[] point = new double[2];

        // To find the points where the terrain is between the two ports, we
        // iterate over the sides of the terrain looking for sides that
        // intersect the two ports.  When two such points are found we can
        // calculate the depth.  If two points are not found then the terrain
        // is not between the two ports.

        for (int j = 0; j < _numberOfPoints; j++) {

            int k = (j + 1) % _numberOfPoints;
            double[] a = { _xPoints[j] + _offset[0], _yPoints[j] + _offset[1] };
            double[] b = { _xPoints[k] + _offset[0], _yPoints[k] + _offset[1] };

            // Check to see if the side of the polygon is between the
            // two ports.

            try {
                double[] p = _lineSegmentIntersectionPoint(p1, p2, a, b);
                if (state++ == 0) {
                    point = p;
                } else {
                    // Now that we found two points, return the depth.
                    return Math.sqrt(Math.pow(p[0] - point[0], 2)
                            + Math.pow(p[1] - point[1], 2));
                }
            } catch (IllegalActionException e) {
                // No intersection. Try the next side.
            }
        }

        // Return a default value of 0.0 indicating that the terrain is not
        // inbetween the two ports.
        return 0.0;
    }

    /** Return the point where two lines segments intersect.  The two line
     *  segments are identified by their start and end points.  Should the lines
     *  not intersect, an IllegalActionException is thrown.
     *  @param a1 Endpoint on the first line segment.
     *  @param a2 Endpoint on the first line segment.
     *  @param b1 Endpoint on the second line segment.
     *  @param b2 Endpoint on the second line segment.
     *  @return Location of the intersection point.
     *  @exception IllegalActionException If the line segments do not intersect
     *  or if the math results in a determinate of 0.
     */
    private double[] _lineSegmentIntersectionPoint(double[] a1, double[] a2,
            double[] b1, double[] b2) throws IllegalActionException {
        try {
            // Calculate the intersection point of the two lines.  This
            // algorithm was found on the Mathematica web site.

            double A1 = a2[1] - a1[1];
            double B1 = a1[0] - a2[0];
            double C1 = A1 * a1[0] + B1 * a1[1];
            double A2 = b2[1] - b1[1];
            double B2 = b1[0] - b2[0];
            double C2 = A2 * b1[0] + B2 * b1[1];

            double det = A1 * B2 - A2 * B1;

            double[] p = new double[2];
            p[0] = (B2 * C1 - B1 * C2) / det;
            p[1] = (A1 * C2 - A2 * C1) / det;

            // We have the intersection point of the two lines.  Now we make
            // sure that this point is present on both line segments.

            // The value 0.00001 was chosen because of rounding errors in
            // the double values.  It is close enough to zero to not make a
            // difference while still accounding for rounding errors.

            if ((p[0] - a1[0]) * (p[0] - a2[0]) <= 0.00001
                    && (p[1] - a1[1]) * (p[1] - a2[1]) <= 0.00001
                    && (p[0] - b1[0]) * (p[0] - b2[0]) <= 0.00001
                    && (p[1] - b1[1]) * (p[1] - b2[1]) <= 0.00001) {

                return p;
            }
        } catch (ArithmeticException e) {
            throw new IllegalActionException("Determinate equal to 0.");
        }

        throw new IllegalActionException("Line segments do not intersect.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private WirelessChannel _channel;

    private int[] _xPoints;

    private int[] _yPoints;

    private EditorIcon _icon;

    private FilledShapeAttribute _terrain;

    private int _numberOfPoints;

    // holds the attenuation value of the object.  The attenuation value
    // is the distance a signal can travel while only losing 50% of it's
    // strength.
    private double _attenuation;

    // this variable is merely used to set the ShapeAttribute
    // with different width, so that is can update the shape.
    private int _number;

    //the location of this actor. This is used as (0, 0) when
    //create the shape.
    private double[] _offset;

    private String _channelName;

    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}

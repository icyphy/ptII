/* An actor that provides terrain properties.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
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
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.FilledShapeAttribute;

//////////////////////////////////////////////////////////////////////////
//// TerrainProperty

/**
This actor implements the PropertyTransformer interface.
It register itself with the wireless channel specified by
the <i>channelName<i> parameter. The channel may call it
getProperty() method to get the property.

@author Yang Zhao
@version $Id$
*/
public class TerrainProperty extends TypedAtomicActor
        implements PropertyTransformer {

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
        xyPoints.setExpression("{{0,0}, {0,5}, {20,5}, {20, 0}}");
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

        //Crate the icon.
        _icon = new EditorIcon(this, "_icon");
        _terrain =
                new FilledShapeAttribute(_icon, "terrain") {
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
    public StringParameter channelName;

    public Parameter xyPoints;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Override the base class to parse the model specified if the
     *  attribue is modelFileOrURL.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
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
                for(int i=0; i<xypointsArray.length(); i++) {
                    ArrayToken xypointArray = (ArrayToken)
                            xypointsArray.getElement(i);
                    _xPoints[i]= ((IntToken)xypointArray.
                            getElement(0)).intValue();
                    _yPoints[i]= ((IntToken)xypointArray.
                            getElement(1)).intValue();
                }
                _number++;
                //set the width different to trigget the shape change...
                _terrain.width.setToken(new IntToken(_number));

            } else {
                throw new IllegalActionException (this,
                "xPoints is required to be an integer array");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }


    /** Initialize the _registeredWithChannel.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _terrain.width.setToken(new IntToken(10));
        _number = 10;
        _offset = new double[2];
        Locatable location = (Locatable)
                getAttribute(LOCATION_ATTRIBUTE_NAME, Locatable.class);
        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for entity "
                    + getName()
                    + ".");
        }
        _offset = location.getLocation();

        CompositeEntity container = (CompositeEntity) getContainer();
        _channelName = channelName.stringValue();
        Entity channel = container.getEntity(_channelName);
        if (channel instanceof WirelessChannel) {
            _channel = (WirelessChannel)channel;
            ((WirelessChannel)channel).
                    registerPropertyTransformer(this, null);
        } else {
            throw new IllegalActionException(this,
            "The channel nam" +
            "e does not refer to a valid channel.");
        }
    }

    /** Check whether the path between the sender and receiver is
     *  intersected with the terrain shape. If yes, set the "power" feild
     *  in the property to be zero, otherwise, do nothing.
     *  FIXME: should the terrain property affect the power value
     *  or should it affect the receivers in range?
     *  FIXME: check java.util to see if there is standard method to
     *  do this check...
     * @param properties The transform properties.
     * @param sender The sending port.
     * @param destination The receiving port.
     * @return The modified transform properties.
     * @exception IllegalActionException If failed to execute the model.
     */
    public RecordToken transformProperties(RecordToken properties,
            WirelessIOPort sender, WirelessIOPort destination)
            throws IllegalActionException {
        double[] p1 = _locationOf(sender);
        double[] p2 = _locationOf(destination);
        double a, b, c, d, x0, y0, x1, y1, k;
        boolean cross = false;
        for (int i = 0; i < _numberOfPoints; i++) {
            for (int j = i+1; j< _numberOfPoints; j++) {
            x0 = _xPoints[i] + _offset[0];
            y0 = _yPoints[i] + _offset[1];
            x1 = _xPoints[j] + _offset[0];
            y1 = _yPoints[j] + _offset[1];
            if (x1 - x0 != 0) {
                k = (y1-y0)/(x1-x0);
                a = p1[1]-y0-k*(p1[0]-x0);
                b=  p2[1]-y0-k*(p2[0]-x0);
            } else {
                a = p1[0]-x0;
                b = p2[0]-x0;
            }
            if(p2[0]-p1[0] != 0) {
                k = (p2[1]-p1[1])/(p2[0]-p1[0]);
                c = y0 - p1[1]-k*(x0-p1[0]);
                d = y1 - p1[1]-k*(x1-p1[0]);
            } else {
                c = x0 - p1[0];
                d = x1 - p1[0];
            }
            if (a*b<0 && c*d<0) {
                cross = true;
                break;
            }
            } //for j.
        }//for i.
        if (cross) {
            Token transmitPower = properties.get("power");

            // Create a record token with the receive power.
            String[] names = {"power"};
            Token[] values = {new DoubleToken(0.0)};
            RecordToken newPower = new RecordToken(names, values);

            // Merge the receive power into the merged token.
            RecordToken result = RecordToken.merge(newPower, properties);
            return result;
        } else {
            return properties;
        }
    }

    /** Override the base class to call wrap up to unregister this with the
     *  channel.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if(_channel != null){
            _channel.unregisterPropertyTransformer(this, null);
        }
    }
    /** Return the location of the given WirelessIOPort.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @exception IllegalActionException If a valid location attribute cannot
     *   be found.
     */
    private double[] _locationOf(WirelessIOPort port) throws IllegalActionException {
        Entity container = (Entity)port.getContainer();
        Locatable location = null;
        location = (Locatable)container.getAttribute(
                LOCATION_ATTRIBUTE_NAME, Locatable.class);
        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port "
                    + port.getName()
                    + ".");
        }
        return location.getLocation();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    private WirelessChannel _channel;
    private int[] _xPoints, _yPoints;
    private EditorIcon _icon;
    private FilledShapeAttribute _terrain;
    private int _numberOfPoints;
    // this variable is merely used to set the ShapeAttribute
    // with different width, so that is can update the shape.
    private int _number;

    //the location of this actor. This is used as (0,0) when
    //create the shape.
    private double[] _offset;

    private String _channelName;
    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}

/* An actor that displays link properties.

 Copyright (c) 2006 The Regents of the University of California.
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
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.TokenProcessor;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.FilledShapeAttribute;
import ptolemy.vergil.kernel.attributes.LineAttribute;

//////////////////////////////////////////////////////////////////////////
//// LinkVisualizer

/**
 This actor implements the TokenProcessor interface.
 It creates a line between two communicating nodes that 
 are within range of one another. It registers itself 
 with the wireless channel specified by the 
 <i>channelName</i> parameter. The default channel is 
 set to AtomicWirelessChannel. The channel may call it
 getProperty() method to get the property.

 @author Heather Taylor
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (hltaylor)
 @Pt.AcceptedRating Red (hltaylor)
 */
public class LinkVisualizer extends TypedAtomicActor implements
        TokenProcessor {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LinkVisualizer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        channelName = new StringParameter(this, "channelName");
        channelName.setExpression("AtomicWirelessChannel");

        xyPoints = new Parameter(this, "xyPoints");
        xyPoints.setExpression("{{0, 0}, {0, 5}, {20, 5}, {20, 0}}");

        //create the default icon.
        _numberOfPoints = 4;
        _xPoints = new int[_numberOfPoints];
        _yPoints = new int[_numberOfPoints];
        _xPoints[0] = 0;
        _yPoints[0] = 0;
        _xPoints[1] = 0;
        _yPoints[1] = 5;
        _xPoints[2] = 5;
        _yPoints[2] = 5;
        _xPoints[3] = 5;
        _yPoints[3] = 0;

        //Create the icon.
        _icon = new EditorIcon(this, "_icon");
        _terrain = new FilledShapeAttribute(_icon, "terrain") {
            protected Shape _newShape() {
                return new Polygon(_xPoints, _yPoints, _numberOfPoints);
            }
        };

        // Set the color to blue.
        _terrain.fillColor.setToken("{0.0, 0.0, 1.0, 1.0}");

        // NOTE: The width is not used, but this triggers a
        // call to _newShape().
        _terrain.width.setToken(new IntToken(10));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the channel.  The default name is "AtomicWirelessChannel".
     */
    public StringParameter channelName;

    /** The x/y coordinates of ? (FIXME)
     *  The default value is an array of integer pairs:
     *  {{0, 0}, {0, 5}, {20, 5}, {20, 0}}
     *  FIXME: need more info
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
 
        _isOff = true;

 


        CompositeEntity container = (CompositeEntity) getContainer();
        _channelName = channelName.stringValue();

        Entity channel = container.getEntity(_channelName);

        if (channel instanceof WirelessChannel) {
            _channel = (WirelessChannel) channel;
            //change made to below line, from registerPropertyTransformer (htaylor)
            ((WirelessChannel) channel).registerTokenProcessor(this, null);
        } else {
            throw new IllegalActionException(this,
                    "The channel name does not refer to a valid channel.");
        }
    }

    /**This method creates & removes a line between the sender and
     * the destination containers, by calling MoMLChangeRequest.
     * @param properties The transform properties.
     * @param token The token to be processed.
     * @param sender The sending port.
     * @param destination The receiving port.
     * @exception IllegalActionException If failed to execute the model.
     */
    public void processTokens(RecordToken properties,
            Token token, WirelessIOPort sender, WirelessIOPort destination)
            throws IllegalActionException {
        
       if(_isOff) {
            Location senderLocation = (Location)sender.getContainer().getAttribute("_location");
            Location destinationLocation = (Location)destination.getContainer().getAttribute("_location");
            double x = (destinationLocation.getLocation())[0] - (senderLocation.getLocation())[0];
            double y = (destinationLocation.getLocation())[1] - (senderLocation.getLocation())[1];
            String moml = "<property name=\"_senderDestLine\" class=\"ptolemy.vergil.kernel.attributes.LineAttribute\">"
                + senderLocation.exportMoML()
                + "<property name=\"x\" value=\""
                + x
                + "\"/>"
                + "<property name=\"y\" value=\""
                + y
                + "\"/>"
                + "</property>";
            ChangeRequest request = new MoMLChangeRequest(this, getContainer(), moml) {
                protected void _execute() throws Exception {
                    super._execute();
                    LineAttribute line = (LineAttribute)getContainer().getAttribute("_senderDestLine");
                    line.moveToFirst();
                    line.setPersistent(false);
                }
            };
            requestChange(request);
            _isOff = false;
       } else {
            if (getContainer().getAttribute("_senderDestLine") != null) {
                String moml = "<deleteProperty name=\"_senderDestLine\"/>";
                ChangeRequest request = new MoMLChangeRequest(this, getContainer(), moml);
                requestChange(request);
                _isOff = true;
            }
        }
        
    }

    /** Override the base class to call wrap up to unregister this with the
     *  channel.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (_channel != null) {
            _channel.unregisterTokenProcessor(this, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private WirelessChannel _channel;

    private int[] _xPoints;

    private int[] _yPoints;

    private EditorIcon _icon;

    private FilledShapeAttribute _terrain;

    private int _numberOfPoints;

    // this variable is merely used to set the ShapeAttribute
    // with different width, so that is can update the shape.
    private int _number;

    private String _channelName;
    
    //Status of radio link line
    private boolean _isOff;
}

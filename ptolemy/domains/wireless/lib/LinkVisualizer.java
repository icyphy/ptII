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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.TokenProcessor;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
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
 @Pt.ProposedRating Red (htaylor)
 @Pt.AcceptedRating Red (htaylor)
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

        EditorIcon link_icon = new EditorIcon(this, "_icon");
        
        _line1 = new LineAttribute(link_icon, "_line1");

        Location line1Loc = new Location(_line1, "_location");
        double[] line1LocVal = { -19.0, -3.0 };
        line1Loc.setLocation(line1LocVal);
        _line1.lineColor.setToken("{0.0, 0.0, 1.0, 1.0}");
        _line1.x.setToken("30.0");
        _line1.y.setToken("20.0");
        _line1.moveToFirst();
        
        _line2 = new LineAttribute(link_icon, "_line2");

        Location line2Loc = new Location(_line2, "_location");
        double[] line2LocVal = { -22.0, 2.0 };
        line2Loc.setLocation(line2LocVal);
        _line2.lineColor.setToken("{0.0, 0.0, 1.0, 1.0}");
        _line2.x.setToken("30.0");
        _line2.y.setToken("-20.0");
        _line2.moveToFirst();
        
        _ellipse1 = new EllipseAttribute(link_icon, "_ellipse1");

        Location ellipse1Loc = new Location(_ellipse1, "_location");
        double[] ellipse1LocVal = { -18.0, -2.0 };
        ellipse1Loc.setLocation(ellipse1LocVal);
        _ellipse1.fillColor.setToken("{1.0, 1.0, 0.0, 1.0}");
        _ellipse1.width.setToken("15.0");
        _ellipse1.height.setToken("15.0");
        _ellipse1.centered.setToken("true");
        
        _ellipse2 = new EllipseAttribute(link_icon, "_ellipse2");

        Location ellipse2Loc = new Location(_ellipse2, "_location");
        double[] ellipse2LocVal = { 8.0, 14.0 };
        ellipse2Loc.setLocation(ellipse2LocVal);
        _ellipse2.fillColor.setToken("{1.0, 0.0, 1.0, 1.0}");
        _ellipse2.width.setToken("15.0");
        _ellipse2.height.setToken("15.0");
        _ellipse2.centered.setToken("true");
        
        _ellipse3 = new EllipseAttribute(link_icon, "_ellipse3");

        Location ellipse3Loc = new Location(_ellipse3, "_location");
        double[] ellipse3LocVal = { 8.0, -16.0 };
        ellipse3Loc.setLocation(ellipse3LocVal);
        _ellipse3.fillColor.setToken("{1.0, 0.0, 1.0, 1.0}");
        _ellipse3.width.setToken("15.0");
        _ellipse3.height.setToken("15.0");
        _ellipse3.centered.setToken("true");
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the channel.  The default name is "AtomicWirelessChannel".
     */
    public StringParameter channelName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the _registeredWithChannel.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isOff = true;


        CompositeEntity container = (CompositeEntity) getContainer();
        _channelName = channelName.stringValue();

        Entity channel = container.getEntity(_channelName);

        if (channel instanceof WirelessChannel) {
            _channel = (WirelessChannel) channel;
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
    ////                       protected variables                 ////
    /** Status of radio link line */
    protected boolean _isOff;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private WirelessChannel _channel;
    
    /** Graphical icon for line1 */
    private LineAttribute _line1;
    
    /** Graphical icon for line1 */
    private LineAttribute _line2;
    
    /** Graphical icon for ellipse1 */
    private EllipseAttribute _ellipse1;
    
    /** Graphical icon for ellipse2 */
    private EllipseAttribute _ellipse2;
    
    /** Graphical icon for ellipse3 */
    private EllipseAttribute _ellipse3;

    private String _channelName;
}

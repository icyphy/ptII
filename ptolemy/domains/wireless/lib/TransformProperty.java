/* An actor that provides transmission properties.

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

import java.net.URL;
import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.hoc.ModelUtilities;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.wireless.kernel.AtomicWirelessChannel;
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// TransformProperty

/**
This actor implements the PropertyTransformer interface. It register itself
and its connected WirelessIOPort with the wireless channel which the 
WirelessIOPort uses. On each firing, it simply sends the token from the <i>data</i>
input<i> ports, and outputs the data on the 
<i>output</i> port. The channel may call it getProperty() method to get
the property as a RecordToken. 

This actor has a <i>modelFileOrURL<i> parameter that specify a model used 
to calculate the properties. When getProperty() is called, it calles the
ModelUtilities.executeModel() method to execute the specified model and
return the result to the channel.

@author Yang Zhao
@version $ $
*/
public class TransformProperty extends TypedAtomicActor 
        implements PropertyTransformer {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TransformProperty(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        data = new TypedIOPort(this, "data", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeSameAs(data);
        // Create and configure the parameters.
        modelFileOrURL = new FileParameter(this, "modelFileOrURL");
        //Crate the icon.
        //FIXME: create a better icon here...
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port that receives the data to be transmitted on the <i>output</i>
     *  port.
     */
    public TypedIOPort data;

    /** Port that sends data to a wireless output.
     */
    public TypedIOPort output;

    /** The file name or URL of the model that this actor invokes to 
     *  transforme property.
     */
    public FileParameter modelFileOrURL;

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
        if (attribute == modelFileOrURL) {
            // Open the file and read the MoML to create a model.
            URL url = modelFileOrURL.asURL();
            if (url != null) {
                // By specifying no workspace argument to the parser, we
                // are asking it to create a new workspace for the referenced
                // model.  This is necessary because the execution of that
                // model will proceed through its own sequences, and it
                // will need to get write permission on the workspace. (this
                // documentation is copied from ModelReference.java)
                MoMLParser parser = new MoMLParser();
                try {
                     _model = parser.parse(null, url);
                } catch (Exception ex) {
                    throw new IllegalActionException(
                        this,
                        ex,
                        "Failed to read model.");
                }
            } else {
                // URL is null... delete the current model.
                _model = null;
            }
            //Set this flag to false, so this actor will register the
            // new model with the channel.
            _registeredWithChannel = false;
        } else {
            super.attributeChanged(attribute);
        }
    }
 
    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        TransformProperty newObject = (TransformProperty)(super.clone(workspace));
        
        newObject._model = null;
        // set the type constraints
        newObject.output.setTypeSameAs(newObject.data);
        return newObject;
    }
    
    /** Read at most one token from the <i>data</i>
     *  port and simply transmit the data on the <i>output</i> port.
     *  If it has not registered with the channel, then register.
     */
    public void fire() throws IllegalActionException {

        super.fire();
        // Note that an sensor node may call the channel to register a 
        // PropertyTransformer. And the channel
        // create the HashMap from a port to its registed 
        // PropertyTransformer in its initialize(). But we have no control about whose 
        // Initialize() method will be executed first. So we do it in
        // the fire method if it has register with the channel.
        // FIXME: this might be too late...
        if (!_registeredWithChannel && _model instanceof CompositeActor ) {
            
            _executable = (CompositeActor)_model;
            Manager manager = _executable.getManager();
            if(manager == null) {
                manager = new Manager(_executable.workspace(), "Manager");
                _executable.setManager(manager);
            }
            Iterator connectedPorts = output.sinkPortList().iterator();
            while (connectedPorts.hasNext()) {
                IOPort port = (IOPort)connectedPorts.next();
                if (!port.isInput() && port instanceof WirelessIOPort) {
                    // Found the port.
                    Entity container = (Entity)(port.getContainer());
                    String channelName
                            = ((WirelessIOPort)port).outsideChannel.stringValue();
                    CompositeEntity container2 = (CompositeEntity)
                            container.getContainer();
                    if (container2 == null) {
                        throw new IllegalActionException(this,
                        "The container does not have a container.");         
                    }
                    Entity channel = container2.getEntity(channelName);
                    if (channel instanceof AtomicWirelessChannel) {
                        ((AtomicWirelessChannel)channel).
                                registerPropertyTransformer(
                                (WirelessIOPort)port, this);
                        _registeredWithChannel = true;
                    } else {
                        throw new IllegalActionException(this,
                        "The connected port does not refer to a valid channel.");
                    }
                }
            }
        }
        if (data.hasToken(0)) {
            Token inputValue = data.get(0);
            if (_debugging) {
                _debug("Input data received: " + inputValue.toString());
            }
            output.send(0, inputValue);
        }
    }
    
    /** Initialize the _registeredWithChannel.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _registeredWithChannel = false;
    }


    /** Override the base class to call stop() on the referenced model.
     */
    public void stop() {
        if(_executable != null){
            _executable.stop();
        }
        super.stop();
    }

    /* Override the base class to call stopFire() on the referenced model.
     */
    public void stopFire() {
        if(_executable != null){
            _executable.stopFire();
        }
        super.stopFire();
    }

    /** Override the base class to call terminate() on the referenced model.
     */
    public void terminate() {
        if(_executable != null){
            _executable.terminate();
        }
        super.terminate();
    }

    /** Invoke the execution of the specified model and return the result.
     * @param properties The transform properties.
     * @param sender The sending port.
     * @param destination The receiving port.
     * @return The modified transform properties.
     * @exception IllegalActionException If failed to execute the model. 
     */
    public RecordToken getProperty(RecordToken properties, 
            WirelessIOPort sender, WirelessIOPort destination) 
            throws IllegalActionException {
        if (_executable!= null) {
            double[] p1 = _locationOf(sender);
            double[] p2 = _locationOf(destination);
            //FIXME: can we provide a more generic way to specify 
            //the labels?
            // create the RecordToken used to set the parameter of the
            // specified model.
            String[] labels = {"SenderLocation", 
                               "ReceiverLocation",
                               "Properties"
                              };
            DoubleToken[] t1 = new DoubleToken[p1.length];
            DoubleToken[] t2 = new DoubleToken[p2.length];
            for(int i=0; i<p1.length; i++) {
                t1[i] = new DoubleToken(p1[i]);
            }
            for(int i=0; i<p2.length; i++) {
                t2[i] = new DoubleToken(p2[i]);
            }
            Token[] value = {new ArrayToken(t1),
                             new ArrayToken(t2),
                             properties };
            RecordToken args = new RecordToken(labels, value);
            
            RecordToken results;
            String[] resultLabels = {"Properties"}; 
            try {
                results = ModelUtilities.executeModel
                        ((CompositeActor)_executable, args, resultLabels);
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                "Execution failed.");
            }
            return (RecordToken)results.get("Properties");
        }
        return null;
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
    private CompositeActor _executable;
    private NamedObj _model;
     
    private boolean _registeredWithChannel = false;
    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}

/*
@Copyright (c) 2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
 * 
 */
package ptolemy.domains.wireless.lib.bluetooth;

///////////////////////////////////////////////////////////////////
////BluetoothDevice

import java.util.HashSet;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessDirector;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

/**
 * This Actor is simulation of a Bluetooth adapter in a Bluetooth enabled device. The simulation is <i>functional</i>,
 * i.e. the hardware is not simulated. Instead, we provide an abstraction based on the Bluetooth dynamics found in
 * popular platforms such as Android and iOS. 
 * <p>
 * The Actor consists of six IO ports: four TypedIOPorts, and two WirelessIOPort. The wired ports simulate the
 * adapters connection to peripheral hardware. The wired input port will accept a String whose value is equivalent
 * to an acceptable command that can be found in the following list:
 * <p>
 * COMMAND_SWITCHON : Switch on this bluetooth device actor.
 * <p>
   COMMAND_SWITCHOFF : Swtich off this bluetooth device actor.
   <p>
   COMMAND_SCAN : Scan for other discoverable bluetooth device actors on this wireless channel.
   <p>
   COMMAND_STOPSCAN : Stop scanning for other discoverable bluetooth device actors on this wireless channel.
   <p>
   COMMAND_CONNECT : Connect to a paired bluetooth device actor on the same wireless channel. The device name to connect must be sent to the wired input details port.
   <p>
   COMMAND_DISCONNECT : Disconnect to a connected bluetooth device actor. The device name to disconnect from must be send to the wired input details port.
   <p>
   COMMAND_PAIR : Pair to a found bluetooth device actor on this wireless channel. The device name to pair to must be sent to the wired input details port.
   <p>
   COMMAND_UNPAIR : Unpair from a previously paired bluetooth device actor on this wireless channel. Note that this does not cause the other actor to unpair, a behavior particular to bluetooth. The device name to unpair from must be sent to the wired input details port.
   <p>
   COMMAND_DISCOVERABLE : Makes this bluetooth device actor discoverable to other scanning bluetooth device actors on the same channel.
   <p>
   COMMAND_HIDE : Makes this bluetooth device actor no longer discoverable.
   <p>
   COMMAND_SENDDATA : Initiate the sending of data. The name of the device to send data to must be sent to the wired input details port. The data must be sent to the wired input data port. The device to send data to must be found, paired, and connected with this bluetooth device actor
   <p>
   COMMAND_NOCOMMAND : The default command, which means that no token was available on the wired input port.
   <p>
 * To issue a command to this actor, only use the lower case equivalent of the second work, after the COMMAND_. Parsing occurs intenerally.
 * <p>
 * This actor can exist in 4 states, who vary in behavior: Off, Idle, Connected, and Scanning.
 * <p>
 * The dynamics of the actor are evaluated via a state machine implemented as a switch-case structure found in the fire() method.
 * <p>
 * In the event that there is a horrible failure, an incorrect input is received, or an input is not available when one is expected (i.e. when commanding connection or sending data,) an IllegalActionException will be thrown.
 * <p>
 * If all goes according to plan, the status of the iteration will be output to the wired output port. The token output to this port may also contain an generic piece of data. To retrieve this data, a downstream actor must call the getData() function on the output token. If this data is a string,
 * it will be output if toString() is called, along with the status. The resulting String will be comma delimited, with the format Status,Data.
 * <p>
 *@author Phillip Azar
 *@version $Id$
 *@since Ptolemy II 11.0
 *@Pt.ProposedRating
 *@Pt.AcceptedRating 
 *
 */

public class BluetoothDevice extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BluetoothDevice(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // Initialize internal variables
        state = States.STATE_OFF;
        _foundDevices = new HashSet<String>();
        _pairedDevices = new HashSet<String>();
        _connectedDevices = new HashSet<String>();
        _discoverable = false;

        
        // Initialize wireless port parameters
        wirelessInputChannelName = new StringParameter(this, "wirelessInputChannelName");
        wirelessInputChannelName.setExpression("WirelessInputChannel");
        
        wirelessOutputChannelName = new StringParameter(this, "wirelessOutputChannelName");
        wirelessOutputChannelName.setExpression("WirelessOutputChannel");
        
        // Initialize wired inputs
        wiredInput = new TypedIOPort(this, "Wired Input", true, false);
        new Parameter(wiredInput, "_showName").setExpression("true");
        
        wiredInputDetails = new TypedIOPort(this, "Wired Input - Detail", true, false);
        new Parameter(wiredInputDetails, "_showName").setExpression("true");
        
        wiredInputData = new TypedIOPort(this, "Wired Input - Data", true, false);
        new Parameter(wiredInputData, "_showName").setExpression("true");
        
        wiredOutput = new TypedIOPort(this, "Wired Output", false, true);
        new Parameter(wiredOutput, "_showName").setExpression("true");
        
        // Set wired port types
        wiredInput.setTypeEquals(BaseType.STRING);
        wiredInputDetails.setTypeEquals(BaseType.STRING);
        wiredInputData.setTypeEquals(BaseType.GENERAL);
        wiredOutput.setTypeEquals(BaseType.GENERAL);
        
        // Initialize wireless input
        wirelessInput = new WirelessIOPort(this, "Wireless Input", true, false);
        new Parameter(wirelessInput, "_showName").setExpression("true");
        new StringAttribute(wirelessInput, "_cardinal").setExpression("SOUTH");
        wirelessInput.outsideChannel.setExpression("$wirelessInputChannelName");
        
        // Initialize wireless output
        wirelessOutput = new WirelessIOPort(this, "Wireless Output", false, true);
        new Parameter(wirelessOutput, "_showName").setExpression("true");
        new StringAttribute(wirelessOutput, "_cardinal").setExpression("SOUTH");
        wirelessOutput.outsideChannel.setExpression("$wirelessOutputChannelName");
        
        //Initialize wireless port types
        wirelessInput.setTypeEquals(BaseType.GENERAL);
        wirelessOutput.setTypeEquals(BaseType.GENERAL);
                
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
        
    /** The input port for wired communication, which could potentially facilitate communication with other
     * devices/components/actors which are not wireless that interact with this actor.
     * This port is of type String, and will be checked internally against a list of valid commands.
     */
    public TypedIOPort wiredInput;
    
    /**
     * The input port for details about wired communications, which will contain a device identifier. The type of this port is String.
     */
    public TypedIOPort wiredInputDetails;
    
    /**
     * The input port for data to be sent. This will only be checked when the command to send data has been issued, and further will only be checked when in the
     * connected state. This port is of type General.
     */
    public TypedIOPort wiredInputData;
    
    /** The output port for wired communication, which could potentially facilitate communication with other
     * devices/components/actors which are not wireless that interact with this actor.
     * This port is of type General.
     */
    public TypedIOPort wiredOutput;
  
    /** The input port for wireless communication, which accepts a BluetoothRecordToken - this is to ensure
     * that any RecordToken at this port only comes from another BluetoothDevice.
     * This port is of type General.
     */
    public WirelessIOPort wirelessInput;
    
    /** Name of the wireless input channel. This is a string that defaults to
     *  "WirelessInputChannel".
     */
    public StringParameter wirelessInputChannelName;
    
    /** The output port for wireless communication, which will output a BluetoothResponseToken of type General.
     * 
     */
    public WirelessIOPort wirelessOutput;
    
    /** Name of the wireless output channel. This is a string that defaults to
     *  "WirelessOutputChannel".
     */
    public StringParameter wirelessOutputChannelName;

    
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (!(getDirector() instanceof WirelessDirector) ){
            throw new IllegalActionException(this.getClassName() + ": Cannot execute without WirelessDirector.");
        }
        
        StringToken _wiredInputToken;
        Token _wiredInputExtra;
        
        if (wiredInput.hasToken(0)){
            _wiredInputToken = (StringToken) wiredInput.get(0);
        }
        else {
            _wiredInputToken = new StringToken("empty");
        }
        
        //Here we will parse our input command string.
        BluetoothCommand command;
        switch(_wiredInputToken.stringValue()){
            case "switchon":
                command = BluetoothCommand.COMMAND_SWITCHON;
                break;
            case "switchoff":
                command = BluetoothCommand.COMMAND_SWITCHOFF;
                break;
            case "scan":
                command = BluetoothCommand.COMMAND_SCAN;
                break;
            case "stopscan":
                command = BluetoothCommand.COMMAND_STOPSCAN;
                break;
            case "connect":
                command = BluetoothCommand.COMMAND_CONNECT;
                break;
            case "disconnect":
                command = BluetoothCommand.COMMAND_DISCONNECT;
                break;
            case "pair":
                command = BluetoothCommand.COMMAND_PAIR;
                break;
            case "unpair":
                command = BluetoothCommand.COMMAND_UNPAIR;
                break;
            case "discoverable":
                command = BluetoothCommand.COMMAND_DISCOVERABLE;
                break;
            case "hide":
                command = BluetoothCommand.COMMAND_HIDE;
                break;
            case "senddata":
                command = BluetoothCommand.COMMAND_SENDDATA;
                break;
            default:
                command = BluetoothCommand.COMMAND_NOCOMMAND;
            }
            
            if (command == null) {
                throw new IllegalActionException("Input string does not equal supported command value");
            }
            
            if (wiredInputDetails.hasToken(0)){
                _wiredInputExtra = (StringToken) wiredInputDetails.get(0);
            }
            else {
                _wiredInputExtra = new StringToken("empty");
            }
            

            // The state machine
            BluetoothStatusToken status;

            switch(state){
                case STATE_OFF:
                    if (command.equals(BluetoothCommand.COMMAND_SWITCHON)){
                        this.state = States.STATE_IDLE;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Switchon");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else {
                        if (this.wiredInputData.hasToken(0)){
                            Token _useless = wiredInputData.get(0);
                        }
                        return;
                    }
                case STATE_IDLE:
                    if (command.equals(BluetoothCommand.COMMAND_SWITCHOFF)){
                        this.state = States.STATE_OFF;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Switchoff");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_SCAN)){
                        this.state = States.STATE_SCANNING;
                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.COMMAND_SCAN, "scan", this.getName(), "" ));
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Scan");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_CONNECT)){
                        if (_wiredInputExtra instanceof StringToken) {
                            String deviceToConnect = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._pairedDevices.contains(deviceToConnect)) {
                                BluetoothResponseToken connectRequest = new BluetoothResponseToken(BluetoothResponse.COMMAND_REQUESTCONNECT, deviceToConnect, this.getName(), "");
                                wirelessOutput.send(0, connectRequest);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempting to connect to:"+deviceToConnect);
                                wiredOutput.send(0, status);  
                            }
                            else {
                                throw new IllegalActionException("Cannot connect to an unpaired device.");
                            }
                        }
                        else {
                            throw new IllegalActionException("Invalid device identifier");
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_DISCOVERABLE)){
                        this._discoverable = true;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Discoverable");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_PAIR)){
                        if (_wiredInputExtra instanceof StringToken){
                            String deviceToPair = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._foundDevices.contains(deviceToPair) && (!this._pairedDevices.contains(deviceToPair))){
                                this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.COMMAND_REQUESTPAIR, deviceToPair, this.getName(), ""));
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempting to pair to:"+deviceToPair);
                                wiredOutput.send(0, status); 
                            }
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_UNPAIR)){
                        if (_wiredInputExtra instanceof StringToken){
                            String deviceToUnpair = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._foundDevices.contains(deviceToUnpair) && (this._pairedDevices.contains(deviceToUnpair))){
                                // Here is a behavior which is particularly interesting in Bluetooth - when you unpair, you don't tell the paired device anything. You just remove it from your list of paired devices and move on.
                                this._pairedDevices.remove(deviceToUnpair);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Unpaired from: "+deviceToUnpair);
                                wiredOutput.send(0, status); 
                            }
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_HIDE)){
                        this._discoverable = false;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Hidden");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    
                    while(this.wirelessInput.hasToken(0)){
                        Token _token = wirelessInput.get(0);
                        if (_token instanceof BluetoothResponseToken) {
                            BluetoothResponseToken _newResponse = (BluetoothResponseToken) _token;
                            if (_newResponse.getDeviceIdentifier().equals(this.getName())){
                                if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_REQUESTCONNECT)){
                                    // For now, we accept all connection requests.
                                    if (this._pairedDevices.contains(_newResponse.getSourceIdentifier())){
                                        this.state = States.STATE_CONNECTED;
                                        this._connectedDevices.add(_newResponse.getSourceIdentifier());
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_ACCEPTCONNECT, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received connection request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_REQUESTPAIR)){
                                    // For now, we accept all pair requests.
                                    if (this._foundDevices.contains(_newResponse.getSourceIdentifier())){
                                        this._pairedDevices.add(_newResponse.getSourceIdentifier());
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_ACCEPTPAIR, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received pair request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_ACCEPTPAIR)) {
                                    this._pairedDevices.add(_newResponse.getSourceIdentifier());
                                    status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Accepted pair request from: "+_newResponse.getSourceIdentifier());
                                    wiredOutput.send(0, status); 
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_ACCEPTCONNECT)) {
                                    this.state = States.STATE_CONNECTED;
                                    this._connectedDevices.add(_newResponse.getSourceIdentifier());
                                    status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Accepted connection request from: "+_newResponse.getSourceIdentifier());
                                    wiredOutput.send(0, status); 
                                }
                            }
                            else if (_newResponse.getDeviceIdentifier().equals("scan")){
                                if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_SCAN)){
                                    if (this._discoverable && (!this._foundDevices.contains(_newResponse.getSourceIdentifier()))){
                                        this._foundDevices.add(_newResponse.getSourceIdentifier());
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_FINDME, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received scan request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                            }
                        }
                    }
                    //Eat useless tokens
                    if (this.wiredInputData.hasToken(0)){
                        Token _useless = wiredInputData.get(0);
                    }
                    break;
                case STATE_CONNECTED:
                    if (command.equals(BluetoothCommand.COMMAND_SWITCHOFF)){
                        this.state = States.STATE_OFF;
                        for (String device : this._connectedDevices){
                            BluetoothResponseToken shuttingOff = new BluetoothResponseToken(BluetoothResponse.COMMAND_DISCONNECT, device, this.getName(), "");
                            wirelessOutput.send(0, shuttingOff);
                        }
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "empty");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_SCAN)){
                        this.state = States.STATE_SCANNING;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "empty");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_CONNECT)){
                        if (_wiredInputExtra instanceof StringToken) {
                            String deviceToConnect = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._pairedDevices.contains(deviceToConnect)) {
                                BluetoothResponseToken connectRequest = new BluetoothResponseToken(BluetoothResponse.COMMAND_REQUESTCONNECT, deviceToConnect, this.getName(), "");
                                wirelessOutput.send(0, connectRequest);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempting to connect to:"+deviceToConnect);
                                wiredOutput.send(0, status);  
                            }
                            else {
                                throw new IllegalActionException("Cannot connect to an unpaired device.");
                            }
                        }
                        else {
                            throw new IllegalActionException("WiredInputDetails port must be filled with device identifier to command connection");
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_DISCONNECT)){
                        if (_wiredInputExtra instanceof StringToken) {
                            String deviceToDisconnect = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._connectedDevices.contains(deviceToDisconnect)) {
                                this._connectedDevices.remove(deviceToDisconnect);
                                BluetoothResponseToken disconnectRequest = new BluetoothResponseToken(BluetoothResponse.COMMAND_DISCONNECT, deviceToDisconnect, this.getName(), "");
                                wirelessOutput.send(0, disconnectRequest);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Disconnecting from: "+deviceToDisconnect);
                                wiredOutput.send(0, status);  
                            }
                            else {
                                throw new IllegalActionException("Cannot disconnect from a device that is not connected.");
                            }
                        }
                        else {
                            throw new IllegalActionException("WiredInputDetails port must be filled with device identifier to command disconnection");
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_PAIR)){
                        if (_wiredInputExtra instanceof StringToken){
                            String deviceToPair = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._foundDevices.contains(deviceToPair) && (!this._pairedDevices.contains(deviceToPair))){
                                this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.COMMAND_REQUESTPAIR, deviceToPair, this.getName(), ""));
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Sending pair request to:"+ deviceToPair);
                                wiredOutput.send(0, status); 
                            }
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_UNPAIR)){
                        if (_wiredInputExtra instanceof StringToken){
                            String deviceToUnpair = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._foundDevices.contains(deviceToUnpair) && (this._pairedDevices.contains(deviceToUnpair) && (!this._connectedDevices.contains(deviceToUnpair)))){
                                this._pairedDevices.remove(deviceToUnpair);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempting to unpair from: "+deviceToUnpair);
                                wiredOutput.send(0, status); 
                            }
                            else if (this._foundDevices.contains(deviceToUnpair) && (this._pairedDevices.contains(deviceToUnpair) && (this._connectedDevices.contains(deviceToUnpair)))){
                                this._connectedDevices.remove(deviceToUnpair);
                                this._pairedDevices.remove(deviceToUnpair);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempting to unpair from: "+deviceToUnpair);
                                wiredOutput.send(0, status); 
                            }
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_DISCOVERABLE)){
                        this._discoverable = true;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Discoverable");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_HIDE)){
                        this._discoverable = false;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Hidden");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_SENDDATA)){
                        if (_wiredInputExtra instanceof StringToken){
                            String deviceToSendData = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._connectedDevices.contains(deviceToSendData) && this.wiredInputData.hasToken(0)){
                                this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_OK, deviceToSendData, this.getName(), wiredInputData.get(0)));
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Sent data to: "+deviceToSendData);
                                wiredOutput.send(0, status); 
                            }
                            else {
                                throw new IllegalActionException("Data to send must be specified on port: "+this.wiredInputData.getName());
                            }
                        }
                    }
                    while(this.wirelessInput.hasToken(0)){
                        Token _token = wirelessInput.get(0);
                        if (_token instanceof BluetoothResponseToken) {
                            BluetoothResponseToken _newResponse = (BluetoothResponseToken) _token;
                            if (_newResponse.getDeviceIdentifier().equals(this.getName())){
                                if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_ACCEPTCONNECT)) {
                                    this._connectedDevices.add(_newResponse.getDeviceIdentifier());
                                    status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Accepted connection request from : "+_newResponse.getSourceIdentifier());
                                    wiredOutput.send(0, status); 
                                   }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_OK)){
                                    if (this._connectedDevices.contains(_newResponse.getSourceIdentifier())){
                                        BluetoothStatusToken<?> _newData = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, _newResponse.getData());
                                        wiredOutput.send(0, _newData);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_REQUESTPAIR)){
                                    // For now, we accept all pair requests.
                                    if (this._foundDevices.contains(_newResponse.getSourceIdentifier())){
                                        this._pairedDevices.add(_newResponse.getSourceIdentifier());
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_ACCEPTPAIR, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received pair request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_REQUESTCONNECT) && (this._connectedDevices.size() <= 7)){
                                    // We will only connect to a new device if we don't have more then 7 connections open.
                                    // See: http://en.wikipedia.org/wiki/Bluetooth#Implementation
                                    if (this._pairedDevices.contains(_newResponse.getSourceIdentifier())){
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_ACCEPTCONNECT, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        this._connectedDevices.add(_newResponse.getSourceIdentifier());
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received connection request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_DISCONNECT)){
                                    if (this._connectedDevices.contains(_newResponse.getSourceIdentifier())){
                                        this._connectedDevices.remove(_newResponse.getSourceIdentifier());
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Disconected from: "+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                            }
                            else if (_newResponse.getDeviceIdentifier().equals("scan")){
                                if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_SCAN)){
                                    if (this._discoverable && (!this._foundDevices.contains(_newResponse.getSourceIdentifier()))){
                                        this._foundDevices.add(_newResponse.getSourceIdentifier());
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_FINDME, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received scan request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                            }
                        }
                    }
                    if (this.state.equals(States.STATE_CONNECTED) && this._connectedDevices.isEmpty()) {
                        this.state = States.STATE_IDLE;
                    }
                    //Eat useless tokens
                    if (this.wiredInputData.hasToken(0)){
                        Token _useless = wiredInputData.get(0);
                    }
                    break;
                case STATE_SCANNING:
                    if (command.equals(BluetoothCommand.COMMAND_SWITCHOFF)){
                        this.state = States.STATE_OFF;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Switch on");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_STOPSCAN)){
                        this.state = States.STATE_IDLE;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Switch off");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_PAIR)){
                        if (_wiredInputExtra instanceof StringToken){
                            String deviceToPair = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._foundDevices.contains(deviceToPair) && (!this._pairedDevices.contains(deviceToPair))){
                                this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.COMMAND_REQUESTPAIR, deviceToPair, this.getName(), ""));
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempt to pair to: "+deviceToPair);
                                wiredOutput.send(0, status);
                            }
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_CONNECT)){
                        if (_wiredInputExtra instanceof StringToken) {
                            String deviceToConnect = ((StringToken) _wiredInputExtra).stringValue();
                            if (this._pairedDevices.contains(deviceToConnect)) {
                                BluetoothResponseToken connectRequest = new BluetoothResponseToken(BluetoothResponse.COMMAND_REQUESTCONNECT, deviceToConnect, this.getName(), "");
                                wirelessOutput.send(0, connectRequest);
                                status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Attempting to connect to:"+deviceToConnect);
                                wiredOutput.send(0, status);  
                            }
                            else {
                                throw new IllegalActionException("Cannot connect to an unpaired device.");
                            }
                        }
                        else {
                            throw new IllegalActionException("WiredInputDetails port must be filled with device identifier to command connection");
                        }
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_DISCOVERABLE)){
                        this._discoverable = true;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Discoverable");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_HIDE)){
                        this._discoverable = false;
                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Hidden");
                        this.wiredOutput.send(0, status);
                        break;
                    }
                    else if (command.equals(BluetoothCommand.COMMAND_NOCOMMAND)){
                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.COMMAND_SCAN, "scan", this.getName(), "" ));
                    }
                    while (wirelessInput.hasToken(0)) {
                        Token _token = wirelessInput.get(0);
                        if (_token instanceof BluetoothResponseToken) {
                            BluetoothResponseToken _newResponse = (BluetoothResponseToken) _token;
                            if (_newResponse.getDeviceIdentifier().equals(this.getName())){
                                if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_FINDME)){
                                    _foundDevices.add(_newResponse.getSourceIdentifier());                            
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_REQUESTCONNECT)){
                                    if (this._pairedDevices.contains(_newResponse.getSourceIdentifier())){
                                        this.state = States.STATE_CONNECTED;
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_ACCEPTCONNECT, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received connection request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.COMMAND_REQUESTPAIR)){
                                    // For now, we accept all pair requests.
                                    if (this._foundDevices.contains(_newResponse.getSourceIdentifier())){
                                        this._pairedDevices.add(_newResponse.getSourceIdentifier());
                                        this.wirelessOutput.send(0, new BluetoothResponseToken(BluetoothResponse.RESPONSE_ACCEPTPAIR, _newResponse.getSourceIdentifier(), this.getName(), ""));
                                        status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received pair request from:"+_newResponse.getSourceIdentifier());
                                        wiredOutput.send(0, status);
                                    }
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_ACCEPTPAIR)) {
                                    this._pairedDevices.add(_newResponse.getSourceIdentifier());
                                    status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Accepted pair request from:"+_newResponse.getSourceIdentifier());
                                    wiredOutput.send(0, status);
                                }
                                else if (_newResponse.getResponse().equals(BluetoothResponse.RESPONSE_ACCEPTCONNECT)) {
                                    this.state = States.STATE_CONNECTED;
                                    this._connectedDevices.add(_newResponse.getSourceIdentifier());
                                    status = new BluetoothStatusToken(BluetoothStatus.STATUS_OK, "Received connection request from:"+_newResponse.getSourceIdentifier());
                                    wiredOutput.send(0, status);
                                }
                            }
                        }
                    }
                    //Eat useless tokens
                    if (this.wiredInputData.hasToken(0)){
                        Token _useless = wiredInputData.get(0);
                    }
                    break;
                default:
                    status = new BluetoothStatusToken(BluetoothStatus.STATUS_ERROR, "Unknown state - Fix me");
                    this.wiredOutput.send(0, status);
                    break;         
            }
        

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private enum States {
        STATE_IDLE,
        STATE_CONNECTED,
        STATE_SCANNING,
        STATE_OFF,
    }
    
    private HashSet<String> _foundDevices;
    private HashSet<String> _pairedDevices;
    private HashSet<String> _connectedDevices; 
    private States state;
    private boolean _discoverable;
   
}

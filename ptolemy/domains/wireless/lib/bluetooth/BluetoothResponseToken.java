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
package ptolemy.domains.wireless.lib.bluetooth;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * This class represents a wireless response from one BluetoothDevice actor to another. It is essentially reimagining of the ObjectToken type, with a few more members critical to making the
 * Bluetooth to Bluetooth communication possible. These include a device identifier, a source identifier, and data.
 * <p>
 * The device identifier field represents the target device for this response token.
 * The source identifier field represents the current device this response token is bieng sent from. This will be set to the return value of this.getName() in all cases except when scanning, where it will be set to "scan".
 * The data field represents an arbitrary peice of data to communicate to another BluetoothDevice actor.
 * <p>
 * Construction of an object of this class requires a BluetoothResponse enum element, a device identifier, a source identifier, and an arbitrary peice of data. The fields of this class are immutable.
 * <p>
 * The toString() method will return the string equivalent of the BluetoothResponse enum element contained within this object.
 * @author Phillip Azar
 *
 * @param <T> - the type for the data contained within this token.
 * @see BluetoothResponse
 * @see BluetoothDevice
 */
public class BluetoothResponseToken<T> extends BluetoothToken {
    
    /**
     * Construct a token with the following parameters:
     * @param response : The response, request, or command desired. Acceptable entries are contained within the BluetoothResponse enumeration.
     * @param deviceIdentifier : A String representing the name of target device.
     * @param sourceIdentifier : A String representing the name of the device constructing this token.
     * @param data : The arbitrary data to send.
     */
    public BluetoothResponseToken(BluetoothResponse response, String deviceIdentifier, String sourceIdentifier, T data){
        this._response = response;
        this._deviceIdentifier = deviceIdentifier;
        this._sourceIdentifier = sourceIdentifier;
        this._data = data;
    }
    
    /**
     * Get the BluetoothResponse enum element associated with this token.
     * @return _response
     */
    public BluetoothResponse getResponse(){
        return this._response;
    }
    
    /**
     * Get the name of the target device associated with this token.
     * @return _deviceIdentifier
     */
    public String getDeviceIdentifier(){
        return this._deviceIdentifier;
    }
    
    /**
     * Get the name of the source device associated with this token.
     * @return _sourceIdentifier
     */
    public String getSourceIdentifier(){
        return this._sourceIdentifier;
    }
    
    /**
     * Get the data stored within this token.
     * @return _data
     */
    public T getData(){
        return this._data;
    }
    
    @Override
    public String toString() {
        switch(this._response){
        case COMMAND_DISCONNECT:
            return "DISCONNECT";
        case COMMAND_REQUESTCONNECT:
            return "REQUESTCONNECT";
        case COMMAND_REQUESTPAIR:
            return "REQUESTPAIR";
        case RESPONSE_ACCEPTPAIR:
            return "ACCEPTPAIR";
        case RESPONSE_ACCEPTCONNECT:
            return "ACCEPTCONNECT";
        case RESPONSE_DENY:
            return "DENY";
        case RESPONSE_FINDME:
            return "FINDME";
        case RESPONSE_OK:
            return "OK";
        default:
            return "nil";
        }
    }
    

    @Override
    public BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof BluetoothResponseToken){
            BluetoothResponseToken right = (BluetoothResponseToken) rightArgument;
            if (this._response == right.getResponse()){
                return new BooleanToken(true);
            }
            else {
                return new BooleanToken(false);
            }
        }
        else {
            throw new IllegalActionException("The argument must be of type BluetoothResponseToken");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private final BluetoothResponse _response;
    private final String _deviceIdentifier;
    private final String _sourceIdentifier;
    private final T _data;
}

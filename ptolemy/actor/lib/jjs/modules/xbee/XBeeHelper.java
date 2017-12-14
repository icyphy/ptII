/* Helper for XBee radio modules.

 Copyright (c) 2015-2017 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.xbee;

import java.util.Map;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;

import io.vertx.core.buffer.Buffer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;

///////////////////////////////////////////////////////////////////
//// XBeeHelper

/**
 * Helper for XBee radio modules.
 * See the xbee module for documentation.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating red (cxh)
 * @Pt.AcceptedRating red (cxh)
 */
public class XBeeHelper extends VertxHelperBase
        implements IDataReceiveListener {

    /** Create an XBee device.
     *  The first argument is an instance of the JavaScript XBee object.
     *  @param actor The actor that this is helping.
     *  @param helping The object that this is helping (XBee).
     *  @param portName The name of the port to open.
     *  @param options Serial port options.
     *  @exception XBeeException If there is no such port, if the port
     *  is in use, if there are too many listeners or if we can't get
     *  an input or output stream for the port.
     */
    public XBeeHelper(Object actor, ScriptObjectMirror helping, String portName,
            Map<String, Object> options) throws XBeeException {
        super(actor, helping);

        Integer baudRate = (Integer) options.get("baudRate");

        // FIXME: Configure using SerialPortParameters argument.
        _device = new XBeeDevice(portName, baudRate);
        _device.open();
        _device.addDataListener(this);

        // Set the send and receive types.
        // First, make sure the arrays are populated.
        supportedSendTypes();
        supportedReceiveTypes();
        // Next, get the option values.
        String receiveTypeName = (String) options.get("receiveType");
        String sendTypeName = (String) options.get("sendType");
        // Next, map these names to data types.
        try {
            _sendType = Enum.valueOf(DATA_TYPE.class,
                    sendTypeName.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid send data type: " + sendTypeName);
        }
        // Finally, do the receive type.
        try {
            _receiveType = Enum.valueOf(DATA_TYPE.class,
                    receiveTypeName.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid receive data type: " + receiveTypeName);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the port, if it is open. */
    public void close() {
        if (_device != null && _device.isOpen()) {
            // _device.close();
        }
    }

    @Override
    public void dataReceived(XBeeMessage message) {

        if (_receiveType == DATA_TYPE.STRING) {
            _currentObj.callMember("emit", "data", message.getDataString());
        } else {
            // Assume a numeric type.
            Buffer buffer = Buffer.buffer(message.getData());
            int size = _sizeOfType(_receiveType);
            int length = buffer.length();
            int numberOfElements = 0;
            if (size > 0) {
                numberOfElements = length / size;
            }
            if (numberOfElements == 1) {
                _currentObj.callMember("emit", "data",
                        _extractFromBuffer(buffer, _receiveType, 0));
            } else if (numberOfElements > 1) {
                // Output a single array.
                Object[] result = new Object[numberOfElements];
                int position = 0;
                for (int i = 0; i < result.length; i++) {
                    result[i] = _extractFromBuffer(buffer, _receiveType,
                            position);
                    position += size;
                }
                // NOTE: If we return result, then the emitter will not
                // emit a native JavaScript array. We have to do a song and
                // dance here which is probably very inefficient (almost
                // certainly... the array gets copied).
                try {
                    _currentObj.callMember("emit", "data",
                            _actor.toJSArray(result));
                } catch (Exception e) {
                    _error("Failed to convert to a JavaScript array: " + e, e);
                    _currentObj.callMember("emit", "data", result);
                }
            } else {
                _error("Expect to receive type " + _receiveType + ", of size "
                        + size
                        + ", but received an insufficient number of bytes: "
                        + buffer.length());
            }
        }
    }

    /** Send data over the radio.
     *  @param data The data to send.
     */
    public void send(final Object data) {
        // For convenience, even though we are not using Vert.x here,
        // we use the Vert.x Buffer.
        Buffer buffer = Buffer.buffer();
        // Handle the case where data is an array.
        if (data instanceof Object[]) {
            for (Object element : (Object[]) data) {
                // JavaScript arrays can have holes, and moreover,
                // it seems that Nashorn's Java.to() function creates
                // a bigger array than needed with trailing null elements.
                if (element != null) {
                    _appendToBuffer(element, _sendType, null, buffer);
                }
            }
        } else {
            _appendToBuffer(data, _sendType, null, buffer);
        }
        if (!_device.isOpen()) {
            try {
                _device.open();
            } catch (XBeeException e) {
                _error(_actor.getFullName() + ": Failed to open XBee device.",
                        e);
            }
        }
        try {
            // FIXME: May not want to broadcast.
            // How to address another device?
            _device.sendBroadcastData(buffer.getBytes());
        } catch (TimeoutException e) {
            _error(_actor.getFullName() + ": Timeout on send.", e);
        } catch (XBeeException e) {
            _error(_actor.getFullName() + ": Failed to send data.", e);
        }
    }

    /** Return an array of the types supported by the current host for
     *  receiveType arguments.
     *  @return an array of the types supported by the current host for
     *  receiveType arguments.
     */
    public static String[] supportedReceiveTypes() {
        // Formerly, we checked to see if _types was null outside of the syncronized block

        // However, Coverity Scan warns:
        // "CID 1349633 (#1 of 1): Check of thread-shared field evades
        // lock acquisition
        // (LOCK_EVASION)5. thread2_checks_field_early: Thread2 checks
        // _types, reading it after Thread1 assigns to _types but
        // before some of the correlated field assignments can
        // occur. It sees the condition
        // ptolemy.actor.lib.jjs.modules.xbee.XBeeHelper._types ==
        // null as being false. It continues on before the critical
        // section has completed, and can read data changed by that
        // critical section while it is in an inconsistent state."

        // Avoid FindBugs LI: Unsynchronized Lazy Initialization (FB.LI_LAZY_INIT_UPDATE_STATIC)
        synchronized (_typesMutex) {
            if (_types == null) {
                // Don't support image type.
                int length = DATA_TYPE.values().length - 1;
                _types = new String[length];
                int i = 0;
                for (DATA_TYPE type : DATA_TYPE.values()) {
                    if (type != DATA_TYPE.IMAGE) {
                        _types[i++] = type.toString().toLowerCase();
                    }
                }
            }
            return _types;
        }
    }

    /** Return an array of the types supported by the current host for
     *  sendType arguments.
     *  @return an array of the types supported by the current host for
     *  sendType arguments.
     */
    public static String[] supportedSendTypes() {
        return supportedReceiveTypes();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The device. */
    XBeeDevice _device;

    /** The receive type for this instance of XBee. */
    private DATA_TYPE _receiveType;

    /** The array of send and receive type names. */
    private static String[] _types;

    /** A mutex used when creating _types. */
    private static Object _typesMutex = new Object();

    /** The send type for this instance of XBee. */
    private DATA_TYPE _sendType;
}

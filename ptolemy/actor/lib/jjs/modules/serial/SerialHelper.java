/* Send and receive bytes via the serial port.

 Copyright (c) 2001-2016 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SerialHelper

/**
 Helper for the serial module.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (winthrop)
 @Pt.AcceptedRating red (winthrop)
 */
public class SerialHelper extends HelperBase {

    /** Open a serial port.
     *  The argument is an instance of the JavaScript SerialPort object.
     *  @param actor The actor associated with this helper.
     *  @param helping The object that this is helping (SerialPort).
     *  @param portName The name of the port to open.
     *  @param ownerName The name of the owner assigned to this port, if opening is successful.
     *  @param timeout Time in milliseconds before failing.
     *  @param options Serial port options (FIXME: define).
     */
    public SerialHelper(
            Object actor,
            ScriptObjectMirror helping,
            String portName,
            String ownerName,
            int timeout,
            Map<String,Object> options) {
        super(actor, helping);
        _portName = portName;
        _ownerName = ownerName;
        _timeout = timeout;
        
        _baudRate = (Integer)options.get("baudRate");
        
        // Set the send and receive types.
        // First, make sure the arrays are populated.
        supportedSendTypes();
        supportedReceiveTypes();
        // Next, get the option values.
        String receiveTypeName = (String)options.get("receiveType");
        String sendTypeName = (String)options.get("sendType");
        // Next, map these names to data types.
        try {
            _sendType = Enum.valueOf(DATA_TYPE.class, sendTypeName.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid send data type: " + sendTypeName);
        }
        // Finally, do the receive type.
        try {
            _receiveType = Enum.valueOf(DATA_TYPE.class, receiveTypeName.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid receive data type: " + receiveTypeName);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the serial port.
     */
    public synchronized void close() {
        _actor.log("Closing serial port.");
        _open = false;
        if (_serialPort != null) {
            if (_inputStream != null) {
                _actor.log("Closing inputStream.");
                try {
                    _inputStream.close();
                } catch (IOException e) {
                    // No idea why closing this would fail.
                    e.printStackTrace();
                }
            }
            if (_outputStream != null) {
                _actor.log("Closing outputStream.");
                try {
                    _outputStream.flush();
                    _outputStream.close();
                } catch (IOException e) {
                    // No idea why closing this would fail.
                    e.printStackTrace();
                }
            }
            if (SerialHelper._openedPorts != null) {
                SerialHelper._openedPorts.remove(_portName);
            }
            _actor.log("About to remove event listeners from the serial port.");
            _serialPort.removeEventListener();
            _actor.log("About to call _serialPort.close().");
            _serialPort.close();
            _serialPort = null;
            _actor.log("Serial port closed.");
        }
    }

    /** Open the serial port. If there is already a serial port open,
     *  close it first.
     *
     *  <p>If the port is not found, then try each available port in
     *  turn and open the first as yet unopened port.  Port names that
     *  include "Bluetooth" and "/dev/cu."  are skipped.  Different
     *  machines may have different port names so port scanning makes
     *  it possible to have models that will open the first N serial
     *  ports.  </p>
     *
     *  @exception IllegalActionException If there is no such port, if
     *  the port is owned by another user, if the port does not
     *  support the specified paramters or if the input or output
     *  fails.
     */
    public synchronized void open() throws IllegalActionException {
        if (_serialPort != null) {
            _serialPort.close();
        }
        CommPortIdentifier portID = null;
        try {
            CommPort port = null;
            try {
                portID = CommPortIdentifier.getPortIdentifier(_portName);
                port = portID.open(_ownerName, _timeout);
            } catch (NoSuchPortException ex) {
                // If the model on a different machine, then the port names maybe different.
                // As a workaround, we cycle through the ports and open the first available port.

                if (SerialHelper._openedPorts == null) {
                    SerialHelper._openedPorts = new LinkedList<String>();
                }
                boolean openedPort = false;
                // Enumerate the available ports.
                Enumeration ports = CommPortIdentifier.getPortIdentifiers();
                while (ports.hasMoreElements()) {
                    CommPortIdentifier identifier = (CommPortIdentifier) ports
                        .nextElement();
                    // Try to open each port in turn.
                    if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        if (identifier.getName().indexOf("/dev/cu.") != -1) {
                            System.out.println("SerialHelper.java: " + _actor.getFullName()
                                    + " Could not find or open " + _portName
                                    + ".  Skipping " + identifier.getName() + " because it is a calling unit (/dev/cu) port");
                            continue;
                        }
                        if (identifier.getName().indexOf("Bluetooth") != -1) {
                            System.out.println("SerialHelper.java: " + _actor.getFullName()
                                    + " Could not find or open " + _portName
                                    + ".  Skipping " + identifier.getName() + " because it is a Bluetooth port");
                            continue;
                        }
                        // Only try to open the port if it has not yet been opened.
                        if (!SerialHelper._openedPorts.contains(identifier.getName())) {
                            try {
                                portID = CommPortIdentifier.getPortIdentifier(identifier.getName());
                                port = portID.open(_ownerName, _timeout);
                                System.out.println("SerialHelper.java: " + _actor.getFullName()
                                        + " Successfully opened "
                                        + identifier.getName() + " because " + _portName
                                        + " could not be opened, the exception was " + ex);
                                _portName = identifier.getName();
                                SerialHelper._openedPorts.add(_portName);
                                openedPort = true;
                                break;
                            } catch (Throwable throwable) {
                                System.out.println("SerialHelper.java: "  + _actor.getFullName()
                                        + " Failed to open " + identifier.getName() + ": "
                                        + throwable + ". Will try the next port (if available).");
                            }
                        }
                    }
                }
                if (!openedPort) {
                    throw ex;
                }
            }

            if (!(port instanceof SerialPort)) {
                _error("Port " + _portName + " is not a serial port.");
                return;
            }
            _serialPort = (SerialPort) port;

            // FIXME: Set the options.
            _serialPort.setSerialPortParams(_baudRate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

            // FIXME: Uncomment the next line to avoid jjs/modules/serial/test/auto/SerialHelloWorld.xml hanging.
            _serialPort.enableReceiveTimeout(_timeout);
            _inputStream = _serialPort.getInputStream();
            _outputStream = _serialPort.getOutputStream();

            _open = true;

            // The RXTX event listener mechanism does not work under OS X.
            // It causes the JVM to crash.
            // _serialPort.addEventListener(this);

            (new Thread(new SerialReader())).start();
            (new Thread(new SerialWriter())).start();
        } catch (NoSuchPortException e) {
            throw new IllegalActionException(_actor, e, "No such port: " + _portName);
        } catch (PortInUseException e) {
            throw new IllegalActionException(_actor, e, "Port " + _portName + " is currently owned by " + portID.getCurrentOwner());
        } catch (UnsupportedCommOperationException e) {
            throw new IllegalActionException(_actor, e, "Port does not support the specified parameters: " + _portName);
        } catch (IOException e) {
            throw new IllegalActionException(_actor, e, "Failed to input input or output stream: " + _portName);
        }
    }
    
    /** Return an array of the types supported by the current host for
     *  receiveType arguments.
     *  @return an array of the types supported by the current host for
     *  receiveType arguments.
     */
    public static String[] supportedReceiveTypes() {
        // FIXME: This is duplicated from XBeeHelper and should be moved to a base class!!
        // also supportedSendTypes.
        
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

    /** The baud rate for the port. */
    private int _baudRate;
    
    /** Input stream. */
    private InputStream _inputStream;

    /** Output stream. */
    private OutputStream _outputStream;

    /** The name of the serial port. */
    private String _portName;

    /** Indicator of whether the port is open. */
    private boolean _open;

    /** List of ports that have been opened while searching because
     *  the initial port could not be found.
     */
    private static List<String> _openedPorts;

    /** The name of the owner. */
    private String _ownerName;
    
    /** The receive type for this instance of XBee. */
    private DATA_TYPE _receiveType;

    /** The timeout for opening. */
    private int _timeout;

    /** The array of send and receive type names. */
    private static String[] _types;

    /** A mutex used when creating _types. */
    private static Object _typesMutex = new Object();

    /** The send type for this instance of XBee. */
    private DATA_TYPE _sendType;

    /** The serial port. */
    private SerialPort _serialPort;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Read from the serial port and emit data.
     */
    public class SerialReader implements Runnable {
        public void run () {
            byte[] message = new byte[1024];
            int length = -1;
            try {
                Buffer buffer = Buffer.buffer();
                // Perform blocking read of up to 1024 bytes until
                // end of stream is observed.
                while (_open && (length = _inputStream.read(message)) > -1) {
                    if (length == 0) {
                        // No bytes were read (why didn't _inputStream block?).
                        continue;
                    }
                    if (_receiveType == DATA_TYPE.STRING || _receiveType == DATA_TYPE.JSON) {
                        // Read until we receive a null byte.
                        for (int i = 0; i < length; i++) {
                            if (message[i] == 0) {
                                if (buffer.length() > 0) {
                                    // JSON parsing is handled on the accessor end.
                                    _currentObj.callMember("emit", "data", new String(buffer.toString()));
                                    buffer = Buffer.buffer();
                                }
                            } else {
                                buffer.appendByte(message[i]);
                            }
                        }
                    } else {
                        // Assume a numeric type.
                        buffer.appendBytes(message, 0, length);
                        int size = _sizeOfType(_receiveType);
                        int numberOfElements = 0;
                        if (size > 0) {
                            numberOfElements = length / size;
                        }
                        if (numberOfElements == 1) {
                            _currentObj.callMember("emit", "data", _extractFromBuffer(buffer, _receiveType, 0));
                        } else if (numberOfElements > 1) {
                            // Output a single array.
                            Object[] result = new Object[numberOfElements];
                            int position = 0;
                            for (int i = 0; i < result.length; i++) {
                                result[i] = _extractFromBuffer(buffer, _receiveType, position);
                                position += size;
                            }
                            // NOTE: If we return result, then the emitter will not
                            // emit a native JavaScript array. We have to do a song and
                            // dance here which is probably very inefficient (almost
                            // certainly... the array gets copied).
                            try {
                                _currentObj.callMember("emit", "data", _actor.toJSArray(result));
                            } catch (Exception e) {
                                _error("Failed to convert to a JavaScript array: " + e);
                                _currentObj.callMember("emit", "data", result);
                            }
                        } else if (numberOfElements <= 0) {
                            _error("Expect to receive type "
                                    + _receiveType
                                    + ", of size " + size
                                    + ", but received an insufficient number of bytes: "
                                    + length);
                        }
                    }
                }
            } catch ( IOException e ) {
                _error("Exception occurred reading from serial port.", e);
            }
            _actor.log("Serial port reader thread exiting.");
        }
    }

    /** Write to the serial port.
     */
    public class SerialWriter implements Runnable  {
        public void run () {
            try {
                int count = 0;
                // FIXME: Just outputting a count.
                while (_open) {
                    String message = _ownerName + ": " + count++;
                    _outputStream.write(message.getBytes());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch ( IOException e ) {
                _error("Exception occurred reading from serial port.", e);
            }
            _actor.log("Serial port writer thread exiting.");
        }
    }
}

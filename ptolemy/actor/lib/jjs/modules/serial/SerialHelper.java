/* Send and receive bytes via the serial port.

 Copyright (c) 2001-2017 The Regents of the University of California.
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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// SerialHelper

/**
 Helper for the serial module.

 @author Edward A. Lee. Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (cxh)
 @Pt.AcceptedRating red (cxh)
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
        
        if (options != null) {
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
        } else {
            // No options specified. Use defaults.
            _baudRate = 9600;
            _sendType = DATA_TYPE.STRING;
            _receiveType = DATA_TYPE.STRING;
        }

        if (_receiveType == null) {
            throw new NullPointerException("_receiveType was null?  Perhaps it was set to an unsupported type?");
        }

        if (_sendType == null) {
            throw new NullPointerException("_sendType was null?  Perhaps it was set to an unsupported type?");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the serial port.
     */
    public void close() {
        // Synchronize on the class because serial ports are a common resource.
        synchronized(SerialHelper.class) {
            _actor.log("Closing serial port.");
            _open = false;
            if (_portName.equals("loopback")) {
                // Port is the dummy loopback port. Free up its use.
                _loopbackUser = null;
            }
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
    public void open() throws IllegalActionException {
        // Synchronize on the class because serial ports are a common resource.
        synchronized(SerialHelper.class) {
            if (_serialPort != null) {
                _serialPort.close();
                _serialPort = null;
            }
            if (_portName.equals("loopback")) {
                // Port is the dummy loopback port. See whether it is already in use.
                if (_loopbackUser != null) {
                    throw new IllegalActionException(_actor,
                            "Port loopback is already in use by " + _loopbackUser);
                }
                _loopbackUser = _ownerName;
                try {
                    _loopbackInput = new PipedInputStream();
                    _loopbackOutput = new PipedOutputStream(_loopbackInput);
                    _inputStream = _loopbackInput;
                    _outputStream = _loopbackOutput;
                } catch (IOException e) {
                    throw new IllegalActionException(_actor, e, "Failed to connect loopback port.");
                }
            } else {
                // Port is not a dummy port.
                CommPortIdentifier portID = null;
                try {
                    CommPort port = null;
                    try {
                        portID = CommPortIdentifier.getPortIdentifier(_portName);
                        port = portID.open(_ownerName, _timeout);
                    } catch (NoSuchPortException ex) {
                        // If a model is on a different machine, then the port names maybe different.
                        // As a workaround, we cycle through the ports and open the first available port.
                        // FIXME: Is that really a good idea? Perhaps use dummy ports instead?

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
            _open = true;

            // The RXTX event listener mechanism does not work under OS X.
            // It causes the JVM to crash.
            // _serialPort.addEventListener(this);

            (new Thread(new SerialReader())).start();
            (new Thread(new SerialWriter())).start();
        }
    }
    
    /** Send data over the serial port.
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
        try {
            while (!_buffersToSend.offer(buffer, 1000, TimeUnit.MILLISECONDS)) {
                // Queue is full. Stall the caller.
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                // Don't support image, long or unsigned int types.
                int length = DATA_TYPE.values().length - 1;
                _types = new String[length];
                int i = 0;
                for (DATA_TYPE type : DATA_TYPE.values()) {
                    if (type != DATA_TYPE.IMAGE
                        // Exclude DATA_TYPE.LONG because JavaScript
                        // does not have a representation for longs.
                        // && type != DATA_TYPE.LONG
                        // Unsigned ints cannot be represented as a
                        // JavaScript number.  Interestingly, signed
                        // ints can be, because they are losslessly
                        // convertible to double.  But neither longs
                        // nor unsigned ints are losslessly
                        // convertible to double.
                        // && type != DATA_TYPE.UNSIGNEDINT
                        ) {
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
    ////                         protected methods                 ////

    /** Append data to be sent to the specified buffer.
     *  This overrides the base class to append a null byte to terminate
     *  string and JSON types.
     *  @param data The data to append.
     *  @param type The type of data append.
     *  @param imageType If the type is IMAGE, then then the image encoding to use, or
     *   null to use the default (JPG).
     *  @param buffer The buffer.
     */
    @Override
    protected void _appendToBuffer(
            final Object data, DATA_TYPE type, String imageType, Buffer buffer) {
        super._appendToBuffer(data, type, imageType, buffer);
        if (type == DATA_TYPE.STRING || type == DATA_TYPE.JSON) {
            buffer.appendByte((byte)0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The baud rate for the port. */
    private int _baudRate;
    
    /** A queue of buffers to send over the serial port. */
    private LinkedBlockingQueue<Buffer> _buffersToSend = new LinkedBlockingQueue<Buffer>();
        
    /** Input stream. */
    private InputStream _inputStream;

    /** If the dummy loopback port is used, these streams provide the communication. */
    private static PipedInputStream _loopbackInput;
    private static PipedOutputStream _loopbackOutput;

    /** If the dummy port is used, the user's name is recorded here. */
    private static String _loopbackUser;

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
    
    /** The receive type for this instance of SerialHelper. */
    private DATA_TYPE _receiveType;

    /** The timeout for opening. */
    private int _timeout;

    /** The array of send and receive type names. */
    private static String[] _types;

    /** A mutex used when creating _types. */
    private static Object _typesMutex = new Object();

    /** The send type for this instance of SerialHelper. */
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
                        // To prevent busy waiting, stall.
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // Ignore.
                        }
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
                        if (_receiveType == null) {
                            throw new NullPointerException("_receiveType was null?  Perhaps it was set to an unsupported type?");
                        }
                        int size = _sizeOfType(_receiveType);
                        int numberOfElements = 0;
                        if (size > 0) {
                            numberOfElements = length / size;
                        }
                        // Output each element separately.
                        int position = 0;
                        for (int i = 0; i < numberOfElements; i++) {
                            // System.out.println("SerialHelper.java: SerialReader: extracting " + _receiveType + " " + position);
                            if (_receiveType == DATA_TYPE.IMAGE
                                // || _receiveType == DATA_TYPE.LONG
                                // || _receiveType == DATA_TYPE.UNSIGNEDINT
                                ) {
                                throw new InternalErrorException(_actor, null, "Receive type " + _receiveType + " is not supported.");
                            }
                            _currentObj.callMember("emit", "data", _extractFromBuffer(buffer, _receiveType, position));
                            position += size;
                        }
                        // If there were elements extracted from the buffer,
                        // then start a new buffer.
                        if (numberOfElements > 0) {
                            if (position < length) {
                                // Save bytes from position to length-1 for the future outputs.
                               buffer = buffer.getBuffer(position, length);
                            } else {
                                buffer = Buffer.buffer();
                            }
                        }
                    }
                }
            } catch ( IOException e ) {
                // Do not report the error if it occurs after closing.
                if (_open) {
                    _open = false;
                    _error("Exception occurred reading from serial port.", e);
                }
            }
            _actor.log("Serial port reader thread exiting.");
        }
    }

    /** Write to the serial port.
     */
    public class SerialWriter implements Runnable  {
        public void run () {
            while (_open) {
                Buffer buffer;
                try {
                    buffer = _buffersToSend.poll(1000, TimeUnit.MILLISECONDS);
                    if (buffer != null) {
                        try {
                            _outputStream.write(buffer.getBytes());

                        } catch ( IOException e ) {
                            _error("Exception occurred writing to the serial port.", e);
                        }
                    }
                } catch (InterruptedException e) {
                    // Ignore and continue.
                }
            }
            _actor.log("Serial port writer thread exiting.");
        }
    }
}

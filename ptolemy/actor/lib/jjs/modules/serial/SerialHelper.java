/* Send and receive bytes via the serial port.

 Copyright (c) 2001-2014 The Regents of the University of California.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

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
     *  @param helping The object that this is helping (SerialPort).
     *  @param portName The name of the port to open.
     *  @param ownerName The name of the owner assigned to this port, if opening is successful.
     *  @param timeout Time in milliseconds before failing.
     *  @param options Serial port options (FIXME: define).
     *  @throws NoSuchPortException If there is no such port.
     *  @throws PortInUseException If the port is in use.
     *  @throws TooManyListenersException If there are too many listeners to
     *   the port (this should not occur).
     *  @throws IOException If we can't get an input or output stream for the port.
     */
    public SerialHelper(
            ScriptObjectMirror helping,
            String portName,
            String ownerName, 
            int timeout,
            Object options) {
        super(helping);
        _portName = portName;
        _ownerName = ownerName;
        _timeout = timeout;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the serial port.
     */
    public synchronized void close() {
        _open = false;
        if (_serialPort != null) {
            if (_inputStream != null) {
                try {
                    _inputStream.close();
                } catch (IOException e) {
                    // No idea why closing this would fail.
                    e.printStackTrace();
                }
            }
            if (_outputStream != null) {
                try {
                    _outputStream.flush();
                    _outputStream.close();
                } catch (IOException e) {
                    // No idea why closing this would fail.
                    e.printStackTrace();
                }
            }
            _serialPort.close();
            _serialPort = null;
            _actor.log("Serial port closed.");
        }
    }
    
    /** Open the serial port. If there is already a serial port open,
     *  close it first.
     *  @throws NoSuchPortException If the port does not exist.
     *  @throws PortInUseException If the port is in use (should not be thrown; error invoked instead).
     *  @throws IOException If opening the input or output stream fails.
     *  @throws TooManyListenersException If there are already too many listeners to the port.
     *  @throws UnsupportedCommOperationException If the specified parameters cannot be set on the port.
     */
    public synchronized void open() throws IllegalActionException {
        if(_serialPort != null) {
            _serialPort.close();
        }
        CommPortIdentifier portID = null;
        try {
            portID = CommPortIdentifier.getPortIdentifier(_portName);
            CommPort port = portID.open(_ownerName, _timeout);
            if (!(port instanceof SerialPort)) {
                _error("Port " + _portName + " is not a serial port.");
                return;
            }
            _serialPort = (SerialPort) port;
            
            // FIXME: Set the options.
            _serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            
            // FIXME: Uncomment the next line to avoid jjs/modules/serial/test/auto/SerialHelloWorld.xml hanging.
            //_serialPort.enableReceiveTimeout(_timeout);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Input stream. */
    private InputStream _inputStream;
    
    /** Output stream. */
    private OutputStream _outputStream;
    
    /** The name of the owner. */
    private String _ownerName;
    
    /** The name of the serial port. */
    private String _portName;
    
    /** Indicator of whether the port is open. */
    private boolean _open;
    
    /** The timeout for opening. */
    private int _timeout;

    /** The serial port. */
    private SerialPort _serialPort;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Read from the serial port and emit data.
     */
    public class SerialReader implements Runnable {        
        public void run () {
            byte[] buffer = new byte[1024];
            int length = -1;
            try {
                // Perform blocking read of up to 1024 bytes until
                // end of stream is observed.
                // FIXME: look for a message delimitter here.
                while ((length = _inputStream.read(buffer)) > -1 ) {
                    final String message = new String(buffer, 0, length);
                    _issueResponse(() -> {
                        _currentObj.callMember("emit", "data", message);
                    });
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

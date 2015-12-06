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

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;

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
public class SerialHelper extends HelperBase implements SerialPortEventListener {
    
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
            Object options)
                    throws NoSuchPortException,
                    PortInUseException,
                    TooManyListenersException, IOException {
        super(helping);
        CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
        _serialPort = (SerialPort) portID.open(ownerName, timeout);
        
        // FIXME: Set the options.
        
        _inputStream = _serialPort.getInputStream();
        _outputStream = _serialPort.getOutputStream();
        _serialPort.addEventListener(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the serial port.
     */
    public synchronized void close() {
        if (_serialPort != null) {
            // FIXME: For some reason, upon closing,
            // RXTX goes into an infinite loop in some phantom thread
            // consuming all your CPU.
            _serialPort.removeEventListener();
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
                    _outputStream.close();
                } catch (IOException e) {
                    // No idea why closing this would fail.
                    e.printStackTrace();
                }
            }
            _serialPort.setDTR(false);
            _serialPort.setRTS(false);
            Thread closeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    _serialPort.close();                    
                }
                
            }, "closeThread");
            closeThread.start();
        }
    }

    /** React to an event from the serial port.  This is the one and
     *  only method required to implement SerialPortEventListener
     *  (which this class implements).  Notifies all threads that
     *  are blocked on this PortListener class.
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent e) {
        System.out.println("FIXME: Serial event: " + e);
        if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int bytesAvailable = _inputStream.available();
                final byte[] dataBytes = new byte[bytesAvailable];
                int bytesRead = _inputStream.read(dataBytes, 0, bytesAvailable);
                // FindBugs asks us to check the return value of in.read().
                if (bytesRead != bytesAvailable) {
                    _error(_currentObj, "Read only " + bytesRead
                            + " bytes from the serial port, but was expecting"
                            + bytesAvailable);
                    // Continue to issue the bytes that were received.
                }

                // FIXME: Here is where data types and message framing should be handled.

                _issueResponse(() -> {
                    _currentObj.callMember("emit", "data", dataBytes);
                });
            } catch (IOException e1) {
                _error(_currentObj, "Failed to read serial port", e1);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Input stream. */
    private InputStream _inputStream;
    
    /** Output stream. */
    private OutputStream _outputStream;

    /** The serial port. */
    private SerialPort _serialPort;
}

/* Ptolemy Actors that operate on the Serial port
 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating red (winthrop@eecs.berkeley.edu)
@AcceptedRating red (winthrop@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SerialComm
/**
Sends and receive bytes via the serial port.  Which serial port and
baud rate to use are set by parameters.  If the specified serial port
is not successfully opened, then another one will automatically be
chosen if available.
<p>
Bytes to be sent must enter the actor as an array of integers.
The lowest order byte from each integer is used.  (Negative numbers
are treated as though 256 has been added enough times to make them
non-negative.)  Likewise, bytes received are broadcast out of the actor
<p>
This actor is a class which implements SerialPortEventListener.
This means that when serial events (such as DATA_AVAILABLE) occurr,
this actor's serialEvent() method gets called.  the serialEvent()
method calls the directors fireAt() method, triggering a call to fire().
<p>
By the time fire() executes, there may be several bytes of available
data.  These are packaged by fire() into an array of integers (one int
per byte) and broadcast.
<p>
This approach assumes that the DATA_AVAILABLE event is defined so that
it will occurr only once until the available data has been consumed.
It also assumes that if additional bytes come in between the call to
.available() (which says how many bytes are available) and the call to
.read() (which retrieves them) that the DATA_AVAILABLE event DOES occur
again, even though the serial buffer was never really empty.
<p>
@param baudRate the baud rate (integer such as 19200) to use
        (applies to both input and output)
@param serialPortName the name (string such as COM1) of the serial port

@author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
(Based on my RxDatagram, and on the IRLink class writen by Xiaojun Liu)
@version $Id$
*/
public class SerialComm extends TypedAtomicActor
        implements SerialPortEventListener {

    /** Construct a SerialComm actor with the given container and name.
     *  Construct the serialPortName and baudRate parameters.  Initialize
     *  baudRate to 19200.  Initialize serialPortName to one element of
     *  the enumeration given by a .getPortIdentifiers() call.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SerialComm(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        dataToSend = new TypedIOPort(this, "dataToSend");
        dataToSend.setInput(true);
        dataToSend.setTypeEquals(new ArrayType(BaseType.INT));

        dataReceived = new TypedIOPort(this, "dataReceived");
        dataReceived.setOutput(true);
        dataReceived.setTypeEquals(new ArrayType(BaseType.INT));

        serialPortName = new Parameter(this, "serialPortName",
                new StringToken("<UNKNOWN>"));
        serialPortName.setTypeEquals(BaseType.STRING);

        baudRate = new Parameter(this, "baudRate");
        baudRate.setTypeEquals(BaseType.INT);
        baudRate.setToken(new IntToken(19200));


        Enumeration allPorts = CommPortIdentifier.getPortIdentifiers();
        while(allPorts.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier)allPorts.nextElement();
            if(id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                serialPortName.setToken(new StringToken(id.getName()));
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The port that receives data to be sent via the serial port.
     *  This port has type integer array.  Each integer in the array is
     *  truncated to its least significant byte and output on the serial
     *  port.
     */
    public TypedIOPort dataToSend;

    /** The port that sends data that has been received via the serial port.
     *  This port has type integer array.  All data available on the serial
     *  port is output in an integer array, one byte per integer.
     */
    public TypedIOPort dataReceived;

    /** The name, such as "COM2", of the serial port used.  This
     *  parameter has string type and by default contains the string
     *  corresponding to the name of the first available serial port.
     */
    public Parameter serialPortName;

    /** The baud rate, such as 115200, for the serial port.  This has
     *  type integer and must be one of the following values:
     *  FIXME find the values.  The default is 19200.
     */
    public Parameter baudRate;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>serialPortName</i>, then hope
     *  the model is not running and do nothing.  Likewise for baudRate.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Maybe thrown (?)
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serialPortName || attribute == baudRate) {
            /* Do nothing */
            // One desired behavior would be to use new serial port
            // and/or new baud rate with next transmission and
            // to set to receive on new port and/or at new baud rate.
            // The latter may be tricky since this actor (which is a
            // java class) implements 'SerialPortEventListener'.
            // I'm not sure what happens when baud rate is altered
            // while it is listening.

            // Another possible desired behavior is to alter the baud
            // rate at the end of the current transmission.  This is
            // useful (though not vital) for some hardware.  For example,
            // when a request is issued to the hardware for it to change
            // its baud rate, then it is desirable to be able to quickly
            // change one's own baud rate so as to catch the reply at the
            // new rate.
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Transfers data between the Ptolemy model and the built in
     *  buffers associated with the serial port.  Actual serial
     *  input and output occur right before or right after fire().
     *  <p>
     *  If at least 1 byte is available, broadcast it.
     *  If an integer-array token is available, take a byte out
     *  of each integer and send the byte stream to the serial port.
     *  @exception IllegalActionException Thrown if try fails.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) _debug("Actor is fired");

        try {

            InputStream in = _serialPort.getInputStream();
            int bytesAvailable = in.available();
            if (bytesAvailable > 0) {
                byte[] dataBytes = new byte[bytesAvailable];
                in.read(dataBytes, 0, bytesAvailable);
                Token[] dataIntTokens = new Token[bytesAvailable];
                for (int j = 0; j < bytesAvailable; j++) {
                    dataIntTokens[j] = new IntToken(dataBytes[j]);
                }
                dataReceived.broadcast(new ArrayToken(dataIntTokens));
            }

            if (dataToSend.getWidth() > 0 && dataToSend.hasToken(0)) {
                ArrayToken dataIntArrayToken = (ArrayToken) dataToSend.get(0);
                OutputStream out = _serialPort.getOutputStream();
                for (int j = 0; j < dataIntArrayToken.length(); j++) {
                    IntToken dataIntOneToken =
                            (IntToken)dataIntArrayToken.getElement(j);
                    out.write((byte)dataIntOneToken.intValue());
                }
                out.flush();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, "I/O error: " +
                    ex.getMessage());
        }
    }

    /** Preinitialize does the resource allocation for this actor.
     *  Specifically, it opens the serial port (setting the baud rate
     *  and other communication settings) and then activates the
     *  serial port's event listening resource, directing events to the
     *  serialEvent() method of this actor.  (serialEvent() is the
     *  default name for such a method, it is not explicitly named in
     *  the calls to .addEventListener() and .notifyOnDataAvailable()
     *  below.)
     *  @exception IllegalActionException if the try fails.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            String serialPortNameValue =
                    ((StringToken)(serialPortName.getToken())).stringValue();
            CommPortIdentifier portID =
                    CommPortIdentifier.getPortIdentifier(serialPortNameValue);
            _serialPort = (SerialPort) portID.open("Ptolemy!", 2000);

            int bits_per_second = ((IntToken)(baudRate.getToken())).intValue();
            _serialPort.setSerialPortParams(
                    bits_per_second,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            _serialPort.addEventListener(this);
            _serialPort.notifyOnDataAvailable(true);
            // Directs serial events on this port to my serialEvent method.
        } catch (Exception ex) {
            throw new IllegalActionException(this,
                    "Communication port initialization failed: "
                    + ex);
        }
    }


    /** serialEvent -
     *  The one and only method required to implement SerialPortEventListener
     *  <p>
     *  Call the directors fireAt() method when new data is available.
     *  By the reqirement of serialEvent() implementing
     *  SerialPortEventListener, no exceptions can be thrown.
     *  However, runtime exceptions are always permitted anyway.
     *  Thus KernelRuntimeException is permitted.
     */
    public void serialEvent(SerialPortEvent e) {
        try {
            if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                getDirector().fireAt(this, getDirector().getCurrentTime());
            }
        } catch (Exception ex) {
            // This class implements javax.comm.SerialPortEventListener,
            // which defines serialEvent() so we can't throw
            // an IllegalActionException here so we throw a RuntimeException
            // instead.
            throw new KernelRuntimeException(this,
                    "serialEvent's call to fireAt() failed: "
                    + ex);
        }
    }

    /** Wrap up deallocates resources, specifically the serial port.
     *  @exception IllegalActionException Maybe thrown (?).
     */
    public void wrapup() throws IllegalActionException {
        if (_serialPort != null) {
            _serialPort.close();
            _serialPort = null;  //FIXME Is this necessary?
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // pointer/handle thingy for the serial port.
    private SerialPort _serialPort;

    // Weirdo thing required for accessing the serial port.
    // Somehow the .initialize() call must do something crucial.
    // Removing this code block makes things fail.  Specifically,
    // it makes the 'try' block in fire() have an exception whose
    // message is the word "null".
    static {
        // FIXME: this will not work under anything but Windows
        new com.sun.comm.Win32Driver().initialize();
    }
}

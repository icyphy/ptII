/* Ptolemy Actors that operate on the Serial port
 Copyright (c) 2001-2003 The Regents of the University of California.
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

package ptolemy.actor.lib.io.comm;

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
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.UnsignedByteToken;
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
Sends and receive bytes via the serial port.  The choice of serial port and
baud rate to use are made by parameters.  If the specified serial port
is not successfully opened, then another one will automatically be
chosen if available.

<p>Currently, this actor will only work under Windows, though in
principle it could be made to work on any platform that supports
the Java Comm api.

<p>If you get the following error on the console:
 Error loading win32com: java.lang.UnsatisfiedLinkError:
 no win32com in java.library.path
Then you need to copy the win32com.dll file to some additional directory,
such as c:\jdk1.3\bin.

<p>This actor is designed for use in DE and SDF.  By default, this actor
does not block in fire().  When fired, if no data is available on the
serial port, then no token is produced.  However, when using this actor in
SDF, you may want to experiment with the <i>blocking</i> parameter.
Setting this true has the actor block in fire() ultil data arrives.

<p>Bytes to be sent must enter the actor as an array of integers.
The lowest order byte from each integer is used.  (Negative numbers
are treated as though 256 has been just added enough times to make them
non-negative.)  Likewise, bytes received are broadcast out of the actor
as an integer array of which only the low bytes carry data.  This
actor makes no guarantee as to the contents of the other three bytes
of the integer.

<p>This actor fulfills the SerialPortEventListener interface.
This means that when serial events (such as DATA_AVAILABLE) occur,
the serialEvent() method gets called.  The serialEvent()
method calls the director's fireAtCurrentTime() method, triggering
a call to fire().

<p>By the time fire() executes, there may be several bytes of available
data.  This is primarily because the UART only signals the software every
8 bytes or so.  These are packaged by fire() into an array of integers
(one int per byte) and broadcast.

<p>The fire() method's reading of serial data is also governed by the
<i>blocking</i>, <i>threshold</i>, and <i>truncation</i> parameters.
Fire() first tests to see if there are at least <i>threshold</i> bytes
of data available.  If so, it reads them, or the <i>truncation</i>
most recent bytes, whichever is fewer.  (Or all the bytes, regardless
of how many, if <i>truncation</i> is set to zero.)  If fewer than
<i>threshold</i> bytes are available, then if <i>blocking</i> is false
it will not read any data, but if <i>blocking</i> is true it will wait
until at least <i>threshold</i> bytes are available.

<p>Because the DATA_AVAILABLE event typically occurs every 8 bytes,
continuous data arriving at 115200 baud can wake up the actor 1440
times per second!  This is to often to be calling fireAt*() on
the director of a DE model.  Thus, after the first fireAtCurrentTime()
call, the serialEvent() callback only notifys the fire() method
(in case it is awaiting additional data to meet its <i>threshold</i>)
and does not call fireAt*() again until the actor has completed a
firing since the last time fireAt*() was called.

@author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
(Based on my RxDatagram, and on the IRLink class writen by Xiaojun Liu)
@version $Id$
@since Ptolemy II 2.0
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
        dataToSend.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        dataReceived = new TypedIOPort(this, "dataReceived");
        dataReceived.setOutput(true);
        dataReceived.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        serialPortName = new Parameter(this, "serialPortName",
                new StringToken("<UNKNOWN>"));
        serialPortName.setTypeEquals(BaseType.STRING);

        baudRate = new Parameter(this, "baudRate");
        baudRate.setTypeEquals(BaseType.INT);
        baudRate.setToken(new IntToken(19200));

        threshold = new Parameter(this, "threshold");
        threshold.setTypeEquals(BaseType.INT);
        threshold.setToken(new IntToken(1));

        truncation = new Parameter(this, "truncation");
        truncation.setTypeEquals(BaseType.INT);
        truncation.setToken(new IntToken(0));

        blocking = new Parameter(this, "blocking");
        blocking.setTypeEquals(BaseType.BOOLEAN);
        blocking.setToken(new BooleanToken(false));

        if (false) System.out.println("<>1<>");
        Enumeration allPorts = CommPortIdentifier.getPortIdentifiers();
        while (allPorts.hasMoreElements()) {
            if (false) System.out.println("<>2<>");
            CommPortIdentifier id = (CommPortIdentifier)allPorts.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                serialPortName.setToken(new StringToken(id.getName()));
                break;
            }
        }
        if (false) System.out.println("<>3<>");
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
     *  FIXME find the values.  The default value is 19200.
     */
    public Parameter baudRate;

    /** The threshold, default value 1, for reading data from the
     *  serial port.  Data will not be read until threshold is
     *  reached or surpassed.
     */
    public Parameter threshold;

    /** The truncation cut.  When reading, older bytes are discarded
     *  until <i>truncation</i> or fewer remain.  Caveat: If this
     *  parameter equals 0, than no data is discarded.
     */
    public Parameter truncation;

    /** Whether fire methof is blocking.  If true, fire waits until
     *  <i>threshold</i> bytes have arrived.  Type is boolean.
     */
    public Parameter blocking;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter changed is <i>serialPortName</i>, then hope
     *  the model is not running and do nothing.  Likewise for baudRate.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serialPortName || attribute == baudRate) {
            /* Do nothing */
            // One desired behavior would be to use the new serial port
            // and/or new baud rate with next transmission and
            // to set to receive on the new port and/or at new baud rate
            // with the reception following that transmission.
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
        } else if (attribute == threshold) {
            _threshold = ((IntToken)threshold.getToken()).intValue();
        } else if (attribute == truncation) {
            _truncation = ((IntToken)truncation.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Transfers data between the Ptolemy model and the built in
     *  buffers associated with the serial port.  Actual serial
     *  input and output occur right before or right after fire().
     *  For example, serial output occurs in response to the .flush()
     *  call below.  This data written to the serial port out to the
     *  serial hardware.  The .flush() method does not wait for the
     *  hardware to complete the transmission, as this might take
     *  many milliseconds (roughly 1mS for every 10 bytes at 115200
     *  baud).
     *  <p>
     *  This fire() method checks for either or both of the following
     *  conditions.  Data may have been received and is available in
     *  the serial port.  A Token may have been received by this actor.
     *  If at least 1 byte is available, broadcast it.
     *  If an integer-array token is available, take a byte out
     *  of each integer and send the byte stream to the serial port.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public synchronized void fire() throws IllegalActionException {
        try {
            InputStream in = _serialPort.getInputStream();
            int bytesAvailable;
            // Note: This needs _threshold to be at least 1.
            while ((bytesAvailable = in.available()) < _threshold
                    && _blocking) {
                try{
                    wait();
                } catch (InterruptedException ex) {
                    throw new IllegalActionException(this,"wait interrupted");
                }
            }

            //(moved to finally clause)_directorFiredAtAlready = false;

            if (bytesAvailable >= _threshold) {
                // Read only if the at least desired amount of data is present.
                if (_truncation != 0 && bytesAvailable > _truncation) {
                    // Attempt to skip excess bytes if more than _truncation.
                    // Reduce bytesAvailable by the actual number skipped.
                    bytesAvailable -= (int)in.skip((long)
                            (bytesAvailable-_truncation));
                }
                byte[] dataBytes = new byte[bytesAvailable];
                in.read(dataBytes, 0, bytesAvailable);
                Token[] dataTokens = new Token[bytesAvailable];
                for (int j = 0; j < bytesAvailable; j++) {
                    dataTokens[j] = new UnsignedByteToken(dataBytes[j]);
                }
                dataReceived.broadcast(new ArrayToken(dataTokens));
            }

            if (dataToSend.getWidth() > 0 && dataToSend.hasToken(0)) {
                ArrayToken dataArrayToken = (ArrayToken) dataToSend.get(0);
                OutputStream out = _serialPort.getOutputStream();
                for (int j = 0; j < dataArrayToken.length(); j++) {
                    UnsignedByteToken dataToken =
                        (UnsignedByteToken)dataArrayToken.getElement(j);
                    out.write(dataToken.byteValue());
                }
                out.flush();
            }

        } catch (IOException ex) {
            throw new IllegalActionException(this, "I/O error: " +
                    ex.getMessage());
        } finally {
            _directorFiredAtAlready = false;
        }
    }

    /** [Pre]initialize does the resource allocation for this actor.
     *  Specifically, it opens the serial port (setting the baud rate
     *  and other communication settings) and then activates the
     *  serial port's event listening resource, directing events to the
     *  serialEvent() method of this actor.  (serialEvent() is the
     *  required name for this method.  It is required since this actor
     *  implements SerialPortEventListener.  It is not explicitly named in
     *  the calls to .addEventListener() and .notifyOnDataAvailable()
     *  below.)
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _directorFiredAtAlready = false;
        try {

            String serialPortNameValue =
                ((StringToken)(serialPortName.getToken())).stringValue();
            CommPortIdentifier portID =
                CommPortIdentifier.getPortIdentifier(serialPortNameValue);
            _serialPort = (SerialPort) portID.open("Ptolemy!", 2000);
            // The 2000 above is 2000mS to open the port, otherwise time out.

            int bits_per_second = ((IntToken)(baudRate.getToken())).intValue();
            _serialPort.setSerialPortParams(
                    bits_per_second,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            _serialPort.addEventListener(this);
            _serialPort.notifyOnDataAvailable(true);
            _serialPort.notifyOnDSR(true);//DataSetReady isDSR
            _serialPort.notifyOnCTS(true);//ClearToSend isCTS
            _serialPort.notifyOnCarrierDetect(true);//isCD
            _serialPort.notifyOnRingIndicator(true);//isRI
            // Direct serial events on this port to my serialEvent() method.

        } catch (Exception ex) {
            // Maybe the port was the problem, _debug() the available ports.
            if (_debugging) _debug("Enumerating available ports."
                    + "  Testing which, if any, are serial ports. {");
            Enumeration allPorts = CommPortIdentifier.getPortIdentifiers();
            while (allPorts.hasMoreElements()) {
                CommPortIdentifier id = (CommPortIdentifier)
                    allPorts.nextElement();
                if (_debugging) {
                    _debug("    {");
                    _debug("        id.toString() = " + id.toString());
                    _debug("        id.getName() = " + id.getName());
                    _debug("        id.getPortType() = " + id.getPortType());
                    _debug("        (id.getPortType() == "
                            + " CommPortIdentifier.PORT_SERIAL) = "
                            + (id.getPortType() ==
                                    CommPortIdentifier.PORT_SERIAL));
                    _debug("    }");
                }
            }
            if (_debugging) _debug("}");

            throw new IllegalActionException(this,
                    "Communication port initialization failed: "
                    + " for available ports, 'listen' to actor & rerun "
                    + ex);
        }
    }


    /** React to an event from the serial port.  This is the one and
     *  only method required to implement SerialPortEventListener
     *  (which this class implements).
     *
     *  <p>Call the director's fireAtCurrentTime() method when new data
     *  is available.  By the requirement of serialEvent() implementing
     *  SerialPortEventListener, no exceptions can be thrown.
     *  However, runtime exceptions are always permitted anyway.  Thus
     *  KernelRuntimeException is permitted.
     */
    public synchronized void serialEvent(SerialPortEvent e) {
        if (false) {
            if (e.getEventType() == SerialPortEvent.CD) {
                System.out.println("+++CD+++");
            }
            if (e.getEventType() == SerialPortEvent.CTS) {
                System.out.println("+++CTS+++");
            }
            if (e.getEventType() == SerialPortEvent.RI) {
                System.out.println("+++RI+++");
            }
            if (e.getEventType() == SerialPortEvent.DSR) {
                System.out.println("+++DSR+++");
            }
        }

        try {
            if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                //if (e.getEventType() == SerialPortEvent.RI && !_serialPort.isRI()) {
                if (!_directorFiredAtAlready) {
                    _directorFiredAtAlready = true;
                    getDirector().fireAtCurrentTime(this);
                }
                notifyAll();
            }
        } catch (Exception ex) {
            // This class implements
            // javax.comm.SerialPortEventListener, which defines
            // serialEvent() so we can't throw an
            // IllegalActionException here.  Thus we throw a
            // RuntimeException instead.
            throw new KernelRuntimeException(this,
                    "serialEvent's call to fireAtCurrentTime() failed: "
                    + ex);
        }
    }

    /** Close the serial port.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        if (_serialPort != null) {
            _serialPort.close();
            _serialPort = null;  //FIXME Is this necessary?
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    private SerialPort _serialPort;

    // Threshold for reading serial port data.  Don't read unless.
    // at least this many bytes are available.
    private int _threshold;

    // Truncation cut for older data.  Bytes older than the most
    // recent <i>truncation</i> bytes are skipped.  Exception:
    // If <i>truncation</i> == 0, than nothing is skipped.
    private int _truncation;

    // Whether this actor is blocking (Vs non-blocking).
    private boolean _blocking;

    // True iff fireAtCurrentTime() has been called on the direcror
    // but either director has not yet fired this actor, or it has
    // been fired but fire() has not completed.  Could be in wait().
    private boolean _directorFiredAtAlready;

    // Required for accessing the serial port.
    // Somehow the .initialize() call must do something crucial.
    // Removing this code makes things fail.  Specifically,
    // it makes the 'try' block in fire() have an exception whose
    // message is the word "null".
    static {
        // FIXME: this will not work under anything but Windows
        new com.sun.comm.Win32Driver().initialize();
    }
}



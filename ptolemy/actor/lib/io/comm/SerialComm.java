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
package ptolemy.actor.lib.io.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// SerialComm

/**
 Send and receive bytes via the serial port.  The serial port and
 baud rate are specified by parameters.
 <p>
 This actor requires that the RXTX serial port API be installed
 from
<a href="http://mfizz.com/oss/rxtx-for-java">http://mfizz.com/oss/rxtx-for-java</a>.

 <p>Under Windows, download the zip file, then do something like:</p>
 <pre>
 cp ~/src/rxtx/mfz-rxtx-2.2-20081207-win-x64/RXTXcomm.jar c:/Program\ Files/Java/jdk1.7.0_51/jre/lib/ext/
 cp ~/src/rxtx/mfz-rxtx-2.2-20081207-win-x64/*.dll c:/Program\ Files/Java/jdk1.7.0_51/jre/bin
 </pre>
 <p>And then rerun <code>(cd $PTII;./configure)</code></p>

 <p>This actor can be used in most domains, but the parameters must
 be chosen carefully to match the domain semantics.  This actor sets
 up a listener to the serial port, and when input data appears on the
 serial port, it calls fireAtCurrentTime() on the director.  This
 behavior is useful in the DE domain, for example (although you will
 likely have to set the <i>stopWhenQueueIsEmpty</i> parameter of the
 director false).

 <p> Some domains, however, such as SDF, ignore the fireAtCurrentTime()
 call.  Such domains, will typically fire this actor when its outputs
 are needed. Consequently, for use in such domains, you will likely
 want to set the <i>blocking</i> parameter of this actor to true.
 When this parameter is true, the fire() method first reads data on
 its input port (if any) and writes it to the serial port, and then
 blocks until sufficient input data are available at the serial port.
 It then reads that data from the serial port and packages it as
 a byte array to produce on the output of this actor.

 <p>The inputs and outputs of this actor are unsigned byte arrays.
 The <i>minimumOutputSize</i> parameter specifies the minimum number of
 bytes that are produced on the output in each firing.  The
 <i>maximumOutputSize</i> parameter specifies the maximum number
 of bytes that are produced on the output on each firing. If these
 two numbers are equal, then when a firing produces data, it will
 always produce the same amount of data.  Otherwise, the amount
 of data produced is nondeterministic.

 <p>The <i>discardOldData</i> parameter, if true, indicates
 that the fire() method may discard bytes.  In particular, if
 there are more than <i>maximumOutputSize</i> bytes available
 on the serial port, then all but the most recent <i>maximumOutputSize</i>
 will be discarded.

 <p>For example, if you wish for this actor to produce only the
 most recent byte read on the serial port each time it fires,
 set <i>discardOldData</i> to true, <i>blocking</i> to true,
 and both <i>minimumOutputSize</i> and <i>maximumOutputSize</i>
 to 1.

 <p> If after firing there are additional data available on the
 input port, then the fire() method will call fireAtCurrentTime()
 on the director before returning.
 <p>
 Note that if there are multiple instances of this actor in a model, then unless
 the model designer has taken special care to ensure sequential
 execution of the actor, inputs from the serial port may be
 nondeterministically received by any of the actors.

 @author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
 (Based on my RxDatagram, and on the IRLink class written by Xiaojun Liu)
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating red (winthrop)
 @Pt.AcceptedRating red (winthrop)
 */
public class SerialComm extends TypedAtomicActor {
    /** Construct a SerialComm actor with the given container and name.
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

        serialPortName = new StringParameter(this, "serialPortName");

        // Enumerate the available ports.
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        String defaultChoice = null;

        while (ports.hasMoreElements()) {
            CommPortIdentifier identifier = (CommPortIdentifier) ports
                    .nextElement();

            if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                String value = identifier.getName();
                serialPortName.addChoice(value);

                if (defaultChoice == null) {
                    defaultChoice = value;
                }
            }
        }

        if (defaultChoice == null) {
            defaultChoice = "no ports available";
            serialPortName.addChoice(defaultChoice);
        }

        serialPortName.setExpression(defaultChoice);

        baudRate = new Parameter(this, "baudRate");
        baudRate.setTypeEquals(BaseType.INT);
        baudRate.setToken(new IntToken(19200));

        // FIXME: Should find the available values. How?
        blocking = new Parameter(this, "blocking");
        blocking.setTypeEquals(BaseType.BOOLEAN);
        blocking.setToken(BooleanToken.FALSE);

        minimumOutputSize = new Parameter(this, "minimumOutputSize");
        minimumOutputSize.setTypeEquals(BaseType.INT);
        minimumOutputSize.setToken(new IntToken(1));

        maximumOutputSize = new Parameter(this, "maximumOutputSize");
        maximumOutputSize.setTypeEquals(BaseType.INT);
        maximumOutputSize.setExpression("MaxInt");

        discardOldData = new Parameter(this, "discardOldData");
        discardOldData.setTypeEquals(BaseType.BOOLEAN);
        discardOldData.setToken(BooleanToken.FALSE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The baud rate of the serial port, such as 19200 (the default),
     *  19200, or 115200, for the serial port.  This has
     *  type integer and defaults to 19200.
     */
    public Parameter baudRate;

    /** Indicator of whether fire method is blocking.  If true, fire()
     *  waits until <i>minimumOutputSize</i> bytes have arrived.
     *  The type is boolean with default false.
     */
    public Parameter blocking;

    /** The input port for data to be sent to the serial port.
     *  This port has type unsigned byte array.
     */
    public TypedIOPort dataToSend;

    /** The output port for data that has been received by the serial port.
     *  This port has type unsigned byte array.
     */
    public TypedIOPort dataReceived;

    /** Indicator of whether to discard old data. If this is true,
     *  then the fire() method will read all available data, but produce
     *  no more than <i>maximumOutputSize</i> bytes on the output,
     *  discarding the rest.  This is a boolean that defaults to false.
     */
    public Parameter discardOldData;

    /** The maximum number of bytes produced in each firing on the output.
     *  This is an integer that defaults to MaxInt. It is required to be
     *  at least as large as <i>minimumOutputSize</i>
     */
    public Parameter maximumOutputSize;

    /** The minimum number of bytes that will be read from the serial
     *  port and produced on the output.  This is required to be a
     *  strictly positive integer, and it defaults to 1.
     */
    public Parameter minimumOutputSize;

    /** Attribute giving the serial port to use. This is a string with
     *  the default being the first serial port listed by the
     *  javax.comm.CommPortIdentifier class.  If there are no serial
     *  ports available (meaning probably that the javax.comm package
     *  is not installed properly), then the value of the string will
     *  be "no ports available".
     */
    public StringParameter serialPortName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter changed is <i>serialPortName</i>, then hope
     *  the model is not running and do nothing.  Likewise for baudRate.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
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
        } else if (attribute == minimumOutputSize) {
            _minimumOutputSize = ((IntToken) minimumOutputSize.getToken())
                    .intValue();

            if (_minimumOutputSize < 1) {
                throw new IllegalActionException(this,
                        "minimumOutputSize is required "
                                + "to be strictly positive.");
            }
        } else if (attribute == maximumOutputSize) {
            _maximumOutputSize = ((IntToken) maximumOutputSize.getToken())
                    .intValue();

            if (_maximumOutputSize < 1) {
                throw new IllegalActionException(this,
                        "maximumOutputSize is required "
                                + "to be strictly positive.");
            }
        } else if (attribute == discardOldData) {
            _discardOldData = ((BooleanToken) discardOldData.getToken())
                    .booleanValue();
        } else if (attribute == blocking) {
            _blocking = ((BooleanToken) blocking.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If input data is available at the serial port, read it and
     *  produce it as a byte array at the output port of this actor;
     *  if a token is available at the input port of this actor,
     *  consume it and send the bytes contained by this token to the
     *  serial port.  If <i>blocking</i> is true, then after writing
     *  the input data to the serial port, stall the calling thread
     *  until there are input data available at the serial port.
     *  The <i>minimumOutputSize</i> specifies the minimum number
     *  of bytes that must be available.
     *  <p>
     *  Before returning, if data is sent to the serial port, this
     *  method calls flush(). However, the flush() method does not
     *  wait for the hardware to complete the transmission, as this
     *  might take many milliseconds (roughly 1mS for every 10 bytes
     *  at 115200 baud). Consequently, the data will not have been
     *  completely produced on the serial port when this returns.
     *  <p>
     *  If data is still available on the serial port when this returns,
     *  then before returning it calls fireAtCurrentTime() on the director.
     *  <p>
     *  Before this method exits, it will either call fireAtCurrentTime()
     *  on the director (if there is already enough input data on the serial
     *  port to be able to fire again and produce the requisite number
     *  of outputs), or it will start a thread that will wait until
     *  there is enough data and then call fireAtCurrentTime().
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // This needs to be synchronized on a single global static object.
        synchronized (PortListener.class) {
            // Allow further calls to fireAt().
            _firingRequested = false;
            try {
                // Produce output to the serial port first.
                if (dataToSend.isOutsideConnected() && dataToSend.hasToken(0)) {
                    ArrayToken dataArrayToken = (ArrayToken) dataToSend.get(0);
                    OutputStream out = _serialPort.getOutputStream();
                    int inputLength = dataArrayToken.length();

                    if (_debugging) {
                        _debug("Writing bytes from the input port to the serial port: "
                                + inputLength);
                    }

                    for (int j = 0; j < inputLength; j++) {
                        UnsignedByteToken dataToken = (UnsignedByteToken) dataArrayToken
                                .getElement(j);
                        out.write(dataToken.byteValue());
                    }

                    out.flush();
                }

                InputStream in = _serialPort.getInputStream();
                int bytesAvailable = in.available();

                if (_debugging) {
                    _debug("Number of input bytes available on the serial port: "
                            + bytesAvailable);
                }

                // If _minimumOutputSize is at least 1, then wait for available data.
                // FIXME: stopFire() is called if the serial port receives
                // a byte while we are in the wait() (by the serialEvent()
                // method, through fireAtCurrentTime(), I think). But we
                // don't want to exit the loop if stopFire() is called for
                // this reason.  We want to continue waiting until there
                // is enough input data.  But this means that if stopFire()
                // is called for some other reason, then this actor will
                // ignore the call.  This is not quite right, and could
                // make the actor fail in domains where stopFire() is essential
                // such as PN.
                while (bytesAvailable < _minimumOutputSize && _blocking
                        && !_stopRequested /* && !_stopFireRequested */) {
                    try {
                        if (_debugging) {
                            _debug("Blocking waiting for minimum number of bytes: "
                                    + _minimumOutputSize);
                        }

                        PortListener.class.wait();
                        bytesAvailable = in.available();

                        if (_debugging) {
                            _debug("Number of input bytes available on the serial port: "
                                    + bytesAvailable);
                        }
                    } catch (InterruptedException ex) {
                        throw new IllegalActionException(this,
                                "Thread interrupted waiting for serial port data.");
                    }
                }
                if (bytesAvailable >= _minimumOutputSize
                        && bytesAvailable > 0) {
                    // Read only if at least desired amount of data is present.
                    if (_discardOldData
                            && bytesAvailable > _maximumOutputSize) {
                        // Skip excess bytes.
                        int excess = bytesAvailable - _maximumOutputSize;

                        if (_debugging) {
                            _debug("Discarding input bytes: " + excess);
                        }

                        bytesAvailable -= (int) in.skip(excess);
                    }

                    int outputSize = bytesAvailable;

                    if (outputSize > _maximumOutputSize) {
                        outputSize = _maximumOutputSize;
                    }

                    byte[] dataBytes = new byte[outputSize];

                    if (_debugging) {
                        _debug("Reading bytes from the serial port: "
                                + outputSize);
                    }

                    int bytesRead = in.read(dataBytes, 0, outputSize);
                    // FindBugs asks us to check the return value of in.read().
                    if (bytesRead != outputSize) {
                        throw new IllegalActionException(this,
                                "Read only " + bytesRead + " bytes, expecting"
                                        + outputSize + " bytes.");
                    }

                    Token[] dataTokens = new Token[outputSize];

                    for (int j = 0; j < outputSize; j++) {
                        dataTokens[j] = new UnsignedByteToken(dataBytes[j]);
                    }

                    if (_debugging) {
                        _debug("Producing byte array on the output port.");
                    }

                    dataReceived.broadcast(
                            new ArrayToken(BaseType.UNSIGNED_BYTE, dataTokens));

                }
                // If there are still enough bytes available to run again,
                // then call fireAtCurrentTime().
                int available = in.available();
                if (available >= _minimumOutputSize) {
                    if (_debugging) {
                        _debug("Calling fireAtCurrentTime() to deal with additional bytes: "
                                + available);
                    }

                    getDirector().fireAtCurrentTime(this);
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "I/O error.");
            }
        }
    }

    /** Perform resource allocation for this actor.
     *  Specifically, open the serial port (setting the baud rate
     *  and other communication settings) and then activate the
     *  serial port's event listening resource, directing events to the
     *  serialEvent() method of this actor.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _firingRequested = false;

        try {
            String serialPortNameValue = serialPortName.stringValue();
            CommPortIdentifier portID = CommPortIdentifier
                    .getPortIdentifier(serialPortNameValue);
            synchronized (PortListener.class) {
                if (_serialPort == null || !toplevel().getName()
                        .equals(portID.getCurrentOwner())) {
                    // NOTE: Do not do this in a static initializer so that
                    // we don't initialize the port if there is no actor using it.
                    // This is synchronized on the SerialComm class to prevent multiple actors
                    // from trying to do this simultaneously.
                    _serialPort = portID.open(toplevel().getName(), 2000);
                    if (_serialPortListener == null) {
                        // This should only be done once.
                        _serialPortListener = new PortListener(this);
                    }

                    // The 2000 above is 2000mS to open the port, otherwise time out.
                    int bits_per_second = ((IntToken) baudRate.getToken())
                            .intValue();
                    _serialPort.setSerialPortParams(bits_per_second,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);

                    // NOTE: SerialPort only supports a single
                    // listener. With this design, only one SerialComm actor
                    // will be awakened in DE.  This is fine if there is only
                    // one SerialComm actor, but if there are more, it may
                    // be unexpected.
                    _serialPort.addEventListener(_serialPortListener);
                    _serialPort.notifyOnDataAvailable(true);
                    // _serialPort.notifyOnDSR(true); // DataSetReady isDSR
                    // _serialPort.notifyOnCTS(true); // ClearToSend isCTS
                    // _serialPort.notifyOnCarrierDetect(true); // isCD
                    // _serialPort.notifyOnRingIndicator(true); // isRI
                }
            }
        } catch (PortInUseException ex) {
            throw new IllegalActionException(this, ex,
                    "Port in use by " + ex.getMessage());
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Communication port initialization failed.");
        }
    }

    /** Override the base class to stop waiting for input data.
     */
    @Override
    public void stop() {
        synchronized (PortListener.class) {
            super.stop();
            PortListener.class.notifyAll();
        }
    }

    /** Override the base class to stop waiting for input data.
     */
    @Override
    public synchronized void stopFire() {
        super.stopFire();
        synchronized (PortListener.class) {
            PortListener.class.notifyAll();
        }
    }

    /** Close the serial port.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_serialPort != null) {
            // Close the input stream.
            // Note that if you fail to do this and call
            // removeEventListener(), then the JVM crashes with
            // an access violation.
            try {
                _serialPort.getInputStream().close();
            } catch (IOException e) {
                // Ignore and print stack trace.
                e.printStackTrace();
            }
            // Strangely, this _must_ be called before closing the port.
            _serialPort.removeEventListener();
            _serialPort.close();
            _serialPort = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that firing has already been requested. */
    private boolean _firingRequested = false;

    /** The serial port. */
    private SerialPort _serialPort;

    /** The listener for the serial port. */
    private PortListener _serialPortListener;

    /** Maximum number of output bytes produced in one firing.
     */
    private int _maximumOutputSize;

    /** Threshold for reading serial port data.  Don't read unless
     *  at least this many bytes are available.
     */
    private int _minimumOutputSize;

    /** Indicator to discard older data. */
    private boolean _discardOldData;

    /** Whether this actor is blocking. */
    private boolean _blocking;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The SerialPort class allows only one listener */
    private static class PortListener implements SerialPortEventListener {
        public PortListener(SerialComm actor) {
            _actor = actor;
        }

        /** React to an event from the serial port.  This is the one and
         *  only method required to implement SerialPortEventListener
         *  (which this class implements).  Notifies all threads that
         *  are blocked on this PortListener class.
         */
        @Override
        public void serialEvent(SerialPortEvent e) {
            synchronized (PortListener.class) {
                if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                    if (_actor._debugging) {
                        _actor._debug(
                                "Received serial event indicating that data is available.");
                    }
                    if (!_actor._firingRequested) {
                        try {
                            if (_actor._debugging) {
                                _actor._debug("Requesting a refiring.");
                            }
                            _actor.getDirector().fireAtCurrentTime(_actor);
                            _actor._firingRequested = true;
                        } catch (IllegalActionException e1) {
                            MessageHandler.error(
                                    "Director is uanble to respond to a firing request.",
                                    e1);
                        }
                    }
                    // In case the actor is blocked waiting for data.
                    PortListener.class.notifyAll();
                }
            }
        }

        private SerialComm _actor;
    }
}

/* An actor that operates a serial port.

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

package ptolemy.actor.lib.io;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.actor.lib.*;

import javax.comm.*;
import java.io.*;
import java.net.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SerialComm
/**
This actor sends and receives integer arrays via the serial port.
Each integer represents a byte, and is truncated to 8 bits prior 
to transmission.

This actor contains a nested class which implements the 
SerialPortEventListener to recieve from the serial port and calls
the director's fireAt method to broadcast the bytes received as 
an array of integers.

This actor has a parameter 'serialPortName' for the serial port.
@author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
(Based on my RxDatagram, and on the IRLink class writen by Xiaojun Liu)
@version $Id$
*/
public class SerialComm extends TypedAtomicActor 
        implements SerialPortEventListener {

    public SerialComm(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container,name);

        dataToSend = new TypedIOPort(this, "dataToSend");
        dataToSend.setInput(true);
        dataToSend.setTypeEquals(new ArrayType(BaseType.INT));

        dataReceived = new TypedIOPort(this, "dataReceived");
        dataReceived.setOutput(true);
        dataReceived.setTypeEquals(new ArrayType(BaseType.INT));

        serialPortName = new Parameter(this, "serialPortName");
        serialPortName.setTypeEquals(BaseType.STRING);

        baudRate = new Parameter(this, "baudRate");
        baudRate.setTypeEquals(BaseType.INT);
        baudRate.setToken(new IntToken(19200));


	  Enumeration allPorts = CommPortIdentifier.getPortIdentifiers();
	  while (allPorts.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier)allPorts.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                serialPortName.setToken(new StringToken(id.getName()));
                return;
            }
        }
        serialPortName.setToken(new StringToken("<UNKNOWN>"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This port inputs the data to be sent to the serial port.
        Value of each integer is masked to the range 0 .. 255.
     */
    public TypedIOPort dataToSend;

    /** This port outputs the data received, one byte per integer in
     * the array.
     */
    public TypedIOPort dataReceived;

    /** The name, such as COM2, of the serial port used.
     */
    public Parameter serialPortName;

    /** The baud rate for the serial port.
     */
    public Parameter baudRate;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>serialPortName</i>, then hope 
     * the model is not running and do nothing.  Likewise if baudRate.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serialPortName || attribute == baudRate) {
            /* Do nothing */
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Broadcasts the data if received, & outputs bitstream if token.
     */
    public void fire() throws IllegalActionException {      
        //System.out.println("Actor is fired");

        try {

            InputStream in = serialPort.getInputStream();
            int bytesAvail = in.available();
            if (bytesAvail > 0) {
                byte[] dataBytes = new byte[bytesAvail];
                in.read(dataBytes, 0, bytesAvail);
                Token[] dataIntTokens = new Token[bytesAvail];
                for (int j = 0; j < bytesAvail; j++) {
                    dataIntTokens[j] = new IntToken(dataBytes[j]);
                }
                dataReceived.broadcast(new ArrayToken(dataIntTokens));
            }

            if (dataToSend.getWidth() > 0 && dataToSend.hasToken(0)) {
                ArrayToken dataIntArrayToken = (ArrayToken) dataToSend.get(0);
                OutputStream out = serialPort.getOutputStream();
                for (int j = 0; j < dataIntArrayToken.length(); j++) {
                    IntToken dataIntOneToken =
                        (IntToken) dataIntArrayToken.getElement(j);
                    out.write((byte)dataIntOneToken.intValue());
                }
                out.flush();
            }

        } catch (Exception ex) {
            System.err.println("Win0" + ex.getMessage());
            throw new IllegalActionException(this,
                    "I/O error " + ex.getMessage());
        }

    }

    /** Preinitialize
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        try {
            String _serialPortName =
                ((StringToken)(serialPortName.getToken())).stringValue();
            CommPortIdentifier portID =
                CommPortIdentifier.getPortIdentifier(_serialPortName);
            serialPort = (SerialPort) portID.open("Ptolemy!", 2000);

            int bits_per_second = ((IntToken)(baudRate.getToken())).intValue();
            serialPort.setSerialPortParams(bits_per_second,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            // Directs serial events on this port to my serialEvent method.
        } catch (Exception ex) {
            System.err.println("Win1 " + ex.getClass().getName()
                    + " " + ex.getMessage());
        }
    }


    /** serialEvent - The one and only method 
     *  required to implement SerialPortEventListener
     */
    public void serialEvent(SerialPortEvent e) {
        try {
            if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                getDirector().fireAt(this, getDirector().getCurrentTime());
            }
        } catch (Exception ex) {
            System.err.println("Win2 " + ex.getMessage());
        }
    }

    /** Wrap up
     */
    public void wrapup() throws IllegalActionException {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Variables
    private SerialPort serialPort;


    static {
        CommDriver driver = new com.sun.comm.Win32Driver();
        driver.initialize();
    }
}


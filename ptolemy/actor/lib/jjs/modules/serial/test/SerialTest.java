/* Simple test of the serial port.

 Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.serial.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// SerialTest

/**
 Test the serial port.

 <p>Based on <a href="https://github.com/NeuronRobotics/nrjavaserial#in_browser" target="_top">https://github.com/NeuronRobotics/nrjavaserial</a>.</p>

 <p>See <a href="https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/RxTxHanging#in_browser" target="_top">https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/RxTxHanging</a>.</p>

 @author Christopher Brooks, based on https://github.com/NeuronRobotics/nrjavaserial
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (cxh)
 @Pt.AcceptedRating red (cxh)
 */
public class SerialTest {
    /**
     * Read and write to a port.
     *
     * <p>Usage:  To see what ports are available:</p>
     * <pre>
     * java -classpath $PTII:${PTII}/lib/nrjavaserial-3.11.0.jar ptolemy.actor.lib.jjs.modules.serial.test.SerialTest
     * </pre>
     *
     * <p>To connect to a port:</p>
     * <pre>
     * java -classpath $PTII:${PTII}/lib/nrjavaserial-3.11.0.jar ptolemy.actor.lib.jjs.modules.serial.test.SerialTest /dev/tty.usbserial-DA01R74U
     * </pre>
     */
    public static void main(String [] args) throws Exception {

        String port = "/dev/tty.Bluetooth-Incoming-Port";

        if (args.length != 1) {
            System.err.println("Usage: java -classpath $PTII:${PTII}/lib/nrjavaserial-3.11.0.jar ptolemy.actor.lib.io.comm.SerialTest /dev/tty");
            System.err.println("Below are the ports:");
            // Enumerate the available ports.
            Enumeration ports = CommPortIdentifier.getPortIdentifiers();
            while (ports.hasMoreElements()) {
                CommPortIdentifier identifier = (CommPortIdentifier) ports
                    .nextElement();
                if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    System.err.println(identifier.getName());
                }
            }
            StringUtilities.exit(1);
        } else {
            port = args[0];
        }

        int baudRate = 9600;
        NRSerialPort serialPort = new NRSerialPort(port, baudRate);
        serialPort.connect();
        System.out.println("Connected to " + serialPort);

        DataInputStream inputStream = new DataInputStream(serialPort.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(serialPort.getOutputStream());

        byte b = (byte)inputStream.read();
        System.out.println("Read a byte: " + b);
        outputStream.write(b);
        System.out.println("Wrote a byte: " + b);

        serialPort.disconnect();
        System.out.println("Called serialPort.disconnect()");
    }
}

/* Test the nrjavaserial locking mechansim

 Copyright (c) 2015 The Regents of the University of California.
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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.TooManyListenersException;

///////////////////////////////////////////////////////////////////
//// SerialPortLockTest

/**
 Test the Serial Port locking
 
 @author Christopher Brooks
 @version $Id: SerialHelper.java 74187 2015-12-27 21:32:38Z cxh $
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (cxh)
 @Pt.AcceptedRating red (cxh)
 */
public class SerialPortLockTest {

    /**
     * Test for a locking problem
     * <p>Usage:</p>
     * <pre>
     * java -classpath ${PTII}:${PTII}/lib/nrjavaserial-3.11.0.devel.jar ptolemy.actor.lib.jjs.modules.serial.test.SerialPortLockTest
     * </pre>
     */
    public static void main(String [] args) {
        try {
            System.out.println("Opening Port 1");
            CommPort port1 = SerialPortLockTest.openPort();
            System.out.println("Port 1: " + port1);
            System.out.println("Opening Port 2");
            CommPort port2 = SerialPortLockTest.openPort();
            System.out.println("Port 2: " + port2);
            System.out.println("Closing port1: ");
            port1.close();
            System.out.println("Closing port2: ");
            port2.close();
            System.out.println("Done.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /** Open the first non-Bluetooth and non-cu Serial port.
     *  @return the port that was opened
     *  @exception Throwable If looking up a port failed.
     */
    public static CommPort openPort() throws Throwable {
        System.out.println("Calling gnu.io.CommPortIdentifier.getPortIdentifiers()");
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        System.out.println("Done calling gnu.io.CommPortIdentifier.getPortIdentifiers()");
        while (ports.hasMoreElements()) {
            CommPortIdentifier identifier = (CommPortIdentifier) ports
                .nextElement();
            if (identifier.getName().indexOf("/dev/cu.") != -1 
                || identifier.getName().indexOf("Bluetooth") != -1) {
                System.out.println("Skipping " + identifier.getName());
            } else {
                CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(identifier.getName());
                CommPort port = portID.open("SerialPortLockTest", 1000 /* timeout for opening */);
                return port;
            }
        }
        throw new RuntimeException("Could not find any non-Bluetooth, non tty serial ports.");
    }
}
/* SerialPortReader handles reading from the serial port on a lower level

// Copyright (c) 2015 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
*/
package ptolemy.actor.lib.jjs.modules.IMUSensor;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/////////////////////////////////////////////////////////////////////////
///// SerialPortReader

/**
 * SerialPortReader is a class meant for reading in bytes from the
 * serial port connection and providing them to the ReaderM class for
 * decoding of the unique packet format used by the IMU sensors.
 *
 * @author Hunter Massey
 * @version $Id$
 * @see SerialPortController
 * @see ReaderM
 * @Pt.ProposedRating Yellow Hunter
 * @Pt.AcceptedRating
 */
public class SerialPortReader extends Thread {

    /** Construct a serial port reader.
     *  Determines OS and chooses serial port name prefix accordingly.
     */
    public SerialPortReader() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            // In a windows environment, use the following
            portName = "COM";
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("mac") >= 0) {
            // In a linux (also, Mac?) environment, use the following
            portName = "/dev/ttyS";
        } else {
            System.out.println("OS not supported!");
        }

        charBuf = new LinkedBlockingQueue<Character>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////


    /**	Initialize the serial port and connect a BufferedInputStream to the serialPort.
     *  @param portNumber The number of theport   
     */
    public int init(int portNumber) {
        portName += portNumber;
        try {
            commID = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException exception) {
            // If port not found, return -1 and print exception			
            exception.printStackTrace();
            return -1;
        }

        try {
            // Opens the serial port, setting the owner to SerialPortReader
            // This will wait up to 5000 ms for the port to open before failing.
            // Additionally, since the return value is an interface, we must cast
            // it to be a SerialPort
            serialPort = (SerialPort) commID.open("SerialPortReader", 5000);
        } catch (PortInUseException exception) {
            // If port is in use when attempting to open,
            // return -1 and print exception
            exception.printStackTrace();
            return -1;
        }

        try {
            // Obtain the InputStream associated with the serial port
            inStream = new BufferedInputStream(serialPort.getInputStream());
        } catch (IOException exception) {
            exception.printStackTrace();
            return -1;
        }

        try {
            // Set the baud rate, data bits, stop bits, and parity bits of the port
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException exception) {
            exception.printStackTrace();
            return -1;
        }

        this.start();
        return 1;
    }

    /** Run method for this class. Starts a thread that constantly
     *	reads in from serial port stream A BufferedInputStream
     *	connected to the serial port is read into a
     *	LinkedBlockingQueue.
     */
    @Override
    public void run() {
        threadIsActive = true;
        int readChar = -1;

        try {
            while (threadIsActive) {
                // Keep attempting to read from BufferedInputStream until success
                while ((readChar = inStream.read()) != -1) {
                    Character charToBuf = Character.valueOf((char) readChar);
                    try {
                        charBuf.put(charToBuf);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /** Takes the next single value from the buffer of Characters read
     *	from the input stream.
     *	@return the character read from serial port as a String
    */
    public String readFromPort() {
        String returnVal = "";
        try {
            returnVal = charBuf.take().toString();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        return returnVal;
    }

    /**	Closes the serial port connection.
     *	Called by class that owns this object to stop serial port connection
     */
    public void closePort() {
        stopThread();
        try {
            // If there is no serial port, then inStream might be null.
            if (inStream != null) {
                inStream.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        serialPort.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    /** Sets the boolean controlling the main reader thread's while
     *	loop to false Called when we wish to close the program's
     *	connection to the serial port.
    */
    private void stopThread() {
        threadIsActive = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** A boolean stating whether the read thread is active. */
    private boolean threadIsActive; 

    /** An InputStream connected to serialPort. */
    private BufferedInputStream inStream; 

    /**  The communication port identifier used to open serialPort. */
    private CommPortIdentifier commID; 

    /**  A concurrent Queue implementation for storing read
     *  characters.
     */
    private LinkedBlockingQueue<Character> charBuf; 

    /** The serialPort object referencing the incoming serial
     * connection.
     */
    private SerialPort serialPort; 

    /** The name of the serial port that serialPort is to connect to -
     * varies by OS.
     */
    private String portName; 

}

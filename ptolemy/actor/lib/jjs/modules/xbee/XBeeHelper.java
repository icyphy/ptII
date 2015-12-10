/* Generate "Hello XBee World!" on a XBee radio connected to a serial port.

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

package ptolemy.actor.lib.jjs.modules.xbee;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.util.StringUtilities;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;

///////////////////////////////////////////////////////////////////
//// XBeeHelper

/**
Helper for XBee radio modules.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 11.0
@Pt.ProposedRating red (winthrop)
@Pt.AcceptedRating red (winthrop)
*/
public class XBeeHelper extends VertxHelperBase {
    
    /** Create an XBee device.
     *  The first argument is an instance of the JavaScript XBee object.
     *  @param helping The object that this is helping (XBee).
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
    public XBeeHelper(
            ScriptObjectMirror helping,
            String portName,
            String ownerName, 
            int timeout,
            Object options)
                    throws NoSuchPortException,
                    PortInUseException,
                    TooManyListenersException, IOException {
        super(helping);
        
        // FIXME: Configure using SerialPortParameters argument.
        _device = new XBeeDevice(portName, 9600);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Close the port, if it is open. */
    public void close() {
        if (_device != null && _device.isOpen()) {
            _device.close();
        }
    }

    /** FIXME: Remove this main function.
     * Generate "Hello XBee World!" on a XBee radio connected to a serial port.
     *
     * <p>To send data, the two radios need to be in api mode.  See
     * <a href="https://docs.digi.com/display/XBJLIB/Building+your+first+XBee+Java+application#in_browser" target="_top">https://docs.digi.com/display/XBJLIB/Building+your+first+XBee+Java+application</a>.</p>
     *
     * <p>To list the ports, use:</p>
     * <pre>
     * java -classpath ${PTII}/lib/lib/nrjavaserial-3.11.0.devel.jar:${PTII}/lib/lib/xbjlib-1.1.0.nrjavaserial.jar:${PTII}/lib/lib/slf4j-api-1.7.12.jar:${PTII}/lib/lib/slf4j-nop-1.7.12.jar ptolemy.actor.lib.jjs.modules.xbee.XBeeHello
     * </pre>
     *
     * <p>To send data on a port, append the port name:</p>
     * <pre>
     * java -classpath ${PTII}/lib/lib/nrjavaserial-3.11.0.devel.jar:${PTII}/lib/lib/xbjlib-1.1.0.nrjavaserial.jar:${PTII}/lib/lib/slf4j-api-1.7.12.jar:${PTII}/lib/lib/slf4j-nop-1.7.12.jar ptolemy.actor.lib.jjs.modules.xbee.XBeeHello /dev/xxyy
     * </pre>
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("java -classpath ... ptolemy.actor.lib.jjs.modules.xbee.XBeeHello /dev/xxyy"); 
            System.err.println("Available ports are:");
            Enumeration ports = CommPortIdentifier.getPortIdentifiers();
            while (ports.hasMoreElements()) {
                CommPortIdentifier identifier = (CommPortIdentifier) ports
                    .nextElement();
                System.err.println(identifier.getName());
            }
            StringUtilities.exit(1);
        }

        int baudRate = 9600;
        XBeeDevice xBeeDevice = new XBeeDevice(args[0], baudRate);
        String dataToSend = "Hello XBee World";
        byte[] dataToSendBytes = dataToSend.getBytes();
         
        try {
            xBeeDevice.open();

            System.out.println("Sending broadcast data \"" + dataToSend + "\"");
             
            xBeeDevice.sendBroadcastData(dataToSendBytes);
             
            System.out.println("Successfully sent broadcast data");
             
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            StringUtilities.exit(1);
        } finally {
            xBeeDevice.close();
        }
    }
    
    /** Send data over the radio.
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
                    // FIXME: Support more data types.
                    _appendToBuffer(element, DATA_TYPE.STRING, null, buffer);
                }
            }
        } else {
            _appendToBuffer(data, DATA_TYPE.STRING, null, buffer);
        }
        if (!_device.isOpen()) {
            try {
                _device.open();
            } catch (XBeeException e) {
                _error(_actor.getFullName() + ": Failed to open XBee device.", e);
            }
        }
        try {
            _device.sendBroadcastData(buffer.getBytes());
        } catch (TimeoutException e) {
            _error(_actor.getFullName() + ": Timeout on send.", e);
        } catch (XBeeException e) {
            _error(_actor.getFullName() + ": Failed to send data.", e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The device. */
    XBeeDevice _device;
}

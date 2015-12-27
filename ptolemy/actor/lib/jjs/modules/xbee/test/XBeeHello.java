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

package ptolemy.actor.lib.jjs.modules.xbee.test;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;

import gnu.io.CommPortIdentifier;

import java.util.Enumeration;

import ptolemy.util.StringUtilities;
/** 
 * Generate "Hello XBee World!" on a XBee radio connected to a serial port.
 *
 * <p>Based on <a href="https://docs.digi.com/display/XBJLIB/Building+your+first+XBee+Java+application#in_browser" target="_top">https://docs.digi.com/display/XBJLIB/Building+your+first+XBee+Java+application</a>.</p>
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class XBeeHello {
    /**
     * Generate "Hello XBee World!" on a XBee radio connected to a serial port.
     *
     * <p>To send data, the two radios need to be in api mode.  See
     * <a href="https://docs.digi.com/display/XBJLIB/Building+your+first+XBee+Java+application#in_browser" target="_top">https://docs.digi.com/display/XBJLIB/Building+your+first+XBee+Java+application</a>.</p>
     *
     * <p>To list the ports, use:</p>
     * <pre>
     * java -classpath ${PTII}:${PTII}/lib/nrjavaserial-3.11.0.devel.jar:${PTII}/lib/xbjlib-1.1.0.nrjavaserial.jar:${PTII}/lib/slf4j-api-1.7.12.jar:${PTII}/lib/slf4j-nop-1.7.12.jar ptolemy.actor.lib.jjs.modules.xbee.test.XBeeHello
     * </pre>
     *
     * <p>To send data on a port, append the port name:</p>
     * <pre>
     * java -classpath ${PTII}:${PTII}/lib/nrjavaserial-3.11.0.devel.jar:${PTII}/lib/xbjlib-1.1.0.nrjavaserial.jar:${PTII}/lib/slf4j-api-1.7.12.jar:${PTII}/lib/slf4j-nop-1.7.12.jar ptolemy.actor.lib.jjs.modules.xbee.test.XBeeHello /dev/xxyy
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
}

/* ReadSerial is a class that opens a new thread to read latest messages from the serial port and places them in a SerialBuffer
*/
package ptolemy.actor.lib.jjs.modules.IMUSensor.io;

import java.io.InputStream;
import java.io.IOException;

/**
 *
 * This class reads message from the specific serial port and save
 * the message to the serial buffer.
 *
 */
public class ReadSerial extends Thread {
    private SerialBuffer ComBuffer;
    private InputStream ComPort;
		public boolean read = true;

    /**
     *
     * Constructor
     *
     * @param SB The buffer to save the incoming messages.
     * @param Port The InputStream from the specific serial port.
     *
     */
    public ReadSerial(SerialBuffer SB, InputStream Port) {
        ComBuffer = SB;
        ComPort = Port;
    }

    public void run() {
        int c;
        try {
            while (read) {
                c = ComPort.read();
                ComBuffer.PutChar(c);
            }
        } catch (IOException e) {
        }
    }
}

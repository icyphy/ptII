package ptolemy.actor.lib.jjs.modules.xbee;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;

public class XBeeHello {
    /* Constants */
    // TODO Replace with the port where your sender module is connected to.
    private static final String PORT = "/dev/tty.usbserial-DA01LK3S";
    // TODO Replace with the baud rate of your sender module.
    private static final int BAUD_RATE = 9600;
     
    private static final String DATA_TO_SEND = "Hello XBee World!";
     
    public static void main(String[] args) {
        XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);
        byte[] dataToSend = DATA_TO_SEND.getBytes();
         
        try {
            myDevice.open();
             
            System.out.format("Sending broadcast data: '%s'", new String(dataToSend));
             
            myDevice.sendBroadcastData(dataToSend);
             
            System.out.println(" >> Success");
             
        } catch (XBeeException e) {
            System.out.println(" >> Error");
            e.printStackTrace();
            System.exit(1);
        } finally {
            myDevice.close();
        }
    }
}
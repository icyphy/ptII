/* SerialBean class provides serial port data exchange operations
*/
package ptolemy.actor.lib.jjs.modules.IMUSensor;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * This bean provides some basic functions to implement full dulplex
 * information exchange through the serial port.
 */
public class SerialBean {

    public int baudRate = 115200;

    String PortName;
    CommPortIdentifier portId;
    SerialPort serialPort;
    OutputStream out;
    InputStream in;
    SerialBuffer SB;
    ReadSerial RT;

    /**
     *
     * Constructor
     *
     * @param PortID the ID of the serial to be used. 1 for COM1,
     * 2 for COM2, etc.
     *
     */
    public SerialBean(int PortID) {
        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.indexOf("win") >= 0){
                // In a windows environment, use the following
                PortName = "COM" + PortID;
        }
        else if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("mac") >= 0){
                // In a linux (also, Mac?) environment, use the following
                PortName = "/dev/ttyS" + PortID;
        }
        else{
                System.out.println("OS not supported!");
        }
    }

    /**
     *
     * This function initialize the serial port for communication. It startss a
     * thread which consistently monitors the serial port. Any signal capturred
     * from the serial port is stored into a buffer area.
     *
     */
    public int Initialize() {
        int InitSuccess = 1;
        int InitFail = -1;
        //System.out.println("started initialize");
        try {
                //System.out.println(PortName);
            portId = CommPortIdentifier.getPortIdentifier(PortName);
            try {
                serialPort = (SerialPort) portId.open("Serial_Communication",
                        2000);
            } catch (PortInUseException e) {
                e.printStackTrace();
                return InitFail;
            }
            //Use InputStream in to read from the serial port, and OutputStream
            //out to write to the serial port.
            try {
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return InitFail;
            }
            //Initialize the communication parameters to 9600, 8, 1, none.
            try {
                serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                e.printStackTrace();
                return InitFail;
            }
        } catch (NoSuchPortException e) {
                e.printStackTrace();
            return InitFail;
        }
        // when successfully opened the serial port,  create a new serial buffer,
        // then create a thread that consistently accepts incoming signals from
        // the serial port. Incoming signals are stored in the serial buffer.
        SB = new SerialBuffer();
        RT = new ReadSerial(SB, in);
        RT.start();
        // return success information
        return InitSuccess;
    }

    /**
     *
     * This function returns a string with a certain length from the incomin
     * messages.
     *
     * @param Length The length of the string to be returned.
     *
     */
    public String ReadPort(int Length) {
        String Msg = null;
        try{
        Msg = SB.GetMsg(Length);
        } catch(Exception e) {e.printStackTrace();}
        return Msg;
    }

    /**
     *
     * This function sends a message through the serial port.
     *
     * @param Msg The string to be sent.
     *
     */
    public void WritePort(String Msg) {
        //int c;
        try {
            for (int i = 0; i < Msg.length(); i++)
                out.write(Msg.charAt(i));
        } catch (IOException e) {
        }
    }

    /**
     *
     * This function closes the serial port in use.
     *
     */
    public void closePort() {
                                RT.read = false;
                          try {
                                        in.close();
                                        out.close();
                                } catch(Exception e) {}
                                //serialPort.removeEventListener();
                                serialPort.close();
    }
}

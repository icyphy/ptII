package ptolemy.domains.dd3d.lib;

import java.io.*;
import java.util.*;
import java.lang.*;
import javax.comm.*;


import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.*;



public class SerialWriter extends TypedAtomicActor {

    public SerialWriter(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);
        _init();

        input = new TypedIOPort(this,"input");
        input.setInput(true);
        input.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void initialize() throws IllegalActionException {
        try {
            outputStream.write(255);
            outputStream.write(0);
            outputStream.write(165);
            outputStream.write(255);
            outputStream.write(1);
            outputStream.write(128);
            outputStream.write(255);
            outputStream.write(2);
            outputStream.write(128);
            outputStream.write(255);
            outputStream.write(3);
            outputStream.write(115);
            outputStream.write(255);
            outputStream.write(4);
            outputStream.write(128);
        } catch (Exception e) {}
    }


    public final void fire() throws IllegalActionException {
        if (input.getWidth() != 0) {
            if (input.hasToken(0)) {
                for (int i=0;i<3;i++) {
                    byte serialData = (byte) ((IntToken) input.get(0)).intValue();
                    //System.out.println((int) serialData);
                    try {
                        outputStream.write(serialData);
                    } catch (Exception e) {}
                }
            }
        }
    }


    private void _init() {

        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier portId = null;
        SerialPort serialPort = null;


        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                System.out.println(portId.getName());
                if (portId.getName().equals("COM2")) {

                    try {
                        serialPort = (SerialPort) portId.open("SimpleWriteApp", 2000);
                        System.out.println("Serial Port open "+serialPort);
                    } catch (PortInUseException e) {
                        System.out.println("Port already in use");
                    }

                    try {
                        outputStream = serialPort.getOutputStream();
                    } catch (IOException e) {
                        System.out.println("setup of Streams failed");
                    }

                    try {
                        serialPort.setSerialPortParams(9600,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    } catch (UnsupportedCommOperationException e) {
                        System.out.println("setup failed");
                    }
                }
            }
        }

    }

    private OutputStream outputStream = null;
}

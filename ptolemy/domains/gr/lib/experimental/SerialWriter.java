/*
  @Copyright (c) 2000-2005 The Regents of the University of California.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the
  above copyright notice and the following two paragraphs appear in all
  copies of this software.

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
package ptolemy.domains.dd3d.lib;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dt.kernel.*;
import ptolemy.kernel.util.*;

import java.io.*;
import java.lang.*;
import java.util.*;

import javax.comm.*;


public class SerialWriter extends TypedAtomicActor {
    public SerialWriter(TypedCompositeActor container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();

        input = new TypedIOPort(this, "input");
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
        } catch (Exception e) {
        }
    }

    public final void fire() throws IllegalActionException {
        if (input.getWidth() != 0) {
            if (input.hasToken(0)) {
                for (int i = 0; i < 3; i++) {
                    byte serialData = (byte) ((IntToken) input.get(0)).intValue();

                    //System.out.println((int) serialData);
                    try {
                        outputStream.write(serialData);
                    } catch (Exception e) {
                    }
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
                        serialPort = (SerialPort) portId.open("SimpleWriteApp",
                                2000);
                        System.out.println("Serial Port open " + serialPort);
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
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
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

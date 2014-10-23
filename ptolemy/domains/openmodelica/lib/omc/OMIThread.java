/* OMIThread runs key modules of the OpenModelica Interactive (OMI).
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package ptolemy.domains.openmodelica.lib.omc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;

/**
      <p>OMIThread runs key modules of OMI: Transfer and Client modules.</P>
      <p>Transfer module tries to get simulation results from a result manager and sends them to the Ptolemy II
      immediately after starting a simulation.</P>
      <p>The Control module is the interface between OMI and Ptolemy II. It is implemented as a single thread to
      support parallel tasks and independent reactivity. As the main controlling and communication instance at
      simulation initialization phase and while simulation is running it manages simulation properties and also
      behavior. A client can permanently send operations as messages to the Control unit, it can react at any
      time to feedback from the other internal OMI components and it also sends messages to a client,
      for example error or status messages.</P>
      <p>The network communication technology TCP/IPv4 is used for sending and receiving messages.
      The above modules are designated for a communication over TCP/IP.</P>
      <p>Prior to running the Transfer and Control module by starting the OMIThread, client and servers are created
      ,the IP and ports of the servers are set and streams for transferring information between OMI and Ptolemy II
      are sets up.</p>

      @author Mana Mirzaei
      @version $Id$
      @since Ptolemy II 10.0
      @Pt.ProposedRating Red (cxh)
      @Pt.AcceptedRating Red (cxh)
 */
public class OMIThread extends Thread {
    /** Construct client and servers, set the IP and ports of the servers and set up streams for transferring information
     *  between client and servers.
     *  @param parameterFilter Filter for showing the result of the simulation..
     *  @param stopTime Stop time of the simulation.
     *  @param outputPort The output port that the result of simulation should be sent to.
     *  @exception IOException If I/O error occurs while creating sockets and streams.
     */
    public OMIThread(String parameterFilter, String stopTime,
            TypedIOPort outputPort) throws IOException {

        // In an interactive processing mode, the simulation does not stop automatically at the stop time that
        // is selected as one of the OpenModelica actors' parameters. So during reading the result back from
        // Transfer server, stop time should be checked.
        _stopTime = stopTime;
        _parameterFilter = parameterFilter;
        _outputPort = outputPort;

        _omcLogger = OMCLogger.getInstance();

        _controlClient = new Socket("localhost", 10501);

        // Set up an output stream to send the operation to Control Server.
        _toServer = new BufferedWriter(new OutputStreamWriter(
                _controlClient.getOutputStream()));

        // Set the protocol of Control Server.
        _toServer.write("setcontrolclienturl#1#127.0.0.1#10500#end");
        _toServer.flush();

        _controlServer = new ServerSocket(10500);
        _controlConnection = _controlServer.accept();

        // Set up an input stream to receive the simulation result back from Control Server.
        _inFromControlServer = new BufferedReader(new InputStreamReader(
                _controlConnection.getInputStream()));

        // Set the protocol of Transfer Server.
        _toServer.write("settransferclienturl#2#127.0.0.1#10502#end");
        _toServer.flush();

        _transferServer = new ServerSocket(10502);
        _transferConnection = _transferServer.accept();

        // Set up an input stream to receive the simulation result back from Transfer Server.
        _inFromTransferServer = new BufferedReader(new InputStreamReader(
                _transferConnection.getInputStream()));

        if (_controlClient.isConnected() && _toServer != null) {
            try {
                // Filter is not case sensitive.
                String parameterSequence = _parameterFilter;
                String[] parameters = null;

                // Split parameters by '#' . e.g. load.w#load.phi.
                String parameterDelimiter = "#";
                parameters = parameterSequence.split(parameterDelimiter);

                if (parameters.length >= 2) {
                    // The setfilter message is sent to the server to set the filter for variable(s) and parameter(s)
                    // which should send from OMI to Ptolemy II.
                    _toServer
                    .write("setfilter#3#" + parameterSequence + "#end");
                    _toServer.flush();
                } else if (parameters.length == 1) {
                    _toServer.write("setfilter#3#" + parameters[0] + "#end");
                    _toServer.flush();
                } else {
                    System.err.println("There is no filter to set!");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    new IOException(e.getMessage()).printStackTrace();
                }

                // The start message is sent to the server to start the simulation.
                _toServer.write("start#4#end");
                _toServer.flush();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    new IOException(e.getMessage()).printStackTrace();
                }
            } catch (IOException e) {
                new IOException(e.getMessage()).printStackTrace();
            }
        }
    }

    /** <p>The simulation result is displayed step by step according to the start time, stop time and number
     *  of intervals of the OpenModelica actor. The formula for calculating the step time is step time =
     *  (stop time - start time) / number of intervals. There is one issue, the simulation does not stop
     *  automatically at the stop time that is selected as one of the OpenModelica actors' parameters.
     *  So during reading the result back from Transfer server, one condition is set up to check if the stop
     *  time is reached. The simulation result according to the parameter's of the OpenModelica actor is sent
     *  in the string format to the output port of the OpenModelica actor to be displayed by Display actor.
     *  </p>
     *  <p>In addition, the Control module runs prior to running the Transfer one to get the confirmation to
     *  run the Transfer module and following running the Transfer module to get the confirmation that the
     *  simulation is shut down.</p>
     */
    @Override
    public void run() {
        // FIXME: this method should throw an exception.

        String lineDelimiter = "end";
        String timeDelimiter = "#";
        String wholeSimulationResult = null;
        String lineSimulationResult = null;
        String[] lineSplitResult = null;
        String[] timeSplitResult = null;
        String outputResult = null;
        char[] transferServerBuffer = new char[1024];
        char[] controlServerBuffer = new char[124];
        try {
            if (_inFromControlServer.read(controlServerBuffer) != -1) {
                String statusMessage = new String(controlServerBuffer).trim();
                if (statusMessage != null) {
                    String loggerInfo = "Confirmation Message from Control Server : "
                            + statusMessage;
                    _omcLogger.getInfo(loggerInfo);
                }
            }
        } catch (IOException e) {
            new Exception(e.getLocalizedMessage()).printStackTrace();
        }

        // Checking the status of Transfer socket, connection and stream.
        try {
            if (_transferServer.isBound() && _transferConnection.isConnected()
                    && _inFromTransferServer.ready()) {
                try {
                    // It returns -1 if the end of the stream is reached.
                    // If not, it returns the number of chars that have been read.
                    Outerloop: while ((_inFromTransferServer
                            .read(transferServerBuffer)) != -1) {

                        wholeSimulationResult = new String(transferServerBuffer)
                        .trim();
                        lineSplitResult = wholeSimulationResult
                                .split(lineDelimiter);
                        for (int i = 0; i < lineSplitResult.length - 1; i++) {
                            lineSimulationResult = lineSplitResult[i];
                            timeSplitResult = lineSimulationResult
                                    .split(timeDelimiter);
                            for (int j = 1; j < timeSplitResult.length; j++) {
                                if (j == 1) {
                                    // Stop the simulation when the stopTime is reached.
                                    // There is a need to have this condition, because the simulation does not
                                    // stop automatically.
                                    if ((timeSplitResult[j].toString()
                                            .startsWith(_stopTime))) {
                                        break Outerloop;
                                    } else {
                                        if (outputResult != null) {
                                            outputResult += "At time : "
                                                    + timeSplitResult[j] + " ";
                                        } else {
                                            outputResult = "At time : "
                                                    + timeSplitResult[j] + " ";
                                        }
                                    }
                                } else {
                                    outputResult += timeSplitResult[j] + " ";
                                }
                            }
                            outputResult += "\n";
                        }
                    }
                try {
                    _outputPort.send(0, new StringToken(outputResult));
                } catch (NoRoomException e) {
                    new IOException(e.getMessage()).printStackTrace();
                } catch (IllegalActionException e) {
                    new IOException(e.getMessage()).printStackTrace();
                }
                } catch (IOException e) {
                    new IOException(e.getMessage()).printStackTrace();
                }

                // FIXME THE RESULT SHOULD BE PASSED AS RECORD TOKEN.
                /*  try {
                      // It returns -1 if the end of the stream is reached.
                      // If not, it returns the number of chars that have been read.
                      Outerloop: while ((streamIndex = _inFromTransferServer
                              .read(transferServerBuffer)) != -1) {
                          wholeSimulationResult = new String(transferServerBuffer)
                          .trim();
                          lineSplitResult = wholeSimulationResult
                                  .split(lineDelimiter);
                          for (int i = 0; i < lineSplitResult.length - 1; i++) {
                              lineSimulationResult = lineSplitResult[i];
                              timeSplitResult = lineSimulationResult
                                      .split(timeDelimiter);
                              for (int j = 1; j < timeSplitResult.length; j++) {
                                  if (!(timeSplitResult[j].compareTo("") == 0)) {
                                      if (j == 1) {
                                          tempInt = (int) Double
                                                  .parseDouble(timeSplitResult[j]);
                                          if ((timeSplitResult[j].toString()
                                                  .startsWith(_stopTime)))
                                              break Outerloop;
                                          else {
                                              if (outputResult != null)
                                                  outputResult += "time" + tempInt;
                                              else
                                                  outputResult = "time" + tempInt;
                                          }
                                      } else {
                                          parameterSplitResult = timeSplitResult[j]
                                                  .split("=");
                                          int tempInt2 = (int) Double
                                                  .parseDouble(parameterSplitResult[1]);
                                          String tempResult = parameterSplitResult[0]
                                                  + tempInt2;
                                          outputResult += tempResult;
                                      }
                                  }
                              }
                          }
                      }
                  try {
                      parameterRecord = new RecordToken(outputResult);
                  } catch (IllegalActionException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  }
                  try {
                      _outputPort.send(0, parameterRecord);
                  } catch (NoRoomException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  } catch (IllegalActionException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  }
                  } catch (IOException e) {
                      e.printStackTrace();
                  }*/
                //
                try {
                    // After displaying the simulation result is done,
                    // the shutdown message is sent to the server to shut the simulation down.
                    char[] controlServerShutDownBuffer = new char[124];
                    _toServer.write("shutdown#5#end");
                    _toServer.flush();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        new IOException(e.getMessage()).printStackTrace();
                    }

                    try {
                        _inFromControlServer.read(controlServerShutDownBuffer);
                        String statusMessage = new String(
                                controlServerShutDownBuffer).trim();
                        if (statusMessage != null) {
                            String loggerInfo = "Confirmation Message from Control Server : "
                                    + statusMessage;
                            _omcLogger.getInfo(loggerInfo);
                        }
                    } catch (IOException e) {
                        new IOException(e.getMessage()).printStackTrace();
                    }
                    // Close all the sockets, streams and connections.
                    _controlClient.close();
                    _controlServer.close();
                    _toServer.close();
                    _inFromControlServer.close();
                    _transferServer.close();
                    _omcLogger.getInfo("Socket and streams are Closed!");
                } catch (IOException e) {
                    new IOException(e.getMessage()).printStackTrace();
                }
            }
        } catch (IOException e) {
            new IOException(e.getMessage()).printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Control client socket.
    private Socket _controlClient = null;

    // The connection for receiving the confirmation message back from Control Server.
    private Socket _controlConnection;

    // Control server socket.
    private ServerSocket _controlServer = null;

    // The input stream to receive the response/simulation result back from the control server.
    private BufferedReader _inFromControlServer = null;

    // The input stream to receive the simulation result back from Transfer Server.
    private BufferedReader _inFromTransferServer = null;

    // OMCLogger Object for accessing a unique source of instance.
    private OMCLogger _omcLogger = null;

    // The output port that the result of simulation should be sent to.
    private TypedIOPort _outputPort;

    // Filter for showing the result of the simulation.
    private String _parameterFilter = null;

    // Stop time of the simulation.
    private String _stopTime = null;

    // The output stream to send the request/operation from control client to the server.
    private BufferedWriter _toServer = null;

    // The connection for receiving the simulation result back from Transfer Server.
    private Socket _transferConnection;

    // Transfer Server socket.
    private ServerSocket _transferServer = null;
}

/* Methods for interfacing client using BSD sockets.
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

package lbnl.lib.openmodelica;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

/** 
    <p> UtilSocket establishes the connection between Ptolemy II and OpenModelica Interactive(OMI) and 
    handles the communication between OMI and UI server/client components by 
    creating TransferServer and ControlServer Threads. </p>

    @author Mana Mirzaei
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
 */
public class UtilSocket implements IUtilSocket {

    ///////////////////////////////////////////////////////////////////
    ////                        public methods                    ////

    /** Establish Control Client socket and set up streams for exchanging data between Control Client and Server. 
     *  @throws IOException If an I/O error occurs while setting up the connection.
     *  @throws UnknownHostException If the IP address and the host could not be determined.
     */
    public void establishSocket() throws IOException, UnknownHostException {

        try {
            _controlClient = new Socket(InetAddress.getByName("127.0.0.1"),
                    10501);

            // Set up an output stream to send the request/operation to the server. 
            _toServer = new BufferedWriter(new OutputStreamWriter(
                    _controlClient.getOutputStream()));

            System.out.println("Connected to Control Server "
                    + _controlClient.getInetAddress());

        } catch (UnknownHostException e) {
            throw new UnknownHostException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error occurs while creating the connection!"
                    + e.getMessage());
        }
    }

    /**  OpenModelica Interactive (OMI) is composed of two modules, control and transfer, for communication over TCP/IP.
     *   This network communication technology is employed in order to exchange messages between Ptolemy II and OMI server and client components. 
     *   
     *   OMI server/client components:
     *   
     *   <pre>
     *   
     *   Name            Description                                                      URL
     *   ---------------------------------------------------------------------------------------------------------
     *   Control Server  Waits for requests from the UI                                   By Default, waits for connection on: 127.0.0.1:10501
     *    ---------------------------------------------------------------------------------------------------------
     *   Control Client  Replies to the UI and sends other synchronization messages to it By Default, tries to connect on:127.0.0.1:10500
     *    ---------------------------------------------------------------------------------------------------------
     *   Transfer Client Sends simulation results to a UI                                 By Default, tries to connect on:127.0.0.1:10502
     *   
     *   
     *   Ptolemy II server/client components: UtilSocket/Control Client, TransferServer and ControlServer
     *   
     *   Name                           Description                                               URL
     *   ---------------------------------------------------------------------------------------------------------
     *   Control Client/UtilSocket      Requests to the OMI Control Server                        By Default, tries to connect on:127.0.0.1:10501
     *    ---------------------------------------------------------------------------------------------------------
     *   Control Server/ControlServer   Waits for information from the OMI Control Client         By Default, waits for connection on: 127.0.0.1:10500
     *   ---------------------------------------------------------------------------------------------------------
     *   Transfer Server/TransferServer Waits for simulation results from the OMI Transfer Client By Default, waits for connection on: 127.0.0.1:10502
     *   
     *   </pre>
     *  
     *   @throws IOException If an I/O error occurs while client/server exchanging data.
     */
    public void exchangeData() throws IOException {

        if (_controlClient.isConnected()) {
            if (_toServer != null) {
                try {
                    // Get input from user using GUI.
                    String _clientRequest = JOptionPane
                            .showInputDialog("Request to the Control Server: ");

                    if (_clientRequest != null) {
                        _toServer.write(_clientRequest);
                        _toServer.flush();
                        System.out
                                .println(_clientRequest
                                        + " sent to the the Control Server successfully!");
                    }

                    ControlServer _controlThread = new ControlServer(_toServer);
                    TransferServer _transferThread = new TransferServer(_toServer);

                    _controlThread.start();

                    synchronized (_controlThread) {
                        try {
                            _controlThread.wait(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            throw new IOException(
                                    "Error occurs while ControlThread is waiting! "
                                            + e.getMessage());
                        }
                    }

                    _transferThread.start();

                    synchronized (_transferThread) {
                        try {
                            _transferThread.wait(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            throw new IOException(
                                    "Error occurs while TransferThread is waiting! "
                                            + e.getMessage());
                        }
                    }
                    _toServer.close();
                    try {
                        if (_controlClient.isConnected()) {
                            _controlClient.close();
                        }
                        System.out.println("Socket is closed!");
                    } catch (IOException e) {
                        throw new IOException(e.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }
            } else
                throw new IOException("Null Stream!");
        } else
            throw new IOException(
                    "No connection to Control Server's port/10501 !");
    }

    /** Create an instance of UtilSocket object in order to provide a global point of access to the instance.
     *  It provides a unique source of the UtilSocket instance.
     *  @return _utilSocket The UtilSocket object representing the instance value.
     */
    public static UtilSocket getInstance() {
        if (_utilSocket == null)
            _utilSocket = new UtilSocket();
        return _utilSocket;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    // Control Client socket.
    private Socket _controlClient = null;

    // An output stream to send the request from Control Client to Control Server.
    private BufferedWriter _toServer = null;

    // UtilSocket Object for accessing a unique source of instance.
    private static UtilSocket _utilSocket = null;
}

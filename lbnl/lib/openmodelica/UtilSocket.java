/* Methods for interfacing clients using BSD sockets.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**    
    <p> UtilSocket establishes the connection from the client to the middleware by <i>establishclientsocket</i>.  
    Then, <i>exchangewithsocket</i> writes data to the socket and reads data from the socket. 
    Finally, the connection is closed by <i>closesocket</i>. </p>

    @author Mana Mirzaei
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
 */
public class UtilSocket implements IUtilSocket {

    ///////////////////////////////////////////////////////////////////
    ////                        public methods                    ////

    /** Close the socket.
     *  @throws IOException If an I/O error occurs at the time of closing the socket.
     */
    public void closeSocket() throws IOException {

        try {
            if (_clientSocket.isConnected())
                _clientSocket.close();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        System.out.println("Socket is closed.");
    }

    /** Establish the client socket. Set up streams for exchanging data between the client and the server. 
     *  @throws IOException If an I/O error occurs when creating the socket.
     *  @throws UnknownHostException If the IP address of the host could not be determined.
     */
    public void establishSocket() throws IOException,
    UnknownHostException {

        // The name of the host.
        String hostName = "localhost";

        // Port number of the OMC server. 10500->10502
        int portNo = 10501;

        try {
            // Set up a connection.
            _clientSocket = new Socket(hostName,portNo);

            System.out
            .println("Connected to " + _clientSocket.getInetAddress());

            // Set up an input stream to receive the response/simulation result back from the server.
            _fromServer = new BufferedReader(new InputStreamReader(
                    _clientSocket.getInputStream()));

            // Set up an output stream to send the request/operation to the server. 
            _toServer = new BufferedWriter(new OutputStreamWriter(
                    _clientSocket.getOutputStream()));
        } catch (UnknownHostException e) {
            throw new UnknownHostException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(
                    "An I/O error occurs when creating the socket!"
                            + e.getMessage());
        }
    }

    /**  There are two modules, control and transfer, in the OpenModelica Interactive (OMI) which are designated for a communication over TCP/IP.
     *   This network communication technology is used to send and receive messages between the client, Ptolemy II and the server, OMC. 
     *
     *   Control module is the interface between OMI and a UI that is implemented as a single thread to support parallel 
     *   tasks and independent reactivity. Control module is considered the major controlling and communication 
     *   instance at simulation initialization phase besides managing simulation properties during simulation runtime. 
     *   A client permanently sends operations as messages to the Control unit. It also reacts to the feedback from other 
     *   internal OMI components and sends messages back to the client, such as error or status messages. 
     *  
     *   Transfer module gets simulation results from a result manager and sends them to the UI upon starting a simulation. 
     *   This module employs filter mask property containing names of all properties whose result values are significant to the UI.
     *
     *   The following commands are available message from the UI to OMI(Request-Reply):
     *   
     *   ---------------------------------------------------------------------------------------------------------
     *   UI Request                     Description                            OMI::Control Reply
     *   ---------------------------------------------------------------------------------------------------------
     *   start#SEQ#end                 Starts or continues the simulation      done#SEQ#end
     *   ---------------------------------------------------------------------------------------------------------
     *   pause#SEQ#end                 Pauses the running simulation           done#SEQ#end
     *   ---------------------------------------------------------------------------------------------------------
     *   changevalue#1#load.w=2.3#end  Change the value of the appended 
     *                                 parameters and sets the simulation 
     *                                 time back to the point where the user 
     *                                 clicked in the UI.
     *   ---------------------------------------------------------------------------------------------------------
     *                                 
     *  The following commands are available messages from OMI::Transfer to UI. TODO COMPLETE AFTER FIXING THE CODE - GETTING BACK THE RESULT BACK FROM 
     *  SERVER TO THE UI.
     *                                 
     *   ---------------------------------------------------------------------------------------------------------                              
     *   OMI::Transfer                   Description                                       UI
     *   ---------------------------------------------------------------------------------------------------------                            
     *   result#ID#Tn#                   
     *   var1=Val:var2=Val# 
     *   par1=Val:par2=Val# 
     *   end
     *  ---------------------------------------------------------------------------------------------------------
     *   result#ID#Tn# 
     *   1=Val:2=Val# 
     *   1=Val:2=Val# 
     *   end
     *   ---------------------------------------------------------------------------------------------------------
     *          
     *   For the time being, the interactive simulation hangs after 1.958, OpenModelica Developer team are trying to fix this issue.
     *   @throws IOException If an I/O error occurs when client and server are exchanging the message.
     */
    public void exchangeData() throws IOException {

        if (_clientSocket.isConnected()) {
            if (_toServer != null && _fromServer != null) {
                try {
                    System.out.println("Enter the request : ");

                    // FIXME WHY GETTING INPUT FROM KEYBOARD IS NOT WORKING. Get input from the user to send to the omc server.
                    // TODO ASK IF THERE IS A NEED TO CHECK IF THE COMMAND COMPLIES WITH THE COMMAND PROTOCOL - LIKE LETTER CASE.

                    // Set up an input stream to get the request/operation from the client.
                    // Scanner _fromClient = new Scanner(System.in);
                    // String _clientRequest = _fromClient.next();

                    String _clientRequest = "\"start#1#end\"";

                    if (_clientRequest != null) {
                        System.out.println("Request from the client :  "
                                + _clientRequest);
                        _toServer.write(_clientRequest);
                        _toServer.flush();
                        System.out.println(_clientRequest
                                + " sent to the server!");
                    }

                    System.out
                    .println("Waiting for the response from the omc server :  ");

                    // int bufferSize = _clientSocket.getReceiveBufferSize() // result 8192 
                    char[] serverBuffer = new char[8192];

                    // FIXME IT IS NOT POSSIBLE TO HAVE THE RESPONSE BACK FROM THE OMC SERVER.
                    // Read simulation result from the server char by char until there is no char to read and write them to the console.
                    int i = 0;
                    while (_fromServer.read(serverBuffer) != -1) {
                        // for (char readChar : serverBuffer) OR serverBuffer[i]
                        System.out.println("Response from the server : "
                                + serverBuffer[i]);
                        i ++;
                    }

                    // Close the streams.
                    _toServer.close();
                    _fromServer.close();
                    System.out.println("Streams closed!");
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }
            } else
                throw new IOException("Null Stream!");
        } else
            throw new IOException("Not connected to the omc server!");
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
    // The client socket.
    private Socket _clientSocket = null;

    // Set up an input stream to receive the response/simulation result back from the server.
    private BufferedReader _fromServer = null;

    // Set up an output stream to send the request/operation to the server.
    private BufferedWriter _toServer = null;

    // UtilSocket Object for accessing a unique source of instance.
    private static UtilSocket _utilSocket = null;
}

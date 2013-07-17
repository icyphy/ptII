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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import ptolemy.util.StringUtilities;

/**    
     This file provides methods that allow clients to
     establish a socket connection. Clients typically call
     the method establishclientsocket() once, 
     and then call the method exchangewithsocket().
     At the end, a client call
     closesocket() to close the socket connection.
     TODO COMPLETE THE DESCRIPTION.

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
     *  @throws IOException
     */
    public void closesocket() throws IOException {

        try {
            if (_clientSocket.isConnected()) 
                _clientSocket.close();
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }

        System.out.println("Client Socket is closed.");
    }

    /** Establish the client socket.
     *  Set up input/output streams for exchanging data between the server and the client. 
     *  @throws IOException 
     */
    public boolean establishclientsocket() throws IOException {

        try {
            // Create a connection.
            _clientSocket = new Socket(_hostName, _portNo);

            System.out
            .println("Connected to " + _clientSocket.getInetAddress());

            // Set up an input stream to receive the response/simulation result back from the server.
            _fromServer = new Scanner(_clientSocket.getInputStream());

            // Set up an output stream to send the request/operation to the server. 
            _toServer = new PrintWriter(new OutputStreamWriter(
                    _clientSocket.getOutputStream()));

            // Set up an input stream to send the request/operation from the client.
            _fromClient = new BufferedReader(new InputStreamReader(System.in));

            //_clientSocket.setSoTimeout(_timeOut);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + _hostName);
            StringUtilities.exit(1);
            return false;
        } catch (IOException e) {
            System.err.println("Couldn't get IO for the connection to: "
                    + _hostName + " !");
            StringUtilities.exit(1);
            return false;
        }

        return true;
    }

    /** Exchange data through the BSD socket.
     *  TODO COMPLETE THE DETAILS
     * @throws Exception 
     */
    public void exchangewithsocket() throws Exception {

        // Server is running since there is a connection.        
        if (_clientSocket.isConnected()) {
            if (_toServer != null && _fromServer != null) {
                try {

                    // In parallel, read the user's input and pass it on to the omc server.
                    // The user's inputs are the following commands:
                    // The basic functions such as start,pause and changevalue of parameter only work now.
                    // The interactive simulation hangs after 1.958. 

                    // Command no.1 : Start the simulation : "start#1#end".
                    // Command no.2 : Pause the simulation : "pause#3#end".
                    // Command no.3 : Change the value of the appended parameters and 
                    // sets the simulation time back to the point where the user clicked in the UI : 
                    // "changevalue#1#load.w=2.3#end".

                    // TODO CHECK FOR MESSAGE PROTOCOL - small/capital case

                    System.out
                    .println("Enter the operation to send to the omc server : ");

                    // FIXME Get operation from the user.
                    //String sentOperation = _fromClient.readLine();
                    String sentOperation = "start#1#end";

                    if (sentOperation != null) {
                        System.out.println("From Client :  " + sentOperation);
                        _toServer.println(sentOperation);
                        if (!_toServer.checkError())
                            System.out.println("Sent " + sentOperation
                                    + " operation to the server!");
                        else
                            throw new Exception(
                                    "There is an error in writting the request on an outputstream of the client!");
                    }

                    // Read characters from the server until the
                    // stream closes and write them to the console.

                    // FIXME HANG HERE
                    while (_fromServer.hasNextLine()) {

                        System.out.println("From server :  "
                                + _fromServer.nextLine());

                        // TODO : CHECK IF THERE IS AN ERROR IN THE RESPONSE OR NOT
                    }

                } catch (IOException e) {
                    System.err.println("Failed to exchange: " + e);
                } finally {
                    _toServer.close();
                    _fromServer.close();
                    System.out.println("Closed all streams!");
                }
            } else
                System.out.println("inputstream/outputstream is null!");
        } else
            System.out.println("Not connected to the server!");
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

    // Set up an input stream to send the request/operation from the client.
    private BufferedReader _fromClient = null;

    // Set up an input stream to receive the response/simulation result back from the server.
    private Scanner _fromServer = null;

    // The name of the host.
    private String _hostName = "127.0.0.1";

    // Port number of the OMC server. 10500-10502
    private int _portNo = 10501;

    // 100 sec wait period.
    // private final int _timeOut = (int) TimeUnit.SECONDS.toMillis(100);

    // Set up an output stream to send the request/operation to the server.
    private PrintWriter _toServer = null;

    // UtilSocket Object for accessing a unique source of instance.
    private static UtilSocket _utilSocket = null;
}

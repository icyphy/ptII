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
import java.net.ServerSocket;
import java.net.Socket;

/**    
     This file provides methods that allow clients to
     establish a socket connection. Clients typically call
     the method establishclientsocket() once, 
     and then call the method exchangewithsocket().
     At the end, a client should call
     closesocket() to close the socket connection.
          
    @author Mana Mirzaei
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class UtilSocket implements IUtilSocket {

    ///////////////////////////////////////////////////////////////////
    ////                        public methods                    ////

    /** Establish the client socket.
     *  @throws IOException 
     */
    public void establishclientsocket() throws IOException {

        // Get the socket port number.

        System.out.println("Getting socket port number: "  + _portNo);

        // Create the communication BSD socket.

        try {
            _clientSocket = new Socket(_hostName, _portNo);
        } catch (IOException e) {
            throw new IOException("Unable to create socket.");
        }
        
        System.out.println("Socket is opened.");
    }

    /** Exchange data through the BSD socket.
     *  @throws IOException 
     */
    public void exchangewithsocket() throws IOException {

        ServerSocket server = new ServerSocket(_portNo);

        //  FIXME Wait for connection from client.
        _clientSocket = server.accept();

        // Set up streams for reading from and writing to the server.
        
        final BufferedReader from_server = new BufferedReader(
                new InputStreamReader(_clientSocket.getInputStream()));

        BufferedWriter to_server = new BufferedWriter(new OutputStreamWriter(
                _clientSocket.getOutputStream()));

        // Set up streams for reading from and writing to the console.

        //FIXME
        BufferedReader from_user = new BufferedReader(new InputStreamReader(
                _clientSocket.getInputStream()));

        final BufferedWriter to_user = new BufferedWriter(
                new OutputStreamWriter(System.out));

        // Create a thread that gets output from the server and display
        // it to the user. 
        Thread serverToUser = new Thread() {
            public void run() {
                char[] buffer = new char[1024];
                int chars_read;

                // Read characters from the server until the
                // stream closes, and write them to the console.
                try {
                    while ((chars_read = from_server.read(buffer)) != -1) {
                        to_user.write(buffer, 0, chars_read);
                        to_user.flush();
                    }
                } catch (IOException e) {
                    // FIXME
                    // throw new IOException("");
                }

            }
        };

        // Start the server-to-user thread 
        serverToUser.start();

        // Read the user's input and pass it on to the server.
        String line;
        while ((line = from_user.readLine()) != null) {
            to_server.write(line + "\r\n");
            to_server.flush();
        }

        server.close();
        from_server.close();
        to_server.close();
        from_user.close();
        to_user.close();
    }

    /** Create an instance of UtilSocket object in order to provide a global point of access to the instance.
     *  It provides a unique source of the UtilSocket instance.
     *  @return The UtilSocket object representing the instance value.
     */
    public static UtilSocket getInstance() {
        if (_utilSocket == null)
            _utilSocket = new UtilSocket();
        return _utilSocket;
    }

    /** Close the socket and 
     *  the server-to-user thread is stopped.
     *  @throws IOException
     */
    public void closesocket() throws IOException {
        _clientSocket.close();
        System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    // The name of the socket for establishing the connection.
    private Socket _clientSocket = null;
    
    // The name of the host.
    private String _hostName = "localhost";

    // Port number of OMC server.
    private int _portNo = 10500;

    // UtilSocket Object for accessing a unique source of instance.
    private static UtilSocket _utilSocket = null;
}
